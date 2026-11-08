package com.example.historialmedico.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val FormasApp = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

//se definen todos los colores que usa Material: los que faltan salen lavanda de la plantilla
private val ColoresClaros = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = Color.White,
    primaryContainer = AzulSuave,
    onPrimaryContainer = AzulTexto,
    secondary = AzulOscuro,
    onSecondary = Color.White,
    secondaryContainer = AzulSuave,
    onSecondaryContainer = AzulTexto,
    tertiary = AzulOscuro,
    onTertiary = Color.White,
    tertiaryContainer = AzulSuave,
    onTertiaryContainer = AzulTexto,
    background = FondoClaro,
    onBackground = TextoPrincipal,
    surface = SuperficieClara,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieGris,
    onSurfaceVariant = TextoSecundario,
    surfaceTint = AzulPrincipal,
    outline = BordeClaro,
    outlineVariant = BordeSuaveClaro,
    surfaceContainerLowest = SuperficieClara,
    surfaceContainerLow = SuperficieClara,
    surfaceContainer = SuperficieClara,
    surfaceContainerHigh = SuperficieClara,
    surfaceContainerHighest = SuperficieGris
)

private val ColoresOscuros = darkColorScheme(
    primary = AzulClaro,
    onPrimary = AzulSobreClaro,
    primaryContainer = AzulOscuro,
    onPrimaryContainer = AzulSuave,
    secondary = AzulClaro,
    onSecondary = AzulSobreClaro,
    secondaryContainer = AzulSecundarioOscuro,
    onSecondaryContainer = AzulSuave,
    tertiary = AzulClaro,
    onTertiary = AzulSobreClaro,
    tertiaryContainer = AzulSecundarioOscuro,
    onTertiaryContainer = AzulSuave,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    surfaceVariant = SuperficieOscuraMaxima,
    onSurfaceVariant = TextoSecundarioOscuro,
    surfaceTint = AzulClaro,
    outline = BordeOscuro,
    outlineVariant = BordeSuaveOscuro,
    surfaceContainerLowest = FondoOscuro,
    surfaceContainerLow = SuperficieOscura,
    surfaceContainer = SuperficieOscura,
    surfaceContainerHigh = SuperficieOscuraAlta,
    surfaceContainerHighest = SuperficieOscuraMaxima
)

@Composable
fun HistorialMedicoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ColoresOscuros else ColoresClaros,
        typography = Typography,
        shapes = FormasApp,
        content = content
    )
}