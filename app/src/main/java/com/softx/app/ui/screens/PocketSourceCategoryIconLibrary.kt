package com.softx.app.ui.screens

import com.softx.app.data.PocketNativeCategoryIconOption
import java.util.Locale

/*
 * Curated category icon catalog for Pocket Pro.
 *
 * Source inspiration:
 * - Lucide categories: https://lucide.dev/icons/categories
 * - Remix Icon: https://remixicon.com/
 *
 * We keep the app-side icon keys normalized so the picker can expose a large,
 * stable library even when the backend sends a smaller icon list.
 */

private val pocketSourceCategoryIconKeys = linkedMapOf(
    "Housing & Living" to listOf(
        "home", "building", "apartment", "warehouse", "door", "key", "lock", "couch", "bed", "lamp",
        "fan", "hammer", "paint_roller", "toolbox", "brick_wall", "shield_home", "garage", "house_heart", "stairs", "window",
    ),
    "Utilities & Bills" to listOf(
        "bolt", "zap", "flame", "droplet", "water", "wifi", "router", "signal", "smartphone", "phone",
        "tv", "satellite", "plug", "battery", "charging_station", "receipt", "file_text", "file_invoice", "credit_card", "barcode",
    ),
    "Food & Grocery" to listOf(
        "shopping_cart", "basket", "shopping_bag", "leaf", "apple", "carrot", "fish", "drumstick", "egg", "wheat",
        "burger", "pizza", "coffee", "tea", "cup", "utensils", "spoon", "fork", "knife", "store",
    ),
    "Transportation" to listOf(
        "car", "bus", "bicycle", "train", "taxi", "fuel_pump", "map", "navigation", "compass", "parking_circle",
        "traffic_light", "road", "steering_wheel", "engine", "wrench", "truck", "airplane", "ship", "scooter", "route",
    ),
    "Income" to listOf(
        "wallet", "coins", "banknote", "dollar_sign", "income_credit_card", "briefcase", "chart_line", "trending_up", "gift", "trophy",
        "crown", "piggy_bank", "safe", "income_receipt", "calculator", "target", "bank", "cash", "badge_dollar_sign", "hand_coins",
    ),
    "Shopping" to listOf(
        "retail_bag", "retail_cart", "boutique_store", "storefront", "shirt", "dress", "shoe", "watch", "sunglasses", "perfume",
        "parcel_box", "package", "delivery_truck", "tag", "badge_percent", "price_barcode", "gift_box", "featured_star", "wishlist_heart", "hanger",
    ),
    "Health & Medical" to listOf(
        "heart", "activity", "pulse", "hospital", "cross", "plus_circle", "pill", "capsules", "syringe", "stethoscope",
        "user_md", "bandage", "thermometer", "shield_plus", "first_aid", "dna", "flask", "microscope", "heart_pulse", "medical_cross",
    ),
    "Education" to listOf(
        "book", "book_open", "notebook", "pencil", "pen", "school", "graduation_cap", "university", "library", "laptop",
        "monitor", "certificate", "award", "chalkboard", "ruler", "eraser", "clipboard", "exam", "brain", "lightbulb",
    ),
    "Entertainment & Lifestyle" to listOf(
        "film", "tv_entertainment", "music", "headphone", "speaker", "play", "pause", "gamepad", "camera", "image",
        "festival_airplane", "beach", "sun", "party_popper", "fireworks", "ticket", "entertainment_star", "smile", "mic", "disco_ball",
    ),
    "Family & Personal" to listOf(
        "users", "user", "family_baby", "family_child", "couple", "family", "family_heart", "family_home", "family_gift", "warm_smile",
        "hand_heart", "user_circle", "contact", "handshake", "hug", "support", "people", "love", "user_check", "user_plus",
    ),
    "Finance & Payments" to listOf(
        "payment_card", "debit_card", "bank_branch", "atm", "transaction", "send", "payment_repeat", "loan", "billing_calendar", "calendar_check",
        "finance_piggy_bank", "savings", "investment", "finance_chart_bar", "finance_chart_pie", "tax", "payment_receipt", "invoice_sheet", "money_wallet", "scale",
    ),
    "Fixed Monthly Expenses" to listOf(
        "monthly_calendar", "monthly_repeat", "renew_cycle", "bill_sync", "clock", "alarm", "bell", "bill_schedule", "bill_receipt", "bill_invoice",
        "rent_home", "internet_wifi", "tuition_school", "service_subscription", "monthly_payment", "due", "bill_checklist", "timeline", "history", "calendar_days",
    ),
    "Business Expense" to listOf(
        "office", "business_building", "company", "team", "staff_group", "meeting", "bullhorn", "ads", "business_laptop", "code",
        "business_tools", "analytics", "business_chart_line", "growth", "business_chart_bar", "budget", "business_finance", "project", "strategy", "business_briefcase",
    ),
    "Miscellaneous" to listOf(
        "more_horizontal", "more_vertical", "dots", "misc_settings", "options", "shuffle", "misc_alert", "misc_warning", "misc_help_circle", "misc_info",
        "question_mark_circle", "archive_box", "archive", "folder", "file", "misc_star", "circle", "grid", "layers", "squares",
    ),
    "Travel & Tour" to listOf(
        "travel_airplane", "passport", "luggage", "travel_map", "location", "hotel", "travel_beach", "travel_sun", "travel_camera", "travel_compass",
        "travel_route", "globe", "travel_navigation", "travel_ticket", "rental_car", "travel_train", "cruise_ship", "backpack", "travel_bag", "mountain",
    ),
    "Subscription & Services" to listOf(
        "service_repeat", "service_refresh", "service_sync", "subscription_plan", "media_play", "service_tv", "stream_music", "app_window", "cloud", "server",
        "service_package", "service_box", "service", "service_settings", "service_calendar", "billing", "invoice", "service_payment", "service_wifi", "service_monitor",
    ),
    "Savings & Investment" to listOf(
        "savings_piggy_bank", "savings_chart_line", "savings_chart_bar", "savings_chart_pie", "savings_coins", "savings_wallet", "savings_bank", "savings_safe", "savings_trending_up", "savings_investment",
        "savings_target", "goal", "graph", "wealth_finance", "money", "savings_cash", "stack", "vault", "capital_growth", "percent",
    ),
    "Kids & Baby" to listOf(
        "baby", "child", "toy", "teddy_bear", "bottle", "stroller", "kids_school", "kids_book", "kids_pencil", "kids_gamepad",
        "kids_gift", "kids_smile", "kids_heart", "kids_family", "care", "diaper", "milk", "playground", "blocks", "balloon",
    ),
    "Work & Office" to listOf(
        "work_briefcase", "work_laptop", "desktop", "work_meeting", "work_users", "work_calendar", "work_schedule", "work_checklist", "work_clipboard", "document",
        "work_file_text", "email", "inbox", "task", "work_project", "workflow", "work_office", "chair", "desk", "printer",
    ),
    "Emergency & Unexpected" to listOf(
        "emergency_alert", "emergency_warning", "siren", "shield", "ambulance", "emergency_hospital", "fire", "emergency_flame", "police", "emergency_help_circle",
        "emergency_info", "emergency", "emergency_cross", "emergency_first_aid", "emergency_bandage", "exclamation_circle", "bug", "fix", "repair", "emergency_tools",
    ),
)

