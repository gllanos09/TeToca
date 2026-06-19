package com.tetocaApp.tetoca.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val EsquemaClaro = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealOnContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = AmberOnContainer,
    tertiary = SkyTertiary,
    onTertiary = SkyOnTertiary,
    tertiaryContainer = SkyContainer,
    onTertiaryContainer = SkyOnContainer,
    background = FondoClaro,
    onBackground = SobreFondoClaro,
    surface = SuperficieClara,
    onSurface = SobreSuperficieClara,
    surfaceVariant = VarianteSuperficieClara,
    onSurfaceVariant = SobreVarianteClara,
    outline = OutlineClaro,
    error = ErrorRojo,
    onError = SobreError,
    errorContainer = ErrorContainer,
    onErrorContainer = SobreErrorContainer
)

private val EsquemaOscuro = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = TealOnPrimaryDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = TealOnContainerDark,
    secondary = AmberSecondaryDark,
    onSecondary = AmberOnSecondaryDark,
    background = FondoOscuro,
    onBackground = SobreFondoOscuro,
    surface = SuperficieOscura,
    onSurface = SobreSuperficieOscura,
    surfaceVariant = VarianteSuperficieOscura,
    onSurfaceVariant = SobreVarianteOscura,
    outline = OutlineOscuro,
    error = ErrorRojo,
    onError = SobreError,
    errorContainer = ErrorContainer,
    onErrorContainer = SobreErrorContainer
)

private val FormasTeToca = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun TetocaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        shapes = FormasTeToca,
        content = content
    )
}
