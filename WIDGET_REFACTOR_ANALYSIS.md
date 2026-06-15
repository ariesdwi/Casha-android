# Casha Android Widget — Refactoring Analysis & Recommendations

## Executive Summary

This document provides a comprehensive analysis of the current widget implementation and proposes a professional refactoring plan to improve UI design, data flow architecture, and synchronization reliability.

**Date**: June 8, 2026  
**Current Status**: Functional but needs UI modernization and data sync improvements  
**Refactor Goal**: Professional UI + Reliable Real-time Data Flow + Better Performance

---

## 1. Current Implementation Analysis

### 1.1 Architecture Overview

**✅ Strengths:**
- Well-structured data models with clear separation of concerns
- Proper use of Jetpack Glance for modern Compose-based widgets
- Good state management with 5 distinct states (NORMAL, LOGGED_OUT, NO_DATA, NOT_PREMIUM, HIDDEN)
- SharedPreferences for data sharing between app and widgets
- WorkManager for periodic refresh (15 min interval)
- Comprehensive widget coverage (Home + Lock screen widgets)

**⚠️ Weaknesses:**
- **Data sync is passive**: Relies on periodic refresh rather than real-time updates
- **No immediate feedback**: After transactions, widgets may show stale data until next refresh cycle
- **Limited error handling**: No retry logic or failure recovery mechanisms
- **Manual update calls**: App needs to explicitly trigger widget updates after data changes
- **Currency formatting utility is basic**: Could be more sophisticated with locale support

---

### 1.2 File Structure Review

```
app/src/main/java/com/casha/app/widget/
├── data/
│   ├── WidgetState.kt ✅              // Clean enum with 5 states
│   ├── WidgetSummary.kt ✅            // Complete data model matching spec
│   ├── SpendStatus.kt ✅              // Status enum with color mapping
│   └── WidgetPreferences.kt ✅        // SharedPreferences wrapper
├── WidgetUpdater.kt ⚠️                // Central update utility (needs improvement)
├── worker/
│   └── WidgetRefreshWorker.kt ⚠️      // Periodic refresh (could be smarter)
├── ui/
│   ├── HomeSmallWidget.kt ⚠️          // UI needs modernization
│   ├── HomeMediumWidget.kt ⚠️         // UI needs modernization
│   ├── LockCircularWidget.kt ⚠️       // Basic implementation, could be enhanced
│   └── LockRectangularWidget.kt ⚠️    // Basic implementation, could be enhanced
├── ui/
│   └── DeepLinkAction.kt ✅           // Clean deep link handler
└── util/
    └── WidgetCurrencyFormatter.kt ⚠️  // Basic but functional
```

**Legend:**
- ✅ = Well implemented, minimal changes needed
- ⚠️ = Functional but needs improvement
- ❌ = Critical issues (none found)

---

### 1.3 Data Flow Analysis

#### Current Flow:
```
1. App fetches data from API
     ↓
2. App saves to SharedPreferences (WidgetPreferences)
     ↓
3. App manually calls WidgetUpdater.refresh()
     ↓
4. WidgetUpdater triggers AppWidgetManager.notifyAppWidgetViewDataChanged()
     ↓
5. Widget reads from SharedPreferences
     ↓
6. Widget renders UI

PLUS: WorkManager triggers refresh every 15 minutes
```

#### Issues Identified:

**1. Delayed Updates**
- Widgets show stale data between manual refreshes
- 15-minute periodic refresh is too slow for real-time experience
- No immediate visual feedback after user actions

**2. Missing Update Triggers**
- Not all transaction operations trigger widget updates
- Budget changes may not immediately reflect in widgets
- Login/logout state changes need explicit updates

**3. No Network Awareness**
- Widgets don't adapt to network availability
- Failed refreshes have no retry mechanism
- No indication when data is outdated

**4. Memory Inefficiency**
- All widgets refresh simultaneously even if not visible
- No optimization for battery or performance

