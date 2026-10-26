package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentPurple

private val SetupTitle = Color(0xFFF8FAFF)
private val SetupBody = Color(0xFFD6E1F8)
private val SetupCard = Color(0xFF1B2A4A)

@Composable
fun InitialSetupScreen(
    preferencesStore: PocketNativePreferencesStore,
    onDone: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedLanguage by remember { mutableStateOf(preferencesStore.languageCode()) }
    var selectedCurrency by remember { mutableStateOf(preferencesStore.currencyCode()) }
    val selectedLanguageOption = PocketNativePreferencesStore.languageOptionFor(selectedLanguage)

    val filteredCurrencies = remember(selectedLanguage) {
        val priority = PocketNativePreferencesStore.CURRENCY_OPTIONS.filter {
            selectedLanguageOption.relatedCurrencies.contains(it.code)
        }
        val remaining = PocketNativePreferencesStore.CURRENCY_OPTIONS.filterNot {
            selectedLanguageOption.relatedCurrencies.contains(it.code)
        }
        priority + remaining
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF172B57), Color(0xFF111B3D), Color(0xFF091022)),
                    radius = 1200f,
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 14.dp)
            .padding(horizontal = 22.dp, vertical = 18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (step == 1) {
                    TextButton(onClick = { step = 0 }) {
                        Row {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = SetupTitle)
                            Text("Back", color = SetupTitle, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Box(modifier = Modifier.height(1.dp))
                }

                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(
                        text = if (step == 0) "Step 1 of 2" else "Step 2 of 2",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            PocketBrandBadge(
                iconSize = 76.dp,
                containerPadding = 18.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Text(
                text = "Personal money manager",
                color = SetupBody.copy(alpha = 0.88f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Text(
                text = if (step == 0) "Choose your language" else "Choose your currency",
                color = SetupTitle,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = if (step == 0) {
                    "Pocket Pro starts in English the first time. Pick the language you want to use, then continue to your currency."
                } else {
                    "Choose the currency you want to use for balances, budgets and reports. The most relevant currencies are shown first."
                },
                color = SetupBody,
                lineHeight = 24.sp,
            )

            Surface(
                color = SetupCard.copy(alpha = 0.92f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = if (step == 0) AccentBlue.copy(alpha = 0.18f) else AccentGreen.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(
                            imageVector = if (step == 0) Icons.Rounded.Language else Icons.Rounded.Payments,
                            contentDescription = null,
                            tint = if (step == 0) AccentBlue else AccentGreen,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    Column {
                        Text(
                            text = if (step == 0) "Current language" else "Current currency",
                            color = SetupBody,
                            fontSize = 13.sp,
                        )
                        Text(
                            text = if (step == 0) {
                                PocketNativePreferencesStore.languageOptionFor(selectedLanguage).label
                            } else {
                                val option = PocketNativePreferencesStore.currencyOptionFor(selectedCurrency)
                                "${option.code} • ${option.label}"
                            },
                            color = SetupTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                        )
                    }
                }
            }

            if (step == 0) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(PocketNativePreferencesStore.LANGUAGE_OPTIONS.take(10)) { option ->
                        SetupChoiceCard(
                            title = option.label,
                            subtitle = option.nativeLabel,
                            selected = option.code == selectedLanguage,
                            onClick = {
                                selectedLanguage = option.code
                                preferencesStore.setLanguageCode(option.code)
                            },
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filteredCurrencies.take(14)) { option ->
                        SetupChoiceCard(
                            title = "${option.code} • ${option.label}",
                            subtitle = "Symbol ${option.symbol}",
                            selected = option.code == selectedCurrency,
                            onClick = {
                                selectedCurrency = option.code
                                preferencesStore.setCurrencyCode(option.code)
                            },
                        )
                    }
                }
            }

            GradientActionButton(
                text = if (step == 0) "Continue" else "Open Pocket Pro",
                onClick = {
                    if (step == 0) {
                        step = 1
                    } else {
                        preferencesStore.setHasCompletedInitialSetup(true)
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                startColor = if (step == 0) AccentBlue else AccentPurple,
                endColor = if (step == 0) AccentPurple else AccentGreen,
            )
        }
    }
}

@Composable
private fun SetupChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = SetupCard.copy(alpha = 0.88f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    color = SetupTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
                Text(
                    text = subtitle,
                    color = SetupBody,
                    fontSize = 14.sp,
                )
            }
            Surface(
                color = if (selected) AccentGreen.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = if (selected) "Selected" else "Choose",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    color = if (selected) Color(0xFF70F0C1) else SetupBody,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}
