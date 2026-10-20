package com.softx.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SouthEast
import androidx.compose.ui.graphics.vector.ImageVector

fun pocketFeatureIcon(feature: String): ImageVector = when (feature.trim().lowercase()) {
    "home" -> Icons.Rounded.Home
    "records" -> Icons.AutoMirrored.Rounded.ReceiptLong
    "add", "add entry" -> Icons.Rounded.Add
    "plans", "budget", "reminders" -> Icons.Rounded.CalendarMonth
    "goals" -> Icons.Rounded.Savings
    "accounts" -> Icons.Rounded.AccountBalanceWallet
    "search" -> Icons.Rounded.Search
    "reports" -> Icons.Rounded.BarChart
    "more", "settings", "tools" -> Icons.Rounded.Settings
    "profile" -> Icons.Rounded.Person
    else -> Icons.Rounded.Home
}

fun pocketFeatureAssetName(feature: String): String? = when (feature.trim().lowercase()) {
    "home" -> "house"
    "records" -> "receipt-text"
    "add", "add entry" -> "plus"
    "plans", "budget", "reminders" -> "calendar-days"
    "goals" -> "piggy-bank"
    "accounts" -> "wallet"
    "search" -> "search"
    "reports" -> "chart-column"
    "more", "settings", "tools" -> null
    "profile" -> "user-round"
    else -> null
}

fun pocketQuickAddIcon(mode: String): ImageVector = when (mode.trim().lowercase()) {
    "income" -> Icons.AutoMirrored.Rounded.TrendingUp
    "expense" -> Icons.AutoMirrored.Rounded.TrendingDown
    "give loan", "lent" -> Icons.Rounded.SouthEast
    "get back", "receive" -> Icons.Rounded.NorthWest
    "take loan", "borrow" -> Icons.Rounded.NorthWest
    "pay back", "repay" -> Icons.Rounded.SouthEast
    "close" -> Icons.Rounded.Close
    else -> Icons.Rounded.Add
}

fun pocketQuickAddAssetName(mode: String): String? = when (mode.trim().lowercase()) {
    "income" -> "trending-up"
    "expense" -> "trending-down"
    "give loan", "lent" -> "arrow-down-right"
    "get back", "receive" -> "arrow-up-left"
    "take loan", "borrow" -> "arrow-up-left"
    "pay back", "repay" -> "arrow-down-right"
    "close" -> "circle-x"
    else -> null
}
