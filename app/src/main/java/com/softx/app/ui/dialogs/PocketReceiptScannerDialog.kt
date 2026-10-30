package com.softx.app.ui.dialogs

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.parser.ParsedReceipt
import com.softx.app.parser.PocketReceiptParser
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun PocketReceiptScannerDialog(
    onDismiss: () -> Unit,
    onReceiptScanned: (ParsedReceipt) -> Unit,
) {
    val context = LocalContext.current
    val preferencesStore = remember { PocketNativePreferencesStore(context) }
    val isBangla = preferencesStore.languageCode() == "bn"

    var isProcessing by remember { mutableStateOf(false) }
    var parsedResult by remember { mutableStateOf<ParsedReceipt?>(null) }
    var editableMerchant by remember { mutableStateOf("") }
    var editableAmount by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    fun processBitmap(bitmap: Bitmap) {
        isProcessing = true
        statusMessage = if (isBangla) "রসিদ স্ক্যান করা হচ্ছে..." else "Scanning receipt with OCR..."
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                isProcessing = false
                val result = PocketReceiptParser.parse(visionText.text)
                if (result != null) {
                    parsedResult = result
                    editableMerchant = result.merchant
                    editableAmount = if (result.amount % 1.0 == 0.0) result.amount.toLong().toString() else result.amount.toString()
                    statusMessage = if (isBangla) "সফলভাবে স্ক্যান সম্পন্ন!" else "Receipt parsed successfully!"
                } else {
                    statusMessage = if (isBangla) "কোনো খরচের পরিমাণ খুঁজে পাওয়া যায়নি।" else "Could not detect total amount on receipt."
                    Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                isProcessing = false
                statusMessage = "OCR error: ${e.localizedMessage}"
                Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show()
            }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            processBitmap(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            }.getOrNull()
            if (bitmap != null) {
                processBitmap(bitmap)
            } else {
                Toast.makeText(context, "Could not load selected image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loadSample(sampleText: String) {
        val result = PocketReceiptParser.parse(sampleText)
        if (result != null) {
            parsedResult = result
            editableMerchant = result.merchant
            editableAmount = if (result.amount % 1.0 == 0.0) result.amount.toLong().toString() else result.amount.toString()
            statusMessage = if (isBangla) "ডেমো রসিদ সফলভাবে রিড করা হয়েছে!" else "Demo receipt parsed successfully!"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(26.dp)),
            color = Color(0xFF101422),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column {
                            Text(
                                text = if (isBangla) "স্মার্ট রসিদ স্ক্যানার (OCR)" else "Smart Receipt Scanner",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                            Text(
                                text = if (isBangla) "বিল বা মেমোর ছবি তুলুন, বাকিটা অটোমেটিক" else "Snap or pick a receipt to auto-log",
                                color = TextSecondary,
                                fontSize = 12.sp,
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                        )
                    }
                }

                // Camera & Gallery Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFF070913),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "ক্যামেরা" else "Camera",
                                color = Color(0xFF070913),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                        }
                    }

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283F)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Image,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "গ্যালারি" else "Gallery",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }

                if (isProcessing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = statusMessage,
                            color = TextSecondary,
                            fontSize = 13.sp,
                        )
                    }
                }

                // Parsed Result Card
                val currentResult = parsedResult
                if (currentResult != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF181F33),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBangla) "স্ক্যানকৃত তথ্য" else "Detected Details",
                                        color = AccentGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                    )
                                }

                                Surface(
                                    color = Color(0xFF00E5FF).copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(
                                        text = currentResult.categoryKey.uppercase(),
                                        color = Color(0xFF00E5FF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = editableMerchant,
                                onValueChange = { editableMerchant = it },
                                label = { Text(if (isBangla) "দোকান / মার্চেন্টের নাম" else "Merchant / Store") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )

                            OutlinedTextField(
                                value = editableAmount,
                                onValueChange = { editableAmount = it },
                                label = { Text(if (isBangla) "মোট টাকার পরিমাণ" else "Total Amount") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )

                            Button(
                                onClick = {
                                    val finalAmount = editableAmount.toDoubleOrNull() ?: currentResult.amount
                                    val finalReceipt = currentResult.copy(
                                        merchant = editableMerchant.ifBlank { currentResult.merchant },
                                        amount = finalAmount,
                                    )
                                    onReceiptScanned(finalReceipt)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF070913),
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBangla) "✅ এক ট্যাপে খরচে এন্ট্রি দিন" else "✅ Auto-Fill to Expense",
                                        color = Color(0xFF070913),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    )
                                }
                            }
                        }
                    }
                }

                // Sample Receipts for Instant Testing
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBangla) "কাছে রসিদ না থাকলে ডেমো টেস্ট করুন:" else "Or test with demo sample receipts:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    loadSample("""
                                        AGORA SUPERSTORE
                                        Gulshan-2, Dhaka
                                        Date: 28/10/2026
                                        Milk, Eggs, Bread, Oil
                                        Subtotal: 1420.00
                                        Total Payable: 1491.00
                                        Thank You!
                                    """.trimIndent())
                                },
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        ) {
                            Text(
                                text = "🛒 Agora (৳1491)",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    loadSample("""
                                        STAR KABAB & RESTAURANT
                                        Dhanmondi, Dhaka
                                        Date: 29-10-2026
                                        Kacchi x 2, Borhani x 2
                                        Grand Total: Tk 850.00
                                        Paid by Card
                                    """.trimIndent())
                                },
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        ) {
                            Text(
                                text = "🍔 Star Kabab (৳850)",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    loadSample("""
                                        LABAID PHARMA
                                        Date: 2026-10-30
                                        Napa, Sergel, Monas
                                        NET TOTAL: 620.00
                                        Cash Paid: 1000.00
                                    """.trimIndent())
                                },
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        ) {
                            Text(
                                text = "💊 Labaid (৳620)",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
