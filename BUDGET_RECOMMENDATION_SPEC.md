# Budget Recommendation Feature — Android Implementation Spec

Dokumentasi lengkap fitur Budget Recommendation AI di Casha untuk implementasi di Android.

---

## 1. Overview

Fitur ini memberikan rekomendasi budget personalized berbasis AI melalui chat interface. User bisa meminta rekomendasi budget, melihat alokasi yang disarankan, debt payoff plan, lalu apply ke budget mereka.

| Aspect | Detail |
|--------|--------|
| Trigger | User mengetik di chat (e.g., "Rekomendasi budget untuk nabung rumah") |
| AI Processing | Backend parses intent → returns structured recommendation |
| Display | Rich card di chat bubble |
| Action | "Terapkan Budget" button → bulk apply ke budget user |
| Response Type | `BUDGET_RECOMMENDATION` |

---

## 2. User Flow

```
User types message (e.g. "budget recommendation untuk nabung rumah")
       │
       ▼
┌──────────────────┐
│ POST /chat/parse │  ← sends message to AI
└────────┬─────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ Response: intent = BUDGET_RECOMMENDATION │
│ + BudgetRecommendationData (structured) │
└────────┬────────────────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│ Display BudgetRecommendationCard │
│ (rich UI in chat bubble)         │
└────────┬─────────────────────────┘
         │
         ▼ User taps "Terapkan Budget"
┌─────────────────────────────────────────┐
│ POST /budgets/apply-recommendations     │
│ Body: { month, budgets: [...] }         │
└────────┬────────────────────────────────┘
         │
         ▼
┌────────────────────────┐
│ Button → "Budget Diterapkan" ✓ │
└────────────────────────┘
```

---

## 3. API Endpoints

### 3.1 Get Budget Recommendations

```
GET /budgets/recommendations
```

**Headers:**
```
Authorization: Bearer <token>
Accept-Language: id|en|ar|de|es|fr|hi|ja|ko|pt-BR|zh-Hans
```

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| monthlyIncome | Double | Yes | User's monthly income |
| fixedExpenses[category] | Double | No | Fixed expenses by category |

**Response:**
```json
{
  "status": "success",
  "data": {
    "summary": {
      "strategy": "Balanced",
      "totalAllocated": 8500000,
      "needsPercentage": 50,
      "wantsPercentage": 30,
      "savingsPercentage": 20
    },
    "recommendations": [
      {
        "category": "Food & Drinks",
        "suggestedAmount": 2000000,
        "reasoning": "Berdasarkan pengeluaran rata-rata kamu"
      },
      {
        "category": "Transportation",
        "suggestedAmount": 1500000,
        "reasoning": "Termasuk bensin dan parkir"
      }
    ],
    "insights": [
      "Pengeluaran makan kamu 20% lebih tinggi dari rata-rata",
      "Kamu bisa hemat 500rb dengan meal prep"
    ],
    "milestones": [
      {
        "title": "Emergency Fund",
        "target": "3 bulan pengeluaran",
        "action": "Sisihkan 1jt/bulan"
      }
    ],
    "financialSummary": {
      "monthlyIncome": 10000000,
      "currentSpending": 7500000,
      "debts": 2000000,
      "assets": 50000000
    }
  }
}
```

---

### 3.2 Apply Recommendations (Bulk)

```
POST /budgets/apply-recommendations
```

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "month": "2026-05",
  "budgets": [
    {
      "category": "Food & Drinks",
      "amount": 2000000
    },
    {
      "category": "Transportation",
      "amount": 1500000
    },
    {
      "category": "Savings",
      "amount": 2000000
    }
  ]
}
```

**Response:**
```json
{
  "status": "success",
  "data": null
}
```

---

## 4. Data Models

### 4.1 BudgetRecommendationData (from Chat)

This is the structured data returned by the chat AI when intent is `BUDGET_RECOMMENDATION`:

```kotlin
data class BudgetRecommendationData(
    val title: String,                          // "Budget Plan untuk Nabung Rumah"
    val summary: String,                        // "Berdasarkan income 10jt, ini alokasi optimal..."
    val monthlyIncome: Double,                  // 10000000.0
    val totalDebtObligation: Double,            // 2000000.0
    val freeCashflow: Double,                   // 8000000.0
    val recommendedBudgets: List<RecommendedBudget>,
    val debtPayoffPlan: DebtPayoffPlan?,        // nullable — only if user has debts
    val coachingNote: String                    // "Fokus lunasi KTA dulu sebelum nabung besar"
)

