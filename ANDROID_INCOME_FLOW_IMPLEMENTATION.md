# Manual Income Transaction Flow - Android Implementation

Dokumentasi ini menjelaskan alur lengkap dari user menginputkan manual income transaction sampai wallet balance terupdate di sistem Casha Android.

---

## 📊 Flow Overview - Android Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│              UI LAYER - AddTransactionScreen (Compose)                  │
│                                                                           │
│  User Input:                                                              │
│  - Income Name                                                            │
│  - Amount                                                                 │
│  - Date & Time                                                            │
│  - Income Type (SALARY, FREELANCE, BUSINESS, INVESTMENT, GIFT, OTHER)   │
│  - Source (Optional)                                                      │
│  - Asset ID (Optional - link to wallet)                                  │
│  - Is Recurring                                                           │
│  - Frequency (if recurring)                                              │
│  - Note (Optional)                                                        │
│                                                                           │
│  File: app/src/main/java/com/casha/app/ui/feature/transaction/         │
│        AddTransactionScreen.kt                                           │
│                          [Save Button]                                   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                 STATE MANAGEMENT - TransactionViewModel                  │
│                                                                           │
│  • Validasi input data                                                   │
│  • Create CreateIncomeRequest object                                     │
│  • Call addIncome() method                                               │
│                                                                           │
│  Step 1: Save to Local Database (Optimistic Update)                     │
│  └─ IncomeRepositoryImpl.saveIncome()                                    │
│     - Simpan ke IncomeEntity dengan status "pending sync"               │
│     - Add ke observable incomes list                                    │
│                                                                           │
│  Step 2: Sync ke Remote API (Async)                                      │
│  └─ AddIncomeUseCase.invoke()                                            │
│                                                                           │
│  File: app/src/main/java/com/casha/app/ui/feature/transaction/         │
│        TransactionViewModel.kt                                           │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                 USE CASE LAYER - AddIncomeUseCase                        │
│                                                                           │
│  • Validasi amount > 0                                                   │
│  • Validasi name tidak kosong                                            │
│  • Call IncomeRepository.saveIncome()                                    │
│                                                                           │
│  File: app/src/main/java/com/casha/app/domain/usecase/transaction/     │
│        AddIncomeUseCase.kt                                               │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│             DATA LAYER - IncomeRepositoryImpl                             │
│                                                                           │
│  1. OPTIMISTIC LOCAL SAVE:                                               │
│     - Create IncomeEntity dari CreateIncomeRequest                       │
│     - Set isSynced = false (indicates pending)                           │
│     - Save ke IncomeDao.insertIncome()                                   │
│     - Return immediately untuk UI responsiveness                         │
│                                                                           │
│  2. ASYNC REMOTE SYNC (Background):                                      │
│     - Format data sesuai API spec (CreateIncomeRequestDto)               │
│     - Tambahkan Authorization header (Bearer token)                      │
│     - Setup request parameters:                                          │
│       * name: String                                                     │
│       * amount: Double                                                   │
│       * datetime: ISO8601 format                                         │
│       * type: IncomeType enum (SALARY, FREELANCE, etc)                   │
│       * source: Optional String                                          │
│       * assetId: Optional String (link ke wallet)                        │
│       * isRecurring: Boolean                                             │
│       * frequency: Optional IncomeFrequency                              │
│       * note: Optional String                                            │
│                                                                           │
│  3. Call IncomeApiService.createIncome()                                 │
│                                                                           │
│  File: app/src/main/java/com/casha/app/data/remote/impl/               │
│        IncomeRepositoryImpl.kt                                            │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│            API SERVICE LAYER - IncomeApiService                          │
│                                                                           │
│  Interface Definition:                                                   │
│  - POST /income                                                           │
│  - GET /income                                                            │
│  - GET /income/summary                                                    │
│                                                                           │
│  File: app/src/main/java/com/casha/app/data/remote/api/                │
│        FeatureApiServices.kt (contains IncomeApiService)                 │
│                                                                           │
│  Retrofit configuration:                                                 │
│  @POST("income")                                                         │
│  suspend fun createIncome(                                               │
│    @Body request: CreateIncomeRequestDto                                 │
│  ): BaseResponse<IncomeDto>                                              │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                  NETWORK LAYER - Retrofit Request                        │
│                                                                           │
│  Network Client handles:                                                 │
│  1. Authorization (Bearer token)                                         │
│  2. Request serialization (JSON)                                         │
│  3. Response parsing                                                     │
│  4. Error handling (network errors, timeouts)                            │
│  5. Retry logic (exponential backoff)                                    │
│                                                                           │
│  Post /income                                                             │
│  Request Body:                                                           │
│  {                                                                        │
│    "name": "Monthly Salary",                                             │
│    "amount": 5000000,                                                    │
│    "datetime": "2026-06-23T10:30:00Z",                                  │
│    "type": "SALARY",                                                     │
│    "source": "PT ABC",                                                   │
│    "assetId": "wallet-uuid-123",                                         │
│    "isRecurring": true,                                                  │
│    "frequency": "MONTHLY",                                               │
│    "note": "Monthly salary"                                              │
│  }                                                                        │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    BACKEND API - POST /income                            │
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
│           DATA LAYER RESPONSE - IncomeRepositoryImpl (cont'd)             │
│                                                                           │
│  3. SYNC SUCCESS HANDLING:                                               │
│     - Parse response IncomeDto                                           │
│     - Map DTO to Domain Model (IncomeCasha)                              │
│     - Update local IncomeEntity:                                         │
│       * Mark isSynced = true                                             │
│       * Set remoteId from response                                       │
│       * Update createdAt/updatedAt from server                           │
│     - Save updated entity to database                                    │
│                                                                           │
│  4. TRIGGER WALLET REFRESH (if assetId provided):                        │
│     - Emit SyncEventBus.emitSyncCompleted()                              │
│     - This notifies DashboardViewModel to refresh wallet                 │
│                                                                           │
│  5. SYNC FAILURE HANDLING:                                               │
│     - Keep local IncomeEntity with isSynced = false                      │
│     - Mark as "pending sync" for retry later                             │
│     - Throw exception for UI error handling                              │
│                                                                           │
│  File: app/src/main/java/com/casha/app/data/remote/impl/               │
│        IncomeRepositoryImpl.kt                                            │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│            🔄 WALLET UPDATE PROCESS (Automatic via Backend)              │
│                                                                           │
│  SCENARIO 1: User links income ke wallet (assetId provided)             │
│  ├─ Backend automatically updates wallet balance                         │
│  ├─ Wallet.balance = Wallet.balance + Income.amount                     │
│  ├─ DashboardViewModel receives SyncEventBus signal                     │
│  └─ Triggers refreshDashboard(force = true)                             │
│                                                                           │
│  SCENARIO 2: Automatic Wallet Refresh via Sync Event                    │
│  ├─ SyncEventBus triggers dashboard refresh                              │
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
│                                                                           │
│  File: app/src/main/java/com/casha/app/ui/feature/dashboard/           │
│        DashboardViewModel.kt                                             │
│        setupSyncEventListener()                                          │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│           STATE MANAGEMENT - TransactionViewModel (cont'd)               │
│                                                                           │
│  After API response is handled:                                          │
│  • Mark local income as "synced"                                         │
│  • Replace local income dengan remote data                               │
│  • Update @StateFlow incomes list (triggers recomposition)              │
│  • Clear isLoading flag                                                  │
│  • Emit success notification                                             │
│                                                                           │
│  ✓ Success Path: Show success toast/snackbar, navigate back             │
│  ✗ Error Path: Keep local copy, show retry button                       │
│                                                                           │
│  File: app/src/main/java/com/casha/app/ui/feature/transaction/         │
│        TransactionViewModel.kt                                           │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                  UI LAYER - Success State                                │
│                                                                           │
│  ✓ Income berhasil ditambahkan                                           │
│  ✓ List terupdate dengan income baru                                     │
│  ✓ Summary refreshed otomatis                                            │
│  ✓ Wallet balance terupdate (jika linked ke asset)                       │
│  ✓ Navigate back ke transaction list atau show confirmation             │
│                                                                           │
│  File: app/src/main/java/com/casha/app/ui/feature/transaction/         │
│        AddTransactionScreen.kt                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📡 API Endpoints Dan Responses

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

**File:** [FeatureApiServices.kt](app/src/main/java/com/casha/app/data/remote/api/FeatureApiServices.kt#L52)

---

### 2. **Get Incomes** (Refresh data)
```
Method: GET
Path: /income
Headers:
  - Authorization: Bearer {token}

Query Parameters:
  - type: Optional[IncomeType]  // SALARY, FREELANCE, BUSINESS, INVESTMENT, GIFT, OTHER
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

**File:** [FeatureApiServices.kt](app/src/main/java/com/casha/app/data/remote/api/FeatureApiServices.kt#L48)

---

### 3. **Get Income Summary**
```
Method: GET
Path: /income/summary
Headers:
  - Authorization: Bearer {token}

Query Parameters:
  - period: String (format: "2026-06")

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
        "type": "FREELANCE",
        "total": 5000000,
        "count": 1
      }
    ]
  }
}
```

**File:** [FeatureApiServices.kt](app/src/main/java/com/casha/app/data/remote/api/FeatureApiServices.kt#L49)

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

**File:** [WalletApiService.kt](app/src/main/java/com/casha/app/data/remote/api/WalletApiService.kt#L8)

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

**File:** [WalletApiService.kt](app/src/main/java/com/casha/app/data/remote/api/WalletApiService.kt#L10)

---

## 🔄 Data Flow Timeline

### User Perspective:
```
t=0s   User enters income details di AddTransactionScreen
t=0.1s User taps "Save" button
       
