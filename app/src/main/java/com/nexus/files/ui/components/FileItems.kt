package com.nexus.files.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.GetApp
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.nexus.files.data.FileEntry
import com.nexus.files.data.FileKind
import com.nexus.files.data.Formats
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily
import com.nexus.files.ui.theme.NexusMotion
import java.io.File

/** Brand-flavored glyph + color per file type. */
data class KindStyle(val icon: ImageVector, val color: Color)

@Composable
fun kindStyle(kind: FileKind): KindStyle {
    val extra = LocalNexusExtra.current
    return when (kind) {
        FileKind.FOLDER -> KindStyle(Icons.Rounded.Folder, extra.docs)
        FileKind.IMAGE -> KindStyle(Icons.Rounded.Image, Color(0xFFFFB74A))
        FileKind.VIDEO -> KindStyle(Icons.Rounded.Movie, Color(0xFFFF8A3D))
        FileKind.AUDIO -> KindStyle(Icons.Rounded.MusicNote, Color(0xFFB6FF3C))
        FileKind.PDF -> KindStyle(Icons.Rounded.PictureAsPdf, Color(0xFFFF5252))
        FileKind.DOC -> KindStyle(Icons.Rounded.Description, extra.docs)
        FileKind.PRESENTATION -> KindStyle(Icons.Rounded.PlayCircle, Color(0xFF22EF7E))
        FileKind.APK -> KindStyle(Icons.Rounded.GetApp, Color(0xFF2E9BF0))
        FileKind.ARCHIVE -> KindStyle(Icons.Rounded.FolderZip, extra.media)
        FileKind.CODE -> KindStyle(Icons.Rounded.Code, Color(0xFF9B7BF0))
        FileKind.KEY -> KindStyle(Icons.Rounded.Key, Color(0xFFFFD600))
        FileKind.TEXT -> KindStyle(Icons.Rounded.Article, extra.docs)
        FileKind.OTHER -> KindStyle(Icons.Rounded.InsertDriveFile, extra.outlineSoft)
    }
}

/** 40-44dp squircle with either a glyph or an image thumbnail + selection check. */
@Composable
fun FileIcon(
    entry: FileEntry,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 46.dp,
) {
    val style = kindStyle(entry.kind)
    val shape = RoundedCornerShape(15.dp)
    Box(modifier.size(size)) {
        if (entry.kind == FileKind.IMAGE) {
            AsyncImage(
                model = entry.file,
                contentDescription = entry.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .border(1.dp, LocalNexusExtra.current.hairline, shape),
            )
        } else {
            IconSquircle(
                icon = style.icon,
                tint = style.color,
                background = style.color,
                size = size,
                modifier = Modifier.fillMaxSize(),
            )
        }
        SelectionBadge(
            visible = selected,
            modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
        )
    }
}

/** Emerald check pill that springs in when an item is selected. */
@Composable
fun SelectionBadge(visible: Boolean, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "badgePop",
    )
    if (scale > 0.01f) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 0f)
                }
                .size(18.dp)
                .background(LocalNexusExtra.current.success, RoundedCornerShape(9.dp))
                .border(2.dp, LocalNexusExtra.current.surfaceLow, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center,
        ) {
            TickGlyph(progress = 1f, color = Color(0xFF002B14), modifier = Modifier.size(10.dp))
        }
    }
}

/** List row: folder variant (tag chip + count metadata). */@Composable
fun FolderListRow(
    entry: FileEntry,
    tag: String?,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = LocalNexusExtra.current
    RowEntryContainer(selected = selected, onClick = onClick, onLongClick = onLongClick, modifier = modifier) {
        val style = kindStyle(FileKind.FOLDER)
        FileIcon(entry = entry.copy(kind = FileKind.FOLDER), selected = selected)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.name,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = MonoFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${entry.metaLabel()} · ${Formats.date(entry.modified)}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = MonoFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        if (tag != null) {
            TagChip(tag, color = extra.outlineSoft)
            Spacer(Modifier.width(4.dp))
        }
        KebabButton(onMenu)
    }
}

/** List row: file variant (size · date · type chip). */
@Composable
fun FileListRow(
    entry: FileEntry,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = LocalNexusExtra.current
    val style = kindStyle(entry.kind)
    RowEntryContainer(selected = selected, onClick = onClick, onLongClick = onLongClick, modifier = modifier) {
        FileIcon(entry = entry, selected = selected)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.name,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = MonoFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Formats.size(entry.size),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = MonoFontFamily,
                    color = extra.success,
                )
                Text(
                    "  ·  ${Formats.date(entry.modified)}  ·  ",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = MonoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TagChip(entry.ext.uppercase(), color = style.color)
            }
        }
        KebabButton(onMenu)
    }
}

@Composable
private fun KebabButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    IconButton(onClick = onClick, modifier = Modifier.pressScale(interaction, 0.9f)) {
        Icon(
            Icons.Rounded.MoreVert,
            contentDescription = "More",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Animated container handling selection borders + press squish. */
@Composable
private fun RowEntryContainer(
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val extra = LocalNexusExtra.current
    val interaction = remember { MutableInteractionSource() }
    val bg by animateColorAsState(
        if (selected) extra.docs.copy(alpha = 0.12f) else extra.surfaceLow,
        tween(NexusMotion.baseMs), label = "rowBg",
    )
    val borderColor by animateColorAsState(
        if (selected) extra.docs else extra.hairline,
        tween(NexusMotion.baseMs), label = "rowBorder",
    )
    val borderWidth by animateFloatAsState(if (selected) 1.8f else 1f, tween(NexusMotion.quickMs), label = "rowBorderW")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(20.dp))
            .border(borderWidth.dp, borderColor, RoundedCornerShape(20.dp))
            .pressScale(interaction)
            .combinedClickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

/** Grid tile: full-bleed thumbnail (or glyph) with gradient scrim + name. */
@Composable
fun FileGridTile(
    entry: FileEntry,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = LocalNexusExtra.current
    val style = kindStyle(entry.kind)
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(extra.surfaceLow, shape)
            .border(1.dp, if (selected) extra.docs else extra.hairline, shape)
            .pressScale(interaction)
            .tappable(interaction, onClick = onClick),
    ) {
        if (entry.kind == FileKind.IMAGE) {
            AsyncImage(
                model = entry.file,
                contentDescription = entry.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.55f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.72f),
                        ),
                    ),
            )
        } else {
            Box(
                Modifier.fillMaxSize().padding(bottom = 34.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconSquircle(
                    icon = style.icon,
                    tint = style.color,
                    background = style.color,
                    size = 54.dp,
                    corner = 18.dp,
                    iconSize = 28.dp,
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.6f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.45f),
                        ),
                    ),
            )
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Text(
                entry.name,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = MonoFontFamily,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
            )
            Text(
                Formats.size(entry.size),
                fontFamily = MonoFontFamily,
                fontSize = 10.sp,
                color = extra.success,
            )
        }
        SelectionBadge(
            visible = selected,
            modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
        )
    }
}
private fun FileEntry.metaLabel(): String = when {
    isDirectory && kind == FileKind.FOLDER -> {
        val counts = file.listFiles()?.size ?: 0
        val subdirs = file.listFiles()?.count { it.isDirectory } ?: 0
        when {
            counts == 0 -> "Empty folder"
            subdirs > 0 -> "$counts items"
            else -> "$counts files"
        }
    }
    else -> Formats.size(size)
}
