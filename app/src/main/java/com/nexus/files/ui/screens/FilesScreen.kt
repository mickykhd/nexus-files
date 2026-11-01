package com.nexus.files.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.nexus.files.data.FileEntry
import com.nexus.files.data.FileFilter
import com.nexus.files.data.Formats
import com.nexus.files.data.SortOrder
import com.nexus.files.ui.components.FileGridTile
import com.nexus.files.ui.components.FileListRow
import com.nexus.files.ui.components.FolderListRow
import com.nexus.files.ui.components.IconSquircle
import com.nexus.files.ui.components.NexusCard
import com.nexus.files.ui.components.SectionHeader
import com.nexus.files.ui.components.TickGlyph
import com.nexus.files.ui.components.enterStagger
import com.nexus.files.ui.components.pressScale
import com.nexus.files.ui.components.tappable
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily
import com.nexus.files.ui.theme.NexusMotion

@Composable
fun FilesScreen(
    entries: List<FileEntry>,
    folders: List<FileEntry>,
    files: List<FileEntry>,
    selection: Set<String>,
    filter: FileFilter,
    sort: SortOrder,
    gridMode: Boolean,
    loading: Boolean,
    breadcrumbLabel: List<Pair<String, java.io.File>>,
    freeBytes: Long,
    totalBytes: Long,
    onNavigate: (java.io.File) -> Unit,
    onUp: () -> Unit,
    onFilter: (FileFilter) -> Unit,
    onSort: (SortOrder) -> Unit,
    onToggleGrid: () -> Unit,
    onOpen: (FileEntry) -> Unit,
    onLongPress: (FileEntry) -> Unit,
    onMenu: (FileEntry) -> Unit,
    onShareSelection: () -> Unit,
    onDeleteSelection: () -> Unit,
    onMoveSelection: () -> Unit,
    onNew: () -> Unit,
    onSearch: () -> Unit,
) {
    val extra = LocalNexusExtra.current

    Box(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            BrowserTopBar(
                crumbs = breadcrumbLabel,
                gridMode = gridMode,
                onUp = onUp,
                onCrumb = onNavigate,
                onSearch = onSearch,
                onToggleGrid = onToggleGrid,
                onSort = onSort,
            )

            FilterChips(filter = filter, onFilter = onFilter)

            AnimatedVisibility(
                visible = selection.isNotEmpty(),
                enter = expandVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                SelectionActionBar(
                    count = selection.size,
                    totalBytes = selection.sumOf { path ->
                        val f = java.io.File(path)
                        if (f.isDirectory) f.walkTopDown().filter { it.isFile }.sumOf { it.length() } else f.length()
                    },
                    onShare = onShareSelection,
                    onDelete = onDeleteSelection,
                    onMove = onMoveSelection,
                )
            }

            AnimatedContent(
                targetState = gridMode,
                transitionSpec = {
                    (fadeIn(tween(260)) + slideInHorizontally { if (targetState) it / 6 else -it / 6 }) togetherWith
                        (fadeOut(tween(180)) + slideOutHorizontally { if (targetState) -it / 6 else it / 6 })
                },
                label = "gridSwitch",
            ) { isGrid ->
                if (isGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 108.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 150.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(folders + files, key = { _, it -> it.file.absolutePath }) { index, entry ->
                            FileGridTile(
                                entry = entry,
                                selected = entry.file.absolutePath in selection,
                                onClick = { onOpen(entry) },
                                onLongClick = { onLongPress(entry) },
                                modifier = Modifier.enterStagger(index, 28),
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (folders.isNotEmpty()) {
                            item(key = "fh") { SectionHeader("Folders (${folders.size})", trailing = "Directories") }
                            itemsIndexed(folders, key = { _, it -> it.file.absolutePath }) { index, entry ->
                                FolderListRow(
                                    entry = entry,
                                    tag = folderTag(entry),
                                    selected = entry.file.absolutePath in selection,
                                    onClick = { onOpen(entry) },
                                    onLongClick = { onLongPress(entry) },
                                    onMenu = { onMenu(entry) },
                                    modifier = Modifier.enterStagger(index, 32),
                                )
                            }
                        }
                        if (files.isNotEmpty()) {
                            item(key = "sh") { Spacer(Modifier.height(8.dp)) }
                            item(key = "xh") {
                                SectionHeader(
                                    "Files (${files.size})",
                                    trailing = "Sorted by ${sort.label}",
                                )
                            }
                            itemsIndexed(files, key = { _, it -> it.file.absolutePath }) { index, entry ->
                                FileListRow(
                                    entry = entry,
                                    selected = entry.file.absolutePath in selection,
                                    onClick = { onOpen(entry) },
                                    onLongClick = { onLongPress(entry) },
                                    onMenu = { onMenu(entry) },
                                    modifier = Modifier.enterStagger(index + folders.size, 32),
                                )
                            }
                        }
                        if (!loading && folders.isEmpty() && files.isEmpty()) {
                            item(key = "empty") {
                                EmptyFolderHint()
                            }
                        }
                    }
                }
            }
        }

        // Bottom "storage + New" bar
        BottomStorageBar(
            freeBytes = freeBytes,
            totalBytes = totalBytes,
            onNew = onNew,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun folderTag(entry: FileEntry): String {
    // Real location tag: "Internal" for root-level folders, the real parent
    // name for anything nested (e.g. DCIM for DCIM/Camera).
    val root = android.os.Environment.getExternalStorageDirectory()
    val parent = entry.file.parentFile ?: return "Internal"
    return if (parent.absolutePath == root.absolutePath) "Internal" else parent.name
}

@Composable
private fun BrowserTopBar(
    crumbs: List<Pair<String, java.io.File>>,
    gridMode: Boolean,
    onUp: () -> Unit,
    onCrumb: (java.io.File) -> Unit,
    onSearch: () -> Unit,
    onToggleGrid: () -> Unit,
    onSort: (SortOrder) -> Unit,
) {
    val extra = LocalNexusExtra.current
    var sortMenu by remember { mutableStateOf(false) }

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopIcon(Icons.Rounded.ArrowBack, onClick = onUp)
            Icon(
                Icons.Rounded.PhoneAndroid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Row(
                Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                crumbs.forEachIndexed { index, (label, dir) ->
                    val last = index == crumbs.lastIndex
                    if (index > 0) {
                        Text(
                            "  ›  ",
                            fontFamily = MonoFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                    }
                    if (last) {
                        Box(
                            Modifier
                                .background(extra.docs.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                                .border(1.dp, extra.docs.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .tappable(remember { MutableInteractionSource() }) { onCrumb(dir) }
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Folder,
                                    contentDescription = null,
                                    tint = extra.docs,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    label,
                                    fontFamily = MonoFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.W700,
                                    color = extra.docs,
                                )
                            }
                        }
                    } else {
                        Text(
                            label,
                            fontFamily = MonoFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .graphicsLayer { }
                                .tappable(remember { MutableInteractionSource() }) { onCrumb(dir) },
                        )
                    }
                }
            }
            TopIcon(Icons.Rounded.Search, onClick = onSearch)
            TopIcon(if (gridMode) Icons.Rounded.List else Icons.Rounded.GridView, onClick = onToggleGrid)
            Box {
                TopIcon(Icons.Rounded.Sort, onClick = { sortMenu = true })
                DropdownMenu(
                    expanded = sortMenu,
                    onDismissRequest = { sortMenu = false },
                    properties = PopupProperties(focusable = true),
                    containerColor = extra.surfaceHigh,
                ) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(order.label, fontFamily = MonoFontFamily, fontSize = 13.sp)
                                    Spacer(Modifier.weight(1f))
                                    TickGlyph(
                                        progress = 1f,
                                        color = extra.success,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            },
                            onClick = {
                                sortMenu = false
                                onSort(order)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        Modifier
            .size(38.dp)
            .pressScale(interaction, 0.88f)
            .tappable(interaction, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun FilterChips(filter: FileFilter, onFilter: (FileFilter) -> Unit) {
    val extra = LocalNexusExtra.current
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FileFilter.entries.forEach { item ->
            val selected = item == filter
            val bg by animateColorAsState2(if (selected) extra.success else extra.surfaceMid)
            val border by animateColorAsState2(if (selected) extra.success else extra.hairline)
            val scale by animateFloatAsState(
                if (selected) 1.05f else 1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "chipScale",
            )
            val interaction = remember { MutableInteractionSource() }
            Row(
                Modifier
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .background(bg, RoundedCornerShape(20.dp))
                    .border(1.5.dp, border, RoundedCornerShape(20.dp))
                    .pressScale(interaction, 0.94f)
                    .tappable(interaction) { onFilter(item) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn() + slideInHorizontally { -it / 2 },
                    exit = fadeOut() + slideOutHorizontally { -it / 2 },
                ) {
                    Row {
                        TickGlyph(
                            progress = 1f,
                            color = Color(0xFF002B14),
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                }
                Text(
                    item.label,
                    fontFamily = MonoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W700,
                    color = if (selected) Color(0xFF002B14) else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun animateColorAsState2(target: Color) =
    androidx.compose.animation.animateColorAsState(target, tween(NexusMotion.quickMs), label = "chip")

@Composable
private fun SelectionActionBar(
    count: Int,
    totalBytes: Long,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
) {
    val extra = LocalNexusExtra.current
    NexusCard(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = extra.docs,
                modifier = Modifier.size(26.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "$count item${if (count == 1) "" else "s"} selected",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = MonoFontFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "${Formats.size(totalBytes)} total",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = MonoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            ActionIcon(Icons.Rounded.Share, onClick = onShare)
            Spacer(Modifier.width(14.dp))
            ActionIcon(Icons.Rounded.Delete, tint = extra.danger, onClick = onDelete)
            Spacer(Modifier.width(14.dp))
            ActionIcon(Icons.Rounded.DriveFileMove, onClick = onMove)
            Spacer(Modifier.width(14.dp))
            ActionIcon(Icons.Rounded.MoreVert, onClick = onMove)
        }
    }
}

@Composable
private fun ActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        Modifier
            .size(34.dp)
            .pressScale(interaction, 0.85f)
            .tappable(interaction, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun BottomStorageBar(
    freeBytes: Long,
    totalBytes: Long,
    onNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = LocalNexusExtra.current
    Box(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        NexusCard(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconSquircle(
                    icon = Icons.Rounded.Storage,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    background = extra.surfaceHighest,
                    size = 40.dp,
                    corner = 13.dp,
                    iconSize = 20.dp,
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Internal Storage",
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = MonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "${Formats.size(freeBytes)} free of ${Formats.size(totalBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = MonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        val fabInteraction = remember { MutableInteractionSource() }
        val fabScale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = NexusMotion.bouncySpring,
            label = "fabPop",
        )
        ExtendedFloatingActionButton(
            onClick = onNew,
            containerColor = extra.docs,
            contentColor = Color(0xFF00363D),
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
            shape = RoundedCornerShape(22.dp),
            interactionSource = fabInteraction,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .graphicsLayer { scaleX = fabScale; scaleY = fabScale }
                .pressScale(fabInteraction, 0.94f),
            icon = { Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(22.dp)) },
            text = {
                Text(
                    "New",
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.W700,
                    fontSize = 15.sp,
                )
            },
        )
    }
}

@Composable
private fun EmptyFolderHint() {
    val extra = LocalNexusExtra.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconSquircle(
            icon = Icons.Rounded.CreateNewFolder,
            tint = extra.outlineSoft,
            background = extra.surfaceHighest,
            size = 64.dp,
            corner = 22.dp,
            iconSize = 30.dp,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "Nothing here yet",
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.W700,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Tap \"New\" to create a folder or file",
            fontFamily = MonoFontFamily,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Per-file overflow menu (opens from the row kebab). */
@Composable
fun FileActionsMenu(
    entry: FileEntry?,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onVault: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val extra = LocalNexusExtra.current
    DropdownMenu(
        expanded = entry != null,
        onDismissRequest = onDismiss,
        containerColor = extra.surfaceHigh,
    ) {
        DropdownMenuItem(text = { Text("Open", fontFamily = MonoFontFamily, fontSize = 13.sp) }, onClick = { onOpen(); onDismiss() })
        DropdownMenuItem(text = { Text("Share", fontFamily = MonoFontFamily, fontSize = 13.sp) }, onClick = { onShare(); onDismiss() })
        DropdownMenuItem(text = { Text("Rename", fontFamily = MonoFontFamily, fontSize = 13.sp) }, onClick = { onRename(); onDismiss() })
        DropdownMenuItem(text = { Text("Move to Vault", fontFamily = MonoFontFamily, fontSize = 13.sp) }, onClick = { onVault(); onDismiss() })
        Divider(color = extra.hairline)
        DropdownMenuItem(
            text = { Text("Delete", fontFamily = MonoFontFamily, fontSize = 13.sp, color = extra.danger) },
            onClick = { onDelete(); onDismiss() },
        )
    }
}
