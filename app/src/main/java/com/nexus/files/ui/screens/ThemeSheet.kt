package com.nexus.files.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.files.data.AppearanceSettings
import com.nexus.files.ui.components.IconSquircle
import com.nexus.files.ui.components.NexusSlider
import com.nexus.files.ui.components.NexusSwitch
import com.nexus.files.ui.components.RadioCardRow
import com.nexus.files.ui.components.SectionHeader
import com.nexus.files.ui.components.TickGlyph
import com.nexus.files.ui.components.enterStagger
import com.nexus.files.ui.theme.AccentPalette
import com.nexus.files.ui.theme.ContrastLevel
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily
import com.nexus.files.ui.theme.NexusMotion
import com.nexus.files.ui.theme.ThemeMode

/**
 * "Theme & Appearance" bottom sheet: theme modes, palette swatches, surface
 * contrast and monochromatic icons. Everything previews live and the big
 * Apply button commits the animated theme transition.
 */
@Composable
fun ThemeSheetContent(
    current: AppearanceSettings,
    onApply: (AppearanceSettings) -> Unit,
    onClose: () -> Unit,
) {
    var mode by remember { mutableStateOf(current.mode) }
    var palette by remember { mutableStateOf(current.palette) }
    var contrast by remember { mutableFloatStateOf(current.contrast.raw.toFloat()) }
    var mono by remember { mutableStateOf(current.monochromeIcons) }
    val extra = LocalNexusExtra.current

    Column(Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconSquircle(
                icon = Icons.Rounded.AutoAwesome,
                tint = extra.docs,
                background = extra.docs,
                size = 44.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Theme & Appearance",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Customize Material You dynamic styling",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = MonoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            androidx.compose.material3.TextButton(onClick = onClose) {
                Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp)
            }
        }

        Spacer(Modifier.height(6.dp))
        SectionHeader("Theme Mode")

        val modes = listOf(
            Triple(ThemeMode.DYNAMIC, Icons.Rounded.AutoAwesome, "Dynamic System" to "Wallpaper Matched"),
            Triple(ThemeMode.AMOLED, Icons.Rounded.DarkMode, "Pure AMOLED" to "#000000 True Black"),
            Triple(ThemeMode.MIDNIGHT, Icons.Rounded.Bedtime, "Cool Midnight" to "Deep Navy Tint"),
            Triple(ThemeMode.LIGHT, Icons.Rounded.LightMode, "Crisp Light" to "High Luminance"),
        )
        Column(
            Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            modes.forEachIndexed { index, (value, icon, labels) ->
                RadioCardRow(
                    selected = mode == value,
                    onSelect = { mode = value },
                    title = labels.first,
                    subtitle = labels.second,
                    modifier = Modifier.enterStagger(index, 40),
                    leading = {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (mode == value) extra.docs else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        SectionHeader("Color Palette Swatches")

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(extra.surfaceMid, RoundedCornerShape(20.dp))
                .border(1.dp, extra.hairline, RoundedCornerShape(20.dp))
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            AccentPalette.entries.forEachIndexed { index, entry ->
                Swatch(
                    palette = entry,
                    selected = palette == entry,
                    onSelect = { palette = entry },
                    index = index,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Surface contrast + monochrome icons cards
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(extra.surfaceMid, RoundedCornerShape(20.dp))
                    .border(1.dp, extra.hairline, RoundedCornerShape(20.dp))
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Contrast,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Surface Contrast",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = MonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        ContrastLevel.fromRaw(contrast.toInt()).label,
                        fontFamily = MonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W700,
                        color = extra.success,
                    )
                }
                Spacer(Modifier.height(10.dp))
                NexusSlider(value = contrast, onValueChange = { contrast = it })
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .background(extra.surfaceMid, RoundedCornerShape(20.dp))
                    .border(1.dp, extra.hairline, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconSquircle(
                    icon = Icons.Rounded.GridView,
                    tint = MaterialTheme.colorScheme.onSurface,
                    background = extra.surfaceHighest,
                    size = 36.dp,
                    corner = 12.dp,
                    iconSize = 18.dp,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Monochromatic Icons",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = MonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Tint folder icons to palette accent",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = MonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                NexusSwitch(checked = mono, onCheckedChange = { mono = it })
            }
        }

        Spacer(Modifier.height(18.dp))

        // Apply
        val scale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "applyPop",
        )
        Button(
            onClick = {
                onApply(
                    AppearanceSettings(
                        mode = mode,
                        palette = palette,
                        contrast = ContrastLevel.fromRaw(contrast.toInt()),
                        monochromeIcons = mono,
                    )
                )
                onClose()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = extra.docs,
                contentColor = Color(0xFF00363D),
            ),
        ) {
            TickGlyph(progress = 1f, color = Color(0xFF00363D), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                "Apply Appearance",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.W700,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun Swatch(
    palette: AccentPalette,
    selected: Boolean,
    onSelect: () -> Unit,
    index: Int,
) {
    val extra = LocalNexusExtra.current
    val pop by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = NexusMotion.bouncySpring,
        label = "swatchPop",
    )
    val ring by animateColorAsState(
        if (selected) Color.White else Color.Transparent,
        tween(NexusMotion.quickMs), label = "swatchRing",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.enterStagger(index + 4, 40),
    ) {
        Box(
            Modifier
                .size(48.dp)
                .graphicsLayer { scaleX = pop; scaleY = pop }
                .background(Color.Transparent, CircleShape)
                .border(2.dp, ring, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(palette.seed, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    TickGlyph(progress = 1f, color = Color(0xFF00363D), modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            palette.label,
            fontFamily = MonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.W700,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
