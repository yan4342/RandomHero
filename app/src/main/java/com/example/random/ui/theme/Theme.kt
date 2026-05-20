package com.example.random.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.random.model.ThemeColorSettings

private fun darkColorSchemeFor(themeColors: ThemeColorSettings) = darkColorScheme(
    primary = Color(themeColors.accentColor),
    secondary = Color(themeColors.teamAColor),
    tertiary = Color(themeColors.teamBColor),
    background = themeColors.backgroundColor?.let(::Color) ?: DarkBgColor,
    surface = themeColors.cardColor?.let(::Color) ?: SurfaceElevatedDark,
    onPrimary = contentColorFor(Color(themeColors.accentColor)),
    onSecondary = contentColorFor(Color(themeColors.teamAColor)),
    onTertiary = contentColorFor(Color(themeColors.teamBColor)),
    onBackground = contentColorFor(themeColors.backgroundColor?.let(::Color) ?: DarkBgColor),
    onSurface = contentColorFor(themeColors.cardColor?.let(::Color) ?: SurfaceElevatedDark)
)

private fun lightColorSchemeFor(themeColors: ThemeColorSettings) = lightColorScheme(
    primary = Color(themeColors.accentColor),
    secondary = Color(themeColors.teamAColor),
    tertiary = Color(themeColors.teamBColor),
    background = themeColors.backgroundColor?.let(::Color) ?: BgColor,
    surface = themeColors.cardColor?.let(::Color) ?: SurfaceElevatedLight,
    onPrimary = contentColorFor(Color(themeColors.accentColor)),
    onSecondary = contentColorFor(Color(themeColors.teamAColor)),
    onTertiary = contentColorFor(Color(themeColors.teamBColor)),
    onBackground = contentColorFor(themeColors.backgroundColor?.let(::Color) ?: BgColor),
    onSurface = contentColorFor(themeColors.cardColor?.let(::Color) ?: SurfaceElevatedLight)
)

@Composable
fun RandomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColors: ThemeColorSettings = ThemeColorSettings.Default,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorSchemeFor(themeColors)
    } else {
        lightColorSchemeFor(themeColors)
    }
    val appColors = (if (darkTheme) DarkAppColors else LightAppColors)
        .withThemeColors(themeColors)

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
