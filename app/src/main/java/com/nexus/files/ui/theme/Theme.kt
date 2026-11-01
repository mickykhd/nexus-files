package com.nexus.files.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Contrast levels exposed by the Surface Contrast slider. */
enum class ContrastLevel(val label: String, val raw: Int) {
    SOFT("Soft (Subtle)", 30),
    BALANCED("Balanced (Relaxed)", 55),
    HIGH("High (Material 3 Expressive)", 85);

    companion object {
        fun fromRaw(value: Int): ContrastLevel =
            entries.minBy { kotlin.math.abs(it.raw - value) }
    }
}

private data class SurfaceRamp(
    val canvas: Color,
    val dim: Color,
    val lowest: Color,
    val low: Color,
    val mid: Color,
    val high: Color,
    val highest: Color,
    val bright: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
)

private fun mix(a: Color, b: Color, t: Float): Color = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = 1f,
)

private fun Color.lighten(t: Float) = mix(this, Color.White, t)
private fun Color.darken(t: Float) = mix(this, Color.Black, t)

/** Applies the Surface Contrast slider by pushing surface tiers apart. */
private fun SurfaceRamp.contrast(amount: Float): SurfaceRamp {
    val k = amount.coerceIn(0f, 1f)
    return copy(
        dim = dim.darken(0.06f * k),
        lowest = lowest.darken(0.10f * k),
        low = low.darken(0.05f * k),
        mid = mix(mid, bright, 0.10f * k),
        high = mix(high, bright, 0.18f * k),
        highest = mix(highest, bright, 0.24f * k),
        outline = mix(outline, Color.White, 0.18f * k),
        onSurface = mix(onSurface, Color.White, 0.08f * k),
        onSurfaceVariant = mix(onSurfaceVariant, Color.White, 0.10f * k),
    )
}

private fun midnightRamp(contrast: Float) = SurfaceRamp(
    canvas = Color(0xFF0F141C),
    dim = Color(0xFF0F141C),
    lowest = Color(0xFF090E16),
    low = Color(0xFF171C24),
    mid = Color(0xFF1B2028),
    high = Color(0xFF252A33),
    highest = Color(0xFF30353E),
    bright = Color(0xFF343942),
    onSurface = Color(0xFFDEE2EE),
    onSurfaceVariant = Color(0xFFBAC9CC),
    outline = Color(0xFF849396),
    outlineVariant = Color(0xFF3B494C),
    inverseSurface = Color(0xFFDEE2EE),
    inverseOnSurface = Color(0xFF2C3139),
).contrast(contrast)

private fun amoledRamp(contrast: Float) = SurfaceRamp(
    canvas = Color(0xFF000000),
    dim = Color(0xFF000000),
    lowest = Color(0xFF05070B),
    low = Color(0xFF0B0E14),
    mid = Color(0xFF121720),
    high = Color(0xFF1A212D),
    highest = Color(0xFF222B3A),
    bright = Color(0xFF2A3446),
    onSurface = Color(0xFFDEE2EE),
    onSurfaceVariant = Color(0xFFBAC9CC),
    outline = Color(0xFF849396),
    outlineVariant = Color(0xFF2E3A42),
    inverseSurface = Color(0xFFDEE2EE),
    inverseOnSurface = Color(0xFF2C3139),
).contrast(contrast)

private fun indigoRamp(contrast: Float) = SurfaceRamp(
    canvas = Color(0xFF0A0E17),
    dim = Color(0xFF0A0E17),
    lowest = Color(0xFF06090F),
    low = Color(0xFF0E1522),
    mid = Color(0xFF141C2C),
    high = Color(0xFF1B2536),
    highest = Color(0xFF232F44),
    bright = Color(0xFF2B3850),
    onSurface = Color(0xFFDCE3F2),
    onSurfaceVariant = Color(0xFFB6C2D8),
    outline = Color(0xFF7E8CA6),
    outlineVariant = Color(0xFF33415A),
    inverseSurface = Color(0xFFDCE3F2),
    inverseOnSurface = Color(0xFF242C3C),
).contrast(contrast)

