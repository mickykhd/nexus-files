package com.nexus.files.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

data class DuplicateGroup(
    val sizeBytes: Long,
    val files: List<FileEntry>,
    val matchPercent: Int = 100,
) {
    val wastedBytes: Long get() = sizeBytes * (files.size - 1)
}

data class CleanReport(
    val cacheFiles: List<FileEntry>,
    val cacheBytes: Long,
    val duplicateGroups: List<DuplicateGroup>,
    val duplicateBytes: Long,
    val largeFiles: List<FileEntry>,
    val apkFiles: List<FileEntry>,
    val apkBytes: Long,
    val oldScreens: List<FileEntry>,
    val oldScreenBytes: Long,
) {
    val largeBytes: Long get() = largeFiles.sumOf { it.size }
    val totalRecoverable: Long
        get() = cacheBytes + duplicateBytes + largeBytes + apkBytes + oldScreenBytes

    companion object {
        val EMPTY = CleanReport(emptyList(), 0, emptyList(), 0, emptyList(), emptyList(), 0, emptyList(), 0)
    }
}

/**
 * Real storage analysis: junk/cache patterns, byte-identical duplicates,
 * oversized files, stale APK installers and old screenshots.
 */
class CleanerRepository(private val files: FileRepository) {

    private val junkNames = setOf("thumbs.db", ".ds_store", "desktop.ini")
    private val junkExt = setOf("tmp", "temp", "log", "dmp", "part", "crdownload", "cache")
    private val junkDirs = setOf(".thumbnails", "thumbnails", "cache", ".cache", "temp", "tmp")

    suspend fun scan(onProgress: (Float) -> Unit = {}): CleanReport = withContext(Dispatchers.IO) {
        coroutineScope {
            val root = files.root
            val all = mutableListOf<FileEntry>()
            collect(root, 0, all)
            onProgress(0.45f)

            val cacheDeferred = async {
                all.filter { isJunk(it) }.sortedByDescending { it.size }
            }
            val dupesDeferred = async {
                findDuplicates(all).also { onProgress(0.7f) }
            }
            val largeDeferred = async {
                all.filter {
                    it.size >= LARGE_THRESHOLD && !it.file.absolutePath.contains("/.nexus")
                }.sortedByDescending { it.size }
            }
            val apkDeferred = async {
                all.filter { it.kind == FileKind.APK }.sortedByDescending { it.size }
            }
            val screensDeferred = async {
                files.screenshotCandidates()
                    .filter { System.currentTimeMillis() - it.modified > SIX_MONTHS }
                    .sortedByDescending { it.size }
            }

            val cache = cacheDeferred.await()
            val dupes = dupesDeferred.await()
            val large = largeDeferred.await()
            val apks = apkDeferred.await()
            val screens = screensDeferred.await()
            onProgress(1f)

            CleanReport(
                cacheFiles = cache,
                cacheBytes = cache.sumOf { it.size },
                duplicateGroups = dupes,
                duplicateBytes = dupes.sumOf { it.wastedBytes },
                largeFiles = large,
                apkFiles = apks,
                apkBytes = apks.sumOf { it.size },
                oldScreens = screens,
                oldScreenBytes = screens.sumOf { it.size },
            )
        }
    }

    fun cleanCache(entries: List<FileEntry>): Int {
        var n = 0
        entries.forEach { if (it.file.delete()) n++ }
        return n
    }

    fun deleteAll(entries: List<FileEntry>): Int {
        var n = 0
        entries.forEach { if (it.file.deleteRecursively()) n++ }
        return n
    }

    fun deleteDuplicateCopies(groups: List<DuplicateGroup>): Int {
        var n = 0
        groups.forEach { group ->
            // Auto-keep the newest (highest quality capture) copy, delete the rest.
            val keep = group.files.maxByOrNull { it.modified }
            group.files.filter { it != keep }.forEach { if (it.file.delete()) n++ }
        }
        return n
    }

    /** Depth-limited walk across shared storage. */
    private fun collect(dir: File, depth: Int, out: MutableList<FileEntry>, maxDepth: Int = 6) {
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (child.name.startsWith(".") || child.absolutePath.contains("/.nexus")) continue
            if (child.isDirectory) {
                if (depth < maxDepth) collect(child, depth + 1, out, maxDepth)
            } else {
                out += FileEntry.of(child)
            }
        }
    }

    private fun isJunk(entry: FileEntry): Boolean {
        if (entry.file.absolutePath.contains("/.nexus")) return false
        val lower = entry.name.lowercase()
        if (lower in junkNames) return true
        if (entry.ext in junkExt) return true
        if (entry.ext == "tmp" || entry.ext == "log") return true
        val parent = entry.file.parentFile?.name?.lowercase() ?: ""
        return parent in junkDirs
    }

    private fun findDuplicates(all: List<FileEntry>): List<DuplicateGroup> {
        val candidates = all.filter { it.size >= 64 * 1024 && !it.isDirectory }
            .filterNot { it.file.absolutePath.contains("/.nexus") }
        val bySize = candidates.groupBy { it.size }.filterValues { it.size > 1 }
        val groups = mutableListOf<DuplicateGroup>()
        for ((size, entries) in bySize) {
            val byHash = entries.groupBy { partialHash(it.file) }.filterValues { it.size > 1 }
            for ((_, same) in byHash) {
                if (same.size > 1) groups += DuplicateGroup(size, same.sortedByDescending { it.modified })
            }
        }
        return groups.sortedByDescending { it.wastedBytes }
    }

    private fun partialHash(file: File): String = try {
        val digest = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(256 * 1024)
            val read = input.read(buffer)
            if (read > 0) digest.update(buffer, 0, read)
            // Include tail bytes for large files to reduce false positives
            if (file.length() > buffer.size) {
                input.skip(file.length() - buffer.size - read)
                val tail = input.read(buffer)
                if (tail > 0) digest.update(buffer, 0, tail)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    } catch (_: Exception) {
        file.absolutePath
    }

    companion object {
        const val LARGE_THRESHOLD = 500L * 1024 * 1024
        val SIX_MONTHS = 180L * 24 * 60 * 60 * 1000
    }
}
