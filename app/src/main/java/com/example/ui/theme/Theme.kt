package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = BrowserBlueDark,
  onPrimary = Color(0xFF003063),
  primaryContainer = Color(0xFF00468C),
  onPrimaryContainer = Color(0xFFD4E3FF),
  secondary = Color(0xFFBAC7DE),
  onSecondary = Color(0xFF243142),
  background = SurfaceDark,
  onBackground = Color(0xFFE2E2E8),
  surface = SurfaceDark,
  onSurface = Color(0xFFE2E2E8),
  surfaceVariant = CardDark,
  onSurfaceVariant = Color(0xFFC4C6D0),
  outline = OutlineDark,
  outlineVariant = Color(0xFF384358)
)

private val LightColorScheme = lightColorScheme(
  primary = BrowserBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD4E3FF),
  onPrimaryContainer = Color(0xFF001C3B),
  secondary = Color(0xFF555F71),
  onSecondary = Color.White,
  background = SurfaceLight,
  onBackground = Color(0xFF191C20),
  surface = SurfaceLight,
  onSurface = Color(0xFF191C20),
  surfaceVariant = CardLight,
  onSurfaceVariant = Color(0xFF44474E),
  outline = OutlineLight,
  outlineVariant = Color(0xFFC4C6D0)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
