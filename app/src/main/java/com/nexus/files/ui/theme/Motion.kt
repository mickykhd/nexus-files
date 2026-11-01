package com.nexus.files.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/** Motion tokens shared across the app — springy, expressive, playful. */
object NexusMotion {
    val emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val expressiveSpring = spring<Float>(
        dampingRatio = 0.6f,
        stiffness = Spring.StiffnessMediumLow,
    )
    val bouncySpring = spring<Float>(
        dampingRatio = 0.45f,
        stiffness = Spring.StiffnessMedium,
    )
    val snappySpring = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessHigh,
    )

    const val quickMs = 180
    const val baseMs = 280
    const val slowMs = 420
    const val themeMs = 520

    fun <T> fadeSlide() = tween<T>(baseMs, easing = emphasized)
    fun staggerDelay(index: Int, stepMs: Int = 38, max: Int = 8) =
        (index.coerceAtMost(max)) * stepMs
}