---

### 1.4 UI/UX Analysis

#### Home Screen Widgets

**HomeSmallWidget (2×2) - Current Issues:**
1. Layout feels cramped with too much information
2. Font sizes are small and hard to read at a glance
3. No visual hierarchy - all elements compete for attention
4. Progress bar is thin (3dp) and hard to see
5. Status dot is tiny (7dp) and loses impact
6. Colors lack depth - flat design feels dated
7. No padding/spacing consistency

**HomeMediumWidget (4×2) - Current Issues:**
1. Two-column layout is unbalanced (budget vs buttons)
2. Quick action buttons feel disconnected from budget info
3. Circular budget ring is small (48dp) and hard to read
4. Button icons are not aligned or sized consistently
5. No visual separation between sections
6. Background is plain white with sharp corners

#### Lock Screen Widgets

**LockCircularWidget - Current Issues:**
1. Too much text in small space ("BUDGET" + percentage)
2. Outer ring visual is basic without depth
3. Status color only shows in ring, not prominent enough
4. Falls back to emojis which look unprofessional

**LockRectangularWidget - Current Issues:**
1. Text is too small for lock screen viewing distance
2. Progress bar is very thin (3dp)
3. Layout feels cramped with 4 rows
4. Fallback states use emojis instead of proper icons
5. No visual polish or gradient effects

---

### 1.5 Code Quality Review

**Data Models (WidgetState, WidgetSummary, SpendStatus)** - ✅ Excellent
- Clean Kotlin data classes
- Proper serialization support
- Well-documented
- Type-safe enums

**WidgetPreferences** - ✅ Good
- Clean abstraction over SharedPreferences
- JSON serialization handled properly
- Simple and effective

**WidgetUpdater** - ⚠️ Needs Improvement
```kotlin
// Current implementation is too simple
object WidgetUpdater {
    fun refresh(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        // Updates ALL widgets indiscriminately
        val homeSmallIds = appWidgetManager.getAppWidgetIds(...)
        val homeMediumIds = appWidgetManager.getAppWidgetIds(...)
        // ... etc
    }
}
```

**Issues:**
- No selective update (updates all widgets even if not needed)
- No error handling
- No logging or diagnostics
- Synchronous operation blocks caller

**WidgetRefreshWorker** - ⚠️ Basic Implementation
```kotlin
class WidgetRefreshWorker(context: Context, params: WorkerParameters) 
    : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        WidgetUpdater.refresh(applicationContext)
        return Result.success()
    }
}
```

**Issues:**
- No network check before attempting refresh
- No retry logic on failure
- No exponential backoff
- No constraints (requires WiFi, battery, etc.)
- Always returns success even if refresh failed

**Widget UI Components** - ⚠️ Functional but Dated
- Uses basic Glance composables without advanced features
- No use of Material Design 3 principles
- Hardcoded colors instead of theme system
- No dark mode support
- Limited accessibility considerations

---

## 2. Refactoring Plan

### 2.1 Data Flow Architecture Improvements

#### Phase 1: Real-Time Update System

**Goal**: Eliminate delay between app actions and widget updates

**Implementation:**

1. **Event-Driven Architecture**
```kotlin
// New: WidgetUpdateCoordinator
object WidgetUpdateCoordinator {
    private val _updateEvents = MutableSharedFlow<WidgetUpdateEvent>()
    
    sealed class WidgetUpdateEvent {
        object TransactionAdded : WidgetUpdateEvent()
        object BudgetChanged : WidgetUpdateEvent()
        object BalanceUpdated : WidgetUpdateEvent()
        object LoginStateChanged : WidgetUpdateEvent()
        object PremiumStateChanged : WidgetUpdateEvent()
    }
    
    fun emitUpdate(event: WidgetUpdateEvent) {
        _updateEvents.tryEmit(event)
    }
}
```

