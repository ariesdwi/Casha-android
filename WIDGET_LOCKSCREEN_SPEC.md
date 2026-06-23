# Casha Widget & Lock Screen — Android Implementation Spec

Dokumen ini menjelaskan fitur widget iOS Casha secara detail agar bisa diimplementasikan di Android (App Widget + Glance API).

---

## 1. Overview

| Platform | iOS (Current) | Android (Target) |
|----------|---------------|-------------------|
| Home Screen Widget | WidgetKit (`systemSmall`, `systemMedium`) | App Widget / Jetpack Glance |
| Lock Screen Widget | WidgetKit (`accessoryInline`, `accessoryCircular`, `accessoryRectangular`) | Lock Screen Widgets (Android 14+) / At a Glance |
| Data Sharing | App Group (UserDefaults) | SharedPreferences / DataStore |
| Refresh | Timeline (15 min) | WorkManager / periodic update |
| Deep Link | URL Scheme (`casha://`) | Intent / Deep Link |
| Premium Gate | Check `isPremium` from shared storage | Same logic |

---

## 2. Data Model — `WidgetSummary`

Shared JSON structure yang ditulis main app dan dibaca widget:

```json
{
  "safeSpendToday": 250000.0,
  "currency": "IDR",
  "daysRemaining": 12,
  "monthlyIncome": 10000000.0,
  "spentSoFar": 4500000.0,
  "pendingObligations": 2000000.0,
  "freeRemaining": 3500000.0,
  "status": "comfortable",
  "statusLabel": "Aman",
  "budgetPctUsed": 45,
  "hideBalance": false,
  "lastFetchedAt": "2026-05-19T03:00:00Z",
  "lastUpdatedAt": "2026-05-19T03:05:00Z",
  "spentToday": 75000.0,
  "insightText": "Pengeluaran kamu 20% lebih rendah dari minggu lalu",
  "insightGeneratedAt": "2026-05-19T02:00:00Z",
  "nextBillName": "Netflix",
  "nextBillAmount": 186000.0,
  "nextBillDueInDays": 3
}
```

### Status Enum

| Value | Label | Color (Hex) | Meaning |
|-------|-------|-------------|---------|
| `comfortable` | Aman | `#2E7D32` (green) | Spending within budget |
| `caution` | Hati-hati | `#FF9800` (orange) | Approaching limit |
| `over_budget` | Over Budget | `#F44336` (red) | Exceeded budget |
| `no_income` | Belum ada income | Secondary | No income data |
| `unknown` | - | Secondary | Error/no data |

---

## 3. Shared Storage

### iOS (reference)
- App Group: `group.com.casha.shared`
- Key: `widgetSummary` → JSON-encoded `WidgetSummary`
- Key: `isLoggedIn` → Bool
- Key: `isPremium` → Bool

### Android (recommended)
- Use `SharedPreferences` with a common name (e.g., `casha_widget_prefs`)
- Or `DataStore` (Proto/Preferences) shared between app and widget
- Keys remain the same: `widgetSummary`, `isLoggedIn`, `isPremium`

---

## 4. Refresh Strategy

- **Interval**: Every 15 minutes
- **Trigger**: Also on app foreground and after any transaction saved
- **Android**: Use `WorkManager` with `PeriodicWorkRequest(15, TimeUnit.MINUTES)` + manual `AppWidgetManager.notifyAppWidgetViewDataChanged()` after transactions

---

## 5. Widget Catalog

### 5.1 Lock Screen — Inline (Above Clock)

| Property | Value |
|----------|-------|
| iOS Family | `accessoryInline` |
| Android Equivalent | Lock screen widget (Android 14+) or At a Glance |
| Display | Single line: "Safe spend Rp250rb" |
| Max chars | ~25 characters |
| Tap action | Open budget screen |

**Layout:**
```
⚡ Safe spend Rp250rb
```

**Fallback labels (if text too long):**
1. `"Safe spend Rp250rb"` (primary)
2. `"Aman Rp250rb"` (shorter)
3. `"Rp250rb"` (minimal)

**States:**
| State | Display |
|-------|---------|
| Logged out | 👤 "Login ke Casha" |
| No data | 🔄 "Buka Casha" |
| Hidden balance | 🔒 "Saldo •••" |
| Not premium | 👑 "Casha Premium" |

---

### 5.2 Lock Screen — Circular (Budget Ring)

| Property | Value |
|----------|-------|
| iOS Family | `accessoryCircular` |
| Android Equivalent | Circular complication / small lock screen widget |
| Display | Circular gauge showing budget % used |
| Tap action | Open budget screen |

**Layout:**
```
    ╭──────╮
   │ BUDGET │
   │  45%   │
    ╰──────╯
   (green arc fills to 45%)
```

**Visual spec:**
- Arc/gauge from 0% to 100%
- Arc color = status color (green/orange/red)
- Center: "BUDGET" label (6pt) + percentage (11pt bold)
- Fill value: `budgetPctUsed / 100.0`

**States:**
| State | Display |
|-------|---------|
| Logged out | Person icon |
| No data | Refresh icon |
| Hidden | Lock icon |
| Not premium | Crown icon + "Premium" |

