package com.softx.app.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.TextPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PocketAvatarPreset(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val accent: Color,
)

val PocketAvatarPresets: List<PocketAvatarPreset> = listOf(
    PocketAvatarPreset("classic", "Classic", Icons.Rounded.Person, AccentPurple),
    PocketAvatarPreset("face", "Face", Icons.Rounded.Face, AccentBlue),
    PocketAvatarPreset("work", "Work", Icons.Rounded.Work, AccentOrange),
    PocketAvatarPreset("study", "Study", Icons.Rounded.School, AccentGreen),
    PocketAvatarPreset("money", "Money", Icons.Rounded.Savings, AccentBlue),
    PocketAvatarPreset("magic", "Magic", Icons.Rounded.AutoAwesome, AccentPink),
    PocketAvatarPreset("rocket", "Rocket", Icons.Rounded.RocketLaunch, AccentPurple),
    PocketAvatarPreset("care", "Care", Icons.Rounded.Favorite, AccentPink),
    PocketAvatarPreset("pet", "Pet", Icons.Rounded.Pets, AccentOrange),
)

fun pocketAvatarPresetFor(key: String): PocketAvatarPreset? = PocketAvatarPresets.firstOrNull { it.key == key.trim().lowercase() }

@Composable
fun PocketUserAvatar(
    user: PocketNativeUser?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preset = remember(user?.customAvatarKey) { pocketAvatarPresetFor(user?.customAvatarKey.orEmpty()) }
    var bitmap by remember(user?.customAvatarUri) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(user?.customAvatarUri) {
        bitmap = loadPocketAvatarBitmap(context, user?.customAvatarUri.orEmpty())
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background((preset?.accent ?: AccentPurple).copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            bitmap != null -> {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                )
            }

            preset != null -> {
                Icon(
                    imageVector = preset.icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(size * 0.48f),
                )
            }

            else -> {
                Text(
                    text = user?.avatarInitials?.ifBlank { "PP" } ?: "PP",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (size.value * 0.33f).sp,
                )
            }
        }
    }
}

private suspend fun loadPocketAvatarBitmap(
    context: Context,
    uriString: String,
): ImageBitmap? {
    if (uriString.isBlank()) return null
    return withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(Uri.parse(uriString))?.use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        }.getOrNull()
    }
}
