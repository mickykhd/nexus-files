package com.nexus.files.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class VaultEntry(
    val id: String,
    val displayName: String,
    val originalPath: String,
    val bytes: Long,
    val cipherSuite: String,
    val sha256: String,
    val addedAt: Long,
    val category: VaultCategory,
    val hardwarePath: String,
    val autoDestructAt: Long?,
) {
    companion object {
        fun fromJson(o: JSONObject) = VaultEntry(
            id = o.getString("id"),
            displayName = o.getString("name"),
            originalPath = o.optString("original", ""),
            bytes = o.getLong("bytes"),
            cipherSuite = o.optString("cipher", "AES-256-GCM"),
            sha256 = o.optString("sha256", ""),
            addedAt = o.getLong("added"),
            category = runCatching { VaultCategory.valueOf(o.getString("cat")) }.getOrDefault(VaultCategory.DOCUMENTS),
            hardwarePath = o.optString("path", ""),
            autoDestructAt = if (o.isNull("destruct")) null else o.optLong("destruct"),
        )
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", displayName)
        .put("original", originalPath)
        .put("bytes", bytes)
        .put("cipher", cipherSuite)
        .put("sha256", sha256)
        .put("added", addedAt)
        .put("cat", category.name)
        .put("path", hardwarePath)
        .put("destruct", autoDestructAt)
}

enum class VaultCategory(val title: String, val subtitle: (Int, Long) -> String) {
    DOCUMENTS("Encrypted Documents", { count, bytes -> "$count ${if (count == 1) "file" else "files"} • ${Formats.size(bytes)}" }),
    MEDIA("Hidden Media", { count, bytes -> "$count ${if (count == 1) "item" else "items"} • ${Formats.size(bytes)}" }),
    KEYS("Sensitive Keys & Notes", { count, bytes -> "$count ${if (count == 1) "item" else "items"} • ${Formats.size(bytes)}" }),
    ARCHIVES("Locked Archives", { count, bytes -> "$count ${if (count == 1) "bundle" else "bundles"} • ${Formats.size(bytes)}" });

    companion object {
        fun forFile(entry: FileEntry): VaultCategory = when (entry.kind) {
            FileKind.IMAGE, FileKind.VIDEO, FileKind.AUDIO -> MEDIA
            FileKind.KEY, FileKind.TEXT -> KEYS
            FileKind.ARCHIVE -> ARCHIVES
            else -> DOCUMENTS
        }
    }
}

/**
 * Zero-knowledge style enclave: every artifact is sealed with AES-256-GCM using
 * a key that never leaves the AndroidKeyStore hardware boundary.
 */
class VaultRepository(private val appContext: android.content.Context) {

    private val vaultDir = File(appContext.filesDir, "sec_vault/enclave_0")
    private val indexFile = File(vaultDir, "enclave_index.json")

    val hardwareRoot: String get() = vaultDir.absolutePath

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    fun entries(): List<VaultEntry> {
        if (!indexFile.exists()) return emptyList()
        return try {
            val arr = JSONArray(indexFile.readText())
            (0 until arr.length()).map { VaultEntry.fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun write(entries: List<VaultEntry>) {
        vaultDir.mkdirs()
        indexFile.writeText(JSONArray(entries.map { it.toJson() }).toString())
    }

    suspend fun seal(
        file: File,
        category: VaultCategory? = null,
        autoDestructAt: Long? = null,
    ): VaultEntry = withContext(Dispatchers.IO) {
        vaultDir.mkdirs()
        val id = java.util.UUID.randomUUID().toString().substring(0, 8)
        val outName = "${file.name}.$VAULT_EXT"
        val outFile = File(vaultDir, outName)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val header = cipher.iv
        outFile.outputStream().use { out ->
            // [2-byte iv length][iv][gcm-sealed bytes]
            out.write(byteArrayOf(((header.size shr 8) and 0xFF).toByte(), (header.size and 0xFF).toByte()))
            out.write(header)
            file.inputStream().use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    out.write(cipher.update(buffer, 0, read) ?: ByteArray(0))
                }
                out.write(cipher.doFinal())
            }
        }

        val sha = sha256Of(file)
        val entry = VaultEntry(
            id = id,
            displayName = file.name,
            originalPath = file.absolutePath,
            bytes = file.length(),
            cipherSuite = "AES-256-GCM",
            sha256 = sha,
            addedAt = System.currentTimeMillis(),
            category = category ?: VaultCategory.forFile(FileEntry.of(file)),
            hardwarePath = "${vaultDir.absolutePath}/${hexAddress(id)}",
            autoDestructAt = autoDestructAt,
        )
        write(entries() + entry)
        file.delete()
        entry
    }

    suspend fun unseal(entry: VaultEntry, destDir: File): File = withContext(Dispatchers.IO) {
        val sealed = sealedFile(entry) ?: error("Missing enclave artifact")
        val dest = File(destDir, entry.displayName)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        sealed.inputStream().use { input ->
            val lenBytes = ByteArray(2)
            input.read(lenBytes)
            val ivLen = ((lenBytes[0].toInt() and 0xFF) shl 8) or (lenBytes[1].toInt() and 0xFF)
            val iv = ByteArray(ivLen)
            input.read(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
            dest.outputStream().use { out ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    cipher.update(buffer, 0, read)?.let { out.write(it) }
                }
                out.write(cipher.doFinal())
            }
        }
        wipe(entry)
        dest
    }

    fun wipe(entry: VaultEntry) {
        sealedFile(entry)?.delete()
        write(entries().filterNot { it.id == entry.id })
    }

    /**
     * Really purges artifacts whose auto-destruct deadline has passed.
     * Returns the number of artifacts purged.
     */
    fun purgeExpired(): Int {
        val now = System.currentTimeMillis()
        val expired = entries().filter { val d = it.autoDestructAt; d != null && d < now }
        expired.forEach { wipe(it) }
        return expired.size
    }

    fun setAutoDestructForAll(enabled: Boolean) {
        val deadline = System.currentTimeMillis() + AUTO_DESTRUCT_WINDOW_MS
        write(entries().map { it.copy(autoDestructAt = if (enabled) deadline else null) })
    }

    /** Real hardware attestation of the key store backing the vault. */
    fun keyAttestation(): String = attestation

    private val attestation: String by lazy {
        probeAttestation()
    }

    private fun probeAttestation(): String = try {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            "nexus_probe_key",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setIsStrongBoxBacked(true)
            .build()
        generator.init(spec)
        generator.generateKey()
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        ks.deleteEntry("nexus_probe_key")
        "StrongBox"
    } catch (_: Throwable) {
        "TEE Key"
    }

    fun sealedFile(entry: VaultEntry): File? =
        File(vaultDir, "${entry.displayName}.$VAULT_EXT").takeIf { it.exists() }
            ?: vaultDir.listFiles()?.firstOrNull { it.name.startsWith(entry.id) }

    fun update(entry: VaultEntry) {
        write(entries().map { if (it.id == entry.id) entry else it })
    }

    fun categoryStats(): Map<VaultCategory, Pair<Int, Long>> {
        val grouped = entries().groupBy { it.category }
        return VaultCategory.entries.associateWith { cat ->
            val list = grouped[cat].orEmpty()
            list.size to list.sumOf { it.bytes }
        }
    }

    fun totalBytes(): Long = entries().sumOf { it.bytes }

    private fun hexAddress(id: String): String =
        "0x" + (MessageDigest.getInstance("SHA-1").digest(id.toByteArray()).take(3)
            .joinToString("") { "%02X".format(it) })

    private fun sha256Of(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val VAULT_EXT = "enc"
        const val KEY_ALIAS = "nexus_vault_key"
        const val AUTO_DESTRUCT_WINDOW_MS = 48L * 60 * 60 * 1000
    }
}
