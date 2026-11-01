package com.nexus.files.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.files.data.Formats
import com.nexus.files.data.VaultEntry
import com.nexus.files.ui.components.IconSquircle
import com.nexus.files.ui.components.NexusSwitch
import com.nexus.files.ui.components.TickGlyph
import com.nexus.files.ui.components.enterStagger
import com.nexus.files.ui.components.pressScale
import com.nexus.files.ui.components.tappable
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily
import com.nexus.files.ui.theme.NexusMotion
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

/**
 * "File Inspector" sheet — deep inspection of a sealed vault artifact with
 * toggles and destructive actions.
 */
@Composable
fun FileInspectorContent(
    entry: VaultEntry,
    autoDestruct: Boolean,
    biometricReauth: Boolean,
    onAutoDestructChange: (Boolean) -> Unit,
    onBiometricReauthChange: (Boolean) -> Unit,
    onMoveToPublic: () -> Unit,
    onEncryptedLink: () -> Unit,
    onWipe: () -> Unit,
    onClose: () -> Unit,
) {
    val extra = LocalNexusExtra.current

    Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconSquircle(
                icon = Icons.Rounded.Shield,
                tint = extra.media,
                background = extra.media,
                size = 44.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "File Inspector",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(8.dp))
                    InspectorTag("LOCKED", extra.success)
                }
                Text(
                    "Inspection of selected vault artifact",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = MonoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            androidx.compose.material3.TextButton(onClick = onClose) {
                Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp)
            }
        }

        Spacer(Modifier.height(10.dp))

        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // File name
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(extra.surfaceHigh, RoundedCornerShape(16.dp))
                    .border(1.dp, extra.hairline, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "File Name",
                    fontFamily = MonoFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    entry.displayName,
                    fontFamily = MonoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(190.dp),
                )
            }

            // Size + cipher
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoPill(
                    title = "Enclave Size",
                    value = "${Formats.size(entry.bytes)} (${entry.bytes} B)",
                    modifier = Modifier.weight(1f).enterStagger(1),
                )
                InfoPill(
                    title = "Cipher Suite",
                    value = entry.cipherSuite,
                    valueColor = extra.success,
                    modifier = Modifier.weight(1f).enterStagger(2),
                )
            }

            // SHA-256
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(extra.surfaceHigh, RoundedCornerShape(16.dp))
                    .border(1.dp, extra.hairline, RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .enterStagger(3),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "SHA-256 Checksum",
                        fontFamily = MonoFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    CopyButton { /* copy checksum */ }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    entry.sha256.ifEmpty { "d838fa76e92c4b8a1102e34bf51a2e8812c3044910fb12cd02931" },
                    fontFamily = MonoFontFamily,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Hardware path
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(extra.surfaceHigh, RoundedCornerShape(16.dp))
                    .border(1.dp, extra.hairline, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .enterStagger(4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Hardware Path",
                    fontFamily = MonoFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    entry.hardwarePath,
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(190.dp),
                )
            }

            Spacer(Modifier.height(4.dp))

            // Toggles
            InspectorToggleRow(
                icon = Icons.Rounded.Schedule,
                title = "Auto-destruct timer",
                subtitle = "Purge after 48 hours inactivity",
                checked = autoDestruct,
                onChange = onAutoDestructChange,
                modifier = Modifier.enterStagger(5),
            )
            InspectorToggleRow(
                icon = Icons.Rounded.Fingerprint,
                title = "Biometric re-auth",
                subtitle = "Prompt biometrics before viewing",
                checked = biometricReauth,
                onChange = onBiometricReauthChange,
                modifier = Modifier.enterStagger(6),
            )

            Spacer(Modifier.height(8.dp))

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SheetActionButton(
                    icon = Icons.Rounded.LockOpen,
                    label = "Move to Public",
                    container = extra.surfaceHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    onClick = onMoveToPublic,
                )
                SheetActionButton(
                    icon = Icons.Rounded.Share,
                    label = "Encrypted Link",
                    container = extra.surfaceHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    onClick = onEncryptedLink,
                )
                SheetActionButton(
                    icon = Icons.Rounded.DeleteForever,
                    label = "Permanently Wipe",
                    container = extra.dangerContainer,
                    contentColor = if (extra.dangerContainer.luminanceCompat() > 0.5f) Color(0xFF410002) else Color(0xFFFFDAD6),
                    modifier = Modifier.weight(1f),
                    onClick = onWipe,
                )
            }
        }
    }
}

@Composable
private fun InspectorTag(text: String, color: Color) {
    Box(
        Modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text, fontFamily = MonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.W700, color = color)
    }
}

@Composable
private fun InfoPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = LocalNexusExtra.current.success,
) {
    val extra = LocalNexusExtra.current
    Column(
        modifier
            .background(extra.surfaceHigh, RoundedCornerShape(16.dp))
            .border(1.dp, extra.hairline, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(title, fontFamily = MonoFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            fontFamily = MonoFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.W700,
            color = valueColor,
        )
    }
}

@Composable
private fun CopyButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .background(LocalNexusExtra.current.surfaceHighest, RoundedCornerShape(10.dp))
            .pressScale(interaction)
            .tappable(interaction, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.ContentCopy,
            contentDescription = "Copy",
            tint = LocalNexusExtra.current.docs,
            modifier = Modifier.size(13.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text("Copy", fontFamily = MonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.W700, color = LocalNexusExtra.current.docs)
    }
}

@Composable
private fun InspectorToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = LocalNexusExtra.current
    val bg by animateColorAsState(
        if (checked) extra.surfaceHigh else extra.surfaceMid,
        tween(NexusMotion.baseMs), label = "toggleRow",
    )
    Row(
        modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(18.dp))
            .border(1.dp, extra.hairline, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(if (checked) extra.docs.copy(alpha = 0.18f) else extra.surfaceHighest, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (checked) extra.docs else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontFamily = MonoFontFamily, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, fontFamily = MonoFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        NexusSwitch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SheetActionButton(
    icon: ImageVector,
    label: String,
    container: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val extra = LocalNexusExtra.current
    val interaction = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = NexusMotion.bouncySpring, label = "actionBtn")
    Column(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(container, RoundedCornerShape(16.dp))
            .border(1.dp, extra.hairline, RoundedCornerShape(16.dp))
            .pressScale(interaction)
            .tappable(interaction, onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontFamily = MonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.W700,
            color = contentColor,
        )
    }
}

private fun Color.luminanceCompat(): Float = (red * 0.299f + green * 0.587f + blue * 0.114f)
