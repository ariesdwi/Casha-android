# Dashboard Ordering Refactor — Flow Spec

## Status: ✅ IMPLEMENTED (Partially Complete - Phase 2/3)

## Context

Dashboard saat ini adalah landing screen utama (tab Home, tag 1). User membuka app → langsung lihat Dashboard. Ordering section harus memprioritaskan **informasi yang paling dibutuhkan dalam 3 detik pertama** saat user buka app.

---

## Current Implementation (As-Is)

```swift
// DashboardView.swift — lines 35-42 (ACTUAL CURRENT STATE)
ScrollView {
    VStack(alignment: .leading, spacing: 20) {
        walletCardDeck              // 1️⃣ WalletCardDeck (stacked cards)
        // safeSpendSection         // ⚠️ COMMENTED OUT (line 38)
        budgetAlertSection          // 2️⃣ ✅ Budget alert (conditional)
        reportSection               // 3️⃣ Spending chart (week/month bars)
        recentTransactionsSection   // 4️⃣ ✅ Recent transactions (moved up!)
        goalSection                 // 5️⃣ Goal tracker cards
        Spacer(minLength: 40)
    }
    .padding()
}
```

### Implementation Status:

| Component | Status | Notes |
|-----------|--------|-------|
| `DashboardSafeSpendCard` | ⚠️ Created but commented out | Line 38: `// safeSpendSection` |
| `DashboardBudgetAlertBanner` | ✅ Implemented | Conditional, shows top 2 budgets ≥ threshold |
| Recent Transactions moved up | ✅ Implemented | Now position #4 (before goals) |
| `DashboardWalletSummaryCompact` | ❌ Not created | Still using full `WalletCardDeck` |
| BudgetState integration | ✅ Done | `@EnvironmentObject var budgetState: BudgetState` |

### Current Layout Visual (ACTUAL):

