package com.example.random.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.random.R
import com.example.random.data.HeroDataSource
import com.example.random.data.HeroRepository
import com.example.random.model.Hero
import com.example.random.ui.components.HeroSelectorDialog
import com.example.random.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current

    // 英雄列表状态
    var heroes by remember { mutableStateOf(HeroRepository.heroList) }

    // 编辑弹窗状态
    var showEditDialog by remember { mutableStateOf(false) }
    var editingHero by remember { mutableStateOf<Hero?>(null) }

    // 英雄选择器弹窗（编辑分路用）
    var showHeroSelector by remember { mutableStateOf(false) }

    // 添加弹窗状态
    var showAddDialog by remember { mutableStateOf(false) }

    // 恢复默认确认弹窗
    var showResetConfirm by remember { mutableStateOf(false) }

    // 导入/导出
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val success = HeroDataSource.exportConfig(context, it)
            Toast.makeText(context, if (success) "导出成功" else "导出失败", Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val success = HeroDataSource.importConfig(context, it)
            if (success) {
                HeroRepository.reload(context)
                heroes = HeroRepository.heroList
                Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "导入失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.bg)
    ) {
        // ── 顶部栏 ──────────────────────────────────────────────────────
        TopAppBar(
            title = {
                Text(
                    "英雄分路设置",
                    style = MaterialTheme.typography.headlineMedium,
                    color = appColors.textMain
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Image(
                        painter = painterResource(id = R.drawable.back_button),
                        contentDescription = "返回",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = appColors.bg
            )
        )

        // ── 可滚动主体 ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── 主操作卡片：选择英雄 ──────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = appColors.card),
                shape = appColors.cardShape,
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (appColors.isDark) 0.dp else 1.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "选择英雄编辑分路",
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.textMain
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "点击下方按钮选择英雄，支持搜索和按分路筛选",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSub
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showHeroSelector = true },
                        shape = appColors.buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.gold,
                            contentColor = appColors.darkText
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(48.dp)
                    ) {
                        Text("选择英雄", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // ── 数据管理卡片 ──────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = appColors.card),
                shape = appColors.cardShape,
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (appColors.isDark) 0.dp else 1.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "数据管理",
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.textMain,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // 第一行：添加 + 恢复默认
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textMain)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("添加英雄", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BanColor)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("恢复默认", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    // 第二行：导入 + 导出
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textMain)
                        ) {
                            Text("导入配置", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = { exportLauncher.launch("hero_config.json") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textMain)
                        ) {
                            Text("导出配置", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // ── 预留扩展空间 ──────────────────────────────────────────────
            // 未来可在此处添加更多设置卡片

            Spacer(modifier = Modifier.height(4.dp))
        }

        // 底部安全区域
        Spacer(modifier = Modifier.navigationBarsPadding())
    }

    // ── 编辑弹窗 ──────────────────────────────────────────────────────
    if (showEditDialog && editingHero != null) {
        val currentHero = heroes.find { it.ename == editingHero!!.ename }
        if (currentHero != null) {
            HeroEditDialog(
                hero = currentHero,
                onDismiss = {
                    showEditDialog = false
                    editingHero = null
                },
                onSave = { updatedHero ->
                    HeroDataSource.saveHero(updatedHero)
                    HeroRepository.reload(context)
                    heroes = HeroRepository.heroList
                    showEditDialog = false
                    editingHero = null
                    Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                },
                onDelete = { ename ->
                    HeroDataSource.deleteHero(ename)
                    HeroRepository.reload(context)
                    heroes = HeroRepository.heroList
                    showEditDialog = false
                    editingHero = null
                    Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // ── 添加弹窗 ──────────────────────────────────────────────────────
    if (showAddDialog) {
        AddHeroDialog(
            existingEnameList = heroes.map { it.ename },
            onDismiss = { showAddDialog = false },
            onSave = { newHero ->
                HeroDataSource.saveHero(newHero)
                HeroRepository.reload(context)
                heroes = HeroRepository.heroList
                showAddDialog = false
                Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ── 恢复默认确认弹窗 ──────────────────────────────────────────────
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("恢复默认", color = appColors.textMain) },
            text = { Text("将清除所有自定义修改，恢复为默认英雄数据。确定继续？", color = appColors.textSub) },
            confirmButton = {
                TextButton(onClick = {
                    HeroDataSource.resetToDefault()
                    HeroRepository.reload(context)
                    heroes = HeroRepository.heroList
                    showResetConfirm = false
                    Toast.makeText(context, "已恢复默认", Toast.LENGTH_SHORT).show()
                }) {
                    Text("确定", color = BanColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("取消", color = appColors.textSub)
                }
            },
            containerColor = appColors.card
        )
    }

    // ── 英雄选择器弹窗 ──────────────────────────────────────────────────
    if (showHeroSelector) {
        HeroSelectorDialog(
            heroes = heroes,
            onDismiss = { showHeroSelector = false },
            onHeroSelected = { hero ->
                showHeroSelector = false
                editingHero = hero
                showEditDialog = true
            }
        )
    }
}
