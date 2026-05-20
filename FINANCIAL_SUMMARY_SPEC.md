# Financial Summary Feature — Android Implementation Spec

Dokumentasi lengkap fitur Financial Summary via Chat AI di Casha untuk implementasi di Android.

---

## 1. Overview

Fitur ini memungkinkan user meminta ringkasan keuangan bulanan melalui chat AI. Backend memproses request, mengembalikan structured data, dan app menampilkan konfirmasi dengan AI-generated response text.

| Aspect | Detail |
|--------|--------|
| Trigger | User mengetik di chat (e.g., "Rangkum keuangan bulan ini") |
| AI Processing | Backend parses intent → returns `FINANCIAL_SUMMARY` + structured data |
| Display | Confirmation card dengan badge teal + AI-generated message text |
| Action | Display-only — tidak ada apply/save action |
| Response Type | `FINANCIAL_SUMMARY` |
| Chat Icon | `chart.bar.doc.horizontal.fill` |
| Badge Color | Teal |

> **Note:** Financial Summary adalah **display-only**. Tidak ada state refresh, tidak ada data yang disimpan lokal setelah ditampilkan.

---

## 2. User Flow

```
User types message
(e.g. "Rangkum keuangan bulan ini" / "Summarize my finances this month")
       │
       ▼
┌──────────────────────────┐
│ POST /chat/parse         │  ← text + language header
└────────┬─────────────────┘
         │
         ▼
┌──────────────────────────────────────────┐
│ Response: intent = "FINANCIAL_SUMMARY"   │
│ + message (AI text response)             │
│ + data: FinancialSummaryData (structured)│
└────────┬─────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────┐
│ Display confirmation card:                 │
│  [📊 FINANCIAL_SUMMARY] badge (teal)       │
│  ✓ icon                                    │
│  AI response message text                  │
└────────────────────────────────────────────┘
         │
         ▼ User taps "Buat Transaksi Baru"
┌───────────────────────┐
│ Reset chat to initial  │
└───────────────────────┘
```

---

## 3. API Endpoints

### 3.1 Chat Parse (Text)

