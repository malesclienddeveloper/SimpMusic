package com.maxrave.simpmusic.expect.ui

import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.maxrave.logger.Logger

actual fun createWebViewCookieManager(): WebViewCookieManager =
    object : WebViewCookieManager {
        override fun getCookie(url: String): String {
            val cookie = CookieManager.getInstance()
            return if (cookie.hasCookies()) {
                cookie.getCookie(url) ?: ""
            } else {
                ""
            }
        }

        override fun removeAllCookies() {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }
    }

@Composable
actual fun PlatformWebView(
    state: MutableState<WebViewState>,
    initUrl: String,
    aboveContent: @Composable (BoxScope.() -> Unit),
    onPageFinished: (String) -> Unit,
) {
    Box {
        AndroidView(
            factory = { ctx ->
                // Enable third-party cookies BEFORE creating the WebView.
                // Google login uses cross-domain cookies between accounts.google.com,
                // youtube.com, and music.youtube.com. Without this, the session cookie
                // set during the Google Account Picker flow is silently dropped when
                // redirecting back to music.youtube.com, causing login to fail.
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)

                WebView(ctx).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )

                    // Accept third-party cookies for this specific WebView instance.
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    webViewClient =
                        object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?,
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false
                                Logger.d("PlatformWebView", "shouldOverrideUrlLoading: $url")
                                // Handle intent:// schemes from Google/system to avoid net::ERR_UNKNOWN_URL_SCHEME
                                if (url.startsWith("intent://") || url.startsWith("intent:#Intent;")) {
                                    try {
                                        val intent = android.content.Intent.parseUri(url, android.content.Intent.URI_INTENT_SCHEME)
                                        if (intent != null) {
                                            val context = view?.context ?: return false
                                            val pm = context.packageManager
                                            val info = pm.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                                            if (info != null) {
                                                context.startActivity(intent)
                                                return true
                                            }
                                            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                            if (fallbackUrl != null) {
                                                view.loadUrl(fallbackUrl)
                                                return true
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Logger.e("PlatformWebView", "Failed to handle intent scheme: $url", e)
                                    }
                                    return true
                                }
                                return false
                            }

                            override fun onPageFinished(
                                view: WebView?,
                                url: String?,
                            ) {
                                // Flush cookies to persistent storage after every page
                                // load so the session survives the redirect chain.
                                cookieManager.flush()
                                url?.let {
                                    Logger.d("PlatformWebView", "onPageFinished: $it")
                                    onPageFinished(it)
                                }
                            }
                        }

                    // A WebChromeClient is required for JavaScript alerts/confirms and
                    // window.open, which Google's login page may use.
                    webChromeClient = object : WebChromeClient() {}

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    // Allow mixed content so HTTPS pages can load HTTP sub-resources
                    // during the redirect chain (some Google intermediary pages need this).
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    // Allow the WebView to open multiple windows if Google login
                    // tries to pop up a new window for account selection.
                    settings.setSupportMultipleWindows(false)
                    settings.javaScriptCanOpenWindowsAutomatically = true

                    // Strip WebView identifiers ("; wv" and "Version/4.0") so Google OAuth
                    // does not reject the WebView with 403 disallowed_useragent.
                    val defaultUa = settings.userAgentString ?: ""
                    val cleanUa = if (defaultUa.contains("; wv") || defaultUa.contains("Version/")) {
                        defaultUa.replace("; wv", "").replace(Regex("Version/\\d+\\.\\d+\\s*"), "")
                    } else if (defaultUa.isEmpty()) {
                        CHROME_MOBILE_USER_AGENT
                    } else {
                        defaultUa
                    }
                    settings.userAgentString = cleanUa

                    loadUrl(initUrl)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        aboveContent()
    }
}

private const val CHROME_MOBILE_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

@Composable
actual fun DiscordWebView(
    state: MutableState<WebViewState>,
    aboveContent: @Composable (BoxScope.() -> Unit),
    onLoginDone: (String) -> Unit
) {
    val url = "https://discord.com/login"
    Box {
        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(
                        webView: WebView,
                        url: String,
                    ): Boolean {
                        stopLoading()
                        if (url.endsWith("/app")) {
                            loadUrl(JS_SNIPPET)
                        }
                        return false
                    }
                }
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                if (android.os.Build.MANUFACTURER.equals(MOTOROLA, ignoreCase = true)) {
                    settings.userAgentString = SAMSUNG_USER_AGENT
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onJsAlert(
                        view: WebView,
                        url: String,
                        message: String,
                        result: JsResult,
                    ): Boolean {
                        onLoginDone(message)
                        return true
                    }
                }
                loadUrl(url)
            }
        })
        aboveContent()
    }
}

const val JS_SNIPPET =
    "javascript:(function()%7Bvar%20i%3Ddocument.createElement('iframe')%3Bdocument.body.appendChild(i)%3Balert(i.contentWindow.localStorage.token.slice(1,-1))%7D)()"
private const val MOTOROLA = "motorola"
private const val SAMSUNG_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14; SM-S921U; Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.363"