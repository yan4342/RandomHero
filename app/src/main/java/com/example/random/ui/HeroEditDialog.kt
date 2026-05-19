package com.example.random.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.random.data.HeroRepository
import com.example.random.model.Hero
import com.example.random.ui.components.HeroAvatar
import com.example.random.ui.components.noRippleClickable
import com.example.random.ui.theme.*

/**
 * 编辑英雄分路弹窗
 * 三个槽位分别对应不同权重：
 * - 主分路 (heroType): 权重 0.6
 * - 副分路 (heroType2): 权重 0.4
 * - 第三分路 (heroType3): 权重 0.3
 * 支持删除英雄（二次确认）
 */
@Composable
fun HeroEditDialog(
    hero: Hero,
    onDismiss: () -> Unit,
    onSave: (Hero) -> Unit,
    onDelete: (Int) -> Unit
) {
    val appColors = LocalAppColors.current

    // 三个槽位独立状态，null 表示未选择
    var slot1 by remember(hero) { mutableStateOf<Int?>(hero.heroType) }
    var slot2 by remember(hero) { mutableStateOf(hero.heroType2) }
    var slot3 by remember(hero) { mutableStateOf(hero.heroType3) }

    // 删除确认弹窗
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = appColors.card,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── 头像 + 名称 ──────────────────────────────────────
                HeroAvatar(
                    imageUrl = HeroRepository.getHeroAvatarUrl(hero.ename),
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    hero.cname,
                    style = MaterialTheme.typography.headlineMedium,
                    color = appColors.textMain
                )

                Text(
                    hero.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.textSub
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = appColors.divider)
                Spacer(modifier = Modifier.height(12.dp))

                // ── 三个槽位 ──────────────────────────────────────
                RoleSlotRow(
                    label = "主分路",
                    weightLabel = "权重 60%",
                    selectedRole = slot1,
                    onSelect = { slot1 = it },
                    otherSelected = listOfNotNull(slot2, slot3),
                    required = true,
                    appColors = appColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                RoleSlotRow(
                    label = "副分路",
                    weightLabel = "权重 40%",
                    selectedRole = slot2,
                    onSelect = { slot2 = it },
                    otherSelected = listOfNotNull(slot1, slot3),
                    required = false,
                    appColors = appColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                RoleSlotRow(
                    label = "第三分路",
                    weightLabel = "权重 30%",
                    selectedRole = slot3,
                    onSelect = { slot3 = it },
                    otherSelected = listOfNotNull(slot1, slot2),
                    required = false,
                    appColors = appColors
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── 按钮 ──────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 删除按钮
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BanColor)
                    ) {
                        Text("删除", style = MaterialTheme.typography.labelLarge)
                    }

                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textSub)
                    ) {
                        Text("取消", style = MaterialTheme.typography.labelLarge)
                    }

                    // 保存按钮
                    Button(
                        onClick = {
                            val updated = hero.copy(
                                heroType = slot1 ?: hero.heroType,
                                heroType2 = slot2,
                                heroType3 = slot3
                            )
                            onSave(updated)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.gold,
                            contentColor = appColors.darkText
                        )
                    ) {
                        Text("保存", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    // ── 删除确认弹窗 ──────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除", color = appColors.textMain) },
            text = {
                Text(
                    "确定要删除「${hero.cname}」吗？\n删除后可通过「恢复默认」还原。",
                    color = appColors.textSub
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete(hero.ename)
                }) {
                    Text("删除", color = BanColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消", color = appColors.textSub)
                }
            },
            containerColor = appColors.card
        )
    }
}

// ── 单个槽位列 ──────────────────────────────────────────────────────────────

@Composable
private fun RoleSlotRow(
    label: String,
    weightLabel: String,
    selectedRole: Int?,
    onSelect: (Int?) -> Unit,
    otherSelected: List<Int>,
    required: Boolean,
    appColors: AppColors
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = appColors.textMain
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                weightLabel,
                style = MaterialTheme.typography.labelSmall,
                color = appColors.textSub
            )
            if (required) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "必选",
                    style = MaterialTheme.typography.labelSmall,
                    color = BanColor
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Hero.ROLES.forEach { (roleId, roleName) ->
                val isSelected = roleId == selectedRole
                val isOccupied = roleId in otherSelected
                val tagColor = roleColor(roleId)
                val bgColor = when {
                    isSelected -> tagColor.copy(alpha = 0.2f)
                    isOccupied -> appColors.divider.copy(alpha = 0.5f)
                    else -> appColors.surfaceInput
                }
                val textColor = when {
                    isSelected -> tagColor
                    isOccupied -> appColors.textSub.copy(alpha = 0.4f)
                    else -> appColors.textSub
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = bgColor,
                    modifier = Modifier
                        .weight(1f)
                        .noRippleClickable {
                            if (isSelected) {
                                // 取消选中（主分路不允许取消）
                                if (!required) onSelect(null)
                            } else if (!isOccupied) {
                                onSelect(roleId)
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            roleName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

// ── 分路颜色 ──────────────────────────────────────────────────────────────

private fun roleColor(roleId: Int): Color = when (roleId) {
    1 -> Color(0xFFE67E22) // 对抗路 - 橙
    2 -> Color(0xFF3498DB) // 中路 - 蓝
    3 -> Color(0xFF2ECC71) // 游走 - 绿
    4 -> Color(0xFF9B59B6) // 打野 - 紫
    5 -> Color(0xFFE74C3C) // 发育路 - 红
    else -> Color.Gray
}
