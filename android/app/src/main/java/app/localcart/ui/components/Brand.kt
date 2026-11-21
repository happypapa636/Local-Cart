package app.localcart.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.localcart.R
import app.localcart.ui.theme.Palette
import coil3.compose.AsyncImage
import kotlin.math.max
import kotlin.math.roundToInt

/** The LocalCart mark (awning over a chat bubble). `tile` draws the app icon square instead. */
@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = 40.dp, tile: Boolean = false) {
    Image(
        painter = painterResource(if (tile) R.drawable.brand_tile else R.drawable.brand_mark),
        contentDescription = null,
        modifier = modifier.size(size),
    )
}

/**
 * The scalloped edge of a shop awning, hanging below a [color] surface. It ties headers to the app
 * icon and the website, and is the only decoration the app uses.
 */
@Composable
fun AwningEdge(modifier: Modifier = Modifier, color: Color = Palette.Haldi, scallop: Dp = 22.dp) {
    Canvas(modifier.fillMaxWidth().height(scallop / 2)) {
        val count = max(1, (size.width / scallop.toPx()).roundToInt())
        val d = size.width / count
        for (i in 0 until count) {
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(i * d, -d / 2),
                size = Size(d, d),
            )
        }
    }
}

/**
 * Haldi-yellow header for tab roots, like a painted shop board, with the awning edge below.
 * Draws behind the status bar; [content] goes under the title (search fields, switches).
 */
@Composable
fun SignboardHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().background(Palette.Haldi).statusBarsPadding().padding(top = 12.dp, bottom = 10.dp)) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.displaySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() },
                    )
                    if (subtitle != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Palette.InkMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                trailing()
            }
            content()
        }
        AwningEdge()
    }
}

/** Shrinks slightly while pressed, so a tap is felt before the next screen arrives. */
fun Modifier.pressScale(interaction: InteractionSource, pressed: Float = 0.97f): Modifier = composed {
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressed else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** A soft light sweeping across placeholder blocks while content loads. */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer-x",
    )
    drawWithContent {
        drawContent()
        val w = size.width
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.55f), Color.Transparent),
                start = Offset(x * w - w / 2, 0f),
                end = Offset(x * w, size.height),
            ),
        )
    }
}

@Composable
fun SkeletonBlock(width: Dp, height: Dp, modifier: Modifier = Modifier) {
    Box(modifier.width(width).height(height).clip(RoundedCornerShape(6.dp)).background(Palette.Ink.copy(alpha = 0.07f)))
}

/** List-shaped loading placeholder: a thumbnail and two lines per row. */
@Composable
fun SkeletonList(modifier: Modifier = Modifier, rows: Int = 5, label: String = "Loading") {
    Column(modifier.fillMaxWidth().padding(top = 8.dp).shimmer().semantics { contentDescription = label }) {
        repeat(rows) { i ->
            Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(Palette.Ink.copy(alpha = 0.07f)))
                Spacer(Modifier.width(14.dp))
                Column {
                    SkeletonBlock(width = if (i % 2 == 0) 170.dp else 130.dp, height = 14.dp)
                    Spacer(Modifier.height(8.dp))
                    SkeletonBlock(width = if (i % 2 == 0) 110.dp else 150.dp, height = 11.dp)
                }
            }
        }
    }
}

/** How a shop category looks when there is no photo: an icon on one of the palette tints. */
data class CategoryLook(val icon: ImageVector, val background: Color, val tint: Color)

fun categoryLook(category: String?): CategoryLook = when (category) {
    "kirana" -> CategoryLook(Icons.Outlined.Storefront, Palette.HaldiTint, Palette.Ink)
    "fruits_vegetables" -> CategoryLook(Icons.Outlined.Eco, Palette.LeafTint, Palette.Leaf)
    "dairy" -> CategoryLook(Icons.Outlined.LocalDrink, Palette.Ink.copy(alpha = 0.08f), Palette.Ink)
    "bakery" -> CategoryLook(Icons.Outlined.BakeryDining, Palette.HaldiTint, Palette.Ink)
    "meat_fish" -> CategoryLook(Icons.Outlined.SetMeal, Palette.ChilliTint, Palette.Chilli)
    "pharmacy" -> CategoryLook(Icons.Outlined.LocalPharmacy, Palette.LeafTint, Palette.Leaf)
    "sweets_snacks" -> CategoryLook(Icons.Outlined.Cookie, Palette.ChilliTint, Palette.Chilli)
    "household" -> CategoryLook(Icons.Outlined.CleaningServices, Palette.Ink.copy(alpha = 0.08f), Palette.Ink)
    else -> CategoryLook(Icons.Outlined.ShoppingBasket, Palette.HaldiTint, Palette.Ink)
}

/** Shop photo, or the shop's category icon on its tint. */
@Composable
fun ShopThumb(photoUrl: String?, category: String?, modifier: Modifier = Modifier, size: Dp = 56.dp, onHaldi: Boolean = false) {
    val shape = RoundedCornerShape(size * 0.24f)
    val look = categoryLook(category)
    Box(
        modifier.size(size).clip(shape).background(if (photoUrl != null) Palette.InkTint else if (onHaldi) Palette.Sheet else look.background)
            .border(1.dp, Palette.Rule, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUrl != null) {
            AsyncImage(model = photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(look.icon, contentDescription = null, tint = look.tint, modifier = Modifier.size(size * 0.46f))
        }
    }
}
