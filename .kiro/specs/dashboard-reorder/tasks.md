# Dashboard Reorder Feature - Implementation Tasks

## Task Overview
This document outlines the implementation tasks for the Dashboard Reorder feature, organized into 3 phases for incremental delivery.

**Total Estimated Tasks:** 8 tasks
**Estimated Duration:** 3-4 days (based on 2-3 tasks per day)

---

## Phase 1: UI Components (Tasks 1-2)

### Task 1: Create WalletSummaryCompact Component
**Priority:** HIGH  
**Estimated Time:** 2 hours  
**Dependencies:** None (uses existing wallet data)

**Description:**
Create compressed wallet summary component with expand/collapse functionality to replace the tall WalletCardDeck.

**Implementation Steps:**
1. Create `WalletSummaryCompact.kt` in `ui/feature/dashboard/`
2. Design collapsed state (default):
   - Row layout: Icon + "Total Assets: [amount]" + expand icon
   - Second line: "[count] wallets • Net: [cashflow]"
   - Height: ~52dp
3. Add expand/collapse state management with `remember { mutableStateOf(false) }`
4. Implement expand animation to show full `WalletCardDeck` inline
5. Use `AnimatedVisibility` or `animateContentSize` for smooth expansion
6. Calculate total balance from `walletSummary` or sum of `wallets`
7. Optional: Add tap-to-navigate to Wallet List as alternative action
8. Loading state for when wallet data unavailable

**Acceptance Criteria:**
- [ ] Collapsed state displays total balance and wallet count
- [ ] Collapsed height ≤ 60dp
- [ ] Expand icon clearly indicates expandable content
- [ ] Smooth animation when expanding/collapsing (300ms duration)
- [ ] Expanded state shows full WalletCardDeck inline
- [ ] Component reuses existing WalletCardDeck (no duplication)
- [ ] Optional: Tap navigates to Wallet List screen
- [ ] Loading shimmer while wallet data loads
- [ ] Dark mode support

**Files to Create/Modify:**
- NEW: `app/src/main/java/com/casha/app/ui/feature/dashboard/WalletSummaryCompact.kt`
- MODIFY (minor): `app/src/main/java/com/casha/app/ui/feature/dashboard/WalletCardDeck.kt` (if integration changes needed)

---

### Task 2: Create BudgetAlertBanner Component
**Priority:** MEDIUM  
**Estimated Time:** 2 hours  
**Dependencies:** Task 3 (budget data integration)

**Description:**
Create conditional budget alert banner that only appears when budget categories exceed smart threshold.

**Implementation Steps:**
1. Create `BudgetAlertBanner.kt` in `ui/feature/dashboard/`
2. Design banner layout:
   - Warning icon + category name + % used or amount remaining
   - Chevron/arrow icon indicating tappable
   - Alert color (CashaWarning or CashaDanger)
3. Component takes `budgetAlerts: List<Budget>` parameter
4. Conditional rendering: Only compose if `budgetAlerts.isNotEmpty()`
5. Display max 2 alerts in vertical stack
6. Tap navigation to Budget detail screen
7. Height when empty: 0dp (no placeholder)
8. Loading state: Hidden during load (only show when data available)

**Acceptance Criteria:**
- [ ] Banner only visible when budget alerts exist
- [ ] Displays max 2 budget alerts sorted by severity (highest % first)
- [ ] Shows category name and remaining amount/percentage
- [ ] Tapping banner navigates to Budget detail for that category
- [ ] Banner height = 0dp when no alerts (no empty space)
- [ ] Uses CashaWarning (70-90%) or CashaDanger (≥90%) colors
- [ ] Alert icon clearly indicates warning
- [ ] 48dp minimum touch target for tap action
- [ ] Dark mode colors appropriate

**Files to Create/Modify:**
- NEW: `app/src/main/java/com/casha/app/ui/feature/dashboard/BudgetAlertBanner.kt`

---

## Phase 2: Budget Integration & Smart Threshold (Tasks 3-4)

### Task 3: Integrate Budget Data into DashboardViewModel
**Priority:** MEDIUM  
**Estimated Time:** 1.5 hours  
**Dependencies:** None

**Description:**
Add budget data access to DashboardViewModel and implement smart threshold logic for budget alerts.

**Implementation Steps:**
1. Check if `BudgetRepository` already exists in project
   - If not: Create interface and implementation
2. Inject budget repository/usecase into `DashboardViewModel`
3. Add `budgetAlerts: List<Budget>` to `DashboardUiState`
4. Implement smart threshold calculation:
   ```kotlin
   val calendar = Calendar.getInstance()
   val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
   val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
   val monthElapsedFraction = dayOfMonth.toDouble() / daysInMonth.toDouble()
   val threshold = min(monthElapsedFraction + 0.10, 0.90)
   ```
