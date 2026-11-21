package app.localcart.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.localcart.R

/*
 * LocalCart design tokens, taken from a kirana shopfront. Five colours: paper, ink, leaf, chilli and
 * haldi. Everything else is a tint of those (alpha over paper), never a new hue. Same values as the
 * website (server/public/styles.css) and the app icon.
 *
 * Paper  — the background, a cool off-white like a ruled notebook page.
 * Ink    — blue-black khata ink for text and strong surfaces.
 * Leaf   — dhaniya green: primary actions, open shops, confirmed states.
 * Chilli — mirchi red: errors, cancellations, unread counts, closed shops.
 * Haldi  — signboard yellow: the brand surface (headers, selection, the awning), never body text.
 */
object Palette {
    val Paper = Color(0xFFF4F6F3)
    val Ink = Color(0xFF16213E)
    val Leaf = Color(0xFF0E6B4A)
    val Chilli = Color(0xFFD2412F)
    val Haldi = Color(0xFFF6C445)

    // Tints derived from the five colours.
    val Sheet = Color(0xFFFFFFFF) // lightest tint of paper, for raised slips and inputs
    val InkMuted = Ink.copy(alpha = 0.66f)
    val InkFaint = Ink.copy(alpha = 0.45f)
    val Rule = Ink.copy(alpha = 0.12f)
    val RuleStrong = Ink.copy(alpha = 0.22f)
    // Tints are opaque (the colour at low strength over paper), so they stay clean on haldi and white too.
    val LeafTint = Color(0xFFDDE8E2) // leaf 10% over paper
    val ChilliTint = Color(0xFFF1E4DF) // chilli 10% over paper
    val InkTint = Color(0xFFE9EBEA) // ink 5% over paper
    val HaldiTint = Color(0xFFF5E6BB) // haldi 32% over paper
    val HaldiDeep = Color(0xFFE3AB22) // haldi in shadow: rules and pressed states on a haldi surface
}

/*
 * Anek Devanagari (Ek Type) covers Latin and Devanagari, so a Hindi voice-note transcript sits in the
 * same face as the rest of the UI. Its width axis gives the hierarchy: semi-condensed for headings and
 * money, normal width for reading.
 */
@OptIn(ExperimentalTextApi::class)
private fun anek(weight: Int, width: Float) = Font(
    resId = R.font.anek_devanagari,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight), FontVariation.width(width)),
)

val AnekText = FontFamily(
    anek(400, 100f),
    anek(500, 100f),
    anek(600, 100f),
    anek(700, 100f),
)

val AnekDisplay = FontFamily(
    anek(500, 87.5f),
    anek(600, 87.5f),
    anek(700, 87.5f),
)

// No colour here: text takes LocalContentColor, so buttons and dark surfaces get the right ink automatically.
private val Base = TextStyle(fontFamily = AnekText)

val LocalCartTypography = Typography(
    displaySmall = Base.copy(fontFamily = AnekDisplay, fontWeight = FontWeight(700), fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.4).sp),
    headlineSmall = Base.copy(fontFamily = AnekDisplay, fontWeight = FontWeight(650), fontSize = 26.sp, lineHeight = 30.sp, letterSpacing = (-0.2).sp),
    titleLarge = Base.copy(fontFamily = AnekDisplay, fontWeight = FontWeight(650), fontSize = 22.sp, lineHeight = 26.sp),
    titleMedium = Base.copy(fontWeight = FontWeight(600), fontSize = 17.sp, lineHeight = 22.sp),
    titleSmall = Base.copy(fontWeight = FontWeight(600), fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = Base.copy(fontWeight = FontWeight(400), fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = Base.copy(fontWeight = FontWeight(400), fontSize = 14.5.sp, lineHeight = 21.sp),
    bodySmall = Base.copy(fontWeight = FontWeight(400), fontSize = 13.sp, lineHeight = 18.sp, color = Palette.InkMuted),
    labelLarge = Base.copy(fontWeight = FontWeight(600), fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = Base.copy(fontWeight = FontWeight(500), fontSize = 13.sp, lineHeight = 16.sp),
    labelSmall = Base.copy(fontWeight = FontWeight(500), fontSize = 11.5.sp, lineHeight = 14.sp),
)

/** Money and quantities: condensed, tabular figures so columns of rupees line up on a bill. */
@Immutable
data class Figures(val large: TextStyle, val medium: TextStyle, val small: TextStyle)

val LocalFigures = staticCompositionLocalOf {
    Figures(
        large = Base.copy(fontFamily = AnekDisplay, fontWeight = FontWeight(700), fontSize = 28.sp, lineHeight = 32.sp, fontFeatureSettings = "tnum"),
        medium = Base.copy(fontFamily = AnekDisplay, fontWeight = FontWeight(600), fontSize = 17.sp, lineHeight = 22.sp, fontFeatureSettings = "tnum"),
        small = Base.copy(fontWeight = FontWeight(500), fontSize = 14.5.sp, lineHeight = 20.sp, fontFeatureSettings = "tnum"),
    )
}

/* Radius follows hierarchy: controls 6, slips and rows 10, sheets 18. */
val LocalCartShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

private val Scheme = lightColorScheme(
    primary = Palette.Leaf,
    onPrimary = Palette.Paper,
    primaryContainer = Palette.LeafTint,
    onPrimaryContainer = Palette.Leaf,
    secondary = Palette.Ink,
    onSecondary = Palette.Paper,
    secondaryContainer = Palette.InkTint,
    onSecondaryContainer = Palette.Ink,
    tertiary = Palette.Leaf,
    onTertiary = Palette.Paper,
    background = Palette.Paper,
    onBackground = Palette.Ink,
    surface = Palette.Paper,
    onSurface = Palette.Ink,
    surfaceVariant = Palette.InkTint,
    onSurfaceVariant = Palette.InkMuted,
    surfaceContainerLowest = Palette.Sheet,
    surfaceContainerLow = Palette.Sheet,
    surfaceContainer = Palette.Sheet,
    surfaceContainerHigh = Palette.Sheet,
    surfaceContainerHighest = Palette.Sheet,
    surfaceBright = Palette.Sheet,
    surfaceDim = Palette.Paper,
    surfaceTint = Color.Transparent,
    inverseSurface = Palette.Ink,
    inverseOnSurface = Palette.Paper,
    inversePrimary = Palette.Paper,
    error = Palette.Chilli,
    onError = Palette.Paper,
    errorContainer = Palette.ChilliTint,
    onErrorContainer = Palette.Chilli,
    outline = Palette.RuleStrong,
    outlineVariant = Palette.Rule,
    scrim = Palette.Ink.copy(alpha = 0.4f),
)

@Composable
fun LocalCartTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = LocalCartTypography,
        shapes = LocalCartShapes,
    ) {
        // Default content colour is ink (not pure black) everywhere, including screens without a Surface.
        CompositionLocalProvider(LocalContentColor provides Palette.Ink, content = content)
    }
}
