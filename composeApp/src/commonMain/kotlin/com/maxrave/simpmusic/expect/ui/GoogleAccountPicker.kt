package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable

/**
 * FORK: lets the user pick one of the Google accounts already on the device.
 *
 * Returns a function that opens the system account chooser and reports the picked address through
 * [onResult] (null when cancelled), or null when this platform has no such chooser (Desktop).
 *
 * The address is only a login hint for the Google sign-in page: YouTube Music still needs the
 * session cookies that page produces, which no native account API can hand out.
 */
@Composable
expect fun rememberGoogleAccountPicker(onResult: (String?) -> Unit): (() -> Unit)?
