# 🔮 What If Engine — Feature Specification

Reference document for replicating the iOS What If Engine on Android (and other platforms).

---

## 1. Overview

What If lets a user simulate a hypothetical financial decision by typing a natural-language question into the existing AI chat. The backend detects a `WHAT_IF` intent and returns a rich impact card instead of creating a transaction.

**Key design principle:** no new screen, no new endpoint. Reuses the existing `POST /chat/parse` pipeline. The only client change is rendering a different response card when `intent == "WHAT_IF"`.

**Trigger examples:**
- "Kalau aku cicil iPhone 15jt 12 bulan?"
- "Gimana kalau aku langganan Netflix lagi?"
- "What if I buy a car for 300M?"
- "Simulasi pinjam 50jt buat renovasi"

---

## 2. UX Flow

```
User types "What if" question in chat
        │
        ▼
POST /chat/parse  ←── same endpoint as expense/income
        │
        ▼
Backend returns intent = "WHAT_IF"
        │
        ▼
App renders WhatIfResultCard (NOT TransactionConfirmationCard)
  ┌─────────────────────────────────┐
  │  [WHAT IF] badge + title        │
  │  ─────────────────────────────  │
  │  Cashflow: Before → After bar   │
  │  ─────────────────────────────  │
  │  Financial Health score ring    │  (optional, if backend provides)
  │  ─────────────────────────────  │
  │  Impacted Budgets list          │  (optional)
  │  ─────────────────────────────  │
  │  Goal Impacts list              │  (optional)
  │  ─────────────────────────────  │
  │  Verdict: headline + message    │
  │    • Coaching note (optional)   │
  │    • Urgency note (optional)    │
  │    • Alternatives (lightbulbs)  │
  │  ─────────────────────────────  │
  │  [ 🔄 Simulasi Baru ]           │
  └─────────────────────────────────┘
        │
        ▼
User taps "Simulasi Baru" → resets chat to input state
```

### Critical behavioral differences from transaction intents

| Behavior | Transaction (expense/income) | What If |
|----------|------------------------------|---------|
| Save to local DB | ✅ Yes | ❌ No |
| Refresh dashboard state | ✅ Yes | ❌ No |
| Show confirmation card | ✅ Yes | ❌ No — show `WhatIfResultCard` |
| "New Transaction" button | ✅ Yes | ❌ No — show "Simulasi Baru" |
| Apply to real data | ✅ Automatic | 🔄 Phase 2 (disabled) |

---

## 3. Trigger Words

The backend detects intent automatically. No client-side detection needed. Document these for awareness:

| Language | Trigger words |
|----------|--------------|
| Indonesian | kalau, gimana kalau, bagaimana jika, kalau saya, misalnya, simulasi |
| English | what if, if I, suppose I, simulate |

---

## 4. Backend Integration

### Endpoint (unchanged)

```
POST /chat/parse
Authorization: Bearer <token>
Content-Type: application/json

{
  "input": "Kalau aku cicil iPhone 15jt 12 bulan?"
}
```

### Response shape when `intent == "WHAT_IF"`

```json
{
  "intent": "WHAT_IF",
  "message": "BISA tapi KETAT...",
  "data": {
    "title": "Cicilan — Rp 15jt",
    "subtitle": "12 bulan × Rp 1.25jt/bln",
    "cashflow": {
      "before": 1500000,
      "after": 250000,
      "severity": "warning"
    },
    "financial_health": {
      "score": 62,
      "label": "moderate",
      "breakdown": {
        "dtiScore": 55,
        "cashflowScore": 60,
        "emergencyFundScore": 70,
        "goalProgressScore": 65
      }
    },
    "impacted_budgets": [
      { "category": "Entertainment", "before": 300000, "after": 0, "severity": "danger" },
      { "category": "Snack", "before": 500000, "after": 200000, "severity": "warning" }
    ],
    "goal_impacts": [
      {
        "name": "Dana Darurat",
        "delay_months": 5,
        "original_date": "2027-02-01",
        "new_date": "2027-07-01"
      }
    ],
    "verdict": {
      "severity": "warning",
      "headline": "BISA, tapi cashflow jadi sangat tipis",
      "message": "Setelah cicilan ini, sisa cashflow bulanan lo hanya Rp250rb. Satu kejadian tak terduga bisa bikin lo minus.",
      "coaching_note": "CFP recommendation: pastikan emergency fund minimal 3 bulan pengeluaran sebelum mengambil cicilan baru.",
      "urgency": "Budget Entertainment dan Snack lo harus dipotong habis untuk menutupi cicilan ini.",
      "alternatives": [
        {
          "label": "Cicil 24 bulan (Rp625rb/bln)",
          "impact": "Cashflow after: Rp875rb — jauh lebih aman",
          "action_type": "suggest_tenor",
          "priority": 1
        },
        {
          "label": "Nabung dulu 4.5jt sebagai DP",
          "impact": "Cicilan turun jadi Rp875rb/bln",
          "action_type": "suggest_dp",
          "priority": 2
        }
      ]
    },
    "apply_actions": [
      { "type": "create_liability", "params": { "name": "Cicilan", "amount": 15000000, "tenor": 12 } },
      { "type": "adjust_budget", "params": { "category": "Entertainment", "new_limit": 0 } }
    ]
  }
}
```

