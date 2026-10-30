package com.softx.app.ui.screens

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.SmokingRooms
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Liquor
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.data.PocketNativeTransactionAccountOption
import com.softx.app.data.PocketNativeTransactionCategoryOption
import com.softx.app.data.PocketNativeTransactionFormPayload
import com.softx.app.data.PocketNativeTransactionPaymentMethod
import com.softx.app.data.PocketNativeUser
import com.softx.app.PocketReminderScheduler
import com.softx.app.ui.dialogs.PocketSmartSmsDialog
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.SurfaceCardSoft
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.Calendar

private val AddScreenPanel = Color(0xFF191C24)
private val AddScreenPanelBorder = Color(0xFF262A35)
private val AddScreenAccentLine = Color(0xFFB04358)
private val AddScreenFlatDivider = Color(0xFF2C2F37)

private data class AddModeMeta(
    val key: String,
    val title: String,
    val accent: Color,
    val categoryKind: String,
)

private fun addModeMeta(mode: String): AddModeMeta = when (mode) {
    "income" -> AddModeMeta("income", "Income", AccentBlue, "income")
    "lent" -> AddModeMeta("lent", "Give Loan", AccentPurple, "expense")
    "borrow" -> AddModeMeta("borrow", "Take Loan", Color(0xFF22C55E), "income")
    "receive" -> AddModeMeta("receive", "Get Back", Color(0xFF14B8A6), "income")
    "repay" -> AddModeMeta("repay", "Pay Back", AccentOrange, "expense")
    else -> AddModeMeta("expense", "Expense", AccentPink, "expense")
}

private fun forcedCategoryKeyForMode(mode: String): String = when (mode.trim().lowercase()) {
    "lent" -> "loan_given"
    "borrow" -> "loan_received"
    "receive" -> "due_collection"
    "repay" -> "loan_payment"
    else -> ""
}

