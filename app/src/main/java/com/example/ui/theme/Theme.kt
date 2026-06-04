package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // We enforce our custom GoldenDarkColorScheme in this application to give 
    // a gorgeous, premium, luxury cafe and digital wallet atmosphere.
    MaterialTheme(
        colorScheme = GoldenDarkColorScheme,
        typography = Typography,
        content = content
    )
}
