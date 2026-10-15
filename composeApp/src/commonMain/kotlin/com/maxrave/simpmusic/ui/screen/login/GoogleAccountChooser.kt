package com.maxrave.simpmusic.ui.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxrave.simpmusic.ui.theme.SpotifyStyle
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.login_choose_device_account
import simpmusic.composeapp.generated.resources.login_choose_other
import simpmusic.composeapp.generated.resources.login_choose_subtitle
import simpmusic.composeapp.generated.resources.login_choose_title

/**
 * FORK: the step before Google's sign-in page. One big green button opens the system account
 * chooser; the picked address is passed to the sign-in page as a hint. "Use another account" skips
 * straight to the plain page, so nothing about the cookie-based login itself changes.
 */
@Composable
fun GoogleAccountChooser(
    onPickDeviceAccount: () -> Unit,
    onUseOther: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.login_choose_title),
            style = typo().titleLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
            color = SpotifyStyle.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.login_choose_subtitle),
            style = typo().bodyMedium,
            color = SpotifyStyle.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onPickDeviceAccount,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = SpotifyStyle.accent,
                    contentColor = androidx.compose.ui.graphics.Color.Black,
                ),
        ) {
            Text(
                text = stringResource(Res.string.login_choose_device_account),
                style = typo().titleMedium.copy(fontWeight = FontWeight.Bold),
                color = LocalContentColor.current,
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onUseOther) {
            Text(
                text = stringResource(Res.string.login_choose_other),
                style = typo().bodyMedium,
                color = SpotifyStyle.textSecondary,
            )
        }
    }
}
