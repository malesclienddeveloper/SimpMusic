package com.maxrave.simpmusic.expect.ui

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberGoogleAccountPicker(onResult: (String?) -> Unit): (() -> Unit)? {
    val latest by rememberUpdatedState(onResult)
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val name =
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                } else {
                    null
                }
            latest(name)
        }
    return {
        // The chooser needs no permission; it lists the device's Google accounts and offers to add one.
        @Suppress("DEPRECATION")
        val intent =
            AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null,
            )
        try {
            launcher.launch(intent)
        } catch (e: Exception) {
            latest(null)
        }
    }
}
