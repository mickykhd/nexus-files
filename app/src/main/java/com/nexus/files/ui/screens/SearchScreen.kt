package com.nexus.files.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.files.data.FileEntry
import com.nexus.files.ui.components.FileIcon
import com.nexus.files.ui.components.IconSquircle
import com.nexus.files.ui.components.SectionHeader
import com.nexus.files.ui.components.enterStagger
import com.nexus.files.ui.components.pressScale
import com.nexus.files.ui.components.tappable
import com.nexus.files.ui.theme.LocalNexusExtra
import com.nexus.files.ui.theme.MonoFontFamily

/**
 * Real search: recursive lookup across shared storage with live results.
 */
@Composable
fun SearchScreen(
    query: String,
    results: List<FileEntry>,
    searching: Boolean,
    searched: Boolean,
    onQueryChange: (String) -> Unit,
    onOpen: (FileEntry) -> Unit,
    onBack: () -> Unit,
) {
    val extra = LocalNexusExtra.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .imePadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopBarIconButton(Icons.Rounded.ArrowBack, onClick = onBack)
            Spacer(Modifier.width(6.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = MonoFontFamily),
                shape = RoundedCornerShape(18.dp),
                placeholder = {
                    Text("Search files by name…", fontFamily = MonoFontFamily, fontSize = 13.sp)
                },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = extra.docs) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        androidx.compose.material3.IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = extra.docs,
                    unfocusedBorderColor = extra.hairline,
                    focusedContainerColor = extra.surfaceMid,
                    unfocusedContainerColor = extra.surfaceMid,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focus),
            )
        }

        when {
            query.isBlank() -> {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    IconSquircle(
                        icon = Icons.Rounded.Search,
                        tint = extra.outlineSoft,
                        background = extra.surfaceHighest,
                        size = 64.dp,
                        corner = 22.dp,
                        iconSize = 30.dp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Search across your storage",
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            searching -> {
                Text(
                    "Searching…",
                    fontFamily = MonoFontFamily,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
            results.isEmpty() && searched -> {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    IconSquircle(
                        icon = Icons.Rounded.SearchOff,
                        tint = extra.outlineSoft,
                        background = extra.surfaceHighest,
                        size = 64.dp,
                        corner = 22.dp,
                        iconSize = 30.dp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No files match \"$query\"",
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.W700,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            else -> {
                SectionHeader("Results (${results.size})", trailing = "Name match")
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(results, key = { _, it -> it.file.absolutePath }) { index, entry ->
                        SearchResultRow(entry, index, { onOpen(entry) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(entry: FileEntry, index: Int, onClick: () -> Unit) {
    val extra = LocalNexusExtra.current
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .enterStagger(index, 25)
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
                entry.file.parentFile?.absolutePath
                    ?.removePrefix(android.os.Environment.getExternalStorageDirectory().absolutePath)
                    ?.ifBlank { "/" } ?: "/",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = MonoFontFamily,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    androidx.compose.foundation.layout.Box(
        Modifier
            .size(40.dp)
            .pressScale(interaction, 0.88f)
            .tappable(interaction, onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(21.dp))
    }
}
