package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxrave.simpmusic.ui.theme.SpotifyStyle
import com.maxrave.simpmusic.ui.theme.typo

/**
 * FORK: the lyrics card under the artwork — the line being sung in bold white, the next one dimmed
 * below it, on a rounded raised surface. Tapping it opens the full-screen lyrics.
 *
 * It fills whatever height its parent gives it (the player's existing gap under the artwork), so
 * the one-screen layout maths of the player does not change; the parent only chooses this card
 * over the plain one-line version when the gap is tall enough to hold two lines.
 */
@Composable
internal fun SpotifyLyricsCard(
    current: String,
    next: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(SpotifyStyle.radiusLarge))
                .background(SpotifyStyle.cardSurfaceHigh)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Crossfade(
            targetState = current to next,
            animationSpec = tween(durationMillis = 300),
            label = "spotifyLyricsCard",
        ) { (line, following) ->
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                LyricText(
                    text = line,
                    style = typo().titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = SpotifyStyle.textPrimary,
                    maxLines = 2,
                )
                if (following.isNotBlank()) {
                    LyricText(
                        text = following,
                        style = typo().bodyMedium,
                        color = SpotifyStyle.lyricInactive,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
