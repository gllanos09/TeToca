package com.tetocaApp.tetoca.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EsquemaOscuro = darkColorScheme(
    primary            = AzulPrimario,
    onPrimary          = SobreAzul,
    primaryContainer   = AzulFondo,
    onPrimaryContainer = BeigeAccent,
    secondary          = BeigeAccent,
    onSecondary        = Fondo,
    background         = Fondo,
    onBackground       = SobreFondo,
    surface            = Superficie,
    onSurface          = SobreSuperficie,
    surfaceVariant     = SuperficieAlt,
    onSurfaceVariant   = SobreVariante,
    outline            = Borde,
    error              = ColorError,
    onError            = ColorSobreError,
    errorContainer     = ColorErrorFondo,
    onErrorContainer   = RojoStock
)

private val EsquemaClaro = lightColorScheme(
    primary            = AzulPrimario,
    onPrimary          = Color.White,
    primaryContainer   = AzulFondo,
    onPrimaryContainer = AzulPrimario,
    secondary          = AzulPrimario,
    onSecondary        = Color.White,
    background         = FondoClaro,
    onBackground       = SobreFondoClaro,
    surface            = SuperficieClara,
    onSurface          = SobreFondoClaro,
    surfaceVariant     = BeigeAccent,
    onSurfaceVariant   = SobreVariante,
    outline            = BordeClaro,
    error              = ColorError,
    onError            = ColorSobreError,
    errorContainer     = ColorErrorFondo,
    onErrorContainer   = RojoStock
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun TetocaTheme(
    darkTheme: Boolean = false, // Forzado a false para mantener la estética clara y moderna solicitada
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography  = Typography,
        shapes      = Formas,
        content     = content
    )
}
