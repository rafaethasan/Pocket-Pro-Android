package com.softx.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun AppLockNativeScreen(
    preferencesStore: PocketNativePreferencesStore,
    signedInUser: PocketNativeUser?,
    onBack: () -> Unit,
) {
    val gate = pocketNativeFeatureGate(signedInUser, "app_lock")
    var lockedFeature by remember { mutableStateOf<PocketNativeFeatureGate?>(null) }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val enabled = remember(message) { preferencesStore.appLockEnabled() }

    LazyFeatureColumn(
        title = "App Lock",
        subtitle = if (gate.granted) {
            "Protect Pocket Pro with a PIN when you hand your phone to someone else."
        } else {
            "Protect Pocket Pro with a PIN when you hand your phone to someone else."
        },
        onBack = onBack,
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LockMetricCard("Status", if (enabled) "On" else "Off", if (enabled) AccentGreen else AccentPink, Modifier.weight(1f))
                LockMetricCard("PIN", if (preferencesStore.appLockPin().isBlank()) "Not set" else "${preferencesStore.appLockPin().length} digits", AccentPurple, Modifier.weight(1f))
            }
        }
        item {
            NativeEmptyCard(
                if (gate.granted) {
                    "Set a 4 to 6 digit PIN. Pocket Pro will ask for this PIN before opening your data."
                } else {
                    "Set a 4 to 6 digit PIN. Pocket Pro will ask for this PIN before opening your data."
                },
            )
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Set PIN", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(6) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("New PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentPurple,
                        unfocusedLabelColor = TextSecondary,
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    ),
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it.filter(Char::isDigit).take(6) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirm PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentPurple,
                        unfocusedLabelColor = TextSecondary,
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            if (!gate.granted) {
                                lockedFeature = gate
                            } else if (pin.length < 4) {
                                message = "PIN must be at least 4 digits."
                            } else if (pin != confirmPin) {
                                message = "PIN does not match."
                            } else {
                                preferencesStore.saveAppLock(pin)
                                message = "App lock enabled."
                                pin = ""
                                confirmPin = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceCard,
                            contentColor = TextPrimary,
                        ),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null)
                            Text("Enable", fontWeight = FontWeight.Bold)
                            PocketNativeProBadge()
                        }
                    }
                    Button(
                        onClick = {
                            if (!gate.granted) {
                                lockedFeature = gate
                            } else {
                                preferencesStore.disableAppLock()
                                message = "App lock disabled."
                                pin = ""
                                confirmPin = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceCard,
                            contentColor = TextPrimary,
                        ),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.LockOpen, contentDescription = null)
                            Text("Disable", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        if (message.isNotBlank()) {
            item {
                Text(
                    text = message,
                    color = if (message.contains("enabled", true) || message.contains("disabled", true)) AccentGreen else TextSecondary,
                    lineHeight = 21.sp,
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Password, contentDescription = null, tint = AccentPurple)
                Text("A PIN is required each time the app session starts.", color = TextSecondary)
            }
            HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(top = 12.dp))
        }
    }

    lockedFeature?.let { feature ->
        PocketNativeUpgradeDialog(
            gate = feature,
            onDismiss = { lockedFeature = null },
        )
    }
}

@Composable
private fun LockMetricCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(title, color = TextSecondary)
        Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        HorizontalDivider(color = accent.copy(alpha = 0.2f))
    }
}

@Composable
private fun LazyFeatureColumn(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = {
            item { Spacer(modifier = Modifier.height(34.dp)) }
            item {
                NativeFeatureTopBar(
                    title = title,
                    subtitle = subtitle,
                    onBack = onBack,
                )
            }
            content()
            item { Spacer(modifier = Modifier.height(34.dp)) }
        },
    )
}
