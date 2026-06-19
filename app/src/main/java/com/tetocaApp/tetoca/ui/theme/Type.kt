package com.tetocaApp.tetoca.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Escala tipográfica grande y con peso, para una lectura cómoda.
// (Usa la fuente del sistema; se puede cambiar por una de Google Fonts más adelante.)
private val Familia = FontFamily.Default

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.Normal,
        fontSize = 17.sp, lineHeight = 25.sp, letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Familia, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp
    )
)
