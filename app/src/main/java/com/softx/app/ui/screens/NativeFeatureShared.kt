package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun NativeFeatureTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.06f),
                    contentColor = TextPrimary,
                ),
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp)),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                if (subtitle.isNotBlank()) {
                    NativeInlineInfoButton(title = title, message = subtitle)
                }
            }
            Spacer(modifier = Modifier.width(48.dp))
        }
        androidx.compose.material3.HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
    }
}

/** Keeps contextual help available without adding a second explanatory line to the UI. */
@Composable
fun NativeInlineInfoButton(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    var showInfo by remember(title, message) { mutableStateOf(false) }

    IconButton(
        onClick = { showInfo = true },
        modifier = modifier.size(30.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = "About $title",
            tint = TextSecondary,
            modifier = Modifier.size(17.dp),
        )
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(message, color = TextSecondary, lineHeight = 21.sp) },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("Got it") }
            },
        )
    }
}

@Composable
fun NativeGradientHeroCard(
    title: String,
    subtitle: String,
    accentColors: List<Color>,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(accentColors))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(title, color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = TextPrimary.copy(alpha = 0.82f), lineHeight = 22.sp)
            }
            content()
        }
    }
}

@Composable
fun NativeMetricPill(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.08f)),
                    ),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, color = TextSecondary)
            Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        }
    }
}

@Composable
fun SectionLabel(
    title: String,
    trailing: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        if (trailing.isNotBlank()) {
            Text(trailing, color = TextSecondary)
        }
    }
}

@Composable
fun NativeEmptyCard(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = message,
            color = TextSecondary,
            lineHeight = 21.sp,
        )
        androidx.compose.material3.HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
    }
}

@Composable
fun NativeSimpleErrorCard(
    title: String,
    body: String,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(modifier = Modifier.height(34.dp)) }
        item {
            NativeFeatureTopBar(
                title = title,
                subtitle = body,
                onBack = onBack,
            )
        }
        item {
            NativeEmptyCard(body)
        }
    }
}

fun colorFromHex(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    AccentPurple
}

fun nativeSharedMoney(amount: Double, symbol: String): String {
    val clean = if (amount % 1.0 == 0.0) String.format(Locale.US, "%,d", amount.toLong()) else String.format(Locale.US, "%,.2f", amount)
    return "$symbol$clean"
}

fun signedSharedMoney(amount: Double, symbol: String): String {
    val clean = nativeSharedMoney(kotlin.math.abs(amount), symbol)
    return if (amount >= 0) "+$clean" else "-$clean"
}