@Composable
fun AddTransactionNativeScreen(
    repository: PocketNativeRepository,
    signedInUser: PocketNativeUser? = null,
    initialMode: String = "expense",
    initialEditKind: String = "",
    initialEditId: Int = 0,
    initialEditUrl: String = "",
    initialFallbackDate: String = "",
    initialFallbackAmount: Double = 0.0,
    initialFallbackNote: String = "",
    initialPrefillAmount: Double = 0.0,
    initialPrefillCategoryKey: String = "",
    initialPendingAmount: Double = 0.0,
    initialPendingOriginalAmount: Double = 0.0,
    initialPendingReminderId: String = "",
    initialPendingDueDate: String = "",
    initialPrefillPartyName: String = "",
    initialPrefillNote: String = "",
    restoredAmountText: String = "",
    showModeTabs: Boolean = true,
    onClose: () -> Unit = {},
    onOpenCategoryManager: (String) -> Unit = {},
    onAmountChanged: (String) -> Unit = {},
) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val preferencesStore = remember { PocketNativePreferencesStore(context) }
    var selectedMode by remember(initialMode) { mutableStateOf(initialMode) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    var successText by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var formPayload by remember { mutableStateOf<PocketNativeTransactionFormPayload?>(null) }
    var entryDate by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf(restoredAmountText) }
    var partyName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(0) }
    var selectedCategoryKey by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("CASH") }
    val pendingAmountHint = remember(initialPendingAmount, initialMode, initialEditId) {
        if (initialEditId <= 0 && initialMode in setOf("receive", "repay") && initialPendingAmount > 0.0) initialPendingAmount else 0.0
    }
    val pendingOriginalAmountHint = remember(initialPendingOriginalAmount, initialMode, initialEditId, pendingAmountHint) {
        if (initialEditId <= 0 && initialMode in setOf("receive", "repay")) {
            initialPendingOriginalAmount.coerceAtLeast(pendingAmountHint)
        } else {
            0.0
        }
    }
    val pendingReminderIdHint = remember(initialPendingReminderId, initialMode, initialEditId) {
        if (initialEditId <= 0 && initialMode in setOf("receive", "repay")) initialPendingReminderId.trim() else ""
    }
    val pendingDueDateHint = remember(initialPendingDueDate, initialMode, initialEditId) {
        if (initialEditId <= 0 && initialMode in setOf("receive", "repay")) initialPendingDueDate.trim() else ""
    }
    var lockedFeature by remember { mutableStateOf<PocketNativeFeatureGate?>(null) }
    var editKind by remember(initialEditKind) { mutableStateOf(initialEditKind) }
    var editId by remember(initialEditId) { mutableStateOf(initialEditId) }
    var editUrl by remember(initialEditUrl) { mutableStateOf(initialEditUrl) }
    var amountPadOpen by remember { mutableStateOf(false) }
    var textInputFocused by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showQuickToolsDialog by remember { mutableStateOf(false) }
    var showSmartSmsDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            capturedBitmap = null
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedImageUri = null
        }
    }

    fun syncAccountFromPaymentMethod(
        methodKey: String,
        data: PocketNativeTransactionFormPayload?,
    ) {
        val targetType = when (methodKey.trim().uppercase()) {
            "BKASH", "NAGAD", "ROCKET", "MFS" -> "MOBILE_WALLET"
            "BANK" -> "BANK"
            "CARD" -> "CARD"
            else -> "CASH"
        }
        val matching = data?.accounts?.firstOrNull { option ->
            val optionType = option.type.trim().uppercase()
            optionType == targetType || (targetType == "BANK" && optionType == "SAVINGS")
        } ?: return
        selectedAccountId = matching.id
    }

    fun syncPaymentMethodFromAccount(
        accountId: Int,
        data: PocketNativeTransactionFormPayload?,
    ) {
        val account = data?.accounts?.firstOrNull { it.id == accountId } ?: return
        val targetMethod = when (account.type.trim().uppercase()) {
            "MOBILE_WALLET" -> listOf("BKASH", "NAGAD", "ROCKET", "MFS")
            "BANK", "SAVINGS" -> listOf("BANK")
            "CARD" -> listOf("CARD")
            else -> listOf("CASH")
        }
        val matching = data.paymentMethods.firstOrNull { option ->
            option.key.trim().uppercase() in targetMethod
        } ?: return
        selectedPaymentMethod = matching.key
    }

    fun hydrateForm(data: PocketNativeTransactionFormPayload) {
        formPayload = data
        val localEditMode = data.isEditMode || (initialEditId > 0 && initialEditKind.isNotBlank())
        val fallbackDate = initialFallbackDate.trim()
        val resolvedDate = if (localEditMode && fallbackDate.isNotBlank()) {
            fallbackDate
        } else {
            data.defaultEntryDate
        }
        val resolvedAmount = when {
            localEditMode && data.initialAmount <= 0.0 && initialFallbackAmount > 0.0 -> initialFallbackAmount
            !localEditMode && pendingAmountHint <= 0.0 && data.initialAmount <= 0.0 && initialPrefillAmount > 0.0 -> initialPrefillAmount
            else -> data.initialAmount
        }
        val resolvedPartyName = when {
            data.initialPartyName.isNotBlank() -> data.initialPartyName
            !localEditMode && initialPrefillPartyName.isNotBlank() -> initialPrefillPartyName
            else -> ""
        }
        val resolvedNote = when {
            localEditMode && data.initialNote.isBlank() && initialFallbackNote.isNotBlank() -> initialFallbackNote
            data.initialNote.isNotBlank() -> data.initialNote
            !localEditMode && initialPrefillNote.isNotBlank() -> initialPrefillNote
            else -> ""
        }
        entryDate = resolvedDate
        amountText = if (restoredAmountText.isNotBlank()) {
            restoredAmountText
        } else if (resolvedAmount > 0.0) {
            ("%.2f".format(resolvedAmount)).trimEnd('0').trimEnd('.')
        } else ""
        amountPadOpen = !localEditMode
        textInputFocused = false
        partyName = resolvedPartyName
        note = resolvedNote
        dueDate = ""
        showImageSourceDialog = false
        selectedImageUri = null
        capturedBitmap = null
        selectedAccountId = data.defaultAccountId
        val forcedCategoryKey = forcedCategoryKeyForMode(selectedMode)
        selectedCategoryKey = if (!localEditMode && forcedCategoryKey.isNotBlank()) {
            forcedCategoryKey
        } else if (!localEditMode && initialPrefillCategoryKey.isNotBlank()) {
            initialPrefillCategoryKey
        } else {
            data.defaultCategoryKey
        }
        selectedPaymentMethod = data.defaultPaymentMethod
        editKind = if (localEditMode) {
            data.editEntryKind.ifBlank { initialEditKind }
        } else {
            ""
        }
        editId = if (localEditMode) {
            if (data.editEntryId > 0) data.editEntryId else initialEditId
        } else {
            0
        }
        editUrl = if (localEditMode) initialEditUrl else ""
        syncAccountFromPaymentMethod(data.defaultPaymentMethod, data)
    }

    LaunchedEffect(selectedMode, editKind, editId) {
        loading = true
        saving = false
        errorText = ""
        successText = ""
        showSuccessDialog = false
        val result = repository.transactionForm(selectedMode, editKind, editId)
        loading = false
        if (result.isSuccess) {
            val payload = result.getOrNull()?.payload
            if (payload != null) {
                hydrateForm(payload)
            } else {
                errorText = "Pocket Pro form is empty right now."
            }
        } else {
            errorText = result.exceptionOrNull()?.message ?: "Pocket Pro transaction form could not be loaded."
        }
    }

    LaunchedEffect(amountText) {
        onAmountChanged(amountText)
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 48.dp),
                color = AccentPink,
            )
        }
        return
    }

    val payload = formPayload
    if (payload == null) {
        NativeSimpleErrorCard(
            title = "Add transaction paused",
            body = errorText.ifBlank { "Pocket Pro form is not ready yet." },
            onBack = {},
        )
        return
    }

    val modeMeta = addModeMeta(selectedMode)
    val settlementCompactMode = selectedMode in setOf("receive", "repay") && pendingAmountHint > 0.0
    val keypadCompactMode = settlementCompactMode && amountPadOpen
    val compactLayout = amountPadOpen || textInputFocused || settlementCompactMode
    val selectedAccount = payload.accounts.firstOrNull { it.id == selectedAccountId } ?: payload.accounts.firstOrNull()
    val selectedCategory = payload.categories.firstOrNull { it.key == selectedCategoryKey } ?: payload.categories.firstOrNull()
    val pageTitle = modeMeta.title
    val receiptAttachGate = pocketNativeFeatureGate(signedInUser, "receipt_attach")

    if (showSuccessDialog && successText.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("Done", color = AccentGreen, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("Saved successfully", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(successText, color = TextSecondary)
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(22.dp),
        )
    }

    val keypadOverlayHeight = when {
        !amountPadOpen -> 0.dp
        settlementCompactMode -> 196.dp
        else -> 308.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 14.dp)
            .padding(horizontal = 16.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = if (keypadCompactMode) 4.dp else if (compactLayout) 8.dp else 12.dp,
                bottom = if (amountPadOpen) keypadOverlayHeight + 10.dp else 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(
                when {
                    keypadCompactMode -> 2.dp
                    compactLayout -> 3.dp
                    else -> 6.dp
                },
            ),
        ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    when {
                        keypadCompactMode -> 2.dp
                        compactLayout -> 4.dp
                        else -> 8.dp
                    },
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = onClose,
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.06f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (keypadCompactMode) 8.dp else 12.dp,
                                vertical = if (keypadCompactMode) 6.dp else 9.dp,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(if (keypadCompactMode) 16.dp else if (compactLayout) 18.dp else 20.dp),
                            )
                            Text(
                                text = "Back",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (keypadCompactMode) 11.sp else 12.sp,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        Text(
                            text = pageTitle,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (keypadCompactMode) 17.sp else if (compactLayout) 18.sp else 22.sp,
                        )
                    }
                    IconButton(
                        onClick = { showSmartSmsDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF00E5FF).copy(alpha = 0.12f),
                            contentColor = Color(0xFF00E5FF),
                        ),
                        modifier = Modifier.size(if (keypadCompactMode) 32.dp else if (compactLayout) 36.dp else 46.dp),
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = "Smart SMS Reader",
                            modifier = Modifier.size(if (keypadCompactMode) 16.dp else if (compactLayout) 18.dp else 22.dp),
                        )
                    }
                    IconButton(
                        onClick = { showQuickToolsDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.04f),
                            contentColor = TextPrimary,
                        ),
                        modifier = Modifier.size(if (keypadCompactMode) 32.dp else if (compactLayout) 36.dp else 46.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Tune,
                            contentDescription = "Tools",
                            modifier = Modifier.size(if (keypadCompactMode) 16.dp else if (compactLayout) 18.dp else 24.dp),
                        )
                    }
                }
                if (showModeTabs) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        NativeModeChip("income", "Income", selectedMode, AccentBlue, Modifier.weight(1f)) { selectedMode = it }
                        NativeModeChip("expense", "Expense", selectedMode, AccentPink, Modifier.weight(1f)) { selectedMode = it }
                        NativeModeChip("lent", "Give Loan", selectedMode, AccentPurple, Modifier.weight(1f)) { selectedMode = it }
                    }
                }
            }
        }
        if (successText.isNotBlank()) {
            item {
                NativeStatusCard(successText, AccentGreen)
            }
        }
        if (errorText.isNotBlank()) {
            item {
                NativeStatusCard(errorText, Color(0xFFFF5B6E))
            }
        }
        if (!settlementCompactMode) {
            item {
                NativeTapDateField(
                    title = "Date",
                    value = if (entryDate.isBlank()) "Tap to choose date" else entryDate,
                    icon = Icons.Rounded.CalendarMonth,
                    compactMode = compactLayout,
                    onPickDate = {
                        val parts = entryDate.split("-")
                        val calendar = Calendar.getInstance()
                        val year = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
                        val month = (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
                        val day = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
                        DatePickerDialog(
                            context,
                            { _, pickedYear, pickedMonth, pickedDay ->
                                entryDate = "%04d-%02d-%02d".format(pickedYear, pickedMonth + 1, pickedDay)
                            },
                            year,
                            month.coerceAtLeast(0),
                            day,
                        ).show()
                    },
                )
            }
        }
        val enteredSettlementAmount = amountText.toDoubleOrNull() ?: 0.0
        item {
            NativeInlineAmountField(
                title = when (selectedMode) {
                    "receive" -> "Receive Amount"
                    "repay" -> "Pay Amount"
                    else -> "Amount"
                },
                icon = Icons.Rounded.Payments,
                currencySymbol = payload.currencySymbol,
                amountText = amountText,
                keypadOpen = amountPadOpen,
                compactMode = compactLayout || settlementCompactMode,
                onToggleKeypad = { amountPadOpen = !amountPadOpen },
            )
        }
        if (pendingAmountHint > 0.0 && selectedMode in setOf("receive", "repay")) {
            item {
                NativePendingAmountHint(
                    title = if (selectedMode == "receive") "Remaining to get back" else "Remaining to pay back",
                    amount = pendingAmountHint,
                    currencySymbol = payload.currencySymbol,
                    compactMode = compactLayout || settlementCompactMode,
                    onUseFullAmount = {
                        amountText = ("%.2f".format(pendingAmountHint)).trimEnd('0').trimEnd('.')
                        amountPadOpen = true
                    },
                )
            }
            item {
                NativeLoanSettlementSummary(
                    currentDate = entryDate,
                    dueDate = pendingDueDateHint,
                    originalAmount = pendingOriginalAmountHint,
                    remainingAmount = pendingAmountHint,
                    enteredAmount = enteredSettlementAmount,
                    currencySymbol = payload.currencySymbol,
                    isReceive = selectedMode == "receive",
                    compactMode = true,
                )
            }
        }
        item {
            NativeSelectionField(
                title = "Account",
                currentLabel = selectedAccount?.name ?: "Select account",
                subtitle = selectedAccount?.let { "${it.typeLabel} • ${nativeSharedMoney(it.currentBalance, payload.currencySymbol)}" } ?: "",
                items = payload.accounts,
                itemLabel = { it.name },
                itemSubtitle = { "${it.typeLabel} • ${nativeSharedMoney(it.currentBalance, payload.currencySymbol)}" },
                compactMode = compactLayout || settlementCompactMode,
                onSelected = {
                    selectedAccountId = it.id
                    syncPaymentMethodFromAccount(it.id, payload)
                },
            )
        }
        item {
            NativeCategoryGridField(
                title = "Category",
                selectedLabel = selectedCategory?.label ?: "Select category",
                modeKind = modeMeta.categoryKind,
                items = payload.categories,
                compactMode = compactLayout || settlementCompactMode,
                onOpenCategoryManager = onOpenCategoryManager,
                onSelected = { selectedCategoryKey = it.key },
            )
        }
        item {
            NativeInlineTextFieldCard(
                title = if (selectedMode in setOf("lent", "borrow", "receive", "repay")) "Person" else "Note",
                value = partyName,
                placeholder = if (selectedMode in setOf("lent", "borrow", "receive", "repay")) "Who is involved?" else "Write a short note",
                icon = Icons.Rounded.EditNote,
                singleLine = true,
                compactMode = compactLayout || settlementCompactMode,
                onFocusChange = {
                    textInputFocused = it
                    if (it) amountPadOpen = false
                },
                onValueChange = { partyName = it },
            )
        }
        if (selectedMode in setOf("lent", "borrow")) {
            item {
                NativeTapDateField(
                    title = if (selectedMode == "lent") "Receiving Date" else "Pay Date",
                    value = if (dueDate.isBlank()) {
                        if (selectedMode == "lent") "Set receiving date" else "Set pay date"
                    } else dueDate,
                    icon = Icons.Rounded.CalendarMonth,
                    compactMode = compactLayout,
                    onPickDate = {
                        val parts = dueDate.split("-")
                        val calendar = Calendar.getInstance()
                        val year = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
                        val month = (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
                        val day = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
                        DatePickerDialog(
                            context,
                            { _, pickedYear, pickedMonth, pickedDay ->
                                dueDate = "%04d-%02d-%02d".format(pickedYear, pickedMonth + 1, pickedDay)
                            },
                            year,
                            month.coerceAtLeast(0),
                            day,
                        ).show()
                    },
                )
            }
            if (dueDate.isNotBlank()) {
                item {
                    Text(
                        text = if (selectedMode == "lent") {
                            "Pocket Pro will remind you until this money is received back."
                        } else {
                            "Pocket Pro will remind you until this loan is paid back."
                        },
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
        if (!keypadCompactMode) {
            item {
                NativeInlineTextFieldCard(
                    title = "Description",
                    value = note,
                    placeholder = "Optional details or reminder",
                    icon = Icons.Rounded.CameraAlt,
                    singleLine = true,
                    compactMode = compactLayout || settlementCompactMode,
                    extraLabel = when {
                        selectedImageUri != null -> "Gallery image selected"
                        capturedBitmap != null -> "Camera photo selected"
                        else -> ""
                    },
                    onFocusChange = {
                        textInputFocused = it
                        if (it) amountPadOpen = false
                    },
                    onIconClick = {
                        if (receiptAttachGate.granted) {
                            showImageSourceDialog = true
                        } else {
                            lockedFeature = receiptAttachGate
                        }
                    },
                    onValueChange = { note = it },
                )
            }
        } else if (note.isNotBlank()) {
            item {
                Text(
                    text = note,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        item {
            Button(
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                    if (parsedAmount <= 0.0) {
                        successText = ""
                        errorText = "Enter a valid amount first."
                        return@Button
                    }
                    if (selectedAccountId <= 0 || selectedCategoryKey.isBlank()) {
                        successText = ""
                        errorText = "Please choose account and category."
                        return@Button
                    }
                    if (selectedMode in setOf("lent", "borrow", "receive", "repay") && partyName.trim().isBlank()) {
                        successText = ""
                        errorText = "Add the person's name so this loan stays traceable."
                        return@Button
                    }
                    scope.launch {
                        saving = true
                        successText = ""
                        errorText = ""
                        val result = repository.saveTransaction(
                            mode = selectedMode,
                            entryDate = entryDate,
                            amount = parsedAmount,
                            accountId = selectedAccountId,
                            categoryKey = selectedCategoryKey,
                            paymentMethod = selectedPaymentMethod,
                            partyName = partyName,
                            note = note,
                            editKind = editKind,
                            editId = editId,
                            editUrl = editUrl,
                            dueDate = dueDate,
                        )
                        saving = false
                        if (result.isSuccess) {
                            if (editId <= 0) {
                                when (selectedMode) {
                                    "lent", "borrow" -> {
                                        preferencesStore.addLoanDueReminder(
                                            kind = selectedMode,
                                            counterparty = partyName,
                                            amount = parsedAmount,
                                            dueDate = dueDate,
                                            note = note,
                                        )
                                        if (dueDate.isNotBlank()) {
                                            PocketReminderScheduler.ensureDailyReminder(context)
                                        }
                                    }
                                    "receive", "repay" -> {
                                        val resolved = preferencesStore.resolveLoanDueReminder(
                                            reminderId = pendingReminderIdHint,
                                            settlementMode = selectedMode,
                                            counterparty = partyName,
                                            amount = parsedAmount,
                                            note = note,
                                        )
                                        if (resolved != null) {
                                            PocketReminderScheduler.ensureDailyReminder(context)
                                        }
                                    }
                                }
                            }
                            successText = result.getOrNull()?.message ?: "Saved."
                            showSuccessDialog = true
                            errorText = ""
                            amountText = ""
                            amountPadOpen = false
                            textInputFocused = false
                            partyName = ""
                            note = ""
                            dueDate = ""
                        } else {
                            successText = ""
                            errorText = result.exceptionOrNull()?.message ?: "Pocket Pro transaction could not be saved."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        when {
                            keypadCompactMode -> 38.dp
                            compactLayout || settlementCompactMode -> 42.dp
                            else -> 54.dp
                        },
                    ),
                enabled = !saving &&
                    (amountText.toDoubleOrNull()?.let { it.isFinite() && it > 0.0 } == true) &&
                    selectedAccountId > 0 && selectedCategoryKey.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = modeMeta.accent,
                    contentColor = TextPrimary,
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        "Save",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (keypadCompactMode) 15.sp else if (settlementCompactMode) 16.sp else 18.sp,
                    )
                }
            }
        }
            item { Spacer(modifier = Modifier.height(if (keypadCompactMode) 0.dp else 8.dp)) }
        }

        if (amountPadOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xFF0B111D)),
            ) {
                NativeAmountKeypad(
                    title = "Amount",
                    compactMode = settlementCompactMode,
                    onDigit = { digit ->
                        when (digit) {
                            "." -> if (!amountText.contains('.')) amountText += "."
                            else -> amountText += digit
                        }
                    },
                    onBackspace = {
                        if (amountText.isNotEmpty()) amountText = amountText.dropLast(1)
                    },
                    onClear = { amountText = "" },
                    onDone = { amountPadOpen = false },
                )
            }
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            title = { Text("Add menu photo", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = TextPrimary),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text("Select from gallery", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            cameraLauncher.launch(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = TextPrimary),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text("Open camera", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(24.dp),
        )
    }

    if (showQuickToolsDialog && formPayload != null) {
        QuickToolsDialog(
            mode = selectedMode,
            pendingAmount = pendingAmountHint,
            onDismiss = { showQuickToolsDialog = false },
            onUseFullRemaining = {
                amountText = ("%.2f".format(pendingAmountHint)).trimEnd('0').trimEnd('.')
                amountPadOpen = true
                showQuickToolsDialog = false
            },
            onClearAmount = {
                amountText = ""
                showQuickToolsDialog = false
            },
            onClearPersonOrNote = {
                partyName = ""
                note = ""
                showQuickToolsDialog = false
            },
            onOpenCategoryManager = {
                showQuickToolsDialog = false
                onOpenCategoryManager(modeMeta.categoryKind)
            },
            onSetToday = {
                val calendar = Calendar.getInstance()
                entryDate = "%04d-%02d-%02d".format(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                )
                showQuickToolsDialog = false
            },
            onToggleKeypad = {
                amountPadOpen = !amountPadOpen
                showQuickToolsDialog = false
            },
        )
    }

    if (showSmartSmsDialog) {
        PocketSmartSmsDialog(
            onDismiss = { showSmartSmsDialog = false },
            onTransactionParsed = { parsed ->
                selectedMode = parsed.mode
                amountText = if (parsed.amount % 1.0 == 0.0) parsed.amount.toLong().toString() else parsed.amount.toString()
                partyName = parsed.party
                note = parsed.summary
                if (parsed.categoryKey.isNotBlank()) {
                    selectedCategoryKey = parsed.categoryKey
                }
                if (parsed.paymentMethod.isNotBlank()) {
                    selectedPaymentMethod = parsed.paymentMethod
                }
                amountPadOpen = true
                showSmartSmsDialog = false
            }
        )
    }

    lockedFeature?.let { gate ->
        PocketNativeUpgradeDialog(
            gate = gate,
            onDismiss = { lockedFeature = null },
        )
    }
}

@Composable
private fun QuickToolsDialog(
    mode: String,
    pendingAmount: Double,
    onDismiss: () -> Unit,
    onUseFullRemaining: () -> Unit,
    onClearAmount: () -> Unit,
    onClearPersonOrNote: () -> Unit,
    onOpenCategoryManager: () -> Unit,
    onSetToday: () -> Unit,
    onToggleKeypad: () -> Unit,
) {
    val showUseFull = mode in setOf("receive", "repay") && pendingAmount > 0.0
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Quick tools", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (showUseFull) {
                    QuickToolRow(
                        label = if (mode == "receive") "Use full receive amount" else "Use full pay amount",
                        onClick = onUseFullRemaining,
                    )
                }
                QuickToolRow(label = "Set today as current date", onClick = onSetToday)
                QuickToolRow(label = "Open category manager", onClick = onOpenCategoryManager)
                QuickToolRow(label = "Clear amount", onClick = onClearAmount)
                QuickToolRow(label = "Clear person and note", onClick = onClearPersonOrNote)
                QuickToolRow(label = "Show or hide amount keypad", onClick = onToggleKeypad)
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(22.dp),
    )
}

@Composable
private fun QuickToolRow(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun NativeInteractiveFieldCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = AddScreenPanel),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AddScreenPanelBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(title, color = TextSecondary, fontSize = 11.sp)
                    Text(value, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }
                Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            content?.invoke()
        }
    }
}

