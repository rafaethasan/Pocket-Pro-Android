package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun LoginOptionsScreen(
    onBack: () -> Unit,
    onEmail: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0E121C), Color(0xFF13192A)),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 14.dp)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Text("Sign in options", color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(
            "Choose the sign-in option that feels easiest for you.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp,
        )

        NativeAuthButton("Login with Email", AccentGreen, Icons.Rounded.Email, onEmail)

        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(22.dp),
        ) {
            Text(
                text = "Use your Pocket Pro email and password to keep your records, budgets and reports together.",
                modifier = Modifier.padding(18.dp),
                color = TextSecondary,
                lineHeight = 22.sp,
            )
        }
    }
}

@Composable
private fun NativeAuthButton(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
    ) {
        Icon(icon, contentDescription = null)
        Text(
            text = label,
            modifier = Modifier.padding(start = 10.dp, top = 6.dp, bottom = 6.dp),
            fontWeight = FontWeight.Bold,
        )
    }
}
