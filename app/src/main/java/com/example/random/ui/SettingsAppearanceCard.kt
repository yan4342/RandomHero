package com.example.random.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.random.model.MaterialColorPreset
import com.example.random.model.MaterialColorPresets
import com.example.random.model.ThemeColorSettings
import com.example.random.ui.theme.AppColors
import com.example.random.ui.theme.LocalAppColors
import kotlin.math.roundToInt

internal enum class ThemeColorTarget(val label: String) {
    Background("背景色"),
    Card("卡片色"),
    Accent("主题强调色"),
    TeamA("队伍一颜色"),
    TeamB("队伍二颜色")
}

@Composable
internal fun AppearanceColorCard(
    themeColors: ThemeColorSettings,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current

    Card(
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = appColors.cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (appColors.isDark) 0.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "外观颜色",
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.textMain
                    )
                    Text(
                        "背景、卡片、主题色、队伍色",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSub
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThemeColorTarget.entries.forEach { target ->
                    ColorPreview(
                        color = themeColors.colorFor(target, appColors),
                        size = 30.dp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AppearanceColorDialog(
    themeColors: ThemeColorSettings,
    onPresetSelected: (ThemeColorTarget, Int) -> Unit,
    onCustomClick: (ThemeColorTarget) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("外观颜色", color = appColors.textMain) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ThemeColorTarget.entries.forEach { target ->
                    ColorTargetSection(
                        target = target,
                        currentColor = themeColors.colorFor(target, appColors),
                        appColors = appColors,
                        onPresetSelected = { color -> onPresetSelected(target, color) },
                        onCustomClick = { onCustomClick(target) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成", color = appColors.gold)
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text("恢复默认", color = appColors.textSub)
            }
        },
        containerColor = appColors.card
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorTargetSection(
    target: ThemeColorTarget,
    currentColor: Int,
    appColors: AppColors,
    onPresetSelected: (Int) -> Unit,
    onCustomClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ColorPreview(color = currentColor)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    target.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = appColors.textMain
                )
                Text(
                    colorToHex(currentColor),
                    style = MaterialTheme.typography.labelMedium,
                    color = appColors.textSub
                )
            }
            OutlinedButton(
                onClick = onCustomClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textMain)
            ) {
                Text("自定义")
            }
        }

        val defaultColor = target.defaultColorInt(appColors)
        val presets = listOf(MaterialColorPreset("默认", defaultColor)) +
            MaterialColorPresets.filter { it.color != defaultColor }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
                PresetSwatch(
                    preset = preset,
                    selected = currentColor == preset.color,
                    appColors = appColors,
                    onClick = { onPresetSelected(preset.color) }
                )
            }
        }
    }
}

@Composable
private fun PresetSwatch(
    preset: MaterialColorPreset,
    selected: Boolean,
    appColors: AppColors,
    onClick: () -> Unit
) {
    val color = Color(preset.color)
    val borderColor = if (selected) appColors.textMain else appColors.divider
    val borderWidth = if (selected) 3.dp else 1.dp

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.Transparent,
            shape = CircleShape,
            border = BorderStroke(borderWidth, borderColor),
            modifier = Modifier.fillMaxSize(),
            content = {}
        )
    }
}

@Composable
private fun ColorPreview(color: Int, size: androidx.compose.ui.unit.Dp = 42.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(color))
    )
}

