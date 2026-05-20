package com.example.random.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.random.ui.theme.*
import com.example.random.viewmodel.GameMode
import com.example.random.viewmodel.RandomHeroViewModel

@Composable
fun ModeSelector(viewModel: RandomHeroViewModel, modifier: Modifier = Modifier) {
    val mode by viewModel.mode.collectAsState()
    val appColors = LocalAppColors.current
    val density = LocalDensity.current

    // 记录单个 chip 的宽度和行高，用于确定指示器尺寸
    var chipWidth by remember { mutableStateOf(0.dp) }
    var rowHeight by remember { mutableStateOf(0.dp) }

    // 滑动偏移动画：ROLE_BASED → 0.dp，RANDOM → chipWidth
    val indicatorOffset by animateDpAsState(
        targetValue = if (mode == GameMode.ROLE_BASED) 0.dp else chipWidth,
        animationSpec = tween(durationMillis = 250),
        label = "modeIndicatorOffset"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = appColors.surfaceInput,
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(4.dp)) {
            // 滑动的金色指示器（底层）
            if (chipWidth > 0.dp && rowHeight > 0.dp) {
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .size(width = chipWidth, height = rowHeight)
                        .clip(RoundedCornerShape(20.dp))
                        .background(appColors.gold)
                )
            }

            // 文字层（上层）
            Row(
                modifier = Modifier.onSizeChanged { size ->
                    rowHeight = with(density) { size.height.toDp() }
                }
            ) {
                ModeChip(
                    text = "按位置分配",
                    selected = mode == GameMode.ROLE_BASED,
                    appColors = appColors,
                    modifier = Modifier
                        .weight(1f)
                        .onSizeChanged { size ->
                            chipWidth = with(density) { size.width.toDp() }
                        },
                    onClick = { viewModel.setMode(GameMode.ROLE_BASED) }
                )
                ModeChip(
                    text = "完全随机",
                    selected = mode == GameMode.RANDOM,
                    appColors = appColors,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setMode(GameMode.RANDOM) }
                )
            }
        }
    }
}

@Composable
private fun ModeChip(
    text: String,
    selected: Boolean,
    appColors: AppColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (selected) appColors.darkText else appColors.textSub,
        animationSpec = tween(250),
        label = "modeChipText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .noRippleClickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}
