/**
 * @file PreferenceManager.kt
 * @brief Wrapper SharedPreferences przechowujący motyw i język aplikacji.
 */
package com.example.otomotuzplus.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Wybór motywu aplikacji.
 *
 * - [LIGHT]  zawsze używa jasnego schematu kolorów Material3.
 * - [DARK]   zawsze używa ciemnego schematu kolorów Material3.
 * - [SYSTEM] podąża za ustawieniem trybu ciemnego systemu operacyjnego
 *   przez [androidx.compose.foundation.isSystemInDarkTheme].
 */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

/**
 * Cienka nakładka na [SharedPreferences] przechowująca preferencje użytkownika
 * między restartami aplikacji.
 *
 * Wszystkie zapisy używają `apply()` (asynchronicznie), więc nie blokują wątku
 * głównego. Plik preferencji nosi nazwę `"app_prefs"`.
 *
 * @param context Kontekst aplikacji lub Activity używany do otwarcia pliku preferencji.
 */
class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val KEY_NOTIFICATIONS_REFUSED = "notifications_refused"

    /**
     * Zapisuje informację czy użytkownik jawnie odrzucił prośbę o uprawnienie do powiadomień.
     *
     * Gdy `true`, okno dialogowe uzasadnienia w [OtomotUZplusApp] jest pomijane przez
     * cały pozostały czas życia aplikacji na tym urządzeniu.
     *
     * @param refused `true` jeśli użytkownik kliknął "Może później" w oknie uprawnień.
     */
    fun setNotificationsRefused(refused: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_REFUSED, refused).apply()
    }

    /**
     * @return `true` jeśli użytkownik wcześniej odrzucił prośbę o uprawnienie do powiadomień.
     */
    fun wasNotificationsRefused(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_REFUSED, false)
    }
    /**
     * Zapisuje wybrany [ThemeMode].
     * @param mode Motyw do zastosowania przy następnej rekompozycji.
     */
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    /**
     * @return Zapisany [ThemeMode]; domyślnie [ThemeMode.SYSTEM] jeśli nie ustawiono.
     */
    fun getThemeMode(): ThemeMode {
        val name = prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
        return ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
    }

    /**
     * Zapisuje wybrany język interfejsu.
     * @param lang Identyfikator języka — `"Polski"` lub `"English"`.
     */
    fun setLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
    }

    /**
     * @return Zapisany identyfikator języka; domyślnie `"Polski"` jeśli nie ustawiono.
     */
    fun getLanguage(): String {
        return prefs.getString("language", "Polski") ?: "Polski"
    }
}
