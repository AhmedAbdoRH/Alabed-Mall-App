package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ElAbedRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = ElAbedRoyalBlueContainer,
    onPrimaryContainer = OnElAbedRoyalBlueContainer,
    secondary = ElAbedVibrantGreen,
    onSecondary = Color.White,
    secondaryContainer = ElAbedVibrantGreenContainer,
    onSecondaryContainer = OnElAbedVibrantGreenContainer,
    tertiary = ElAbedWarmOrange,
    onTertiary = Color.White,
    tertiaryContainer = ElAbedWarmOrangeContainer,
    onTertiaryContainer = OnElAbedWarmOrangeContainer,
    background = ElAbedBackground,
    onBackground = ElAbedTextPrimary,
    surface = ElAbedSurface,
    onSurface = ElAbedTextPrimary,
    surfaceVariant = ElAbedSurfaceVariant,
    onSurfaceVariant = ElAbedTextSecondary,
    outline = ElAbedOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = NightRoyalBlue,
    onPrimary = Color(0xFF06164D),
    primaryContainer = NightRoyalBlueContainer,
    onPrimaryContainer = OnNightRoyalBlueContainer,
    secondary = NightVibrantGreen,
    onSecondary = Color(0xFF003816),
    secondaryContainer = NightVibrantGreenContainer,
    onSecondaryContainer = OnNightVibrantGreenContainer,
    tertiary = NightWarmOrange,
    onTertiary = Color(0xFF422000),
    tertiaryContainer = NightWarmOrangeContainer,
    onTertiaryContainer = OnNightWarmOrangeContainer,
    background = EveningPlazaBackground,
    onBackground = EveningPlazaTextPrimary,
    surface = EveningPlazaSurface,
    onSurface = EveningPlazaTextPrimary,
    surfaceVariant = EveningPlazaSurfaceVariant,
    onSurfaceVariant = EveningPlazaTextSecondary,
    outline = EveningPlazaOutline
)

val MallShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ElAbedMallTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MallTypography,
        shapes = MallShapes,
        content = content
    )
}