---

### 5.3 Lock Screen — Circular (Balance Ring)

| Property | Value |
|----------|-------|
| iOS Family | `accessoryCircular` |
| Display | Purple ring showing remaining balance ratio |
| Tap action | Open wallet screen |

**Layout:**
```
    ╭──────╮
   │ 3.5jt  │
    ╰──────╯
   (purple arc)
```

**Visual spec:**
- Arc color: Purple `#7F77DD`
- Fill value: `min(freeRemaining / monthlyIncome, 1.0)`
- Center: abbreviated amount (e.g., "3.5jt", "$3.5k")

---

### 5.4 Lock Screen — Rectangular (Budget Bar)

| Property | Value |
|----------|-------|
| iOS Family | `accessoryRectangular` |
| Android Equivalent | Rectangular lock screen widget |
| Max height | ~76pt |
| Tap action | Open budget screen |

**Layout:**
```
┌─────────────────────────────────────────┐
│ SAFE SPEND TODAY          [Aman]        │
│ Rp250.000                               │
│ ████████░░░░░░░░░ (progress bar)        │
│ 45% budget • 12 hari lagi              │
└─────────────────────────────────────────┘
```

**Visual spec:**
- Row 1: "SAFE SPEND TODAY" (8pt) + status badge (capsule with status color)
- Row 2: Currency amount (14pt bold)
- Row 3: Progress bar (spent today / safe spend today)
  - Green < 80%, Orange < 100%, Red ≥ 100%
- Row 4: "45% budget • 12 hari lagi" (8pt secondary)

**States:**
| State | Display |
|-------|---------|
| Logged out | Person icon + "Belum login" + "Buka Casha untuk mulai" |
| No data | Refresh icon + "Menunggu data" + "Buka app untuk sinkronisasi" |
| Hidden | Lock icon + "Balance hidden" + "•••" |
| Not premium | Crown icon + "Casha Premium" + "Upgrade untuk akses widget" |

---

### 5.5 Home Screen — Small (Quick Action)

| Property | Value |
|----------|-------|
| iOS Family | `systemSmall` (~155x155pt) |
| Android Equivalent | 2x2 App Widget |
| Tap action | Open add-expense |

**Layout:**
```
┌─────────────────────────┐
│ ⚡ SAFE SPEND        🟢 │
│                         │
│ Rp250.000               │
│                         │
│ Spent: Rp75.000         │
│ ████████░░░░ (bar)      │
│                         │
│ 🟢 Aman   12 hari lagi │
└─────────────────────────┘
```

**Visual spec:**
- Header: bolt icon + "SAFE SPEND" (9pt) + status dot (7pt circle)
- Amount: safe spend today (22pt heavy)
- Spent today bar (if > 0): label + capsule progress bar (3pt)
- Footer: status badge + days remaining

---

### 5.6 Home Screen — Medium (Budget + Actions)

| Property | Value |
|----------|-------|
| iOS Family | `systemMedium` (~329x155pt) |
| Android Equivalent | 4x2 App Widget |
| Layout | Two columns: Budget (left) + Actions (right) |

**Layout:**
```
┌───────────────────────────────┬────────────┐
│ ⚡ SAFE SPEND TODAY            │ [✨ AI]    │
│ Rp250.000                      │ [📊 Report]│
│ Spent: 75rb ████░░░            │ [📈 Budget]│
│                                │            │
│  ╭─╮                           │            │
│  │45%│ 🟢 Aman                 │            │
│  ╰─╯  12 hari lagi            │            │
└───────────────────────────────┴────────────┘
```

**Left column — Budget Summary:**
- "SAFE SPEND TODAY" header (9pt bold)
- Amount (20pt heavy)
- Spent today progress bar (if available)
- Budget ring (48x48): circular stroke showing `budgetPctUsed`
- Status badge (capsule) + days remaining

**Right column — Quick Action Buttons:**
3 pill-shaped buttons, each with icon + background color:

| Button | Icon | Color | Action (Deep Link) |
|--------|------|-------|--------------------|
| AI Chat | `wand.and.stars` / ✨ | `#3389E6` (blue) | `casha://add-transaction` |
| Report | `chart.xyaxis.line` / 📊 | `#7F77DD` (purple) | `casha://report` |
| Budget | `chart.bar.fill` / 📈 | `#2E7D32` (green) | `casha://budget` |

**Button style:** Rounded rectangle, ~52pt wide, icon centered, background fill with accent color.

---

## 6. Deep Link Schema

| URL | Action |
|-----|--------|
| `casha://add-expense` | Open add expense |
| `casha://add-transaction` | Open AI chat (add transaction) |
| `casha://budget` | Open budget screen |
| `casha://wallet` | Open wallet screen |
| `casha://report` | Open report screen |
| `casha://login` | Open login screen |
| `casha://subscription` | Open premium subscription |

---

## 7. Premium Gating

Widgets check `isPremium` from shared storage:
- **If NOT premium + logged in** → Show premium upsell view (crown icon + "Upgrade")
- **If NOT logged in** → Show login prompt
- **If premium + logged in** → Show actual widget content
- **Tap on premium widget** → Open subscription screen

