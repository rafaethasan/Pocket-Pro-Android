package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.SurfaceCardSoft
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun NativeTabPlaceholderScreen(
    destination: String,
    signedInUser: PocketNativeUser?,
    onOpenRoadmap: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0C1018), Color(0xFF101624)),
                ),
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(destination, color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = "${signedInUser?.fullName ?: "Pocket Pro User"}, this section is getting ready.",
                    color = TextSecondary,
                    lineHeight = 22.sp,
                )
                Text(
                    text = "Please go back for now and try again after the next update.",
                    color = TextSecondary,
                    lineHeight = 22.sp,
                )
                AssistChip(
                    onClick = onOpenRoadmap,
                    colors = AssistChipDefaults.assistChipColors(containerColor = SurfaceCardSoft),
                    label = { Text("Go back") },
                    leadingIcon = {
                        Icon(Icons.Rounded.AutoGraph, contentDescription = null, tint = AccentOrange)
                    },
                )
            }
        }
    }
}
