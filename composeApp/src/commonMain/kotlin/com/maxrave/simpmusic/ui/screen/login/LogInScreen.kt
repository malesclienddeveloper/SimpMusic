package com.maxrave.simpmusic.ui.screen.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.maxrave.common.Config
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.expect.ui.PlatformBackHandler
import com.maxrave.simpmusic.expect.ui.PlatformWebView
import com.maxrave.simpmusic.expect.ui.createWebViewCookieManager
import com.maxrave.simpmusic.expect.ui.rememberGoogleAccountPicker
import com.maxrave.simpmusic.expect.ui.rememberWebViewState
import com.maxrave.simpmusic.extension.ultraThinBarStyle
import com.maxrave.simpmusic.ui.component.DevLogInBottomSheet
import com.maxrave.simpmusic.ui.component.DevLogInType
import com.maxrave.simpmusic.ui.component.RippleIconButton
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.Help
import com.maxrave.simpmusic.ui.icon.LogoDev
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.LogInViewModel
import com.maxrave.simpmusic.viewModel.SettingsViewModel
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.log_in
import simpmusic.composeapp.generated.resources.login_failed
import simpmusic.composeapp.generated.resources.login_success

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: LogInViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    hideBottomNavigation: () -> Unit,
    showBottomNavigation: () -> Unit,
) {
    val hazeState = rememberHazeState()
    val coroutineScope = rememberCoroutineScope()
    var devLoginSheet by rememberSaveable {
        mutableStateOf(false)
    }

    val state = rememberWebViewState()

    // FORK: optional step before Google's page — pick a device account and pass it as a login hint.
    // The cookie capture below is untouched, so the session is still read from the web login.
    var accountHint by rememberSaveable { mutableStateOf<String?>(null) }
    var chooserDone by rememberSaveable { mutableStateOf(false) }
    var isLoggingIn by rememberSaveable { mutableStateOf(false) }

    // FORK: Google refuses sign-in from embedded WebViews ("This browser or app may not be
    // secure"). The WebView still tells Google it is an app no matter what User-Agent we set
    // (Sec-CH-UA says "Android WebView", X-Requested-With carries the package name), so this
    // cannot be spoofed away. When Google's rejection page shows up, explain it and offer the
    // cookie import path instead of leaving the user on a dead end.
    var showLoginHelp by rememberSaveable { mutableStateOf(false) }
    var reloadWebView by rememberSaveable { mutableStateOf(0) }

    val pickDeviceAccount =
        rememberGoogleAccountPicker { email ->
            if (email != null) {
                accountHint = email
                chooserDone = true
            }
        }
    val showChooser = pickDeviceAccount != null && !chooserDone

    // When the user chooses an account hint, strip passive=true from Config.LOG_IN_URL
    // so Google doesn't attempt a silent SSO check (which aborts if cookies are empty).
    // Instead, Google will display the interactive sign-in flow for the selected account.
    val loginUrl =
        remember(accountHint) {
            val hint = accountHint
            val cleanBaseUrl =
                Config.LOG_IN_URL
                    .replace("passive=true&", "")
                    .replace("&passive=true", "")
                    .replace("passive=true", "")
            if (hint == null) {
                cleanBaseUrl
            } else {
                val enc = java.net.URLEncoder.encode(hint, "UTF-8")
                val sep = if (cleanBaseUrl.contains("?")) "&" else "?"
                "${cleanBaseUrl}${sep}Email=$enc&login_hint=$enc"
            }
        }

    // Handle back button on device: if currently in the web view after picking an account,
    // go back to the account chooser instead of exiting the entire login screen.
    PlatformBackHandler(enabled = pickDeviceAccount != null && chooserDone) {
        chooserDone = false
        accountHint = null
    }

    LaunchedEffect(state) {
        snapshotFlow { state.value }.collect {
            Logger.d(
                "LogInScreen",
                "WebViewState: ${
                    when (it) {
                        is com.maxrave.simpmusic.expect.ui.WebViewState.Finished -> "Finished"
                        is com.maxrave.simpmusic.expect.ui.WebViewState.Loading -> "Loading ${it.progress}%"
                    }
                }",
            )
        }
    }

    // Hide bottom navigation when entering this screen
    LaunchedEffect(Unit) {
        hideBottomNavigation()
        createWebViewCookieManager().removeAllCookies()
    }

    // Show bottom navigation when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            showBottomNavigation()
        }
    }

    Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState)) {
        Column {
            Spacer(
                Modifier
                    .size(
                        innerPadding.calculateTopPadding() + 64.dp,
                    ),
            )
            if (showChooser) {
                GoogleAccountChooser(
                    onPickDeviceAccount = { pickDeviceAccount?.invoke() },
                    onUseOther = { chooserDone = true },
                )
            } else {
                // WebView for YouTube Music login.
                // FORK: key() lets "Try again" rebuild the WebView after Google refuses a sign-in.
                key(reloadWebView) {
                    PlatformWebView(
                        state,
                        loginUrl,
                        aboveContent = {
                            if (devLoginSheet) {
                                DevLogInBottomSheet(
                                    onDismiss = {
                                        devLoginSheet = false
                                    },
                                    onDone = { cookie ->
                                        coroutineScope.launch {
                                            val success = settingsViewModel.addAccount(cleanPastedCookie(cookie))
                                            if (success) {
                                                viewModel.makeToast(getString(Res.string.login_success))
                                                navController.navigateUp()
                                            } else {
                                                viewModel.makeToast(getString(Res.string.login_failed))
                                            }
                                        }
                                    },
                                    type = DevLogInType.YouTube,
                                )
                            }
                        }
                    ) { url ->
                        Logger.d("LogInScreen", "Current URL: $url")
                        // FORK: Google answers embedded browsers with its "browser or app may not be
                        // secure" page — surface the help dialog instead of a dead end.
                        if (isGoogleSignInBlocked(url)) {
                            Logger.w("LogInScreen", "Google refused the WebView sign-in: $url")
                            showLoginHelp = true
                        }
                        // Google login redirects to music.youtube.com with query parameters
                        // such as ?pli=1 or ?authuser=0. Use startsWith so we detect the
                        // final landing URL regardless of trailing query string or hash.
                        if (url.startsWith("https://music.youtube.com") || url.startsWith("http://music.youtube.com")) {
                            if (isLoggingIn) return@PlatformWebView
                            val cookie = createWebViewCookieManager().getCookie(url)
                            // Make sure authentication cookies (SAPISID, __Secure-3PAPISID, LOGIN_INFO, or SID)
                            // are actually present before attempting to add the account.
                            val hasAuth = cookie.contains("SAPISID") ||
                                cookie.contains("__Secure-3PAPISID") ||
                                cookie.contains("LOGIN_INFO") ||
                                cookie.contains("SID=")
                            if (!hasAuth) {
                                Logger.d("LogInScreen", "On music.youtube.com but auth cookies are not yet ready")
                                return@PlatformWebView
                            }

                            isLoggingIn = true
                            coroutineScope.launch {
                                val success = settingsViewModel.addAccount(cookie)
                                if (success) {
                                    createWebViewCookieManager().removeAllCookies()
                                    viewModel.makeToast(getString(Res.string.login_success))
                                    navController.navigateUp()
                                } else {
                                    isLoggingIn = false
                                    viewModel.makeToast(getString(Res.string.login_failed))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top App Bar with haze effect
        TopAppBar(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .hazeBlur(HazeInput.Sources(hazeState), ultraThinBarStyle()),
            title = {
                Text(
                    text = stringResource(Res.string.log_in),
                    style = typo().titleMedium,
                )
            },
            navigationIcon = {
                Box(Modifier.padding(horizontal = 5.dp)) {
                    RippleIconButton(
                        SimpIcons.ArrowBackIosNew,
                        Modifier.size(32.dp),
                        true,
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        if (pickDeviceAccount != null && chooserDone) {
                            chooserDone = false
                            accountHint = null
                        } else {
                            navController.navigateUp()
                        }
                    }
                }
            },
            actions = {
                // FORK: reach the sign-in help even when Google's block page is not auto-detected.
                IconButton(
                    onClick = {
                        showLoginHelp = true
                    },
                ) {
                    Icon(
                        SimpIcons.Help,
                        "Login help",
                    )
                }
                IconButton(
                    onClick = {
                        devLoginSheet = true
                    },
                ) {
                    Icon(
                        SimpIcons.LogoDev,
                        "Developer Mode",
                    )
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
        )

        // FORK: Google blocked the in-app sign-in — explain it and offer the cookie import path.
        if (showLoginHelp) {
            LoginHelpSheet(
                onDismiss = { showLoginHelp = false },
                onImportCookie = {
                    showLoginHelp = false
                    devLoginSheet = true
                },
                onRetry = {
                    showLoginHelp = false
                    isLoggingIn = false
                    reloadWebView++
                },
            )
        }
    }
}

// FORK: does this URL belong to Google's "This browser or app may not be secure" rejection?
private fun isGoogleSignInBlocked(url: String): Boolean =
    url.contains("accounts.google.com") &&
        (url.contains("signin/rejected") || url.contains("rrk=") || url.contains("info/unknownerror"))

// FORK: accept what people actually paste — "Cookie: a=b; c=d", stray whitespace or line breaks.
private fun cleanPastedCookie(raw: String): String =
    raw
        .trim()
        .removePrefix("Cookie:")
        .removePrefix("cookie:")
        .trim()
        .replace("\n", "")
        .replace("\r", "")