5. Filter budgets where `(spent / amount) > threshold`
6. Sort by % used descending, take top 2
7. Fetch budget data in `refreshDashboardInternal()` alongside other data

**Acceptance Criteria:**
- [ ] Budget repository accessible in DashboardViewModel
- [ ] Smart threshold logic implemented correctly
- [ ] `budgetAlerts` contains max 2 budgets sorted by severity
- [ ] Alerts only include budgets exceeding smart threshold
- [ ] Example validation: On day 15 of 30-day month, threshold = 0.60 (60%)
- [ ] Budget data fetched in parallel with other dashboard data
- [ ] No crash if budget data unavailable (empty list fallback)

**Files to Create/Modify:**
- MODIFY: `app/src/main/java/com/casha/app/ui/feature/dashboard/DashboardViewModel.kt`
- POSSIBLY NEW: `app/src/main/java/com/casha/app/domain/usecase/budget/GetBudgetAlertsUseCase.kt`


---

### Task 4: Add Unit Tests for Smart Threshold Logic
**Priority:** LOW  
**Estimated Time:** 1 hour  
**Dependencies:** Task 3

**Description:**
Write unit tests for smart threshold calculation to ensure it works correctly across different dates.

**Implementation Steps:**
1. Create `DashboardViewModelTest.kt` in test directory
2. Test cases:
   - Day 1 of 30-day month → threshold = 11% (0.033 + 0.10)
   - Day 15 of 30-day month → threshold = 60% (0.50 + 0.10)
   - Day 27 of 30-day month → threshold = 90% (0.90 + 0.10, capped at 0.90)
   - Day 30 of 30-day month → threshold = 90% (capped)
3. Test budget filtering:
   - Budget at 50% on day 15 (threshold 60%) → no alert
   - Budget at 65% on day 15 (threshold 60%) → alert shown
4. Test sorting: Multiple budgets over threshold sorted by % used descending
5. Test max 2 alerts: 5 budgets over threshold → only top 2 returned

**Acceptance Criteria:**
- [ ] At least 5 test cases for smart threshold calculation
- [ ] Tests cover edge cases (first day, last day, mid-month)
- [ ] Tests verify filtering logic (over/under threshold)
- [ ] Tests verify sorting and max 2 alerts rule
- [ ] All tests pass

**Files to Create/Modify:**
- NEW: `app/src/test/java/com/casha/app/ui/feature/dashboard/DashboardViewModelTest.kt`

---

## Phase 3: Dashboard Reordering & Integration (Tasks 5-8)

### Task 5: Reorder Dashboard LazyColumn Items
**Priority:** HIGH  
**Estimated Time:** 1 hour  
**Dependencies:** Tasks 1, 2, 3

**Description:**
Update DashboardScreen.kt to reorder LazyColumn items according to new specification and integrate new components.

**Implementation Steps:**
1. Open `DashboardScreen.kt`
2. Modify LazyColumn items block with new order:
   ```kotlin
   item { WelcomeHeader(greetingText) }
   item { WalletSummaryCompact(wallets, summary, ...) }
   if (uiState.budgetAlerts.isNotEmpty()) {
       item { BudgetAlertBanner(alerts = uiState.budgetAlerts, ...) }
   }
   item { RecentTransactionsSection(...) }
   item { ReportSection(...) }
   item { GoalSection(...) }
   ```
3. Remove old `WalletCardDeck` item (now inside WalletSummaryCompact)
4. Add proper spacing between items (12.dp per current pattern)
5. Test scroll behavior on different screen sizes

**Acceptance Criteria:**
- [ ] Component order matches specification:
  1. WelcomeHeader
  2. WalletSummaryCompact
  3. BudgetAlertBanner (conditional)
  4. RecentTransactionsSection
  5. ReportSection
  6. GoalSection
- [ ] Old WalletCardDeck removed from LazyColumn (now inside compact component)
- [ ] Spacing between items consistent (12dp)
- [ ] No layout jank during scroll
- [ ] Components render correctly on small (Pixel 4a) and large (Pixel 6 Pro) screens
- [ ] Pull-to-refresh still works

**Files to Create/Modify:**
- MODIFY: `app/src/main/java/com/casha/app/ui/feature/dashboard/DashboardScreen.kt`

---

### Task 6: Add Localization Strings
**Priority:** MEDIUM  
**Estimated Time:** 30 minutes  
**Dependencies:** Tasks 1, 2

