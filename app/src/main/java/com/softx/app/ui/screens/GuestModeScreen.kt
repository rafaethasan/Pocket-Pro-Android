package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun GuestModeScreen(
    repository: PocketNativeRepository,
    preferencesStore: PocketNativePreferencesStore,
    onBack: () -> Unit,
    onCreateAccount: () -> Unit,
    onContinue: (PocketNativeUser) -> Unit,
) {
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E254F), Color(0xFF11183B), Color(0xFF090F24)),
                    radius = 1200f,
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 14.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        TextButton(onClick = onBack) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                Text("Back", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        Surface(
            color = AccentOrange,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Visibility,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(30.dp),
            )
        }

        Text(
            text = "Guest mode",
            color = TextPrimary,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Start using Pocket Pro right away without creating an account first.",
            color = TextSecondary,
            lineHeight = 24.sp,
        )

        GuestInfoCard(
            icon = Icons.Rounded.Storage,
            text = "Your guest records stay only on this device until you create an account.",
        )
        GuestInfoCard(
            icon = Icons.Rounded.CloudOff,
            text = "Cloud sync and backup are not available while you stay in guest mode.",
        )
        GuestInfoCard(
            icon = Icons.Rounded.Security,
            text = "If the app is removed or the device is reset, guest data may be lost.",
        )

        if (errorText.isNotBlank()) {
            Surface(
                color = Color(0xFF351B1B),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = errorText,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFFFC2C2),
                    lineHeight = 20.sp,
                )
            }
        }

        if (loading) {
            Surface(
                color = Color.White.copy(alpha = 0.07f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.2.dp,
                        color = Color.White,
                    )
                }
            }
        } else {
            GradientActionButton(
                text = "Continue as guest",
                onClick = {
                    loading = true
                    errorText = ""
                    scope.launch {
                        val result = repository.quickStart(
                            uiLanguage = preferencesStore.languageCode(),
                            currencyCode = preferencesStore.currencyCode(),
                        )
                        loading = false
                        if (result.isSuccess) {
                            onContinue(result.getOrThrow())
                        } else {
                            errorText = result.exceptionOrNull()?.message ?: "Guest mode could not be started right now."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                startColor = Color(0xFFFF9200),
                endColor = Color(0xFFFF6200),
            )
        }

        GradientActionButton(
            text = "Create account instead",
            onClick = onCreateAccount,
            modifier = Modifier.fillMaxWidth(),
            startColor = AccentBlue,
            endColor = Color(0xFF7A4CFF),
        )

        Text(
            text = "You can create an account later and keep using the same simple money flow.",
            color = TextSecondary,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Composable
private fun GuestInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Surface(
        color = Color.White.copy(alpha = 0.07f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = AccentOrange.copy(alpha = 0.18f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFFFB34D),
                    modifier = Modifier.padding(12.dp),
                )
            }
            Text(
                text = text,
                color = TextSecondary,
                lineHeight = 21.sp,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
