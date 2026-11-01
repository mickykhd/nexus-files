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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import com.nexus.files.data.FileEntry
import com.nexus.files.data.FileFilter
import com.nexus.files.data.VaultEntry
import com.nexus.files.ui.components.NexusBottomSheet
import com.nexus.files.ui.screens.CleanScreen
import com.nexus.files.ui.screens.FileActionsMenu
import com.nexus.files.ui.screens.FileInspectorContent
import com.nexus.files.ui.screens.FilesScreen
import com.nexus.files.ui.screens.NameDialog
import com.nexus.files.ui.screens.NexusBottomNav
import com.nexus.files.ui.screens.NexusToast
import com.nexus.files.ui.screens.NexusTopBar
import com.nexus.files.ui.screens.OverviewScreen
import com.nexus.files.ui.screens.PermissionScreen
import com.nexus.files.ui.screens.PinDialog
import com.nexus.files.ui.screens.SearchScreen
import com.nexus.files.ui.screens.ThemeSheetContent
import com.nexus.files.ui.screens.VaultScreen
import com.nexus.files.ui.theme.NexusTheme
import com.nexus.files.util.authenticate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusApp(viewModel: MainViewModel) {
    val appearance by viewModel.appearance.collectAsState()
    val overview by viewModel.overview.collectAsState()
    val files by viewModel.files.collectAsState()
    val clean by viewModel.clean.collectAsState()
    val vault by viewModel.vault.collectAsState()
    val toast by viewModel.toast.collectAsState()
    val search by viewModel.search.collectAsState()
    val storageAccess by viewModel.storageAccess.collectAsState()
    val context = LocalContext.current

    // Real permission state: re-checked every time the app returns to foreground.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshPermissions()
    }

    NexusTheme(
        mode = appearance.mode,
        palette = appearance.palette,
        contrast = appearance.contrast,
        monochromeIcons = appearance.monochromeIcons,
    ) {
        val canvas = com.nexus.files.ui.theme.LocalNexusExtra.current.canvas

        if (!storageAccess) {
            Column(Modifier.fillMaxSize().background(canvas)) {
                PermissionScreen(
                    needsLegacy = viewModel.needsLegacyPermissions(),
                    onOpenSettings = { context.startActivity(viewModel.storageAccessIntent()) },
                    onRecheck = { viewModel.refreshPermissions() },
                )
            }
            NexusToast(message = toast, onDone = { viewModel.clearToast() })
            return@NexusTheme
        }

        var tab by rememberSaveable { mutableStateOf(NexusTab.OVERVIEW) }
        var showThemeSheet by remember { mutableStateOf(false) }
        var menuEntry by remember { mutableStateOf<FileEntry?>(null) }
        var renameEntry by remember { mutableStateOf<FileEntry?>(null) }
        var showNewFolder by remember { mutableStateOf(false) }
        var inspectorEntry by remember { mutableStateOf<VaultEntry?>(null) }
        var pinMode by remember { mutableStateOf<PinMode?>(null) }
        var pendingVaultEntry by remember { mutableStateOf<VaultEntry?>(null) }
        var searchOpen by remember { mutableStateOf(false) }

        val themeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val inspectorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // Back: close overlays, then leave folders before leaving the app.
        val inSubFolder = tab == NexusTab.FILES &&
            files.currentDir != null &&
            files.currentDir!!.absolutePath != viewModel.filesRepo.root.absolutePath
        BackHandler(enabled = searchOpen || inSubFolder || inspectorEntry != null || showThemeSheet) {
            when {
                searchOpen -> { searchOpen = false; viewModel.clearSearch() }
                inspectorEntry != null -> inspectorEntry = null
                showThemeSheet -> showThemeSheet = false
                else -> viewModel.goUp()
            }
        }

        if (searchOpen) {
            SearchScreen(
                query = search.query,
                results = search.results,
                searching = search.searching,
                searched = search.query.isNotBlank(),
                onQueryChange = { viewModel.search(it) },
                onOpen = { entry ->
                    searchOpen = false
                    viewModel.clearSearch()
                    tab = NexusTab.FILES
                    if (entry.isDirectory) viewModel.openDirectory(entry.file) else viewModel.open(entry)
                },
                onBack = { searchOpen = false; viewModel.clearSearch() },
            )
            return@NexusTheme
        }

        Column(
            Modifier
                .fillMaxSize()
                .background(canvas),
        ) {
            when (tab) {
                NexusTab.OVERVIEW -> NexusTopBar(
                    contextChip = null,
                    onMenu = { tab = NexusTab.FILES },
                    onSettings = { showThemeSheet = true },
                    onTheme = { showThemeSheet = true },
                )
                NexusTab.FILES -> {}
                NexusTab.CLEAN -> NexusTopBar(
                    contextChip = "Cleaner",
                    onMenu = { tab = NexusTab.OVERVIEW },
                    onSettings = { showThemeSheet = true },
                    onTheme = { showThemeSheet = true },
                )
                NexusTab.VAULT -> NexusTopBar(
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
                        onSearch = { searchOpen = true },
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
                        onSearch = { searchOpen = true },
                    )
                    NexusTab.CLEAN -> CleanScreen(
                        report = clean.report,
                        scanning = clean.scanning,
                        progress = clean.scanProgress,
                        selectedLarge = clean.selectedLarge,
                        boosted = clean.boosted,
                        overviewStats = overview.stats,
                        autoCleanScheduled = clean.autoCleanScheduled,
                        nextCleanAt = clean.nextCleanAt,
                        onBoost = { viewModel.oneTapBoost() },
                        onCleanCache = { viewModel.cleanCache() },
                        onCleanDuplicates = { viewModel.deleteDuplicateCopies() },
                        onDeleteSelectedLarge = { viewModel.deleteSelectedLarge() },
                        onToggleLarge = { viewModel.toggleLarge(it) },
                        onSelectAll = { viewModel.selectAllLarge() },
                        onDeselectAll = { viewModel.deselectLarge() },
                        onDeleteApks = { viewModel.deleteApks() },
                        onDeleteScreens = { viewModel.deleteOldScreens() },
                        onAutoClean = { viewModel.setAutoClean(it) },
                    )
                    NexusTab.VAULT -> VaultScreen(
                        entries = vault.entries,
                        categoryStats = vault.categoryStats,
                        unlocked = vault.unlocked,
                        failedAttempts = vault.failedAttempts,
                        keyAttestation = vault.keyAttestation,
                        onSelectEntry = { entry -> requestVaultOpen(viewModel, vault, entry, pending = { pendingVaultEntry = it }, pin = { pinMode = it }, open = { inspectorEntry = it }) },
                        onUnlock = { requestVaultOpen(viewModel, vault, null, pending = { pendingVaultEntry = it }, pin = { pinMode = it }, open = { inspectorEntry = it }) },
                        onCategory = { viewModel.notify("Partition: ${it.title}") },
                    )
                }
            }

            NexusToast(message = toast, onDone = { viewModel.clearToast() })
            NexusBottomNav(selected = tab, onSelect = { tab = it })
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

        // Vault PIN setup / entry (real verification, real failed-attempt count)
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
                        pendingVaultEntry?.let { inspectorEntry = it }
                        pendingVaultEntry = null
                        pinMode = null
                    } else {
                        viewModel.recordFailedAttempt()
                    }
                    ok
                },
                onDismiss = { pinMode = null; pendingVaultEntry = null },
            )
        }
    }
}

