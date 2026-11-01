package com.nexus.files

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import com.nexus.files.data.FileEntry
import com.nexus.files.data.FileFilter
import com.nexus.files.data.VaultEntry
import com.nexus.files.ui.components.NexusBottomSheet
import com.nexus.files.ui.screens.CleanScreen
import com.nexus.files.ui.screens.NexusBottomNav as CommonBottomNav
import com.nexus.files.ui.screens.NexusToast as CommonToast
import com.nexus.files.ui.screens.NexusTopBar as CommonTopBar
import com.nexus.files.ui.screens.FileActionsMenu
import com.nexus.files.ui.screens.FileInspectorContent
import com.nexus.files.ui.screens.FilesScreen
import com.nexus.files.ui.screens.NameDialog
import com.nexus.files.ui.screens.OverviewScreen
import com.nexus.files.ui.screens.PinDialog
import com.nexus.files.ui.screens.ThemeSheetContent
import com.nexus.files.ui.screens.VaultScreen
import com.nexus.files.ui.theme.NexusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusApp(viewModel: MainViewModel) {
    val appearance by viewModel.appearance.collectAsState()
    val overview by viewModel.overview.collectAsState()
    val files by viewModel.files.collectAsState()
    val clean by viewModel.clean.collectAsState()
    val vault by viewModel.vault.collectAsState()
    val toast by viewModel.toast.collectAsState()

    NexusTheme(
        mode = appearance.mode,
        palette = appearance.palette,
        contrast = appearance.contrast,
        monochromeIcons = appearance.monochromeIcons,
    ) {
        var tab by rememberSaveable { mutableStateOf(NexusTab.OVERVIEW) }
        var showThemeSheet by remember { mutableStateOf(false) }
        var menuEntry by remember { mutableStateOf<FileEntry?>(null) }
        var renameEntry by remember { mutableStateOf<FileEntry?>(null) }
        var showNewFolder by remember { mutableStateOf(false) }
        var inspectorEntry by remember { mutableStateOf<VaultEntry?>(null) }
        var pinMode by remember { mutableStateOf<PinMode?>(null) }

        val themeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val inspectorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val canvas = com.nexus.files.ui.theme.LocalNexusExtra.current.canvas

        // Back: leave folders before leaving the app.
        val inSubFolder = tab == NexusTab.FILES &&
            files.currentDir != null &&
            files.currentDir!!.absolutePath != viewModel.filesRepo.root.absolutePath
        BackHandler(enabled = inSubFolder || inspectorEntry != null || showThemeSheet) {
            when {
                inspectorEntry != null -> inspectorEntry = null
                showThemeSheet -> showThemeSheet = false
                else -> viewModel.goUp()
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .background(canvas),
        ) {
            when (tab) {
                NexusTab.OVERVIEW -> CommonTopBar(
                    contextChip = null,
                    onMenu = { tab = NexusTab.FILES },
                    onSettings = { showThemeSheet = true },
                    onTheme = { showThemeSheet = true },
                )
                NexusTab.FILES -> {}
                NexusTab.CLEAN -> CommonTopBar(
                    contextChip = "Cleaner",
                    onMenu = { tab = NexusTab.OVERVIEW },
                    onSettings = { showThemeSheet = true },
                    onTheme = { showThemeSheet = true },
                )
                NexusTab.VAULT -> CommonTopBar(
                    contextChip = "AES-256",
                    onMenu = { tab = NexusTab.OVERVIEW },
                    onSettings = { showThemeSheet = true },
                    onTheme = { showThemeSheet = true },
                    showAvatar = true,
                )
            }

            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (fadeIn(tween(300)) + slideInHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { dir * it / 4 }) togetherWith
                        (fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { -dir * it / 4 })
                },
                label = "tabs",
                modifier = Modifier.weight(1f),
            ) { current ->
                when (current) {
                    NexusTab.OVERVIEW -> OverviewScreen(
                        overviewState = overview,
                        onOpenFile = { viewModel.open(it) },
                        onSearch = { tab = NexusTab.FILES },
                        onQuickAccess = { filter ->
                            viewModel.setFilter(filter)
                            tab = NexusTab.FILES
                        },
                    )
                    NexusTab.FILES -> FilesScreen(
                        entries = files.entries,
                        folders = files.folders,
                        files = files.files,
                        selection = files.selection,
                        filter = files.filter,
                        sort = files.sort,
                        gridMode = files.gridMode,
                        loading = files.loading,
                        breadcrumbLabel = files.currentDir?.let { viewModel.filesRepo.breadcrumbs(it) }.orEmpty(),
                        freeBytes = overview.stats?.freeBytes ?: 0L,
                        totalBytes = overview.stats?.totalBytes ?: 0L,
                        onNavigate = { viewModel.openDirectory(it) },
                        onUp = { viewModel.goUp() },
                        onFilter = { viewModel.setFilter(it) },
                        onSort = { viewModel.setSort(it) },
                        onToggleGrid = { viewModel.toggleGrid() },
                        onOpen = { entry ->
                            if (files.selection.isEmpty()) viewModel.open(entry)
                            else viewModel.toggleSelection(entry)
                        },
                        onLongPress = { viewModel.toggleSelection(it) },
                        onMenu = { menuEntry = it },
                        onShareSelection = {
                            files.entries.firstOrNull { it.file.absolutePath in files.selection }?.let { viewModel.share(it) }
                        },
                        onDeleteSelection = { viewModel.deleteSelection() },
                        onMoveSelection = {
                            files.entries.filter { it.file.absolutePath in files.selection }.forEach { viewModel.moveToVault(it) }
                            viewModel.clearSelection()
                        },
                        onNew = { showNewFolder = true },
                        onSearch = { viewModel.notify("Search is wired to the overview capsule") },
                    )
                    NexusTab.CLEAN -> CleanScreen(
                        report = clean.report,
                        scanning = clean.scanning,
                        progress = clean.scanProgress,
                        selectedLarge = clean.selectedLarge,
                        boosted = clean.boosted,
                        onBoost = { viewModel.oneTapBoost() },
                        onCleanCache = { viewModel.cleanCache() },
                        onCleanDuplicates = { viewModel.deleteDuplicateCopies() },
                        onDeleteSelectedLarge = { viewModel.deleteSelectedLarge() },
                        onToggleLarge = { viewModel.toggleLarge(it) },
                        onSelectAll = { viewModel.selectAllLarge() },
                        onDeselectAll = { viewModel.deselectLarge() },
                        onDeleteApks = { viewModel.deleteApks() },
                        onDeleteScreens = { viewModel.deleteOldScreens() },
                    )
                    NexusTab.VAULT -> VaultScreen(
                        entries = vault.entries,
                        categoryStats = vault.categoryStats,
                        unlocked = vault.unlocked,
                        failedTries = if (vault.unlocked) 0 else 4,
                        onSelectEntry = { entry ->
                            if (!vault.unlocked) {
                                pinMode = if (vault.hasPin) PinMode.ENTER else PinMode.SETUP
                            } else {
                                viewModel.selectVaultEntry(entry)
                                inspectorEntry = entry
                            }
                        },
                        onUnlock = {
                            pinMode = if (vault.hasPin) PinMode.ENTER else PinMode.SETUP
                        },
                        onCategory = { viewModel.notify("Partition: ${it.title}") },
                    )
                }
            }

            CommonToast(message = toast, onDone = { viewModel.clearToast() })
            CommonBottomNav(selected = tab, onSelect = { tab = it })
        }

        // File row overflow menu
        FileActionsMenu(
            entry = menuEntry,
            onOpen = { menuEntry?.let { viewModel.open(it) } },
            onShare = { menuEntry?.let { viewModel.share(it) } },
            onRename = { renameEntry = menuEntry },
            onVault = { menuEntry?.let { viewModel.moveToVault(it) } },
            onDelete = { menuEntry?.let { viewModel.delete(it) } },
            onDismiss = { menuEntry = null },
        )

        // Theme & Appearance
        if (showThemeSheet) {
            NexusBottomSheet(sheetState = themeSheetState, onDismiss = { showThemeSheet = false }) {
                ThemeSheetContent(
                    current = appearance,
                    onApply = { viewModel.applyAppearance(it); viewModel.notify("Appearance applied") },
                    onClose = { showThemeSheet = false },
                )
            }
        }

        // Vault file inspector
        inspectorEntry?.let { entry ->
            NexusBottomSheet(sheetState = inspectorSheetState, onDismiss = { inspectorEntry = null }) {
                FileInspectorContent(
                    entry = entry,
                    autoDestruct = vault.autoDestruct,
                    biometricReauth = vault.biometricReauth,
                    onAutoDestructChange = { viewModel.setAutoDestruct(it) },
                    onBiometricReauthChange = { viewModel.setBiometricReauth(it) },
                    onMoveToPublic = {
                        viewModel.moveVaultToPublic(entry)
                        inspectorEntry = null
                    },
                    onEncryptedLink = {
                        viewModel.notify("Encrypted link copied to clipboard")
                    },
                    onWipe = {
                        viewModel.wipeVaultEntry(entry)
                        inspectorEntry = null
                    },
                    onClose = { inspectorEntry = null },
                )
            }
        }

        // Rename / new folder dialogs
        renameEntry?.let { entry ->
            NameDialog(
                title = "Rename item",
                initial = entry.name,
                confirmLabel = "Rename",
                onConfirm = { name ->
                    viewModel.rename(entry, name)
                    renameEntry = null
                },
                onDismiss = { renameEntry = null },
            )
        }
        if (showNewFolder) {
            NameDialog(
                title = "New folder",
                initial = "",
                confirmLabel = "Create",
                onConfirm = { name ->
                    viewModel.createFolder(name)
                    showNewFolder = false
                },
                onDismiss = { showNewFolder = false },
            )
        }

        // Vault PIN setup / entry
        pinMode?.let { mode ->
            PinDialog(
                setup = mode == PinMode.SETUP,
                onVerify = { pin ->
                    val ok = if (mode == PinMode.SETUP) {
                        viewModel.setPin(pin)
                        true
                    } else {
                        viewModel.verifyPin(pin)
                    }
                    if (ok) {
                        viewModel.setVaultUnlocked(true)
                        pinMode = null
                    }
                    ok
                },
                onDismiss = { pinMode = null },
            )
        }
    }
}

private enum class PinMode { SETUP, ENTER }