private fun lightRamp(contrast: Float) = SurfaceRamp(
    canvas = Color(0xFFF6F8FC),
    dim = Color(0xFFF6F8FC),
    lowest = Color(0xFFFFFFFF),
    low = Color(0xFFEEF2F8),
    mid = Color(0xFFE2E8F0),
    high = Color(0xFFCBD5E1),
    highest = Color(0xFFBFCCDC),
    bright = Color(0xFFB4C3D6),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF41505B),
    outline = Color(0xFF6B7A88),
    outlineVariant = Color(0xFFC3CDD9),
    inverseSurface = Color(0xFF2C3139),
    inverseOnSurface = Color(0xFFEEF2F8),
).contrast(contrast)

/** Triads per accent palette: primary / secondary / tertiary seeds. */
private fun triad(palette: AccentPalette) = when (palette) {
    AccentPalette.CYAN -> Triple(Color(0xFF00E5FF), Color(0xFF44DDC1), Color(0xFF22EF7E))
    AccentPalette.LIME -> Triple(Color(0xFF22EF7E), Color(0xFFB6FF3C), Color(0xFF00E5FF))
    AccentPalette.AMETHYST -> Triple(Color(0xFF9B7BF0), Color(0xFFC7A6FF), Color(0xFF54D2F0))
    AccentPalette.CORAL -> Triple(Color(0xFFFF8A3D), Color(0xFFFFB74A), Color(0xFFFF5C7A))
    AccentPalette.OCEAN -> Triple(Color(0xFF2E9BF0), Color(0xFF7BD0FF), Color(0xFF44DDC1))
}

private data class AccentSet(
    val primary: Color, val onPrimary: Color, val primaryContainer: Color, val onPrimaryContainer: Color,
    val secondary: Color, val onSecondary: Color, val secondaryContainer: Color, val onSecondaryContainer: Color,
    val tertiary: Color, val onTertiary: Color, val tertiaryContainer: Color, val onTertiaryContainer: Color,
)

private fun accents(palette: AccentPalette, light: Boolean): AccentSet {
    val (p, s, t) = triad(palette)
    return if (light) {
        AccentSet(
            primary = p.darken(0.62f), onPrimary = Color.White,
            primaryContainer = p.lighten(0.62f), onPrimaryContainer = p.darken(0.62f),
            secondary = s.darken(0.55f), onSecondary = Color.White,
            secondaryContainer = s.lighten(0.60f), onSecondaryContainer = s.darken(0.55f),
            tertiary = t.darken(0.55f), onTertiary = Color.White,
            tertiaryContainer = t.lighten(0.60f), onTertiaryContainer = t.darken(0.55f),
        )
    } else {
        AccentSet(
            primary = p.lighten(0.22f), onPrimary = p.darken(0.78f),
            primaryContainer = p, onPrimaryContainer = p.darken(0.62f),
            secondary = s.lighten(0.15f), onSecondary = s.darken(0.75f),
            secondaryContainer = s, onSecondaryContainer = s.darken(0.65f),
            tertiary = t.lighten(0.12f), onTertiary = t.darken(0.75f),
            tertiaryContainer = t, onTertiaryContainer = t.darken(0.60f),
        )
    }
}

