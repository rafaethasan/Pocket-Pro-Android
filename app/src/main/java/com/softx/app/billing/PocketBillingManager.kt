package com.softx.app.billing

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.softx.app.data.PocketNativePreferencesStore

class PocketBillingManager private constructor(private val context: Context) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_MONTHLY = "pocket_pro_monthly"
        const val PRODUCT_YEARLY = "pocket_pro_yearly"
        const val PRODUCT_LIFETIME = "pocket_pro_lifetime"

        @Volatile
        private var instance: PocketBillingManager? = null

        fun getInstance(context: Context): PocketBillingManager {
            return instance ?: synchronized(this) {
                instance ?: PocketBillingManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val preferencesStore = PocketNativePreferencesStore(context)
    private var billingClient: BillingClient? = null

    var isProUser by mutableStateOf(preferencesStore.isProMember())
        private set

    var currentTier by mutableStateOf(
        when (preferencesStore.proTier()) {
            "pro_monthly" -> PocketProTier.PRO_MONTHLY
            "pro_yearly" -> PocketProTier.PRO_YEARLY
            "pro_lifetime" -> PocketProTier.PRO_LIFETIME
            else -> if (preferencesStore.isProMember()) PocketProTier.PRO_YEARLY else PocketProTier.FREE
        }
    )
        private set

    var isBillingConnected by mutableStateOf(false)
        private set

    var productDetailsMap by mutableStateOf<Map<String, ProductDetails>>(emptyMap())
        private set

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        startConnection()
    }

    fun startConnection(onReady: (() -> Unit)? = null) {
        val client = billingClient ?: return
        if (client.isReady) {
            isBillingConnected = true
            queryProducts()
            onReady?.invoke()
            return
        }

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isBillingConnected = true
                    queryProducts()
                    queryActivePurchases()
                    onReady?.invoke()
                } else {
                    isBillingConnected = false
                }
            }

            override fun onBillingServiceDisconnected() {
                isBillingConnected = false
            }
        })
    }

    private fun queryProducts() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val subsProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
        )

        val inAppProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        )

        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subsProductList)
            .build()

        client.queryProductDetailsAsync(subsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val newMap = productDetailsMap.toMutableMap()
                for (details in productDetailsList) {
                    newMap[details.productId] = details
                }
                productDetailsMap = newMap
            }
        }

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inAppProductList)
            .build()

        client.queryProductDetailsAsync(inAppParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val newMap = productDetailsMap.toMutableMap()
                for (details in productDetailsList) {
                    newMap[details.productId] = details
                }
                productDetailsMap = newMap
            }
        }
    }

    fun queryActivePurchases() {
        val client = billingClient ?: return
        if (!client.isReady) return

        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }

        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        var hasValidPro = false
        var activeTier = PocketProTier.FREE

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                hasValidPro = true

                val product = purchase.products.firstOrNull()
                activeTier = when (product) {
                    PRODUCT_MONTHLY -> PocketProTier.PRO_MONTHLY
                    PRODUCT_YEARLY -> PocketProTier.PRO_YEARLY
                    PRODUCT_LIFETIME -> PocketProTier.PRO_LIFETIME
                    else -> PocketProTier.PRO_YEARLY
                }

                if (!purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient?.acknowledgePurchase(ackParams) { /* acknowledged */ }
                }
            }
        }

        if (hasValidPro) {
            setProStatus(true, activeTier)
        }
    }

    fun launchBillingFlow(
        activity: Activity,
        productId: String,
        onFallbackDemo: () -> Unit = {},
    ) {
        val client = billingClient
        val details = productDetailsMap[productId]

        if (client != null && client.isReady && details != null) {
            val productDetailsParamsList = when (details.productType) {
                BillingClient.ProductType.SUBS -> {
                    val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken.orEmpty()
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(details)
                            .setOfferToken(offerToken)
                            .build()
                    )
                }
                else -> {
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(details)
                            .build()
                    )
                }
            }

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            client.launchBillingFlow(activity, flowParams)
        } else {
            // When running in emulator / debug without Google Play Store active product:
            onFallbackDemo()
        }
    }

    fun getFormattedPrice(productId: String, fallbackPrice: String): String {
        val details = productDetailsMap[productId] ?: return fallbackPrice
        return when (details.productType) {
            BillingClient.ProductType.SUBS -> {
                val pricingPhase = details.subscriptionOfferDetails
                    ?.firstOrNull()
                    ?.pricingPhases
                    ?.pricingPhaseList
                    ?.firstOrNull()
                pricingPhase?.formattedPrice ?: fallbackPrice
            }
            BillingClient.ProductType.INAPP -> {
                details.oneTimePurchaseOfferDetails?.formattedPrice ?: fallbackPrice
            }
            else -> fallbackPrice
        }
    }

    fun setProStatus(isPro: Boolean, tier: PocketProTier) {
        isProUser = isPro
        currentTier = tier
        val tierKey = when (tier) {
            PocketProTier.PRO_MONTHLY -> "pro_monthly"
            PocketProTier.PRO_YEARLY -> "pro_yearly"
            PocketProTier.PRO_LIFETIME -> "pro_lifetime"
            PocketProTier.FREE -> "free"
        }
        preferencesStore.setProMember(isPro, tierKey)
    }

    fun toggleDemoPro(unlocked: Boolean) {
        setProStatus(unlocked, if (unlocked) PocketProTier.PRO_YEARLY else PocketProTier.FREE)
    }
}
