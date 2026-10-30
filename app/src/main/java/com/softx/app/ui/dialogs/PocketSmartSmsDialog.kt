package com.softx.app.ui.dialogs

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.parser.ParsedTransaction
import com.softx.app.parser.PocketTransactionParser
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.SurfaceCardSoft
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun PocketSmartSmsDialog(
    onDismiss: () -> Unit,
    onTransactionParsed: (ParsedTransaction) -> Unit,
) {
    val context = LocalContext.current
    var smsInput by remember { mutableStateOf("") }
    var parsedResult by remember { mutableStateOf<ParsedTransaction?>(null) }

    // Check clipboard on launch
    LaunchedEffect(Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            val item = clipboard.primaryClip?.getItemAt(0)
            val clipText = item?.text?.toString()?.trim().orEmpty()
            if (clipText.isNotBlank()) {
                val parsed = PocketTransactionParser.parse(clipText)
                if (parsed != null) {
                    smsInput = clipText
                    parsedResult = parsed
                }
            }
        }
    }

    fun updateInput(newText: String) {
        smsInput = newText
        parsedResult = if (newText.isNotBlank()) PocketTransactionParser.parse(newText) else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Smart SMS Reader",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                        Text(
                            text = "bKash, Nagad & Bank auto-detect",
                            color = TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "পেস্ট করুন আপনার ব্যাংক বা বিকাশ/নগদের এসএমএস। পকেট প্রো নিজে থেকেই পরিমাণ, ক্যাটাগরি ও মাধ্যম হিসাবভুক্ত করবে।",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )

                // Input box
                OutlinedTextField(
                    value = smsInput,
                    onValueChange = { updateInput(it) },
                    placeholder = {
                        Text(
                            "Paste transactional SMS here...",
                            color = TextSecondary.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp, max = 130.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF2A2E3B),
                        focusedContainerColor = SurfaceCardSoft,
                        unfocusedContainerColor = SurfaceCardSoft,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )

                // Quick Paste & Demo actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim().orEmpty()
                                if (clipText.isNotBlank()) updateInput(clipText)
                            },
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(Icons.Rounded.ContentPaste, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste Clip", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                updateInput("Payment Tk 450.00 to Agora Superstore successful. Fee Tk 0.00. Balance Tk 2,100.00. TrxID 8A29147 at 30/10/2026")
                            },
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(Icons.Rounded.FlashOn, contentDescription = null, tint = AccentPink, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("bKash Demo", color = AccentPink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                updateInput("Acct *1234 debited for BDT 1,250.00 on 30-Oct-26 at KFC DHANMONDI. Avail Bal BDT 12,340.00")
                            },
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(Icons.Rounded.FlashOn, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bank Demo", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Live Parsed Result Preview Card
                parsedResult?.let { parsed ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E2330))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (parsed.mode == "income") "Income Detected" else "Expense Detected",
                                    color = if (parsed.mode == "income") AccentBlue else AccentPink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                )
                            }
                            Text(
                                text = "৳" + if (parsed.amount % 1.0 == 0.0) parsed.amount.toLong().toString() else parsed.amount.toString(),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            )
                        }

                        Text(
                            text = "Counterparty: ${parsed.party}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Category: ${parsed.categoryKey.replaceFirstChar { it.uppercase() }}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                            )
                            Text(
                                text = "• Method: ${parsed.paymentMethod}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                            )
                            if (parsed.referenceId.isNotBlank()) {
                                Text(
                                    text = "• Ref: ${parsed.referenceId}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    parsedResult?.let {
                        onTransactionParsed(it)
                        onDismiss()
                    }
                },
                enabled = parsedResult != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF0D1117),
                    disabledContainerColor = Color(0xFF2A2E3B),
                    disabledContentColor = TextSecondary,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (parsedResult != null) "Auto-Fill Entry (হিসাব যোগ করুন)" else "Paste SMS to Auto-Fill",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        },
        dismissButton = {},
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(22.dp),
    )
}
