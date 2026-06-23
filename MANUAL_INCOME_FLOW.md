# Manual Income Transaction Flow - Dokumentasi Lengkap

Dokumentasi ini menjelaskan alur lengkap dari user menginputkan manual income transaction sampai wallet balance terupdate di sistem.

---

## 📊 Flow Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         UI LAYER - AddIncomeView                         │
│                                                                           │
│  User Input:                                                              │
│  - Income Name                                                            │
│  - Amount                                                                 │
│  - Date & Time                                                            │
│  - Income Type (Salary, Bonus, Investment, etc)                          │
│  - Source (Optional)                                                      │
│  - Asset ID (Optional - link to wallet)                                  │
│  - Is Recurring                                                           │
│  - Frequency (if recurring)                                              │
│  - Note (Optional)                                                        │
│                                                                           │
│                          [Submit Button]                                  │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    STATE MANAGEMENT - IncomeState                        │
│                                                                           │
│  • Validasi input data                                                   │
│  • Create CreateIncomeRequest object                                     │
│  • Call addIncome() method                                               │
│                                                                           │
│  Step 1: Save to Local Database (Optimistic Update)                     │
│  └─ LocalIncomeRepositoryProtocol.addIncome()                           │
│     - Simpan dengan status "pending"                                     │
│     - Add ke @Published incomes list                                     │
│                                                                           │
│  Step 2: Sync ke Remote API (Async)                                      │
│  └─ CreateIncomeUseCase.execute()                                        │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    USE CASE LAYER - CreateIncomeUseCase                  │
│                                                                           │
│  • Validasi amount > 0                                                   │
│  • Validasi name tidak kosong                                            │
│  • Call RemoteIncomeRepositoryProtocol.createIncome()                   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│              DATA LAYER - IncomeRemoteRepositoryImpl                      │
│                                                                           │
│  • Format data sesuai API spec                                           │
│  • Tambahkan Authorization header (Bearer token)                         │
│  • Setup request parameters:                                             │
│    - name: String                                                        │
│    - amount: Double                                                      │
│    - datetime: ISO8601 format                                            │
│    - type: IncomeType enum                                               │
│    - source: Optional String                                             │
│    - assetId: Optional String                                            │
│    - isRecurring: Boolean                                                │
│    - frequency: Optional IncomeFrequency                                │
│    - note: Optional String                                               │
│                                                                           │
│  • Call NetworkClient.request()                                          │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    API ENDPOINT - POST /income                           │
│                                                                           │
│  Backend Processing:                                                      │
│  1. Receive income creation request                                      │
│  2. Validate request data                                                │
│  3. Create IncomeCasha record in database                                │
│  4. [IF assetId provided] Update associated wallet balance:             │
│     - GET wallet by assetId                                              │
│     - ADD amount ke wallet.balance                                       │
│     - UPDATE wallet in database                                          │
│  5. Return response dengan:                                              │
│     - id: Generated UUID                                                 │
│     - Created income data                                                │
│     - Updated wallet data (if applicable)                                │
│                                                                           │
│  Response Format:                                                        │
│  {                                                                        │
│    "status": "success",                                                  │
│    "data": {                                                              │
│      "id": "uuid-string",                                                │
│      "name": "Salary",                                                   │
│      "amount": 5000000,                                                  │
│      "datetime": "2026-06-23T10:30:00Z",                                │
│      "type": "SALARY",                                                   │
│      "source": "PT ABC",                                                 │
│      "assetId": "wallet-uuid",                                           │
│      "isRecurring": true,                                                │
│      "frequency": "MONTHLY",                                             │
│      "note": "Monthly salary",                                           │
│      "createdAt": "2026-06-23T10:30:00Z",                               │
│      "updatedAt": "2026-06-23T10:30:00Z"                                │
│    }                                                                      │
│  }                                                                        │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│            DATA LAYER RESPONSE - IncomeRemoteRepositoryImpl              │
│                                                                           │
│  • Parse response IncomeCreateResponse                                   │
│  • Map DTO to Domain Model (IncomeCasha)                                 │
│  • Return domain model ke Use Case                                       │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    STATE MANAGEMENT - IncomeState (cont'd)               │
│                                                                           │
│  Step 3: Handle Response & Update UI                                    │
│  • Mark local income as "synced"                                         │
│  • Replace local income dengan remote data                               │
│  • Update @Published incomes list                                        │
│  • Clear isLoading flag                                                  │
│  • Call loadSummary() untuk refresh summary                              │
│                                                                           │
│  ✓ Success Path: Show success message                                   │
│  ✗ Error Path: Keep local copy, queue untuk retry nanti                 │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│            🔄 WALLET UPDATE PROCESS (Automatic via Backend)              │
│                                                                           │
│  SCENARIO 1: User links income ke wallet (assetId provided)             │
│  ├─ Backend automatically updates wallet balance                         │
│  ├─ Wallet.balance = Wallet.balance + Income.amount                     │
│  └─ Next time wallet is refreshed, user sees updated balance            │
│                                                                           │
│  SCENARIO 2: Refreshing Wallet Data                                     │
│  ├─ Call GetWalletsUseCase.execute()                                    │
│  ├─ Hit: GET /wallets                                                    │
│  ├─ Receive latest wallet data dengan balance terupdate                 │
│  └─ Update UI dengan latest balance                                     │
│                                                                           │
│  SCENARIO 3: Get Wallet Summary                                         │
│  ├─ Call GetWalletSummaryUseCase.execute()                              │
│  ├─ Hit: GET /wallets/summary                                            │
│  ├─ Receive aggregated summary (liquid balance, credit used, etc)       │
│  └─ Display di dashboard                                                │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      UI LAYER - Success State                            │
│                                                                           │
│  • Income berhasil ditambahkan                                           │
│  • List terupdate dengan income baru                                     │
│  • Summary refreshed                                                     │
│  • Wallet balance terupdate (jika linked)                                │
│  • Navigate back ke IncomeListView atau show confirmation               │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📡 API Endpoints Yang Digunakan

### 1. **Create Income** (Endpoint Utama)
```
Method: POST
Path: /income
Headers:
  - Authorization: Bearer {token}
  - Content-Type: application/json

Request Body:
{
  "name": "Monthly Salary",
  "amount": 5000000,
  "datetime": "2026-06-23T10:30:00Z",
  "type": "SALARY",
  "source": "PT ABC Company",
  "assetId": "wallet-uuid-123",  // Optional - link ke wallet
  "isRecurring": true,
  "frequency": "MONTHLY",
  "note": "Monthly salary payment"
}

Response:
{
  "status": "success",
  "data": {
    "id": "income-uuid-456",
    "name": "Monthly Salary",
    "amount": 5000000,
    "datetime": "2026-06-23T10:30:00Z",
    "type": "SALARY",
    "source": "PT ABC Company",
    "assetId": "wallet-uuid-123",
    "isRecurring": true,
    "frequency": "MONTHLY",
    "note": "Monthly salary payment",
    "createdAt": "2026-06-23T10:30:00Z",
    "updatedAt": "2026-06-23T10:30:00Z"
  }
}
```

**File:** [Core/Sources/Networking/Endpoint.swift](Core/Sources/Networking/Endpoint.swift#L27)

---

### 2. **Get Incomes** (Refresh data)
```
Method: GET
Path: /income
Headers:
  - Authorization: Bearer {token}

Query Parameters:
  - type: Optional[IncomeType]  // SALARY, BONUS, INVESTMENT, etc
  - startDate: Optional[String]
  - endDate: Optional[String]

Response:
{
  "status": "success",
  "data": [
    {
      "id": "income-uuid-456",
      "name": "Monthly Salary",
      "amount": 5000000,
      // ... rest of fields
    },
    // ... more incomes
  ]
}
```

**File:** [Core/Sources/Networking/Endpoint.swift](Core/Sources/Networking/Endpoint.swift#L28)

---

### 3. **Get Income Summary**
```
Method: GET
Path: /income/summary
Headers:
  - Authorization: Bearer {token}

Query Parameters:
  - month: String (format: "2026-06")

Response:
{
  "status": "success",
  "data": {
    "totalIncome": 15000000,
    "count": 3,
    "byType": [
      {
        "type": "SALARY",
        "total": 10000000,
        "count": 2
      },
      {
        "type": "BONUS",
        "total": 5000000,
        "count": 1
      }
    ]
  }
}
```

**File:** [Core/Sources/Networking/Endpoint.swift](Core/Sources/Networking/Endpoint.swift#L29)

---

### 4. **Get Wallets** (Untuk refresh wallet data setelah income update)
```
Method: GET
Path: /wallets
Headers:
  - Authorization: Bearer {token}

Response:
{
  "status": "success",
  "data": [
    {
      "id": "wallet-uuid-123",
      "name": "Main Wallet",
      "type": "LIQUID",
      "source": "ASSET",
      "balance": 15000000,  // ✓ Termasuk income yang baru ditambahkan
      "createdAt": "2026-01-01T00:00:00Z",
      "updatedAt": "2026-06-23T10:30:00Z"
    }
  ]
}
```

**File:** [Core/Sources/Networking/Endpoint.swift](Core/Sources/Networking/Endpoint.swift#L214)

---

### 5. **Get Wallet Summary** (Aggregated view)
```
Method: GET
Path: /wallets/summary
Headers:
  - Authorization: Bearer {token}

Response:
{
  "status": "success",
  "data": {
    "liquidBalance": 15000000,      // Total liquid assets
    "totalCreditUsed": 2000000,     // Total credit used
    "totalCreditLimit": 10000000,   // Total credit limit
    "availableCredit": 8000000,     // Available credit
    "walletCount": 3
  }
}
```

**File:** [Core/Sources/Networking/Endpoint.swift](Core/Sources/Networking/Endpoint.swift#L215)

---

## 🔄 Data Flow Timeline

### User Perspective:
```
t=0s   User enters income details
t=0.1s User taps "Submit" button
       
t=0.2s IncomeState.addIncome() is called
       └─ Create local record immediately
       
t=0.3s Local income appears in list (Optimistic Update)
       
t=0.5s Request sent to backend: POST /income
       
t=1.0s Backend responds with created income
       └─ Backend also updated linked wallet (if assetId provided)
       
t=1.2s Local record marked as synced
       
t=1.5s User navigates to wallet screen
       └─ Initial balance still shows old value
       
t=1.6s User refreshes wallet or wallet auto-refreshes
       └─ GET /wallets hit
       
t=2.0s Backend returns updated wallet balance
       └─ Balance now includes income amount
       
t=2.1s Wallet UI updated with new balance ✓
```

---

## 💾 Data Persistence

### Local Storage (SQLite via Core Data):
- **IncomeEntity** - Stores income records locally
  - Synced status tracking
  - Offline support
  - Quick access untuk UI

- **Sync Strategy:**
  1. Save locally immediately
  2. Async sync to remote
  3. If sync succeeds → mark as synced
  4. If sync fails → keep local, retry later

### Remote Storage (Backend API):
- **Income Table** - Authoritative source
- **Wallet/Asset Balance** - Auto-updated when income linked to asset

---

## 📝 Related Use Cases

### CreateIncomeUseCase
**File:** [Domain/UseCase/Income/CreateIncomeUseCase.swift](Domain/UseCase/Income/CreateIncomeUseCase.swift)

```swift
// Validations:
- Amount must be > 0
- Name cannot be empty
```

### AddIncomeLocalUseCase
**File:** [Domain/UseCase/Income/Local/AddIncomeLocalUseCase.swift](Domain/UseCase/Income/Local/AddIncomeLocalUseCase.swift)

```swift
// Save to local database
// Used for optimistic updates and offline support
```

### GetWalletsUseCase
**File:** [Domain/UseCase/Wallet/GetWalletsUseCase.swift](Domain/UseCase/Wallet/GetWalletsUseCase.swift)

```swift
// Fetch latest wallet data
// Called after income update to show new balance
```

### UpdateWalletUseCase
**File:** [Domain/UseCase/Wallet/UpdateWalletUseCase.swift](Domain/UseCase/Wallet/UpdateWalletUseCase.swift)

```swift
// Manual wallet update if needed
// Endpoint: PATCH /assets/:id or /loans/:id
```

### GetWalletSummaryUseCase
**File:** [Domain/UseCase/Wallet/GetWalletSummaryUseCase.swift](Domain/UseCase/Wallet/GetWalletSummaryUseCase.swift)

```swift
// Get aggregated wallet summary
// Shows total liquid balance, credit info, etc
```

---

## 🏗️ Architecture Layers

```
┌─────────────────────────────────────┐
│      UI LAYER (SwiftUI)              │
│  - AddIncomeView                     │
│  - IncomeListView                    │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│  STATE MANAGEMENT (ObservableObject) │
│  - IncomeState                       │
│  - Owns: AddIncome, GetIncomes, etc  │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   USE CASE LAYER (Business Logic)    │
│  - CreateIncomeUseCase               │
│  - GetIncomesUseCase                 │
│  - GetWalletsUseCase                 │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   REPOSITORY LAYER (Data Access)     │
│  - RemoteIncomeRepositoryImpl         │
│  - LocalIncomeRepositoryImpl          │
│  - WalletRepositoryImpl               │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   NETWORKING LAYER (HTTP)            │
│  - NetworkClient                     │
│  - Endpoints                         │
│  - Request/Response handling         │
└─────────────────────────────────────┘
```

---

## ⚠️ Error Handling

### Validation Errors (Client-side):
- Empty income name → Show error message
- Amount ≤ 0 → Show error message
- Invalid date → Show error picker

### Network Errors:
- Backend returns error → Retry mechanism
- No internet → Keep local, sync when online
- Timeout → Show retry button

### Success with Offline:
- Income saved locally
- Badge shown indicating "pending sync"
- Auto-sync when network returns

---

## 🔗 Related Documentation

- [WALLET_FEATURE.md](WALLET_FEATURE.md) - Wallet management details
- [SYSTEM_DESIGN.md](SYSTEM_DESIGN.md) - Overall architecture
- [casha-widget-flow.md](casha-widget-flow.md) - Widget flow (similar pattern)

---

## 🎯 Quick Reference - Key Files

| Component | File | Purpose |
|-----------|------|---------|
| UI | [AddIncomeView.swift](App/Module/Income/Sources/main/AddIncomeView.swift) | Income input form |
| State | [IncomeState.swift](App/Module/Income/Sources/state/IncomeState.swift) | State management |
| UseCase | [CreateIncomeUseCase.swift](Domain/UseCase/Income/CreateIncomeUseCase.swift) | Business logic |
| Repository | [IncomeRemoteRepositoryImpl.swift](Data/Repository/Implementation/Remote/IncomeRemoteRepositoryImpl.swift) | API calls |
| Models | [Income.swift](Domain/Model/Income/Income.swift) | Domain models |
| Endpoints | [Endpoint.swift](Core/Sources/Networking/Endpoint.swift) | All API endpoints |
| Wallet UseCase | [GetWalletsUseCase.swift](Domain/UseCase/Wallet/GetWalletsUseCase.swift) | Wallet refresh |
| Wallet Repo | [WalletRepositoryImpl.swift](Data/Repository/Implementation/Remote/WalletRepositoryImpl.swift) | Wallet API calls |

---

## 📞 Support

Untuk pertanyaan lebih lanjut atau report bug, silakan buka issue di repository ini.

Generated: June 23, 2026