@Composable
internal fun CustomColorDialog(
    target: ThemeColorTarget,
    initialColor: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val appColors = LocalAppColors.current
    var red by remember(target, initialColor) { mutableIntStateOf(redOf(initialColor)) }
    var green by remember(target, initialColor) { mutableIntStateOf(greenOf(initialColor)) }
    var blue by remember(target, initialColor) { mutableIntStateOf(blueOf(initialColor)) }
    var hexText by remember(target, initialColor) { mutableStateOf(colorToHex(initialColor)) }
    val parsedColor = parseHexColor(hexText)
    val isValid = parsedColor != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${target.label}自定义", color = appColors.textMain) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColorPreview(color = parsedColor ?: rgbToColorInt(red, green, blue))
                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { value ->
                            hexText = value.uppercase()
                            parseHexColor(value)?.let { color ->
                                red = redOf(color)
                                green = greenOf(color)
                                blue = blueOf(color)
                            }
                        },
                        label = { Text("#RRGGBB") },
                        isError = !isValid,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = appColors.textMain),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!isValid) {
                    Text(
                        "请输入 6 位十六进制颜色，如 #1E88E5",
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.ban
                    )
                }

                RgbSlider(
                    label = "R",
                    value = red,
                    color = Color.Red,
                    onValueChange = {
                        red = it
                        hexText = colorToHex(rgbToColorInt(red, green, blue))
                    }
                )
                RgbSlider(
                    label = "G",
                    value = green,
                    color = Color(0xFF2E7D32),
                    onValueChange = {
                        green = it
                        hexText = colorToHex(rgbToColorInt(red, green, blue))
                    }
                )
                RgbSlider(
                    label = "B",
                    value = blue,
                    color = Color(0xFF1565C0),
                    onValueChange = {
                        blue = it
                        hexText = colorToHex(rgbToColorInt(red, green, blue))
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsedColor?.let(onSave) },
                enabled = isValid
            ) {
                Text("保存", color = if (isValid) appColors.gold else appColors.textSub)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = appColors.textSub)
            }
        },
        containerColor = appColors.card
    )
}

@Composable
private fun RgbSlider(
    label: String,
    value: Int,
    color: Color,
    onValueChange: (Int) -> Unit
) {
    val appColors = LocalAppColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "$label $value",
            style = MaterialTheme.typography.labelMedium,
            color = appColors.textMain,
            modifier = Modifier.width(48.dp)
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt().coerceIn(0, 255)) },
            valueRange = 0f..255f,
            steps = 254,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

internal fun ThemeColorSettings.colorFor(target: ThemeColorTarget, appColors: AppColors): Int {
    return when (target) {
        ThemeColorTarget.Background -> backgroundColor ?: appColors.bg.toArgbInt()
        ThemeColorTarget.Card -> cardColor ?: appColors.card.toArgbInt()
        ThemeColorTarget.Accent -> accentColor
        ThemeColorTarget.TeamA -> teamAColor
        ThemeColorTarget.TeamB -> teamBColor
    }
}

private fun ThemeColorTarget.defaultColorInt(appColors: AppColors): Int {
    return when (this) {
        ThemeColorTarget.Background -> ThemeColorSettings.Default.backgroundColor
            ?: appColors.bg.toArgbInt()
        ThemeColorTarget.Card -> ThemeColorSettings.Default.cardColor
            ?: appColors.card.toArgbInt()
        ThemeColorTarget.Accent -> ThemeColorSettings.DEFAULT_ACCENT_COLOR
        ThemeColorTarget.TeamA -> ThemeColorSettings.DEFAULT_TEAM_A_COLOR
        ThemeColorTarget.TeamB -> ThemeColorSettings.DEFAULT_TEAM_B_COLOR
    }
}

internal fun colorToHex(color: Int): String {
    return "#${(color and 0x00FFFFFF).toString(16).padStart(6, '0').uppercase()}"
}

private fun parseHexColor(value: String): Int? {
    val normalized = value.trim().removePrefix("#")
    if (!Regex("^[0-9A-Fa-f]{6}$").matches(normalized)) return null
    return 0xFF000000.toInt() or normalized.toInt(16)
}

private fun rgbToColorInt(red: Int, green: Int, blue: Int): Int {
    return 0xFF000000.toInt() or
        (red.coerceIn(0, 255) shl 16) or
        (green.coerceIn(0, 255) shl 8) or
        blue.coerceIn(0, 255)
}

private fun redOf(color: Int): Int = (color shr 16) and 0xFF
private fun greenOf(color: Int): Int = (color shr 8) and 0xFF
private fun blueOf(color: Int): Int = color and 0xFF

private fun Color.toArgbInt(): Int {
    val alpha = (this.alpha * 255).roundToInt().coerceIn(0, 255)
    val red = (this.red * 255).roundToInt().coerceIn(0, 255)
    val green = (this.green * 255).roundToInt().coerceIn(0, 255)
    val blue = (this.blue * 255).roundToInt().coerceIn(0, 255)
    return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}