**Description:**
Add all required string resources for new components in both English and Indonesian.

**Implementation Steps:**
1. Add strings to `app/src/main/res/values/strings.xml` (English)
2. Add strings to `app/src/main/res/values-id/strings.xml` (Indonesian)
3. Strings needed:
   - `dashboard_wallet_summary_total`: "Total Assets" / "Total Aset"
   - `dashboard_wallet_summary_wallets`: "%d wallets" / "%d wallets"
   - `dashboard_budget_alert_title`: "Budget Alert" / "Peringatan Budget"
   - `dashboard_budget_alert_remaining`: "%s remaining" / "Sisa %s"
4. Update components to use `stringResource(R.string.xxx)` instead of hardcoded strings

**Acceptance Criteria:**
- [ ] All strings added to English strings.xml
- [ ] All strings added to Indonesian strings-id.xml
- [ ] Components use stringResource() for all user-facing text
- [ ] Strings display correctly when device language is Indonesian
- [ ] Strings display correctly when device language is English
- [ ] Plurals handled correctly (e.g., "1 wallet" vs "3 wallets")

**Files to Create/Modify:**
- MODIFY: `app/src/main/res/values/strings.xml`
- MODIFY: `app/src/main/res/values-id/strings.xml`
- MODIFY: All new component files to use localized strings

---

### Task 7: Implement Navigation from Budget Alert Banner
**Priority:** MEDIUM  
**Estimated Time:** 30 minutes  
**Dependencies:** Task 2

**Description:**
Add click handling to BudgetAlertBanner that navigates to Budget detail screen for the selected category.

**Implementation Steps:**
1. Update `BudgetAlertBanner` to accept `onAlertClick: (Budget) -> Unit` callback
2. In `DashboardScreen`, pass navigation lambda:
   ```kotlin
   onAlertClick = { budget ->
       navController.navigate(Screen.BudgetDetail(budget.id).route)
   }
   ```
3. Ensure Budget detail screen route exists (check `navigation/Screen.kt`)
4. If route doesn't exist, create it or navigate to Budget tab with category pre-selected
5. Test navigation flow: Dashboard → tap alert → Budget detail opens

**Acceptance Criteria:**
- [ ] Tapping budget alert banner triggers navigation
- [ ] Navigation targets correct budget category detail
- [ ] Back navigation returns to Dashboard (back stack preserved)
- [ ] If multiple alerts shown, each navigates to its own category
- [ ] Navigation works on first tap (no double-tap required)

**Files to Create/Modify:**
- MODIFY: `app/src/main/java/com/casha/app/ui/feature/dashboard/BudgetAlertBanner.kt`
- MODIFY: `app/src/main/java/com/casha/app/ui/feature/dashboard/DashboardScreen.kt`
- POSSIBLY MODIFY: `app/src/main/java/com/casha/app/navigation/Screen.kt` (if route missing)

---

### Task 8: End-to-End Testing & Validation
**Priority:** HIGH  
**Estimated Time:** 2 hours  
**Dependencies:** All previous tasks

**Description:**
Comprehensive testing of the complete Dashboard Reorder feature across different scenarios and devices.

**Implementation Steps:**
1. **Functional Testing:**
   - Dashboard loads successfully with all new components
   - Wallet Summary Compact expands/collapses smoothly
   - Budget alerts appear only when threshold exceeded
   - Recent Transactions still displays correctly in new position
   - Report and Goals sections still functional

