# Transaction Module — UX Simplification Spec

## Context

Analisis profesional terhadap flow Transaction List → Detail → Edit di Casha iOS.
Fokus: mengurangi friction, menghilangkan informasi tidak relevan, mempercepat aksi harian.

**Files yang dianalisis:**
- `App/Module/Transactions/Sources/main/TransactionListView.swift`
- `App/Module/Transactions/Sources/main/TransactionDetailView.swift`
- `App/Module/Transactions/Sources/main/TransactionEditView.swift`
- `App/Module/Transactions/Sources/subView/TransactionFilterBar.swift`
- `App/UI/Component/SharedDailyTransactionCard.swift`

---

## Current Architecture

```
TransactionListView
├── SearchBar (live search, onChange triggers state.searchTransactions)
├── TransactionFilterBar (This month | Other month | This year | Custom)
├── SharedDailyTransactionCard (grouped by date, collapsible)
│   ├── Header (day, date, net amount, expand toggle)
│   ├── Stats row (↓ income total, ↑ expense total)
│   └── SharedTransactionRowView (per item)
│       ├── Circle icon (red=expense, green=income)
│       ├── Name + [INCOME/EXPENSE badge]
│       ├── Category label
│       ├── Amount (colored)
│       └── Time
│       └── NavigationLink → TransactionDetailView
│
TransactionDetailView (4 separate cards!)
├── Card 1: headerSection (80pt icon circle + name + type badge) ~120pt
├── Card 2: amountStatusSection (label + big amount) ~80pt
├── Card 3: categorySection (tag icon + category name) ~70pt
├── Card 4: detailsSection (date, updated, sync status) ~100pt
├── [Convert to Installment button] (conditional: if liabilityId)
└── Toolbar ellipsis menu → Edit | Delete
    └── TransactionEditView (sheet)
        ├── Section: Name, Amount (CurrencyInputField), Category picker
        ├── Section: DatePicker
        ├── Section: "Confirmed" toggle (isSynced) ⚠️
        └── Save button
```

---

## Problems Identified

### A. Transaction List

| # | Problem | Detail | Severity |
|---|---------|--------|----------|
| A1 | No period summary | User doesn't know total spent/earned for selected period | High |
| A2 | Filter only by time | No category, wallet, or amount range filter | Medium |
| A3 | INCOME/EXPENSE badge on every row | Triple-redundant: circle color + amount sign + badge | Low |
| A4 | No swipe actions | Must navigate to detail just to delete one transaction | High |
| A5 | "Other month" dropdown UX | Menu inside horizontal ScrollView — feels awkward | Low |
| A6 | No "today" quick summary | User wants to know "berapa sudah keluar hari ini?" instantly | Medium |
| A7 | Pagination invisible | Uses Color.clear.frame(height: 20).onAppear — user doesn't know more data exists | Low |

### B. Transaction Detail

| # | Problem | Detail | Severity |
|---|---------|--------|----------|
| B1 | 4 separate cards | Fragmented layout wastes space; must scroll on iPhone SE | High |
| B2 | Edit/Delete behind 3 taps | Ellipsis → Menu → Action — slow for daily use | High |
| B3 | "Sync status" shown | Technical detail (isSynced) — 99% users don't need this | High |
| B4 | 80pt decorative icon circle | Beautiful but wastes ~120pt vertical space | Medium |
| B5 | "EXPENSE" type badge | Redundant — user knows what they tapped from the list | Low |
| B6 | "Updated at" shown | Rarely useful — "Created at" is enough | Low |
| B7 | Category in own card | Should be a row in details, not a standalone 70pt card | Medium |
| B8 | Over-engineered loading | Glassmorphism + rotation spinner for a simple delete | Low |

### C. Transaction Edit

| # | Problem | Detail | Severity |
|---|---------|--------|----------|
| C1 | "Confirmed" toggle | isSynced is internal sync state exposed to user — confusing | High |
| C2 | Form/Section style | Feels like iOS Settings, not a finance app | Medium |
| C3 | Opens as sheet | Blocks context, can't reference other info while editing | Low |

---

## Recommendations

### List Improvement 1: Period Summary Header

Add a compact summary between filter bar and transaction list:

```
┌──────────────────────────────────────────────────┐
│  💸 Pengeluaran: Rp4.500.000                     │
│  💰 Pemasukan:   Rp8.000.000                     │
│  📊 Net:         +Rp3.500.000                    │
└──────────────────────────────────────────────────┘
```

