package com.nexus.files

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.files.data.AppearanceSettings
import com.nexus.files.data.CleanReport
import com.nexus.files.data.CleanerRepository
import com.nexus.files.data.FileEntry
import com.nexus.files.data.FileFilter
import com.nexus.files.data.FileRepository
import com.nexus.files.data.Formats
import com.nexus.files.data.OverviewData
import com.nexus.files.data.SortOrder
import com.nexus.files.data.StorageVolumeInfo
import com.nexus.files.data.VaultCategory
import com.nexus.files.data.VaultEntry
import com.nexus.files.data.VaultRepository
import com.nexus.files.data.SettingsRepository
import com.nexus.files.work.CleanWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Which bottom-navigation destination is showing. */
enum class NexusTab { OVERVIEW, FILES, CLEAN, VAULT }

data class FilesUiState(
    val currentDir: File? = null,
    val entries: List<FileEntry> = emptyList(),
    val folders: List<FileEntry> = emptyList(),
    val files: List<FileEntry> = emptyList(),
    val selection: Set<String> = emptySet(),
    val filter: FileFilter = FileFilter.ALL,
    val sort: SortOrder = SortOrder.NAME,
    val gridMode: Boolean = false,
    val loading: Boolean = true,
)

data class OverviewUiState(
    val stats: com.nexus.files.data.StorageStats? = null,
    val volumes: List<StorageVolumeInfo> = emptyList(),
    val recent: List<FileEntry> = emptyList(),
    val fileCount: Int = 0,
    val quickCounts: Map<com.nexus.files.data.FileKind, Int> = emptyMap(),
    val loading: Boolean = true,
)

data class SearchUiState(
    val query: String = "",
    val results: List<FileEntry> = emptyList(),
    val searching: Boolean = false,
)

data class CleanUiState(
    val report: CleanReport? = null,
    val scanning: Boolean = true,
    val scanProgress: Float = 0f,
    val selectedLarge: Set<String> = emptySet(),
    val cleanedBytes: Long = 0L,
    val boosted: Boolean = false,
    val autoCleanScheduled: Boolean = false,
    val nextCleanAt: Long? = null,
)

