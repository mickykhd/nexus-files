package com.nexus.files.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.files.data.StorageCategory
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.NexusMotion

/**
 * Animated sweep ring used by the Clean screen ("RECOVERABLE 23.4 GB").
 * The arc draws itself in and the center content fades up.
 */
@Composable
fun StorageDonut(
    progress: Float,
    modifier: Modifier = Modifier,
    ringWidth: Dp = 14.dp,
    trackColor: Color = LocalNexusExtra.current.surfaceHighest,
    sweepColors: List<Color> = listOf(
        LocalNexusExtra.current.docs,
        LocalNexusExtra.current.media,
        LocalNexusExtra.current.success,
    ),
    center: @Composable () -> Unit,
) {
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        sweep.snapTo(0f)
        sweep.animateTo(progress.coerceIn(0f, 1f), tween(1400, easing = NexusMotion.emphasized))
    }
    val contentAlpha by animateFloatAsState(1f, tween(700, delayMillis = 250), label = "donutContent")

    Box(modifier = modifier.size(190.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(190.dp)) {
            val stroke = Stroke(width = ringWidth.toPx(), cap = StrokeCap.Round)
            val inset = ringWidth.toPx() / 2
            val arcSize = Size(size.width - 2 * inset, size.height - 2 * inset)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            drawArc(
                brush = Brush.sweepGradient(sweepColors),
                startAngle = -90f,
                sweepAngle = 360f * sweep.value,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
        }
        Box(Modifier.graphicsLayer { alpha = contentAlpha }) { center() }
    }
}

/**
 * Segmented storage bar: each category grows into place with a stagger when the
 * stats arrive (Overview card).
 */
@Composable
fun SegmentedStorageBar(
    categories: List<StorageCategory>,
    totalBytes: Long,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
) {
    val extra = LocalNexusExtra.current
    val anim = remember(categories) { Animatable(0f) }
    LaunchedEffect(categories) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(1100, easing = NexusMotion.emphasized))
    }
    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(extra.surfaceHighest),
    ) {
        categories.forEachIndexed { index, cat ->
            val target = if (totalBytes <= 0) 0f else (cat.bytes.toFloat() / totalBytes)
            val appear by animateFloatAsState(
                targetValue = if (anim.value >= index * 0.18f + 0.05f) target else 0f,
                animationSpec = tween(700, easing = NexusMotion.emphasized),
                label = "seg$index",
            )
            val color = when (cat.colorKey) {
                com.nexus.files.data.CategoryColor.SYSTEM -> extra.docs
                com.nexus.files.data.CategoryColor.MEDIA -> extra.media
                com.nexus.files.data.CategoryColor.DOCS -> Color(0xFF2E9BF0)
                com.nexus.files.data.CategoryColor.SUCCESS -> extra.success
                com.nexus.files.data.CategoryColor.WARNING -> extra.warning
            }
            if (index > 0) Spacer(Modifier.width(2.dp))
            Box(
                Modifier
                    .fillMaxWidth(fraction = appear.coerceIn(0f, 1f))
                    .height(height)
                    .background(color, RoundedCornerShape(height / 2)),
            )
        }
    }
}

/** Thin animated progress line used inside cleaner cards. */
@Composable
fun ThinProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = LocalNexusExtra.current.success,
) {
    val fill by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600, easing = NexusMotion.emphasized),
        label = "thinProgress",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(LocalNexusExtra.current.surfaceHighest),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction = fill)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
    }
}
