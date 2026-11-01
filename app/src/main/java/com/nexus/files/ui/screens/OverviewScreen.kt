package com.nexus.files.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GetApp
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.files.MainViewModel
import com.nexus.files.data.FileEntry
import com.nexus.files.data.FileFilter
import com.nexus.files.data.Formats
import com.nexus.files.data.StorageStats
import com.nexus.files.ui.components.AnimatedCounter
import com.nexus.files.ui.components.FileIcon
import com.nexus.files.ui.components.IconSquircle
import com.nexus.files.ui.components.LegendDot
import com.nexus.files.ui.components.NexusCard
import com.nexus.files.ui.components.SectionHeader
import com.nexus.files.ui.components.SegmentedStorageBar
import com.nexus.files.ui.components.ThinProgress
import com.nexus.files.ui.components.enterStagger
import com.nexus.files.ui.components.kindStyle
import com.nexus.files.ui.components.pressScale
import com.nexus.files.ui.components.tappable
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily

@Composable
fun OverviewScreen(
    overviewState: com.nexus.files.OverviewUiState,
    onOpenFile: (FileEntry) -> Unit,
    onSearch: () -> Unit,
    onQuickAccess: (FileFilter) -> Unit,
) {
    val extra = LocalNexusExtra.current
    val state = overviewState

    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Search capsule
        item(key = "search") {
            SearchCapsule(
                placeholder = "Search ${state.fileCount.coerceAtLeast(1).let { String.format(java.util.Locale.ROOT, "%,d", it) }} files, apps, media…",
                onClick = onSearch,
                modifier = Modifier.padding(horizontal = 16.dp).enterStagger(0),
            )
        }

        // Internal storage card
        item(key = "storage") {
            NexusCard(
                modifier = Modifier.padding(horizontal = 16.dp).enterStagger(1),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
            ) {
                StorageCard(state.stats)
            }
        }

        // Real mounted storage volumes (internal + any SD cards)
        val volumes = state.volumes
        itemsIndexed(volumes.filterNot { it.isPrimary }, key = { _, it -> it.root.absolutePath }) { index, vol ->
            VolumeRow(
                volume = vol,
                modifier = Modifier.padding(horizontal = 16.dp).enterStagger(2 + index),
            )
        }

        // Quick access
        item(key = "qa-header") { SectionHeader("Quick Access", trailing = "Categories") }
        item(key = "qa") {
            val quick = state.quickCounts
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .enterStagger(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickTile("Images", formatCount(quick[com.nexus.files.data.FileKind.IMAGE] ?: 0), Color(0xFFFFB74A), Modifier.weight(1f)) { onQuickAccess(FileFilter.IMAGES) }
                QuickTile("Videos", formatCount(quick[com.nexus.files.data.FileKind.VIDEO] ?: 0), Color(0xFFFF8A3D), Modifier.weight(1f)) { onQuickAccess(FileFilter.MEDIA) }
                QuickTile(
                    "Docs",
                    formatCount(
                        (quick[com.nexus.files.data.FileKind.PDF] ?: 0) +
                            (quick[com.nexus.files.data.FileKind.DOC] ?: 0) +
                            (quick[com.nexus.files.data.FileKind.TEXT] ?: 0) +
                            (quick[com.nexus.files.data.FileKind.PRESENTATION] ?: 0),
                    ),
                    extra.docs,
                    Modifier.weight(1f),
                ) { onQuickAccess(FileFilter.DOCS) }
                QuickTile("APKs", formatCount(quick[com.nexus.files.data.FileKind.APK] ?: 0), Color(0xFF2E9BF0), Modifier.weight(1f)) { onQuickAccess(FileFilter.ARCHIVES) }
            }
        }

        // Recent files
        item(key = "recent-header") { SectionHeader("Recent Files", trailing = "Sorted by Date") }
        if (state.loading) {
            items(3) { index -> SkeletonRow(Modifier.padding(horizontal = 16.dp)) }
        } else {
            itemsIndexed(state.recent, key = { _, it -> it.file.absolutePath }) { index, entry ->
                RecentRow(
                    entry = entry,
                    onClick = { onOpenFile(entry) },
                    modifier = Modifier.padding(horizontal = 16.dp).enterStagger(index + 5),
                )
            }
        }
    }
}