```
┌─────────────────────────────────────────┐
│  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓  │
│  ┃  WALLET CARD DECK (~210pt)        ┃  │  ← Takes entire viewport
│  ┃  Net Cashflow + Wallets (swipe)   ┃  │
│  ┃  Period picker inside             ┃  │
│  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛  │
│                                         │
│  ⚠️ [SAFE SPEND - COMMENTED OUT]        │  ← Exists but disabled (line 38)
│                                         │
│  ┌─ BUDGET ALERT (conditional) ──────┐  │  ← ✅ IMPLEMENTED
│  │  ⚠️ Food budget 85% used          │  │     Shows if ≥ threshold
│  └───────────────────────────────────┘  │
│                                         │
│  ┌─ SPENDING REPORT ─────────────────┐  │  ← Must scroll to see
│  │  Week/Month toggle                │  │
│  │  Bar chart (daily/weekly)         │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌─ RECENT TRANSACTIONS ─────────────┐  │  ← ✅ MOVED UP (before goals)
│  │  List of latest entries           │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌─ GOALS ───────────────────────────┐  │  ← Now last position
│  │  Horizontal scroll cards (3 max)  │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### Budget Alert Implementation Details:

**Current Logic** (lines 221-230):
```swift
var budgetAlerts: [BudgetCasha] {
    let calendar = Calendar.current
    let now = Date()
    let day = calendar.component(.day, from: now)
    let daysInMonth = calendar.range(of: .day, in: .month, for: now)?.count ?? 30
    let monthElapsedFraction = Double(day) / Double(daysInMonth)
    // Only warn if spending pace is 10%+ ahead of the month elapsed
    let threshold = min(monthElapsedFraction + 0.10, 0.90)
    return budgetState.budgets
        .filter { $0.amount > 0 && ($0.spent / $0.amount) > threshold }
        .sorted { ($0.spent / $0.amount) > ($1.spent / $1.amount) }
        .prefix(2)
        .map { $0 }
}
```

**Smart Threshold:**
- Not fixed 80% — dynamically adjusts based on **month progress**
- Example: If 15 days into month (50%), alert triggers at 60% (50% + 10%)
- Prevents early-month false alarms
- Shows max 2 most critical budgets

---

## Problems with Current Order

| # | Problem | Impact | Status |
|---|---------|--------|--------|
| 1 | **WalletCardDeck takes ~250pt** (210pt card + padding + page indicator). On iPhone SE/Mini, it fills the entire visible viewport. User must scroll to see anything else. | First-time users don't know there's more below | ❌ Still present |
| 2 | **Recent Transactions was LAST** — but it's the most relevant daily info ("apa yang sudah gue keluarin hari ini?") | Daily check-in required scrolling past charts and goals | ✅ FIXED - Now position #4 |
| 3 | **No Safe Spend Today on Dashboard** — the #1 value prop of Casha. Only available via Widget. | Users must add widget or go to Cashflow API manually | ⚠️ Component exists but commented out |
| 4 | **Report Section is #2** — spending chart is weekly/monthly review, not daily need | Takes prime real estate from daily-use info | ✅ FIXED - Now position #3 (after budget alert) |
| 5 | **No budget alert/status** — user doesn't know if they're over budget until they go to Budget tab | Missed opportunity for proactive guidance | ✅ FIXED - Smart threshold alert implemented |
| 6 | **Goals is #3** — goals are monthly/quarterly check, not daily | Occupies above-fold space unnecessarily | ✅ FIXED - Now position #5 (last) |

---

## Proposed Order (To-Be)

```swift
ScrollView {
    VStack(alignment: .leading, spacing: 20) {
        safeSpendHero               // 1️⃣ NEW: Safe Spend Today (compact hero)
        walletSummaryCompact        // 2️⃣ CHANGED: Wallet summary (1-line, expandable)
        budgetAlertBanner           // 3️⃣ NEW: Budget alert (conditional, only if ≥80%)
        recentTransactionsSection   // 4️⃣ MOVED UP: Today's transactions
        reportSection               // 5️⃣ KEPT: Spending chart (moved down)
        goalSection                 // 6️⃣ KEPT: Goals (moved to last)
        Spacer(minLength: 40)
    }
    .padding()
}
```

### Proposed Layout Visual:

```
┌─────────────────────────────────────────┐
│                                         │
│  ┌─ SAFE SPEND TODAY (hero) ─────────┐  │  ← Instant answer: "berapa aman hari ini?"
│  │  Rp 250.000 / hari               │  │     ~80pt, always visible
│  │  🟢 Aman • 12 hari lagi          │  │
│  │  ━━━━━━━━━━━━░░░░ 62%            │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌─ WALLET SUMMARY (1-line) ─────────┐  │  ← Compressed. Tap to expand.
│  │  💰 Total: Rp45.000.000    [>]    │  │     ~44pt
│  └───────────────────────────────────┘  │
│                                         │
│  ┌─ BUDGET ALERT (conditional) ──────┐  │  ← Only shows if ≥80% budget used
│  │  ⚠️ Food budget tinggal 15%       │  │     ~50pt, or 0pt if healthy
│  └───────────────────────────────────┘  │
│                                         │
│  ┌─ TRANSAKSI HARI INI ─────────────┐  │  ← Daily relevance #1
│  │  Kopi Kenangan    -50rb   08:30   │  │
│  │  Grab Car         -35rb   12:15   │  │
│  │  Transfer masuk   +500rb  14:00   │  │
│  │  ─────────────────────────────    │  │
│  │  Net hari ini: -Rp85.000          │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌─ SPENDING REPORT ─────────────────┐  │  ← Weekly review (scrollable)
│  │  Week/Month chart                 │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌─ GOALS ───────────────────────────┐  │  ← Monthly check
│  │  Horizontal scroll (3 cards)      │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

---

## New Components Needed

### 1. `DashboardSafeSpendHero`

Menampilkan Safe Spend Today sebagai hero section teratas.

```
┌──────────────────────────────────────────────────┐
│                                                  │
│  Aman dibelanjakan hari ini                      │  ← subtitle, .caption
│                                                  │
│  Rp 250.000                                      │  ← .largeTitle, bold
│                                                  │
│  🟢 Aman   •   12 hari lagi   •   62% terpakai  │  ← status row
│                                                  │
│  ━━━━━━━━━━━━━━━━━━━━░░░░░░░░                    │  ← progress bar
│                                                  │
└──────────────────────────────────────────────────┘
```

