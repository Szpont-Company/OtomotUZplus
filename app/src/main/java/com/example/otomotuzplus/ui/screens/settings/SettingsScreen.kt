package com.example.otomotuzplus.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.otomotuzplus.LoginActivity
import com.example.otomotuzplus.data.ThemeMode
import com.example.otomotuzplus.ui.components.SettingsClickItem
import com.example.otomotuzplus.ui.models.AppStrings
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    strings: AppStrings
) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(strings.chooseLanguage, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    listOf("Polski", "English").forEach { lang ->
                        TextButton(
                            onClick = {
                                onLanguageChange(lang)
                                showLanguageDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = lang, 
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(strings.chooseTheme, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        val label = when (mode) {
                            ThemeMode.SYSTEM -> strings.themeSystem
                            ThemeMode.LIGHT -> strings.themeLight
                            ThemeMode.DARK -> strings.themeDark
                        }
                        TextButton(
                            onClick = {
                                onThemeChange(mode)
                                showThemeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label, 
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settings) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(strings.appPreferences, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            val themeLabel = when (themeMode) {
                ThemeMode.SYSTEM -> strings.themeSystem
                ThemeMode.LIGHT -> strings.themeLight
                ThemeMode.DARK -> strings.themeDark
            }

            SettingsClickItem(
                title = "${strings.theme} ($themeLabel)",
                icon = Icons.Default.Palette,
                onClick = { showThemeDialog = true }
            )

            SettingsClickItem(
                title = "${strings.language} ($currentLanguage)",
                icon = Icons.Default.Language,
                onClick = { showLanguageDialog = true }
            )

            SettingsClickItem(strings.notifications, Icons.Default.Notifications, {})

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(context, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) {
                Text(strings.logout, color = Color.White)
            }
        }
    }
}