data class VaultUiState(
    val entries: List<VaultEntry> = emptyList(),
    val categoryStats: Map<VaultCategory, Pair<Int, Long>> = emptyMap(),
    val selection: Set<String> = emptySet(),
    val unlocked: Boolean = false,
    val hasPin: Boolean = false,
    val failedAttempts: Int = 0,
    val autoDestruct: Boolean = true,
    val biometricReauth: Boolean = true,
    val keyAttestation: String = "TEE Key",
    val busy: Boolean = false,
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val fileRepo = FileRepository(app)
    private val cleanRepo = CleanerRepository(fileRepo)
    val vaultRepo = VaultRepository(app)
    private val settingsRepo = SettingsRepository(app)

    val filesRepo: FileRepository get() = fileRepo

    private val _appearance = MutableStateFlow(AppearanceSettings())
    val appearance: StateFlow<AppearanceSettings> = _appearance.asStateFlow()

    private val _storageAccess = MutableStateFlow(fileRepo.hasStorageAccess())
    val storageAccess: StateFlow<Boolean> = _storageAccess.asStateFlow()

    private val _overview = MutableStateFlow(OverviewUiState())
    val overview: StateFlow<OverviewUiState> = _overview.asStateFlow()

    private val _search = MutableStateFlow(SearchUiState())
    val search: StateFlow<SearchUiState> = _search.asStateFlow()

    private val _files = MutableStateFlow(FilesUiState(currentDir = fileRepo.root))
    val files: StateFlow<FilesUiState> = _files.asStateFlow()

    private val _clean = MutableStateFlow(CleanUiState())
    val clean: StateFlow<CleanUiState> = _clean.asStateFlow()

    private val _vault = MutableStateFlow(VaultUiState(keyAttestation = vaultRepo.keyAttestation()))
    val vault: StateFlow<VaultUiState> = _vault.asStateFlow()

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepo.appearance.collect { _appearance.value = it }
        }
        viewModelScope.launch {
            settingsRepo.pinFlow().collect { pin -> _vault.update { it.copy(hasPin = pin) } }
        }
        viewModelScope.launch {
            settingsRepo.autoDestruct.collect { v -> _vault.update { it.copy(autoDestruct = v) } }
        }
        viewModelScope.launch {
            settingsRepo.biometricReauth.collect { v -> _vault.update { it.copy(biometricReauth = v) } }
        }
        refreshScheduleState()
        refreshAll()
    }

    fun notify(message: String) {
        _toast.value = message
    }

    fun clearToast() {
        _toast.value = null
    }

    /** Re-checks storage access (called whenever the app returns to foreground). */
    fun refreshPermissions() {
        val granted = fileRepo.hasStorageAccess()
        val changed = granted != _storageAccess.value
        _storageAccess.value = granted
        if (granted && changed) refreshAll()
    }

    fun storageAccessIntent() = fileRepo.storageAccessIntent()
    fun needsLegacyPermissions() = fileRepo.needsLegacyPermissions()
    fun legacyPermissions() = fileRepo.legacyPermissions()

    fun refreshAll() {
        if (!_storageAccess.value) {
            _overview.update { it.copy(loading = false) }
            _clean.update { it.copy(scanning = false) }
            return
        }
        refreshOverview()
        openDirectory(_files.value.currentDir ?: fileRepo.root)
        refreshCleaner()
        refreshVault()
    }

    // ---------------- Appearance ----------------

    fun applyAppearance(settings: AppearanceSettings) {
        _appearance.value = settings
        viewModelScope.launch { settingsRepo.saveAppearance(settings) }
    }

    // ---------------- Overview & search ----------------

    fun refreshOverview() {
        viewModelScope.launch {
            _overview.update { it.copy(loading = true) }
            val data = withContext(Dispatchers.IO) { fileRepo.overviewData() }
            val volumes = withContext(Dispatchers.IO) { fileRepo.volumes() }
            _overview.value = OverviewUiState(
                stats = data.stats,
                volumes = volumes,
                recent = data.recent,
                fileCount = data.totalFiles,
                quickCounts = data.counts,
                loading = false,
            )
        }
    }

    fun search(query: String) {
        _search.update { it.copy(query = query, searching = query.isNotBlank()) }
        if (query.isBlank()) {
            _search.value = SearchUiState()
            return
        }
        viewModelScope.launch {
            val results = withContext(Dispatchers.IO) { fileRepo.search(query) }
            _search.update { it.copy(results = results, searching = false) }
        }
    }

    fun clearSearch() {
        _search.value = SearchUiState()
    }

    // ---------------- Files ----------------

    fun openDirectory(dir: File) {
        viewModelScope.launch {
            _files.update { it.copy(currentDir = dir, loading = true, selection = emptySet()) }
            val entries = withContext(Dispatchers.IO) {
                fileRepo.listing(dir, _files.value.filter, _files.value.sort)
            }
            _files.update {
                it.copy(
                    entries = entries,
                    folders = entries.filter { e -> e.isDirectory },
                    files = entries.filter { e -> !e.isDirectory },
                    loading = false,
                )
            }
        }
    }

    fun goUp(): Boolean {
        val current = _files.value.currentDir ?: return false
        val parent = current.parentFile ?: return false
        if (current.absolutePath == fileRepo.root.absolutePath) return false
        openDirectory(parent)
        return true
    }

    fun setFilter(filter: FileFilter) {
        _files.update { it.copy(filter = filter) }
        _files.value.currentDir?.let(::openDirectory)
    }

    fun setSort(sort: SortOrder) {
        _files.update { it.copy(sort = sort) }
        _files.value.currentDir?.let(::openDirectory)
    }

    fun toggleGrid() {
        _files.update { it.copy(gridMode = !it.gridMode) }
    }

    fun toggleSelection(entry: FileEntry) {
        _files.update {
            val sel = if (entry.file.absolutePath in it.selection) it.selection - entry.file.absolutePath
            else it.selection + entry.file.absolutePath
            it.copy(selection = sel)
        }
    }

    fun clearSelection() = _files.update { it.copy(selection = emptySet()) }

    fun deleteSelection() {
        val paths = _files.value.selection
        var removed = 0
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                paths.forEach { path ->
                    val entry = FileEntry.of(File(path))
                    if (fileRepo.delete(entry)) removed++
                }
            }
            notify("$removed item(s) deleted")
            clearSelection()
            _files.value.currentDir?.let(::openDirectory)
            refreshOverview()
            refreshCleaner()
        }
    }

    fun createFolder(name: String) {
        val dir = _files.value.currentDir ?: return
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { fileRepo.createFolder(dir, name) }
            notify(if (ok) "Folder \"$name\" created" else "Could not create folder")
            openDirectory(dir)
        }
    }

    fun rename(entry: FileEntry, newName: String) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { fileRepo.rename(entry, newName) }
            notify(if (ok) "Renamed to \"$newName\"" else "Rename failed")
            _files.value.currentDir?.let(::openDirectory)
        }
    }

    fun delete(entry: FileEntry) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { fileRepo.delete(entry) }
            notify(if (ok) "${entry.name} deleted" else "Delete failed")
            _files.value.currentDir?.let(::openDirectory)
            refreshOverview()
            refreshCleaner()
        }
    }

    fun open(entry: FileEntry) {
        if (entry.isDirectory) openDirectory(entry.file) else fileRepo.open(entry)
    }

    fun share(entry: FileEntry) = fileRepo.share(entry)

    fun moveToVault(entry: FileEntry) {
        viewModelScope.launch {
            _vault.update { it.copy(busy = true) }
            val source = if (entry.isDirectory) {
                entry.file.walkTopDown().filter { it.isFile }.firstOrNull()
            } else entry.file
            if (source == null) {
                notify("Nothing to seal")
                _vault.update { it.copy(busy = false) }
                return@launch
            }
            val deadline = if (_vault.value.autoDestruct) {
                System.currentTimeMillis() + VaultRepository.AUTO_DESTRUCT_WINDOW_MS
            } else null
            withContext(Dispatchers.IO) { vaultRepo.seal(source, autoDestructAt = deadline) }
            _vault.update { it.copy(busy = false) }
            notify("${entry.name} sealed in vault")
            _files.value.currentDir?.let(::openDirectory)
            refreshOverview()
            refreshVault()
        }
    }

    // ---------------- Cleaner ----------------

    fun refreshCleaner() {
        viewModelScope.launch {
            _clean.update { it.copy(scanning = true, scanProgress = 0f) }
            val report = withContext(Dispatchers.IO) {
                cleanRepo.scan { p -> _clean.update { it.copy(scanProgress = p) } }
            }
            _clean.update {
                it.copy(
                    report = report,
                    scanning = false,
                    selectedLarge = report.largeFiles.take(3).map { e -> e.file.absolutePath }.toSet(),
                )
            }
        }
    }

    fun toggleLarge(path: String) {
        _clean.update {
            val sel = if (path in it.selectedLarge) it.selectedLarge - path else it.selectedLarge + path
            it.copy(selectedLarge = sel)
        }
    }

    fun selectAllLarge() {
        val all = _clean.value.report?.largeFiles.orEmpty().map { it.file.absolutePath }.toSet()
        _clean.update { it.copy(selectedLarge = all) }
    }

    fun deselectLarge() = _clean.update { it.copy(selectedLarge = emptySet()) }

    fun cleanCache() {
        viewModelScope.launch {
            val report = _clean.value.report ?: return@launch
            val freed = withContext(Dispatchers.IO) {
                cleanRepo.cleanCache(report.cacheFiles)
                report.cacheBytes
            }
            _clean.update { it.copy(cleanedBytes = it.cleanedBytes + freed, boosted = true) }
            notify("Cache purged — ${Formats.size(freed)} reclaimed")
            refreshCleaner()
            refreshOverview()
        }
    }

    fun deleteDuplicateCopies() {
        viewModelScope.launch {
            val report = _clean.value.report ?: return@launch
            val freed = report.duplicateBytes
            withContext(Dispatchers.IO) { cleanRepo.deleteDuplicateCopies(report.duplicateGroups) }
            _clean.update { it.copy(cleanedBytes = it.cleanedBytes + freed, boosted = true) }
            notify("Duplicate copies removed")
            refreshCleaner()
            refreshOverview()
        }
    }

    fun deleteSelectedLarge() {
        viewModelScope.launch {
            val report = _clean.value.report ?: return@launch
            val targets = report.largeFiles.filter { it.file.absolutePath in _clean.value.selectedLarge }
            val freed = targets.sumOf { it.size }
            withContext(Dispatchers.IO) { cleanRepo.deleteAll(targets) }
            _clean.update { it.copy(cleanedBytes = it.cleanedBytes + freed, selectedLarge = emptySet(), boosted = true) }
            notify("Large files deleted — ${Formats.size(freed)} reclaimed")
            refreshCleaner()
            refreshOverview()
        }
    }

    fun deleteApks() {
        viewModelScope.launch {
            val report = _clean.value.report ?: return@launch
            val freed = report.apkBytes
            withContext(Dispatchers.IO) { cleanRepo.deleteAll(report.apkFiles) }
            _clean.update { it.copy(cleanedBytes = it.cleanedBytes + freed, boosted = true) }
            notify("Stale APK installers removed")
            refreshCleaner()
            refreshOverview()
        }
    }

    fun deleteOldScreens() {
        viewModelScope.launch {
            val report = _clean.value.report ?: return@launch
            val freed = report.oldScreenBytes
            withContext(Dispatchers.IO) { cleanRepo.deleteAll(report.oldScreens) }
            _clean.update { it.copy(cleanedBytes = it.cleanedBytes + freed, boosted = true) }
            notify("Old screenshots cleared")
            refreshCleaner()
            refreshOverview()
        }
    }

    fun oneTapBoost() {
        viewModelScope.launch {
            val report = _clean.value.report ?: return@launch
            val freed = report.cacheBytes + report.duplicateBytes + report.apkBytes
            withContext(Dispatchers.IO) {
                cleanRepo.cleanCache(report.cacheFiles)
                cleanRepo.deleteDuplicateCopies(report.duplicateGroups)
                cleanRepo.deleteAll(report.apkFiles)
            }
            _clean.update { it.copy(cleanedBytes = it.cleanedBytes + freed, boosted = true) }
            notify("Boost complete — ${Formats.size(freed)} reclaimed")
            refreshCleaner()
            refreshOverview()
        }
    }

    // ---------------- Scheduled deep clean ----------------

    fun setAutoClean(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                val next = withContext(Dispatchers.IO) { CleanWorker.schedule(getApplication()) }
                _clean.update { it.copy(autoCleanScheduled = true, nextCleanAt = next) }
                notify("Weekly auto-clean scheduled")
            } else {
                withContext(Dispatchers.IO) { CleanWorker.cancel(getApplication()) }
                _clean.update { it.copy(autoCleanScheduled = false, nextCleanAt = null) }
                notify("Auto-clean disabled")
            }
        }
    }

    private fun refreshScheduleState() {
        viewModelScope.launch {
            val scheduled = withContext(Dispatchers.IO) { CleanWorker.isScheduled(getApplication()) }
            _clean.update {
                it.copy(autoCleanScheduled = scheduled, nextCleanAt = if (scheduled) CleanWorker.nextRunAt() else null)
            }
        }
    }

    // ---------------- Vault ----------------

    fun refreshVault() {
        viewModelScope.launch {
            val purged = withContext(Dispatchers.IO) {
                val n = vaultRepo.purgeExpired()
                if (n > 0) notify("$n sealed artifact(s) auto-destructed")
                n
            }
            val entries = withContext(Dispatchers.IO) { vaultRepo.entries() }
            val stats = withContext(Dispatchers.IO) { vaultRepo.categoryStats() }
            _vault.update {
                it.copy(
                    entries = entries.sortedByDescending { e -> e.addedAt },
                    categoryStats = stats,
                    selection = emptySet(),
                )
            }
        }
    }

    fun setVaultUnlocked(value: Boolean) {
        _vault.update { it.copy(unlocked = value, failedAttempts = if (value) 0 else it.failedAttempts) }
    }

    fun recordFailedAttempt() {
        _vault.update { it.copy(failedAttempts = it.failedAttempts + 1) }
    }

    fun selectVaultEntry(entry: VaultEntry?) {
        _vault.update { it.copy(selection = if (entry == null) emptySet() else setOf(entry.id)) }
    }

    fun selectedVaultEntry(): VaultEntry? =
        _vault.value.entries.firstOrNull { it.id in _vault.value.selection }

    fun moveVaultToPublic(entry: VaultEntry) {
        viewModelScope.launch {
            _vault.update { it.copy(busy = true) }
            val dest = withContext(Dispatchers.IO) {
                val dir = File(fileRepo.root, "Download")
                dir.mkdirs()
                vaultRepo.unseal(entry, dir)
            }
            _vault.update { it.copy(busy = false, selection = emptySet()) }
            notify("${dest.name} restored to Download")
            refreshVault()
            refreshOverview()
        }
    }

    fun wipeVaultEntry(entry: VaultEntry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { vaultRepo.wipe(entry) }
            notify("Artifact permanently wiped")
            refreshVault()
        }
    }

    fun setAutoDestruct(value: Boolean) {
        viewModelScope.launch {
            settingsRepo.setAutoDestruct(value)
            withContext(Dispatchers.IO) { vaultRepo.setAutoDestructForAll(value) }
            refreshVault()
        }
    }

    fun setBiometricReauth(value: Boolean) {
        viewModelScope.launch { settingsRepo.setBiometricReauth(value) }
    }

    fun setPin(pin: String) {
        viewModelScope.launch { settingsRepo.setPin(pin) }
    }

    suspend fun verifyPin(pin: String): Boolean = settingsRepo.verifyPin(pin)

    fun hasPinSync(): Boolean = _vault.value.hasPin

    fun context(): Context = getApplication()
}
