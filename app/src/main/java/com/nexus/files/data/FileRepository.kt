package com.nexus.files.data

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Real file access against shared storage. The app uses MANAGE_EXTERNAL_STORAGE
 * (All files access) so it behaves like a true device file manager.
 */
class FileRepository(private val context: Context) {

    val root: File get() = Environment.getExternalStorageDirectory()

    fun hasAllFilesAccess(): Boolean = Environment.isExternalStorageManager()

    fun listing(dir: File): List<FileEntry> = withContextCompat {
        val children = dir.listFiles()?.map { FileEntry.of(it) } ?: emptyList()
        children.sortedWith(
            compareByDescending<FileEntry> { it.isDirectory }
                .thenBy { it.name.lowercase(Locale.ROOT) },
        )
    }

    fun listing(dir: File, filter: FileFilter, sort: SortOrder): List<FileEntry> = withContextCompat {
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
        filtered.sortedWith(compareByDescending<FileEntry> { it.isDirectory }.then(comparator))
    }

    fun storageStats(): StorageStats = withContextCompat {
        val stat = StatFs(root.absolutePath)
        val total = stat.totalBytes
        val free = stat.availableBytes
        val used = total - free

        var media = 0L
        var docs = 0L
        var apps = 0L
        var other = 0L
        scan(root) { entry ->
            when (entry.kind) {
                FileKind.IMAGE, FileKind.VIDEO, FileKind.AUDIO -> media += entry.size
                FileKind.DOC, FileKind.PDF, FileKind.PRESENTATION, FileKind.TEXT, FileKind.CODE, FileKind.KEY -> docs += entry.size
                FileKind.APK, FileKind.ARCHIVE -> apps += entry.size
                else -> other += entry.size
            }
        }
        val system = (used - (media + docs + apps + other)).coerceAtLeast(total / 40)
        StorageStats(
            totalBytes = total,
            usedBytes = used,
            appsBytes = apps,
            mediaBytes = media,
            systemBytes = system,
            otherBytes = other + (used - (media + docs + apps + other + system)).coerceAtLeast(0L),
        )
    }

    fun recentFiles(limit: Int = 12): List<FileEntry> = withContextCompat {
        val out = mutableListOf<FileEntry>()
        listOf("Download", "DCIM", "Documents", "Movies", "Music", "Pictures", "Movies").map { File(root, it) }
            .filter { it.isDirectory }.forEach { dir ->
                dir.walkTopDown()
                    .maxDepth(3)
                    .filter { it.isFile && !it.name.startsWith(".") && !it.absolutePath.contains("/.nexus") }
                    .forEach { out += FileEntry.of(it) }
            }
        out.sortedByDescending { it.modified }.take(limit)
    }

    fun quickAccessCounts(): Map<FileKind, Int> = withContextCompat {
        val counts = mutableMapOf(
            FileKind.IMAGE to 0, FileKind.VIDEO to 0, FileKind.AUDIO to 0,
            FileKind.PDF to 0, FileKind.APK to 0, FileKind.ARCHIVE to 0,
        )
        scan(root) { entry ->
            val c = counts[entry.kind]
            if (c != null) counts[entry.kind] = c + 1
        }
        counts
    }

    fun totalFileCount(): Int = withContextCompat {
        var count = 0
        scan(root) { count++ }
        count
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

    fun createFolder(parent: File, name: String): Boolean {
        val target = File(parent, name)
        return !target.exists() && target.mkdirs()
    }

    fun rename(entry: FileEntry, newName: String): Boolean {
        val target = File(entry.file.parentFile, newName)
        return entry.file.renameTo(target)
    }

    fun delete(entry: FileEntry): Boolean = entry.file.deleteRecursively()

    fun sizeOf(entry: FileEntry): Long = if (entry.isDirectory) {
        entry.file.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    } else entry.file.length()

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

    fun copyTo(entry: FileEntry, destDir: File): Boolean {
        val dest = File(destDir, entry.file.name)
        return try {
            if (entry.isDirectory) entry.file.copyRecursively(dest, overwrite = false)
            else entry.file.copyTo(dest, overwrite = false)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun newFile(parent: File, name: String, content: String): File {
        val f = File(parent, name)
        f.parentFile?.mkdirs()
        f.writeText(content)
        return f
    }

    fun imageThumbnailPath(entry: FileEntry): String? =
        if (entry.kind == FileKind.IMAGE) entry.file.absolutePath else null

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

    fun mediaStoreImages(): List<String> {
        val result = mutableListOf<String>()
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.DATA),
            null, null, null,
        )
        cursor?.use {
            val idx = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) result += it.getString(idx)
        }
        return result
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

    /** Walks shared storage with a depth limit, skipping hidden & vault dirs. */
    private fun scan(dir: File, depth: Int = 0, maxDepth: Int = 5, onEach: (FileEntry) -> Unit) {
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (child.name.startsWith(".")) continue
            if (child.isDirectory) {
                if (depth < maxDepth) scan(child, depth + 1, maxDepth, onEach)
            } else {
                onEach(FileEntry.of(child))
            }
        }
    }

    private fun <T> withContextCompat(block: () -> T): T = block()
}

/** Utility for seeding formatted stamps. */
fun formatStamp(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.ROOT).format(Date(millis))
