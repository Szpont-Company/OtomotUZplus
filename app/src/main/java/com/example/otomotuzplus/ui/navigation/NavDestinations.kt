/**
 * @file NavDestinations.kt
 * @brief Enum nawigacyjny AppDestinations i model elementu paska nawigacji.
 */
package com.example.otomotuzplus.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Główne miejsca docelowe nawigacji wyświetlane na dolnym pasku nawigacyjnym.
 *
 * `MainActivity` używa tego enuma jako głównego stanu nawigacji; steruje
 * zarówno wyświetlanym ekranem jak i zaznaczonym elementem paska nav.
 * Ekrany nakładkowe ([com.example.otomotuzplus.ui.screens.details.AdDetailScreen]
 * i [com.example.otomotuzplus.ui.screens.settings.SettingsScreen]) nie są
 * tu reprezentowane — kontrolowane osobnym stanem boolowskim lub nullable.
 */
enum class AppDestinations { HOME, SEARCH, ADD, FAVORITES, PROFILE }

/**
 * Deskryptor pojedynczego elementu w [androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold].
 *
 * @property destination [AppDestinations] do którego nawiguje ten element.
 * @property label       Zlokalizowana etykieta wyświetlana pod ikoną.
 * @property selectedIcon   Ikona renderowana gdy miejsce docelowe jest aktywne.
 * @property unselectedIcon Ikona renderowana gdy miejsce docelowe jest nieaktywne.
 */
data class NavigationItem(
    val destination: AppDestinations,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