2. **Device Testing:**
   - Small phone (Pixel 4a, 5.8" screen): Wallet compact visible without scroll
   - Standard phone (Pixel 6, 6.4" screen): All top 3 sections visible
   - Large phone (Pixel 6 Pro, 6.7" screen): Most content visible without scroll
   - Tablet (10" screen): Layout adapts appropriately


3. **State Testing:**
   - Loading state: Shimmers display correctly
   - Error state: Dashboard still usable if data fails
   - Empty state: Budget alert hidden when no alerts
   - Offline mode: Dashboard loads from cached data

4. **Edge Cases:**
   - 0 wallets → Wallet Summary shows empty state
   - All budgets healthy → Budget alert banner hidden (0dp height)
   - Month-end (day 30/31) → Smart threshold capped at 90%

5. **Performance Testing:**
   - Dashboard load time < 1 second on Pixel 4a
   - Scroll maintains 60 FPS (no jank)
   - Pull-to-refresh completes in < 2 seconds (online)
   - Wallet expand/collapse animation smooth (300ms)

6. **Regression Testing:**
   - Period selector still works in Wallet Summary
   - Report Week/Month toggle still functional
   - Goals navigation still works
   - Transaction detail navigation still works
   - Pull-to-refresh still works

**Acceptance Criteria:**
- [ ] All functional tests pass
- [ ] Dashboard tested on at least 3 different screen sizes
- [ ] All state scenarios tested (loading, error, empty, success)
- [ ] All edge cases handled gracefully
- [ ] Performance metrics met (load < 1s, scroll 60 FPS)
- [ ] No regressions in existing features
- [ ] Dark mode looks correct on all components
- [ ] No crashes or ANRs during testing

**Testing Checklist:**
- [ ] WalletSummaryCompact expands/collapses smoothly
- [ ] BudgetAlertBanner only shows when alerts exist
- [ ] Component order matches specification
- [ ] Navigation from budget alert works
- [ ] Pull-to-refresh updates all sections
- [ ] Offline mode works (cached data)
- [ ] Dark mode colors appropriate
- [ ] TalkBack announces content correctly (if Task 9 completed)
- [ ] No performance regressions

**Files to Create/Modify:**
- POSSIBLY NEW: `app/src/androidTest/java/com/casha/app/ui/feature/dashboard/DashboardScreenTest.kt` (if integration tests desired)

---

## Summary

### Task Breakdown by Priority
- **HIGH Priority:** Tasks 1, 5, 8 (3 tasks)
- **MEDIUM Priority:** Tasks 2, 3, 6, 7 (4 tasks)
- **LOW Priority:** Task 4 (1 task)

### Estimated Timeline
- **Phase 1 (UI Components):** 4 hours → Day 1
- **Phase 2 (Budget Integration):** 2.5 hours → Day 2
- **Phase 3 (Integration & Testing):** 4 hours → Days 3-4

**Total Estimated Time:** 10.5 hours (~1.5 work days or 3-4 days at 2-3 hours/day)

### Dependencies Graph
```
Task 1 (WalletSummaryCompact) ───────┐
                                      ├─> Task 5 (Dashboard Reordering)
Task 3 (Budget Integration)           │       └─> Task 8 (Testing)
  └─> Task 2 (BudgetAlertBanner) ────┤
  └─> Task 4 (Unit Tests)            │
                                      │
Task 6 (Localization) ────────────────┤
Task 7 (Navigation) ──────────────────┘
```

### Critical Path
The critical path for feature delivery is:
1. Task 1 (Wallet compact)
2. Task 3 → Task 2 (Budget integration → alerts)
3. Task 5 (Dashboard reordering)
4. Task 8 (Testing)

Tasks 4, 6, 7 can be done in parallel or after main implementation.

---

## Optional Enhancement (Not in Critical Path)

### Task 9: Add Accessibility Support
**Priority:** MEDIUM  
**Estimated Time:** 1 hour  
**Dependencies:** Tasks 1, 2, 5

**Description:**
Ensure all new components meet accessibility guidelines for TalkBack users and have proper content descriptions.

**Implementation Steps:**
1. Add `contentDescription` to all interactive elements:
   - Expand/collapse button in WalletSummaryCompact
   - Budget alert banner tap target
2. Add semantic labels for numeric values:
   - Budget alert: "Budget alert: Food category 85% used, 75,000 rupiah remaining"
3. Ensure minimum 48dp touch targets for all tappable elements
4. Test with TalkBack enabled:
   - Navigate through dashboard components
   - Verify all content is announced correctly
   - Check that interactive elements are focusable
5. Verify color contrast ratios (WCAG AA): 4.5:1 for normal text

**Acceptance Criteria:**
- [ ] All interactive elements have contentDescription
- [ ] TalkBack announces all text content correctly
- [ ] Minimum 48dp touch targets for all tappable elements verified
- [ ] Color contrast ratios meet WCAG AA standard (4.5:1)
- [ ] Status indicators have text labels (not color-only)
- [ ] TalkBack navigation flows logically through dashboard
- [ ] Semantic labels provide context for numeric values

**Files to Create/Modify:**
- MODIFY: `app/src/main/java/com/casha/app/ui/feature/dashboard/WalletSummaryCompact.kt`
- MODIFY: `app/src/main/java/com/casha/app/ui/feature/dashboard/BudgetAlertBanner.kt`

---

## Next Steps

1. **Review tasks.md with team**
2. **Prioritize tasks** based on business value
3. **Assign tasks** to developers
4. **Set up feature branch:** `feature/dashboard-reorder`
5. **Begin implementation** starting with Phase 1 (WalletSummaryCompact)
6. **Incremental testing** after each phase
7. **Final validation** with Task 8 before merging
