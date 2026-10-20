package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: PocketNativeRepository,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    onLoginSuccess: (PocketNativeUser) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3A2265), Color(0xFF1B1A47), Color(0xFF090F24)),
                    radius = 1200f,
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 14.dp)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                Text("Back", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.size(4.dp))

        Text(
            text = "Welcome back",
            color = TextPrimary,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Sign in with your Pocket Pro email and password to continue where you left off.",
            color = TextSecondary,
            lineHeight = 24.sp,
        )

        Spacer(modifier = Modifier.size(4.dp))

        FilledAuthTextField(
            value = email,
            onValueChange = {
                email = it
                errorText = ""
            },
            label = "Email or username",
            leadingIcon = Icons.Rounded.Email,
        )

        FilledAuthTextField(
            value = password,
            onValueChange = {
                password = it
                errorText = ""
            },
            label = "Password",
            leadingIcon = Icons.Rounded.Lock,
            trailingIcon = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
            onTrailingIconClick = { showPassword = !showPassword },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onForgotPassword) {
                Text("Forgot password?", color = AccentPurple, fontWeight = FontWeight.SemiBold)
            }
        }

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
                text = "Sign In",
                onClick = {
                    if (loading) return@GradientActionButton
                    errorText = ""
                    loading = true
                    scope.launch {
                        val result = repository.login(email, password)
                        loading = false
                        if (result.isSuccess) {
                            onLoginSuccess(result.getOrThrow())
                        } else {
                            errorText = result.exceptionOrNull()?.message ?: "We could not sign you in. Please try again."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                startColor = AccentPurple,
                endColor = AccentPink,
            )
        }

        Surface(
            color = Color.White.copy(alpha = 0.06f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        ) {
            Text(
                text = "Use the same Pocket Pro email and password to open your records, budgets, goals and reports.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                color = TextSecondary,
                lineHeight = 21.sp,
            )
        }

        Spacer(modifier = Modifier.size(8.dp))
    }
}