private fun humanizeIconKey(key: String): String =
    key.split('_')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase(Locale.getDefault()) else first.toString()
            }
        }

fun pocketNormalizeIconLookup(raw: String): String {
    val qualifierPrefixes = listOf(
        "income_", "retail_", "parcel_", "price_", "featured_", "wishlist_", "festival_",
        "family_", "payment_", "finance_", "money_", "monthly_", "renew_", "bill_",
        "rent_", "internet_", "tuition_", "business_", "misc_", "travel_", "rental_",
        "cruise_", "service_", "savings_", "wealth_", "capital_", "kids_", "work_",
        "emergency_", "warm_",
    )
    var normalized = raw.lowercase(Locale.US)
    qualifierPrefixes.forEach { prefix ->
        if (normalized.startsWith(prefix)) {
            normalized = normalized.removePrefix(prefix)
        }
    }
    return normalized.replace('_', ' ')
}

fun pocketSourceCategoryIconCount(): Int =
    pocketSourceCategoryIconKeys.values.sumOf { it.size }

fun pocketMergedCategoryIconSections(
    remoteOptions: List<PocketNativeCategoryIconOption>,
): Map<String, List<PocketNativeCategoryIconOption>> {
    val remoteByKey = remoteOptions.associateBy { it.key.lowercase() }
    val used = linkedSetOf<String>()
    val merged = linkedMapOf<String, List<PocketNativeCategoryIconOption>>()

    pocketSourceCategoryIconKeys.forEach { (section, keys) ->
        val icons = keys.map { key ->
            val normalizedKey = key.lowercase()
            used += normalizedKey
            remoteByKey[normalizedKey] ?: PocketNativeCategoryIconOption(
                key = key,
                label = humanizeIconKey(key),
                iconClass = key,
            )
        }
        merged[section] = icons
    }

    val more = remoteOptions
        .filterNot { it.key.lowercase() in used }
        .distinctBy { it.key.lowercase() }
    if (more.isNotEmpty()) {
        merged["More"] = more
    }

    return merged
}
