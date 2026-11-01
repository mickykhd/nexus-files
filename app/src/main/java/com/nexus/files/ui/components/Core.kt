package com.nexus.files.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily
import com.nexus.files.ui.theme.NexusMotion
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Squishy press feedback used on every tappable surface. */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.965f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = NexusMotion.snappySpring,
        label = "pressScale",
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Tappable surface wiring: ripple + shared interaction source for press scale. */
@Composable
fun Modifier.tappable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = this.clickable(
    interactionSource = interactionSource,
    indication = LocalIndication.current,
    enabled = enabled,
    onClick = onClick,
)

/** Cards fade + rise in with a stagger based on their position. */
@Composable
fun Modifier.enterStagger(index: Int, stepMs: Int = 45): Modifier {
    val alpha = remember { Animatable(0f) }
    val ty = remember { Animatable(18f) }
    LaunchedEffect(Unit) {
        val delay = NexusMotion.staggerDelay(index, stepMs).toLong()
        launch {
            kotlinx.coroutines.delay(delay)
            ty.animateTo(0f, NexusMotion.expressiveSpring)
        }
        kotlinx.coroutines.delay(delay)
        alpha.animateTo(1f, tween(NexusMotion.baseMs))
    }
    return this.graphicsLayer {
        this.alpha = alpha.value
        translationY = ty.value
    }
}

/** The signature tonal container: squircle, hairline edge, surface tier 1. */
@Composable
fun NexusCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    color: Color = LocalNexusExtra.current.surfaceLow,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    val hairline = LocalNexusExtra.current.hairline
    val interaction = remember { MutableInteractionSource() }
    val stroke = border ?: BorderStroke(1.dp, hairline)
    if (onClick != null) {
        Surface(
            modifier = modifier.pressScale(interaction),
            shape = shape,
            color = color,
            border = stroke,
            onClick = onClick,
            interactionSource = interaction,
        ) {
            Box(Modifier.padding(contentPadding)) { content() }
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = color,
            border = stroke,
        ) {
            Box(Modifier.padding(contentPadding)) { content() }
        }
    }
}

/** Squircle icon container housing file-type glyphs or thumbnails. */
@Composable
fun IconSquircle(
    icon: ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    corner: Dp = 15.dp,
    iconSize: Dp = 22.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(background.copy(alpha = 0.18f), RoundedCornerShape(corner))
            .border(1.dp, LocalNexusExtra.current.hairline, RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Uppercase mono section header: "FOLDERS (3)". */
@Composable
fun SectionHeader(
    title: String,
    trailing: String? = null,
    modifier: Modifier = Modifier,
    accent: Color = LocalNexusExtra.current.docs,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            fontFamily = MonoFontFamily,
            fontSize = 13.sp,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.W700,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.weight(1f))
        if (trailing != null) {
            Text(
                text = trailing,
                color = accent,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** Small pill badge (e.g. "PDF", "Internal", "AES-256"). */
@Composable
fun TagChip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalNexusExtra.current.docs,
    filled: Boolean = false,
) {
    val bg by animateColorAsState(
        if (filled) color.copy(alpha = 0.22f) else LocalNexusExtra.current.surfaceHigh,
        tween(NexusMotion.quickMs), label = "chipBg",
    )
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (filled) color.copy(alpha = 0.5f) else LocalNexusExtra.current.hairline,
                RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            fontFamily = MonoFontFamily,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.W700,
            letterSpacing = 0.6.sp,
            color = color,
            maxLines = 1,
        )
    }
}

/** Count-up number used for big storage readouts. */
@Composable
fun AnimatedCounter(
    value: Double,
    decimals: Int = 1,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayMedium,
    color: Color = LocalNexusExtra.current.docs,
    prefix: String = "",
    suffix: String = "",
    fontFamily: FontFamily = MonoFontFamily,
) {
    val animated by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(900, easing = NexusMotion.emphasized),
        label = "counter",
    )
    val text = if (decimals == 0) animated.roundToInt().toString()
    else String.format(java.util.Locale.ROOT, "%.${decimals}f", animated)
    Text(
        text = "$prefix$text$suffix",
        style = style,
        color = color,
        fontFamily = fontFamily,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = modifier,
    )
}

@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalNexusExtra.current.hairline),
    )
}

fun springScale(): androidx.compose.animation.core.SpringSpec<Float> =
    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)

/** Convenience column wrapper to avoid importing layout everywhere. */
@Composable
fun VStack(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier) { content() }
}
