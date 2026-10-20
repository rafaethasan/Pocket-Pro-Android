package com.softx.app.ui.screens

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.softx.app.BuildConfig
import com.softx.app.data.PocketNativeApiFactory
import java.net.URI

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PocketWebAppScreen(
    startUrl: String = "${BuildConfig.POCKET_BASE_URL}pocket-pro/open",
    onPocketSessionReady: (() -> Unit)? = null,
    onRequireNativeAuth: () -> Unit,
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var sessionReadySent by remember(startUrl) { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PocketNativeApiFactory.syncSessionToWebView(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
            webView?.destroy()
            webView = null
        }
    }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1118)),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                WebView(viewContext).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    settings.setSupportZoom(false)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean {
                            val target = request?.url?.toString().orEmpty()
                            if (shouldReturnToNativeAuth(target)) {
                                onRequireNativeAuth()
                                return true
                            }
                            if (!isTrustedPocketUrl(target)) {
                                openExternalUrl(viewContext, target)
                                return true
                            }
                            return false
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            if (shouldReturnToNativeAuth(url.orEmpty())) {
                                onRequireNativeAuth()
                                return
                            }
                            if (!sessionReadySent && onPocketSessionReady != null && isPocketEntryReady(url.orEmpty())) {
                                PocketNativeApiFactory.syncSessionFromWebView(viewContext)
                                sessionReadySent = true
                                onPocketSessionReady()
                            }
                        }
                    }
                    setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                        enqueuePocketDownload(
                            context = viewContext,
                            url = url,
                            userAgent = userAgent.orEmpty(),
                            contentDisposition = contentDisposition.orEmpty(),
                            mimeType = mimeType.orEmpty(),
                        )
                    }
                    PocketNativeApiFactory.syncSessionToWebView(viewContext)
                    loadUrl(startUrl)
                    webView = this
                }
            },
            update = { currentWebView ->
                PocketNativeApiFactory.syncSessionToWebView(context)
                webView = currentWebView
            },
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33000000))
                    .statusBarsPadding(),
                contentAlignment = Alignment.TopCenter,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier,
                    color = Color.White,
                )
            }
        }
    }
}

private fun isPocketEntryReady(rawUrl: String): Boolean {
    val uri = runCatching { URI(rawUrl) }.getOrNull() ?: return false
    val path = (uri.path ?: "").trim()
    return path == "/pocket-pro/open" ||
        path == "/money" ||
        path == "/pocket/setup/currency" ||
        path == "/pocket/setup/start"
}

private fun isTrustedPocketUrl(rawUrl: String): Boolean {
    val target = runCatching { URI(rawUrl) }.getOrNull() ?: return false
    val trusted = runCatching { URI(BuildConfig.POCKET_BASE_URL) }.getOrNull() ?: return false
    return target.scheme.equals("https", ignoreCase = true) &&
        target.host.equals(trusted.host, ignoreCase = true)
}

private fun openExternalUrl(context: Context, rawUrl: String) {
    val uri = runCatching { Uri.parse(rawUrl) }.getOrNull() ?: return
    if (uri.scheme != "https" && uri.scheme != "http") return
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}

private fun shouldReturnToNativeAuth(rawUrl: String): Boolean {
    val uri = runCatching { URI(rawUrl) }.getOrNull() ?: return false
    val path = (uri.path ?: "").trim()
    return path == "/pocket/auth" ||
        path == "/pocket/welcome" ||
        path == "/pocket" ||
        path == "/pocket-pro" ||
        path == "/login" ||
        path == "/client-login"
}

private fun enqueuePocketDownload(
    context: Context,
    url: String,
    userAgent: String,
    contentDisposition: String,
    mimeType: String,
) {
    runCatching {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setMimeType(mimeType.ifBlank { "application/octet-stream" })
            val guessedName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            setTitle(guessedName)
            setDescription("Downloading from Pocket Pro")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            addRequestHeader("User-Agent", userAgent)
            val cookie = CookieManager.getInstance().getCookie(url)
            if (!cookie.isNullOrBlank()) {
                addRequestHeader("Cookie", cookie)
            }
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, guessedName)
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, "Download could not be started", Toast.LENGTH_SHORT).show()
    }
}