**Data source:** `GET /cashflow/safe-spend-today` → sudah ada `GetSafeSpendTodayUseCase`

**State additions to `DashboardState`:**
```swift
@Published var safeSpendToday: SafeSpendToday?
```

**Status colors:**
| Status | Condition | Color |
|--------|-----------|-------|
| Aman | budgetPctUsed < 70% | `cashaPrimary` (green) |
| Hati-hati | 70% ≤ budgetPctUsed < 90% | `.orange` |
| Over | budgetPctUsed ≥ 90% | `cashaDanger` (red) |

**Height:** ~100pt (compact hero, not a full card)

---

### 2. `DashboardWalletSummaryCompact`

Replaces the full `WalletCardDeck` (210pt) with a single compressed line (~52pt).

```
┌──────────────────────────────────────────────────┐
│  👛  Total Aset: Rp 45.000.000          [ ∨ ]   │
│      3 wallets • Net: +Rp3.500.000 bulan ini     │
└──────────────────────────────────────────────────┘
```

**Behavior:**
- Tap `[ ∨ ]` → expand to show full `WalletCardDeck` inline (animated)
- Or tap → navigate to Wallet List page
- Shows total balance across all wallets (already in `walletState`)

**Decision:** Keep full `WalletCardDeck` accessible via expand OR as a separate screen. Don't remove — compress.

---

### 3. `DashboardBudgetAlertBanner`

Conditional banner — only shows when any budget category ≥ 80% used.

```
┌──────────────────────────────────────────────────┐
│  ⚠️  Food budget tinggal 15% (Rp75.000)    [→]  │
└──────────────────────────────────────────────────┘
```

**Rules:**
- Show max 2 alerts (most critical first)
- If all budgets healthy (< 80%) → section height = 0, completely hidden
- Tap → navigate to Budget tab/detail
- Sort by % used descending

**Data source:** `BudgetState.budgets` → filter where `usedPercentage >= 80`

**State:** Use existing `@EnvironmentObject var budgetState: BudgetState` (needs to be added to Dashboard)

---

## Migration Strategy

### Phase 1: Add Safe Spend Hero (non-breaking) ⚠️ PARTIALLY DONE

**Status:** Component created but commented out

**What's Done:**
1. ✅ `DashboardSafeSpendCard` view created
2. ✅ `safeSpendToday` property added to `DashboardState`
3. ✅ Fetch logic in `refreshDashboard()` exists
4. ⚠️ Component commented out in DashboardView (line 38)

**What's Needed:**
- Uncomment `safeSpendSection` in DashboardView
- Test and ensure proper data display
- Verify Safe Spend updates on scenePhase change

**Current Code:**
```swift
// Line 38 in DashboardView.swift
// safeSpendSection  ← UNCOMMENT THIS
```

### Phase 2: Move Transactions Up ✅ COMPLETED

**Status:** Fully implemented

**What's Done:**
1. ✅ `recentTransactionsSection` moved to position #4 (before goals)
2. ✅ Section visible without excessive scrolling
3. ✅ Transactions now prioritized over goals

**Result:** Daily relevant info now appears earlier in scroll

### Phase 3: Add Budget Alert ✅ COMPLETED

**Status:** Fully implemented with smart threshold

**What's Done:**
1. ✅ `budgetState` added as `@EnvironmentObject` to DashboardView
2. ✅ `DashboardBudgetAlertBanner` created and integrated
3. ✅ Smart threshold logic: `monthElapsedFraction + 0.10`
4. ✅ Conditional rendering (only shows if alerts exist)
5. ✅ Shows max 2 most critical budgets
6. ✅ Tap navigation to Budget tab

**Advanced Feature:**
- Dynamic threshold prevents early-month false alerts
- Example: 15 days into 30-day month = 50% elapsed
  - Alert triggers at 60% budget used (not fixed 80%)
  - Smarter than simple percentage threshold

### Phase 4: Compress Wallet (PENDING) ❌ NOT STARTED

**Status:** Not implemented

**What's Needed:**
1. Create `DashboardWalletSummaryCompact` view (~52pt height)
2. Add expand/collapse state management
3. Replace `walletCardDeck` with compact version
4. Keep full deck as expandable option

---

## Code Changes Required

