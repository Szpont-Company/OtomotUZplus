/**
 * @file MainActivity.kt
 * @brief Główna Activity po zalogowaniu – korzeń drzewa Compose i zarządca nawigacji stanowej.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.otomotuzplus

import android.Manifest
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.ads.MobileAds

/**
 * Jedyna Activity po zalogowaniu, która hostuje całe drzewo UI Compose.
 *
 * Zadania obsługiwane na poziomie Activity:
 * - Inicjalizacja AdMob (`MobileAds.initialize`).
 * - Żądanie uprawnienia `POST_NOTIFICATIONS` na Android 13+.
 * - Odczyt zapisanego motywu i języka z [PreferenceManager] i
 *   przekazanie ich do [OtomotUZplusApp].
 * - Pobieranie tokenu rejestracji FCM i dołączanie nasłuchiwacza migawek Firestore
 *   na kolekcji `notifications`, aby przychodzące zdarzenia "polubień"
 *   wyzwalały lokalne powiadomienia push przez [NotificationHelper].
 *
 * Cały stan nawigacji w aplikacji żyje w [OtomotUZplusApp] (jako [Composable]),
 * nie w tej Activity.
 */
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> hasNotificationPermission = isGranted }

    private val repository = FirebaseRepository()
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
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
        enableEdgeToEdge()

        MobileAds.initialize(this) {}

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
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    notificationsPermissionGranted = hasNotificationPermission
                )
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_DIAG", "Pobieranie tokenu nie powiodło się", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM_DIAG", "SUKCES! Twój token FCM to: $token")

            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null) {

                    var isInitialSnapshot = true

                    db.collection("notifications")
                        .whereEqualTo("toUser", currentUserId)
                        .addSnapshotListener { snapshots, e ->
                            if (e != null) {
                                Log.w("FCM_DIAG", "Błąd nasłuchiwania powiadomień", e)
                                return@addSnapshotListener
                            }

                            if (isInitialSnapshot) {
                                isInitialSnapshot = false
                                return@addSnapshotListener
                            }

                            for (dc in snapshots!!.documentChanges) {
                                if (dc.type == DocumentChange.Type.ADDED) {
                                    val carTitle = dc.document.getString("carTitle") ?: "Auto"
                                    val message = strings.likeNotificationMessage.format(carTitle)

                                    NotificationHelper.sendNotification(
                                        this@MainActivity,
                                        strings.likeNotificationTitle,
                                        message
                                    )
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("FCM_DIAG", "Blad podczas zapisu tokenu do repozytorium: ${e.message}", e)
            }
        }
    }
}

/**
 * Główna kompozycja posiadająca cały współdzielony stan nawigacji i UI aplikacji.
 *
 * ## Model nawigacji
 * Nawigacja sterowana stanem zamiast trasami:
 * - `currentDestination` wybiera który ekran zakładki jest pokazany wewnątrz
 *   [NavigationSuiteScaffold].
 * - `selectedCar != null` nakłada [AdDetailScreen] na cały scaffold.
 * - `showSettings == true` nakłada [SettingsScreen] na cały scaffold.
 * - [BackHandler] zdejmuje warstwy: szczegóły → ustawienia → HOME.
 *
 * ## Głębokie linki między ekranami
 * Zmienne `pendingSearch*` przenoszą stan początkowych filtrów z ekranu Home
 * do [SearchScreen]. [SearchScreen] konsumuje je raz przez `onInitialFiltersConsumed`.
 *
 * ## Przepływ danych
 * `allCarsFromDb` wypełniane raz przez [LaunchedEffect] subskrybujący
 * [FirebaseRepository.observeCars] i przekazywane jawnie do każdego ekranu
 * który go potrzebuje — brak warstwy ViewModel ani DI.
 *
 * @param repository                  Singleton dostępu do danych.
 * @param themeMode                   Aktywny motyw.
 * @param onThemeChange               Callback do zapisania nowego motywu.
 * @param currentLanguage             Aktywny identyfikator języka.
 * @param onLanguageChange            Callback do zapisania nowego języka.
 * @param notificationsRefused        Czy użytkownik odrzucił okno dialogowe uprawnień.
 * @param onSetNotificationsRefused   Callback do zapisania flagi odrzucenia.
 * @param onRequestNotificationPermission Callback uruchamiający systemowe okno uprawnień.
 * @param notificationsPermissionGranted  Aktualny stan przyznania uprawnień.
 */
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
        Log.d("FCM_DIAG", "=== KLIKNIĘTO SERDUSZKO ===")
        Log.d("FCM_DIAG", "Otrzymany klucz (key): $key")

        if (favoriteCars.contains(key)) {
            Log.d("FCM_DIAG", "Auto było już w ulubionych -> USUWANIE z ulubionych (brak powiadomienia).")
            favoriteCars = favoriteCars - key
        } else {
            Log.d("FCM_DIAG", "Auta nie ma w ulubionych -> DODAWANIE do ulubionych.")
            favoriteCars = favoriteCars + key

            val realId = key.substringBefore("|")
            Log.d("FCM_DIAG", "Wycięte czyste ID do bazy danych: $realId")
            Log.d("FCM_DIAG", "Liczba aut w lokalnej pamięci podręcznej (allCarsFromDb): ${allCarsFromDb.size}")

            val likedCar = allCarsFromDb.find { it.id == realId }
            if (likedCar != null) {
                Log.d("FCM_DIAG", "Sukces! Znaleziono auto w bazie: ${likedCar.title}")
                Log.d("FCM_DIAG", "Wartość sellerId dla tego auta to: '${likedCar.sellerId}'")

                if (likedCar.sellerId.isNotEmpty()) {
                    Log.d("FCM_DIAG", "Próba uruchomienia repository.sendLikeNotification...")
                    repository.sendLikeNotification(likedCar.sellerId, likedCar.title)
                } else {
                    Log.w("FCM_DIAG", "OSTRZEŻENIE: sellerId jest PUSTE. Nie ma dokąd wysłać powiadomienia.")
                }
            } else {
                Log.w("FCM_DIAG", "BŁĄD: Mimo wycięcia ID ($realId), nadal nie ma takiego auta w allCarsFromDb.")
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
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                showRationaleDialog = true
            }
        }

        if (showRationaleDialog) {
            AlertDialog(
                onDismissRequest = { },
                icon = { Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text(text = strings.dealNotification, style = MaterialTheme.typography.headlineSmall) },
                text = { Text(text = strings.dealNotificationDescription, style = MaterialTheme.typography.bodyMedium) },
                confirmButton = { Button(onClick = { onRequestNotificationPermission() }) { Text(strings.enable) } },
                dismissButton = { TextButton(onClick = { onSetNotificationsRefused(true) }) { Text(strings.maybeLater) } }
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