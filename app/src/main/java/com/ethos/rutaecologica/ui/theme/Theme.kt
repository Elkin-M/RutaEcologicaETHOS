package com.ethos.rutaecologica.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = EthosGreen,
    onPrimary = EthosOnDark,
    primaryContainer = EthosGreenPale,
    onPrimaryContainer = EthosGreenDark,
    secondary = EthosTeal,
    onSecondary = EthosOnDark,
    secondaryContainer = EthosTealLight,
    tertiary = EthosGold,
    background = EthosSand,
    onBackground = EthosGreenDark,
    surface = EthosSurface,
    onSurface = EthosGreenDark,
    surfaceVariant = EthosSandDark,
    error = EthosError
)

private val DarkColors = darkColorScheme(
    primary = EthosGreenLight,
    onPrimary = EthosGreenDark,
    primaryContainer = EthosGreen,
    secondary = EthosTealLight,
    background = EthosGreenDark,
    onBackground = EthosOnDark,
    surface = Color(0xFF20302A),
    onSurface = EthosOnDark,
    error = EthosError
)

@Composable
fun RutaEcologicaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = EthosTypography,
        content = content
    )
}
