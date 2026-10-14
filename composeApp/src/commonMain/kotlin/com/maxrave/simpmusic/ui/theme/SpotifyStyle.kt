package com.maxrave.simpmusic.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Dark-surface palette modelled on the "dark, layered, green accent" look popular in music apps.
 *
 * Kept in its own file on purpose: this fork tracks upstream, so every look-and-feel decision that
 * can live outside upstream's files does. Upstream files only call into this one.
 *
 * No third-party brand assets are used — only generic colour values and layout conventions.
 */
object SpotifyStyle {
    /** Accent green. Also used as the seed so every seed-derived role shifts towards green. */
    val accent = Color(0xFF1ED760)

    val pageBackground = Color(0xFF121212)
    val raisedSurface = Color(0xFF1A1A1A)
    val cardSurface = Color(0xFF242424)
    val cardSurfaceHigh = Color(0xFF2E2E2E)
    val navBarBackground = Color(0xFF0B0B0B)

    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFB3B3B3)
    val textDisabled = Color(0xFF6A6A6A)

    /** Inactive lyric lines are dimmed white rather than a different hue. */
    val lyricInactive = Color(0x80FFFFFF)

    /** The translation row under the line being sung. */
    val lyricTranslationActive = Color(0xD9FFFFFF)

    // --- Elegance tokens: one spacing/shape scale so every screen breathes the same way. ---

    /** Side gutter of a full-width screen section. */
    val screenPadding = 16.dp

    /** Gap between two stacked sections (Home shelves, Library blocks). */
    val sectionGap = 24.dp

    /** Corner radii: thumbnails, cards, the lyrics card / mini player. */
    val radiusSmall = 6.dp
    val radiusCard = 12.dp
    val radiusLarge = 16.dp

    /** Hairline for dividers and card borders; barely there on purpose. */
    val hairline = Color(0x1FFFFFFF)

    /** Soft lift for floating surfaces (mini player). */
    val floatingShadow = 12.dp
}

/**
 * Re-skins the generated dark scheme: near-black page, stepped grey containers, green primary
 * with dark content on it. Text roles are pinned to white / soft grey for the high-contrast look.
 */
internal fun ColorScheme.withSpotifyDarkSurfaces(): ColorScheme =
    copy(
        primary = SpotifyStyle.accent,
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF14532D),
        onPrimaryContainer = Color(0xFFD7FBE3),
        background = SpotifyStyle.pageBackground,
        onBackground = SpotifyStyle.textPrimary,
        surface = SpotifyStyle.pageBackground,
        onSurface = SpotifyStyle.textPrimary,
        surfaceVariant = SpotifyStyle.cardSurface,
        onSurfaceVariant = SpotifyStyle.textSecondary,
        surfaceTint = Color.Transparent,
        surfaceBright = SpotifyStyle.cardSurfaceHigh,
        surfaceDim = Color(0xFF000000),
        surfaceContainerLowest = Color(0xFF000000),
        surfaceContainerLow = SpotifyStyle.raisedSurface,
        surfaceContainer = SpotifyStyle.cardSurface,
        surfaceContainerHigh = SpotifyStyle.cardSurfaceHigh,
        surfaceContainerHighest = Color(0xFF3A3A3A),
        outline = SpotifyStyle.textDisabled,
        outlineVariant = Color(0xFF333333),
    )
