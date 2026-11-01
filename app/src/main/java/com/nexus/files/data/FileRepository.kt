package com.nexus.files.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.util.Locale

/**
 * Real file access against shared storage. On Android 11+ the app uses
 * MANAGE_EXTERNAL_STORAGE (All files access); on Android 10 it uses the
 * READ/WRITE_EXTERNAL_STORAGE runtime permissions.
 */
class FileRepository(private val context: Context) {

    val root: File get() = Environment.getExternalStorageDirectory()

    // ---------------- Storage access ----------------

    fun hasStorageAccess(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
    }

    /** Intent that takes the user to the real system permission page. */
    fun storageAccessIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val specific = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:${context.packageName}"),
            )
            if (specific.resolveActivity(context.packageManager) != null) specific
            else Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        } else {
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}"),
            )
        }
    }

    /** Android 10 falls back to classic runtime permissions. */
    fun legacyPermissions(): Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    fun needsLegacyPermissions(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.R

    // ---------------- Real volumes ----------------

    /**
     * Every mounted user-visible volume (internal + removable SD cards/USB),
     * derived from the real mount points with real StatFs numbers.
     */
    fun volumes(): List<StorageVolumeInfo> {
        val roots = LinkedHashMap<String, File>()
        roots[root.absolutePath] = root
        runCatching { context.getExternalFilesDirs(null) }.getOrNull()?.forEach { dir ->
            if (dir != null) {
                val volumeRoot = File(dir.absolutePath.substringBefore("/Android"))
                roots.putIfAbsent(volumeRoot.absolutePath, volumeRoot)
            }
        }
        return roots.values.map { volume ->
            val stat = runCatching { StatFs(volume.absolutePath) }.getOrNull()
            val total = stat?.totalBytes ?: 0L
            val free = stat?.availableBytes ?: 0L
            val isPrimary = volume.absolutePath == root.absolutePath
            StorageVolumeInfo(
                name = if (isPrimary) "Internal Storage" else volumeLabel(volume),
                root = volume,
                totalBytes = total,
                freeBytes = free,
                isPrimary = isPrimary,
            )
        }.sortedByDescending { it.isPrimary }
    }

    private fun volumeLabel(volume: File): String {
        val n = volume.name
        // Removable volumes are usually mounted as their volume UUID (A1B2-C3D4)
        return if (n.matches(Regex("[A-Fa-f0-9]{4,}-[A-Fa-f0-9]{4,}"))) "SD Card · $n" else n
    }

    // ---------------- Listing & metadata ----------------

    fun listing(dir: File, filter: FileFilter, sort: SortOrder): List<FileEntry> {
        val children = dir.listFiles()?.map { FileEntry.of(it) } ?: emptyList()
        val filtered = if (filter.kinds == null) children else children.filter {
            it.isDirectory || it.kind in filter.kinds
        }
        val comparator = when (sort) {
            SortOrder.NAME -> compareBy<FileEntry> { it.name.lowercase(Locale.ROOT) }
            SortOrder.SIZE -> compareByDescending { it.size }
            SortOrder.DATE -> compareByDescending { it.modified }
            SortOrder.TYPE -> compareBy<FileEntry> { it.kind.ordinal }.thenBy { it.name.lowercase(Locale.ROOT) }
        }
        return filtered.sortedWith(compareByDescending<FileEntry> { it.isDirectory }.then(comparator))
    }

    /**
     * One single walk of shared storage that computes everything the Overview
     * needs: category bytes, per-kind counts, recent files and total count.
     */
    fun overviewData(): OverviewData {
        var media = 0L
        var docs = 0L
        var apps = 0L
        var other = 0L
        var total = 0
        val counts = mutableMapOf(
            FileKind.IMAGE to 0, FileKind.VIDEO to 0, FileKind.AUDIO to 0,
            FileKind.PDF to 0, FileKind.DOC to 0, FileKind.PRESENTATION to 0,
            FileKind.APK to 0, FileKind.ARCHIVE to 0,
        )
        val recent = ArrayList<FileEntry>()

        walk(root) { entry ->
            total++
            when (entry.kind) {
                FileKind.IMAGE, FileKind.VIDEO, FileKind.AUDIO -> media += entry.size
                FileKind.DOC, FileKind.PDF, FileKind.PRESENTATION, FileKind.TEXT, FileKind.CODE, FileKind.KEY -> docs += entry.size
                FileKind.APK, FileKind.ARCHIVE -> apps += entry.size
                else -> other += entry.size
            }
            counts[entry.kind]?.let { counts[entry.kind] = it + 1 }
            recent += entry
            if (recent.size > 400) {
                recent.sortByDescending { it.modified }
                while (recent.size > 300) recent.removeAt(recent.size - 1)
            }
        }
        recent.sortByDescending { it.modified }

        val stat = StatFs(root.absolutePath)
        val totalBytes = stat.totalBytes
        val used = totalBytes - stat.availableBytes
        val system = (used - (media + docs + apps + other)).coerceAtLeast(totalBytes / 60)
        return OverviewData(
            stats = StorageStats(
                totalBytes = totalBytes,
                usedBytes = used,
                appsBytes = apps,
                mediaBytes = media,
                systemBytes = system,
                otherBytes = (used - (media + docs + apps + other + system)).coerceAtLeast(0L),
            ),
            counts = counts,
            recent = recent.take(14),
            totalFiles = total,
        )
    }

    /** Real recursive search across shared storage. */
    fun search(query: String, limit: Int = 200): List<FileEntry> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase(Locale.ROOT)
        val out = ArrayList<FileEntry>()
        walk(root, maxDepth = 7) { entry ->
            if (entry.name.lowercase(Locale.ROOT).contains(q)) {
                out += entry
                if (out.size >= limit * 3) return@walk
            }
        }
        return out.sortedWith(
            compareByDescending<FileEntry> { it.name.lowercase(Locale.ROOT).startsWith(q) }
                .thenByDescending { it.modified },
        ).take(limit)
    }

    fun breadcrumbs(dir: File): List<Pair<String, File>> {
        val result = mutableListOf<Pair<String, File>>()
        var current: File? = dir
        while (current != null && current.absolutePath.startsWith(root.absolutePath)) {
            result.add(0, (if (current == root) "Internal" else current.name) to current)
            current = current.parentFile
        }
        return result
    }

    // ---------------- Operations ----------------

    fun createFolder(parent: File, name: String): Boolean {
        val target = File(parent, name)
        return !target.exists() && target.mkdirs()
    }

    fun rename(entry: FileEntry, newName: String): Boolean {
        val target = File(entry.file.parentFile, newName)
        return entry.file.renameTo(target)
    }

    fun delete(entry: FileEntry): Boolean = entry.file.deleteRecursively()

    fun share(entry: FileEntry) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", entry.file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = context.contentResolver.getType(uri) ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share ${entry.name}").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun open(entry: FileEntry) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", entry.file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, context.contentResolver.getType(uri) ?: guessMime(entry.ext))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }.onFailure {
            Toast.makeText(context, "No app available to open ${entry.name}", Toast.LENGTH_SHORT).show()
        }
    }

    fun screenshotCandidates(): List<FileEntry> {
        val dirs = listOf(
            File(root, "Pictures/Screenshots"),
            File(root, "DCIM/Screenshots"),
            File(root, "Pictures"),
        )
        return dirs.filter { it.isDirectory }.flatMap { dir ->
            dir.listFiles()
                ?.filter { it.isFile && FileEntry.kindOf(it.extension.lowercase(Locale.ROOT)) == FileKind.IMAGE }
                ?.map { FileEntry.of(it) } ?: emptyList()
        }
    }

    private fun guessMime(ext: String): String = when (ext) {
        "pdf" -> "application/pdf"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "mp4" -> "video/mp4"
        "mkv" -> "video/x-matroska"
        "mp3" -> "audio/mpeg"
        "zip" -> "application/zip"
        "apk" -> "application/vnd.android.package-archive"
        "txt" -> "text/plain"
        else -> "*/*"
    }

    /** Walks shared storage with a depth limit, skipping hidden & app-private dirs. */
    private fun walk(dir: File, depth: Int = 0, maxDepth: Int = 5, onEach: (FileEntry) -> Unit) {
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (child.name.startsWith(".") || child.absolutePath.contains("/Android/data") ||
                child.absolutePath.contains("/Android/obb")
            ) continue
            if (child.isDirectory) {
                if (depth < maxDepth) walk(child, depth + 1, maxDepth, onEach)
            } else {
                onEach(FileEntry.of(child))
            }
        }
    }
}