@Composable
private fun NativeInlineAmountField(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currencySymbol: String,
    amountText: String,
    keypadOpen: Boolean,
    compactMode: Boolean,
    onToggleKeypad: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleKeypad() },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                title,
                color = TextSecondary,
                fontSize = if (compactMode) 13.sp else 15.sp,
                modifier = Modifier.width(if (compactMode) 74.dp else 88.dp),
            )
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(currencySymbol, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = if (compactMode) 15.sp else 16.sp)
                Text(
                    text = if (amountText.isBlank()) "0.00" else amountText,
                    color = if (amountText.isBlank()) TextSecondary else TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = if (compactMode) 17.sp else 18.sp,
                )
            }
            Icon(
                imageVector = if (keypadOpen) Icons.Rounded.Check else icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(if (compactMode) 18.dp else 20.dp),
            )
        }
        HorizontalDivider(color = AddScreenAccentLine, thickness = 2.dp)
    }
}

@Composable
private fun NativePendingAmountHint(
    title: String,
    amount: Double,
    currencySymbol: String,
    compactMode: Boolean = false,
    onUseFullAmount: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compactMode) 2.dp else 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text(title, color = TextSecondary, fontSize = if (compactMode) 10.sp else 13.sp)
                Text(
                    text = "$currencySymbol ${nativeSharedMoney(amount, "")}".trim(),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compactMode) 14.sp else 18.sp,
                )
                Text(
                    text = "Enter how much you received or paid now.",
                    color = TextSecondary,
                    fontSize = if (compactMode) 9.sp else 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(
                onClick = onUseFullAmount,
                contentPadding = PaddingValues(horizontal = if (compactMode) 4.dp else 8.dp, vertical = 0.dp),
            ) {
                Text(
                    "Use full",
                    color = AccentBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (compactMode) 12.sp else 14.sp,
                )
            }
        }
        HorizontalDivider(color = AddScreenAccentLine, thickness = 1.dp)
    }
}

