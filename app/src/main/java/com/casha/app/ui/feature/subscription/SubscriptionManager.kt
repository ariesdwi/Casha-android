package com.casha.app.ui.feature.subscription

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coreSubscriptionManager: com.casha.app.core.auth.SubscriptionManager
) : ViewModel(), PurchasesUpdatedListener {

    // -- State
    private val _hasPremiumAccess = MutableStateFlow(false)
    val hasPremiumAccess: StateFlow<Boolean> = _hasPremiumAccess

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
        
        // Monitor core status changes (from DataStore) to keep VM state in sync
        coreSubscriptionManager.isPremium
            .onEach { isPremium ->
                _hasPremiumAccess.value = isPremium
            }
            .launchIn(viewModelScope)
    }

    // MARK: - Setup
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
                // Retry connection if needed or notify user
            }
        })
    }

    // MARK: - Load Products
    suspend fun loadProducts() {
        _isLoading.value = true
        _errorMessage.value = null

        // Subscriptions
        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("premium.casha.monthly")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ))
            .build()

        // One-time (lifetime)
        val inappParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("premium.casha.lifetime")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ))
            .build()

        val allProducts = mutableListOf<ProductDetails>()

        billingClient.queryProductDetailsAsync(subParams) { result, productDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList?.let { allProducts.addAll(it) }
            }
            
            billingClient.queryProductDetailsAsync(inappParams) { resultInapp, productDetailsListInapp ->
                if (resultInapp.responseCode == BillingClient.BillingResponseCode.OK) {
                    productDetailsListInapp?.let { allProducts.addAll(it) }
                }
                _products.value = allProducts
                _isLoading.value = false
            }
        }
    }

    // MARK: - Purchase
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

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    // MARK: - PurchasesUpdatedListener
    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        _isPurchasing.value = false
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // User cancelled, no error needed
            }
            else -> {
                _errorMessage.value = "Purchase failed: ${result.debugMessage}"
            }
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
                            _hasPremiumAccess.value = true
                            viewModelScope.launch {
                                coreSubscriptionManager.setPremiumStatus(true)
                            }
                        }
                    }
                } else {
                    _hasPremiumAccess.value = true
                    coreSubscriptionManager.setPremiumStatus(true)
                }
            }
        }
    }

    // MARK: - Restore / Check Status
    suspend fun checkSubscriptionStatus() {
        // Check subscriptions
        val subParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
            
        billingClient.queryPurchasesAsync(subParams) { result, purchases ->
            val subPurchases = if (result.responseCode == BillingClient.BillingResponseCode.OK) purchases else emptyList()
            
            // Check one-time purchases
            val inappParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
                
            billingClient.queryPurchasesAsync(inappParams) { resultInapp, purchasesInapp ->
                val inappPurchases = if (resultInapp.responseCode == BillingClient.BillingResponseCode.OK) purchasesInapp else emptyList()
                
                val allPurchases = subPurchases + inappPurchases
                val isPremium = allPurchases.any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                _hasPremiumAccess.value = isPremium
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
