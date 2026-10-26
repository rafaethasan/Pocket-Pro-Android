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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonAddAlt1
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.BuildConfig
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple

private val WelcomeTitle = Color(0xFFFFFFFF)
private val WelcomeBody = Color(0xFFE5D8F3)

@Composable
fun GetStartedScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onGuest: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val termsUrl = "${BuildConfig.POCKET_BASE_URL}pocket/terms-of-use"
    val privacyUrl = "${BuildConfig.POCKET_BASE_URL}pocket/privacy-policy"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4A1672), Color(0xFF5A1E73), Color(0xFF2C1048)),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Spacer(modifier = Modifier.height(26.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PocketBrandBadge()
                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    text = "Pocket Pro",
                    color = WelcomeTitle,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.padding(top = 14.dp))
                Text(
                    text = "Track daily expenses, income, budgets, goals and reports with a clean flow that feels easy from the first tap.",
                    color = WelcomeBody,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GradientActionButton(
                    text = "Sign In",
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth(),
                    startColor = Color(0xFF7A3BFF),
                    endColor = Color(0xFFAE4BFF),
                )

                OutlinedButton(
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WelcomeTitle),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.22f),
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.PersonAddAlt1,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 10.dp),
                        )
                        Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                TextButton(onClick = onGuest) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.Visibility,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.88f),
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(
                            text = "Continue without account",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Text(
                    text = "By continuing, you agree to the Pocket Pro Terms of Use and Privacy Policy.",
                    color = WelcomeBody,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = { uriHandler.openUri(termsUrl) }) {
                        Text("Terms of Use", color = AccentPink)
                    }
                    TextButton(onClick = { uriHandler.openUri(privacyUrl) }) {
                        Text("Privacy Policy", color = AccentPurple)
                    }
                }
            }
        }
    }
}
