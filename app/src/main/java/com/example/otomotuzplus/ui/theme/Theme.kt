package com.example.otomotuzplus.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandGold,
    secondary = DarkSlate900,
    tertiary = ActionBlue,
    background = DarkSlate950,
    surface = DarkSlate900,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Slate300,
    onSurface = Slate300,
    surfaceVariant = DarkSlate800,
    onSurfaceVariant = Slate400,
    outline = DarkSlate700
)

private val LightColorScheme = lightColorScheme(
    primary = BrandGold,
    secondary = Slate100,
    tertiary = ActionBlue,
    background = LightBackground,
    surface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkText,
    onSurface = DarkText,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate400,
    outline = Slate200
)

@Composable
fun OtomotUZplusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}