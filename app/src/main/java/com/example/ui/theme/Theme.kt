package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = TealDark,
    primaryContainer = TealPrimary,
    onPrimaryContainer = Color.White,
    
    secondary = AmberAccent,
    onSecondary = Color.Black,
    secondaryContainer = AmberLightDark,
    onSecondaryContainer = AmberLight,
    
    tertiary = PremiumLight,
    onTertiary = PremiumPurple,
    tertiaryContainer = PremiumLightDark,
    
    background = AppBackgroundDark,
    onBackground = TextPrimaryDark,
    
    surface = AppSurfaceDark,
    onSurface = TextPrimaryDark,
    
    surfaceVariant = TealMistDark,
    onSurfaceVariant = TextSecondaryDark,
    
    error = DangerColor,
    onError = Color.White,
    
    outline = TextMutedDark,
    outlineVariant = DividerColorDark
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}
