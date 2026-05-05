# Wallet Feature — Full Integration Guide

Reference document for Wallet feature implementation (iOS → Android porting).

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [API Endpoints](#api-endpoints)
3. [UI/UX Flow](#uiux-flow)
4. [Screen Specifications](#screen-specifications)
5. [State Management](#state-management)
6. [Data Models](#data-models)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                          │
│  Dashboard (WalletCardDeck) · WalletListView            │
│  AddWalletView · EditWalletView · TransferWalletView    │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                    State (ViewModel)                     │
│  WalletState — @Published wallets, summary, default     │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                    Use Cases                             │
│  GetWallets · GetWalletSummary · AddLiquidWallet        │
│  AddCreditCard · UpdateWallet · DeleteWallet            │
│  SetDefaultWallet · ClearDefaultWallet · TransferWallet │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                    Repository                            │
│  WalletRepositoryImpl → Alamofire/NetworkClient          │
└─────────────────────────────────────────────────────────┘
```

---

## API Endpoints

### Common Headers

```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json   (POST / PATCH only)
```

### Common Response Wrapper

```json
{
  "data": <payload>,
  "message": "success",
  "status": 200
}
```

---

### 1. GET /wallets — Fetch All Wallets

**Response:**
```json
{
  "data": [
    {
      "id": "abc123",
      "source": "ASSET",
      "name": "BCA",
      "type": "SAVINGS_ACCOUNT",
      "balance": 7000000,
      "description": null,
      "bankName": "BCA",
      "creditLimit": null,
      "billingDay": null,
      "dueDay": null,
      "minimumPayment": null,
      "createdAt": "2025-01-01T00:00:00.000Z",
      "updatedAt": "2025-05-01T00:00:00.000Z"
    },
    {
      "id": "def456",
      "source": "LOAN",
      "name": "Citi Platinum",
      "type": "CREDIT_CARD",
      "balance": 2500000,
      "bankName": "Citibank",
      "creditLimit": 10000000,
      "billingDay": 25,
      "dueDay": 15,
      "minimumPayment": 125000,
      "createdAt": "2025-02-01T00:00:00.000Z",
      "updatedAt": "2025-05-01T00:00:00.000Z"
    }
  ]
}
```

**Enums:**

| Field | Values |
|-------|--------|
| `source` | `ASSET` (liquid) · `LOAN` (credit card) |
| `type` | `CASH` · `SAVINGS_ACCOUNT` · `CHECKING_ACCOUNT` · `E_WALLET` · `CREDIT_CARD` · `OTHER` |

---

### 2. GET /wallets/summary — Dashboard Summary

**Response:**
```json
{
  "data": {
    "liquidBalance": 15000000,
    "totalCreditUsed": 2500000,
    "totalCreditLimit": 10000000,
    "availableCredit": 7500000,
    "walletCount": 3
  }
}
```

---

### 3. POST /assets — Create Liquid Wallet

**Request:**
```json
{
  "name": "BCA Tabungan",
  "type": "SAVINGS_ACCOUNT",
  "amount": 5000000,
  "description": "Rekening utama",
  "bankName": "BCA"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `name` | string | ✅ | Wallet name |
| `type` | string | ✅ | One of: CASH, SAVINGS_ACCOUNT, CHECKING_ACCOUNT, E_WALLET, OTHER |
| `amount` | number | ✅ | Initial balance (NOT "balance") |
| `description` | string | ❌ | Optional description |
| `bankName` | string | ❌ | Optional bank/issuer |

**Response:** → `WalletDTO` in `data`

---

### 4. POST /liabilities — Create Credit Card

**Request:**
```json
{
  "name": "Citi Platinum",
  "category": "CREDIT_CARD",
  "bankName": "Citibank",
  "creditLimit": 10000000,
  "currentBalance": 2500000,
  "interestRate": 2.25,
  "principal": 0,
  "billingDay": 25,
  "dueDay": 15,
  "interestType": "MONTHLY",
  "minPaymentPercentage": 5.0,
  "lateFee": 150000
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `name` | string | ✅ | Card name |
| `category` | string | ✅ | Always `"CREDIT_CARD"` |
| `bankName` | string | ✅ | Bank/issuer |
| `creditLimit` | number | ✅ | Total limit |
| `currentBalance` | number | ✅ | Amount already used |
| `interestRate` | number | ✅ | Monthly rate |
| `principal` | number | ✅ | Always `0` for new CC |
| `billingDay` | int | ❌ | 1–31 |
| `dueDay` | int | ❌ | 1–31 |
| `interestType` | string | ❌ | `"MONTHLY"` or `"FLAT"` |
| `minPaymentPercentage` | number | ❌ | e.g. `5.0` = 5% |
| `lateFee` | number | ❌ | Late fee amount |

**Response:** → `LiabilityDTO` in `data` (map to wallet locally)

---

### 5. PATCH /assets/:id or /loans/:id — Update Wallet

Route based on source:
- `ASSET` → `PATCH /assets/:id`
- `LOAN` → `PATCH /loans/:id`

**Request (all fields optional):**
```json
{
  "name": "BCA Baru",
  "amount": 8000000
}
```

---

### 6. DELETE /assets/:id or /loans/:id — Delete Wallet

Route based on source:
- `ASSET` → `DELETE /assets/:id`
- `LOAN` → `DELETE /loans/:id`

No request body.

---

### 7. PATCH /wallets/default — Set Default Wallet

```json
{
  "walletId": "abc123"
}
```

---

### 8. DELETE /wallets/default — Clear Default Wallet

No request body.

---

### 9. POST /wallets/transfer — Transfer Between Wallets

**Request:**
```json
{
  "fromWalletId": "abc123",
  "toWalletId": "xyz789",
  "amount": 2000000,
  "note": "Transfer ke tabungan"
}
```

| Field | Type | Required |
|-------|------|----------|
| `fromWalletId` | string | ✅ |
| `toWalletId` | string | ✅ |
| `amount` | number | ✅ |
| `note` | string | ❌ |

**Response:**
```json
{
  "data": {
    "from": { "id": "abc123", "name": "BCA", "balance": 5000000, ... },
    "to":   { "id": "xyz789", "name": "Tabungan", "balance": 4500000, ... }
  }
}
```

---

## UI/UX Flow

### Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                          DASHBOARD                                     │
│                                                                        │
│  ┌─────────────────────────────────────────┐                          │
│  │         WALLET CARD DECK                 │  ← Main featured card   │
│  │  Net Cashflow / Wallet balance           │     (swipe L/R to cycle)│
│  └─────────────────────────────────────────┘                          │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐                                 │
│  │Thumb1│ │Thumb2│ │Thumb3│ │Thumb4│  ← Horizontal thumbnail strip    │
│  └──────┘ └──────┘ └──────┘ └──────┘     Tap = switch featured card   │
│                                                                        │
│  [Reports] [Goals] [Recent Transactions]                               │
└──────────────────────────────────────────────────────────────────────┘
        │
        │ (Tap "Manage Wallets" or from TabBar/Settings)
        ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      WALLET LIST VIEW                                  │
│                                                                        │
│  Toolbar: [Done]        [↔ Transfer]  [+ Add]                         │
│                                                                        │
│  ┌─ Summary Card ───────────────────────────┐                         │
│  │ Total Cash: Rp15.000.000                  │                         │
│  │ Available Credit: Rp7.500.000             │                         │
│  │ CC Used: Rp2.500.000 / Limit: Rp10.000.000│                        │
│  └───────────────────────────────────────────┘                         │
│                                                                        │
│  Section: "Cash & Bank Accounts"                                       │
│  ┌──────────────────────────────────────────┐                         │
│  │ 🏦 BCA              Rp7.000.000  [★ Default]│                      │
│  │ 📱 GoPay            Rp500.000               │                      │
│  └──────────────────────────────────────────┘                         │
│                                                                        │
│  Section: "Credit Cards"                                               │
│  ┌──────────────────────────────────────────┐                         │
│  │ 💳 Citi Platinum     Rp2.500.000 used     │                         │
│  │    Available: Rp7.500.000                  │                         │
│  └──────────────────────────────────────────┘                         │
│                                                                        │
│  Actions per wallet:                                                   │
│  • Tap → toggle default                                                │
│  • Swipe left → Edit / Delete                                          │
└──────────────────────────────────────────────────────────────────────┘
        │                    │                    │
        ▼                    ▼                    ▼
┌─────────────┐   ┌──────────────────┐   ┌─────────────────────┐
│ ADD WALLET  │   │ EDIT WALLET      │   │ TRANSFER WALLET     │
└─────────────┘   └──────────────────┘   └─────────────────────┘
```

---

### Navigation Flow

```
Dashboard
 └─ WalletCardDeck (embedded)
     ├─ Swipe/Tap → cycle between cards
     └─ Eye icon → toggle balance visibility

WalletListView (sheet from Dashboard or Tab)
 ├─ [+ Add] → AddWalletView (sheet)
 │    ├─ Segmented: [Cash/Bank] | [Credit Card]
 │    ├─ Form fields
 │    └─ Save → POST /assets or POST /liabilities
 ├─ [↔ Transfer] → TransferWalletView (sheet)
 │    ├─ Select From wallet
 │    ├─ Select To wallet
 │    ├─ Enter amount
 │    ├─ Optional note
 │    └─ Confirm → POST /wallets/transfer
 ├─ Tap wallet row → toggle as default
 ├─ Swipe left → EditWalletView (sheet)
 │    ├─ Edit name / balance
 │    └─ Save → PATCH /assets/:id or /loans/:id
 └─ Swipe left → Delete (confirmation dialog)
      └─ Confirm → DELETE /assets/:id or /loans/:id
```

---

## Screen Specifications

### A. Dashboard — WalletCardDeck

| Element | Spec |
|---------|------|
| Layout | Featured card (200pt height) + horizontal thumbnail strip below |
| Featured card | Shows full wallet detail or Net Cashflow |
| Thumbnail strip | 130×76pt cards, horizontal scroll |
| Interaction | Swipe L/R on featured = cycle; Tap thumbnail = switch |
| Animation | Spring (response 0.44, damping 0.8) on card transition |
| Eye toggle | Hides ALL balances simultaneously |
| Period picker | Only on Net Cashflow card (dropdown sheet) |
| Sync indicator | Featured card opacity 0.6 when syncing |

**Card types in deck:**
1. **Net Cashflow** (always index 0) — shows total income, expense, net
2. **Wallet cards** (index 1…n) — each wallet from `GET /wallets`

---

### B. WalletListView

| Element | Spec |
|---------|------|
| Layout | ScrollView with summary card + sectioned wallet list |
| Toolbar left | "Done" button → dismiss |
| Toolbar right | Transfer icon (↔) + Add icon (+) |
| Summary card | liquidBalance, availableCredit, CC used/limit |
| Sections | "Cash & Bank Accounts" (source=ASSET) + "Credit Cards" (source=LOAN) |
| Wallet row | Icon + Name + Balance + Default badge |
| Default toggle | Tap row = set default (PATCH /wallets/default) |
| Edit | Swipe left or long press → EditWalletView |
| Delete | Swipe left → confirmation dialog |
| Toast | Success/Error banner at bottom overlay |

---

### C. AddWalletView

| Element | Spec |
|---------|------|
| Segmented control | "Cash / Bank" vs "Credit Card" |
| **Liquid wallet form** | |
| - Name | TextField, required |
| - Type | Picker: Cash, Savings, Checking, E-Wallet, Other |
| - Initial Balance | Numeric TextField |
| - Bank Name | TextField, optional |
| - Description | TextField, optional |
| **Credit card form** | |
| - Card Name | TextField |
| - Bank/Issuer | TextField |
| - Credit Limit | Numeric TextField |
| - Current Balance Used | Numeric TextField |
| - Billing Day + Due Day | Side-by-side HStack, 1–31 picker |
| - Interest Rate | Numeric TextField |
| - Interest Type | Picker: MONTHLY / FLAT |
| - Min Payment % | Numeric TextField |
| - Late Fee | Numeric TextField |
| Save button | Disabled until required fields filled |

---

### D. EditWalletView

| Element | Spec |
|---------|------|
| Fields | Name (editable) + Balance (editable) |
| Pre-filled | From existing wallet data |
| Save | PATCH /assets/:id or /loans/:id |

---

### E. TransferWalletView

| Element | Spec |
|---------|------|
| From selector | Tap → sheet with wallet list (excludes "To" wallet) |
| To selector | Tap → sheet with wallet list (excludes "From" wallet) |
| Swap button | ↕ icon between from/to — swaps them |
| Amount | Large numeric input with currency prefix |
| Validation | Amount > 0, from ≠ to, balance check for ASSET source |
| Note | Optional text field |
| Transfer button | Full-width CTA, disabled until valid |
| Loading state | ProgressView replaces button text |
| Success | Haptic + green banner + auto-dismiss 1.5s |
| Error | Haptic + red error message |

**Wallet Picker (sub-sheet):**
- Shows all wallets EXCEPT the one already selected in the other slot
- Each row: icon + name + bank + balance + type badge
- Tap to select → dismiss picker

---

### F. DefaultWalletPromptView

| Element | Spec |
|---------|------|
| Trigger | Dashboard `.task` — shown when wallets loaded but no default set |
| Layout | Sheet with wallet list + "Set as Default" button |
| Behavior | Tap wallet → checkmark; Tap "Set as Default" → PATCH /wallets/default → dismiss |

---

## State Management

### WalletState (ViewModel equivalent)

```
Published Properties:
├── wallets: [Wallet]           — all wallets
├── summary: WalletSummary?     — dashboard summary
├── defaultWalletId: String?    — current default
├── isLoading: Bool
├── errorMessage: String?
└── successMessage: String?

Computed Properties:
├── liquidWallets: [Wallet]     — wallets.filter { source == .asset }
└── creditWallets: [Wallet]     — wallets.filter { source == .loan }

Methods:
├── loadAll()                   — parallel fetch wallets + summary
├── fetchWallets()              — GET /wallets
├── fetchSummary()              — GET /wallets/summary
├── setDefault(walletId:)       — optimistic update + PATCH
├── clearDefault()              — optimistic update + DELETE
├── addLiquidWallet(_:)         — POST /assets → append to list
├── addCreditCard(_:)           — POST /liabilities → append to list
├── updateWallet(id:source:request:) — PATCH → update in list
├── deleteWallet(id:source:)    — DELETE → remove from list
├── transferWallet(_:)          — POST /wallets/transfer → update both wallets
└── clearMessages()             — reset error/success
```

### Refresh triggers:
- After transaction created (expense/income) → `fetchWallets()` + `fetchSummary()`
- After add/edit/delete wallet → local update (no re-fetch needed)
- After transfer → update both `from` and `to` wallet locally from response
- App foreground → `refreshDashboard()` which includes wallet refresh

---

## Data Models

### Wallet

```kotlin
// Android equivalent
data class Wallet(
    val id: String,
    val source: WalletSource,     // ASSET | LOAN
    val name: String,
    val type: WalletType,         // CASH | SAVINGS_ACCOUNT | CHECKING_ACCOUNT | E_WALLET | CREDIT_CARD | OTHER
    val balance: Double,
    val description: String?,
    val createdAt: String,        // ISO8601
    val updatedAt: String,        // ISO8601
    val bankName: String?,
    val creditLimit: Double?,
    val billingDay: Int?,
    val dueDay: Int?,
    val minimumPayment: Double?,
) {
    val availableCredit: Double?
        get() = if (source == LOAN && creditLimit != null) max(0.0, creditLimit - balance) else null
}

enum class WalletSource { ASSET, LOAN }
enum class WalletType { CASH, SAVINGS_ACCOUNT, CHECKING_ACCOUNT, E_WALLET, CREDIT_CARD, OTHER }
```

### WalletSummary

```kotlin
data class WalletSummary(
    val liquidBalance: Double,
    val totalCreditUsed: Double,
    val totalCreditLimit: Double,
    val availableCredit: Double,
    val walletCount: Int
)
```

### Request Models

```kotlin
data class AddLiquidWalletRequest(
    val name: String,
    val type: String,          // WalletType.name
    val amount: Double,        // ⚠️ Backend key is "amount" NOT "balance"
    val description: String? = null,
    val bankName: String? = null
)

data class AddCreditCardRequest(
    val name: String,
    val category: String = "CREDIT_CARD",
    val bankName: String,
    val creditLimit: Double,
    val currentBalance: Double,
    val interestRate: Double,
    val principal: Double = 0.0,
    val billingDay: Int? = null,
    val dueDay: Int? = null,
    val interestType: String? = null,    // "MONTHLY" | "FLAT"
    val minPaymentPercentage: Double? = null,
    val lateFee: Double? = null
)

data class UpdateWalletRequest(
    val name: String? = null,
    val amount: Double? = null   // ⚠️ Key is "amount" NOT "balance"
)

data class TransferWalletRequest(
    val fromWalletId: String,
    val toWalletId: String,
    val amount: Double,
    val note: String? = null
)
```

### Response for Transfer

```kotlin
data class TransferWalletResponse(
    val from: Wallet,
    val to: Wallet
)
```

---

## Important Notes

### ⚠️ API Field Name Gotchas

| What you think | What backend expects |
|----------------|---------------------|
| `balance` | `amount` (POST /assets, PATCH /assets/:id) |
| POST /loans | ❌ Use `POST /liabilities` for credit cards |
| balance field in CC | `currentBalance` (not `balance`) |

### Routing by Source

```
if (wallet.source == ASSET) {
    create → POST /assets
    update → PATCH /assets/${id}
    delete → DELETE /assets/${id}
} else { // LOAN
    create → POST /liabilities
    update → PATCH /loans/${id}
    delete → DELETE /loans/${id}
}
```

### Default Wallet

- Used by AI to auto-assign transactions to a wallet
- Only ONE wallet can be default at a time
- Stored server-side, returned via user profile (`profile.defaultWalletId`)
- Visually indicated with a ★ badge on wallet cards

### Wallet in Transactions

- When creating a transaction, optionally pass `assetId` to link it to a wallet
- After transaction creation, refresh wallet balances (`fetchWallets` + `fetchSummary`)