t=0.2s TransactionViewModel.addIncome() is called
       └─ Create local IncomeEntity immediately
       
t=0.3s Local income appears in list (Optimistic Update)
       └─ IncomeDao.insertIncome() saves to SQLite
       
t=0.5s Request sent to backend: POST /income
       └─ Via IncomeRepositoryImpl
       
t=1.0s Backend responds with created income
       └─ Backend also updated linked wallet (if assetId provided)
       
t=1.2s IncomeRepositoryImpl receives response
       └─ Local record marked as synced (isSynced = true)
       └─ SyncEventBus emits sync completed signal
       
t=1.3s DashboardViewModel receives sync event
       └─ Triggers refreshDashboard(force = true)
       
t=1.4s Wallet refresh request sent: GET /wallets
       
t=1.8s Backend returns updated wallet balance
       └─ Balance now includes income amount
       
t=1.9s WalletViewModel updates its state
       └─ New balance value set in UI state
       
t=2.0s Dashboard UI recomposes dan shows updated balance ✓
       └─ Balance terupdate di CardBalanceSection
       
t=2.1s Success confirmation shown to user
       └─ Navigate back ke transaction list
```

---

## 💾 Local Data Persistence

### IncomeEntity (SQLite via Room)
```kotlin
@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Double,
    val datetime: Date,
    val type: IncomeType,
    val source: String?,
    val assetId: String?,
    val isRecurring: Boolean,
    val frequency: IncomeFrequency?,
    val note: String?,
    val isSynced: Boolean,           // Track sync status
    val remoteId: String?,           // Link ke remote ID
    val createdAt: Date,
    val updatedAt: Date
)
```

**File:** [CategorIncomeEntities.kt](app/src/main/java/com/casha/app/data/local/entity/CategorIncomeEntities.kt)

### Sync Strategy:
1. **Optimistic Save**: Simpan locally immediately dengan isSynced = false
2. **Async Sync**: Send ke backend in background
3. **If Sync Succeeds**: Mark isSynced = true, update server data
4. **If Sync Fails**: Keep local, mark for retry later, show error to user
5. **Offline Support**: User dapat terus menambah income offline, akan disync otomatis

---

## 🏗️ Complete Architecture Layers

### Layer 1: UI (Compose)
- **File:** [AddTransactionScreen.kt](app/src/main/java/com/casha/app/ui/feature/transaction/AddTransactionScreen.kt)
- **Responsibility:** Income input form, validation display, success/error feedback

### Layer 2: State Management (ViewModel)
- **File:** [TransactionViewModel.kt](app/src/main/java/com/casha/app/ui/feature/transaction/TransactionViewModel.kt)
- **Methods:** 
  - `addIncome(request: CreateIncomeRequest)`
  - Manages UI state, handles loading, error, success states

### Layer 3: Use Cases (Business Logic)
- **File:** [AddIncomeUseCase.kt](app/src/main/java/com/casha/app/domain/usecase/transaction/AddIncomeUseCase.kt)
- **Responsibility:** Input validation, call repository

### Layer 4: Repository (Data Access)
- **File:** [IncomeRepositoryImpl.kt](app/src/main/java/com/casha/app/data/remote/impl/IncomeRepositoryImpl.kt)
- **Methods:**
  - `saveIncome(request)` - Optimistic save + async sync
  - `getIncomes()` - Fetch from API with local fallback
  - `updateIncome()` - Update existing income
  - `deleteIncome()` - Delete income

### Layer 5: API Service (Network)
- **File:** [FeatureApiServices.kt](app/src/main/java/com/casha/app/data/remote/api/FeatureApiServices.kt)
- **Interface:** `IncomeApiService`
- **Methods:** createIncome(), getIncomes(), getSummary()

### Layer 6: Local Database (Room/SQLite)
- **File:** [RemainingDaos.kt](app/src/main/java/com/casha/app/data/local/dao/RemainingDaos.kt)
- **Interface:** `IncomeDao`
- **Methods:** insertIncome(), getAllIncomes(), deleteById()

### Layer 7: Sync Coordination
- **File:** [SyncEventBus.kt](app/src/main/java/com/casha/app/core/network/SyncEventBus.kt)
- **Purpose:** Broadcast sync completion events to trigger dashboard refresh

---

## ⚙️ Key Use Cases (Domain Layer)

| Use Case | File | Purpose |
|----------|------|---------|
| **AddIncomeUseCase** | [AddIncomeUseCase.kt](app/src/main/java/com/casha/app/domain/usecase/transaction/AddIncomeUseCase.kt) | Create new income |
| **GetIncomesUseCase** | [GetIncomesUseCase.kt](app/src/main/java/com/casha/app/domain/usecase/transaction/GetIncomesUseCase.kt) | Fetch all incomes |
| **UpdateIncomeUseCase** | [UpdateIncomeUseCase.kt](app/src/main/java/com/casha/app/domain/usecase/transaction/UpdateIncomeUseCase.kt) | Modify existing income |
| **DeleteIncomeUseCase** | [DeleteIncomeUseCase.kt](app/src/main/java/com/casha/app/domain/usecase/transaction/DeleteIncomeUseCase.kt) | Remove income |
| **GetWalletsUseCase** | [GetWalletsUseCase.kt](app/src/main/java/com/casha/app/domain/usecase/wallet/GetWalletsUseCase.kt) | Fetch wallet data |
| **GetWalletSummaryUseCase** | [WalletUseCases.kt](app/src/main/java/com/casha/app/domain/usecase/wallet/WalletUseCases.kt) | Get aggregated wallet summary |

---

## 📝 Domain Models

### IncomeCasha
```kotlin
data class IncomeCasha(
    val id: String,
    val name: String,
    val amount: Double,
    val datetime: Date,
    val type: IncomeType,
    val source: String?,
    val assetId: String?,              // Link ke wallet/asset
    val isRecurring: Boolean,
    val frequency: IncomeFrequency?,
    val note: String?,
    val createdAt: Date,
    val updatedAt: Date
)

