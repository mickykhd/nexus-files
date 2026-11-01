package com.nexus.files.data

import java.io.File
import java.util.Locale

enum class FileKind {
    FOLDER, IMAGE, VIDEO, AUDIO, PDF, DOC, PRESENTATION, APK, ARCHIVE, CODE, KEY, TEXT, OTHER
}

data class FileEntry(
    val file: File,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val modified: Long,
    val ext: String,
    val kind: FileKind,
) {
    companion object {
        fun of(file: File): FileEntry {
            val ext = file.extension.lowercase(Locale.ROOT)
            return FileEntry(
                file = file,
                name = file.name,
                isDirectory = file.isDirectory,
                size = if (file.isDirectory) file.walkTopDown().filter { it.isFile }.sumOf { it.length() }.coerceAtMost(Long.MAX_VALUE) else file.length(),
                modified = file.lastModified(),
                ext = ext,
                kind = if (file.isDirectory) FileKind.FOLDER else kindOf(ext),
            )
        }

        fun kindOf(ext: String): FileKind = when (ext) {
            "png", "jpg", "jpeg", "gif", "webp", "bmp", "heic", "heif", "dng", "svg", "avif" -> FileKind.IMAGE
            "mp4", "mkv", "webm", "mov", "avi", "3gp", "m4v" -> FileKind.VIDEO
            "mp3", "wav", "flac", "aac", "ogg", "m4a", "opus" -> FileKind.AUDIO
            "pdf" -> FileKind.PDF
            "doc", "docx", "odt", "rtf", "xls", "xlsx", "csv", "pptx", "key", "numbers" ->
                if (ext == "pptx" || ext == "key") FileKind.PRESENTATION else FileKind.DOC
            "apk", "aab", "xapk" -> FileKind.APK
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz" -> FileKind.ARCHIVE
            "kt", "java", "py", "js", "ts", "cpp", "c", "h", "cs", "go", "rs", "xml", "json", "html", "css", "sh", "gradle", "fig" -> FileKind.CODE
            "pem", "key", "p12", "jks", "keystore", "gpg", "enc" -> FileKind.KEY
            "txt", "md", "log" -> FileKind.TEXT
            else -> FileKind.OTHER
        }
    }
}

enum class FileFilter(val label: String, val kinds: Set<FileKind>?) {
    ALL("All", null),
    PDFS("PDFs", setOf(FileKind.PDF)),
    ARCHIVES("Archives", setOf(FileKind.ARCHIVE)),
    IMAGES("Images", setOf(FileKind.IMAGE)),
    MEDIA("Media", setOf(FileKind.IMAGE, FileKind.VIDEO, FileKind.AUDIO)),
    DOCS("Docs", setOf(FileKind.DOC, FileKind.PRESENTATION, FileKind.TEXT)),
}

enum class SortOrder(val label: String) {
    NAME("Name"), SIZE("Size"), DATE("Date Modified"), TYPE("Type")
}

/** Category split used by the storage overview card. */
data class StorageCategory(
    val label: String,
    val bytes: Long,
    val colorKey: CategoryColor,
)

enum class CategoryColor { SYSTEM, MEDIA, DOCS, SUCCESS, WARNING }

data class StorageStats(
    val totalBytes: Long,
    val usedBytes: Long,
    val appsBytes: Long,
    val mediaBytes: Long,
    val systemBytes: Long,
    val otherBytes: Long,
) {
    val freeBytes: Long get() = totalBytes - usedBytes
    val usedPercent: Int get() = if (totalBytes <= 0) 0 else ((usedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)

    fun categories(): List<StorageCategory> = listOf(
        StorageCategory("Apps", appsBytes, CategoryColor.DOCS),
        StorageCategory("Media", mediaBytes, CategoryColor.MEDIA),
        StorageCategory("System", systemBytes, CategoryColor.SYSTEM),
        StorageCategory("Other", otherBytes, CategoryColor.WARNING),
    )
}

object Formats {
    private val units = arrayOf("B", "KB", "MB", "GB", "TB")

    fun size(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        var value = bytes.toDouble()
        var unit = 0
        while (value >= 1024 && unit < units.size - 1) {
            value /= 1024
            unit++
        }
        val text = if (value >= 100) "%.0f" else if (value >= 10) "%.1f" else "%.1f"
        return String.format(Locale.ROOT, "%s %s", String.format(Locale.ROOT, text, value), units[unit])
    }

    fun date(millis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - millis
        val day = 24 * 60 * 60 * 1000L
        return when {
            diff < 60 * 1000L -> "Just now"
            diff < 60 * 60 * 1000L -> "${diff / (60 * 1000L)} min ago"
            isToday(millis) -> "Today, " + time(millis)
            diff < 2 * day -> "Yesterday"
            diff < 7 * day -> "${diff / day} days ago"
            else -> shortDate(millis)
        }
    }

    fun time(millis: Long = System.currentTimeMillis()): String {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val min = cal.get(java.util.Calendar.MINUTE)
        val amPm = if (hour < 12) "AM" else "PM"
        val h12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.ROOT, "%02d:%02d %s", h12, min, amPm)
    }

    fun shortDate(millis: Long): String {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        val month = cal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.SHORT, Locale.ROOT) ?: ""
        return "$month ${cal.get(java.util.Calendar.DAY_OF_MONTH)}, ${cal.get(java.util.Calendar.YEAR)}"
    }

    private fun isToday(millis: Long): Boolean {
        val a = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        val b = java.util.Calendar.getInstance()
        return a.get(java.util.Calendar.YEAR) == b.get(java.util.Calendar.YEAR) &&
            a.get(java.util.Calendar.DAY_OF_YEAR) == b.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
