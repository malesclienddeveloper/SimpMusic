package com.maxrave.simpmusic.ui.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.common.Config
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.home.Content
import com.maxrave.domain.data.model.home.HomeItem
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.domain.utils.toTrack
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.HomeViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.play_all
import simpmusic.composeapp.generated.resources.quick_picks

/** How many songs the grid shows. Two columns, so keep it even. */
private const val QUICK_ACCESS_COUNT = 8

/**
 * The "quick picks" shelf as a two-column grid of compact cards (artwork flush left, title beside
 * it), the way streaming apps put a few one-tap items at the very top of Home.
 *
 * It takes the same inputs as [QuickPicks] and does exactly what a tap and a long-press did there —
 * start that song's radio, open the song's sheet — so only the presentation differs. A card list
 * built from Rows rather than a lazy grid, because it sits INSIDE Home's LazyColumn item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpotifyQuickAccessGrid(
    homeItem: HomeItem,
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel(),
) {
    var bottomSheetShow by remember { mutableStateOf(false) }
    var sheetTrack by remember { mutableStateOf<Track?>(null) }

    if (bottomSheetShow) {
        NowPlayingBottomSheet(
            onDismiss = { bottomSheetShow = false },
            song = sheetTrack?.toSongEntity(),
            navController = navController,
        )
    }

    Column(Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.quick_picks),
                style = typo().titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
            )
            if (homeItem.playAllEndpoint != null) {
                TextButton(
                    onClick = { viewModel.playAll(homeItem) },
                    colors =
                        ButtonDefaults
                            .textButtonColors()
                            .copy(contentColor = MaterialTheme.colorScheme.onSurface),
                ) {
                    Text(stringResource(Res.string.play_all), style = typo().bodySmall)
                }
            }
        }
        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            homeItem.contents
                .filterNotNull()
                .take(QUICK_ACCESS_COUNT)
                .chunked(2)
                .forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        pair.forEach { content ->
                            QuickAccessCard(
                                data = content,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val firstQueue: Track = content.toTrack()
                                    viewModel.setQueueData(
                                        QueueData.Data(
                                            listTracks = arrayListOf(firstQueue),
                                            firstPlayedTrack = firstQueue,
                                            playlistId = "RDAMVM${content.videoId}",
                                            playlistName = "\"${content.title}\" Radio",
                                            playlistType = PlaylistType.RADIO,
                                            continuation = null,
                                        ),
                                    )
                                    viewModel.loadMediaItem(
                                        firstQueue,
                                        type = Config.SONG_CLICK,
                                    )
                                },
                                onLongClick = {
                                    sheetTrack = content.toTrack()
                                    bottomSheetShow = true
                                },
                            )
                        }
                        // An odd count leaves the last row one card short; keep that card its width.
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickAccessCard(
    data: Content,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .height(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val url = data.thumbnails.lastOrNull()?.url
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalPlatformContext.current)
                    .data(url)
                    .crossfade(550)
                    .diskCacheKey(url)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
            placeholder = rememberHolderPainter(),
            error = rememberHolderPainter(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = data.title,
            style = typo().titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}
