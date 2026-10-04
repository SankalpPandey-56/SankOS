package com.sankos.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sankDataStore by preferencesDataStore(name = "sankos_prefs")

/** Clock presentation on the home screen. */
enum class ClockStyle { BIG, DOT }

data class SankPrefs(
    val clockStyle: ClockStyle = ClockStyle.BIG,
    val accentEnabled: Boolean = true,
    val use24Hour: Boolean = false,
    val hasSeenDrawerHint: Boolean = false,
)

/** User preferences, persisted locally in DataStore. */
class PrefsRepository(private val context: Context) {

    private object Keys {
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val ACCENT_ENABLED = booleanPreferencesKey("accent_enabled")
        val USE_24H = booleanPreferencesKey("use_24h")
        val DRAWER_HINT_SEEN = booleanPreferencesKey("drawer_hint_seen")
    }

    val prefs: Flow<SankPrefs> = context.sankDataStore.data.map { p ->
        SankPrefs(
            clockStyle = p[Keys.CLOCK_STYLE]?.let { stored ->
                runCatching { ClockStyle.valueOf(stored) }.getOrNull()
            } ?: ClockStyle.BIG,
            accentEnabled = p[Keys.ACCENT_ENABLED] ?: true,
            use24Hour = p[Keys.USE_24H] ?: false,
            hasSeenDrawerHint = p[Keys.DRAWER_HINT_SEEN] ?: false,
        )
    }

    suspend fun setClockStyle(style: ClockStyle) {
        context.sankDataStore.edit { it[Keys.CLOCK_STYLE] = style.name }
    }

    suspend fun setAccentEnabled(enabled: Boolean) {
        context.sankDataStore.edit { it[Keys.ACCENT_ENABLED] = enabled }
    }

    suspend fun setUse24Hour(enabled: Boolean) {
        context.sankDataStore.edit { it[Keys.USE_24H] = enabled }
    }

    suspend fun setDrawerHintSeen() {
        context.sankDataStore.edit { it[Keys.DRAWER_HINT_SEEN] = true }
    }
}
