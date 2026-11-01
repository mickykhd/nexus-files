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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.files.data.CleanReport
import com.nexus.files.data.FileEntry
import com.nexus.files.data.Formats
import com.nexus.files.ui.components.AnimatedCounter
import com.nexus.files.ui.components.IconSquircle
import com.nexus.files.ui.components.LegendDot
import com.nexus.files.ui.components.NexusCard
import com.nexus.files.ui.components.NexusCheckbox
import com.nexus.files.ui.components.NexusSwitch
import com.nexus.files.ui.components.SectionHeader
import com.nexus.files.ui.components.StorageDonut
import com.nexus.files.ui.components.TagChip
import com.nexus.files.ui.components.ThinProgress
import com.nexus.files.ui.components.enterStagger
import com.nexus.files.ui.components.kindStyle
import com.nexus.files.ui.components.pressScale
import com.nexus.files.ui.components.tappable
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily
import com.nexus.files.ui.theme.NexusMotion

@Composable
fun CleanScreen(
    report: CleanReport?,
    scanning: Boolean,
    progress: Float,
    selectedLarge: Set<String>,
    boosted: Boolean,
    overviewStats: com.nexus.files.data.StorageStats?,
    autoCleanScheduled: Boolean,
    nextCleanAt: Long?,
    onBoost: () -> Unit,
    onCleanCache: () -> Unit,
    onCleanDuplicates: () -> Unit,
    onDeleteSelectedLarge: () -> Unit,
    onToggleLarge: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDeleteApks: () -> Unit,
    onDeleteScreens: () -> Unit,
    onAutoClean: (Boolean) -> Unit,
) {
    val extra = LocalNexusExtra.current
    val cacheBytes = report?.cacheBytes ?: 0L
    val dupBytes = report?.duplicateBytes ?: 0L
    val largeBytes = report?.largeBytes ?: 0L
    val apkBytes = report?.apkBytes ?: 0L
    val screenBytes = report?.oldScreenBytes ?: 0L
    val total = report?.totalRecoverable ?: 0L

    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "health") {
            val usedPercent = overviewStats?.usedPercent ?: 0
            val healthLabel = when {
                usedPercent >= 90 -> "$usedPercent% used · Critical"
                usedPercent >= 75 -> "$usedPercent% used · Warning"
                else -> "$usedPercent% used · Healthy"
            }
            val healthColor = when {
                usedPercent >= 90 -> extra.danger
                usedPercent >= 75 -> extra.warning
                else -> extra.success
            }
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatusChip(Icons.Rounded.Speed, healthLabel, healthColor, Modifier.weight(1f))
                StatusChip(
                    Icons.Rounded.VerifiedUser,
                    if (total <= 0) "Nothing to reclaim" else "${Formats.size(total)} reclaimable",
                    extra.success,
                    Modifier.weight(1f),
                )
            }
        }

        // Recoverable donut
        item(key = "donut") {
            NexusCard(
                modifier = Modifier.padding(horizontal = 16.dp).enterStagger(0),
                contentPadding = PaddingValues(vertical = 22.dp),
            ) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (scanning) {
                        StorageDonut(progress = progress.coerceIn(0.05f, 0.95f)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "SCANNING…",
                                    fontFamily = MonoFontFamily,
                                    fontSize = 12.sp,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "${(progress * 100).toInt()}%",
                                    fontFamily = MonoFontFamily,
                                    fontWeight = FontWeight.W700,
                                    fontSize = 30.sp,
                                    color = extra.docs,
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        ThinProgress(progress = progress, modifier = Modifier.padding(horizontal = 60.dp))
                    } else {
                        StorageDonut(progress = if (total <= 0) 0.06f else 0.72f) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "RECOVERABLE",
                                    fontFamily = MonoFontFamily,
                                    fontSize = 11.sp,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    AnimatedCounter(
                                        value = total / (1024.0 * 1024 * 1024),
                                        decimals = 1,
                                        style = MaterialTheme.typography.displaySmall,
                                        color = extra.docs,
                                    )
                                    Text(
                                        " GB",
                                        fontFamily = MonoFontFamily,
                                        fontWeight = FontWeight.W700,
                                        fontSize = 20.sp,
                                        color = extra.success,
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "${Formats.size(dupBytes)} pending review",
                                    fontFamily = MonoFontFamily,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Storage breakdown
        item(key = "breakdown") {
            NexusCard(
                modifier = Modifier.padding(horizontal = 16.dp).enterStagger(1),
                contentPadding = PaddingValues(18.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = extra.docs, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Storage Breakdown",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.W700,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Nexus Deep Cleaner identifies space that can be reclaimed instantly without losing personal media or application configurations.",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = MonoFontFamily,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendEntry("Cache", cacheBytes, extra.warning, Modifier.weight(1f))
                        LegendEntry("Large Files", largeBytes, extra.docs, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendEntry("Clones & APKs", dupBytes + apkBytes, extra.media, Modifier.weight(1f))
                        LegendEntry("Screens", screenBytes, extra.success, Modifier.weight(1f))
                    }
                }
            }
        }

        // Optimization ready + boost button
        item(key = "boost") {
            Column(Modifier.padding(horizontal = 16.dp).enterStagger(2)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(extra.surfaceMid, RoundedCornerShape(20.dp))
                        .border(1.dp, extra.hairline, RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = extra.docs, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (boosted) "Storage Optimized" else "Storage Optimization Ready",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.W700,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Reclaiming space improves read/write indexing speed.",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = MonoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                BoostButton(
                    label = "One-Tap Boost & Clean (${Formats.size(cacheBytes + dupBytes + apkBytes)})",
                    onClick = onBoost,
                )
            }
        }

        item(key = "smart-header") {
            val categoriesWithFindings = listOf(
                cacheBytes > 0, dupBytes > 0, largeBytes > 0, apkBytes > 0, screenBytes > 0,
            ).count { it }
            SectionHeader(
                "Smart Cleaning Recommendations",
                trailing = if (categoriesWithFindings == 0) "No clutter found" else "$categoriesWithFindings Categor${if (categoriesWithFindings == 1) "y" else "ies"} Detected",
            )
        }

        // 1. App cache
        item(key = "cache") {
            CleanActionCard(
                icon = Icons.Rounded.Layers,
                title = "App Cache & Temp Data",
                tag = "Instant purge",
                amount = Formats.size(cacheBytes),
                amountColor = extra.docs,
                description = "Hidden thumbnail caches, streaming buffers, and web rendering cache. Personal chats and credentials remain intact.",
                footerChip = "Zero Data Loss" to extra.success,
                actionLabel = "Clean Cache",
                index = 3,
                onClick = onCleanCache,
            )
        }

        // 2. Duplicates
        item(key = "dupes") {
            DuplicateCard(
                report = report,
                onClean = onCleanDuplicates,
                index = 4,
            )
        }

        // 3. Large files
        item(key = "large") {
            LargeFilesCard(
                report = report,
                selected = selectedLarge,
                onToggle = onToggleLarge,
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll,
                onDelete = onDeleteSelectedLarge,
                index = 5,
            )
        }

        // 4. APKs
        item(key = "apks") {
            CleanActionCard(
                icon = Icons.Rounded.DeleteSweep,
                title = "Unused APK Installers",
                tag = "Obsolete",
                amount = Formats.size(apkBytes),
                amountColor = extra.docs,
                description = "Package installers (.apk) sit idle after software deployment. Removing them does not affect installed apps.",
                footerChip = "${report?.apkFiles?.size ?: 0} files ready to purge" to extra.warning,
                actionLabel = "Quick Delete",
                index = 6,
                onClick = onDeleteApks,
            )
        }

        // 5. Old screenshots
        item(key = "screens") {
            OldScreensCard(
                report = report,
                onDelete = onDeleteScreens,
                index = 7,
            )
        }

        // Schedule (real WorkManager state)
        item(key = "schedule") {
            NexusCard(
                modifier = Modifier.padding(horizontal = 16.dp).enterStagger(8),
                contentPadding = PaddingValues(18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (autoCleanScheduled) Icons.Rounded.CalendarMonth else Icons.Rounded.HourglassTop,
                        contentDescription = null,
                        tint = if (autoCleanScheduled) extra.success else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (autoCleanScheduled) "Auto-clean scheduled" else "Auto-clean is off",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.W700,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            if (autoCleanScheduled && nextCleanAt != null) {
                                "Junk older than 7 days is cleaned every Sunday · next run ${Formats.date(nextCleanAt)} at ${Formats.time(nextCleanAt)}"
                            } else {
                                "Turn on to clean junk older than 7 days every Sunday at 3:00 AM"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = MonoFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    NexusSwitch(checked = autoCleanScheduled, onCheckedChange = onAutoClean)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(icon: ImageVector, label: String, color: Color, modifier: Modifier = Modifier) {
    val extra = LocalNexusExtra.current
    Row(
        modifier
            .background(extra.surfaceMid, RoundedCornerShape(16.dp))
            .border(1.dp, extra.hairline, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            fontFamily = MonoFontFamily,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.W700,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LegendEntry(label: String, bytes: Long, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        LegendDot(color)
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = MonoFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            Formats.size(bytes),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.W700,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BoostButton(label: String, onClick: () -> Unit) {
    val extra = LocalNexusExtra.current
    val interaction = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = NexusMotion.bouncySpring, label = "boost")
    Row(
        Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(
                Brush.horizontalGradient(listOf(extra.docs, extra.media)),
                RoundedCornerShape(28.dp),
            )
            .pressScale(interaction, 0.96f)
            .tappable(interaction, onClick = onClick)
            .padding(vertical = 17.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.W700,
            fontSize = 14.sp,
            color = Color(0xFF00363D),
        )
    }
}

@Composable
private fun CleanActionCard(
    icon: ImageVector,
    title: String,
    tag: String,
    amount: String,
    amountColor: Color,
    description: String,
    footerChip: Pair<String, Color>,
    actionLabel: String,
    index: Int,
    onClick: () -> Unit,
) {
    val extra = LocalNexusExtra.current
    NexusCard(
        modifier = Modifier.padding(horizontal = 16.dp).enterStagger(index),
        contentPadding = PaddingValues(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconSquircle(icon = icon, tint = extra.docs, background = extra.docs, size = 40.dp, corner = 13.dp, iconSize = 20.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(3.dp))
                    TagChip(tag, color = extra.warning, filled = true)
                }
                Text(
                    amount,
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.W700,
                    fontSize = 18.sp,
                    color = amountColor,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = MonoFontFamily,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TagChip(footerChip.first, color = footerChip.second)
                Spacer(Modifier.weight(1f))
                PillActionButton(actionLabel, onClick = onClick)
            }
        }
    }
}

@Composable
private fun PillActionButton(label: String, danger: Boolean = false, onClick: () -> Unit) {
    val extra = LocalNexusExtra.current
    val interaction = remember { MutableInteractionSource() }
    val bg by animateColorAsState(
        if (danger) extra.danger else extra.surfaceHighest,
        tween(NexusMotion.quickMs), label = "pillBtn",
    )
    Row(
        Modifier
            .background(bg, RoundedCornerShape(18.dp))
            .border(1.dp, extra.hairline, RoundedCornerShape(18.dp))
            .pressScale(interaction, 0.94f)
            .tappable(interaction, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.W700,
            fontSize = 12.sp,
            color = if (danger) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DuplicateCard(report: CleanReport?, onClean: () -> Unit, index: Int) {
    val extra = LocalNexusExtra.current
    val groups = report?.duplicateGroups.orEmpty()
    NexusCard(
        modifier = Modifier.padding(horizontal = 16.dp).enterStagger(index),
        contentPadding = PaddingValues(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconSquircle(icon = Icons.Rounded.FolderCopy, tint = extra.media, background = extra.media, size = 40.dp, corner = 13.dp, iconSize = 20.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Duplicate Files Detected",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "${groups.sumOf { it.files.size }} identical binaries & shots",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = MonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        Formats.size(report?.duplicateBytes ?: 0L),
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        fontSize = 18.sp,
                        color = extra.success,
                    )
                    TagChip("Redundancy", color = extra.warning, filled = true)
                }
            }
            if (groups.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                val group = groups.first()
                Text(
                    "Sample: ${group.files.first().name} (${Formats.size(group.sizeBytes)} × ${group.files.size})",
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DuplicateThumb(group.files.getOrNull(0), "Original (${group.files.getOrNull(0)?.file?.parentFile?.name ?: "src"})", Modifier.weight(1f))
                    Spacer(Modifier.width(10.dp))
                    TagChip("Match ${group.matchPercent}%", color = extra.success, filled = true)
                    Spacer(Modifier.width(10.dp))
                    DuplicateThumb(group.files.getOrNull(1), "Copy (${group.files.getOrNull(1)?.file?.parentFile?.name ?: "dst"})", Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = extra.success, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Auto-keeps highest quality",
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                PillActionButton("Review Duplicates", onClick = onClean)
            }
        }
    }
}

@Composable
private fun DuplicateThumb(entry: FileEntry?, label: String, modifier: Modifier = Modifier) {
    val extra = LocalNexusExtra.current
    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(extra.surfaceHigh, RoundedCornerShape(14.dp))
                .border(1.dp, extra.hairline, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (entry != null && entry.kind == com.nexus.files.data.FileKind.IMAGE) {
                coil3.compose.AsyncImage(
                    model = entry.file,
                    contentDescription = label,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                )
            } else {
                Icon(
                    Icons.Rounded.PhotoLibrary,
                    contentDescription = null,
                    tint = extra.outlineSoft,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontFamily = MonoFontFamily,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LargeFilesCard(
    report: CleanReport?,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDelete: () -> Unit,
    index: Int,
) {
    val extra = LocalNexusExtra.current
    val largeFiles = report?.largeFiles.orEmpty().take(4)
    val selectedBytes = report?.largeFiles.orEmpty()
        .filter { it.file.absolutePath in selected }
        .sumOf { it.size }

    NexusCard(
        modifier = Modifier.padding(horizontal = 16.dp).enterStagger(index),
        contentPadding = PaddingValues(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Large Unused Files (>500MB)",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "High-bitrate screen recordings, archival videos, stale packages",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    Formats.size(report?.largeBytes ?: 0L),
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.W700,
                    fontSize = 18.sp,
                    color = extra.docs,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${selected.size} file${if (selected.size == 1) "" else "s"} selected",
                fontFamily = MonoFontFamily,
                fontSize = 11.sp,
                color = extra.success,
            )
            Spacer(Modifier.height(10.dp))

            largeFiles.forEach { entry ->
                val checked = entry.file.absolutePath in selected
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NexusCheckbox(checked = checked, onCheckedChange = { onToggle(entry.file.absolutePath) })
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        kindStyle(entry.kind).icon,
                        contentDescription = null,
                        tint = kindStyle(entry.kind).color,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            entry.name,
                            fontFamily = MonoFontFamily,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "${entry.file.parentFile?.name ?: ""}/ · Unpacked",
                            fontFamily = MonoFontFamily,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        Formats.size(entry.size),
                        fontFamily = MonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W700,
                        color = extra.docs,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row {
                Text(
                    "Select All",
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    color = extra.docs,
                    modifier = Modifier.tappable(remember { MutableInteractionSource() }, onClick = onSelectAll),
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    "Deselect All",
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.tappable(remember { MutableInteractionSource() }, onClick = onDeselectAll),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(extra.danger, RoundedCornerShape(20.dp))
                    .pressScale(remember { MutableInteractionSource() }, 0.96f)
                    .tappable(remember { MutableInteractionSource() }, onClick = onDelete)
                    .padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Delete Selected (${Formats.size(selectedBytes)})",
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.W700,
                    fontSize = 13.sp,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun OldScreensCard(report: CleanReport?, onDelete: () -> Unit, index: Int) {
    val extra = LocalNexusExtra.current
    val screens = report?.oldScreens.orEmpty()
    NexusCard(
        modifier = Modifier.padding(horizontal = 16.dp).enterStagger(index),
        contentPadding = PaddingValues(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconSquircle(icon = Icons.Rounded.PhotoLibrary, tint = Color(0xFFFFB74A), background = Color(0xFFFFB74A), size = 40.dp, corner = 13.dp, iconSize = 20.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Old Screenshots & Memes",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Over 6 months old",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = MonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        Formats.size(report?.oldScreenBytes ?: 0L),
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        fontSize = 18.sp,
                        color = extra.docs,
                    )
                    Text(
                        "${screens.size} items",
                        fontFamily = MonoFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                screens.take(3).forEach { s ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(56.dp)
                            .background(extra.surfaceHigh, RoundedCornerShape(12.dp))
                            .border(1.dp, extra.hairline, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (s.kind == com.nexus.files.data.FileKind.IMAGE) {
                            coil3.compose.AsyncImage(
                                model = s.file,
                                contentDescription = s.name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                            )
                        } else {
                            Icon(Icons.Rounded.InsertDriveFile, contentDescription = null, tint = extra.outlineSoft)
                        }
                    }
                }
                Box(
                    Modifier
                        .width(56.dp)
                        .height(56.dp)
                        .background(extra.surfaceHigh, RoundedCornerShape(12.dp))
                        .border(1.dp, extra.hairline, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "+${(screens.size - 3).coerceAtLeast(0)}",
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Review before deletion",
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                PillActionButton("Review & Select", onClick = onDelete)
            }
        }
    }
}
