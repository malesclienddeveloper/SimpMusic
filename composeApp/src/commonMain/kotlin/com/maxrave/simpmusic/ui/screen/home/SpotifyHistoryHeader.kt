package com.maxrave.simpmusic.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.SpotifyStyle
import com.maxrave.simpmusic.ui.theme.typo

/**
 * FORK: the strip above the history list. A large title on the left, a round green play button on
 * the right that starts the newest song. Kept in its own file so RecentlySongsScreen only gains one call.
 */
@Composable
fun SpotifyHistoryHeader(
    title: String,
    enabled: Boolean,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = SpotifyStyle.screenPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = typo().titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
            color = SpotifyStyle.textPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        Box(
            modifier =
                Modifier
                    .size(52.dp)
                    .alpha(if (enabled) 1f else 0.4f)
                    .clip(CircleShape)
                    .background(SpotifyStyle.accent)
                    .clickable(enabled = enabled, onClick = onPlay),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = SimpIcons.PlayArrow,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.Black,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}