### Fields that can be null/absent

- `financial_health` — optional, render section only when present
- `impacted_budgets` — may be empty array, hide section
- `goal_impacts` — may be empty array, hide section
- `verdict.coaching_note` — optional
- `verdict.urgency` — optional
- `verdict.alternatives` — may be empty array

---

## 5. Data Models (Android)

### Main model

```kotlin
data class WhatIfSimulation(
    val title: String,
    val subtitle: String,
    val cashflow: WhatIfCashflowImpact,
    val financialHealth: WhatIfFinancialHealth?,
    val impactedBudgets: List<WhatIfBudgetImpact>,
    val goalImpacts: List<WhatIfGoalImpact>,
    val verdict: WhatIfVerdict,
    val applyActions: List<WhatIfApplyAction>
)
```

### Supporting models

```kotlin
data class WhatIfCashflowImpact(
    val before: Double,
    val after: Double,
    val severity: WhatIfSeverity   // "safe" | "warning" | "danger"
)

data class WhatIfFinancialHealth(
    val score: Int,               // 0–100
    val label: String,            // "good" | "moderate" | "poor"
    val breakdown: WhatIfHealthBreakdown?
)

data class WhatIfHealthBreakdown(
    val dtiScore: Int,
    val cashflowScore: Int,
    val emergencyFundScore: Int,
    val goalProgressScore: Int
)

data class WhatIfBudgetImpact(
    val category: String,
    val before: Double,
    val after: Double,
    val severity: WhatIfSeverity
)

data class WhatIfGoalImpact(
    val name: String,
    val delayMonths: Int,
    val originalDate: String?,
    val newDate: String?
)

data class WhatIfVerdict(
    val severity: WhatIfSeverity,
    val headline: String,
    val message: String,
    val coachingNote: String?,
    val urgency: String?,
    val alternatives: List<WhatIfAlternative>
)

data class WhatIfAlternative(
    val label: String,
    val impact: String,
    val actionType: String,
    val priority: Int
)

data class WhatIfApplyAction(
    val type: String,
    val params: Map<String, Any>
)

enum class WhatIfSeverity(val raw: String) {
    SAFE("safe"),
    WARNING("warning"),
    DANGER("danger");

    companion object {
        fun from(raw: String?) = values().firstOrNull { it.raw == raw } ?: WARNING
    }
}
```

### JSON key mapping (snake_case → camelCase)

| JSON key | Kotlin field |
|----------|-------------|
| `financial_health` | `financialHealth` |
| `impacted_budgets` | `impactedBudgets` |
| `goal_impacts` | `goalImpacts` |
| `apply_actions` | `applyActions` |
| `delay_months` | `delayMonths` |
| `original_date` | `originalDate` |
| `new_date` | `newDate` |
| `coaching_note` | `coachingNote` |
| `action_type` | `actionType` |

---

## 6. Platform Mapping (iOS → Android)

| iOS (Swift) | Android equivalent |
|-------------|-------------------|
| `ChatParseResult.intent == .whatIf` | Check `ChatParseResult.intent == "WHAT_IF"` |
| `WhatIfResultCard` (SwiftUI View) | `WhatIfResultCard` Composable |
| `WhatIfSimulation` domain model | `WhatIfSimulation` data class |
| `WhatIfDataDTO.toDomain()` | Gson/Moshi deserialization + mapping |
| `severityColor(_ severity)` | `severity.toColor()` extension |
| `severityIcon(_ severity)` | `severity.toIcon()` extension |
| `onResetTapped()` callback | `onResetTapped: () -> Unit` lambda |
| `formatCurrency(amount)` | `NumberFormat.getCurrencyInstance()` with user currency |
| `.transition(.scale.combined(with: .opacity))` | `AnimatedVisibility` with scale+fade |

---

## 7. Severity Color & Icon Mapping

| Severity | Color | Icon (Material) |
|----------|-------|-----------------|
| `safe` | `cashaSuccess` green (`#2E7D32`) | `Icons.Filled.CheckCircle` |
| `warning` | orange (`#FF9800`) | `Icons.Filled.Warning` |
| `danger` | `cashaDanger` red (`#F44336`) | `Icons.Filled.Cancel` |

Financial health score color:
- `75–100` → green
- `50–74` → orange
- `0–49` → red

---

## 8. Card UI Layout (Composable)

