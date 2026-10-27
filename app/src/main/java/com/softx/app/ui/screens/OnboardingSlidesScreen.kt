package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

private data class PocketOnboardingSlide(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconStart: Color,
    val iconEnd: Color,
    val glowColor: Color,
    val buttonStart: Color,
    val buttonEnd: Color,
)

@Composable
fun OnboardingSlidesScreen(
    onSkip: () -> Unit,
    onComplete: () -> Unit,
) {
    val slides = remember {
        listOf(
            PocketOnboardingSlide(
                title = "Your money, at a glance",
                subtitle = "Home shows your available balance first. Tap the eye icon whenever you want to hide or reveal it.",
                icon = Icons.Rounded.AccountBalanceWallet,
                iconStart = Color(0xFF3283FF),
                iconEnd = Color(0xFF11B3D9),
                glowColor = Color(0xFF0D89D0),
                buttonStart = Color(0xFF3283FF),
                buttonEnd = Color(0xFF11B3D9),
            ),
            PocketOnboardingSlide(
                title = "Add a record in seconds",
                subtitle = "Use the + button for income, expense, loan given, loan taken, payback, or money received.",
                icon = Icons.Rounded.AddCircle,
                iconStart = Color(0xFFB03CFF),
                iconEnd = Color(0xFFF33892),
                glowColor = Color(0xFF7B379C),
                buttonStart = Color(0xFFB03CFF),
                buttonEnd = Color(0xFFF33892),
            ),
            PocketOnboardingSlide(
                title = "Keep loans clear",
                subtitle = "Choose the right loan action, add the person and amount, then track every payment until it is settled.",
                icon = Icons.AutoMirrored.Rounded.TrendingUp,
                iconStart = Color(0xFF0DBF9E),
                iconEnd = Color(0xFF13D7B0),
                glowColor = Color(0xFF0C7A6A),
                buttonStart = Color(0xFF0DBF9E),
                buttonEnd = Color(0xFF13D7B0),
            ),
            PocketOnboardingSlide(
                title = "Review and stay protected",
                subtitle = "Records keeps your full history. Reports shows progress, and Backup & Restore keeps a copy of your data.",
                icon = Icons.Rounded.BarChart,
                iconStart = Color(0xFFFF7B19),
                iconEnd = Color(0xFFFF3648),
                glowColor = Color(0xFF8D3B2F),
                buttonStart = Color(0xFFFF7B19),
                buttonEnd = Color(0xFFFF3648),
            ),
        )
    }
    var page by remember { mutableIntStateOf(0) }
    val current = slides[page]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        current.glowColor.copy(alpha = 0.52f),
                        Color(0xFF111732),
                        Color(0xFF090D21),
                    ),
                    radius = 980f,
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Quick tour • ${page + 1} of ${slides.size}",
                    color = TextSecondary.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                    IconButton(onClick = onSkip) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close quick tour",
                            tint = TextSecondary,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(244.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    current.glowColor.copy(alpha = 0.42f),
                                    current.glowColor.copy(alpha = 0.08f),
                                    Color.Transparent,
                                ),
                            ),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(126.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(current.iconStart, current.iconEnd),
                                ),
                                shape = RoundedCornerShape(28.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = current.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(58.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = current.title,
                    color = TextPrimary,
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = current.subtitle,
                    color = TextSecondary,
                    fontSize = 18.sp,
                    lineHeight = 29.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    slides.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .size(if (index == page) 28.dp else 10.dp, 10.dp)
                                .background(
                                    color = if (index == page) Color.White else Color.White.copy(alpha = 0.28f),
                                    shape = RoundedCornerShape(20.dp),
                                ),
                        )
                    }
                }

                GradientActionButton(
                    text = if (page == slides.lastIndex) "Open Pocket Pro" else "Next",
                    onClick = {
                        if (page == slides.lastIndex) {
                            onComplete()
                        } else {
                            page += 1
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    startColor = current.buttonStart,
                    endColor = current.buttonEnd,
                )
            }
        }
    }
}
