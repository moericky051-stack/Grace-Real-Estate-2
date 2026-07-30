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

private val DarkColorScheme =
  darkColorScheme(
    primary = RealEstateGold,
    onPrimary = Color.Black,
    secondary = RealEstateGoldLight,
    tertiary = RealEstateGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RealEstateNavy,
    onPrimary = Color.White,
    secondary = RealEstateGold,
    onSecondary = Color.Black,
    tertiary = RealEstateGreen,
    background = RealEstateBg,
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
  )

@Composable
fun MyApplicationTheme(
  themeOption: AppThemeOption = AppThemeOption.NAVY_GOLD,
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (themeOption.isDark) {
    darkColorScheme(
      primary = themeOption.primaryColor,
      onPrimary = Color.White,
      secondary = themeOption.accentColor,
      onSecondary = Color.Black,
      tertiary = RealEstateGreen,
      background = themeOption.headerColor,
      surface = Color(0xFF1E293B),
      onBackground = Color.White,
      onSurface = Color.White
    )
  } else {
    lightColorScheme(
      primary = themeOption.primaryColor,
      onPrimary = Color.White,
      secondary = themeOption.accentColor,
      onSecondary = Color.Black,
      tertiary = RealEstateGreen,
      background = RealEstateBg,
      surface = Color.White,
      onBackground = Color(0xFF0F172A),
      onSurface = Color(0xFF0F172A)
    )
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
