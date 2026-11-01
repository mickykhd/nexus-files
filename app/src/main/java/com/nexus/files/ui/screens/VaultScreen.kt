package com.nexus.files.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.nexus.files.data.VaultCategory
import com.nexus.files.data.VaultEntry
import com.nexus.files.ui.components.IconSquircle
import com.nexus.files.ui.components.NexusCard
import com.nexus.files.ui.components.SectionHeader
import com.nexus.files.ui.components.TagChip
import com.nexus.files.ui.components.enterStagger
import com.nexus.files.ui.components.pressScale
import com.nexus.files.ui.components.tappable
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily

@Composable
fun VaultScreen(
    entries: List<VaultEntry>,
    categoryStats: Map<VaultCategory, Pair<Int, Long>>,
    unlocked: Boolean,
    failedTries: Int,
    onSelectEntry: (VaultEntry) -> Unit,
    onUnlock: () -> Unit,
    onCategory: (VaultCategory) -> Unit,
) {
    val extra = LocalNexusExtra.current

    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Title block
        item(key = "title") {
            Row(
                Modifier.padding(horizontal = 16.dp).enterStagger(0),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Private Safe & Vault",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Zero-knowledge storage enclave",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = MonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconSquircle(
                    icon = Icons.Rounded.Shield,
                    tint = extra.success,
                    background = extra.success,
                    size = 44.dp,
                )
            }
        }

        // Biometrics card
        item(key = "biometrics") {
            NexusCard(
                modifier = Modifier.padding(horizontal = 16.dp).enterStagger(1),
                contentPadding = PaddingValues(16.dp),
                onClick = if (unlocked) null else onUnlock,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .background(
                                if (unlocked) extra.success.copy(alpha = 0.16f) else extra.surfaceHighest,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (unlocked) Icons.Rounded.LockOpen else Icons.Rounded.Fingerprint,
                            contentDescription = null,
                            tint = if (unlocked) extra.success else extra.docs,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (unlocked) "Vault Unlocked" else "Biometrics Engaged",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                fontFamily = MonoFontFamily,
                                fontWeight = FontWeight.W700,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            Spacer(Modifier.width(8.dp))
                            TagChip("Knox Protected", color = extra.docs, filled = true)
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            if (unlocked) "Enclave mounted · AES-256-GCM active"
                            else "Locked with Fingerprint / PIN • $failedTries failed tries left",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = MonoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = if (unlocked) extra.success else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        item(key = "cat-header") {
            SectionHeader("Vault Categories", trailing = "${categoryStats.size} Partitions")
        }

        // 2x2 category grid
        item(key = "cats") {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryTile(VaultCategory.DOCUMENTS, categoryStats, Modifier.weight(1f), Icons.Rounded.PictureAsPdf, extra.docs, onCategory, 0)
                    CategoryTile(VaultCategory.MEDIA, categoryStats, Modifier.weight(1f), Icons.Rounded.VisibilityOff, Color(0xFFFFB74A), onCategory, 1)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryTile(VaultCategory.KEYS, categoryStats, Modifier.weight(1f), Icons.Rounded.Key, extra.success, onCategory, 2)
                    CategoryTile(VaultCategory.ARCHIVES, categoryStats, Modifier.weight(1f), Icons.Rounded.FolderZip, extra.media, onCategory, 3)
                }
            }
        }

        item(key = "files-header") {
            SectionHeader("Protected Files (${entries.size})", trailing = "Sort by: Enclave Added")
        }

        // Protected file rows
        items(entries.size, key = { entries[it].id }) { index ->
            val entry = entries[index]
            VaultFileRow(
                entry = entry,
                locked = !unlocked,
                index = index + 4,
                onClick = { onSelectEntry(entry) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (entries.isEmpty()) {
            item(key = "empty") {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    IconSquircle(
                        icon = Icons.Rounded.Article,
                        tint = extra.outlineSoft,
                        background = extra.surfaceHighest,
                        size = 60.dp,
                        corner = 20.dp,
                        iconSize = 28.dp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No sealed artifacts yet",
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Use " + "\"Move to Vault\"" + " from the Files tab",
                        fontFamily = MonoFontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: VaultCategory,
    stats: Map<VaultCategory, Pair<Int, Long>>,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color,
    onCategory: (VaultCategory) -> Unit,
    index: Int,
) {
    val extra = LocalNexusExtra.current
    val (count, bytes) = stats[category] ?: (0 to 0L)
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier
            .background(extra.surfaceLow, RoundedCornerShape(20.dp))
            .border(1.dp, extra.hairline, RoundedCornerShape(20.dp))
            .pressScale(interaction)
            .tappable(interaction) { onCategory(category) }
            .padding(16.dp)
            .enterStagger(index),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            category.title,
            style = MaterialTheme.typography.titleSmall,
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.W700,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            category.subtitle(count, bytes),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = MonoFontFamily,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VaultFileRow(
    entry: VaultEntry,
    locked: Boolean,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = LocalNexusExtra.current
    val interaction = remember { MutableInteractionSource() }
    val icon = when (entry.category) {
        VaultCategory.DOCUMENTS -> Icons.Rounded.PictureAsPdf
        VaultCategory.MEDIA -> Icons.Rounded.Image
        VaultCategory.KEYS -> Icons.Rounded.Key
        VaultCategory.ARCHIVES -> Icons.Rounded.FolderZip
    }
    val tint = when (entry.category) {
        VaultCategory.DOCUMENTS -> extra.docs
        VaultCategory.MEDIA -> Color(0xFFFFB74A)
        VaultCategory.KEYS -> extra.success
        VaultCategory.ARCHIVES -> extra.media
    }
    Row(
        modifier
            .enterStagger(index)
            .fillMaxWidth()
            .background(extra.surfaceLow, RoundedCornerShape(20.dp))
            .border(1.dp, extra.hairline, RoundedCornerShape(20.dp))
            .pressScale(interaction)
            .tappable(interaction, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            IconSquircle(icon = icon, tint = tint, background = tint, size = 44.dp)
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(17.dp)
                    .background(extra.surfaceHighest, CircleShape)
                    .border(1.dp, extra.surfaceLow, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(10.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${Formats.size(entry.bytes)} · ${entry.cipherSuite.replace("AES-256-", "Encrypted ")}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = { onClick() }) {
            Icon(
                Icons.Rounded.Info,
                contentDescription = "Inspect",
                tint = extra.docs,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