```
POST /chat/parse
```

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
Accept-Language: id|en|ar|de|es|fr|hi|ja|ko|pt-BR|zh-Hans
```

**Request Body:**
```json
{
  "input": "Rangkum keuangan bulan ini",
  "language": "id"
}
```

**Response (FINANCIAL_SUMMARY):**
```json
{
  "status": "success",
  "message": "Berikut ringkasan keuangan kamu bulan Mei 2026 📊\n\nTotal pemasukan: Rp10.000.000\nTotal pengeluaran: Rp7.500.000\nSisa budget: Rp2.500.000\nBudget terpakai: 75%\nSafe to spend hari ini: Rp250.000\n\nKamu punya 4 akun dengan total aset Rp150.000.000. Pengeluaran kamu masih dalam batas aman! 💪",
  "data": {
    "period": {
      "month": "Mei",
      "year": 2026,
      "daysPassed": 19,
      "daysRemaining": 12
    },
    "income": {
      "total": 10000000.0
    },
    "spending": {
      "total": 7500000.0
    },
    "budget": {
      "total": 10000000.0,
      "spent": 7500000.0,
      "remaining": 2500000.0,
      "percentage": 75
    },
    "safeToSpend": {
      "daily": 250000.0,
      "remaining": 3000000.0
    },
    "assets": {
      "total": 150000000.0,
      "accounts": [
        { "name": "BCA Tabungan", "amount": 50000000.0 },
        { "name": "Mandiri", "amount": 30000000.0 },
        { "name": "OVO", "amount": 2000000.0 },
        { "name": "GoPay", "amount": 500000.0 }
      ]
    },
    "currency": "IDR"
  },
  "intent": "FINANCIAL_SUMMARY"
}
```

### 3.2 Chat Parse (Image / Receipt)

```
POST /chat/parse-image
```

**Headers:**
```
Authorization: Bearer <token>
Content-Type: multipart/form-data
Accept-Language: id
```

**Multipart fields:**
| Field | Type | Description |
|-------|------|-------------|
| `file` | Binary | Image file (receipt/screenshot) |
| `input` | String (optional) | Additional text context |

Response structure is the same as text parse.

---

## 4. Data Models

### 4.1 FinancialSummaryData

```kotlin
data class FinancialSummaryData(
    val period: Period,
    val totalIncome: Double,            // 10000000.0
    val totalSpending: Double,          // 7500000.0
    val budgetTotal: Double,            // 10000000.0
    val budgetSpent: Double,            // 7500000.0
    val budgetRemaining: Double,        // 2500000.0
    val budgetPercentage: Int,          // 75
    val safeToSpendDaily: Double,       // 250000.0
    val safeToSpendRemaining: Double,   // 3000000.0
    val totalAssets: Double,            // 150000000.0
    val accounts: List<AssetAccount>,
    val currency: String                // "IDR"
) {
    data class Period(
        val month: String,          // "Mei" — already localized by backend
        val year: Int,              // 2026
        val daysPassed: Int,        // 19
        val daysRemaining: Int      // 12
    )

    data class AssetAccount(
        val name: String,           // "BCA Tabungan"
        val amount: Double          // 50000000.0
    )
}
```

### 4.2 ChatParseResult

```kotlin
data class ChatParseResult(
    val intent: ChatIntent,
    val message: String,                                    // AI-generated text
    val transactionData: ChatTransactionData? = null,       // EXPENSE/INCOME
    val multiExpenseSummary: MultiExpenseSummary? = null,   // MULTI_EXPENSE
    val whatIf: WhatIfSimulation? = null,                   // WHAT_IF
    val financialSummary: FinancialSummaryData? = null,     // FINANCIAL_SUMMARY
    val budgetRecommendation: BudgetRecommendationData? = null // BUDGET_RECOMMENDATION
)
```

### 4.3 DTO (JSON mapping)

The API nests structured data under the `data` key, decoded based on `intent` value:

| Intent | `data` shape |
|--------|-------------|
| `EXPENSE` / `INCOME` | `ChatTransactionData` object |
| `MULTI_EXPENSE` | `[ChatTransactionData]` array |
| `WHAT_IF` | `WhatIfDataDTO` object |
| `FINANCIAL_SUMMARY` | `FinancialSummaryDataDTO` object |
| `BUDGET_RECOMMENDATION` | `BudgetRecommendationDataDTO` object |

---

## 5. UI Card Layout

Financial Summary uses the **generic confirmation card** (same base as other intents), not a dedicated rich card:

```
┌──────────────────────────────────────────────────────┐
│  ┌──┐   [📊 FINANCIAL_SUMMARY]  (teal badge)         │
│  │ ✓ │                                                │
│  └──┘   Berikut ringkasan keuangan kamu bulan        │
│         Mei 2026 📊                                   │
│                                                       │
│         Total pemasukan: Rp10.000.000                 │
│         Total pengeluaran: Rp7.500.000                │
│         Sisa budget: Rp2.500.000                      │
│         Budget terpakai: 75%                          │
│         Safe to spend hari ini: Rp250.000             │
│                                                       │
│         Kamu punya 4 akun dengan total aset          │
│         Rp150.000.000. Pengeluaran kamu masih        │
│         dalam batas aman! 💪                          │
└──────────────────────────────────────────────────────┘
```

### Card Structure

| Element | Detail |
|---------|--------|
| Leading icon | `checkmark.circle.fill` in green circle (32x32) |
| Intent badge | "FINANCIAL_SUMMARY" text + `chart.bar.doc.horizontal.fill` icon, teal background, white text, rounded corner 8 |
| Message body | `result.message` — multi-line AI-generated text, subheadline size |
| Background | `systemGray6` |
| Corner radius | 16dp |
| Padding | 14dp all sides |
| Transition | Scale + opacity animation |

### Error State

If parsing fails:

```
┌──────────────────────────────────────────────────────┐
│  ┌──┐   Oops!                                        │
│  │ ⚠️ │  (orange icon)                                │
│  └──┘   Ada yang salah. Coba lagi ya.                │
└──────────────────────────────────────────────────────┘
```

---

## 6. Action Buttons

Financial Summary has **no action buttons** specific to the result. Only the global "Buat Transaksi Baru" / "New Transaction" button at the bottom of the confirmation area is shown, which resets the chat.

```
┌──────────────────────────────────────────────┐
│  + Buat Transaksi Baru                       │
└──────────────────────────────────────────────┘
```

Tapping this calls `resetToInitialState()`:
- `showConfirmation = false`
- `messageInput = ""`
- `sentMessages.clear()`
- `financialSummaryData = null`
- All state flags reset

---

## 7. Intent → UI Mapping (All Intents)

For reference, all intents and their visual treatment in the confirmation card:

| Intent | Icon | Badge Color | Has Rich Card |
|--------|------|-------------|----------------|
| `EXPENSE` | `arrow.up.circle.fill` | Orange | No |
| `INCOME` | `arrow.down.circle.fill` | Green | No |
| `PAYMENT` | `creditcard.fill` | Blue | No |
| `MULTI_EXPENSE` | `cart.fill` | Purple | No |
| `WHAT_IF` | `wand.and.stars` | Primary | Yes (WhatIfCard) |
| `FINANCIAL_SUMMARY` | `chart.bar.doc.horizontal.fill` | Teal | No |
| `BUDGET_RECOMMENDATION` | `lightbulb.fill` | Indigo | Yes (BudgetRecommendationCard) |
| `UNKNOWN` | `info.circle.fill` | Gray | No |

---

## 8. State Management

```kotlin
// ViewModel / UI State
data class ChatUiState(
    val isSending: Boolean = false,
    val showConfirmation: Boolean = false,
    val transactionSuccess: Boolean = false,
    val aiResponseMessage: String = "",
    val lastIntent: String = "",
    val financialSummaryData: FinancialSummaryData? = null,  // display-only, not persisted
    // ... other intent-specific states
)
```

After `FINANCIAL_SUMMARY` response:
- `transactionSuccess = true`
- `aiResponseMessage = result.message`
- `lastIntent = "FINANCIAL_SUMMARY"`
- `showConfirmation = true`
- `financialSummaryData = result.financialSummary` (stored for optional rich rendering)
- **No** local DB write
- **No** list refresh calls

---

## 9. Language / Localization

- Send `Accept-Language` header on every chat request
- The `message` field in the response is already in the user's language
- The `period.month` field ("Mei", "May", "五月") is pre-localized by the backend
- All currency values come as `Double` — format on client using user's locale

Supported languages: `id`, `en`, `ar`, `de`, `es`, `fr`, `hi`, `ja`, `ko`, `pt-BR`, `zh-Hans`

---

## 10. Example Trigger Phrases

| Language | Example Message |
|----------|----------------|
| Indonesian | "Rangkum keuangan bulan ini" |
| English | "Summarize my finances this month" |
| Indonesian | "Berapa total pengeluaran saya bulan ini?" |
| English | "How much did I spend this month?" |
| Indonesian | "Ringkasan keuangan Mei 2026" |

---

## 11. Android File Structure (Suggestion)

```
feature/chat/
├── data/
│   ├── model/
│   │   ├── ChatIntent.kt
│   │   ├── ChatParseResult.kt
│   │   ├── FinancialSummaryData.kt
│   │   └── ChatParseRequest.kt
│   └── repository/
│       └── ChatRepository.kt
├── ui/
│   ├── components/
│   │   ├── ConfirmationCard.kt          // Generic card for all intents
│   │   ├── IntentBadge.kt               // Badge with icon + label + color
│   │   └── NewTransactionButton.kt
│   └── ChatScreen.kt
└── viewmodel/
    └── ChatViewModel.kt                 // handleChatResult(), resetToInitialState()
```

---

## 12. FinancialSummaryData Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| `period.month` | String | Month name, pre-localized ("Mei", "May") |
| `period.year` | Int | 4-digit year |
| `period.daysPassed` | Int | Days elapsed in current month |
| `period.daysRemaining` | Int | Days remaining in current month |
| `totalIncome` | Double | Total income recorded this month |
| `totalSpending` | Double | Total spending recorded this month |
| `budgetTotal` | Double | Total budget set for the month |
| `budgetSpent` | Double | Amount spent against budget |
| `budgetRemaining` | Double | Budget not yet spent |
| `budgetPercentage` | Int | Percentage of budget used (0–100) |
| `safeToSpendDaily` | Double | Recommended daily spending limit |
| `safeToSpendRemaining` | Double | Total remaining safe-to-spend this month |
| `totalAssets` | Double | Sum of all linked accounts |
| `accounts` | List | Individual accounts with name + balance |
| `currency` | String | ISO currency code ("IDR", "USD") |

> All numeric fields default to `0` if missing from API response. `currency` defaults to `"IDR"`.
