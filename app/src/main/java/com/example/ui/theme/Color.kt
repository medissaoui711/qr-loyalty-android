package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Espresso & Gold Theme Colors
val EspressoBlack = Color(0xFF100C09) // Background
val ObsidianDark = Color(0xFF1E1610)  // Surface
val CoffeeGold = Color(0xFFE5A93B)    // Primary
val AmberGold = Color(0xFFFFB300)     // Accent
val WarmCream = Color(0xFFF7EBE1)     // On Surface Light
val DarkBrown = Color(0xFF3E2723)     // Muted container
val CafeBronze = Color(0xFF8D6E63)    // Secondary
val SoftGrey = Color(0xFF9E9E9E)
val GlassWhite = Color(0x1AFFFFFF)

val GoldenDarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = CoffeeGold,
    onPrimary = Color(0xFF1F1206),
    secondary = CafeBronze,
    onSecondary = Color.White,
    secondaryContainer = DarkBrown,
    onSecondaryContainer = WarmCream,
    background = EspressoBlack,
    onBackground = WarmCream,
    surface = ObsidianDark,
    onSurface = WarmCream,
    error = Color(0xFFEF5350)
)