data class RecommendedBudget(
    val category: String,       // "Food & Drinks"
    val amount: Double,         // 2000000.0
    val percentage: Double,     // 20.0 (of total budget)
    val priority: String,       // "essential" | "debt" | "saving" | "lifestyle"
    val note: String            // "Termasuk makan siang kantor"
)

data class DebtPayoffPlan(
    val strategy: String,                   // "avalanche" | "snowball"
    val loans: List<LoanPayoff>,
    val extraPaymentSuggestion: Double,     // 500000.0
    val estimatedMonthsToDebtFree: Int      // 18
)

data class LoanPayoff(
    val name: String,           // "KTA BCA"
    val balance: Double,        // 15000000.0
    val monthlyPayment: Double, // 1000000.0
    val monthsToPayoff: Int     // 15
)
```

### 4.2 Chat Intent Enum

```kotlin
enum class ChatIntent(val value: String) {
    EXPENSE("EXPENSE"),
    INCOME("INCOME"),
    PAYMENT("PAYMENT"),
    MULTI_EXPENSE("MULTI_EXPENSE"),
    WHAT_IF("WHAT_IF"),
    FINANCIAL_SUMMARY("FINANCIAL_SUMMARY"),
    BUDGET_RECOMMENDATION("BUDGET_RECOMMENDATION"),
    UNKNOWN("UNKNOWN")
}
```

### 4.3 Apply Recommendation Request

```kotlin
data class ApplyRecommendationsRequest(
    val month: String,                  // "2026-05" (API format)
    val budgets: List<BudgetApplyItem>
)

data class BudgetApplyItem(
    val category: String,
    val amount: Double
)
```

---

## 5. UI Card Layout — BudgetRecommendationCard

The card is a rich, multi-section component displayed in the chat bubble:

```
┌──────────────────────────────────────────────────────┐
│ ┌──┐  [💡 BUDGET RECOMMENDATION]  (indigo badge)    │
│ │💡│                                                  │
│ └──┘  Budget Plan untuk Nabung Rumah                 │
│       Berdasarkan income 10jt, ini alokasi...        │
├──────────────────────────────────────────────────────┤
│  Pemasukan      │   Cicilan       │  Free Cashflow   │
│  Rp10.000.000   │   Rp2.000.000   │  Rp8.000.000    │
│  (green)        │   (red)         │  (blue)          │
├──────────────────────────────────────────────────────┤
│  ALOKASI BUDGET                                      │
│                                                      │
│  ● Food & Drinks                   Rp2.000.000      │
│  ████████████████░░░░ 20%                  │
│  Termasuk makan siang kantor                         │
│                                                      │
│  ● Transportation                  Rp1.500.000      │
│  ████████████░░░░░░░░ 15%                  │
│  Bensin + parkir harian                              │
│                                                      │
│  ● Savings                         Rp2.000.000      │
│  ████████████████░░░░ 20%                  │
│  Target DP rumah                                     │
│                                                      │
│  ● Entertainment                   Rp1.000.000      │
│  ████████░░░░░░░░░░░░ 10%                  │
│  Streaming + hangout                                 │
├──────────────────────────────────────────────────────┤
│  DEBT PAYOFF PLAN            [AVALANCHE] (red badge) │
│                                                      │
│  KTA BCA                                             │
│  Sisa: Rp15.000.000          Rp1.000.000/bln        │
│                               15 bln lagi            │
│                                                      │
│  KPR Mandiri                                         │
│  Sisa: Rp200.000.000         Rp3.000.000/bln        │
│                               67 bln lagi            │
│                                                      │
│  📅 Estimasi bebas hutang: 18 bulan                  │
├──────────────────────────────────────────────────────┤
│  ┌──┐ Fokus lunasi KTA dulu sebelum nabung          │
│  │✓👤│ besar. Setelah KTA lunas, alokasikan          │
│  └──┘ cicilan ke tabungan rumah.                     │
├──────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────┐  │
│  │       ✓ Terapkan Budget        │  (indigo btn) │  │
│  └────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────┐  │
│  │       ↺ Tanya Lagi             │  (gray btn)   │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

