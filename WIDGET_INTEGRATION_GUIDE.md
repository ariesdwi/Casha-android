# Widget Integration Guide - Quick Start

This guide shows you how to integrate the new event-driven widget system into your ViewModels and managers.

---

## Quick Reference

**Emit an event after any data change that should update widgets:**

```kotlin
import com.casha.app.widget.WidgetUpdateCoordinator
import com.casha.app.widget.WidgetUpdateCoordinator.WidgetUpdateEvent

// After transaction operations
WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.TransactionAdded)

// After budget changes
WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.BudgetChanged)

// After balance updates
WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.BalanceUpdated)

// After login/logout
WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.LoginStateChanged)

// After premium state change
WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.PremiumStateChanged)

// After hide balance toggle
WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.HideBalanceToggled)
```

---

## Integration Locations

### 1. Transaction ViewModel

**File**: `app/src/main/java/com/casha/app/ui/feature/transaction/TransactionViewModel.kt`

```kotlin
import com.casha.app.widget.WidgetUpdateCoordinator
import com.casha.app.widget.WidgetUpdateCoordinator.WidgetUpdateEvent

class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    // ... other dependencies
) : ViewModel() {
    
    // Add transaction
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.insert(transaction)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.TransactionAdded)
        }
    }
    
    // Update transaction
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.update(transaction)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.TransactionAdded)
        }
    }
    
    // Delete transaction
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            transactionRepository.delete(transactionId)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.TransactionAdded)
        }
    }
}
```

---

### 2. Budget ViewModel

**File**: `app/src/main/java/com/casha/app/ui/feature/budget/BudgetViewModel.kt`

```kotlin
import com.casha.app.widget.WidgetUpdateCoordinator
import com.casha.app.widget.WidgetUpdateCoordinator.WidgetUpdateEvent

class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    // ... other dependencies
) : ViewModel() {
    
    // Update budget
    fun updateBudget(budget: BudgetCasha) {
        viewModelScope.launch {
            budgetRepository.updateBudget(budget)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.BudgetChanged)
        }
    }
    
    // Create budget
    fun createBudget(budget: BudgetCasha) {
        viewModelScope.launch {
            budgetRepository.createBudget(budget)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.BudgetChanged)
        }
    }
    
    // Delete budget
    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budgetId)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.BudgetChanged)
        }
    }
}
```

---

### 3. Wallet ViewModel (Balance Updates)

**File**: `app/src/main/java/com/casha/app/ui/feature/wallet/WalletViewModel.kt`

```kotlin
import com.casha.app.widget.WidgetUpdateCoordinator
import com.casha.app.widget.WidgetUpdateCoordinator.WidgetUpdateEvent

class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    // ... other dependencies
) : ViewModel() {
    
    // Update wallet balance
    fun updateWalletBalance(walletId: String, newBalance: Double) {
        viewModelScope.launch {
            walletRepository.updateBalance(walletId, newBalance)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.BalanceUpdated)
        }
    }
    
    // Transfer between wallets
    fun transferBetweenWallets(fromId: String, toId: String, amount: Double) {
        viewModelScope.launch {
            walletRepository.transfer(fromId, toId, amount)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.BalanceUpdated)
        }
    }
}
```

---

### 4. Auth Manager

**File**: `app/src/main/java/com/casha/app/core/auth/AuthManager.kt`

```kotlin
import com.casha.app.widget.WidgetUpdateCoordinator
import com.casha.app.widget.WidgetUpdateCoordinator.WidgetUpdateEvent
import com.casha.app.widget.data.WidgetPreferences

class AuthManager @Inject constructor(
    private val context: Context,
    // ... other dependencies
) {
    
    // Login
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val user = authApi.login(email, password)
            
            // Save login state
            WidgetPreferences.setLoggedIn(context, true)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.LoginStateChanged)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Logout
    suspend fun logout() {
        // Clear auth data
        // ...
        
        // Save logout state
        WidgetPreferences.setLoggedIn(context, false)
        
        // ✅ Emit event to update widgets
        WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.LoginStateChanged)
    }
}
```

---

### 5. Subscription Manager

**File**: `app/src/main/java/com/casha/app/core/auth/SubscriptionManager.kt`

