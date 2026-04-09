@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.otomotuzplus

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.otomotuzplus.data.PreferenceManager
import com.example.otomotuzplus.data.ThemeMode
import com.example.otomotuzplus.ui.components.PlaceholderScreen
import com.example.otomotuzplus.ui.models.EnglishStrings
import com.example.otomotuzplus.ui.models.PolishStrings
import com.example.otomotuzplus.ui.navigation.AppDestinations
import com.example.otomotuzplus.ui.navigation.NavigationItem
import com.example.otomotuzplus.ui.screens.favorites.FavoritesScreen
import com.example.otomotuzplus.ui.screens.home.HomeScreen
import com.example.otomotuzplus.ui.screens.profile.ProfileScreen
import com.example.otomotuzplus.ui.screens.search.SearchScreen
import com.example.otomotuzplus.ui.screens.settings.SettingsScreen
import com.example.otomotuzplus.ui.theme.OtomotUZplusTheme
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        val prefManager = PreferenceManager(this)
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(prefManager.getThemeMode()) }
            var currentLanguage by remember { mutableStateOf(prefManager.getLanguage()) }

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            OtomotUZplusTheme(darkTheme = darkTheme) {
                OtomotUZplusApp(
                    themeMode = themeMode,
                    onThemeChange = { 
                        themeMode = it
                        prefManager.setThemeMode(it)
                    },
                    currentLanguage = currentLanguage,
                    onLanguageChange = { 
                        currentLanguage = it
                        prefManager.setLanguage(it)
                    },
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                )
            }
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Użytkownik pozwolił, powiadomienia będą działać!
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "offers_channel"
            val name = "Nowe Oferty"
            val descriptionText = "Powiadomienia o nowych autach i promocjach"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun OtomotUZplusApp(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var pendingSearchQuery by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSearchBrand by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSearchShowFilters by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var favoriteCars by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var showRationaleDialog by rememberSaveable { mutableStateOf(false) }

    val strings = if (currentLanguage == "Polski") PolishStrings else EnglishStrings
    val context = LocalContext.current

    fun openSearch(query: String? = null, brand: String? = null, showFilters: Boolean? = false) {
        pendingSearchQuery = query
        pendingSearchBrand = brand
        pendingSearchShowFilters = showFilters
        if (currentDestination != AppDestinations.SEARCH) {
            currentDestination = AppDestinations.SEARCH
        }
    }

    BackHandler(enabled = showSettings || currentDestination != AppDestinations.HOME) {
        if (showSettings) {
            showSettings = false
        } else {
            currentDestination = AppDestinations.HOME
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

            if (!isGranted) {
                showRationaleDialog = true
            }
        }
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = {
                showRationaleDialog = false
            },
            icon = { Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = {
                Text(
                    text = "Powiadomienia o okazjach",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Ciągle przegapiasz tanie Passaty? Włącz powiadomienia, aby otrzymywać info o nowych autach i promocjach!",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRationaleDialog = false
                        onRequestNotificationPermission()
                    }
                ) {
                    Text("Włącz")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog = false
                    }
                ) {
                    Text("Może później")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    val navItems = listOf(
        NavigationItem(AppDestinations.HOME, strings.home, Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem(AppDestinations.SEARCH, strings.search, Icons.Filled.Search, Icons.Outlined.Search),
        NavigationItem(AppDestinations.ADD, strings.add, Icons.Filled.AddCircle, Icons.Outlined.AddCircle),
        NavigationItem(AppDestinations.FAVORITES, strings.favorites, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        NavigationItem(AppDestinations.PROFILE, strings.profile, Icons.Filled.Person, Icons.Outlined.Person)
    )

    val navItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray,
            indicatorColor = Color.Transparent
        )
    )

    val navSuiteColors = NavigationSuiteDefaults.colors(
        navigationBarContainerColor = MaterialTheme.colorScheme.background,
        navigationBarContentColor = Color.Gray
    )

    if (showSettings) {
        SettingsScreen(
            onBack = { showSettings = false },
            themeMode = themeMode,
            onThemeChange = onThemeChange,
            currentLanguage = currentLanguage,
            onLanguageChange = onLanguageChange,
            strings = strings
        )
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                navItems.forEach { item ->
                    item(
                        icon = {
                            Icon(
                                imageVector = if (item.destination == currentDestination) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = item.destination == currentDestination,
                        onClick = { currentDestination = item.destination },
                        colors = navItemColors
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            navigationSuiteColors = navSuiteColors
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    when (currentDestination) {
                        AppDestinations.HOME -> HomeScreen(
                            strings = strings,
                            onNavigateToSearch = { openSearch() },
                            onNavigateToAdd = { currentDestination = AppDestinations.ADD },
                            onSearchSubmit = { submittedQuery ->
                                if (submittedQuery.isNotBlank()) {
                                    openSearch(query = submittedQuery)
                                }
                            },
                            onBrandSelect = { selectedBrand ->
                                openSearch(brand = selectedBrand)
                            },
                            onSeeAllClick = { openSearch() },
                            onNotificationsClick = { },
                            favoriteCars = favoriteCars,
                            onFavoriteToggle = { key ->
                                favoriteCars = if (favoriteCars.contains(key)) {
                                    favoriteCars - key
                                } else {
                                    favoriteCars + key
                                }
                            }
                        )
                        AppDestinations.SEARCH -> SearchScreen(
                            strings = strings,
                            initialQuery = pendingSearchQuery,
                            initialBrand = pendingSearchBrand,
                            initialShowFilters = pendingSearchShowFilters,
                            favoriteCars = favoriteCars,
                            onFavoriteToggle = { key ->
                                favoriteCars = if (favoriteCars.contains(key)) {
                                    favoriteCars - key
                                } else {
                                    favoriteCars + key
                                }
                            },
                            onInitialFiltersConsumed = {
                                pendingSearchQuery = null
                                pendingSearchBrand = null
                                pendingSearchShowFilters = null
                            }
                        )
                        AppDestinations.ADD -> PlaceholderScreen(strings.add)
                        AppDestinations.FAVORITES -> FavoritesScreen(
                            strings = strings,
                            favoriteCars = favoriteCars,
                            onFavoriteToggle = { key ->
                                favoriteCars = if (favoriteCars.contains(key)) {
                                    favoriteCars - key
                                } else {
                                    favoriteCars + key
                                }
                            }
                        )
                        AppDestinations.PROFILE -> ProfileScreen(
                            onSettingsClick = { showSettings = true },
                            strings = strings
                        )
                    }
                }
            }
        }
    }
}