@Composable
private fun NativeLoanSettlementSummary(
    currentDate: String,
    dueDate: String,
    originalAmount: Double,
    remainingAmount: Double,
    enteredAmount: Double,
    currencySymbol: String,
    isReceive: Boolean,
    compactMode: Boolean = false,
) {
    val nextRemaining = (remainingAmount - enteredAmount.coerceAtLeast(0.0)).coerceAtLeast(0.0)
    val amountLabel = if (isReceive) "Receive Amount" else "Pay Amount"
    val dueLabel = if (isReceive) "Receiving Date" else "Pay Date"
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compactMode) 2.dp else 8.dp),
    ) {
        NativeLoanSummaryRow(
            leftLabel = "Date",
            leftValue = currentDate.ifBlank { "-" },
            rightLabel = dueLabel,
            rightValue = dueDate.ifBlank { "-" },
            compactMode = compactMode,
        )
        NativeLoanSummaryRow(
            leftLabel = "Total",
            leftValue = nativeSharedMoney(originalAmount, currencySymbol),
            rightLabel = "Remaining",
            rightValue = nativeSharedMoney(remainingAmount, currencySymbol),
            compactMode = compactMode,
        )
        NativeLoanSummaryRow(
            leftLabel = amountLabel,
            leftValue = if (enteredAmount > 0.0) nativeSharedMoney(enteredAmount, currencySymbol) else "${currencySymbol}0.00",
            rightLabel = "After Save",
            rightValue = nativeSharedMoney(nextRemaining, currencySymbol),
            compactMode = compactMode,
        )
        HorizontalDivider(color = AddScreenAccentLine, thickness = 1.dp)
    }
}

