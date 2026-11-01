package com.nexus.files.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Accent palettes offered by the Theme & Appearance sheet. */
enum class AccentPalette(val label: String, val seed: Color) {
    CYAN("Cyan", Color(0xFF00E5FF)),
    LIME("Lime", Color(0xFF22EF7E)),
    AMETHYST("Amethyst", Color(0xFF9B7BF0)),
    CORAL("Coral", Color(0xFFFF8A3D)),
    OCEAN("Ocean", Color(0xFF2E9BF0)),
}

/** Theme modes offered by the Theme & Appearance sheet. */
enum class ThemeMode(val title: String, val subtitle: String) {
    DYNAMIC("Dynamic System", "Wallpaper Matched"),
    AMOLED("Pure AMOLED", "#000000 True Black"),
    MIDNIGHT("Cool Midnight", "Deep Navy Tint"),
    LIGHT("Crisp Light", "High Luminance"),
}

/**
 * Extra colors that live outside Material's tonal palette but are part of the
 * Nexus Expressive Material system (semantic storage indicators, surfaces, etc.).
 */
@Immutable
data class NexusExtraColors(
    val canvas: Color,
    val surfaceLowest: Color,
    val surfaceLow: Color,
    val surfaceMid: Color,
    val surfaceHigh: Color,
    val surfaceHighest: Color,
    val outlineSoft: Color,
    val success: Color,
    val onSuccessContainer: Color,
    val successContainer: Color,
    val warning: Color,
    val media: Color,
    val docs: Color,
    val system: Color,
    val danger: Color,
    val onDanger: Color,
    val dangerContainer: Color,
    val scrim: Color,
    val hairline: Color,
    val mono: Boolean,
)

val LocalNexusExtra = staticCompositionLocalOf {
    NexusExtraColors(
        canvas = Color(0xFF0F141C),
        surfaceLowest = Color(0xFF090E16),
        surfaceLow = Color(0xFF171C24),
        surfaceMid = Color(0xFF1B2028),
        surfaceHigh = Color(0xFF252A33),
        surfaceHighest = Color(0xFF30353E),
        outlineSoft = Color(0xFF3B494C),
        success = Color(0xFF00E676),
        successContainer = Color(0xFF00532A),
        onSuccessContainer = Color(0xFF7DFFB2),
        warning = Color(0xFFFFD600),
        media = Color(0xFF44DDC1),
        docs = Color(0xFF00E5FF),
        system = Color(0xFF00E5FF),
        danger = Color(0xFFFF5252),
        onDanger = Color(0xFFFFFFFF),
        dangerContainer = Color(0xFF93000A),
        scrim = Color(0x73000000),
        hairline = Color(0x14FFFFFF),
        mono = false,
    )
}

@Composable
fun isDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.LIGHT -> false
    else -> true || isSystemInDarkTheme()
}