/**
 * Real gatekeeping for the vault: unlock first if needed, then (when the
 * user has enabled it) demand a fresh biometric / credential check before
 * the inspector can open. Falls back to the vault PIN when the device has
 * no biometrics or screen-lock credential enrolled.
 */
private fun requestVaultOpen(
    viewModel: MainViewModel,
    state: VaultUiState,
    entry: VaultEntry?,
    pending: (VaultEntry?) -> Unit,
    pin: (PinMode) -> Unit,
    open: (VaultEntry?) -> Unit,
) {
    fun proceed() {
        if (entry != null) {
            viewModel.selectVaultEntry(entry)
            open(entry)
        } else {
            viewModel.setVaultUnlocked(true)
        }
    }

    when {
        !state.unlocked -> {
            pending(entry)
            pin(if (state.hasPin) PinMode.ENTER else PinMode.SETUP)
        }
        entry != null && state.biometricReauth -> {
            authenticate(
                context = viewModel.context(),
                title = "Confirm it's you",
                subtitle = "Biometric re-authentication to inspect ${entry.displayName}",
                onSuccess = { proceed() },
                onFailure = {
                    viewModel.recordFailedAttempt()
                    viewModel.notify("Authentication failed")
                },
                onPinFallback = {
                    pending(entry)
                    pin(if (state.hasPin) PinMode.ENTER else PinMode.SETUP)
                },
            )
        }
        else -> proceed()
    }
}

private enum class PinMode { SETUP, ENTER }
