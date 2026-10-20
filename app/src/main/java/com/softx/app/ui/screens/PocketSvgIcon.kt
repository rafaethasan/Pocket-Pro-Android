package com.softx.app.ui.screens

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

@Composable
fun PocketSvgIcon(
    assetName: String?,
    fallback: ImageVector,
    tint: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    if (assetName.isNullOrBlank()) {
        Icon(
            imageVector = fallback,
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier,
        )
        return
    }

    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components { add(SvgDecoder.Factory()) }
        .build()
    val model = ImageRequest.Builder(context)
        .data("file:///android_asset/pocket-icons/lucide/$assetName.svg")
        .crossfade(false)
        .build()

    SubcomposeAsyncImage(
        model = model,
        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint),
        loading = {
            Icon(
                imageVector = fallback,
                contentDescription = contentDescription,
                tint = tint,
                modifier = modifier,
            )
        },
        error = {
            Icon(
                imageVector = fallback,
                contentDescription = contentDescription,
                tint = tint,
                modifier = modifier,
            )
        },
    )
}