**Implementation:**
```swift
private var periodSummaryView: some View {
    let totalExpense = state.cashflowSections
        .flatMap { $0.items }
        .filter { $0.direction == .out }
        .reduce(0) { $0 + abs($1.amount) }
    let totalIncome = state.cashflowSections
        .flatMap { $0.items }
        .filter { $0.direction == .in }
        .reduce(0) { $0 + abs($1.amount) }
    
    return VStack(alignment: .leading, spacing: 4) {
        HStack {
            Label(CurrencyFormatter.format(totalExpense), systemImage: "arrow.up")
                .foregroundColor(.red)
            Spacer()
            Label(CurrencyFormatter.format(totalIncome), systemImage: "arrow.down")
                .foregroundColor(.green)
        }
        .font(.caption.weight(.medium))
    }
    .padding(.horizontal)
    .padding(.vertical, 8)
}
```

**Effort:** 1 hour | **Impact:** High

---

### List Improvement 2: Remove INCOME/EXPENSE Badge

**File:** `App/UI/Component/SharedDailyTransactionCard.swift` (line ~335)

**Before:**
```swift
HStack(alignment: .center, spacing: 8) {
    Text(item.name)
        .font(.body.weight(.medium))
        .foregroundColor(.primary)
        .lineLimit(1)
    
    Text(item.isIncome ? "INCOME" : "EXPENSE")
        .font(.system(size: 9, weight: .bold))
        .foregroundColor(.white)
        .padding(.horizontal, 6)
        .padding(.vertical, 3)
        .background(item.isIncome ? Color.green : Color.red)
        .cornerRadius(4)
}
```

**After:**
```swift
Text(item.name)
    .font(.body.weight(.medium))
    .foregroundColor(.primary)
    .lineLimit(1)
```

**Rationale:** Circle color (green/red) + amount sign (+/-) already differentiates. Badge is noise.

**Effort:** 5 min | **Impact:** Medium (cleaner visual density)

---

### List Improvement 3: Context Menu (Long Press)

Since current layout uses ScrollView + VStack (not List), swipe actions aren't natively available. Use context menu as alternative:

```swift
// Add to SharedTransactionRowView or its NavigationLink wrapper:
.contextMenu {
    Button {
        // Navigate to edit
    } label: {
        Label("Edit", systemImage: "pencil")
    }
    Button(role: .destructive) {
        // Delete with confirmation
    } label: {
        Label("Hapus", systemImage: "trash")
    }
}
```

**Effort:** 30 min | **Impact:** High (reduces flow from 4 taps to 1 long press)

---

### List Improvement 4: Category Filter Chips (Phase 2)

Below time filter, add scrollable category chips:

```
[All ✓] [Food (12)] [Transport (8)] [Shopping (5)] [...]
```

- Show top 5 categories from current data
- Client-side filtering (data already loaded)
- "All" selected by default

**Effort:** 3-4 hours | **Impact:** Medium

---

### Detail Improvement 1: Merge 4 Cards → 1 Unified Card

**Current:** 4 cards, ~370pt total, needs scroll on small phones.

**Proposed:**
```
┌──────────────────────────────────────────────────┐
│                                                  │
│  [🍽️ 40pt]  Kopi                                │
│              Food • 27 Mei 2026, 08:30          │
│                                                  │
│  ─────────────────────────────────────────────── │
│                                                  │
│  -Rp 25.000                                      │
│                                                  │
│  ─────────────────────────────────────────────── │
│                                                  │
│  Wallet       BCA                                │
│  Dibuat       27 Mei 2026, 08:32                │
│                                                  │
│  ─────────────────────────────────────────────── │
│                                                  │
│  [ ✏️ Edit ]                    [ 🗑️ Hapus ]    │
│                                                  │
└──────────────────────────────────────────────────┘
```

**Removed:**
- 80pt decorative circle → 40pt inline icon
- "EXPENSE" / "INCOME" badge → color on amount is enough
- "Sync status" row → internal state, remove from UI
- "Updated at" row → keep only "Dibuat"
- Separate category card → inline in header subtitle

**Height:** ~370pt → ~200pt (46% reduction)

