package com.casha.app.ui.feature.subscription

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BillingViewModel — handles Google Play Billing lifecycle only.
 *
 * SINGLE SOURCE OF TRUTH:
 *   All premium status reads come from [coreSubscriptionManager.isPremium] (DataStore).
 *   This ViewModel only WRITES to DataStore (via [coreSubscriptionManager.setPremiumStatus])
 *   after verifying purchases with the billing client.
 *
 *   hasPremiumAccess is a StateFlow backed directly by DataStore — not a separate in-memory copy.
 */
@HiltViewModel
class SubscriptionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coreSubscriptionManager: com.casha.app.core.auth.SubscriptionManager
) : ViewModel(), PurchasesUpdatedListener {

    // ── Single source of truth: DataStore-backed via core manager ──────────────
    val hasPremiumAccess: StateFlow<Boolean> = coreSubscriptionManager.isPremium
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = false
        )

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var billingClient: BillingClient

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        setupBillingClient()
    }

    // ── Billing Setup ──────────────────────────────────────────────────────────

    private fun setupBillingClient() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    viewModelScope.launch {
                        loadProducts()
                        checkSubscriptionStatus()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Will retry on next app foreground; DataStore persists last known state
            }
        })
    }

    // ── Load Products ──────────────────────────────────────────────────────────

    suspend fun loadProducts() {
        _isLoading.value = true
        _errorMessage.value = null

        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("casha.premium.yearly")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("premium.casha.monthly")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("com.casha.premium.weekly")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ))
            .build()

        val inappParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("premium.casha.lifetime")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ))
            .build()

        val allProducts = mutableListOf<ProductDetails>()

        billingClient.queryProductDetailsAsync(subParams) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                list?.let { allProducts.addAll(it) }
            }
            billingClient.queryProductDetailsAsync(inappParams) { resultInapp, listInapp ->
                if (resultInapp.responseCode == BillingClient.BillingResponseCode.OK) {
                    listInapp?.let { allProducts.addAll(it) }
                }
                _products.value = allProducts
                _isLoading.value = false
            }
        }
    }

    // ── Purchase ───────────────────────────────────────────────────────────────

    fun purchase(activity: Activity, productDetails: ProductDetails) {
        _isPurchasing.value = true
        _errorMessage.value = null

        val productDetailsParamsList = if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        } else {
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )
        }

        billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
        )
    }

    // ── PurchasesUpdatedListener ───────────────────────────────────────────────

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        _isPurchasing.value = false
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.forEach { handlePurchase(it) }
            BillingClient.BillingResponseCode.USER_CANCELED -> { /* No error needed */ }
            else -> _errorMessage.value = "Purchase failed: ${result.debugMessage}"
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        viewModelScope.launch {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(ackParams) { result ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            // Write ONLY to DataStore — hasPremiumAccess flow updates automatically
                            viewModelScope.launch {
                                coreSubscriptionManager.setPremiumStatus(true)
                            }
                        }
                    }
                } else {
                    // Already acknowledged, just persist
                    coreSubscriptionManager.setPremiumStatus(true)
                }
            }
        }
    }

    // ── Restore / Verify ───────────────────────────────────────────────────────

    suspend fun checkSubscriptionStatus() {
        val subParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(subParams) { result, purchases ->
            val subPurchases = if (result.responseCode == BillingClient.BillingResponseCode.OK) purchases else emptyList()

            val inappParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient.queryPurchasesAsync(inappParams) { resultInapp, purchasesInapp ->
                val inappPurchases = if (resultInapp.responseCode == BillingClient.BillingResponseCode.OK) purchasesInapp else emptyList()

                val isPremium = (subPurchases + inappPurchases).any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                // Write ONLY to DataStore — hasPremiumAccess flow updates automatically
                viewModelScope.launch {
                    coreSubscriptionManager.setPremiumStatus(isPremium)
                }
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _isLoading.value = true
            checkSubscriptionStatus()
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
