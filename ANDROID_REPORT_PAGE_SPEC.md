# Report Page — Android Adaptation Spec
**Platform target:** Android (Jetpack Compose)  
**Source:** iOS `App/Module/Report/` — current production skin  
**Status:** 📋 Requirements

---

## 1. Overview

Halaman Report adalah dedicated analytics screen yang dapat diakses dari tab/navigation bottom bar. Tujuan layar ini adalah memberi user gambaran pengeluaran mereka berdasarkan kalender harian, distribusi kategori (pie chart), dan daftar kategori terurut.

---

## 2. Screen Layout (Scroll View — top to bottom)

```
┌─────────────────────────────────────────────┐
│  [← Back]   Spending Report         [Filter ⋮] │  ← TopAppBar
├─────────────────────────────────────────────┤
│  ┌── CALENDAR SECTION ────────────────────┐ │
│  │  Title: "Daily Report"                 │ │
│  │  Subtitle: "Aug 2025" / "Week 1–7 Aug" │ │
│  │                                        │ │
│  │  [< chevron]  Aug 2025  [chevron >]    │ │
│  │                                        │ │
│  │  Mon Tue Wed Thu Fri Sat Sun           │ │
│  │  [day cells with spending heat color]  │ │
│  └────────────────────────────────────────┘ │
│                                             │
│  ─────────────── DIVIDER ────────────────── │
│                                             │
│  ┌── CHART HEADER ────────────────────────┐ │
│  │  Total: Rp 1.250.000               [i] │ │  ← period total
│  │  Subtitle: "Aug 1 – Aug 31, 2025"      │ │
│  └────────────────────────────────────────┘ │
│                                             │
│  ┌── PIE CHART ───────────────────────────┐ │
│  │   [Donut Chart — category distribution]│ │
│  │   Overlay %: ≥ 5% shown on segment    │ │
│  │   Legend: below chart                  │ │
│  └────────────────────────────────────────┘ │
│                                             │
│  ┌── CATEGORY LIST ───────────────────────┐ │
│  │  Food                    Rp 500.000 >  │ │  ← tap → detail (premium)
│  │  Transport               Rp 300.000 🔒 │ │  ← lock if free tier
│  │  Entertainment           Rp 250.000 >  │ │
│  │  …                                     │ │
│  └────────────────────────────────────────┘ │
│                                             │
│       [bottom padding 40dp]                 │
└─────────────────────────────────────────────┘
```

---

## 3. Components

### 3.1 TopAppBar
| Property | Value |
|---|---|
| Title | `"Spending Report"` (localized) |
| Navigation icon | Back arrow (if pushed via NavController) |
| Action | Overflow menu icon (`⋮`) → `FilterMenu` |

**Filter menu items:**
- This Week
- This Month ✓ *(default)*
- This Year
- Custom Range… → opens `DateRangePickerDialog`

---

### 3.2 Calendar Section Card
Renders inside a `Card` with `cornerRadius = 20dp`, `elevation = 4dp`.

**Three modes** controlled by `selectedPeriod`:

#### Mode: Month (default)
- Navigation header: `[<]  "Aug 2025"  [>]`
- Grid: 7 columns × n rows (Mon–Sun header row)
- Each day cell:
  - Date number
  - Color intensity based on `dailySpending[date].amount` (heat map scale 0→max)
  - Tap → opens `DailyTransactionBottomSheet`
- "No spending on [day]" hint shown below grid

#### Mode: Week
- Navigation header: `[<]  "Aug 1 – Aug 7, 2025"  [>]`
- Single row of 7 day cells (same style as month)
- Navigate by ±7 days

#### Mode: Year
- Navigation header: `[<]  "2025"  [>]`
- Grid of 12 month tiles, each showing monthly total
- Tap on month → drills down to month view (`onMonthTap`)

**Calendar Cell design:**
```
┌─────┐
│ 14  │   ← date number (small, secondary color)
│ ●   │   ← spending dot / heat tint
└─────┘
```
- Background tint: interpolate from `cashaCard` (no spend) → `cashaPrimary` (max spend)
- Selected day: border `cashaPrimary`, 2dp stroke
- Loading state: show shimmer on cells

---

### 3.3 Daily Transaction Bottom Sheet
Triggered when user taps a day in the calendar.

| Property | Value |
|---|---|
| Type | `ModalBottomSheet` |
| Height | `WRAP_CONTENT` (≤ 60% screen) |
| Drag handle | Visible |
| Content | Date title + scrollable list of `TransactionCasha` items for that day |
| Empty state | "No transactions on this day." |

---

### 3.4 Pie Chart (Donut)
Use **MPAndroidChart** or **Vico** (or native Compose `Canvas`).