enum class IncomeType {
    SALARY, FREELANCE, BUSINESS, INVESTMENT, GIFT, REFUND, OTHER
}

enum class IncomeFrequency {
    DAILY, WEEKLY, BIWEEKLY, MONTHLY, YEARLY
}
```

**File:** [IncomeModels.kt](app/src/main/java/com/casha/app/domain/model/IncomeModels.kt)

---

## 🔗 Event Flow - SyncEventBus

The income module integrates dengan dashboard via `SyncEventBus`:

```kotlin
// In IncomeRepositoryImpl.saveIncome()
val result = safeApiCall { apiService.createIncome(dto) }
result.onSuccess {
    // ... mark as synced, save to DB ...
    syncEventBus.emitSyncCompleted()  // ← Trigger dashboard refresh
}

// In DashboardViewModel.setupSyncEventListener()
viewModelScope.launch {
    syncEventBus.syncCompletedEvent.collect {
        refreshDashboard(force = true)  // ← Refresh wallet balance
    }
}
```

**File:** [SyncEventBus.kt](app/src/main/java/com/casha/app/core/network/SyncEventBus.kt)

---

## ✅ Implementation Checklist

- [x] **API Endpoints Defined** - IncomeApiService in FeatureApiServices.kt
- [x] **Domain Models** - IncomeCasha, IncomeType, IncomeFrequency
- [x] **Local Database** - IncomeEntity, IncomeDao with Room
- [x] **Repository Layer** - IncomeRepositoryImpl dengan optimistic updates
- [x] **Use Cases** - AddIncomeUseCase, GetIncomesUseCase, etc.
- [x] **UI Layer** - AddTransactionScreen dengan income support
- [x] **State Management** - TransactionViewModel
- [x] **Bug Fix** - Fixed wallet balance calculation in SyncUseCases
- [x] **Sync Events** - SyncEventBus untuk trigger dashboard refresh
- [ ] **Enhanced Logging** - Add detailed logs untuk income flow debugging
- [ ] **Retry Mechanism** - Implement exponential backoff untuk failed syncs
- [ ] **Error Handling UI** - Better error messages dan retry buttons
- [ ] **Unit Tests** - Test income creation, sync, and balance update
- [ ] **Integration Tests** - Test complete flow end-to-end

---

## 🐛 Known Issues & Fixes

### Issue 1: Wallet Balance Not Updated After Adding Income ✅ FIXED
**Root Cause:** `loadFromLocal()` in SyncUseCases menggunakan `.firstOrNull()` pada Flow, yang tidak wait untuk database update

**Fix Applied:** Changed to use `getAllIncomesOnce()` suspend function
```kotlin
// BEFORE (Line 212 - WRONG)
val allIncomes = incomeDao.getAllIncomes().firstOrNull() ?: emptyList()

