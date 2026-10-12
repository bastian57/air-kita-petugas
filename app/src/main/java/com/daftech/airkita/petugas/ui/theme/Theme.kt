package com.daftech.airkita.petugas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PetugasColors = lightColorScheme(
    primary = AirBlue,
    onPrimary = CardBg,
    primaryContainer = AirLight,
    onPrimaryContainer = Navy,
    secondary = AirLight,
    background = AppBg,
    onBackground = Navy,
    surface = CardBg,
    onSurface = Navy,
    error = Red
)

@Composable
fun PetugasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PetugasColors,
        content = content
    )
}