/** Static (un-animated) target scheme for a configuration. */
@Composable
fun nexusColorScheme(
    mode: ThemeMode,
    palette: AccentPalette,
    contrast: ContrastLevel,
): ColorScheme {
    val context = LocalContext.current
    val light = mode == ThemeMode.LIGHT
    val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val ramp = when {
        mode == ThemeMode.DYNAMIC && dynamicAvailable -> {
            val dyn = if (light) dynamicLightColorScheme(context) else dynamicDarkColorScheme(context)
            SurfaceRamp(
                canvas = dyn.background,
                dim = dyn.surfaceDim,
                lowest = dyn.surfaceContainerLowest,
                low = dyn.surfaceContainerLow,
                mid = dyn.surfaceContainer,
                high = dyn.surfaceContainerHigh,
                highest = dyn.surfaceContainerHighest,
                bright = dyn.surfaceBright,
                onSurface = dyn.onSurface,
                onSurfaceVariant = dyn.onSurfaceVariant,
                outline = dyn.outline,
                outlineVariant = dyn.outlineVariant,
                inverseSurface = dyn.inverseSurface,
                inverseOnSurface = dyn.inverseOnSurface,
            )
        }
        mode == ThemeMode.DYNAMIC -> midnightRamp(contrast.raw / 100f)
        mode == ThemeMode.AMOLED -> amoledRamp(contrast.raw / 100f)
        mode == ThemeMode.MIDNIGHT -> indigoRamp(contrast.raw / 100f)
        else -> lightRamp(contrast.raw / 100f)
    }
    val acc = accents(palette, light)
    val errorSet = if (light) {
        listOf(Color(0xFFBA1A1A), Color.White, Color(0xFFFFDAD6), Color(0xFF93000A))
    } else {
        listOf(Color(0xFFFFB4AB), Color(0xFF690005), Color(0xFF93000A), Color(0xFFFFDAD6))
    }

    return if (light) {
        lightColorScheme(
            primary = acc.primary,
            onPrimary = acc.onPrimary,
            primaryContainer = acc.primaryContainer,
            onPrimaryContainer = acc.onPrimaryContainer,
            secondary = acc.secondary,
            onSecondary = acc.onSecondary,
            secondaryContainer = acc.secondaryContainer,
            onSecondaryContainer = acc.onSecondaryContainer,
            tertiary = acc.tertiary,
            onTertiary = acc.onTertiary,
            tertiaryContainer = acc.tertiaryContainer,
            onTertiaryContainer = acc.onTertiaryContainer,
            error = errorSet[0], onError = errorSet[1],
            errorContainer = errorSet[2], onErrorContainer = errorSet[3],
            background = ramp.canvas, onBackground = ramp.onSurface,
            surface = ramp.canvas, onSurface = ramp.onSurface,
            surfaceVariant = ramp.mid, onSurfaceVariant = ramp.onSurfaceVariant,
            surfaceContainerLowest = ramp.lowest,
            surfaceContainerLow = ramp.low,
            surfaceContainer = ramp.mid,
            surfaceContainerHigh = ramp.high,
            surfaceContainerHighest = ramp.highest,
            surfaceBright = ramp.bright,
            surfaceDim = ramp.dim,
            outline = ramp.outline,
            outlineVariant = ramp.outlineVariant,
            inverseSurface = ramp.inverseSurface,
            inverseOnSurface = ramp.inverseOnSurface,
            inversePrimary = acc.primary.darken(0.4f),
            scrim = Color.Black,
        )
    } else {
        darkColorScheme(
            primary = acc.primary,
            onPrimary = acc.onPrimary,
            primaryContainer = acc.primaryContainer,
            onPrimaryContainer = acc.onPrimaryContainer,
            secondary = acc.secondary,
            onSecondary = acc.onSecondary,
            secondaryContainer = acc.secondaryContainer,
            onSecondaryContainer = acc.onSecondaryContainer,
            tertiary = acc.tertiary,
            onTertiary = acc.onTertiary,
            tertiaryContainer = acc.tertiaryContainer,
            onTertiaryContainer = acc.onTertiaryContainer,
            error = errorSet[0], onError = errorSet[1],
            errorContainer = errorSet[2], onErrorContainer = errorSet[3],
            background = ramp.canvas, onBackground = ramp.onSurface,
            surface = ramp.canvas, onSurface = ramp.onSurface,
            surfaceVariant = ramp.mid, onSurfaceVariant = ramp.onSurfaceVariant,
            surfaceContainerLowest = ramp.lowest,
            surfaceContainerLow = ramp.low,
            surfaceContainer = ramp.mid,
            surfaceContainerHigh = ramp.high,
            surfaceContainerHighest = ramp.highest,
            surfaceBright = ramp.bright,
            surfaceDim = ramp.dim,
            outline = ramp.outline,
            outlineVariant = ramp.outlineVariant,
            inverseSurface = ramp.inverseSurface,
            inverseOnSurface = ramp.inverseOnSurface,
            inversePrimary = acc.primary.darken(0.62f),
            scrim = Color.Black,
        )
    }
}

