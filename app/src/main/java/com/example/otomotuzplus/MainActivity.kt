package com.example.otomotuzplus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otomotuzplus.ui.theme.OtomotUZplusTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OtomotUZplusTheme {
                OtomotUZplusApp()
            }
        }
    }
}

// --- 1. GŁÓWNA STRUKTURA I NAWIGACJA ---
@Composable
fun OtomotUZplusApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        // Zmiana na wbudowane ikony z Compose
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            // Ten Box zajmuje całe miejsce nad dolnym paskiem
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                // MAGIA NAWIGACJI: Wyświetlamy ekran w zależności od wybranej zakładki
                when (currentDestination) {
                    AppDestinations.HOME -> HomeScreen()
                    AppDestinations.SEARCH -> SearchScreen()
                    AppDestinations.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}

// --- 2. ZDEFINIOWANE ZAKŁADKI ---
enum class AppDestinations(val label: String, val icon: ImageVector) {
    HOME("Główna", Icons.Filled.Home),
    SEARCH("Szukaj", Icons.Filled.Search),
    SETTINGS("Ustawienia", Icons.Filled.Settings),
}

// --- 3. EKRANY (ZAWARTOŚĆ ZAKŁADEK) ---

@Composable
fun HomeScreen() {
    // Tu zrobimy ładny wygląd z żółtym autem
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Strona Główna", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("To strona która odpala się na starcie aplikacji.", modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun SearchScreen() {
    // Tu będą filtry i ogłoszenia
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Wyszukiwarka", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Tutaj dodamy filtry i listę aut.", modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    // Ekran ustawień z przyciskiem wylogowania na samym dole
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ustawienia", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Tryb ciemny / Język - do zrobienia", modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.weight(1f)) // Wypycha przycisk na sam dół ekranu

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(context, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Wyloguj się")
        }
    }
}