// AFTER (CORRECT)
val allIncomes = incomeDao.getAllIncomesOnce()
```

**File:** [SyncUseCases.kt](app/src/main/java/com/casha/app/domain/usecase/dashboard/SyncUseCases.kt#L212)

---

## 🚀 Next Steps for Full Implementation

1. **Add Logging:** Enhanced logging di IncomeRepositoryImpl untuk track sync status
2. **Retry Logic:** Implement automatic retry untuk failed income syncs
3. **Testing:** Add unit tests untuk income flow
4. **Error Messages:** Better UX untuk error handling
5. **UI Enhancements:** Add loading states, error indicators
6. **Documentation:** Add comments ke code untuk maintainability

---

## 📞 Troubleshooting

### Wallet balance not updating after income added?
1. Check if `assetId` is provided when adding income
2. Verify `SyncEventBus.emitSyncCompleted()` is called after sync
3. Ensure `DashboardViewModel.setupSyncEventListener()` is running
4. Check network logs untuk POST /income response

### Income not appearing in list?
1. Verify IncomeDao.insertIncome() is called
2. Check if `getIncomesFlow()` is being observed in ViewModel
3. Check local database file untuk data persistence

### Sync failing silently?
1. Add logging ke IncomeRepositoryImpl
2. Check `safeApiCall()` untuk error details
3. Verify API token is valid
4. Check backend logs untuk 401/500 errors

---

Generated: June 23, 2026  
Based on: iOS Implementation in MANUAL_INCOME_FLOW.md
