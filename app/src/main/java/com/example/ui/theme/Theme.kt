package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = BrowserBlueDark,
  onPrimary = Color(0xFF003063),
  primaryContainer = Color(0xFF1A73E8),
  onPrimaryContainer = Color(0xFFFFFFFF),
  secondary = Color(0xFFBAC7DE),
  onSecondary = Color(0xFF243142),
  secondaryContainer = Color(0xFF384358),
  onSecondaryContainer = Color(0xFFD4E3FF),
  background = SurfaceDark,
  onBackground = Color(0xFFE2E2E8),
  surface = CardDark,
  onSurface = Color(0xFFE2E2E8),
  surfaceVariant = Color(0xFF202636),
  onSurfaceVariant = Color(0xFFC4C6D0),
  outline = OutlineDark,
  outlineVariant = Color(0xFF262E3E),
  error = Color(0xFFFFB4AB),
  onError = Color(0xFF690005),
  errorContainer = Color(0xFF93000A),
  onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
  primary = BrowserBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD4E3FF),
  onPrimaryContainer = Color(0xFF001C3B),
  secondary = Color(0xFF555F71),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFDDE3EA),
  onSecondaryContainer = Color(0xFF131C28),
  background = SurfaceLight,
  onBackground = Color(0xFF191C20),
  surface = CardLight,
  onSurface = Color(0xFF191C20),
  surfaceVariant = Color(0xFFF0F3F6),
  onSurfaceVariant = Color(0xFF44474E),
  outline = OutlineLight,
  outlineVariant = Color(0xFFE2E8F0),
  error = Color(0xFFBA1A1A),
  onError = Color.White,
  errorContainer = Color(0xFFFFDAD6),
  onErrorContainer = Color(0xFF410002)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

