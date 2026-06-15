# Safe Spend Today API Integration

## Overview
Integrated the `/cashflow/safe-spend-today` API endpoint to replace manual widget calculation with server-side computation. This ensures widget data consistency and reduces client-side logic.

## API Endpoint

### Request
```
GET /cashflow/safe-spend-today
Authorization: Bearer {token}
```

### Response
```json
{
  "code": 200,
  "status": "success",
  "message": "Request successful",
  "data": {
    "safeSpendToday": 0,
    "currency": "IDR",
    "daysRemaining": 23,
    "budgetPctUsed": 72,
    "monthlyIncome": 0,
    "spentSoFar": 12598000,
    "pendingObligations": 0,
    "freeRemaining": 0,
    "status": "moderate",
    "statusLabel": "Moderate"
  }
}
```

## Implementation

### 1. API Layer

#### **FeatureApiServices.kt** - Added new endpoint
```kotlin
interface CashflowApiService {
    // ... existing endpoints
    
    @GET("cashflow/safe-spend-today")
    suspend fun getSafeSpendToday(): BaseResponse<SafeSpendTodayDto>
}
```

#### **FeatureDtos.kt** - Added DTO
```kotlin
@Serializable
data class SafeSpendTodayDto(
    val safeSpendToday: Double = 0.0,
    val currency: String = com.casha.app.core.util.CurrencyFormatter.defaultCurrency,
    val daysRemaining: Int = 0,
    val budgetPctUsed: Int = 0,
    val monthlyIncome: Double = 0.0,
    val spentSoFar: Double = 0.0,
    val pendingObligations: Double = 0.0,
    val freeRemaining: Double = 0.0,
    val status: String = "",
    val statusLabel: String = ""
)
```

### 2. Domain Layer

#### **DashboardModels.kt** - Added domain model
```kotlin
data class SafeSpendToday(
    val safeSpendToday: Double,
    val currency: String,
    val daysRemaining: Int,
    val budgetPctUsed: Int,
    val monthlyIncome: Double,
    val spentSoFar: Double,
    val pendingObligations: Double,
    val freeRemaining: Double,
    val status: String,
    val statusLabel: String
)
```

#### **FeatureRepositories.kt** - Added repository method
```kotlin
interface CashflowRepository {
    suspend fun getHistory(month: String?, year: String?, page: Int, pageSize: Int): CashflowHistoryResponse
    suspend fun getSummary(month: String?, year: String?): CashflowSummary
    suspend fun getSafeSpendToday(): SafeSpendToday  // ✅ NEW
    suspend fun deleteGroup(groupId: String)
    suspend fun renameGroup(groupId: String, newName: String)
}
```

#### **DashboardUseCases.kt** - Added use case
```kotlin
class GetSafeSpendTodayUseCase @Inject constructor(
    private val repository: CashflowRepository
) {
    suspend fun execute(): SafeSpendToday {
        return repository.getSafeSpendToday()
    }
}
```

### 3. Data Layer

#### **CashflowRepositoryImpl.kt** - Implemented repository method
```kotlin
override suspend fun getSafeSpendToday(): SafeSpendToday {
    val result = safeApiCall { apiService.getSafeSpendToday() }
    return result.fold(
        onSuccess = { response -> 
            response.data?.toSafeSpendDomain() ?: SafeSpendToday(
                safeSpendToday = 0.0,
                currency = "IDR",
                daysRemaining = 0,
                budgetPctUsed = 0,
                monthlyIncome = 0.0,
                spentSoFar = 0.0,
                pendingObligations = 0.0,
                freeRemaining = 0.0,
                status = "",
                statusLabel = ""
            )
        },
        onFailure = { 
            SafeSpendToday(/* fallback data */)
        }
    )
}

private fun SafeSpendTodayDto.toSafeSpendDomain() = SafeSpendToday(
    safeSpendToday = safeSpendToday,
    currency = currency,
    daysRemaining = daysRemaining,
    budgetPctUsed = budgetPctUsed,
    monthlyIncome = monthlyIncome,
    spentSoFar = spentSoFar,
    pendingObligations = pendingObligations,
    freeRemaining = freeRemaining,
    status = status,
    statusLabel = statusLabel
)
```

### 4. Presentation Layer

#### **DashboardViewModel.kt** - Updated widget data logic

**Before (Manual Calculation):**
```kotlin
private fun updateWidgetData() {
    viewModelScope.launch {
        val state = _uiState.value
        val summary = state.cashflowSummary ?: return@launch
        
        // Manual calculations:
        // - daysRemaining = daysInMonth - dayOfMonth
        // - budgetPct = (expense / income) * 100
        // - safeSpend = (income - expense) / daysRemaining
        // - status logic (comfortable, caution, over_budget, no_income)
        
        val widgetSummary = WidgetSummary(/* manually computed values */)
        WidgetUpdater.updateSummary(appContext, widgetSummary)
    }
}
```

**After (API-based):**
```kotlin
private fun updateWidgetData() {
    viewModelScope.launch {
        val isPremium = subscriptionManager.isPremium.firstOrNull() ?: false
        val isLoggedIn = authManager.accessToken.firstOrNull() != null

        WidgetUpdater.setAuthState(appContext, isLoggedIn = isLoggedIn, isPremium = isPremium)

        if (!isLoggedIn || !isPremium) return@launch

        try {
            // ✅ Use API endpoint instead of manual calculation
            val safeSpendData = getSafeSpendTodayUseCase.execute()
            
            // Still compute today's spending locally (not provided by API)
            val spentToday = try {
                getTotalSpendingUseCase.execute(SpendingPeriod.CUSTOM(todayStart, todayEnd))
            } catch (_: Exception) { 0.0 }

            val widgetSummary = WidgetSummary(
                safeSpendToday = safeSpendData.safeSpendToday,
                currency = safeSpendData.currency,
                daysRemaining = safeSpendData.daysRemaining,
                monthlyIncome = safeSpendData.monthlyIncome,
                spentSoFar = safeSpendData.spentSoFar,
                freeRemaining = safeSpendData.freeRemaining,
                status = safeSpendData.status,
                statusLabel = safeSpendData.statusLabel,
                budgetPctUsed = safeSpendData.budgetPctUsed,
                lastUpdatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
                spentToday = spentToday
            )

            WidgetUpdater.updateSummary(appContext, widgetSummary)
        } catch (e: Exception) {
            android.util.Log.e("DashboardViewModel", "Failed to update widget data: ${e.message}")
        }
    }
}
```

