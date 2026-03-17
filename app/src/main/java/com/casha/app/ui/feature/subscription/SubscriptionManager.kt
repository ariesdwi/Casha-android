package com.casha.app.ui.feature.subscription

import android.app.Activity
import android.content.Context
import android.util.Log
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

    companion object {
        private const val TAG = "BillingVM"
    }

    init {
        Log.d(TAG, "Initializing BillingClient...")
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .build()

        setupBillingClient()
    }

    // ── Billing Setup ──────────────────────────────────────────────────────────

    private fun setupBillingClient() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                Log.d(TAG, "Billing setup finished. Response: ${result.responseCode}, Message: ${result.debugMessage}")
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    viewModelScope.launch {
                        loadProducts()
                        checkSubscriptionStatus()
                    }
                } else {
                    Log.e(TAG, "Billing setup FAILED: code=${result.responseCode}, msg=${result.debugMessage}")
                    _errorMessage.value = "Billing setup failed: ${result.debugMessage}"
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected — will retry on next foreground")
            }
        })
    }

    // ── Load Products ──────────────────────────────────────────────────────────

    suspend fun loadProducts() {
        _isLoading.value = true
        _errorMessage.value = null

        Log.d(TAG, "Loading products...")

        val subProductIds = listOf("casha.premium.yearly", "premium.casha.monthly", "com.casha.premium.weekly")
        val inappProductIds = listOf("com.casha.premium.lifetime.new")

        Log.d(TAG, "Querying SUBS: $subProductIds")
        Log.d(TAG, "Querying INAPP: $inappProductIds")

        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subProductIds.map { id ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            })
            .build()

        val inappParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inappProductIds.map { id ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            })
            .build()

        val allProducts = mutableListOf<ProductDetails>()

        billingClient.queryProductDetailsAsync(subParams) { result, list ->
            Log.d(TAG, "SUBS query result: code=${result.responseCode}, msg=${result.debugMessage}, count=${list?.size ?: 0}")
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                list?.forEach { product ->
                    Log.d(TAG, "  Found SUB: ${product.productId} — ${product.title}")
                }
                list?.let { allProducts.addAll(it) }
            } else {
                Log.e(TAG, "SUBS query FAILED: code=${result.responseCode}, msg=${result.debugMessage}")
            }
            billingClient.queryProductDetailsAsync(inappParams) { resultInapp, listInapp ->
                Log.d(TAG, "INAPP query result: code=${resultInapp.responseCode}, msg=${resultInapp.debugMessage}, count=${listInapp?.size ?: 0}")
                if (resultInapp.responseCode == BillingClient.BillingResponseCode.OK) {
                    listInapp?.forEach { product ->
                        Log.d(TAG, "  Found INAPP: ${product.productId} — ${product.title}")
                    }
                    listInapp?.let { allProducts.addAll(it) }
                } else {
                    Log.e(TAG, "INAPP query FAILED: code=${resultInapp.responseCode}, msg=${resultInapp.debugMessage}")
                }
                Log.d(TAG, "Total products loaded: ${allProducts.size}")
                _products.value = allProducts
                _isLoading.value = false
            }
        }
    }

    // ── Purchase ───────────────────────────────────────────────────────────────

    fun purchase(activity: Activity, productDetails: ProductDetails) {
        _isPurchasing.value = true
        _errorMessage.value = null

        Log.d(TAG, "Starting purchase flow for product: ${productDetails.productId}")

        val productDetailsParamsList = if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val offerDetails = productDetails.subscriptionOfferDetails
            
            // Log all available offers for debugging
            offerDetails?.forEachIndexed { index, offer ->
                val phases = offer.pricingPhases.pricingPhaseList.joinToString { it.formattedPrice }
                Log.d(TAG, "Offer $index: Token=${offer.offerToken}, Phases=[$phases]")
            }

            // Find an offer with a valid token, prioritizing the one with the most pricing phases (e.g. Free Trial + Regular)
            val selectedOffer = offerDetails?.maxByOrNull { it.pricingPhases.pricingPhaseList.size }
            val offerToken = selectedOffer?.offerToken

            if (offerToken == null) {
                Log.e(TAG, "Cannot purchase SUB: No valid offerToken found for ${productDetails.productId}.")
                _errorMessage.value = "Product unavailable: Missing offer details in Play Console."
                _isPurchasing.value = false
                return
            }

            Log.d(TAG, "Selected offer token: $offerToken")
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

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val launchResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Failed to launch billing flow. Code: ${launchResult.responseCode}, Msg: ${launchResult.debugMessage}")
            _errorMessage.value = "Billing flow error: ${launchResult.debugMessage}"
            _isPurchasing.value = false
        } else {
            Log.d(TAG, "Billing flow launched successfully. Waiting for user input...")
        }
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
