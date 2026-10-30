package com.softx.app.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.billing.PocketBillingManager
import com.softx.app.billing.PocketProTier
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

private val GoldGradient = Brush.linearGradient(
    listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0xFFFFA000))
)
private val CyanGradient = Brush.linearGradient(
    listOf(Color(0xFF00E5FF), Color(0xFF0091EA))
)
private val PremiumCardBg = Color(0xFF131826)
private val PremiumBorder = Color(0xFF232B40)

@Composable
fun PocketPaywallScreen(
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val activity = context as? Activity
    val billingManager = remember { PocketBillingManager.getInstance(context) }
    var selectedPlanId by remember { mutableStateOf(PocketBillingManager.PRODUCT_YEARLY) }
    val isPro = billingManager.isProUser

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070913),
                        Color(0xFF0E1322),
                        Color(0xFF090D18),
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Top Navigation Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f)),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Surface(
                        color = Color(0xFFFFD54F).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.3f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPro) "PRO ACTIVE" else "POCKET VIP",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            billingManager.queryActivePurchases()
                            Toast.makeText(context, "Checking existing purchases...", Toast.LENGTH_SHORT).show()
                        },
                    ) {
                        Text("Restore", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            // Hero Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFFFD700).copy(alpha = 0.28f),
                                        Color.Transparent,
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0xFFFFD54F).copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(42.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Pocket Pro Plus",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "স্মার্ট ট্র্যাকিং ও স্বয়ংক্রিয় আর্থিক ব্যবস্থাপনা",
                        color = Color(0xFF00E5FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "ম্যানুয়াল টাইপিং বাদ দিয়ে বিকাশ, নগদ ও ব্যাংকের হিসাব রাখুন সম্পূর্ণ অটোমেটিক।",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                    )
                }
            }

            // Plan Selectors
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Annual Plan (Featured)
                    PaywallPlanCard(
                        title = "Annual Pro (বাৎসরিক)",
                        subtitle = "7 Days Free Trial included",
                        price = "৳৭৯৯ / বছর",
                        secondaryPrice = "মাত্র ৳৬৬ / মাস",
                        badge = "BEST VALUE • SAVE 35%",
                        isSelected = selectedPlanId == PocketBillingManager.PRODUCT_YEARLY,
                        accentColor = Color(0xFFFFD54F),
                        onClick = { selectedPlanId = PocketBillingManager.PRODUCT_YEARLY },
                    )

                    // Monthly Plan
                    PaywallPlanCard(
                        title = "Monthly Pro (মাসিক)",
                        subtitle = "Flexible month-to-month",
                        price = "৳৯৯ / মাস",
                        secondaryPrice = "Cancel anytime",
                        badge = "",
                        isSelected = selectedPlanId == PocketBillingManager.PRODUCT_MONTHLY,
                        accentColor = Color(0xFF00E5FF),
                        onClick = { selectedPlanId = PocketBillingManager.PRODUCT_MONTHLY },
                    )

                    // Lifetime VIP
                    PaywallPlanCard(
                        title = "Lifetime VIP (এককালীন)",
                        subtitle = "Pay once, use forever",
                        price = "৳১,৯৯৯",
                        secondaryPrice = "One-time payment • No renewal",
                        badge = "LIMITED DEAL",
                        isSelected = selectedPlanId == PocketBillingManager.PRODUCT_LIFETIME,
                        accentColor = AccentPink,
                        onClick = { selectedPlanId = PocketBillingManager.PRODUCT_LIFETIME },
                    )
                }
            }

            // Feature Checklist (Free vs Pro)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Everything in Pocket Pro Plus",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = PremiumCardBg,
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PremiumBorder),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            PaywallFeatureRow(
                                icon = Icons.Rounded.AutoAwesome,
                                iconTint = Color(0xFF00E5FF),
                                title = "Smart SMS Auto-Reader",
                                subtitle = "বিকাশ, নগদ ও ব্যাংকের এসএমএস থেকে আনলিমিটেড অটো-এন্ট্রি",
                            )
                            PaywallFeatureRow(
                                icon = Icons.Rounded.Share,
                                iconTint = AccentGreen,
                                title = "Unlimited WhatsApp Reminders",
                                subtitle = "বাকি আদায়ের জন্য যেকোনো সময় পেশাদার তাগাদা পাঠান",
                            )
                            PaywallFeatureRow(
                                icon = Icons.Rounded.PictureAsPdf,
                                iconTint = AccentPink,
                                title = "Branded Statement PDF & Excel",
                                subtitle = "ব্যবসায়িক বা ব্যক্তিগত ক্লিন স্টেটমেন্ট এক্সপোর্ট করুন",
                            )
                            PaywallFeatureRow(
                                icon = Icons.Rounded.CloudDone,
                                iconTint = Color(0xFFFFD54F),
                                title = "End-to-End Encrypted Cloud Sync",
                                subtitle = "ফোন হারালেও আপনার আর্থিক তথ্য সুরক্ষিত ও সর্বদা প্রস্তুত",
                            )
                            PaywallFeatureRow(
                                icon = Icons.Rounded.Security,
                                iconTint = AccentBlue,
                                title = "Zero Ads & 100% Privacy Guarantee",
                                subtitle = "কোনো প্রকার বিজ্ঞাপন ছাড়া নিরবচ্ছিন্ন প্রিমিয়াম অভিজ্ঞতা",
                            )
                        }
                    }
                }
            }

            // Social Proof / Testimonial
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    color = Color(0xFF0F1522),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row {
                            repeat(5) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "“বিকাশের অটো এসএমএস রিডার আমার প্রতিদিনের হিসাব রাখার সময় বাঁচিয়ে দিয়েছে!”",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        )
                    }
                }
            }

            // Demo / Sandbox Switch for Testing on Emulator
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                val nextState = !isPro
                                billingManager.toggleDemoPro(nextState)
                                Toast.makeText(
                                    context,
                                    if (nextState) "⚡ Pro Unlocked (Sandbox Mode)" else "Switched to Free Tier",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                        color = Color.White.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = if (isPro) Icons.Rounded.CheckCircle else Icons.Rounded.FlashOn,
                                contentDescription = null,
                                tint = if (isPro) AccentGreen else Color(0xFF00E5FF),
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPro) "Demo: Pro Active (Tap to lock)" else "Demo: Tap to Test-Unlock Pro",
                                color = if (isPro) AccentGreen else Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        // Floating Sticky CTA at Bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color(0xFF0A0D17).copy(alpha = 0.94f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = {
                        if (activity != null) {
                            billingManager.launchBillingFlow(
                                activity = activity,
                                productId = selectedPlanId,
                                onFallbackDemo = {
                                    billingManager.toggleDemoPro(true)
                                    Toast.makeText(context, "🎉 Welcome to Pocket Pro Plus!", Toast.LENGTH_LONG).show()
                                    onClose()
                                },
                            )
                        } else {
                            billingManager.toggleDemoPro(true)
                            onClose()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(12.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFFFFD54F)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD54F),
                        contentColor = Color(0xFF1A1200),
                    ),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPro) "YOU ARE ALREADY PRO" else if (selectedPlanId == PocketBillingManager.PRODUCT_YEARLY) "Start 7-Day Free Trial" else "Unlock Pocket Pro Plus",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                        )
                    }
                }

                Text(
                    text = "Google Play দ্বারা সুরক্ষিত • যেকোনো সময় বাতিলযোগ্য",
                    color = TextSecondary,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun PaywallPlanCard(
    title: String,
    subtitle: String,
    price: String,
    secondaryPrice: String,
    badge: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFF172033) else PremiumCardBg,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else PremiumBorder,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) accentColor else Color.Transparent)
                            .border(1.5.dp, if (isSelected) accentColor else TextSecondary.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0E1322))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }

                if (badge.isNotBlank()) {
                    Surface(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = badge,
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = secondaryPrice,
                        color = accentColor.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Text(
                    text = price,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun PaywallFeatureRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )
        }
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(16.dp),
        )
    }
}