## Benefits

### 1. **Server-Side Computation**
- Budget calculation logic centralized on server
- Consistent calculations across platforms (Android, iOS, Web)
- Easier to update calculation logic without app updates

### 2. **Reduced Client Complexity**
- Removed manual calculation logic (~40 lines of code)
- No need to maintain budget status mapping on client
- Less room for calculation errors

### 3. **Better Data Accuracy**
- Server has authoritative data
- Includes `pendingObligations` (future feature)
- Status labels localized by server

### 4. **Improved Maintainability**
- Single source of truth for safe spend calculation
- API contract clearly defined
- Easier to test and debug

## Data Flow

```
User opens Dashboard
         ↓
DashboardViewModel.loadInitialData()
         ↓
DashboardViewModel.refreshDashboardInternal()
         ↓
DashboardViewModel.updateWidgetData()
         ↓
GetSafeSpendTodayUseCase.execute()
         ↓
CashflowRepository.getSafeSpendToday()
         ↓
CashflowApiService.getSafeSpendToday()
         ↓
API: GET /cashflow/safe-spend-today
         ↓
Server computes:
  - safeSpendToday = (monthlyIncome - spentSoFar) / daysRemaining
  - budgetPctUsed = (spentSoFar / monthlyIncome) * 100
  - status = based on budgetPctUsed thresholds
         ↓
Response: SafeSpendTodayDto
         ↓
Convert to domain model: SafeSpendToday
         ↓
Map to WidgetSummary
         ↓
WidgetPreferences.saveSummary()
         ↓
Widget refreshes with new data
```

## API Log Example

```
🔵 === FIRST REQUEST ===
🔵 REQ URL: https://cashabe-self.vercel.app/cashflow/safe-spend-today
🔵 REQ METHOD: get
🔵 REQ HEADERS: ["Authorization": "Bearer {token}"]
🔵 =====================

✅ === FIRST RESPONSE ===
✅ RES STATUS CODE: 200
✅ RES BODY:
{
  "code":200,
  "status":"success",
  "message":"Request successful",
  "data":{
    "safeSpendToday":0,
    "currency":"IDR",
    "daysRemaining":23,
    "budgetPctUsed":72,
    "monthlyIncome":0,
    "spentSoFar":12598000,
    "pendingObligations":0,
    "freeRemaining":0,
    "status":"moderate",
    "statusLabel":"Moderate"
  }
}
✅ ====================

✅ WidgetDataWriter: Primary endpoint success
- budgetPctUsed: 72%
- safeSpendToday: 0.0
- spentSoFar: 12598000.0

✅ WidgetDataWriter: Widget data saved and reload requested
```

## Files Modified

### API & DTOs
1. `app/src/main/java/com/casha/app/data/remote/api/FeatureApiServices.kt`
   - Added `getSafeSpendToday()` endpoint

2. `app/src/main/java/com/casha/app/data/remote/dto/FeatureDtos.kt`
   - Added `SafeSpendTodayDto` data class

### Domain Layer
3. `app/src/main/java/com/casha/app/domain/model/DashboardModels.kt`
   - Added `SafeSpendToday` domain model

4. `app/src/main/java/com/casha/app/domain/repository/FeatureRepositories.kt`
   - Added `getSafeSpendToday()` to CashflowRepository interface
   - Added import for SafeSpendToday

5. `app/src/main/java/com/casha/app/domain/usecase/dashboard/DashboardUseCases.kt`
   - Added `GetSafeSpendTodayUseCase` class

### Data Layer
6. `app/src/main/java/com/casha/app/data/remote/impl/CashflowRepositoryImpl.kt`
   - Implemented `getSafeSpendToday()` method
   - Added `toSafeSpendDomain()` mapper function

### Presentation Layer
7. `app/src/main/java/com/casha/app/ui/feature/dashboard/DashboardViewModel.kt`
   - Added `getSafeSpendTodayUseCase` dependency
   - Replaced manual calculation with API call in `updateWidgetData()`

## Testing Checklist

- [x] Build successful without errors
- [x] API endpoint defined in CashflowApiService
- [x] DTO created with correct serialization
- [x] Domain model created
- [x] Repository interface updated
- [x] Repository implementation added
- [x] Use case created
- [x] DashboardViewModel updated
- [ ] Test API call with real data
- [ ] Test widget update after Dashboard load
- [ ] Test fallback behavior on API error
- [ ] Test widget data accuracy
- [ ] Verify status labels match design

## Next Steps

1. **Test on Device/Emulator:**
   - Install app and login
   - Open Dashboard to trigger API call
   - Check widget displays correct data
   - Verify status labels and colors

2. **Error Handling:**
   - Test behavior when API fails
   - Ensure widget shows fallback or previous data
   - Check logs for error messages

3. **Performance:**
   - Monitor API response time
   - Ensure widget updates smoothly
   - Check battery/network usage

4. **Future Enhancements:**
   - Use `pendingObligations` when feature is ready
   - Add caching to reduce API calls
   - Implement incremental updates
