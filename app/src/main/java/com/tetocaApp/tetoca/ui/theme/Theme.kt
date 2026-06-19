package com.tetocaApp.tetoca.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.dp

private val EsquemaOscuro = darkColorScheme(
    primary            = Violeta,
    onPrimary          = SobreVioleta,
    primaryContainer   = VioletaFondo,
    onPrimaryContainer = VioletaClaro,
    secondary          = AmbarStock,
    onSecondary        = Fondo,
    background         = Fondo,
    onBackground       = SobreFondo,
    surface            = Superficie,
    onSurface          = SobreSuperficie,
    surfaceVariant     = SuperficieAlt,
    onSurfaceVariant   = SobreVariante,
    outline            = Borde,
    error              = Error,
    onError            = SobreError,
    errorContainer     = ErrorFondo,
    onErrorContainer   = RojoStock
)

private val EsquemaClaro = lightColorScheme(
    primary            = VioletaClaroTema,
    onPrimary          = SobreVioleta,
    primaryContainer   = VioletaFondo,
    onPrimaryContainer = VioletaClaro,
    background         = FondoClaro,
    onBackground       = Fondo,
    surface            = SuperficieClara,
    onSurface          = Fondo,
    error              = Error,
    onError            = SobreError
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TetocaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography  = Typography,
        shapes      = Formas,
        content     = content
    )
}
