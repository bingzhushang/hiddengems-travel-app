package com.hiddengems.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// Primary Colors (Forest Green)
val Primary50 = Color(0xFFE8F5E9)
val Primary100 = Color(0xFFC8E6C9)
val Primary200 = Color(0xFFA5D6A7)
val Primary300 = Color(0xFF81C784)
val Primary400 = Color(0xFF66BB6A)
val Primary500 = Color(0xFF4CAF50)
val Primary600 = Color(0xFF43A047)
val Primary700 = Color(0xFF388E3C)
val Primary800 = Color(0xFF2E7D32)
val Primary900 = Color(0xFF1B5E20)

// Secondary Colors
val Forest = Color(0xFF2D5A27)
val Lake = Color(0xFF4DA8A8)
val Sunset = Color(0xFFFF8A65)
val Earth = Color(0xFF8D6E63)
val Sky = Color(0xFF64B5F6)
val Stone = Color(0xFF78909C)

// Functional Colors
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFF9800)
val Error = Color(0xFFF44336)
val Info = Color(0xFF2196F3)

// Crowd Level Colors
val CrowdLow = Color(0xFF4CAF50)
val CrowdMedium = Color(0xFFFF9800)
val CrowdHigh = Color(0xFFF44336)

// Neutral Colors
val Neutral900 = Color(0xFF1A1A1A)
val Neutral800 = Color(0xFF333333)
val Neutral700 = Color(0xFF555555)
val Neutral600 = Color(0xFF757575)
val Neutral500 = Color(0xFF9E9E9E)
val Neutral400 = Color(0xFFBDBDBD)
val Neutral300 = Color(0xFFE0E0E0)
val Neutral200 = Color(0xFFEEEEEE)
val Neutral100 = Color(0xFFF5F5F5)
val Neutral50 = Color(0xFFFAFAFA)

// Typography
val Typography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = Primary600,
    onPrimary = Color.White,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary900,
    secondary = Forest,
    onSecondary = Color.White,
    secondaryContainer = Primary50,
    onSecondaryContainer = Primary900,
    tertiary = Lake,
    onTertiary = Color.White,
    background = Neutral50,
    onBackground = Neutral900,
    surface = Color.White,
    onSurface = Neutral900,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral700,
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Neutral300,
    outlineVariant = Neutral200,
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary400,
    onPrimary = Color.Black,
    primaryContainer = Primary800,
    onPrimaryContainer = Primary100,
    secondary = Primary500,
    onSecondary = Color.Black,
    secondaryContainer = Primary800,
    onSecondaryContainer = Primary100,
    tertiary = Lake,
    onTertiary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF333333),
    outlineVariant = Color(0xFF444444),
)

@Composable
fun HiddenGemsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Crowd Level Helper Functions
fun getFullCrowdLevelText(level: String): String {
    return when (level.uppercase()) {
        "LOW" -> "人流少"
        "MEDIUM" -> "人流中等"
        "HIGH" -> "人流较多"
        else -> level
    }
}

fun getShortCrowdLevelText(level: String): String {
    return when (level.uppercase()) {
        "LOW" -> "少"
        "MEDIUM" -> "中"
        "HIGH" -> "多"
        else -> level
    }
}

fun getCrowdLevelColor(level: String): Color {
    return when (level.uppercase()) {
        "LOW" -> CrowdLow
        "MEDIUM" -> CrowdMedium
        "HIGH" -> CrowdHigh
        else -> Neutral500
    }
}