/** Semantic / storage-indicator colors derived from the ramp + palette. */
@Composable
fun nexusExtraColors(
    scheme: ColorScheme,
    palette: AccentPalette,
    light: Boolean,
    contrast: ContrastLevel,
): NexusExtraColors {
    val k = contrast.raw / 100f
    return if (light) {
        NexusExtraColors(
            canvas = scheme.background,
            surfaceLowest = scheme.surfaceContainerLowest,
            surfaceLow = scheme.surfaceContainerLow,
            surfaceMid = scheme.surfaceContainer,
            surfaceHigh = scheme.surfaceContainerHigh,
            surfaceHighest = scheme.surfaceContainerHighest,
            outlineSoft = scheme.outlineVariant,
            success = Color(0xFF00874A),
            successContainer = Color(0xFF9CF5C5),
            onSuccessContainer = Color(0xFF00391D),
            warning = Color(0xFF8A6D00),
            media = Color(0xFF00695C),
            docs = Color(0xFF006B7D),
            system = Color(0xFF006B7D),
            danger = Color(0xFFD32F2F),
            onDanger = Color.White,
            dangerContainer = Color(0xFFFFDAD6),
            scrim = Color(0x66000000),
            hairline = Color(0f, 0f, 0f, alpha = 0.05f + 0.05f * k),
            mono = false,
        )
    } else {
        NexusExtraColors(
            canvas = scheme.background,
            surfaceLowest = scheme.surfaceContainerLowest,
            surfaceLow = scheme.surfaceContainerLow,
            surfaceMid = scheme.surfaceContainer,
            surfaceHigh = scheme.surfaceContainerHigh,
            surfaceHighest = scheme.surfaceContainerHighest,
            outlineSoft = scheme.outlineVariant,
            success = Color(0xFF00E676),
            successContainer = Color(0xFF00331B),
            onSuccessContainer = Color(0xFF7DFFB2),
            warning = Color(0xFFFFD600),
            media = triad(palette).second,
            docs = triad(palette).first,
            system = triad(palette).first,
            danger = Color(0xFFFF5252),
            onDanger = Color(0xFF410002),
            dangerContainer = Color(0xFF93000A),
            scrim = Color(0x73000000),
            hairline = Color(0x14FFFFFF),
            mono = false,
        )
    }
}

private val NexusColorAnimSpec = tween<Color>(NexusMotion.themeMs, easing = NexusMotion.emphasized)

/** Animates any target color when the appearance changes. */
@Composable
private fun Color.anim(): Color =
    animateColorAsState(this, NexusColorAnimSpec, label = "nexusColor").value

/**
 * Nexus theme: Material 3 with Nexus Expressive extras. All colors animate when
 * the user applies a new appearance so the whole app cross-fades gracefully.
 */
