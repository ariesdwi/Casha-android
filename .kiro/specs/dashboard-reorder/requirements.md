# Dashboard Reorder Feature - Requirements

## Overview
Refactor the Dashboard screen component order to prioritize daily-relevance information over monthly/quarterly content, reducing scroll depth and improving first-screen value within 3 seconds of app launch.

## Business Goals
1. **Reduce time to first meaningful info**: From ~2s (scroll required) to <0.5s (visible immediately)
2. **Prioritize daily-use features**: Surface most-checked information (transactions) above periodic reviews (goals, reports)
3. **Improve actionability**: Add conditional budget alerts that only appear when user needs guidance
4. **Maintain feature parity**: No existing functionality should be removed or degraded

## User Problems Addressed

### Problem 1: WalletCardDeck Dominates Viewport (Priority: HIGH)
**Current State:**
- WalletCardDeck (including pager) takes ~200dp height
- On smaller devices (Pixel 4a, Samsung A-series), fills entire first screen
- Users must scroll to see any other content

**Impact:**
- First-time users don't realize there's more content below
- Daily check-in requires unnecessary scrolling

**Solution:**
- Create `WalletSummaryCompact` component (~52dp height)
- Compress to 1-2 lines showing total balance + wallet count
- Add expand/collapse functionality to reveal full WalletCardDeck
- Users who need detailed wallet view can still access it

### Problem 2: Budget Status Invisible Until User Checks Budget Tab (Priority: MEDIUM)
**Current State:**
- No budget alerts or warnings on Dashboard
- User only discovers overspending by manually checking Budget tab

**Impact:**
- Missed opportunity for proactive financial guidance
- Users exceed budgets without realizing

**Solution:**
- Create `BudgetAlertBanner` component (conditional rendering)
- Show only when budget(s) reach smart threshold
- Smart threshold: `monthElapsedFraction + 10%` (not fixed 80%)
  - Example: 15 days into 30-day month (50%) → alert at 60% budget used
  - Prevents early-month false alarms
- Display max 2 most critical budget categories
- Tap to navigate to Budget detail

### Problem 3: Content Ordered by Visual Appeal, Not Usage Frequency (Priority: MEDIUM)
**Current State:**
- Report Section (spending chart): Position #2
- Goals: Position #3
- Recent Transactions: Position #4 (last)

**Impact:**
- Daily-use content (transactions) buried below weekly/monthly review content
- Users scroll past irrelevant sections to reach what they need

**Solution:**
- Reorder by usage frequency:
  1. Wallet Summary → Daily reference ("How much do I have?")
  2. Budget Alert → Conditional daily warning
  3. Recent Transactions → Daily review ("What did I spend today?")
  4. Report → Weekly/monthly pattern review
  5. Goals → Monthly/quarterly progress check

## Functional Requirements

### FR-1: Wallet Summary Compact Component
- Display total balance across all wallets
- Show wallet count (e.g., "3 wallets")
- Optional: Show net cashflow for current period
- Tap to expand/collapse full WalletCardDeck inline
- Alternative tap action: Navigate to Wallet List screen
- Height constraint: ≤ 60dp in collapsed state

### FR-2: Budget Alert Banner Component
- Conditional rendering: Only visible when alert conditions met
- Alert condition: Budget category percentage ≥ smart threshold
  - Smart threshold = `min(monthElapsedFraction + 0.10, 0.90)`
  - monthElapsedFraction = currentDay / daysInMonth
- Display maximum 2 budget alerts
- Sort by severity (highest % used first)
- Show: Category name, % used or amount remaining, warning icon
- Tap behavior: Navigate to Budget detail screen for that category
- Height when hidden: 0dp (no placeholder space)
- Data source: `BudgetState.budgets` (existing)