### File: `DashboardView.swift`

**Current body (lines 33-39):**
```swift
VStack(alignment: .leading, spacing: 24) {
    walletCardDeck
    reportSection
    goalSection
    recentTransactionsSection
    Spacer(minLength: 40)
}
```

**Final body:**
```swift
VStack(alignment: .leading, spacing: 20) {
    safeSpendHero
    walletSummaryCompact
    if hasBudgetAlerts { budgetAlertBanner }
    recentTransactionsSection
    reportSection
    goalSection
    Spacer(minLength: 40)
}
```

### File: `DashboardState.swift`

**Add:**
```swift
@Published var safeSpendToday: SafeSpendToday?

// In refreshDashboard():
let safeSpendTask = Task { try await getSafeSpendToday.execute() }
// ... await alongside other tasks
self.safeSpendToday = try? await safeSpendTask.value
```

### New files to create:

| File | Location | Purpose |
|------|----------|---------|
| `DashboardSafeSpendHero.swift` | `App/Module/Dashboard/Sources/subView/` | Safe Spend hero card |
| `DashboardWalletSummaryCompact.swift` | `App/Module/Dashboard/Sources/subView/` | Compressed wallet line |
| `DashboardBudgetAlertBanner.swift` | `App/Module/Dashboard/Sources/subView/` | Conditional budget alert |

### Files to modify:

| File | Change |
|------|--------|
| `DashboardView.swift` | Reorder sections, add new components |
| `DashboardState.swift` | Add `safeSpendToday` property + fetch |
| `DashboardView.swift` | Add `@EnvironmentObject var budgetState: BudgetState` |
| `MainTabView.swift` | Pass `budgetState` to Dashboard's NavigationStack |

---

## Design Decisions & Rationale

### Why Safe Spend is #1?

User buka app → pertanyaan di kepala mereka: **"Berapa lagi yang aman gue keluarin hari ini?"**

Ini jawaban instan. Gak perlu scrolling, gak perlu mikir. Angka besar, warna status, selesai. 3 detik.

### Why compress Wallet (bukan hapus)?

WalletCardDeck itu 210pt — hampir setengah layar iPhone SE. Beautiful, tapi blocking konten di bawahnya. Solusi: compress ke 1 baris (~52pt) dengan opsi expand. User yang mau lihat detail wallet masih bisa. Yang gak butuh gak kehilangan viewport.

### Why Transactions before Report?

| Content | Usage Frequency | Info Type |
|---------|----------------|-----------|
| Recent Transactions | **Daily** (setiap buka app) | "Apa yang sudah keluar hari ini?" |
| Spending Report | **Weekly** (review pattern) | "Berapa total minggu ini?" |
| Goals | **Monthly** (progress check) | "Udah berapa persen?" |

Order by frequency = order by relevance to the moment.

### Why Budget Alert is conditional?

- Jangan kasih info yang gak actionable
- Kalau semua budget sehat → gak perlu gangguin user
- Kalau ada yang mendekati limit → BARU kasih heads up
- Ini pattern "notification by exception" — bukan spam

---

## Acceptance Criteria

- [ ] Safe Spend hero visible immediately when Dashboard loads (no scroll)
- [ ] Wallet info accessible tapi tidak mendominasi viewport
- [ ] Recent transactions visible tanpa scroll di iPhone 14+ (atau max 1 scroll di SE)
- [ ] Budget alert hanya muncul kalau ada budget ≥ 80% usage
- [ ] Tap budget alert → navigate ke Budget detail
- [ ] Tap wallet summary → expand atau navigate ke wallet list
- [ ] Safe Spend updates saat app menjadi active (scenePhase change)
- [ ] All existing functionality preserved (no feature removal)
- [ ] Smooth transitions between expanded/collapsed wallet states

---

## Metrics to Track (Post-Launch)

| Metric | Baseline | Target |
|--------|----------|--------|
| Time to first meaningful info | ~2s (scroll to find) | < 0.5s (visible immediately) |
| Dashboard scroll depth | Users scroll 2-3 sections | Most info visible without scroll |
| Budget tab visits from alert | N/A (new) | Track CTR on alert banner |
| Wallet expand rate | N/A (new) | < 30% = compression successful |
