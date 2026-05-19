package com.example.random.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class AppColors(
    val isDark: Boolean,
    val bg: Color,
    val card: Color,
    val textMain: Color,
    val textSub: Color,
    val gold: Color,
    val teamA: Color,
    val teamB: Color,
    val ban: Color,
    val divider: Color,
    val avatarBg: Color,
    val darkText: Color,
    val surfaceElevated: Color,
    val surfaceInput: Color,
    val buttonShape: RoundedCornerShape,
    val cardShape: RoundedCornerShape
)

val LightAppColors = AppColors(
    isDark = false,
    bg = BgColor,
    card = SurfaceElevatedLight,
    textMain = TextColor,
    textSub = SubTextColor,
    gold = GoldColor,
    teamA = TeamAColor,
    teamB = TeamBColor,
    ban = BanColor,
    divider = DividerColor,
    avatarBg = AvatarBgColor,
    darkText = DarkTextColor,
    surfaceElevated = SurfaceElevatedLight,
    surfaceInput = SurfaceInputLight,
    buttonShape = RoundedCornerShape(24.dp),
    cardShape = RoundedCornerShape(14.dp)
)

val DarkAppColors = AppColors(
    isDark = true,
    bg = DarkBgColor,
    card = SurfaceElevatedDark,
    textMain = DarkTextMainColor,
    textSub = DarkSubTextColor,
    gold = GoldColor,
    teamA = TeamAColor,
    teamB = TeamBColor,
    ban = BanColor,
    divider = DarkDividerColor,
    avatarBg = DarkAvatarBgColor,
    darkText = DarkTextColor,
    surfaceElevated = SurfaceElevatedDark,
    surfaceInput = SurfaceInputDark,
    buttonShape = RoundedCornerShape(24.dp),
    cardShape = RoundedCornerShape(14.dp)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

val AppColors.current: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