2. **Smart Update Dispatcher**
```kotlin
// Replaces current WidgetUpdater
class WidgetUpdateDispatcher(private val context: Context) {
    
    suspend fun updateWidgets(
        event: WidgetUpdateEvent,
        force: Boolean = false
    ) {
        // Selective update based on event type
        when (event) {
            is TransactionAdded -> updateBudgetWidgets()
            is BalanceUpdated -> updateBalanceWidgets()
            // ... specific updates
        }
    }
    
    private suspend fun updateBudgetWidgets() {
        // Only update widgets that show budget data
        // Skip if widget is not visible (optimize battery)
    }
}
```

3. **Integration Points**
- TransactionViewModel: Emit event after save
- BudgetViewModel: Emit event after budget changes
- AuthManager: Emit event on login/logout
- SubscriptionManager: Emit event on premium state change

**Benefits:**
- Immediate widget updates after user actions
- Battery-efficient (selective updates)
- Decoupled architecture
- Easy to debug and maintain

---

#### Phase 2: Intelligent Worker System

**Goal**: Smarter background refresh with network awareness and retry logic

**Implementation:**

```kotlin
class SmartWidgetRefreshWorker(
    context: Context, 
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // Check network connectivity
        if (!isNetworkAvailable()) {
            return Result.retry()
        }
        
        // Check if data is stale (last update > 15 min)
        val lastUpdate = WidgetPreferences.getLastUpdatedAt(applicationContext)
        if (!isDataStale(lastUpdate)) {
            return Result.success() // Skip unnecessary work
        }
        
        return try {
            // Fetch fresh data from API
            val summary = fetchWidgetData()
            
            // Save to preferences
            WidgetPreferences.saveWidgetSummary(applicationContext, summary)
            
            // Update widgets
            WidgetUpdateDispatcher(applicationContext)
                .updateWidgets(WidgetUpdateEvent.PeriodicRefresh)
            
            Result.success()
        } catch (e: Exception) {
            Log.e("WidgetWorker", "Refresh failed", e)
            Result.retry() // Automatic retry with backoff
        }
    }
    
    private fun isDataStale(lastUpdate: Instant?): Boolean {
        lastUpdate ?: return true
        return Duration.between(lastUpdate, Instant.now()).toMinutes() > 15
    }
}
```

**Worker Constraints:**
```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true) // Don't drain battery
    .build()

val workRequest = PeriodicWorkRequestBuilder<SmartWidgetRefreshWorker>(
    repeatInterval = 15,
    repeatIntervalTimeUnit = TimeUnit.MINUTES
)
    .setConstraints(constraints)
    .setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,
        WorkRequest.MIN_BACKOFF_MILLIS,
        TimeUnit.MILLISECONDS
    )
    .build()
```

