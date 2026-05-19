package com.example.random.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.random.model.HeroCombo
import com.example.random.model.TeamSlot
import com.example.random.ui.theme.*
import com.example.random.ui.components.HeroAvatar

@Composable
fun TeamColumn(
    title: String,
    team: List<TeamSlot>,
    titleColor: Color,
    onReRoll: (Int) -> Unit,
    activeCombos: List<HeroCombo> = emptyList(),
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    // 共享动画 - 只创建一次 infiniteTransition
    val infiniteTransition = rememberInfiniteTransition(label = "borderPulse")
    val pulseWidth = infiniteTransition.animateFloat(
        initialValue = 1.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseWidth"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = if (appColors.isDark) 0.dp else 1.dp),
        shape = appColors.cardShape,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                color = titleColor,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 5.dp)
            )

            HorizontalDivider(color = appColors.divider, thickness = 1.dp)

            team.forEachIndexed { index, slot ->
                HeroSlot(
                    slot = slot,
                    onClick = { onReRoll(index) },
                    activeCombos = activeCombos,
                    pulseWidth = pulseWidth.value
                )
                if (index < team.size - 1) {
                    HorizontalDivider(color = appColors.divider, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun HeroSlot(
    slot: TeamSlot,
    onClick: () -> Unit,
    activeCombos: List<HeroCombo> = emptyList(),
    pulseWidth: Float = 2f
) {
    val appColors = LocalAppColors.current

    // 检测当前英雄所属的组合
    val comboForHero = activeCombos.firstOrNull { combo -> slot.ename in combo.heroIds }
    val displayName = comboForHero?.name ?: slot.name
    val borderColor = if (comboForHero != null) Color(AndroidColor.parseColor(comboForHero.borderColor)) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Text(
            slot.positionName,
            style = MaterialTheme.typography.bodySmall,
            color = appColors.textSub,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(appColors.avatarBg)
                .border(
                    width = if (comboForHero != null) pulseWidth.dp else 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            if (slot.avatarUrl.isNotEmpty()) {
                HeroAvatar(
                    imageUrl = slot.avatarUrl,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Text(
            displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = appColors.textMain,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
