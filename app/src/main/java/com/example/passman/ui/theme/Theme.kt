package com.example.passman.ui.theme

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

private val BlueThemeLight = lightColors(
    primary = blue700,
    onPrimary = Color.White,
    primaryVariant = blue800,
    secondary = yellow500
)

private val BlueThemeDark = darkColors(
    primary = blue200,
    secondary = yellow200,
    surface = blueDarkPrimary
)

private val DarkColors = darkColors(
    primary = Red300,
    primaryVariant = Red700,
    onPrimary = Color.Black,
    secondary = Red300,
    onSecondary = Color.Black,
    error = Red200
)

private val LightColors = lightColors(
    primary = Red700,
    primaryVariant = Red900,
    onPrimary = Color.White,
    secondary = Red700,
    secondaryVariant = Red900,
    onSecondary = Color.White,
    error = Red800
)

@Composable
fun PassManTheme(darkTheme: Boolean, content: @Composable() () -> Unit) {

    val colors = if (darkTheme) {
        BlueThemeDark
    } else {
        BlueThemeLight
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}