**Implementation sketch:**
```swift
private var unifiedDetailCard: some View {
    VStack(spacing: 0) {
        // Header: icon + name + category/date
        HStack(spacing: 14) {
            Circle()
                .fill(typeColor.opacity(0.15))
                .frame(width: 44, height: 44)
                .overlay(
                    Image(systemName: categoryIcon)
                        .foregroundColor(typeColor)
                        .font(.system(size: 18))
                )
            
            VStack(alignment: .leading, spacing: 4) {
                Text(currentTransaction.name)
                    .font(.headline)
                Text("\(currentTransaction.category) • \(shortDateFormatted)")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            Spacer()
        }
        .padding(16)
        
        Divider().padding(.horizontal, 16)
        
        // Amount
        Text(amountFormatted)
            .font(.system(size: 28, weight: .bold, design: .rounded))
            .foregroundColor(typeColor)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
        
        Divider().padding(.horizontal, 16)
        
        // Details rows
        VStack(spacing: 12) {
            detailRow(title: "Wallet", value: walletName ?? "-")
            detailRow(title: "Dibuat", value: createdAtFormatted)
        }
        .padding(16)
        
        Divider().padding(.horizontal, 16)
        
        // Action buttons
        actionButtonsRow
            .padding(16)
    }
    .background(Color.cashaCard)
    .cornerRadius(16)
}
```

**Effort:** 2-3 hours | **Impact:** High

---

### Detail Improvement 2: Bottom Action Buttons (Replace Ellipsis Menu)

```swift
private var actionButtonsRow: some View {
    HStack(spacing: 12) {
        Button {
            guard currentTransaction.isSynced else { showingSyncAlert = true; return }
            showingEditSheet = true
        } label: {
            Label("Edit", systemImage: "pencil")
                .font(.subheadline.weight(.semibold))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(Color.cashaPrimary.opacity(0.1))
                .foregroundColor(.cashaPrimary)
                .cornerRadius(10)
        }
        
        Button {
            guard currentTransaction.isSynced else { showingSyncAlert = true; return }
            showingDeleteAlert = true
        } label: {
            Label("Hapus", systemImage: "trash")
                .font(.subheadline.weight(.semibold))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(Color.red.opacity(0.1))
                .foregroundColor(.red)
                .cornerRadius(10)
        }
    }
}
```

**Tap reduction:** 3 taps → 1 tap (edit) or 2 taps (delete + confirm).

**Effort:** 30 min | **Impact:** High

---

### Detail Improvement 3: Group View Simplification

**Current:** 3 cards (header + amount + items list)
**Proposed:** 1 card with internal sections

```
┌──────────────────────────────────────────────────┐
│  🛒 Belanja Indomaret         Total: -Rp52.000  │
│     3 items • 27 Mei 2026, 14:30                │
│  ─────────────────────────────────────────────── │
│  • Susu ......................... -Rp15.000      │
│  • Roti ......................... -Rp12.000      │
│  • Sabun ........................ -Rp25.000      │
│  ─────────────────────────────────────────────── │
│  [ ✏️ Edit Nama ]        [ 🗑️ Hapus Semua ]    │
└──────────────────────────────────────────────────┘
```

Each item: long press → context menu (Edit Item / Delete Item).

**Effort:** 2 hours | **Impact:** Medium

---

### Edit Improvement 1: Remove "Confirmed" Toggle

**File:** `App/Module/Transactions/Sources/main/TransactionEditView.swift`

```swift
// DELETE this entire computed property:
var confirmationSection: some View {
    Section {
        Toggle(NSLocalizedString("transactions.edit.confirmed", ...), isOn: $isSynced)
    }
}

// And remove its usage from body Form
```

**Rationale:** `isSynced` is internal sync logic. User toggle = data corruption risk.

**Effort:** 5 min | **Impact:** Medium

---

### Edit Improvement 2: Better "Sync Required" UX

Instead of confusing alert, disable buttons with inline explanation:

```swift
// Replace alert-based approach with:
Button { showingEditSheet = true } label: { ... }
    .disabled(!currentTransaction.isSynced)

if !currentTransaction.isSynced {
    HStack(spacing: 6) {
        ProgressView().scaleEffect(0.7)
        Text("Menunggu sinkronisasi...")
            .font(.caption)
            .foregroundColor(.orange)
    }
}
```

**Effort:** 30 min | **Impact:** Medium

---

## Implementation Priority

