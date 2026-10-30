package com.softx.app.billing

enum class PocketProTier(val label: String, val badge: String) {
    FREE("Free", "Basic"),
    PRO_MONTHLY("Pro Monthly", "Plus"),
    PRO_YEARLY("Pro Annual", "VIP Plus"),
    PRO_LIFETIME("Pro Lifetime", "VIP Elite")
}

data class PocketBillingProduct(
    val productId: String,
    val title: String,
    val subtitle: String,
    val period: String,
    val priceFormatted: String,
    val monthlyEquivalent: String = "",
    val discountPercent: String = "",
    val isRecommended: Boolean = false,
    val hasTrial: Boolean = false,
    val trialDays: Int = 7,
)

data class ProFeatureItem(
    val title: String,
    val description: String,
    val iconKey: String,
)