### FR-3: Dashboard Section Reordering
New component order (top to bottom):
1. Welcome Header (unchanged)
2. **WalletSummaryCompact** (CHANGED - replaces WalletCardDeck)
3. **BudgetAlertBanner** (NEW - conditional)
4. **RecentTransactionsSection** (MOVED UP - was #4)
5. **ReportSection** (MOVED DOWN - was #2)
6. **GoalSection** (MOVED DOWN - was #3)

### FR-4: Data Loading & State Management
- Add `budgetAlerts: List<Budget>` computed property to ViewModel
- Integrate `BudgetRepository` into `DashboardViewModel` (if not already present)
- Handle loading states for new components (shimmer/skeleton)
- Handle error states gracefully (show last known data or empty state)

### FR-5: Budget State Integration
- DashboardViewModel needs access to budget data
- Options:
  - A) Inject `BudgetRepository` and fetch in ViewModel
  - B) Create `GetBudgetAlertsUseCase` that encapsulates smart threshold logic
- ViewModel calculates smart threshold and filters budgets
- No need for separate `@EnvironmentObject` pattern (that's SwiftUI-specific)

## Non-Functional Requirements

### NFR-1: Performance
- Dashboard initial load time: < 1 second on mid-range devices (Pixel 4a)
- Wallet expand/collapse animation: 300ms smooth transition
- No jank during scroll (maintain 60 FPS)

### NFR-2: Compatibility
- Support Android API 26+ (Android 8.0 Oreo)
- Responsive design for all screen sizes:
  - Small phones (< 5.5"): Pixel 4a, Samsung Galaxy A series
  - Standard phones (5.5" - 6.5"): Pixel 6, Samsung S series
  - Large phones (> 6.5"): Pixel 6 Pro, Samsung Ultra series
- Dark mode support for all new components

### NFR-3: Accessibility
- All interactive elements must have contentDescription
- Color status indicators must not be sole means of conveying info (include text labels)
- Minimum touch target: 48dp for all tappable elements
- Support TalkBack screen reader
- Text contrast ratios: WCAG AA compliance (4.5:1 for normal text)

### NFR-4: Maintainability
- Follow existing project architecture patterns:
  - MVVM with Hilt DI
  - UseCase pattern for business logic
  - Repository pattern for data access
- Component files in `ui/feature/dashboard/` package
- Consistent naming: `Dashboard[ComponentName].kt`
- Comprehensive KDoc comments for public functions
- Unit tests for ViewModel logic (smart threshold calculation, state updates)

## Technical Constraints

### TC-1: Existing Component Modifications
Files requiring changes:
- `DashboardScreen.kt`: Reorder LazyColumn items, add new components
- `DashboardViewModel.kt`: Add budget alerts logic
- `DashboardUiState.kt`: Add new state properties
- `WalletCardDeck.kt`: May need minor refactor for expand/collapse integration

New files to create:
- `WalletSummaryCompact.kt`: Compressed wallet summary
- `BudgetAlertBanner.kt`: Conditional budget warning banner

### TC-2: Design System Consistency
- Use existing theme colors from `ui/theme/`:
  - CashaSuccess, CashaWarning, CashaDanger for status
  - MaterialTheme.colorScheme for surfaces
- Use existing typography scale (MaterialTheme.typography)
- Use existing spacing (4dp grid: 4, 8, 12, 16, 20, 24 dp)
- Card corner radius: 24dp (match existing Dashboard cards)
- Card elevation: 2dp (match existing Dashboard cards)

## Success Metrics

### Immediate (Post-Implementation)
- [ ] Wallet section height reduced by ~150dp (200dp → 50dp compressed)
- [ ] Recent Transactions visible without scroll on standard screens (6" - 6.5")
- [ ] Budget alerts appear only when threshold exceeded (no false positives)

### Short-term (1 Week Post-Release)
- Dashboard scroll depth reduced by 40% (avg scroll position higher)
- Time to first interaction decreased by 30%
- Budget tab visits from alert banner: Track CTR (target > 15%)

### Long-term (1 Month Post-Release)
- Wallet expand rate < 30% (compression successful - most users don't need detail)
- User feedback: Reduced complaints about "hard to find transactions"

## Out of Scope (Future Considerations)
- Dashboard customization / user-configurable section order
- Animated transitions between component reorders
- A/B testing different section orders
- Dashboard widgets / home screen shortcuts
- Safe Spend Hero component (removed from scope)
- Quick actions from Dashboard

## Acceptance Criteria Summary
1. Wallet Summary Compact shows total balance and expands to full deck on tap
2. Budget Alert Banner only appears when smart threshold exceeded, max 2 alerts
3. Component order matches new specification (Wallet → Alert → Transactions → Report → Goals)
4. All existing Dashboard functionality preserved (no feature removal)
5. Loading and error states handled gracefully for all new components
6. Dark mode support for all new UI elements
7. No performance regression (scroll still 60 FPS, load time < 1s)
8. Accessibility: All elements have contentDescription, minimum 48dp touch targets
9. Code follows project conventions (MVVM, Hilt, UseCase pattern)

## Dependencies & Prerequisites
- Budget data access (via Repository or UseCase)
- Existing WalletCardDeck component must remain functional
- No breaking changes to existing DashboardViewModel public interface

## Risk Assessment

| Risk | Severity | Mitigation |
|------|----------|------------|
| Budget alerts too noisy | Medium | Smart threshold with month-progress weighting prevents early-month false alarms |
| Users prefer old layout | Low | Old layout had scroll issues on small devices; new layout addresses usability problems |
| Performance regression | Low | Lazy loading, no heavy computation in composables, async data fetching |
| Wallet compression confusing | Medium | Clear expand icon, animation feedback, preserves full deck accessibility |

## Localization Requirements
New strings needed (examples in English):
- `dashboard_wallet_summary_total`: "Total Assets"
- `dashboard_wallet_summary_count`: "%d wallets"
- `dashboard_budget_alert_title`: "Budget Alert"
- `dashboard_budget_alert_remaining`: "%s remaining"

All strings must support Indonesian (ID) and English (EN) locales.