### Phase 1: Quick Wins (< 1 hour total, zero risk)

| # | Change | File | Time |
|---|--------|------|------|
| 1 | Remove INCOME/EXPENSE badge from rows | SharedDailyTransactionCard.swift | 5 min |
| 2 | Remove sync status from detail | TransactionDetailView.swift | 5 min |
| 3 | Remove "Confirmed" toggle from edit | TransactionEditView.swift | 5 min |
| 4 | Remove "EXPENSE"/"INCOME" badge from detail header | TransactionDetailView.swift | 5 min |
| 5 | Remove "Updated at" from detail | TransactionDetailView.swift | 5 min |
| 6 | Add Edit/Delete bottom buttons | TransactionDetailView.swift | 30 min |

### Phase 2: Structural (1-2 days)

| # | Change | File | Time |
|---|--------|------|------|
| 7 | Merge 4 cards → 1 unified card | TransactionDetailView.swift | 2-3 hrs |
| 8 | Add period summary to list | TransactionListView.swift | 1 hr |
| 9 | Add context menu (long press) to rows | SharedDailyTransactionCard.swift | 30 min |
| 10 | Reduce header icon 80pt → 40pt inline | TransactionDetailView.swift | 30 min |
| 11 | Simplify group detail (3 cards → 1) | TransactionDetailView.swift | 2 hrs |

### Phase 3: Enhancement (1 week)

| # | Change | File | Time |
|---|--------|------|------|
| 12 | Swipe actions (requires List refactor) | SharedDailyTransactionCard.swift | 1-2 days |
| 13 | Category filter chips | TransactionFilterBar.swift + State | 3-4 hrs |
| 14 | Better pagination indicator | TransactionListView.swift | 30 min |
| 15 | Inline edit (replace sheet) | Major rewrite | 2-3 days |

---

## Before & After Summary

### Transaction Row

| Aspect | Before | After |
|--------|--------|-------|
| Elements per row | Icon + Name + Badge + Category + Amount + Time | Icon + Name + Category + Amount + Time |
| Visual noise | High (badge competes for attention) | Low (color does the job) |
| Actions available | Tap only (→ detail) | Tap + Long press (context menu) |

### Transaction Detail

| Aspect | Before | After |
|--------|--------|-------|
| Cards | 4 separate | 1 unified |
| Total height | ~370pt (scroll needed) | ~200pt (no scroll) |
| Taps to edit | 3 (... → menu → Edit) | 1 (button) |
| Taps to delete | 4 (... → menu → Delete → Confirm) | 2 (button → Confirm) |
| Irrelevant info shown | 3 (sync, badge, updated) | 0 |
| Decorative space | ~120pt (icon circle) | ~44pt (inline icon) |

### Transaction Edit

| Aspect | Before | After |
|--------|--------|-------|
| Confusing elements | "Confirmed" toggle | Removed |
| Sections | 4 (details, date, confirmed, error) | 3 (details, date, error) |

---

## Risk Assessment

| Change | Risk | Mitigation |
|--------|------|------------|
| Remove badge | None — purely visual | Color still differentiates |
| Remove sync status | Low | Keep in debug/dev settings if needed |
| Remove Confirmed toggle | Medium — edge case: stuck unsynced tx | Add "Force Sync All" in Settings > Debug |
| Merge 4 cards → 1 | Low — same data, new layout | Keep all sheets/alerts intact |
| Context menu | None — additive | Keep NavigationLink as primary |
| Bottom buttons | None — additive | Keep ellipsis menu as backup initially |

---

## Acceptance Criteria

### Phase 1
- [ ] No INCOME/EXPENSE badge visible on transaction rows
- [ ] No "Sync status" row in transaction detail
- [ ] No "Confirmed" toggle in transaction edit form
- [ ] No type badge in detail header
- [ ] Edit and Delete buttons visible at bottom of detail view
- [ ] All edit/delete functionality still works correctly

### Phase 2
- [ ] Transaction detail is 1 card, fits without scroll on iPhone 14+
- [ ] Period summary shows at top of transaction list
- [ ] Long-press context menu works on transaction rows
- [ ] Group detail is 1 unified card
- [ ] iPhone SE: detail fits with minimal scroll

### Phase 3
- [ ] Category filter chips filter instantly (client-side)
- [ ] Swipe-to-delete works with confirmation
- [ ] Pagination shows loading indicator text
