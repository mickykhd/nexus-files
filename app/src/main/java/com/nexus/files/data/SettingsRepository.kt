package com.nexus.files.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nexus.files.ui.theme.AccentPalette
import com.nexus.files.ui.theme.ContrastLevel
import com.nexus.files.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nexus_settings")

data class AppearanceSettings(
    val mode: ThemeMode = ThemeMode.DYNAMIC,
    val palette: AccentPalette = AccentPalette.CYAN,
    val contrast: ContrastLevel = ContrastLevel.HIGH,
    val monochromeIcons: Boolean = true,
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val MODE = stringPreferencesKey("theme_mode")
        val PALETTE = stringPreferencesKey("accent_palette")
        val CONTRAST = intPreferencesKey("surface_contrast")
        val MONO = booleanPreferencesKey("mono_icons")
        val PIN_HASH = stringPreferencesKey("vault_pin_hash")
        val PIN_SALT = stringPreferencesKey("vault_pin_salt")
        val AUTO_DESTRUCT = booleanPreferencesKey("vault_auto_destruct")
        val BIOMETRIC_REAUTH = booleanPreferencesKey("vault_biometric_reauth")
    }

    val appearance: Flow<AppearanceSettings> = context.dataStore.data.map { prefs ->
        AppearanceSettings(
            mode = prefs[Keys.MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.DYNAMIC,
            palette = prefs[Keys.PALETTE]?.let { runCatching { AccentPalette.valueOf(it) }.getOrNull() } ?: AccentPalette.CYAN,
            contrast = ContrastLevel.fromRaw(prefs[Keys.CONTRAST] ?: ContrastLevel.HIGH.raw),
            monochromeIcons = prefs[Keys.MONO] ?: true,
        )
    }

    suspend fun saveAppearance(settings: AppearanceSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MODE] = settings.mode.name
            prefs[Keys.PALETTE] = settings.palette.name
            prefs[Keys.CONTRAST] = settings.contrast.raw
            prefs[Keys.MONO] = settings.monochromeIcons
        }
    }

    val autoDestruct: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_DESTRUCT] ?: true }
    val biometricReauth: Flow<Boolean> = context.dataStore.data.map { it[Keys.BIOMETRIC_REAUTH] ?: true }

    suspend fun setAutoDestruct(value: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_DESTRUCT] = value }
    }

    suspend fun setBiometricReauth(value: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_REAUTH] = value }
    }

    suspend fun hasPin(): Boolean = context.dataStore.data.first().contains(Keys.PIN_HASH)

    fun pinFlow(): Flow<Boolean> = context.dataStore.data.map { it.contains(Keys.PIN_HASH) }

    suspend fun setPin(pin: String) {
        val salt = java.util.UUID.randomUUID().toString().take(8)
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_SALT] = salt
            prefs[Keys.PIN_HASH] = hashPin(pin, salt)
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = context.dataStore.data.first()
        val salt = prefs[Keys.PIN_SALT] ?: return false
        val hash = prefs[Keys.PIN_HASH] ?: return false
        return hashPin(pin, salt) == hash
    }

    private fun hashPin(pin: String, salt: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest("$salt:$pin".toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
