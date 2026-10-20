package com.softx.app.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun PocketDeleteConfirmationDialog(
    title: String,
    message: String,
    deleting: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!deleting) onDismiss()
        },
        title = {
            Text(
                title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                message,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        },
        dismissButton = {
            TextButton(
                enabled = !deleting,
                onClick = onDismiss,
            ) {
                Text("No", color = TextSecondary)
            }
        },
        confirmButton = {
            TextButton(
                enabled = !deleting,
                onClick = onConfirm,
            ) {
                Text(
                    if (deleting) "Deleting..." else "Yes",
                    color = AccentPink,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        containerColor = SurfaceCard,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    )
}
