@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.otomotuzplus

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.otomotuzplus.data.FirebaseRepository
import com.example.otomotuzplus.data.PreferenceManager
import com.example.otomotuzplus.data.ThemeMode
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
import com.example.otomotuzplus.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> hasNotificationPermission = isGranted }

    private val repository = FirebaseRepository()
    private var hasNotificationPermission by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefManager = PreferenceManager(this)
        val lang = prefManager.getLanguage()
        val strings = if (lang == "Polski") PolishStrings else EnglishStrings

        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        NotificationHelper.createNotificationChannel(this, strings)
        createNotificationChannel()
        enableEdgeToEdge()

        setContent {
            var themeMode by remember { mutableStateOf(prefManager.getThemeMode()) }
            var currentLanguage by remember { mutableStateOf(prefManager.getLanguage()) }
            var notificationsRefused by remember { mutableStateOf(prefManager.wasNotificationsRefused()) }

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            OtomotUZplusTheme(darkTheme = darkTheme) {
                OtomotUZplusApp(
                    repository = repository,
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
                    notificationsRefused = notificationsRefused,
                    onSetNotificationsRefused = { refused ->
                        notificationsRefused = refused
                        prefManager.setNotificationsRefused(refused)
                    },
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    notificationsPermissionGranted = hasNotificationPermission
                )
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Pobieranie tokenu nie powiodło się", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "Twój token FCM to: $token")

            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null) {
                repository.updateFcmToken(currentUserId, token)
            } else {
                repository.updateFcmToken("unauthenticated_device", token)
            }
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
    repository: FirebaseRepository,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    notificationsRefused: Boolean,
    onSetNotificationsRefused: (Boolean) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    notificationsPermissionGranted: Boolean
) {
    val strings = if (currentLanguage == "Polski") PolishStrings else EnglishStrings
    val context = LocalContext.current

    val shouldShowDialog = !notificationsPermissionGranted && !notificationsRefused &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var pendingSearchQuery by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSearchBrand by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSearchShowFilters by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var favoriteCars by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var selectedCar by remember { mutableStateOf<com.example.otomotuzplus.models.CarAd?>(null) }
    var allCarsFromDb by remember { mutableStateOf<List<com.example.otomotuzplus.models.CarAd>>(emptyList()) }

    LaunchedEffect(Unit) {
        repository.observeCars { updatedCars ->
            allCarsFromDb = updatedCars
        }
    }

    val toggleFavorite: (String) -> Unit = { key ->
        if (favoriteCars.contains(key)) {
            favoriteCars = favoriteCars - key
        } else {
            favoriteCars = favoriteCars + key
            val likedCar = allCarsFromDb.find { it.id == key }
            if (likedCar != null && likedCar.sellerId.isNotEmpty()) {
                repository.sendLikeNotification(context, likedCar.sellerId, likedCar.title)
            }
        }
    }

    var showRationaleDialog by rememberSaveable { mutableStateOf(false) }

    fun openSearch(query: String? = null, brand: String? = null, showFilters: Boolean? = false) {
        pendingSearchQuery = query
        pendingSearchBrand = brand
        pendingSearchShowFilters = showFilters
        if (currentDestination != AppDestinations.SEARCH) {
            currentDestination = AppDestinations.SEARCH
        }
    }

    BackHandler(enabled = selectedCar != null || showSettings || currentDestination != AppDestinations.HOME) {
        if (selectedCar != null) {
            selectedCar = null
        } else if (showSettings) {
            showSettings = false
        } else {
            currentDestination = AppDestinations.HOME
        }
    }

    if (shouldShowDialog) {
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
                onDismissRequest = { },
                icon = { Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text(text = strings.dealNotification, style = MaterialTheme.typography.headlineSmall) },
                text = { Text(text = strings.dealNotificationDescription, style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    Button(onClick = { onRequestNotificationPermission() }) { Text(strings.enable) }
                },
                dismissButton = {
                    TextButton(onClick = { onSetNotificationsRefused(true) }) { Text(strings.maybeLater) }
                }
            )
        }
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

    if (selectedCar != null) {
        com.example.otomotuzplus.ui.screens.details.AdDetailScreen(
            car = selectedCar!!,
            strings = strings,
            onBackClick = { selectedCar = null }
        )
    } else if (showSettings) {
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
                            carsFromDb = allCarsFromDb,
                            onNavigateToSearch = { openSearch() },
                            onNavigateToAdd = { currentDestination = AppDestinations.ADD },
                            onSearchSubmit = { submittedQuery ->
                                if (submittedQuery.isNotBlank()) {
                                    openSearch(query = submittedQuery)
                                }
                            },
                            onBrandSelect = { selectedBrand -> openSearch(brand = selectedBrand) },
                            onSeeAllClick = { openSearch() },
                            onNotificationsClick = { },
                            favoriteCars = favoriteCars,
                            onFavoriteToggle = toggleFavorite,
                            onCarClick = { clickedCar -> selectedCar = clickedCar }
                        )

                        AppDestinations.SEARCH -> SearchScreen(
                            strings = strings,
                            initialQuery = pendingSearchQuery,
                            initialBrand = pendingSearchBrand,
                            initialShowFilters = pendingSearchShowFilters,
                            favoriteCars = favoriteCars,
                            onFavoriteToggle = toggleFavorite,
                            onInitialFiltersConsumed = {
                                pendingSearchQuery = null
                                pendingSearchBrand = null
                                pendingSearchShowFilters = null
                            },
                            allCarsFromDb = allCarsFromDb,
                            onCarClick = { clickedCar -> selectedCar = clickedCar }
                        )

                        AppDestinations.ADD -> com.example.otomotuzplus.ui.screens.add.AddScreen(
                            strings = strings,
                            onNavigateBack = { currentDestination = AppDestinations.HOME }
                        )

                        AppDestinations.FAVORITES -> FavoritesScreen(
                            strings = strings,
                            favoriteCars = favoriteCars,
                            onFavoriteToggle = toggleFavorite,
                            allCarsFromDb = allCarsFromDb,
                            onCarClick = { clickedCar -> selectedCar = clickedCar }
                        )

                        AppDestinations.PROFILE -> ProfileScreen(
                            onSettingsClick = { showSettings = true },
                            strings = strings,
                            allCarsFromDb = allCarsFromDb,
                            onCarClick = { clickedCar -> selectedCar = clickedCar }
                        )
                    }
                }
            }
        }
    }
}