@Composable
fun SearchCapsule(
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = LocalNexusExtra.current
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(extra.surfaceMid, RoundedCornerShape(26.dp))
            .border(1.dp, extra.hairline, RoundedCornerShape(26.dp))
            .pressScale(interaction, 0.98f)
            .tappable(interaction, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = extra.docs, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            placeholder,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = MonoFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.Rounded.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Icon(Icons.Rounded.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun StorageCard(stats: StorageStats?) {
    val extra = LocalNexusExtra.current
    val usedGb = (stats?.usedBytes ?: 0L) / BYTES_GB
    val totalGb = (stats?.totalBytes ?: 0L) / BYTES_GB
    val freeGb = (stats?.freeBytes ?: 0L) / BYTES_GB
    val percent = stats?.usedPercent ?: 0

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconSquircle(
                icon = Icons.Rounded.Storage,
                tint = extra.docs,
                background = extra.docs,
                size = 44.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Internal Storage",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = MonoFontFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "$percent% used",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = MonoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "$percent%",
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.W700,
                fontSize = 22.sp,
                color = extra.docs,
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedCounter(
                value = usedGb,
                decimals = 1,
                style = MaterialTheme.typography.displayMedium,
                color = extra.docs,
                fontFamily = MonoFontFamily,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "of ${stats?.totalBytes?.let { "%.0f".format(it / BYTES_GB) } ?: "128"} GB",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = MonoFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${"%.1f".format(freeGb)} GB free",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = MonoFontFamily,
                color = extra.success,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }

        Spacer(Modifier.height(12.dp))
        SegmentedStorageBar(categories = stats?.categories().orEmpty(), totalBytes = stats?.totalBytes ?: 1L)
        Spacer(Modifier.height(14.dp))

        val cats = stats?.categories().orEmpty()
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendEntry(cats.getOrNull(0), Modifier.weight(1f))
            LegendEntry(cats.getOrNull(1), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendEntry(cats.getOrNull(2), Modifier.weight(1f))
            LegendEntry(cats.getOrNull(3), Modifier.weight(1f))
        }
    }
}

@Composable
private fun LegendEntry(category: com.nexus.files.data.StorageCategory?, modifier: Modifier = Modifier) {
    val extra = LocalNexusExtra.current
    if (category == null) {
        Spacer(modifier)
        return
    }
    val color = when (category.colorKey) {
        com.nexus.files.data.CategoryColor.SYSTEM -> extra.docs
        com.nexus.files.data.CategoryColor.MEDIA -> extra.media
        com.nexus.files.data.CategoryColor.DOCS -> Color(0xFF2E9BF0)
        com.nexus.files.data.CategoryColor.SUCCESS -> extra.success
        com.nexus.files.data.CategoryColor.WARNING -> extra.warning
    }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        LegendDot(color)
        Spacer(Modifier.width(6.dp))
        Text(
            category.label,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = MonoFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            Formats.size(category.bytes),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.W700,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun VolumeRow(
    volume: com.nexus.files.data.StorageVolumeInfo,
    modifier: Modifier = Modifier,
) {
    val extra = LocalNexusExtra.current
    val used = volume.usedBytes / (1024.0 * 1024 * 1024)
    val total = volume.totalBytes / (1024.0 * 1024 * 1024)
    val free = volume.freeBytes / (1024.0 * 1024 * 1024)
    val tint = extra.media
    SourceRow(
        icon = Icons.Rounded.Dns,
        title = volume.name,
        subtitle = "${"%.1f".format(free)} GB free of ${"%.1f".format(total)} GB",
        progress = if (volume.totalBytes <= 0) 0f else volume.usedBytes.toFloat() / volume.totalBytes,
        tint = tint,
        modifier = modifier,
    )
}

@Composable
private fun SourceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    progress: Float,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val extra = LocalNexusExtra.current
    val interaction = remember { MutableInteractionSource() }
    NexusCard(modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconSquircle(icon = icon, tint = tint, background = tint, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = MonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(7.dp).background(extra.success, CircleShape))
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = MonoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                ThinProgress(progress = progress, color = tint)
            }
            Spacer(Modifier.width(10.dp))
            Icon(
                Icons.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(16.dp)
                    .pressScale(interaction, 0.85f),
            )
        }
    }
}

@Composable
private fun QuickTile(
    label: String,
    count: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val extra = LocalNexusExtra.current
    val interaction = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = tween(300), label = "tile")
    Column(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(extra.surfaceLow, RoundedCornerShape(20.dp))
            .border(1.dp, extra.hairline, RoundedCornerShape(20.dp))
            .pressScale(interaction)
            .tappable(interaction, onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconSquircle(
            icon = iconForLabel(label),
            tint = tint,
            background = tint,
            size = 38.dp,
            corner = 13.dp,
            iconSize = 19.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            fontFamily = MonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.W700,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            count,
            fontFamily = MonoFontFamily,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun iconForLabel(label: String): ImageVector = when (label) {
    "Images" -> Icons.Rounded.Image
    "Videos" -> Icons.Rounded.Movie
    "Docs" -> Icons.Rounded.Description
    else -> Icons.Rounded.GetApp
}

@Composable
private fun RecentRow(
    entry: FileEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = LocalNexusExtra.current
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier
            .fillMaxWidth()
            .background(extra.surfaceLow, RoundedCornerShape(18.dp))
            .border(1.dp, extra.hairline, RoundedCornerShape(18.dp))
            .pressScale(interaction)
            .tappable(interaction, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FileIcon(entry = entry, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.name,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                "${Formats.size(entry.size)} · ${Formats.date(entry.modified)}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = MonoFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SkeletonRow(modifier: Modifier = Modifier) {
    val extra = LocalNexusExtra.current
    Row(
        modifier
            .fillMaxWidth()
            .background(extra.surfaceLow, RoundedCornerShape(18.dp))
            .border(1.dp, extra.hairline, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).background(extra.surfaceHigh, RoundedCornerShape(13.dp)))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Box(Modifier.fillMaxWidth(0.6f).height(12.dp).background(extra.surfaceHigh, RoundedCornerShape(6.dp)))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(0.4f).height(10.dp).background(extra.surfaceHigh, RoundedCornerShape(5.dp)))
        }
    }
}

private const val BYTES_GB = 1024.0 * 1024.0 * 1024.0

private fun formatCount(count: Int): String =
    String.format(java.util.Locale.ROOT, "%,d", count)
