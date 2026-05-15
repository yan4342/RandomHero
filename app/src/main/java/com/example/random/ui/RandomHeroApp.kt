package com.example.random.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.io.FileOutputStream
import com.example.random.R
import com.example.random.data.HeroRepository
import com.example.random.model.Hero
import com.example.random.model.HeroCombo
import com.example.random.model.TeamSlot
import com.example.random.viewmodel.RandomHeroViewModel
import com.example.random.ui.components.HeroAvatar
import com.example.random.ui.components.HeroSelectorDialog
import com.example.random.ui.components.noRippleClickable

// Colors from WeChat version
val BgColor = Color(0xFFF5F5F5)
val DarkBgColor = Color(0xFF121212)
val CardColor = Color.White
val DarkCardColor = Color(0xFF1E1E1E)
val GoldColor = Color(0xFFBCA676)
val DarkTextColor = Color(0xFF30210D)
val TeamAColor = Color(0xFF3D8BFD)
val TeamBColor = Color(0xFFE33E33)
val BanColor = Color(0xFFD81E06)
val DividerColor = Color(0xFFEEEEEE)
val DarkDividerColor = Color(0xFF333333)
val TextColor = Color(0xFF333333)
val DarkTextMainColor = Color(0xFFDDDDDD)
val SubTextColor = Color(0xFF888888)
val DarkSubTextColor = Color(0xFF999999)
val AvatarBgColor = Color(0xFFEEEEEE)
val DarkAvatarBgColor = Color(0xFF2C2C2C)

private val FzmeiheiFont = FontFamily(Font(R.font.fzmh))

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RandomHeroApp() {
    val viewModel: RandomHeroViewModel = viewModel()
    val isDark = isSystemInDarkTheme()
    val activeCombos by viewModel.activeCombos.collectAsState()
    val showShareScreen by viewModel.showShareScreen.collectAsState()
    val expandBanBar by viewModel.expandBanBar.collectAsState()
    val autoShare by viewModel.autoShare.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Background capture of ShareResultScreen (no visible transition)
    LaunchedEffect(autoShare) {
        if (autoShare) {
            kotlinx.coroutines.delay(100)
            captureShareInBackground(context, viewModel.getShareData())
            viewModel.closeShareScreen()
        }
    }

    // Navigate to share screen (view mode)
    if (showShareScreen && !autoShare) {
        ShareResultScreen(
            data = viewModel.getShareData(),
            expandBanBar = expandBanBar,
            onBackClick = { viewModel.closeShareScreen() }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color.Black else BgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Selection
            ModeSelector(viewModel)

            Spacer(modifier = Modifier.height(15.dp))

            // Ban Section
            BanSection(viewModel, activeCombos)

            Spacer(modifier = Modifier.height(10.dp))

            // Teams side by side - Reduced spacing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TeamColumn(
                    title = "队伍上",
                    team = viewModel.teamA.collectAsState().value,
                    titleColor = TeamAColor,
                    onReRoll = { index -> viewModel.reRollOne("A", index) },
                    activeCombos = activeCombos,
                    modifier = Modifier.weight(1f)
                )

                TeamColumn(
                    title = "队伍下",
                    team = viewModel.teamB.collectAsState().value,
                    titleColor = TeamBColor,
                    onReRoll = { index -> viewModel.reRollOne("B", index) },
                    activeCombos = activeCombos,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Randomize Button
                Button(
                    onClick = { viewModel.randomize() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldColor,
                        contentColor = DarkTextColor
                    ),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        "重新抽取",
                        fontSize = 18.sp,
                        fontFamily = FzmeiheiFont
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))

                // Share Icon - click to share, long press for menu
                var showShareMenu by remember { mutableStateOf(false) }
                Box {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .combinedClickable(
                                onClick = { viewModel.openShareScreen(expandBan = true, autoShare = true) },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showShareMenu = true
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "分享",
                            modifier = Modifier.size(24.dp),
                            tint = if (isDark) DarkTextMainColor else TextColor
                        )
                    }
                    DropdownMenu(
                        expanded = showShareMenu,
                        onDismissRequest = { showShareMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("查看结果") },
                            onClick = {
                                showShareMenu = false
                                viewModel.openShareScreen(expandBan = false)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Hero Selector Modal
        if (viewModel.showSelector.collectAsState().value) {
            HeroSelectorDialog(
                heroes = viewModel.filteredHeroes.collectAsState().value,
                onDismiss = { viewModel.closeSelector() },
                onHeroSelected = { viewModel.confirmBan(it) }
            )
        }
    }
}

@Composable
fun ModeSelector(viewModel: RandomHeroViewModel) {
    val mode by viewModel.mode.collectAsState()
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) DarkTextMainColor else TextColor
    
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.noRippleClickable { viewModel.setMode("role") }
        ) {
            RadioButton(
                selected = mode == "role",
                onClick = { viewModel.setMode("role") },
                colors = RadioButtonDefaults.colors(selectedColor = GoldColor, unselectedColor = GoldColor)
            )
            Text("按位置分配", fontSize = 14.sp, color = textColor)
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.noRippleClickable { viewModel.setMode("random") }
        ) {
            RadioButton(
                selected = mode == "random",
                onClick = { viewModel.setMode("random") },
                colors = RadioButtonDefaults.colors(selectedColor = GoldColor, unselectedColor = GoldColor)
            )
            Text("完全随机", fontSize = 14.sp, color = textColor)
        }
    }
}