@Composable
fun NexusTheme(
    mode: ThemeMode,
    palette: AccentPalette,
    contrast: ContrastLevel,
    monochromeIcons: Boolean,
    content: @Composable () -> Unit,
) {
    val target = nexusColorScheme(mode, palette, contrast)
    val light = mode == ThemeMode.LIGHT

    val animated = if (light) {
        lightColorScheme(
            primary = target.primary.anim(), onPrimary = target.onPrimary.anim(),
            primaryContainer = target.primaryContainer.anim(), onPrimaryContainer = target.onPrimaryContainer.anim(),
            secondary = target.secondary.anim(), onSecondary = target.onSecondary.anim(),
            secondaryContainer = target.secondaryContainer.anim(), onSecondaryContainer = target.onSecondaryContainer.anim(),
            tertiary = target.tertiary.anim(), onTertiary = target.onTertiary.anim(),
            tertiaryContainer = target.tertiaryContainer.anim(), onTertiaryContainer = target.onTertiaryContainer.anim(),
            error = target.error.anim(), onError = target.onError.anim(),
            errorContainer = target.errorContainer.anim(), onErrorContainer = target.onErrorContainer.anim(),
            background = target.background.anim(), onBackground = target.onBackground.anim(),
            surface = target.surface.anim(), onSurface = target.onSurface.anim(),
            surfaceVariant = target.surfaceVariant.anim(), onSurfaceVariant = target.onSurfaceVariant.anim(),
            surfaceContainerLowest = target.surfaceContainerLowest.anim(),
            surfaceContainerLow = target.surfaceContainerLow.anim(),
            surfaceContainer = target.surfaceContainer.anim(),
            surfaceContainerHigh = target.surfaceContainerHigh.anim(),
            surfaceContainerHighest = target.surfaceContainerHighest.anim(),
            surfaceBright = target.surfaceBright.anim(),
            surfaceDim = target.surfaceDim.anim(),
            outline = target.outline.anim(),
            outlineVariant = target.outlineVariant.anim(),
            inverseSurface = target.inverseSurface.anim(),
            inverseOnSurface = target.inverseOnSurface.anim(),
            inversePrimary = target.inversePrimary.anim(),
            scrim = target.scrim.anim(),
        )
    } else {
        darkColorScheme(
            primary = target.primary.anim(), onPrimary = target.onPrimary.anim(),
            primaryContainer = target.primaryContainer.anim(), onPrimaryContainer = target.onPrimaryContainer.anim(),
            secondary = target.secondary.anim(), onSecondary = target.onSecondary.anim(),
            secondaryContainer = target.secondaryContainer.anim(), onSecondaryContainer = target.onSecondaryContainer.anim(),
            tertiary = target.tertiary.anim(), onTertiary = target.onTertiary.anim(),
            tertiaryContainer = target.tertiaryContainer.anim(), onTertiaryContainer = target.onTertiaryContainer.anim(),
            error = target.error.anim(), onError = target.onError.anim(),
            errorContainer = target.errorContainer.anim(), onErrorContainer = target.onErrorContainer.anim(),
            background = target.background.anim(), onBackground = target.onBackground.anim(),
            surface = target.surface.anim(), onSurface = target.onSurface.anim(),
            surfaceVariant = target.surfaceVariant.anim(), onSurfaceVariant = target.onSurfaceVariant.anim(),
            surfaceContainerLowest = target.surfaceContainerLowest.anim(),
            surfaceContainerLow = target.surfaceContainerLow.anim(),
            surfaceContainer = target.surfaceContainer.anim(),
            surfaceContainerHigh = target.surfaceContainerHigh.anim(),
            surfaceContainerHighest = target.surfaceContainerHighest.anim(),
            surfaceBright = target.surfaceBright.anim(),
            surfaceDim = target.surfaceDim.anim(),
            outline = target.outline.anim(),
            outlineVariant = target.outlineVariant.anim(),
            inverseSurface = target.inverseSurface.anim(),
            inverseOnSurface = target.inverseOnSurface.anim(),
            inversePrimary = target.inversePrimary.anim(),
            scrim = target.scrim.anim(),
        )
    }

    val targetExtra = nexusExtraColors(target, palette, light, contrast)
    val extra = targetExtra.copy(
        canvas = targetExtra.canvas.anim(),
        surfaceLowest = targetExtra.surfaceLowest.anim(),
        surfaceLow = targetExtra.surfaceLow.anim(),
        surfaceMid = targetExtra.surfaceMid.anim(),
        surfaceHigh = targetExtra.surfaceHigh.anim(),
        surfaceHighest = targetExtra.surfaceHighest.anim(),
        outlineSoft = targetExtra.outlineSoft.anim(),
        success = targetExtra.success.anim(),
        successContainer = targetExtra.successContainer.anim(),
        onSuccessContainer = targetExtra.onSuccessContainer.anim(),
        warning = targetExtra.warning.anim(),
        media = targetExtra.media.anim(),
        docs = targetExtra.docs.anim(),
        system = targetExtra.system.anim(),
        danger = targetExtra.danger.anim(),
        onDanger = targetExtra.onDanger.anim(),
        dangerContainer = targetExtra.dangerContainer.anim(),
        scrim = targetExtra.scrim.anim(),
        hairline = targetExtra.hairline.anim(),
        mono = monochromeIcons,
    )

    CompositionLocalProvider(LocalNexusExtra provides extra) {
        MaterialTheme(
            colorScheme = animated,
            typography = NexusTypography,
            content = content,
        )
    }
}