**Benefits:**
- Network-aware (don't waste retries without connectivity)
- Battery-efficient (respects battery state)
- Self-healing (retry logic with exponential backoff)
- Skip unnecessary work (check staleness first)

---

#### Phase 3: Data Sync Observer

**Goal**: Monitor app database changes and auto-update widgets

**Implementation:**

```kotlin
class WidgetSyncObserver(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val context: Context
) {
    
    init {
        // Observe transaction changes
        transactionDao.observeAll()
            .onEach { transactions ->
                WidgetUpdateCoordinator.emitUpdate(
                    WidgetUpdateEvent.TransactionAdded
                )
            }
            .launchIn(scope)
        
        // Observe budget changes
        budgetDao.observeAll()
            .onEach { budgets ->
                WidgetUpdateCoordinator.emitUpdate(
                    WidgetUpdateEvent.BudgetChanged
                )
            }
            .launchIn(scope)
    }
}
```

**Initialize in Application class:**
```kotlin
class CashaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize widget sync observer
        val database = CashaDatabase.getDatabase(this)
        WidgetSyncObserver(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao(),
            context = this
        )
    }
}
```

**Benefits:**
- Automatic synchronization
- No manual update calls needed
- Real-time consistency between app and widgets
- Works even if app is in background

---

### 2.2 UI/UX Modernization

#### Design Principles

1. **Material Design 3 (Material You)**
   - Dynamic color system
   - Elevation and shadows
   - Rounded corners (16dp radius)
   - Proper spacing (8dp grid)

2. **Visual Hierarchy**
   - Large, bold primary information
   - Secondary information in smaller, muted text
   - Clear sections with proper separation

3. **Accessibility**
   - Minimum text size 12sp
   - High contrast ratios (WCAG AA)
   - Touch targets minimum 48dp
   - Semantic descriptions for screen readers

4. **Professional Polish**
   - Subtle gradients and shadows
   - Smooth animations (where supported)
   - Consistent icon style
   - Premium feel without clutter

---

#### HomeSmallWidget Redesign (2×2)

**Before:**
- Cramped layout
- Small text
- Thin progress bar
- Flat design

**After: Modern Card Design**

```
┌─────────────────────────────┐
│ ⚡ Safe Spend      🟢 Aman   │
│                             │
│      Rp250.000             │
│                             │
│                             │
│ Spent Rp75rb               │
│ ████████████████░░░░        │ (6dp height)
│                             │
│        12 days left         │
└─────────────────────────────┘
```

**Key Improvements:**
1. **Larger Amount**: 24sp → 28sp bold (primary focus)
2. **Better Spacing**: Consistent 12dp padding
3. **Thicker Progress Bar**: 3dp → 6dp (more visible)
4. **Gradient Background**: Subtle white → light gray gradient
5. **Rounded Corners**: 16dp corner radius
6. **Bigger Status Badge**: 9dp circle instead of 7dp
7. **Clearer Typography**: Heavier font weights

**Visual Specs:**
- Container: Rounded rectangle, 16dp radius, subtle shadow
- Background: Linear gradient from top (#FFFFFF) to bottom (#F8F8F8)
- Primary text (amount): 28sp, ExtraBold, Black color
- Secondary text: 12sp, Medium, 70% opacity
- Progress bar: 6dp height, rounded ends, status color with 20% opacity background
- Padding: 12dp all sides
- Status badge: 9dp circle with status color

---

#### HomeMediumWidget Redesign (4×2)

**Before:**
- Unbalanced two-column layout
- Small budget ring
- Disconnected action buttons

**After: Unified Dashboard Design**

```
┌────────────────────────────────────────────────┐
│ ⚡ Safe Spend Today               🟢 Comfortable│
│                                                │
│ Rp250.000           ╭───────╮                 │
│                     │  45%  │                 │
│ Spent Rp75rb        │BUDGET │                 │
│ ████████░░░░        ╰───────╯                 │
│                     12 days left              │
│ ─────────────────────────────────────────────  │
│  [✨ AI Chat]  [📊 Report]  [📈 Budget]      │
└────────────────────────────────────────────────┘
```

**Key Improvements:**
1. **Single Column Layout**: Budget info at top, actions at bottom
2. **Larger Budget Ring**: 48dp → 64dp (more readable)
3. **Better Button Design**: Full-width row of 3 equal buttons
4. **Clear Separation**: Divider line between sections
5. **More Information**: Room for additional insights
6. **Unified Theme**: Cohesive design language

**Button Design:**
- Width: (container width - 24dp spacing) / 3
- Height: 40dp
- Corner radius: 12dp
- Icon size: 20dp
- Icon + text layout (horizontal)
- Colors: Brand blue (#3389E6), Purple (#7F77DD), Green (#2E7D32)

---

#### LockCircularWidget Redesign

**Before:**
- Text too small
- Basic ring design

**After: Premium Gauge Design**

```
    ╭─────────╮
   │  ╱───╲   │
  │  │ 45% │  │  ← Gradient arc from green to status color
  │  │BUDGET│  │
   │  ╲───╱   │
    ╰─────────╯
```

**Key Improvements:**
1. **Gradient Arc**: Smooth gradient from starting color to status color
2. **Larger Text**: 12sp percentage (was 11sp)
3. **Shadow Effect**: Subtle inner shadow on arc
4. **Better Fallback Icons**: Use Material Icons instead of emojis
5. **Thicker Arc**: 6dp stroke width (was 4dp implied)

---

#### LockRectangularWidget Redesign

**Before:**
- Text too small
- Thin progress bar
- Cramped layout

**After: Professional Bar Widget**

```
┌────────────────────────────────────────────────┐
│  SAFE SPEND TODAY          [🟢 Comfortable]    │
│  Rp250.000                                     │
│  ██████████████████░░░░░░░                     │ (5dp height)
│  45% of budget used • 12 days remaining        │
└────────────────────────────────────────────────┘
```

**Key Improvements:**
1. **Larger Amount**: 16sp bold (was 14sp)
2. **Thicker Progress Bar**: 5dp height (was 3dp)
3. **Better Spacing**: 8dp between rows
4. **Proper Icons**: Material Icons instead of emojis in fallback states
5. **Clearer Labels**: More descriptive footer text

**Fallback State Redesign:**
```
┌────────────────────────────────────────────────┐
│  [Person Icon]  Not Logged In                  │
│                 Tap to open Casha              │
└────────────────────────────────────────────────┘
```

---

### 2.3 Technical Improvements

#### 1. Theme System

**Current**: Hardcoded colors throughout
**New**: Centralized theme system

```kotlin
object WidgetTheme {
    // Brand Colors
    val CashaGreen = Color(0xFF2E7D32)
    val CashaRed = Color(0xFFF44336)
    val CashaOrange = Color(0xFFFF9800)
    val CashaPurple = Color(0xFF7F77DD)
    val CashaBlue = Color(0xFF3389E6)
    
    // Status Colors (from SpendStatus)
    fun getStatusColor(status: SpendStatus): Color = when (status) {
        SpendStatus.COMFORTABLE -> CashaGreen
        SpendStatus.CAUTION -> CashaOrange
        SpendStatus.OVER_BUDGET -> CashaRed
        else -> Color.Gray
    }
    
    // Text Colors
    val TextPrimary = Color.Black
    val TextSecondary = Color.Black.copy(alpha = 0.7f)
    val TextTertiary = Color.Black.copy(alpha = 0.5f)
    
    // Background Colors
    val BackgroundLight = Color(0xFFF8F8F8)
    val BackgroundCard = Color.White
    
    // Shapes
    val CornerRadiusLarge = 16.dp
    val CornerRadiusMedium = 12.dp
    val CornerRadiusSmall = 8.dp
}
```

#### 2. Enhanced Currency Formatter

```kotlin
object WidgetCurrencyFormatter {
    
    fun formatShort(
        amount: Double, 
        currency: String = "IDR",
        locale: Locale = Locale.getDefault()
    ): String {
        val symbol = currencySymbol(currency)
        val absAmount = abs(amount)
        
        return when (currency) {
            "IDR" -> when {
                absAmount >= 1_000_000_000 -> "${symbol}${formatDecimal(absAmount / 1_000_000_000, locale)}M"
                absAmount >= 1_000_000 -> "${symbol}${formatDecimal(absAmount / 1_000_000, locale)}jt"
                absAmount >= 1_000 -> "${symbol}${formatDecimal(absAmount / 1_000, locale)}rb"
                else -> "${symbol}${absAmount.toLong()}"
            }
            else -> when {
                absAmount >= 1_000_000_000 -> "${symbol}${formatDecimal(absAmount / 1_000_000_000, locale)}B"
                absAmount >= 1_000_000 -> "${symbol}${formatDecimal(absAmount / 1_000_000, locale)}M"
                absAmount >= 1_000 -> "${symbol}${formatDecimal(absAmount / 1_000, locale)}k"
                else -> "${symbol}${absAmount.toLong()}"
            }
        }
    }
    
    fun formatFull(
        amount: Double, 
        currency: String = "IDR",
        locale: Locale = Locale.getDefault()
    ): String {
        val numberFormat = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = Currency.getInstance(currency)
        }
        return numberFormat.format(amount)
    }
    
    private fun formatDecimal(value: Double, locale: Locale): String {
        val formatter = DecimalFormat("#.#", DecimalFormatSymbols(locale))
        return formatter.format(value)
    }
}
```

#### 3. Accessibility Support

```kotlin
// Add content descriptions for screen readers
GlanceModifier.semantics {
    contentDescription = when (state) {
        WidgetState.NORMAL -> {
            "Safe spend today is ${formatFull(summary.safeSpendToday)}, " +
            "you've spent ${formatFull(summary.spentToday)}, " +
            "status is ${summary.statusLabel}, " +
            "${summary.daysRemaining} days remaining in month"
        }
        WidgetState.LOGGED_OUT -> "Not logged in, tap to open Casha"
        WidgetState.NOT_PREMIUM -> "Premium feature, tap to upgrade"
        // ... other states
    }
}
```

#### 4. Error Handling & Logging

```kotlin
object WidgetLogger {
    private const val TAG = "CashaWidget"
    
    fun logUpdate(widgetType: String, success: Boolean, error: Throwable? = null) {
        if (success) {
            Log.d(TAG, "Updated $widgetType successfully")
        } else {
            Log.e(TAG, "Failed to update $widgetType", error)
        }
    }
    
    fun logDataFetch(success: Boolean, dataAge: Duration? = null) {
        if (success) {
            Log.d(TAG, "Widget data fetched, age: ${dataAge?.toMinutes()}min")
        } else {
            Log.w(TAG, "Widget data fetch failed")
        }
    }
}
```

---

## 3. Implementation Roadmap

### Phase 1: Foundation (Week 1)
- [ ] Create WidgetUpdateCoordinator event system
- [ ] Implement WidgetUpdateDispatcher (replace WidgetUpdater)
- [ ] Add WidgetTheme centralized colors
- [ ] Enhance WidgetCurrencyFormatter with locale support
- [ ] Add WidgetLogger utility

### Phase 2: Smart Worker (Week 1-2)
- [ ] Refactor WidgetRefreshWorker with network awareness
- [ ] Add retry logic with exponential backoff
- [ ] Implement staleness check
- [ ] Add proper error handling
- [ ] Test worker constraints

### Phase 3: UI Modernization - Home Widgets (Week 2)
- [ ] Redesign HomeSmallWidget with new visual specs
- [ ] Redesign HomeMediumWidget with unified layout
- [ ] Apply WidgetTheme colors
- [ ] Increase text sizes and spacing
- [ ] Add gradient backgrounds
- [ ] Thicker progress bars
- [ ] Better button design

### Phase 4: UI Modernization - Lock Widgets (Week 2-3)
- [ ] Redesign LockCircularWidget with gradient arc
- [ ] Redesign LockRectangularWidget with better spacing
- [ ] Replace emoji fallbacks with Material Icons
- [ ] Larger text sizes for lock screen viewing
- [ ] Test on lock screen

### Phase 5: Real-Time Sync (Week 3)
- [ ] Implement WidgetSyncObserver
- [ ] Add event emitters in ViewModels
- [ ] Add event emitters in AuthManager
- [ ] Add event emitters in SubscriptionManager
- [ ] Test immediate updates after actions

### Phase 6: Accessibility & Polish (Week 3-4)
- [ ] Add semantic descriptions to all widgets
- [ ] Test with TalkBack screen reader
- [ ] Ensure minimum touch targets (48dp)
- [ ] High contrast mode support
- [ ] Test on various device sizes
- [ ] Performance optimization
- [ ] Battery usage testing

### Phase 7: Testing & QA (Week 4)
- [ ] Unit tests for data models
- [ ] Unit tests for formatters
- [ ] Integration tests for worker
- [ ] UI tests for widgets
- [ ] Manual testing on physical devices
- [ ] Lock screen widget testing (Android 14+)
- [ ] Performance profiling
- [ ] Battery impact analysis

---

## 4. Success Metrics

### Performance Metrics
- **Update Latency**: < 1 second from action to widget update
- **Battery Impact**: < 1% per day with typical usage
- **Memory Usage**: < 50MB total for all widgets
- **Crash Rate**: < 0.1% of widget renders

### User Experience Metrics
- **Readability**: Text readable from 50cm distance
- **Glanceability**: Primary info understood in < 2 seconds
- **Accessibility**: WCAG AA compliance
- **Polish**: Professional appearance matching iOS quality

### Technical Metrics
- **Update Success Rate**: > 99% successful updates
- **Sync Accuracy**: Widget data matches app data 100%
- **Worker Efficiency**: Skip unnecessary refreshes when data is fresh
- **Error Recovery**: Automatic retry with max 3 attempts

---

## 5. Risk Assessment

### Low Risk
- UI visual updates (can be easily reverted)
- Theme system (additive change)
- Logger additions (no functional impact)

### Medium Risk
- Worker refactoring (test thoroughly on various network conditions)
- Currency formatter changes (ensure backward compatibility)

### High Risk
- Event-driven update system (complex coordination, potential race conditions)
- Sync observer (may cause performance issues if not optimized)

**Mitigation Strategy:**
- Feature flags for gradual rollout
- Extensive testing on different Android versions
- Rollback plan for each phase
- Monitor crash reports and ANRs closely

---

## 6. Conclusion

The current widget implementation is functional but needs modernization. The proposed refactoring plan addresses three key areas:

1. **Data Flow**: From passive polling to real-time event-driven updates
2. **UI/UX**: From basic functional design to professional Material Design 3
3. **Reliability**: From basic worker to intelligent, self-healing background refresh

**Estimated Effort**: 3-4 weeks for complete refactoring
**Expected Outcome**: Professional, reliable widgets with real-time sync that match iOS quality

**Next Steps**:
1. Review and approve this plan
2. Start with Phase 1 (Foundation)
3. Implement phases incrementally with testing between each phase
4. Monitor metrics and user feedback
5. Iterate based on real-world usage

---

## Appendix A: Current vs. Proposed Architecture Diagram

### Current Architecture
```
┌──────────────┐
│     App      │
│ (ViewModels) │
└──────┬───────┘
       │ Manual call to WidgetUpdater
       ↓
┌──────────────┐     ┌────────────────┐
│SharedPrefs   │────→│    Widgets     │
│(Widget Data) │     │(Read on render)│
└──────────────┘     └────────────────┘
       ↑
       │
┌──────────────┐
│WorkManager   │
│(15 min poll) │
└──────────────┘
```

### Proposed Architecture
```
┌──────────────┐
│     App      │
│ (ViewModels) │──────→ Emit events
└──────┬───────┘
       │
       ↓
┌─────────────────────┐
│WidgetUpdateCoordinator│
│  (Event Hub)        │
└──────┬──────────────┘
       │
       ↓
┌─────────────────────┐     ┌────────────────┐
│WidgetUpdateDispatcher│────→│    Widgets     │
│(Smart selective     │     │(Real-time data)│
│ update)             │     └────────────────┘
└─────────┬───────────┘
          │
          ↓
    ┌──────────────┐
    │SharedPrefs   │
    │(Widget Data) │
    └──────────────┘
          ↑
          │
    ┌──────────────┐
    │SmartWorker   │
    │(Network-aware│
    │ 15 min poll) │
    └──────────────┘
```

---

## Appendix B: Design Mockups

[Would include visual mockups here if this were a real design document]

---

**Document Version**: 1.0  
**Last Updated**: June 8, 2026  
**Author**: Kiro AI  
**Status**: Pending Review