```
┌────────────────────────────────────────┐
│  Header                                │
│  [wand icon] [WHAT IF badge]           │
│  Title (bold)                          │
│  Subtitle (secondary)                  │
├────────────────────────────────────────┤
│  Cashflow Bulanan                      │
│  Sebelum: Rp1.5jt  →  Sesudah: Rp250rb│
│  [██████████████████] before bar       │
│  [████              ] after bar        │
├────────────────────────────────────────┤ (if present)
│  Financial Health                      │
│  [score ring 62]  moderate             │
│  DTI 55 · Cashflow 60 · ...            │
├────────────────────────────────────────┤ (if non-empty)
│  Budget Terdampak                      │
│  ⚠ Entertainment  Rp300rb → Rp0       │
│  ⚠ Snack          Rp500rb → Rp200rb   │
├────────────────────────────────────────┤ (if non-empty)
│  Dampak ke Goal                        │
│  ⚠ Dana Darurat — Mundur 5 bulan      │
│    2027-02-01 → 2027-07-01             │
├────────────────────────────────────────┤
│  Verdict                               │
│  ⚠ BISA, tapi cashflow jadi tipis     │
│  Full message text                     │
│  🎓 coaching note (optional)           │
│  ❗ urgency note (optional)            │
│  💡 Alternative 1: Cicil 24 bulan…    │
│     impact text                        │
│  💡 Alternative 2: Nabung DP dulu…    │
├────────────────────────────────────────┤
│  [ 🔄  Simulasi Baru ]                 │
└────────────────────────────────────────┘
```

---

## 9. ViewModel Changes

No new ViewModel needed. Extend the existing `ChatViewModel` (or equivalent):

```kotlin
// Inside existing ChatViewModel
sealed class ChatUiState {
    // ... existing states
    data class WhatIfResult(val simulation: WhatIfSimulation, val message: String) : ChatUiState()
}

// In parseChatResult():
"WHAT_IF" -> {
    val simulation = result.data?.toWhatIfSimulation()
    _uiState.value = ChatUiState.WhatIfResult(simulation, result.message)
    // DO NOT save to Room / local DB
    // DO NOT refresh transaction/income state
}
```

---

## 10. Chat View Changes (Composable)

```kotlin
// In ChatScreen composable, inside message list rendering:
when (val state = chatUiState) {
    is ChatUiState.WhatIfResult -> {
        WhatIfResultCard(
            simulation = state.simulation,
            onResetTapped = { viewModel.resetToIdle() }
        )
    }
    // ... existing cases for expense/income/payment
}
```

Bottom bar behavior when state is `WhatIfResult`:
- Hide normal input field
- Show single button: **"Simulasi Baru"** → calls `viewModel.resetToIdle()`

---

## 11. Phase 2 — Apply Actions (Disabled for Now)

The response includes `apply_actions` for when the user wants to actually apply the simulation. **Do not implement for Phase 1.**

```kotlin
// Phase 2 only:
// POST /whatif/apply
// Body: { applyActions: [...] }
// After apply: refresh wallet, budget, goal states
```

The "Terapkan" button should be hidden or shown as disabled with a "Coming Soon" tooltip.

---

## 12. File References (iOS → Android mapping)

| iOS File | Android equivalent |
|----------|--------------------|
| `Domain/Model/Chat/WhatIfResult.swift` | `domain/model/chat/WhatIfSimulation.kt` |
| `Data/.../DTOs/Chat/ChatParseDTO.swift` (WhatIfDataDTO section) | `data/remote/dto/chat/ChatParseDto.kt` |
| `App/Module/AddTransaction/Sources/WhatIfResultCard.swift` | `ui/chat/components/WhatIfResultCard.kt` |
| `App/Module/AddTransaction/Sources/AddMessageView.swift` (`handleChatResult`) | `ui/chat/ChatViewModel.kt` |
| `Domain/Model/Chat/ChatParseResult.swift` (`case .whatIf`) | `domain/model/chat/ChatIntent.kt` (`WHAT_IF`) |

---

## 13. Android Implementation Checklist

- [ ] Add `WHAT_IF` to `ChatIntent` enum
- [ ] Create `WhatIfSimulation` + supporting data classes
- [ ] Add `WhatIfDataDTO` deserialization in `ChatParseDto`
- [ ] Map `ChatParseDto.toDomain()` for `WHAT_IF` intent
- [ ] Add `WhatIfResult` state to `ChatUiState` sealed class
- [ ] Handle `WHAT_IF` in `ChatViewModel.parseChatResult()` — **skip DB save, skip state refresh**
- [ ] Create `WhatIfResultCard` Composable
  - [ ] Header section (badge + title + subtitle)
  - [ ] Cashflow section with before/after bars
  - [ ] Financial health ring (shown only if non-null)
  - [ ] Impacted budgets list (shown only if non-empty)
  - [ ] Goal impacts list (shown only if non-empty)
  - [ ] Verdict section (headline, message, coaching note, urgency, alternatives)
  - [ ] "Simulasi Baru" reset button
- [ ] Severity color + icon helper functions
- [ ] Currency formatter (use user's selected currency from prefs)
- [ ] Show "Simulasi Baru" button in bottom bar when state is `WhatIfResult`
- [ ] Animate card entry (scale + fade in)
- [ ] Phase 2: "Terapkan" button (hidden/disabled for now)
