package com.ethos.rutaecologica.ui.theme

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

@Composable
fun RutaEcologicaTheme(
    content: @Composable () -> Unit
) {
    // Se fuerza siempre LightColors (o DarkColors si prefieres) sin importar el sistema
    MaterialTheme(
        colorScheme = LightColors,
        typography = EthosTypography,
        content = content
    )
}