@Composable
private fun NativeLoanSummaryRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    compactMode: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compactMode) 8.dp else 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(leftLabel, color = TextSecondary, fontSize = if (compactMode) 10.sp else 12.sp)
            Text(leftValue, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = if (compactMode) 11.sp else 14.sp)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(rightLabel, color = TextSecondary, fontSize = if (compactMode) 10.sp else 12.sp)
            Text(rightValue, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = if (compactMode) 11.sp else 14.sp)
        }
    }
}

@Composable
private fun NativeAmountKeypad(
    title: String,
    compactMode: Boolean = false,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit,
) {
    val rows = listOf(
        listOf("1", "2", "3", "back"),
        listOf("4", "5", "6", "clear"),
        listOf("7", "8", "9", "dot"),
        listOf("00", "0", "000", "done"),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AddScreenPanelBorder)
            .background(AddScreenPanel),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF8E8E92))
                .padding(horizontal = if (compactMode) 8.dp else 10.dp, vertical = if (compactMode) 4.dp else 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, color = Color(0xFF1A1A1A), fontSize = if (compactMode) 13.sp else 16.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(if (compactMode) 12.dp else 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color(0xFF1A1A1A), modifier = Modifier.size(if (compactMode) 16.dp else 20.dp))
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color(0xFF1A1A1A), modifier = Modifier.size(if (compactMode) 16.dp else 20.dp))
            }
        }
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    val isDone = key == "done"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(if (compactMode) 44.dp else 72.dp)
                            .clickable {
                                when (key) {
                                    "back" -> onBackspace()
                                    "clear" -> onClear()
                                    "dot" -> onDigit(".")
                                    "done" -> onDone()
                                    else -> onDigit(key)
                                }
                            },
                        color = if (isDone) Color(0xFFFF6363) else AddScreenPanel,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AddScreenPanelBorder),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            when (key) {
                                "back" -> Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(if (compactMode) 16.dp else 24.dp))
                                "clear" -> Text("-", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = if (compactMode) 18.sp else 24.sp)
                                "dot" -> Text(".", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = if (compactMode) 18.sp else 24.sp)
                                "done" -> Text("Done", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = if (compactMode) 13.sp else 18.sp)
                                else -> Text(key, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = if (compactMode) 16.sp else 22.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NativeTapDateField(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    compactMode: Boolean = false,
    onPickDate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPickDate() },
        verticalArrangement = Arrangement.spacedBy(if (compactMode) 6.dp else 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                color = TextSecondary,
                fontSize = if (compactMode) 14.sp else 15.sp,
                modifier = Modifier.width(if (compactMode) 74.dp else 88.dp),
            )
            Text(
                value,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = if (compactMode) 14.sp else 16.sp,
                modifier = Modifier.weight(1f),
            )
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        HorizontalDivider(color = AddScreenFlatDivider, thickness = 1.dp)
    }
}

