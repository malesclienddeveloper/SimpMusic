package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.domain.data.model.library.LibraryCollectionPreview
import com.maxrave.domain.data.model.library.LibraryOverview
import com.maxrave.simpmusic.ui.icon.Download
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.GraphicEq
import com.maxrave.simpmusic.ui.icon.Groups
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.ui.theme.SpotifyStyle
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.downloaded
import simpmusic.composeapp.generated.resources.favorite
import simpmusic.composeapp.generated.resources.followed
import simpmusic.composeapp.generated.resources.library_artist_count
import simpmusic.composeapp.generated.resources.library_favorite_empty_hint
import simpmusic.composeapp.generated.resources.liked_songs_count
import simpmusic.composeapp.generated.resources.most_played

/**
 * FORK: the four collections at the top of Your library as quiet list rows — a 56dp cover (round
 * for artists), the title in white, the count in soft grey, nothing else. Replaces upstream's
 * pastel tile grid; the signature is unchanged so LibraryScreen did not need to move.
 *
 * With no artwork yet a row shows a flat gradient tile with a glyph instead, so a new user sees the
 * same rows waiting to be filled. [overview] is null until the first read lands, and the rows then
 * draw their titles alone.
 */
@Composable
fun LibraryTilingBox(
    navController: NavController,
    overview: LibraryOverview?,
) {
    val open = { type: LibraryDynamicPlaylistType ->
        navController.navigate(LibraryDynamicPlaylistDestination(type = type.toStringParams()))
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        val favoriteCount = overview?.favorite?.count
        CollectionRow(
            title = Res.string.favorite,
            subtitle =
                when {
                    overview == null -> null
                    favoriteCount == 0 -> stringResource(Res.string.library_favorite_empty_hint)
                    favoriteCount != null -> pluralStringResource(Res.plurals.liked_songs_count, favoriteCount, favoriteCount)
                    else -> null
                },
            preview = overview?.favorite,
            glyph = SimpIcons.Favorite,
            glyphColors = listOf(Color(0xFF1ED760), Color(0xFF0B6B30)),
            round = false,
        ) { open(LibraryDynamicPlaylistType.Favorite) }
        CollectionRow(
            title = Res.string.followed,
            subtitle =
                overview?.followed?.count?.let {
                    pluralStringResource(Res.plurals.library_artist_count, it, it)
                },
            preview = overview?.followed,
            glyph = SimpIcons.Groups,
            glyphColors = listOf(Color(0xFF3D3D3D), Color(0xFF1F1F1F)),
            round = true,
        ) { open(LibraryDynamicPlaylistType.Followed) }
        CollectionRow(
            title = Res.string.most_played,
            subtitle =
                overview?.mostPlayed?.count?.let {
                    pluralStringResource(Res.plurals.liked_songs_count, it, it)
                },
            preview = overview?.mostPlayed,
            glyph = SimpIcons.GraphicEq,
            glyphColors = listOf(Color(0xFF3D3D3D), Color(0xFF1F1F1F)),
            round = false,
        ) { open(LibraryDynamicPlaylistType.MostPlayed) }
        CollectionRow(
            title = Res.string.downloaded,
            subtitle =
                overview?.downloaded?.count?.let {
                    pluralStringResource(Res.plurals.liked_songs_count, it, it)
                },
            preview = overview?.downloaded,
            glyph = SimpIcons.Download,
            glyphColors = listOf(Color(0xFF3D3D3D), Color(0xFF1F1F1F)),
            round = false,
        ) { open(LibraryDynamicPlaylistType.Downloaded) }
    }
}

@Composable
private fun CollectionRow(
    title: StringResource,
    subtitle: String?,
    preview: LibraryCollectionPreview?,
    glyph: ImageVector,
    glyphColors: List<Color>,
    round: Boolean,
    onClick: () -> Unit,
) {
    val shape: Shape = if (round) CircleShape else RoundedCornerShape(SpotifyStyle.radiusSmall)
    val cover = preview?.thumbnails?.firstOrNull()
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = SpotifyStyle.screenPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (cover != null) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(cover)
                        .crossfade(true)
                        .build(),
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(shape),
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .clip(shape)
                        .background(Brush.linearGradient(glyphColors)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = glyph,
                    contentDescription = null,
                    tint = SpotifyStyle.textPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(title),
                style = typo().titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = SpotifyStyle.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = typo().bodySmall,
                    color = SpotifyStyle.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
