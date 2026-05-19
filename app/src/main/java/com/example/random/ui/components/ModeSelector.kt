package com.example.random.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.random.ui.theme.*
import com.example.random.viewmodel.GameMode
import com.example.random.viewmodel.RandomHeroViewModel

@Composable
fun ModeSelector(viewModel: RandomHeroViewModel, modifier: Modifier = Modifier) {
    val mode by viewModel.mode.collectAsState()
    val appColors = LocalAppColors.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = appColors.surfaceInput,
        modifier = modifier
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            ModeChip(
                text = "按位置分配",
                selected = mode == GameMode.ROLE_BASED,
                appColors = appColors,
                modifier = Modifier.weight(1f),
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

@Composable
private fun ModeChip(
    text: String,
    selected: Boolean,
    appColors: AppColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) appColors.gold else Color.Transparent,
        animationSpec = tween(200),
        label = "modeChipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) appColors.darkText else appColors.textSub,
        animationSpec = tween(200),
        label = "modeChipText"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .noRippleClickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
}
