package com.perpenda.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Error,
    onError = OnError
)

private val DarkColors = darkColorScheme(
    primary = ColorDarkPrimary,
    onPrimary = ColorDarkOnPrimary,
    primaryContainer = ColorDarkPrimaryContainer,
    onPrimaryContainer = ColorDarkOnPrimaryContainer,
    secondary = ColorDarkSecondary,
    onSecondary = ColorDarkOnSecondary,
    secondaryContainer = ColorDarkSecondaryContainer,
    onSecondaryContainer = ColorDarkOnSecondaryContainer,
    tertiary = ColorDarkTertiary,
    onTertiary = ColorDarkOnTertiary,
    background = ColorDarkBackground,
    onBackground = ColorDarkOnBackground,
    surface = ColorDarkSurface,
    surfaceVariant = ColorDarkSurfaceVariant,
    onSurface = ColorDarkOnSurface,
    onSurfaceVariant = ColorDarkOnSurfaceVariant,
    outline = ColorDarkOutline,
    outlineVariant = ColorDarkOutlineVariant,
    error = ColorDarkError,
    onError = ColorDarkOnPrimary
)

private val LocalPerpendaColors = staticCompositionLocalOf { PerpendaLight }

object PerpendaTheme {
    val colors: PerpendaColors
        @Composable @ReadOnlyComposable get() = LocalPerpendaColors.current
}

@Composable
fun PerpendaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalPerpendaColors provides if (darkTheme) PerpendaDark else PerpendaLight
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}