---

## 8. Privacy Mode (Hide Balance)

When `hideBalance == true`:
- Replace all monetary values with "••••"
- Show lock icon
- Label: "Saldo Tersembunyi" / "Balance hidden"
- Tap opens app normally

---

## 9. States & Edge Cases

Every widget must handle these 4 states:

| # | State | Condition | Action |
|---|-------|-----------|--------|
| 1 | **Normal** | Logged in + premium + has data | Show widget content |
| 2 | **Logged out** | `isLoggedIn == false` | Show login prompt |
| 3 | **No data** | `lastUpdatedAt == distantPast` or null | Show "waiting for data" |
| 4 | **Not premium** | `isPremium == false` | Show upgrade prompt |
| 5 | **Hidden** | `hideBalance == true` | Show masked view |

---

## 10. Currency Formatting

### Short format (for compact spaces)
| Amount | Currency | Output |
|--------|----------|--------|
| 250000 | IDR | Rp250rb |
| 3500000 | IDR | Rp3.5jt |
| 15000000 | IDR | Rp15jt |
| 2500 | USD | $2.5k |
| 1500000 | USD | $1.5M |

### Full format
| Amount | Currency | Output |
|--------|----------|--------|
| 250000 | IDR | Rp250.000 |
| 3500000 | IDR | Rp3.500.000 |

### Rules:
- Use locale-appropriate grouping separator
- For IDR: "rb" = ribu (thousands), "jt" = juta (millions)
- For USD/EUR: "k" = thousands, "M" = millions
- Always show currency symbol prefix

---

## 11. Colors Reference

| Name | Hex | Usage |
|------|-----|-------|
| Brand Green (cashaSuccess) | `#2E7D32` | Comfortable status, budget ring |
| Brand Red (cashaDanger) | `#F44336` | Over budget |
| Orange (caution) | `#FF9800` | Caution status |
| Brand Purple | `#7F77DD` | Balance ring, report button |
| Blue (AI action) | `#3389E6` | AI chat button |
| Yellow (premium) | System yellow | Crown/premium icon |

---

## 12. Android Implementation Notes

### Jetpack Glance (Recommended)
```kotlin
// Glance widget example structure
class CashaSafeSpendWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val summary = loadWidgetSummary(context)
            CashaWidgetContent(summary)
        }
    }
}
```

### Key differences from iOS:
1. **No timeline concept** → Use `WorkManager` for periodic refresh + `GlanceAppWidget.update()` after transactions
2. **Lock screen widgets** → Only Android 14+ (API 34). Use `AppWidgetProviderInfo` with `widgetFeatures="reconfigurable|configuration_optional"` and `targetCellWidth`/`targetCellHeight`
3. **Interactions** → Use `actionStartActivity` or `actionRunCallback` instead of WidgetURL
4. **Circular gauges** → Use `Canvas` composable in Glance or custom `drawArc`
5. **Inline (above clock)** → Not available on Android; skip or use At a Glance integration

### Suggested widget sizes:
| iOS | Android (cells) | Android (min dp) |
|-----|-----------------|------------------|
| systemSmall | 2×2 | 110×110 |
| systemMedium | 4×2 | 250×110 |
| accessoryCircular | 1×1 (lock screen) | 52×52 |
| accessoryRectangular | 3×1 (lock screen) | 180×52 |
| accessoryInline | N/A | Not available |

### Refresh triggers:
1. WorkManager periodic (15 min minimum)
2. After save transaction → `AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged()`
3. On app open/resume → update SharedPreferences + trigger widget update
4. On login/logout state change

---

## 13. File Structure (Android Suggestion)

```
app/src/main/
├── widget/
│   ├── data/
│   │   ├── WidgetSummary.kt          // Data class (matches JSON above)
│   │   ├── SpendStatus.kt            // Enum with color mapping
│   │   └── WidgetPreferences.kt      // SharedPreferences read/write
│   ├── receiver/
│   │   ├── SafeSpendWidgetReceiver.kt
│   │   └── BudgetWidgetReceiver.kt
│   ├── ui/
│   │   ├── HomeSmallWidget.kt        // 2x2 Glance widget
│   │   ├── HomeMediumWidget.kt       // 4x2 Glance widget
│   │   ├── LockCircularWidget.kt     // Lock screen circular
│   │   └── LockRectangularWidget.kt  // Lock screen rectangular
│   ├── worker/
│   │   └── WidgetRefreshWorker.kt    // WorkManager periodic refresh
│   └── util/
│       └── CurrencyFormatter.kt      // Short/full format
├── res/xml/
│   ├── widget_home_small_info.xml
│   ├── widget_home_medium_info.xml
│   ├── widget_lock_circular_info.xml
│   └── widget_lock_rectangular_info.xml
```

---

## 14. API Endpoint (for reference)

Widget data comes from:
```
GET /cashflow/safe-spend-today
Authorization: Bearer <token>
```

Response matches `WidgetSummary` fields. Main app fetches → saves to shared storage → widget reads.
