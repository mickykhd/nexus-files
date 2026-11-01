package com.nexus.files.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily
import com.nexus.files.ui.theme.NexusMotion

/**
 * Expressive switch: the floating thumb stretches (24 → 28dp) while pressed,
 * exactly like the Nexus design system spec.
 */
@Composable
fun NexusSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalNexusExtra.current.success,
) {
    val extra = LocalNexusExtra.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val thumbW by animateDpAsState(
        if (pressed) 28.dp else 24.dp,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "thumbW",
    )
    val trackColor by animateColorAsState(
        if (checked) tint.copy(alpha = 0.85f) else extra.surfaceHighest,
        tween(NexusMotion.quickMs), label = "track",
    )
    val thumbOffset by animateDpAsState(
        if (checked) 24.dp else 2.dp,
        spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "thumbX",
    )
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, NexusMotion.snappySpring, label = "switchScale")

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .width(52.dp)
            .height(30.dp)
            .background(trackColor, CircleShape)
            .tappable(interaction, enabled) { onCheckedChange(!checked) }
            .pointerInput(enabled) {
                detectHorizontalDragGestures { _, drag ->
                    if (enabled && ((drag > 24 && !checked) || (drag < -24 && checked))) onCheckedChange(!checked)
                }
            }
            .alpha(if (enabled) 1f else 0.5f),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = thumbOffset)
                .size(width = thumbW, height = 24.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, extra.hairline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(if (checked) tint else extra.outlineSoft, CircleShape),
            )
        }
    }
}

/** Rounded-md checkbox with a hand-drawn animated tick. */
@Composable
fun NexusCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalNexusExtra.current.success,
) {
    val extra = LocalNexusExtra.current
    val boxColor by animateColorAsState(
        if (checked) tint else Color.Transparent,
        tween(NexusMotion.quickMs), label = "cbBg",
    )
    val borderColor by animateColorAsState(
        if (checked) tint else extra.outlineSoft,
        tween(NexusMotion.quickMs), label = "cbBorder",
    )
    val tick by animateFloatAsState(
        if (checked) 1f else 0f,
        spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "tick",
    )
    val pop by animateFloatAsState(
        if (checked) 1f else 0.92f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cbScale",
    )
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = pop; scaleY = pop }
            .size(24.dp)
            .background(boxColor, RoundedCornerShape(7.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(7.dp))
            .tappable(interaction) { onCheckedChange(!checked) }
            .drawWithCache {
                val path = Path().apply {
                    moveTo(size.width * 0.25f, size.height * 0.52f)
                    lineTo(size.width * 0.44f, size.height * 0.70f)
                    lineTo(size.width * 0.76f, size.height * 0.32f)
                }
                onDrawBehind {
                    if (tick > 0f) {
                        drawPath(
                            path = path,
                            color = Color(0xFF002B14),
                            style = Stroke(
                                width = 2.6.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(28f * tick, 28f), 0f,
                                ),
                            ),
                        )
                    }
                }
            },
    )
}

/** Surface Contrast slider with a fat, playful thumb. */
@Composable
fun NexusSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalNexusExtra.current.success,
) {
    val extra = LocalNexusExtra.current
    val fill by animateFloatAsState(value / 100f, NexusMotion.snappySpring, label = "sliderFill")
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val thumbScale by animateFloatAsState(if (pressed) 1.35f else 1f, NexusMotion.bouncySpring, label = "thumb")

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onValueChange(((offset.x / size.width) * 100f).coerceIn(0f, 100f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    onValueChange(((change.position.x / size.width) * 100f).coerceIn(0f, 100f))
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        val travel = maxWidth - 18.dp
        val thumbOffset by animateDpAsState(travel * fill, tween(240, easing = NexusMotion.emphasized), label = "thumbX")
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(extra.surfaceHighest, CircleShape),
        )
        Box(
            Modifier
                .fillMaxWidth(fraction = fill.coerceIn(0.02f, 1f))
                .height(8.dp)
                .background(tint.copy(alpha = 0.85f), CircleShape),
        )
        Box(
            Modifier
                .offset(x = thumbOffset)
                .size(18.dp)
                .graphicsLayer { scaleX = thumbScale; scaleY = thumbScale }
                .background(Color.White, CircleShape)
                .border(3.dp, tint.copy(alpha = 0.9f), CircleShape),
        )
    }
}

/** Radio-style option row used in the Theme & Appearance sheet. */
@Composable
fun RadioCardRow(
    selected: Boolean,
    onSelect: () -> Unit,
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = LocalNexusExtra.current
    val bg by animateColorAsState(
        if (selected) extra.success.copy(alpha = 0.14f) else extra.surfaceMid,
        tween(NexusMotion.baseMs), label = "radioBg",
    )
    val border by animateColorAsState(
        if (selected) extra.docs else extra.hairline,
        tween(NexusMotion.baseMs), label = "radioBorder",
    )
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(18.dp))
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .pressScale(interaction)
            .tappable(interaction) { onSelect() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = MonoFontFamily,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = MonoFontFamily,
            )
        }
        SelectionDot(selected)
    }
}

/** Expanding circle + tick that pops in when an option is selected. */
@Composable
fun SelectionDot(selected: Boolean, modifier: Modifier = Modifier) {
    val extra = LocalNexusExtra.current
    val scale by animateFloatAsState(
        if (selected) 1f else 0.85f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "dot",
    )
    val bg by animateColorAsState(
        if (selected) extra.success else Color.Transparent,
        tween(NexusMotion.quickMs), label = "dotBg",
    )
    Box(
        modifier = modifier
            .size(24.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(bg, CircleShape)
            .border(1.5.dp, if (selected) extra.success else extra.outlineSoft, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        TickGlyph(
            progress = if (selected) 1f else 0f,
            color = Color(0xFF002B14),
            modifier = Modifier.size(14.dp),
        )
    }
}

/** Standalone animated tick glyph. */
@Composable
fun TickGlyph(progress: Float, color: Color, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(progress, NexusMotion.bouncySpring, label = "tickGlyph")
    Canvas(modifier) {
        if (animated <= 0f) return@Canvas
        val p = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.52f)
            lineTo(size.width * 0.43f, size.height * 0.72f)
            lineTo(size.width * 0.78f, size.height * 0.30f)
        }
        drawPath(
            path = p,
            color = color,
            style = Stroke(
                width = this.size.minDimension * 0.16f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(40f * animated, 40f), 0f,
                ),
            ),
        )
    }
}

/** Small dot used by storage legends and status chips. */
@Composable
fun LegendDot(color: Color, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, NexusMotion.bouncySpring) }
    Box(
        modifier
            .size(8.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .background(color, CircleShape),
    )
}
