package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = GoldAccent,
  onPrimary = NavyBlueDark,
  primaryContainer = RoyalPurpleLight,
  onPrimaryContainer = Color.White,
  secondary = RoyalPurpleLight,
  onSecondary = Color.White,
  secondaryContainer = NavyBluePrimary,
  onSecondaryContainer = GoldAccentLight,
  tertiary = GoldAccentLight,
  onTertiary = NavyBlueDark,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkCard,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
  primary = NavyBluePrimary,
  onPrimary = Color.White,
  primaryContainer = NavyBlueDark,
  onPrimaryContainer = Color.White,
  secondary = RoyalPurpleSecondary,
  onSecondary = Color.White,
  secondaryContainer = RoyalPurpleLight,
  onSecondaryContainer = Color.White,
  tertiary = GoldAccent,
  onTertiary = NavyBlueDark,
  background = SlateGrayBackground,
  onBackground = TextPrimary,
  surface = SlateGraySurface,
  onSurface = TextPrimary,
  surfaceVariant = SlateGrayBackground,
  onSurfaceVariant = TextSecondary,
  outline = SlateBorder
)

@Composable
fun ChurchBaseTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
