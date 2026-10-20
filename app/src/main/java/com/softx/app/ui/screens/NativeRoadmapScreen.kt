package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.InsertChart
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun NativeRoadmapScreen(
    onBack: () -> Unit,
) {
    val features = listOf(
        Triple("Daily money records", "Add income, expenses and lent amounts in one place so every record stays easy to follow.", Icons.Rounded.Wallet),
        Triple("Budget planning", "Set spending limits and see how much you have already used this month.", Icons.Rounded.InsertChart),
        Triple("Savings goals", "Create personal goals, save step by step and keep progress visible.", Icons.Rounded.Flag),
        Triple("Reports and insights", "Review categories, balances and monthly summaries whenever you need them.", Icons.Rounded.Savings),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0C1018), Color(0xFF101624)),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 14.dp)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }
        Text("What you can do in Pocket Pro", color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(
            "Pocket Pro helps you manage everyday money, stay on top of your plans, and understand your spending more clearly.",
            color = TextSecondary,
            lineHeight = 22.sp,
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            itemsIndexed(features) { index, item ->
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(22.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            "Feature ${index + 1}",
                            color = if (index == 0) AccentGreen else AccentOrange,
                            fontWeight = FontWeight.Bold,
                        )
                        Icon(item.third, contentDescription = null, tint = if (index % 2 == 0) AccentGreen else AccentOrange)
                        Text(item.first, color = TextPrimary, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
                        Text(item.second, color = TextSecondary, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}
