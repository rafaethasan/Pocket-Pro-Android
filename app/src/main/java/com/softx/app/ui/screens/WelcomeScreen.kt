package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    onSeeRoadmap: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF121522), Color(0xFF14192A), Color(0xFF0A0C12)),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AccentPurple, AccentPink),
                        ),
                        shape = RoundedCornerShape(32.dp),
                    )
                    .padding(28.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(28.dp),
                    ) {
                        Text(
                            text = "Pocket Pro",
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "Keep every taka organized in one place",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "Track income, daily expenses, budgets, goals and reports with a flow that feels easy every day.",
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                    )
                }
            }
        }

        NativeFeatureCard(
            title = "Daily expense tracking",
            subtitle = "Add income and expenses quickly so your everyday records stay clear and up to date.",
            accent = AccentGreen,
            icon = Icons.Rounded.Wallet,
        )
        NativeFeatureCard(
            title = "Budgets and reports",
            subtitle = "Follow your budget, monthly progress and spending summaries without extra effort.",
            accent = AccentPurple,
            icon = Icons.Rounded.PieChart,
        )
        NativeFeatureCard(
            title = "Goals and planning",
            subtitle = "Save for personal targets, stay motivated, and keep long-term plans in view.",
            accent = AccentPink,
            icon = Icons.Rounded.Flag,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            shape = RoundedCornerShape(22.dp),
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
            Spacer(modifier = Modifier.padding(4.dp))
            Text("Get Started")
        }

        OutlinedButton(
            onClick = onSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(22.dp),
        ) {
            Text("Have an account? Sign in")
        }

        OutlinedButton(
            onClick = onSeeRoadmap,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text("Explore features")
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun NativeFeatureCard(
    title: String,
    subtitle: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                color = accent.copy(alpha = 0.2f),
                shape = RoundedCornerShape(18.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(12.dp),
                )
            }
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(subtitle, color = TextSecondary, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}