```kotlin
import com.casha.app.widget.WidgetUpdateCoordinator
import com.casha.app.widget.WidgetUpdateCoordinator.WidgetUpdateEvent
import com.casha.app.widget.data.WidgetPreferences

class SubscriptionManager @Inject constructor(
    private val context: Context,
    // ... other dependencies
) {
    
    // Purchase premium
    suspend fun purchasePremium(): Result<Unit> {
        return try {
            // Process purchase
            // ...
            
            // Save premium state
            WidgetPreferences.setPremium(context, true)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.PremiumStateChanged)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Cancel subscription
    suspend fun cancelSubscription() {
        // Process cancellation
        // ...
        
        // Save non-premium state
        WidgetPreferences.setPremium(context, false)
        
        // ✅ Emit event to update widgets
        WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.PremiumStateChanged)
    }
    
    // Restore purchase
    suspend fun restorePurchase(): Result<Boolean> {
        return try {
            val isPremium = subscriptionApi.checkStatus()
            
            // Save restored state
            WidgetPreferences.setPremium(context, isPremium)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.PremiumStateChanged)
            
            Result.success(isPremium)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

### 6. Settings/Profile ViewModel (Hide Balance)

**File**: `app/src/main/java/com/casha/app/ui/feature/profile/ProfileViewModel.kt`

```kotlin
import com.casha.app.widget.WidgetUpdateCoordinator
import com.casha.app.widget.WidgetUpdateCoordinator.WidgetUpdateEvent

class ProfileViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    // ... other dependencies
) : ViewModel() {
    
    // Toggle hide balance
    fun toggleHideBalance() {
        viewModelScope.launch {
            val currentState = settingsRepository.getHideBalance()
            val newState = !currentState
            
            settingsRepository.setHideBalance(newState)
            
            // ✅ Emit event to update widgets
            WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.HideBalanceToggled)
        }
    }
}
```

---

## Dashboard ViewModel (Safe Spend Today)

**File**: `app/src/main/java/com/casha/app/ui/feature/dashboard/DashboardViewModel.kt`

```kotlin
import com.casha.app.widget.WidgetUpdateCoordinator
import com.casha.app.widget.WidgetUpdateCoordinator.WidgetUpdateEvent
import com.casha.app.widget.data.WidgetPreferences
import com.casha.app.widget.data.WidgetSummary

