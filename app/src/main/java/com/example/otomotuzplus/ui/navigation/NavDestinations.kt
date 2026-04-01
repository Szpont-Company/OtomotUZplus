package com.example.otomotuzplus.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations { HOME, SEARCH, ADD, FAVORITES, PROFILE }

data class NavigationItem(
    val destination: AppDestinations,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
