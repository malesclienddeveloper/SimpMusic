package com.maxrave.simpmusic.ui.screen.login

// FORK: shown when Google refuses the in-app sign-in ("This browser or app may not be secure").
//
// Why this dialog exists: Google has blocked Google sign-in from embedded WebViews since 2021,
// and no User-Agent trick changes that — the WebView keeps telling Google it is an app
// (Sec-CH-UA answers "Android WebView", X-Requested-With carries the app package name), while
// Google reads both on the sign-in endpoint. The path that works is the one SimpMusic itself
// documents for Desktop: sign in from a real browser (SimpMusic Utils extension extracts the
// YouTube Music cookies) and import the cookies here.

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.login_google_blocked_close
import simpmusic.composeapp.generated.resources.login_google_blocked_howto
import simpmusic.composeapp.generated.resources.login_google_blocked_import
import simpmusic.composeapp.generated.resources.login_google_blocked_message
import simpmusic.composeapp.generated.resources.login_google_blocked_retry
import simpmusic.composeapp.generated.resources.login_google_blocked_title

@Composable
fun LoginHelpSheet(
    onDismiss: () -> Unit,
    onImportCookie: () -> Unit,
    onRetry: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.login_google_blocked_title),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.login_google_blocked_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(Res.string.login_google_blocked_howto),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onImportCookie) {
                Text(text = stringResource(Res.string.login_google_blocked_import))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onRetry) {
                    Text(text = stringResource(Res.string.login_google_blocked_retry))
                }
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(Res.string.login_google_blocked_close))
                }
            }
        },
    )
}