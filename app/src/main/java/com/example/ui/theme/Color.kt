package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val GeometricBlue = Color(0xFF0061A4)
val GeometricBlueDark = Color(0xFF004A7D)
val GeometricBlueContainer = Color(0xFFD1E4FF)
val GeometricBlueOnContainer = Color(0xFF001D36)
val GeometricBg = Color(0xFFF7F9FC)
val GeometricSurfaceVariant = Color(0xFFEEF1F6)
val GeometricBorder = Color(0xFFDEE2EB)
val GeometricTextPrimary = Color(0xFF1A1C1E)
val GeometricTextSecondary = Color(0xFF44474E)
val GeometricAccentGreen = Color(0xFF006E46)

// Dynamic Color Palette Base Values
val RealEstateNavy = Color(0xFF0F172A)
val RealEstateNavyLight = Color(0xFF1E293B)
val RealEstateGold = Color(0xFFF59E0B)
val RealEstateGoldLight = Color(0xFFFDE68A)
val RealEstateGreen = Color(0xFF10B981)
val RealEstateBlue = Color(0xFF38BDF8)
val RealEstateRed = Color(0xFFEF4444)
val RealEstateBg = GeometricBg

val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkPrimary = Color(0xFF38BDF8)

enum class AppThemeOption(
    val titleMm: String,
    val titleEn: String,
    val headerColor: Color,
    val primaryColor: Color,
    val accentColor: Color,
    val isDark: Boolean
) {
    NAVY_GOLD(
        titleMm = "Classic Navy (ပင်မ ရေတပ်ပြာ + ရွှေရောင်)",
        titleEn = "Classic Navy & Gold",
        headerColor = Color(0xFF0F172A),
        primaryColor = Color(0xFF1E3A8A),
        accentColor = Color(0xFFF59E0B),
        isDark = false
    ),
    DARK_MODE(
        titleMm = "Midnight Dark (ညဉ့်ဘက် အမှောင် Theme)",
        titleEn = "Midnight Dark Mode",
        headerColor = Color(0xFF020617),
        primaryColor = Color(0xFF38BDF8),
        accentColor = Color(0xFFF59E0B),
        isDark = true
    ),
    EMERALD_GREEN(
        titleMm = "Emerald Nature (သဘာဝ မြရောင် Theme)",
        titleEn = "Lush Emerald Green",
        headerColor = Color(0xFF064E3B),
        primaryColor = Color(0xFF065F46),
        accentColor = Color(0xFF34D399),
        isDark = false
    ),
    ROYAL_PURPLE(
        titleMm = "Royal Luxury (မင်းသွေး ခရမ်းရောင် Theme)",
        titleEn = "Royal Purple & Gold",
        headerColor = Color(0xFF3B0764),
        primaryColor = Color(0xFF581C87),
        accentColor = Color(0xFFF59E0B),
        isDark = false
    ),
    CHARCOAL_GOLD(
        titleMm = "Modern Charcoal (ခေတ်မီ ကာဗွန် Theme)",
        titleEn = "Modern Charcoal & Amber",
        headerColor = Color(0xFF18181B),
        primaryColor = Color(0xFF27272A),
        accentColor = Color(0xFFF59E0B),
        isDark = true
    )
}