| Property | Value |
|---|---|
| Chart type | Donut (inner radius ≈ 50% of outer) |
| Angular inset | 1.5dp between segments |
| Corner radius | 4dp per segment |
| Percentage label | Overlay on segment if share ≥ 5% |
| Legend | Below chart, horizontal wrap |
| Frame height | 300dp |
| Background | `cashaBackground` card, `cornerRadius=12dp`, elevation `2dp` |

**Category color order** (assign by index, cycle if > 12):
```
Blue, Green, Orange, Red, Purple,
Pink, Teal, Indigo, Brown, Cyan,
Mint, Yellow
```

---

### 3.5 Category List
Vertical `LazyColumn` of `CategoryRowItem`.

**Row anatomy:**
```
[Category Name]           [Rp X.XXX.XXX] [> / 🔒]
[X% of total spending]
```

| Element | Detail |
|---|---|
| Title font | `titleMedium` (headline weight) |
| Subtitle font | `bodySmall`, `cashaTextSecondary` |
| Amount font | `titleMedium` bold, color `#F44336` (expense red) |
| Trailing icon — premium | Chevron right, `cashaTextSecondary` |
| Trailing icon — free | Lock icon `🔒`, `cashaPrimary`, circle background tint |

**Tap behavior:**
- **Premium user** → navigate to `CategoryDetailScreen(category: String)`
- **Free user** → show `PaywallBottomSheet`

---

### 3.6 Empty State
Show when `categorySpendings` list is empty.

```
[Illustration / icon]
"No spending data for this period."
```
Center-aligned, vertically centered in the content area.

---

### 3.7 Date Range Picker Dialog
Shown when user selects "Custom Range…" from filter menu.

| Property | Value |
|---|---|
| Type | `DateRangePicker` inside `AlertDialog` or `BottomSheet` |
| Inputs | Start Date, End Date |
| Actions | Apply, Cancel |
| On Apply | Call `viewModel.setCustomFilter(start, end)` |
| Height hint | `MATCH_PARENT` on sheet → `presentationDetents` equivalent = `peekHeight 50%` |

---

## 4. State / ViewModel

### 4.1 `ReportViewModel` (equivalent of `ReportState`)

```kotlin
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase,
    private val getDailySpendingUseCase: GetDailySpendingUseCase,
    private val getMonthlySpendingUseCase: GetMonthlySpendingUseCase,
    private val getTransactionsByDateUseCase: GetTransactionsByDateUseCase,
    private val getTransactionsByCategoryUseCase: GetTransactionsByCategoryUseCase,
) : ViewModel() {

    // Exposed UI state
    val uiState: StateFlow<ReportUiState>
    
    // Actions
    fun setFilter(period: ReportFilterPeriod)
    fun setCustomFilter(start: LocalDate, end: LocalDate)
    fun navigatePeriod(offset: Int)       // ±1 for week/month/year nav
    fun selectDay(date: LocalDate)        // triggers daily transaction load
    fun onMonthTap(month: YearMonth)      // drills into month from year view
    fun refreshAllData()
}
```

### 4.2 `ReportUiState`

```kotlin
data class ReportUiState(
    val isLoading: Boolean = false,
    val selectedPeriod: ReportFilterPeriod = ReportFilterPeriod.MONTH,
    val calendarDisplayMonth: YearMonth = YearMonth.now(),
    val dailySpending: List<DailySpending> = emptyList(),
    val monthlySpending: List<MonthlySpending> = emptyList(),
    val categorySpendings: List<ChartCategorySpending> = emptyList(),
    val selectedDayTransactions: List<Transaction> = emptyList(),
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null,
    val showDaySheet: Boolean = false,
    val selectedDay: LocalDate? = null,
)
```

### 4.3 `ReportFilterPeriod` enum

```kotlin
enum class ReportFilterPeriod {
    WEEK, MONTH, YEAR, CUSTOM;

    val displayTitle: String get() = when (this) {
        WEEK   -> "This Week"
        MONTH  -> "This Month"
        YEAR   -> "This Year"
        CUSTOM -> "Custom Range"
    }
}
```

---

## 5. Data Models

### 5.1 `DailySpending`
```kotlin
data class DailySpending(
    val date: LocalDate,
    val amount: Double,
)
```

### 5.2 `MonthlySpending`
```kotlin
data class MonthlySpending(
    val month: YearMonth,
    val amount: Double,
)
```

### 5.3 `ChartCategorySpending`
```kotlin
data class ChartCategorySpending(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val total: Double,
    val percentage: Double,  // 0.0 – 1.0
)
```

### 5.4 `Transaction`
Re-use the shared `Transaction` domain model already defined in the Android project.

---

## 6. Navigation

