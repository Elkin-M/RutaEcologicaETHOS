package com.ethos.rutaecologica.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = EthosHeaderNavy,
    onPrimary = Color.White,
    primaryContainer = EthosSecondarySlate,
    onPrimaryContainer = Color.White,
    secondary = EthosAccentTeal,
    onSecondary = Color.White,
    secondaryContainer = EthosAccentTeal.copy(alpha = 0.2f),
    tertiary = EthosPrimaryYellow,
    background = EthosBackground,
    onBackground = EthosTextDark,
    surface = Color.White,
    onSurface = EthosTextDark,
    surfaceVariant = EthosBackground,
    error = EthosError
)

@Composable
fun RutaEcologicaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = EthosTypography,
        content = content
    )
}
