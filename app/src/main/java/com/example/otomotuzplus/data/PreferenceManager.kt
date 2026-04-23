package com.example.otomotuzplus.data

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode { LIGHT, DARK, SYSTEM }

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val KEY_NOTIFICATIONS_REFUSED = "notifications_refused"

    fun setNotificationsRefused(refused: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_REFUSED, refused).apply()
    }

    fun wasNotificationsRefused(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_REFUSED, false)
    }
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun getThemeMode(): ThemeMode {
        val name = prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
        return ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
    }

    fun getLanguage(): String {
        return prefs.getString("language", "Polski") ?: "Polski"
    }
}