---

## 6. Card Sections Detail

### 6.1 Header Section
- Icon: `lightbulb.fill` in indigo circle (36x36)
- Badge: "BUDGET RECOMMENDATION" (white text, indigo background, rounded 8)
- Title: `data.title` (subheadline bold)
- Summary: `data.summary` (caption, secondary color)

### 6.2 Income Stats Section
Three equal-width columns:

| Column | Label | Value | Color |
|--------|-------|-------|-------|
| Left | "Pemasukan" | `monthlyIncome` formatted | Green |
| Center | "Cicilan" | `totalDebtObligation` formatted | Red |
| Right | "Free Cashflow" | `freeCashflow` formatted | Blue |

### 6.3 Budget Allocations Section
- Section title: "ALOKASI BUDGET" (caption bold, uppercase, secondary)
- For each `RecommendedBudget`:
  - Priority dot (8x8 circle, color by priority)
  - Category name (caption bold)
  - Amount (12pt bold rounded) + Percentage
  - Progress bar: `percentage / 100` fill, 6pt height, rounded
  - Note text (caption2, secondary) — only if not empty

**Priority Colors:**
| Priority | Color |
|----------|-------|
| essential | Blue |
| debt | Red |
| saving | Green |
| lifestyle | Orange |
| (default) | Secondary/Gray |

### 6.4 Debt Payoff Plan Section (optional)
Only shown if `debtPayoffPlan != null && loans.isNotEmpty`

- Section title: "DEBT PAYOFF PLAN" (caption bold)
- Strategy badge: uppercase, white text on red background (capsule)
- For each loan:
  - Name (caption bold)
  - Balance: "Sisa: Rp15.000.000" (caption2, secondary)
  - Monthly payment: "Rp1.000.000/bln" (11pt bold, red)
  - Months remaining: "15 bln lagi" (caption2, secondary)
- Footer: calendar icon + "Estimasi bebas hutang: **X bulan**"

### 6.5 Coaching Note Section
- Icon: `person.fill.checkmark` in indigo circle (28x28)
- Text: `data.coachingNote` (caption, secondary)

### 6.6 Action Buttons
Two buttons, stacked vertically:

**"Terapkan Budget" button:**
| State | Icon | Label | Background | Enabled |
|-------|------|-------|------------|---------|
| Default | ○ checkmark.circle | "Terapkan Budget" | Indigo | Yes |
| Loading | ProgressView spinner | "Menerapkan..." | Indigo (0.7 opacity) | No |
| Applied | ✓ checkmark.circle.fill | "Budget Diterapkan" | Green | No |

**"Tanya Lagi" button:**
- Icon: `arrow.counterclockwise`
- Label: "Tanya Lagi"
- Background: Gray5
- Always enabled
- Action: Reset chat for new question

---

## 7. Apply Budget Logic

When user taps "Terapkan Budget":

```kotlin
// 1. Map recommended budgets to API format
val request = ApplyRecommendationsRequest(
    month = getCurrentMonthFormatted(), // "2026-05"
    budgets = data.recommendedBudgets.map { budget ->
        BudgetApplyItem(
            category = budget.category,
            amount = budget.amount
        )
    }
)

// 2. Call API
val success = budgetRepository.applyRecommendations(request)

// 3. Update UI state
if (success) {
    isApplied = true
    // Refresh budget list/state
}
```