class DashboardViewModel @Inject constructor(
    private val context: Context,
    // ... other dependencies
) : ViewModel() {
    
    // Fetch safe spend data
    fun fetchSafeSpendData() {
        viewModelScope.launch {
            try {
                val safeSpendData = repository.getSafeSpendToday()
                
                // Create widget summary
                val widgetSummary = WidgetSummary(
                    safeSpendToday = safeSpendData.safeSpend,
                    currency = safeSpendData.currency,
                    daysRemaining = safeSpendData.daysRemaining,
                    monthlyIncome = safeSpendData.monthlyIncome,
                    spentSoFar = safeSpendData.spentSoFar,
                    pendingObligations = safeSpendData.pendingObligations,
                    freeRemaining = safeSpendData.freeRemaining,
                    status = safeSpendData.status,
                    statusLabel = safeSpendData.statusLabel,
                    budgetPctUsed = safeSpendData.budgetPctUsed,
                    hideBalance = safeSpendData.hideBalance,
                    lastFetchedAt = Instant.now().toString(),
                    lastUpdatedAt = Instant.now().toString(),
                    spentToday = safeSpendData.spentToday,
                    insightText = safeSpendData.insightText,
                    insightGeneratedAt = safeSpendData.insightGeneratedAt,
                    nextBillName = safeSpendData.nextBillName,
                    nextBillAmount = safeSpendData.nextBillAmount,
                    nextBillDueInDays = safeSpendData.nextBillDueInDays
                )
                
                // Save to widget preferences
                WidgetPreferences.saveSummary(context, widgetSummary)
                
                // ✅ Emit event to update widgets
                WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.ManualRefresh)
                
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
```

---

## Event Type Selection Guide

Choose the appropriate event type based on what changed:

| What Changed | Event Type | Widgets Updated |
|--------------|-----------|-----------------|
| Transaction added/updated/deleted | `TransactionAdded` | Budget widgets (Home Small/Medium, Lock) |
| Budget limit/category changed | `BudgetChanged` | Budget widgets |
| Wallet balance changed | `BalanceUpdated` | Balance widgets (Lock Circular) |
| User logged in/out | `LoginStateChanged` | All widgets (state change) |
| Premium purchased/cancelled | `PremiumStateChanged` | All widgets (state change) |
| Hide balance toggled | `HideBalanceToggled` | All widgets (display change) |
| Manual refresh (pull-to-refresh) | `ManualRefresh` | All widgets (forced) |
| Background periodic refresh | `PeriodicRefresh` | All widgets |

---

## Testing Your Integration

### 1. Check Logs

Enable widget logging in debug builds:

```bash
adb logcat | grep CashaWidget
```

You should see:
```
D/CashaWidget: Widget update started: TransactionAdded
D/CashaWidget: Updated HomeSmallWidgetReceiver: 2 instances
D/CashaWidget: Updated HomeMediumWidgetReceiver: 1 instances
D/CashaWidget: Widget update completed: TransactionAdded
```

### 2. Test Real-Time Updates

1. Add a widget to your home screen
2. Open the app
3. Add a transaction
4. Immediately check widget - it should update within 1 second
5. Check logcat for "Widget update started"

### 3. Test Network Awareness

1. Turn off WiFi/mobile data
2. Wait for periodic refresh (15 min)
3. Check logs for "Worker skipped: No network connectivity"
4. Turn on network
5. Worker should retry automatically

### 4. Test Staleness Skip

1. Add a transaction (updates widgets)
2. Within 15 minutes, trigger periodic refresh
3. Check logs for "Worker skipped: Data is still fresh"

---

## Troubleshooting

### Widgets not updating?

1. **Check if event is emitted:**
   ```kotlin
   WidgetUpdateCoordinator.emitUpdate(WidgetUpdateEvent.TransactionAdded)
   ```

2. **Check logcat** for widget update logs:
   ```bash
   adb logcat | grep CashaWidget
   ```

3. **Verify WidgetPreferences** has data:
   ```kotlin
   val summary = WidgetPreferences.getSummary(context)
   Log.d("Widget", "Summary: $summary")
   ```

4. **Check Application** listener is initialized:
   - Look in `CashaApplication.onCreate()`
   - Should call `initializeWidgetUpdateListener()`

### Events not being received?

- Make sure the Application class has the event listener
- Check that the CoroutineScope is not cancelled
- Verify SharedFlow has enough buffer capacity

### Worker not running?

- Check WorkManager constraints (network, battery)
- Verify worker is enqueued: `adb shell dumpsys jobscheduler`
- Check for worker errors in logcat

---

## Migration from Old System

### Before (Manual Updates):
```kotlin
// OLD: Manual update call
fun saveTransaction(transaction: Transaction) {
    repository.save(transaction)
    WidgetUpdater.refresh(context) // ❌ Manual call
}
```

### After (Event-Driven):
```kotlin
// NEW: Event emission
fun saveTransaction(transaction: Transaction) {
    repository.save(transaction)
    WidgetUpdateCoordinator.emitUpdate(
        WidgetUpdateEvent.TransactionAdded
    ) // ✅ Automatic update via event
}
```

### Removing Old Code

Once all integrations are complete, you can remove:
- ❌ All `WidgetUpdater.refresh(context)` calls
- ❌ The old `WidgetUpdater` class (keep for reference until Phase 4)

---

## Best Practices

### ✅ DO:
- Emit events after successful operations
- Use the most specific event type (e.g., `TransactionAdded` not `ManualRefresh`)
- Emit events inside the ViewModel/Manager after the data is saved
- Use `viewModelScope.launch` for coroutine operations

### ❌ DON'T:
- Emit events before the operation completes
- Use `ManualRefresh` for everything (defeats selective update optimization)
- Emit events in the UI layer (keep in ViewModel/Manager)
- Block the main thread with widget updates

---

## Summary

**To integrate the new widget system:**

1. Import `WidgetUpdateCoordinator` and `WidgetUpdateEvent`
2. Add `emitUpdate()` calls after data changes
3. Choose the appropriate event type
4. Test with logcat to verify updates
5. Remove old manual `WidgetUpdater` calls

**That's it!** The system handles the rest automatically.

---

**Need help?** Check the logs with `adb logcat | grep CashaWidget` or review the Phase 1 completion document.
