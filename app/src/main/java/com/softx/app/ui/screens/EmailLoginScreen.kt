package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun EmailLoginScreen(
    repository: PocketNativeRepository,
    onBack: () -> Unit,
    onLoginSuccess: (PocketNativeUser) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF111522), Color(0xFF0D1017)),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 14.dp)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Text("Email login", color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(
            "Use your Pocket Pro email or login ID to continue to your money records, budget and reports.",
            color = TextSecondary,
            lineHeight = 23.sp,
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
            label = { Text("Email or login ID") },
            singleLine = true,
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
            label = { Text("Password") },
            singleLine = true,
        )

        if (errorText.isNotBlank()) {
            Surface(
                color = Color(0xFF351B1B),
                shape = RoundedCornerShape(22.dp),
            ) {
                Text(
                    text = errorText,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFFFC2C2),
                    lineHeight = 20.sp,
                )
            }
        }

        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Helpful note", color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    "Use the same account details you already use so your expenses, budget and reports stay together.",
                    color = TextSecondary,
                    lineHeight = 22.sp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Your personal records will be ready after sign-in", color = AccentGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        Button(
            onClick = {
                if (loading) {
                    return@Button
                }
                errorText = ""
                loading = true
                scope.launch {
                    val result = repository.login(email, password)
                    loading = false
                    if (result.isSuccess) {
                        onLoginSuccess(result.getOrThrow())
                    } else {
                        errorText = result.exceptionOrNull()?.message ?: "We could not sign you in right now."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Email, contentDescription = null)
                    Text("Sign in")
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
        ) {
            Text("Back to sign-in options")
        }
    }
}