@Composable
fun BanSection(viewModel: RandomHeroViewModel, activeCombos: List<HeroCombo> = emptyList()) {
    val banList by viewModel.banList.collectAsState()
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) DarkCardColor else CardColor
    
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
        shape = RoundedCornerShape(5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Ban位",
                color = BanColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Grid of 8 slots (4x2)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
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
    val isDark = isSystemInDarkTheme()
    val avatarBg = if (isDark) DarkAvatarBgColor else AvatarBgColor
    val defaultBorderColor = if (isDark) DarkDividerColor else Color(0xFFDDDDDD)
    val textMain = if (isDark) DarkTextMainColor else TextColor
    val textSub = if (isDark) DarkSubTextColor else Color(0xFF666666)
    
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
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            // Avatar Wrapper
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(avatarBg)
                    .border(1.dp, borderColor, RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hero != null) {
                    HeroAvatar(
                        imageUrl = HeroRepository.getHeroAvatarUrl(hero.ename),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("+", fontSize = 20.sp, color = textSub)
                }
            }
            
            // Remove Button
            if (hero != null) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(BanColor)
                        .noRippleClickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("×", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Text(
            text = displayName,
            fontSize = 11.sp,
            color = textSub,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun TeamColumn(
    // 团队列
    title: String,
    team: List<TeamSlot>,
    titleColor: Color,
    onReRoll: (Int) -> Unit,
    activeCombos: List<HeroCombo> = emptyList(),
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) DarkCardColor else CardColor
    val divColor = if (isDark) DarkDividerColor else DividerColor
    
    Card(
        // 团队列卡片
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
        shape = RoundedCornerShape(5.dp),// 圆角
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(20.dp),// 内边距
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                color = titleColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 5.dp)// 垂直内边距
            )
            
            HorizontalDivider(color = divColor, thickness = 1.dp)// 水平分隔线
            
            team.forEachIndexed { index, slot ->
                HeroSlot(slot, onClick = { onReRoll(index) }, activeCombos = activeCombos)
                if (index < team.size - 1) {
                    HorizontalDivider(color = divColor, thickness = 1.dp)// 水平分隔线
                }
            }
        }
    }
}

@Composable
fun HeroSlot(slot: TeamSlot, onClick: () -> Unit, activeCombos: List<HeroCombo> = emptyList()) {// 英雄槽位
    val isDark = isSystemInDarkTheme()
    val avatarBg = if (isDark) DarkAvatarBgColor else AvatarBgColor
    val textMain = if (isDark) DarkTextMainColor else TextColor
    val textSub = if (isDark) DarkSubTextColor else Color(0xFF888888)
    
    // 检测当前英雄所属的组合
    val comboForHero = activeCombos.firstOrNull { combo -> slot.ename in combo.heroIds }
    val displayName = comboForHero?.name ?: slot.name
    val borderColor = if (comboForHero != null) Color(AndroidColor.parseColor(comboForHero.borderColor)) else Color.Transparent
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable { onClick() }
            .padding(vertical = 8.dp)// 垂直内边距
    ) {
        Text(
            slot.positionName,
            fontSize = 12.sp,
            color = textSub,
            modifier = Modifier.padding(bottom = 2.dp)// 底部内边距
        )
        
        val infiniteTransition = rememberInfiniteTransition(label = "borderPulse")
        val pulseWidth = infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseWidth"
        )
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(avatarBg)
                .border(
                    width = if (comboForHero != null) pulseWidth.value.dp else 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(5.dp)
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textMain,
            modifier = Modifier.padding(top = 2.dp)// 顶部内边距
        )
    }
}

// ── Background capture of ShareResultScreen ────────────────────────────────────

private fun cleanOldShareFiles(context: android.content.Context) {
    context.cacheDir.listFiles()?.forEach { file ->
        if (file.name.startsWith("share_result_") && file.name.endsWith(".png")) {
            file.delete()
        }
    }
}

private suspend fun captureShareInBackground(context: android.content.Context, shareData: com.example.random.model.ShareResultData) {
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
        val activity = context as? Activity ?: return@withContext
        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val metrics = context.resources.displayMetrics
        val width = maxOf(metrics.widthPixels, metrics.heightPixels)
        val height = minOf(metrics.widthPixels, metrics.heightPixels)

        val composeView = ComposeView(context)
        composeView.visibility = View.INVISIBLE
        composeView.setContent {
            ShareResultScreen(
                data = shareData,
                expandBanBar = true,
                skipOrientationLock = true,
                onBackClick = {}
            )
        }

        rootView.addView(
            composeView,
            ViewGroup.LayoutParams(width, height)
        )

        // Allow composition to settle
        kotlinx.coroutines.delay(300)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        composeView.draw(canvas)

        // Remove from hierarchy
        rootView.removeView(composeView)

        cleanOldShareFiles(context)
        val file = File(context.cacheDir, "share_result_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享抽取结果"))
    }
}
