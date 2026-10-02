package com.example.historialmedico.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,

    primaryContainer = AccentBlueLight,
    onPrimaryContainer = AccentBlueDark,

    secondary = AccentBluePressed,
    onSecondary = Color.White,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceAlt,
    onSurface = TextPrimary,
    surfaceVariant = NeutralGray300,
    onSurfaceVariant = NeutralGray700,

    outline = NeutralGray600
)

@Composable
fun historialMedicoSwitchColors()= SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = AccentBlue,
    checkedBorderColor = Color.Transparent,
    uncheckedThumbColor = NeutralGray600,
    uncheckedTrackColor = Color.Transparent,
    uncheckedBorderColor = NeutralGray600
)

@Composable
fun HistorialMedicoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color esta disponible en Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
        shapes = AppShapes,
        content = content
    )
}