@Composable
private fun NativeModeChip(
    key: String,
    label: String,
    selectedMode: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    val active = key == selectedMode
    Card(
        modifier = modifier.clickable { onSelect(key) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF15161B)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) accent else AddScreenPanelBorder),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                color = if (active) accent else TextPrimary,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun NativeStatusCard(
    message: String,
    accent: Color,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(22.dp),
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = accent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun <T> NativeSelectionField(
    title: String,
    currentLabel: String,
    subtitle: String,
    items: List<T>,
    itemLabel: (T) -> String,
    itemSubtitle: (T) -> String,
    compactGrid: Boolean = false,
    compactMode: Boolean = false,
    onSelected: (T) -> Unit,
) {
    var pickerOpen by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { pickerOpen = true },
        verticalArrangement = Arrangement.spacedBy(if (compactMode) 6.dp else 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                color = TextSecondary,
                fontSize = if (compactMode) 14.sp else 15.sp,
                modifier = Modifier.width(if (compactMode) 74.dp else 88.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (compactMode) 1.dp else 2.dp),
            ) {
                Text(currentLabel, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = if (compactMode) 14.sp else 16.sp)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, color = TextSecondary, fontSize = if (compactMode) 10.sp else 12.sp)
                }
            }
            Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        HorizontalDivider(color = AddScreenFlatDivider, thickness = 1.dp)
    }

    if (pickerOpen) {
        AlertDialog(
            onDismissRequest = { pickerOpen = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { pickerOpen = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                if (compactGrid) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        maxItemsInEachRow = 4,
                    ) {
                        items.forEach { item ->
                            NativeCompactOptionChip(
                                label = itemLabel(item),
                                subtitle = itemSubtitle(item),
                                onClick = {
                                    onSelected(item)
                                    pickerOpen = false
                                },
                            )
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(items.size) { index ->
                            val item = items[index]
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelected(item)
                                        pickerOpen = false
                                    }
                                    .padding(horizontal = 4.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(itemLabel(item), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                val extra = itemSubtitle(item)
                                if (extra.isNotBlank()) {
                                    Text(extra, color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun nativeFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SurfaceCard,
    unfocusedContainerColor = SurfaceCard,
    disabledContainerColor = SurfaceCard,
    focusedBorderColor = AccentPurple,
    unfocusedBorderColor = Color(0xFF293247),
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = TextSecondary,
    unfocusedLabelColor = TextSecondary,
)

@Composable
private fun NativeInlineTextFieldCard(
    title: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    singleLine: Boolean,
    compactMode: Boolean = false,
    extraLabel: String = "",
    trailingContent: (@Composable () -> Unit)? = null,
    onIconClick: (() -> Unit)? = null,
    onFocusChange: (Boolean) -> Unit = {},
    onValueChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compactMode) 4.dp else 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.width(if (compactMode) 74.dp else 88.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    title,
                    color = TextSecondary,
                    fontSize = if (compactMode) 14.sp else 15.sp,
                )
                trailingContent?.invoke()
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { onFocusChange(it.isFocused) },
                placeholder = { Text(placeholder, color = TextSecondary) },
                singleLine = singleLine,
                minLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                        onFocusChange(false)
                    },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = TextPrimary,
                ),
                shape = RoundedCornerShape(0.dp),
            )
            if (onIconClick != null) {
                IconButton(onClick = onIconClick, modifier = Modifier.size(if (compactMode) 24.dp else 28.dp)) {
                    Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(if (compactMode) 16.dp else 18.dp))
                }
            }
        }
        if (extraLabel.isNotBlank()) {
            Text(
                extraLabel,
                color = AccentGreen,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = if (compactMode) 74.dp else 88.dp),
            )
        }
        HorizontalDivider(color = AddScreenFlatDivider, thickness = 1.dp)
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun NativeCategoryGridField(
    title: String,
    selectedLabel: String,
    modeKind: String,
    items: List<PocketNativeTransactionCategoryOption>,
    compactMode: Boolean = false,
    onOpenCategoryManager: (String) -> Unit,
    onSelected: (PocketNativeTransactionCategoryOption) -> Unit,
) {
    var pickerOpen by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { pickerOpen = true },
        verticalArrangement = Arrangement.spacedBy(if (compactMode) 6.dp else 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                color = TextSecondary,
                fontSize = if (compactMode) 14.sp else 15.sp,
                modifier = Modifier.width(if (compactMode) 74.dp else 88.dp),
            )
            Text(
                text = selectedLabel,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = if (compactMode) 14.sp else 16.sp,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        HorizontalDivider(color = AddScreenFlatDivider, thickness = 1.dp)
    }

    if (pickerOpen) {
        AlertDialog(
            onDismissRequest = { pickerOpen = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { pickerOpen = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TextButton(
                                onClick = { onOpenCategoryManager(modeKind) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("New Category", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                            }
                            TextButton(
                                onClick = { onOpenCategoryManager(modeKind) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Edit", color = AccentPink, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items.chunked(3).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    rowItems.forEach { item ->
                                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                                            NativeCategoryChip(
                                                item = item,
                                                selected = item.label == selectedLabel,
                                                onClick = {
                                                    onSelected(item)
                                                    pickerOpen = false
                                                },
                                            )
                                        }
                                    }
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun NativeCategoryChip(
    item: PocketNativeTransactionCategoryOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) AccentPurple.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.035f))
            .border(
                width = 1.dp,
                color = if (selected) AccentPurple.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = item.label,
            color = if (selected) TextPrimary else TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NativeCompactOptionChip(
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardSoft),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                maxLines = 2,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                )
            }
        }
    }
}