**Month format conversion:**
- Display: "Mei 2026" (localized)
- API: "2026-05" (yyyy-MM)

---

## 8. Card Styling

| Property | Value |
|----------|-------|
| Background | `systemGray6` / Material surface |
| Corner radius | 16dp |
| Section dividers | 1px divider with 16dp horizontal padding |
| Section padding | 16dp all sides |
| Transition | Scale + opacity animation |
| Progress bar height | 6dp, rounded corners 4dp |
| Button corner radius | 12dp |
| Button padding | 12dp vertical |

---

## 9. Integration with Chat

The budget recommendation card appears in the chat message list as a special message type:

```kotlin
// In chat message adapter/composable
when (message.intent) {
    ChatIntent.BUDGET_RECOMMENDATION -> {
        BudgetRecommendationCard(
            data = message.budgetRecommendationData!!,
            isApplied = budgetAppliedState,
            isApplying = isApplyingState,
            onApplyTapped = { viewModel.applyBudgetRecommendation(message.budgetRecommendationData!!) },
            onResetTapped = { viewModel.resetChat() }
        )
    }
    // ... other intents
}
```

---

## 10. Error Handling

| Scenario | Behavior |
|----------|----------|
| Apply API fails | Show toast/snackbar "Gagal menerapkan budget" |
| Network error | Show retry option |
| No income data | Backend returns simplified recommendation without debt plan |
| Empty recommendations | Show coaching note only with "Tanya Lagi" button |

---

## 11. Android File Structure (Suggestion)

```
feature/chat/
├── data/
│   ├── model/
│   │   ├── BudgetRecommendationData.kt
│   │   └── ApplyRecommendationsRequest.kt
│   └── repository/
│       └── BudgetRecommendationRepository.kt
├── ui/
│   ├── components/
│   │   ├── BudgetRecommendationCard.kt      // Main card composable
│   │   ├── IncomeStatsSection.kt
│   │   ├── BudgetAllocationRow.kt
│   │   ├── DebtPayoffSection.kt
│   │   └── CoachingNoteSection.kt
│   └── ChatScreen.kt
└── viewmodel/
    └── ChatViewModel.kt

feature/budget/
├── data/
│   └── api/
│       └── BudgetApi.kt                     // Retrofit endpoints
└── usecase/
    └── ApplyBudgetRecommendationsUseCase.kt
```

---

## 12. Colors Reference

| Name | Hex | Usage |
|------|-----|-------|
| Indigo | `#5856D6` | Header badge, coaching icon, apply button |
| Green | `#34C759` | Income stat, saving priority, applied button |
| Red | `#FF3B30` | Debt stat, debt priority, strategy badge |
| Blue | `#007AFF` | Free cashflow stat, essential priority |
| Orange | `#FF9500` | Lifestyle priority |
| Gray5 | `#E5E5EA` | "Tanya Lagi" button background |
| Gray6 | `#F2F2F7` | Card background |

---

## 13. Localization Notes

Key strings that need translation:
- "BUDGET RECOMMENDATION" (badge)
- "Pemasukan" / "Income"
- "Cicilan" / "Debt Obligations"
- "Free Cashflow"
- "Alokasi Budget" / "Budget Allocation"
- "Debt Payoff Plan"
- "Estimasi bebas hutang: X bulan" / "Estimated debt-free: X months"
- "Terapkan Budget" / "Apply Budget"
- "Menerapkan..." / "Applying..."
- "Budget Diterapkan" / "Budget Applied"
- "Tanya Lagi" / "Ask Again"
- "Sisa:" / "Remaining:"
- "/bln" / "/mo"
- "bln lagi" / "months left"

The AI response (title, summary, coachingNote, notes) comes in the user's language from the backend based on `Accept-Language` header.
