package com.softx.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeDashboardResponse
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.data.pocketAvatarInitialsFor
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

@Composable
fun ProfileNativeScreen(
    repository: PocketNativeRepository,
    preferencesStore: PocketNativePreferencesStore,
    signedInUser: PocketNativeUser?,
    onBack: (() -> Unit)? = null,
    onSignedOut: () -> Unit,
) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var signingOut by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("") }
    var dashboardResponse by remember { mutableStateOf<PocketNativeDashboardResponse?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loading = true
        errorText = ""
        val result = repository.dashboard()
        loading = false
        if (result.isSuccess) {
            dashboardResponse = result.getOrNull()
        } else {
            errorText = result.exceptionOrNull()?.message ?: "Profile could not be loaded."
        }
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

    val user = pocketNativePreferredFeatureUser(dashboardResponse?.user, signedInUser)
    val summary = dashboardResponse?.dashboard?.summary
    if (user == null || summary == null) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 18.dp),
            contentPadding = PaddingValues(bottom = 176.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(18.dp)) }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        "Profile is getting ready",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        errorText.ifBlank { "Profile information is not ready yet." },
                        color = TextSecondary,
                        lineHeight = 22.sp,
                    )
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
                }
            }
        }
        return
    }
    var liveUser by remember(
        user.userId,
        user.email,
        user.username,
        user.fullName,
        user.customAvatarKey,
        user.customAvatarUri,
    ) {
        mutableStateOf(user)
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        val updatedUser = liveUser.copy(
            customAvatarUri = uri.toString(),
            customAvatarKey = "",
            avatarInitials = pocketAvatarInitialsFor(liveUser.fullName, liveUser.avatarInitials),
        )
        preferencesStore.saveSignedInUser(updatedUser)
        liveUser = updatedUser
        statusText = "Profile photo updated."
        showAvatarDialog = false
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        contentPadding = PaddingValues(bottom = 176.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(modifier = Modifier.height(34.dp)) }
        if (onBack != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            onClick = onBack,
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceCardSoft,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text("Back", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "PRIVATE PROFILE",
                        color = AccentPurple.copy(alpha = 0.92f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Box(
                        modifier = Modifier.clickable { showAvatarDialog = true },
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        PocketUserAvatar(user = liveUser, size = 96.dp)
                        Box(
                            modifier = Modifier
                                .background(AccentPurple, CircleShape)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    Text(liveUser.fullName.ifBlank { "Pocket Pro User" }, color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    Text(liveUser.email.ifBlank { "Connected Pocket Pro account" }, color = TextSecondary)
                    Text(
                        liveUser.bio.ifBlank { "Private money manager, reminders, loans and installment tracking in one place." },
                        color = TextSecondary,
                        lineHeight = 21.sp,
                    )
                    ProfileMetaLine(
                        items = listOf(
                            "Free access",
                            liveUser.role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                            "Editable profile",
                            "Photo ready",
                        ),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        ProfileHeroAction(
                            title = "Edit profile",
                            accent = AccentBlue,
                            icon = Icons.Rounded.Badge,
                            modifier = Modifier.weight(1f),
                            onClick = { showEditDialog = true },
                        )
                        ProfileHeroAction(
                            title = "Photo & avatar",
                            accent = AccentPurple,
                            icon = Icons.Rounded.Image,
                            modifier = Modifier.weight(1f),
                            onClick = { showAvatarDialog = true },
                        )
                    }
                    Text(
                        text = statusText.ifBlank { "Tap your picture to upload a photo or choose an avatar." },
                        color = if (statusText.isBlank()) TextSecondary else AccentGreen,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = "Your profile stays saved on this device, even when you reopen Pocket Pro offline.",
                        color = TextSecondary.copy(alpha = 0.88f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProfileMetricCard("Accounts", summary.accounts.toString(), AccentBlue, Modifier.weight(1f))
                    ProfileMetricCard("Goals", "${summary.goalProgressPercent}%", AccentGreen, Modifier.weight(1f))
                    ProfileMetricCard("Budget", "${summary.budgetUsedPercent}%", AccentOrange, Modifier.weight(1f))
                }
            }

            item {
                ProfileSectionTitle("Account")
            }
            item {
                ProfileActionRow(
                    title = "Edit profile",
                    subtitle = "Update your display name and keep your profile personal.",
                    accent = AccentBlue,
                    icon = Icons.Rounded.Badge,
                    onClick = { showEditDialog = true },
                )
            }
            item {
                ProfileActionRow(
                    title = "Photo & avatar",
                    subtitle = "Upload a profile photo or pick a clean avatar style.",
                    accent = AccentPurple,
                    icon = Icons.Rounded.Image,
                    onClick = { showAvatarDialog = true },
                )
            }
            item {
                ProfileActionRow(
                    title = "Account",
                    subtitle = liveUser.username.ifBlank { liveUser.email },
                    accent = AccentPurple,
                    icon = Icons.Rounded.Person,
                    onClick = null,
                )
            }
            item {
                ProfileActionRow(
                    title = if (signingOut) "Signing out..." else "Sign out",
                    subtitle = "",
                    accent = AccentPink,
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    onClick = {
                        if (!signingOut) {
                            scope.launch {
                                signingOut = true
                                repository.logout()
                                signingOut = false
                                onSignedOut()
                            }
                        }
                    },
                    trailingContent = {
                        if (signingOut) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AccentPink,
                                strokeWidth = 2.dp,
                            )
                        }
                    },
                )
            }

        item { Spacer(modifier = Modifier.height(18.dp)) }
    }

    if (showEditDialog) {
        ProfileEditDialog(
            currentUser = liveUser,
            onDismiss = { showEditDialog = false },
            onSave = { newFullName, newBio ->
                val updatedUser = liveUser.copy(
                    fullName = newFullName,
                    bio = newBio,
                    avatarInitials = pocketAvatarInitialsFor(newFullName, liveUser.avatarInitials),
                )
                preferencesStore.saveSignedInUser(updatedUser)
                liveUser = updatedUser
                statusText = "Profile updated."
                showEditDialog = false
            },
        )
    }

    if (showAvatarDialog) {
        ProfileAvatarDialog(
            currentUser = liveUser,
            onDismiss = { showAvatarDialog = false },
            onUpload = { photoLauncher.launch(arrayOf("image/*")) },
            onChooseAvatar = { avatarKey ->
                val updatedUser = liveUser.copy(
                    customAvatarKey = avatarKey,
                    customAvatarUri = "",
                    avatarInitials = pocketAvatarInitialsFor(liveUser.fullName, liveUser.avatarInitials),
                )
                preferencesStore.saveSignedInUser(updatedUser)
                liveUser = updatedUser
                statusText = "Avatar updated."
                showAvatarDialog = false
            },
            onUseInitials = {
                val updatedUser = liveUser.copy(
                    customAvatarKey = "",
                    customAvatarUri = "",
                    avatarInitials = pocketAvatarInitialsFor(liveUser.fullName, liveUser.avatarInitials),
                )
                preferencesStore.saveSignedInUser(updatedUser)
                liveUser = updatedUser
                statusText = "Initials avatar restored."
                showAvatarDialog = false
            },
        )
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
}

@Composable
private fun ProfileMetricCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(title, color = TextSecondary)
        Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        HorizontalDivider(color = accent.copy(alpha = 0.20f))
    }
}

@Composable
private fun ProfileMiniBadge(
    title: String,
    accent: Color,
) {
    Surface(
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = title,
            color = accent,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun ProfileMetaLine(
    items: List<String>,
) {
    val cleanItems = items.map { it.trim() }.filter { it.isNotBlank() }
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cleanItems.forEachIndexed { index, item ->
            Text(
                text = item,
                color = when (index) {
                    0 -> AccentPurple
                    1 -> AccentOrange
                    2 -> AccentBlue
                    else -> AccentGreen
                },
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
            )
            if (index != cleanItems.lastIndex) {
                Text(
                    text = "|",
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ProfileHeroAction(
    title: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ProfileActionRow(
    title: String,
    subtitle: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)?,
    trailingContent: (@Composable (() -> Unit))? = null,
) {
    Column {
        Row(
            modifier = (if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, color = TextSecondary, lineHeight = 21.sp)
                }
            }
            trailingContent?.invoke()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TextSecondary.copy(alpha = 0.12f)),
        )
    }
}

@Composable
private fun ProfileEditDialog(
    currentUser: PocketNativeUser,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var fullName by remember(currentUser.fullName) {
        mutableStateOf(currentUser.fullName.ifBlank { "Pocket Pro User" })
    }
    var bio by remember(currentUser.bio) {
        mutableStateOf(currentUser.bio)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(fullName.trim(), bio.trim()) }) {
                Text("Save", color = AccentPurple, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        title = { Text("Edit profile", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("About") },
                    minLines = 2,
                    maxLines = 3,
                )
                Text(
                    text = currentUser.email.ifBlank { currentUser.username },
                    color = TextSecondary,
                    lineHeight = 20.sp,
                )
            }
        },
        containerColor = SurfaceCardSoft,
        shape = RoundedCornerShape(20.dp),
    )
}

@Composable
private fun ProfileAvatarDialog(
    currentUser: PocketNativeUser,
    onDismiss: () -> Unit,
    onUpload: () -> Unit,
    onChooseAvatar: (String) -> Unit,
    onUseInitials: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPurple, fontWeight = FontWeight.Bold)
            }
        },
        title = { Text("Profile photo & avatar", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ProfileAvatarAction(
                        title = "Upload photo",
                        icon = Icons.Rounded.AddAPhoto,
                        accent = AccentBlue,
                        modifier = Modifier.weight(1f),
                        onClick = onUpload,
                    )
                    ProfileAvatarAction(
                        title = "Use initials",
                        icon = Icons.Rounded.Person,
                        accent = AccentPurple,
                        modifier = Modifier.weight(1f),
                        onClick = onUseInitials,
                    )
                }
                Text(
                    "Choose a ready avatar style",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                PocketAvatarPresets.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        row.forEach { preset ->
                            val selected = currentUser.customAvatarKey == preset.key
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onChooseAvatar(preset.key) }
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                PocketUserAvatar(
                                    user = currentUser.copy(
                                        customAvatarKey = preset.key,
                                        customAvatarUri = "",
                                    ),
                                    size = 54.dp,
                                )
                                Text(
                                    text = preset.label,
                                    color = if (selected) preset.accent else TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                )
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp),
                                    color = if (selected) preset.accent.copy(alpha = 0.75f) else TextSecondary.copy(alpha = 0.12f),
                                )
                            }
                        }
                        repeat((3 - row.size).coerceAtLeast(0)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        containerColor = SurfaceCardSoft,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun ProfileAvatarAction(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
        )
    }
}
