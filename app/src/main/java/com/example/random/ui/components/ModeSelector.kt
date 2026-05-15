package com.example.random.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.random.ui.theme.GoldColor
import com.example.random.ui.theme.LocalAppColors
import com.example.random.viewmodel.GameMode
import com.example.random.viewmodel.RandomHeroViewModel

@Composable
fun ModeSelector(viewModel: RandomHeroViewModel) {
    val mode by viewModel.mode.collectAsState()
    val appColors = LocalAppColors.current
    val textColor = appColors.textMain

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.noRippleClickable { viewModel.setMode(GameMode.ROLE_BASED) }
        ) {
            RadioButton(
                selected = mode == GameMode.ROLE_BASED,
                onClick = { viewModel.setMode(GameMode.ROLE_BASED) },
                colors = RadioButtonDefaults.colors(selectedColor = GoldColor, unselectedColor = GoldColor)
            )
            Text("按位置分配", fontSize = 14.sp, color = textColor)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.noRippleClickable { viewModel.setMode(GameMode.RANDOM) }
        ) {
            RadioButton(
                selected = mode == GameMode.RANDOM,
                onClick = { viewModel.setMode(GameMode.RANDOM) },
                colors = RadioButtonDefaults.colors(selectedColor = GoldColor, unselectedColor = GoldColor)
            )
            Text("完全随机", fontSize = 14.sp, color = textColor)
        }
    }
}
