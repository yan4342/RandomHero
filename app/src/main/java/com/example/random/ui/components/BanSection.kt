package com.example.random.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.random.data.HeroRepository
import com.example.random.model.Hero
import com.example.random.model.HeroCombo
import com.example.random.ui.theme.*
import com.example.random.viewmodel.RandomHeroViewModel

@Composable
fun BanSection(viewModel: RandomHeroViewModel, activeCombos: List<HeroCombo> = emptyList()) {
    val banList by viewModel.banList.collectAsState()
    val appColors = LocalAppColors.current

    Card(
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = if (appColors.isDark) 0.dp else 1.dp),
        shape = appColors.cardShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Ban位",
                color = BanColor,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Grid of 8 slots (4x2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(4) { col ->
                            val index = row * 4 + col
                            BanSlot(
                                hero = banList.getOrNull(index),
                                onSelect = { viewModel.selectBanHero(index) },
                                onRemove = { viewModel.removeBan(index) },
                                activeCombos = activeCombos,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BanSlot(
    hero: Hero?,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
    activeCombos: List<HeroCombo> = emptyList(),
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val defaultBorderColor = if (appColors.isDark) DarkDividerColor else Color(0xFFDDDDDD)

    // 检测当前英雄所属的组合
    val comboForHero = hero?.let { h -> activeCombos.firstOrNull { combo -> h.ename in combo.heroIds } }
    val displayName = comboForHero?.name ?: hero?.cname ?: "点击禁用"
    val borderColor = if (comboForHero != null) Color(AndroidColor.parseColor(comboForHero.borderColor)) else defaultBorderColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(2.dp)
            .noRippleClickable { onSelect() }
    ) {
        Box(
            modifier = Modifier.size(46.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            // Avatar Wrapper
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(appColors.avatarBg)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hero != null) {
                    HeroAvatar(
                        imageUrl = HeroRepository.getHeroAvatarUrl(hero.ename),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("+", fontSize = 20.sp, color = appColors.textSub)
                }
            }

            // Remove Button
            if (hero != null) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(BanColor.copy(alpha = 0.85f))
                        .noRippleClickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("×", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

//        Text(
//            text = displayName,
//            style = MaterialTheme.typography.labelSmall,
//            color = appColors.textSub,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis,
//            modifier = Modifier.padding(top = 2.dp)
//        )
    }
}
