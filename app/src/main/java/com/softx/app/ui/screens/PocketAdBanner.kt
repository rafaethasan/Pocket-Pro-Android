package com.softx.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.softx.app.BuildConfig

/**
 * A deliberately low-friction ad placement. It stays out of forms, app-lock,
 * backup, and other trust-sensitive flows. Production ads remain disabled
 * until a real AdMob banner unit is supplied through POCKET_ADMOB_BANNER_ID.
 */
@Composable
fun PocketAdBanner(modifier: Modifier = Modifier) {
    val adUnitId = BuildConfig.POCKET_ADMOB_BANNER_ID
    if (adUnitId.isBlank()) return

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        MobileAds.initialize(context) {}
    }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(320)
        val adView = remember(adUnitId, widthDp) {
            AdView(context).apply {
                this.adUnitId = adUnitId
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
                loadAd(AdRequest.Builder().build())
            }
        }
        DisposableEffect(adView) {
            onDispose { adView.destroy() }
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AndroidView(factory = { adView })
        }
    }
}
