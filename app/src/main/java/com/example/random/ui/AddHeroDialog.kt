package com.example.random.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.Dialog
import com.example.random.model.Hero
import com.example.random.ui.components.noRippleClickable
import com.example.random.ui.theme.*

/**
 * 添加新英雄弹窗
 */
@Composable
fun AddHeroDialog(
    existingEnameList: List<Int>,
    onDismiss: () -> Unit,
    onSave: (Hero) -> Unit
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current

    // 表单状态
    var enameText by remember { mutableStateOf("") }
    var cname by remember { mutableStateOf("") }
    var idName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    // 三个槽位独立状态
    var slot1 by remember { mutableStateOf<Int?>(null) }
    var slot2 by remember { mutableStateOf<Int?>(null) }
    var slot3 by remember { mutableStateOf<Int?>(null) }
    var skinName by remember { mutableStateOf("") }
    var mossIdText by remember { mutableStateOf("") }

    // 验证
    val isEnameValid = enameText.toIntOrNull() != null && enameText.toInt() > 0
    val isEnameDuplicate = enameText.toIntOrNull()?.let { it in existingEnameList } == true
    val isFormValid = isEnameValid && !isEnameDuplicate && cname.isNotBlank() &&
        idName.isNotBlank() && title.isNotBlank() && slot1 != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = appColors.card,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // ── 标题 ──────────────────────────────────────
                Text(
                    "添加新英雄",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textMain,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = appColors.divider)
                Spacer(modifier = Modifier.height(12.dp))

                // ── 表单 ──────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 英雄 ID
                    FormField(
                        label = "英雄ID (ename) *",
                        value = enameText,
                        onValueChange = { enameText = it },
                        placeholder = "583",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = enameText.isNotEmpty() && (!isEnameValid || isEnameDuplicate),
                        errorMessage = when {
                            enameText.isNotEmpty() && !isEnameValid -> "请输入有效数字"
                            isEnameDuplicate -> "该ID已存在"
                            else -> null
                        }
                    )

                    // 英雄名称
                    FormField(
                        label = "英雄名称 (cname) *",
                        value = cname,
                        onValueChange = { cname = it },
                        placeholder = "元流之子(刺客)"
                    )

                    // 拼音 ID
                    FormField(
                        label = "拼音ID (idName) *",
                        value = idName,
                        onValueChange = { idName = it },
                        placeholder = "yuanliuzhizi_assassin"
                    )

                    // 称号
                    FormField(
                        label = "称号 (title) *",
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "破局之识"
                    )

                    // 分路选择
                    Text(
                        "分路 *（主分路必选）",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors.textMain
                    )

                    // 主分路
                    AddHeroRoleSlot(
                        label = "主分路",
                        weightLabel = "权重 60%",
                        selectedRole = slot1,
                        onSelect = { slot1 = it },
                        otherSelected = listOfNotNull(slot2, slot3),
                        required = true,
                        appColors = appColors
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 副分路
                    AddHeroRoleSlot(
                        label = "副分路",
                        weightLabel = "权重 40%",
                        selectedRole = slot2,
                        onSelect = { slot2 = it },
                        otherSelected = listOfNotNull(slot1, slot3),
                        required = false,
                        appColors = appColors
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 第三分路
                    AddHeroRoleSlot(
                        label = "第三分路",
                        weightLabel = "权重 30%",
                        selectedRole = slot3,
                        onSelect = { slot3 = it },
                        otherSelected = listOfNotNull(slot1, slot2),
                        required = false,
                        appColors = appColors
                    )

                    // 皮肤名称（可选）
                    FormField(
                        label = "皮肤名称（可选，用|分隔）",
                        value = skinName,
                        onValueChange = { skinName = it },
                        placeholder = "破局之识",
                        singleLine = false
                    )

                    // Moss ID（可选）
                    FormField(
                        label = "Moss ID（可选）",
                        value = mossIdText,
                        onValueChange = { mossIdText = it },
                        placeholder = "9132",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── 按钮 ──────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textSub)
                    ) {
                        Text("取消", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (isFormValid) {
                                val newHero = Hero(
                                    ename = enameText.toInt(),
                                    cname = cname.trim(),
                                    idName = idName.trim(),
                                    title = title.trim(),
                                    heroType = slot1!!,
                                    heroType2 = slot2,
                                    heroType3 = slot3,
                                    skinName = skinName.trim().ifBlank { cname.trim() },
                                    mossId = mossIdText.toIntOrNull() ?: 0
                                )
                                onSave(newHero)
                            } else {
                                Toast.makeText(context, "请填写所有必填项", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.gold,
                            contentColor = appColors.darkText
                        )
                    ) {
                        Text("保存", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true
) {
    val appColors = LocalAppColors.current

    Column {
        Text(
            label,
            fontSize = 13.sp,
            color = appColors.textSub,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        placeholder,
                        fontSize = 14.sp,
                        color = appColors.textSub.copy(alpha = 0.5f)
                    )
                }
            },
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = appColors.textMain
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!singleLine) Modifier.height(80.dp) else Modifier),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (appColors.isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5),
                unfocusedContainerColor = if (appColors.isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = appColors.textMain,
                errorIndicatorColor = Color.Transparent
            ),
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            isError = isError
        )
        if (errorMessage != null) {
            Text(
                errorMessage,
                fontSize = 11.sp,
                color = BanColor,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ── 分路槽位（添加英雄专用） ──────────────────────────────────────────────────────

@Composable
private fun AddHeroRoleSlot(
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
                .padding(bottom = 4.dp)
        ) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = appColors.textMain
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                weightLabel,
                fontSize = 11.sp,
                color = appColors.textSub
            )
            if (required) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "必选",
                    fontSize = 10.sp,
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
                val tagColor = when (roleId) {
                    1 -> Color(0xFFE67E22)
                    2 -> Color(0xFF3498DB)
                    3 -> Color(0xFF2ECC71)
                    4 -> Color(0xFF9B59B6)
                    5 -> Color(0xFFE74C3C)
                    else -> Color.Gray
                }
                val bgColor = when {
                    isSelected -> tagColor.copy(alpha = 0.2f)
                    isOccupied -> appColors.divider.copy(alpha = 0.5f)
                    else -> if (appColors.isDark) Color(0xFF2A2A2A) else Color(0xFFF5F5F5)
                }
                val textColor = when {
                    isSelected -> tagColor
                    isOccupied -> appColors.textSub.copy(alpha = 0.4f)
                    else -> appColors.textSub
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = bgColor,
                    modifier = Modifier
                        .weight(1f)
                        .noRippleClickable {
                            if (isSelected) {
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
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
