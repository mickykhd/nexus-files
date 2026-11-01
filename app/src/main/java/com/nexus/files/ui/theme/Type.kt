package com.nexus.files.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.nexus.files.R

/** Inter for body, metadata & labels. */
val InterFontFamily = FontFamily(
    Font(
        R.font.inter_var, weight = FontWeight.W400,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.inter_var, weight = FontWeight.W500,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.inter_var, weight = FontWeight.W600,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.inter_var, weight = FontWeight.W700,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

/** Roboto Flex for headlines & display. */
val DisplayFontFamily = FontFamily(
    Font(
        R.font.roboto_flex_var, weight = FontWeight.W500,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.roboto_flex_var, weight = FontWeight.W600,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.roboto_flex_var, weight = FontWeight.W700,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

/** Roboto Mono for file names, paths and tabular metadata (tnum). */
val MonoFontFamily = FontFamily(
    Font(R.font.roboto_mono_var, weight = FontWeight.W400),
    Font(R.font.roboto_mono_var, weight = FontWeight.W500),
    Font(R.font.roboto_mono_var, weight = FontWeight.W700),
)

private val tightLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Proportional,
    trim = LineHeightStyle.Trim.None,
)

/** Type scale from the Nexus Expressive Material design system. */
val NexusTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.W700,
        fontSize = 56.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp,
        lineHeightStyle = tightLineHeight,
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.W600,
        fontSize = 44.sp, lineHeight = 52.sp,
        lineHeightStyle = tightLineHeight,
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.W600,
        fontSize = 36.sp, lineHeight = 44.sp,
        lineHeightStyle = tightLineHeight,
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.W600,
        fontSize = 32.sp, lineHeight = 40.sp,
        lineHeightStyle = tightLineHeight,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.W600,
        fontSize = 24.sp, lineHeight = 32.sp,
        lineHeightStyle = tightLineHeight,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.W500,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.15.sp,
        lineHeightStyle = tightLineHeight,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.W600,
        fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp,
        lineHeightStyle = tightLineHeight,
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFontFamily, fontWeight = FontWeight.W600,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.15.sp,
        lineHeightStyle = tightLineHeight,
    ),
    titleSmall = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.W600,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
        lineHeightStyle = tightLineHeight,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.W400,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.W400,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.W400,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.W600,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.W600,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.W600,
        fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp,
    ),
)