| Trigger | Destination |
|---|---|
| Tap category row (premium) | `CategoryDetailScreen(category)` |
| Tap category row (free) | `PaywallBottomSheet` |
| Tap day cell | `DailyTransactionBottomSheet` |
| Tap month tile (year view) | Re-render calendar in month view for tapped month |
| Filter → Custom Range | `DateRangePickerBottomSheet` |

---

## 7. Design Tokens

Use these tokens from the shared design system (map to Android `MaterialTheme` or a custom `CompositionLocal`).

| Token | Light | Dark | Usage |
|---|---|---|---|
| `cashaPrimary` | `#2E7D32` | `#81C784` | Nav arrows, active tint, CTA |
| `cashaAccent` | `#4CAF50` | `#43A047` | Progress, badge |
| `cashaBackground` | `#FFFFFF` | `#121212` | Screen background |
| `cashaCard` | `#EFF0F1` | `#1F1F1F` | Card surfaces |
| `cashaTextPrimary` | `#212121` | `#FFFFFF` | Titles, amounts |
| `cashaTextSecondary` | `#757575` | `#A5D6A7` | Subtitles, captions |
| Expense amount | `#F44336` | `#F44336` | Category total in red |

### Typography mapping (iOS → Compose)

| iOS | Compose `MaterialTheme.typography` |
|---|---|
| `.title2 bold` | `headlineSmall` |
| `.subheadline` | `bodyMedium` |
| `.headline bold` | `titleMedium` (weight Bold) |
| `.caption` | `bodySmall` |
| `.caption2` | `labelSmall` |

---

## 8. Spacing & Shape

| Element | Value |
|---|---|
| Screen horizontal padding | `16dp` |
| Screen top padding | `8dp` |
| Section spacing (VStack gap) | `24dp` |
| Calendar card corner radius | `20dp` |
| Calendar card shadow | `elevation = 4dp` |
| Pie chart card corner radius | `12dp` |
| Pie chart card elevation | `2dp` |
| Category list item vertical spacing | `16dp` |
| Bottom spacer | `40dp` |
| Navigation arrow button size | `32dp × 32dp` circle |
| Navigation arrow background opacity | 10% `cashaPrimary` |

---

## 9. Loading & Error States

| State | Behavior |
|---|---|
| `isLoading = true` | Show shimmer on calendar cells; show `CircularProgressIndicator` centered on chart area |
| Network error | Show `Snackbar` with retry action |
| Empty category list | Show `EmptyStateView` (center) |
| No transactions on day | Bottom sheet shows empty message |

---

## 10. Accessibility

- All interactive elements must have `contentDescription`
- Calendar day cells: `"[Date], spending [amount]"` or `"[Date], no spending"`
- Navigation arrows: `"Previous period"` / `"Next period"`
- Lock icon: `"Premium feature, tap to unlock"`
- Chevron icon: `"Tap to see transactions for [category]"`
- Minimum touch target: `48dp × 48dp`

---

## 11. Localization Keys (from `Report.xcstrings` reference)

| Key | Default EN |
|---|---|
| `report.title` | Spending Report |
| `report.calendar.title` | Daily Report |
| `report.filter.week` | This Week |
| `report.filter.month` | This Month |
| `report.filter.year` | This Year |
| `report.filter.custom` | Custom Range |
| `report.empty.message` | No spending data for this period. |
| `report.percentage_of_total %d` | `%d%% of total spending` |

---

## 12. API Endpoints (same as iOS)

| UseCase | Endpoint | Params |
|---|---|---|
| `GetCategorySpendingUseCase` | `GET /report/category` | `startDate`, `endDate` |
| `GetDailySpendingUseCase` | `GET /report/daily` | `startDate`, `endDate` |
| `GetMonthlySpendingUseCase` | `GET /report/monthly` | `year` |
| `GetTransactionsByDateUseCase` | `GET /transactions` | `date` |
| `GetTransactionsByCategoryUseCase` | `GET /transactions` | `category`, `startDate`, `endDate` |

> ⚠️ Confirm exact endpoint paths with backend — use iOS `Endpoint.swift` constants as the source of truth.

---

## 13. Premium Gate

Category row navigation is gated behind the **Casha Premium** subscription.

| User tier | Tap on category row |
|---|---|
| Free | Show lock icon; tap → `PaywallBottomSheet` |
| Premium | Navigate to `CategoryDetailScreen` |

Use `SubscriptionManager` (or its Android equivalent) to check `hasPremiumAccess`.

---

*Spec derived from iOS `ReportView.swift`, `ReportState.swift`, `ReportCalendarView.swift`, `ReportCategoryPieChart.swift`, `ReportCategoryList.swift` — last updated 2026-06-07.*
