package com.example.random.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.random.R
import com.example.random.data.HeroRepository
import com.example.random.model.Hero
import com.example.random.model.HeroCombo
import com.example.random.model.ShareResultData
import com.example.random.model.TeamSlot
import com.example.random.ui.components.HeroAvatar
import com.example.random.ui.theme.*
import java.io.File
import java.io.FileOutputStream

// ── Main Share Result Screen (横屏布局, 参考 TeamRoomScreen 风格) ──────────────

@Composable
fun ShareResultScreen(
    data: ShareResultData,
    expandBanBar: Boolean = false,
    skipOrientationLock: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val view = LocalView.current
    val context = LocalContext.current

    // 强制横屏 + 全面屏沉浸模式 (skip during background capture)
    if (!skipOrientationLock) {
        LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        ImmersiveStatusBar(view)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(listOf(GradientTop, GradientBottom))
            )
    ) {
        // ── 底部白色亮光流线型花纹 (绘制在最底层，不遮挡其他内容) ──
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height

            // 流线路径 - 3 条交错的弧线
            val lines = listOf(
                listOf(
                    Offset(0f, h * 0.7f),
                    Offset(w * 0.15f, h * 0.45f),
                    Offset(w * 0.35f, h * 0.6f),
                    Offset(w * 0.55f, h * 0.3f),
                    Offset(w * 0.75f, h * 0.5f),
                    Offset(w * 0.9f, h * 0.25f),
                    Offset(w, h * 0.4f)
                ),
                listOf(
                    Offset(0f, h * 0.85f),
                    Offset(w * 0.2f, h * 0.55f),
                    Offset(w * 0.4f, h * 0.7f),
                    Offset(w * 0.6f, h * 0.4f),
                    Offset(w * 0.8f, h * 0.6f),
                    Offset(w, h * 0.35f)
                ),
                listOf(
                    Offset(w * 0.1f, h),
                    Offset(w * 0.25f, h * 0.65f),
                    Offset(w * 0.45f, h * 0.8f),
                    Offset(w * 0.65f, h * 0.5f),
                    Offset(w * 0.85f, h * 0.7f),
                    Offset(w, h * 0.55f)
                )
            )

            // 绘制流线
            for (points in lines) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cpX = (prev.x + curr.x) / 2f
                        quadraticTo(prev.x + (curr.x - prev.x) * 0.3f, prev.y, cpX, (prev.y + curr.y) / 2f)
                        quadraticTo(curr.x - (curr.x - prev.x) * 0.3f, curr.y, curr.x, curr.y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.24f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                )
            }

            // 绘制断点亮点
            val brightDots = listOf(
                Offset(w * 0.15f, h * 0.45f),
                Offset(w * 0.35f, h * 0.6f),
                Offset(w * 0.75f, h * 0.5f),
                Offset(w * 0.2f, h * 0.55f),
                Offset(w * 0.6f, h * 0.4f),
                Offset(w * 0.8f, h * 0.6f),
                Offset(w * 0.65f, h * 0.5f),
                Offset(w * 0.45f, h * 0.8f)
            )
            for (dot in brightDots) {
                // 外光晕
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = 6f,
                    center = dot
                )
                // 核心亮点
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 2f,
                    center = dot
                )
            }
        }

        // Subtle star-like dots (same as TeamRoomScreen)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stepX = size.width / 14f
            val stepY = size.height / 10f
            for (i in 1..26) {
                val x = (i * 37 % 14) * stepX + stepX / 2
                val y = (i * 23 % 10) * stepY + stepY / 2
                drawCircle(
                    color = Color.White.copy(alpha = 0.22f),
                    radius = 2.2f,
                    center = Offset(x, y)
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 58.dp)
                    .height(84.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧: 返回 + 标题
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = TextOnDark
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "5v5 英雄抽取",
                                color = TextOnDark,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(2.dp))

                        }
                    }
                }

            }

            // ── Hero Grid ──
            HeroTeamGrid(
                teamA = data.teamA,
                teamB = data.teamB,
                activeCombos = data.activeCombos,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            // ── Bottom Bar: Share Button ──
            BottomShareBar(
                onCaptureAndShare = { captureAndShare(view, context) },
                onCaptureAndView = { captureAndView(view, context) }
            )
        }

        BanSeatStyleBar(
            banList = data.banList,
            initialExpanded = expandBanBar,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 10.dp)
        )
    }
}

// ── Ban Bar (观战席样式，点击展开) ──────────────────────────────────────────
@Composable
private fun BanSeatStyleBar(
    banList: List<Hero?>,
    initialExpanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    val totalSlots = banList.size.coerceAtLeast(8)
    val slots = banList + List(totalSlots - banList.size) { null }
    val bannedCount = slots.count { it != null }

    Row(
        modifier = modifier.height(74.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut()
        ) {
            BanExpandedPanel(slots = slots)
        }

        BanCollapsedTab(
            bannedCount = bannedCount,
            totalSlots = totalSlots,
            expanded = expanded,
            onClick = { expanded = !expanded }
        )
    }
}

@Composable
private fun BanCollapsedTab(
    bannedCount: Int,
    totalSlots: Int,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(88.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(topStart = 8.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xDD2F86C8),
                        GradientTop
                    )
                )
            )
            .drawWithContent {
                drawContent()
                val stroke = 1.dp.toPx()
                val color = PanelBorder.copy(alpha = 0.45f)
                // 顶边
                drawLine(color, Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke)
                // 左边
                drawLine(color, Offset(stroke / 2, 0f), Offset(stroke / 2, size.height), stroke)
                // 右边
                drawLine(color, Offset(size.width - stroke / 2, 0f), Offset(size.width - stroke / 2, size.height), stroke)
            }
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (expanded) "›" else "‹",
            color = TextOnDark.copy(alpha = 0.9f),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$bannedCount/$totalSlots",
                color = TextMuted,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ban位",
                color = TextOnDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BanExpandedPanel(slots: List<Hero?>) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(topStart = 8.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xDD165884),
                        Color(0xDD0D3157)
                    )
                )
            )
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, GradientTop),
                        startY = size.height * 0.5f,
                        endY = size.height
                    )
                )
            }
            .drawWithContent {
                drawContent()
                val stroke = 1.dp.toPx()
                val color = PanelBorder.copy(alpha = 0.55f)
                // 顶边
                drawLine(color, Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke)
                // 左边
                drawLine(color, Offset(stroke / 2, 0f), Offset(stroke / 2, size.height), stroke)
                // 右边
                drawLine(color, Offset(size.width - stroke / 2, 0f), Offset(size.width - stroke / 2, size.height), stroke)
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        slots.forEachIndexed { index, hero ->
            BanSlotPreview(
                hero = hero,
                index = index
            )
        }
    }
}

@Composable
private fun BanSlotPreview(
    hero: Hero?,
    index: Int
) {
    Column(
        modifier = Modifier.width(38.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0A1F3A).copy(alpha = 0.86f))
                .border(
                    width = 1.dp,
                    color = if (hero != null) BanRed.copy(alpha = 0.7f) else PanelBorder.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hero != null) {
                HeroAvatar(
                    imageUrl = HeroRepository.getHeroAvatarUrl(hero.ename),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "?",
                    color = Color(0xFF5DA8E5),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = hero?.cname ?: "${index + 1}",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


// ── Hero Grid (参考 TeamRoomScreen 的 TeamGrid, 方格卡片布局) ──────────────────



@Composable
private fun HeroTeamGrid(
    teamA: List<TeamSlot>,
    teamB: List<TeamSlot>,
    activeCombos: List<HeroCombo> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (activeCombos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(5.dp))
        PositionLabelRow(teamA)
        Spacer(modifier = Modifier.height(2.dp))
        HeroRow(teamA, activeCombos)
        Spacer(modifier = Modifier.height(10.dp))
        Spacer(modifier = Modifier.height(2.dp))
        HeroRow(teamB, activeCombos)
    }
}

@Composable
private fun PositionLabelRow(team: List<TeamSlot>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 85.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        team.forEachIndexed { index, slot ->
            Text(
                text = slot.positionName,
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(90.dp)
            )
            if (index < team.lastIndex) {
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

@Composable
private fun HeroRow(team: List<TeamSlot>, activeCombos: List<HeroCombo> = emptyList()) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 85.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        team.forEachIndexed { index, slot ->
            HeroAvatarCard(slot = slot, activeCombos = activeCombos)
            if (index < team.lastIndex) {
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

// ── Hero Avatar Card (参考 TeamRoomScreen 的 SeatCard, 紧凑版) ───────────────

@Composable
private fun HeroAvatarCard(
    slot: TeamSlot,
    activeCombos: List<HeroCombo> = emptyList(),
    modifier: Modifier = Modifier
) {
    // 检测当前英雄所属的组合
    val comboForHero = activeCombos.firstOrNull { combo -> slot.ename in combo.heroIds }
    val displayName = comboForHero?.name ?: slot.name
    val borderColor = if (comboForHero != null) Color(AndroidColor.parseColor(comboForHero.borderColor)) else PanelBorder

    val infiniteTransition = rememberInfiniteTransition(label = "shareBorderPulse")
    val pulseAlpha = infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier.width(90.dp),  // 固定宽度，确保所有卡片对齐
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 方形头像格子
        Box(
            modifier = Modifier
                .heightIn(max = 55.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF143E67),
                            Color(0xFF173E66)
                        )
                    )
                )
                .border(
                    width = if (comboForHero != null) 2.dp else 1.dp,
                    color = if (comboForHero != null) borderColor.copy(alpha = pulseAlpha.value) else borderColor,
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (slot.avatarUrl.isNotEmpty()) {
                HeroAvatar(
                    imageUrl = slot.avatarUrl,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "?",
                    color = Color(0xFF5DA8E5),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        // 英雄名字
        Text(
            text = displayName,
            color = if (comboForHero != null) Color(AndroidColor.parseColor(comboForHero.borderColor)) else TextOnDark,
            fontSize = 12.sp,
            fontWeight = if (comboForHero != null) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

    }
}



// ── Bottom Share Bar ───────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomShareBar(
    onCaptureAndShare: () -> Unit,
    onCaptureAndView: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = GoldAccent,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .width(160.dp)
                    .height(40.dp)
                    .combinedClickable(
                        onClick = { onCaptureAndShare() },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            expanded = true
                        }
                    )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.button),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "分享",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "分享结果",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FzmeiheiFont,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("查看") },
                    onClick = {
                        expanded = false
                        onCaptureAndView()
                    }
                )
                DropdownMenuItem(
                    text = { Text("分享") },
                    onClick = {
                        expanded = false
                        onCaptureAndShare()
                    }
                )
            }
        }
    }
}

// ── Lock Screen Orientation Helper ─────────────────────────────────────────────

@Composable
private fun ImmersiveStatusBar(view: View) {
    DisposableEffect(Unit) {
        val activity = view.context as? Activity
        if (activity != null) {
            val insetsController = WindowCompat.getInsetsController(activity.window, view)
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            if (activity != null) {
                val insetsController = WindowCompat.getInsetsController(activity.window, view)
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }
}

@Composable
private fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = orientation



        onDispose {
            originalOrientation?.let {
                activity.requestedOrientation = it
            }
        }
    }
}

// ── Screenshot Capture & Share ──────────────────────────────────────────────────

private fun captureAndShare(view: View, context: Context) {
    cleanOldShareFiles(context)
    val rootView = findSuitableCaptureView(view)

    val bitmap = Bitmap.createBitmap(
        rootView.width,
        rootView.height,
        Bitmap.Config.ARGB_8888
    )
    val canvas = AndroidCanvas(bitmap)
    rootView.draw(canvas)

    val file = File(
        context.cacheDir,
        "share_result_${System.currentTimeMillis()}.png"
    )
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    bitmap.recycle()

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "分享抽取结果"))
}

private fun captureAndView(view: View, context: Context) {
    cleanOldShareFiles(context)
    val rootView = findSuitableCaptureView(view)

    val bitmap = Bitmap.createBitmap(
        rootView.width,
        rootView.height,
        Bitmap.Config.ARGB_8888
    )
    val canvas = AndroidCanvas(bitmap)
    rootView.draw(canvas)

    val file = File(
        context.cacheDir,
        "share_result_${System.currentTimeMillis()}.png"
    )
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    bitmap.recycle()

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "image/png")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(viewIntent, "查看抽取结果"))
}

private fun findSuitableCaptureView(view: View): View {
    var current = view
    while (current.parent is View) {
        val parent = current.parent as View
        if (parent.width > 0 && parent.height > 0) {
            current = parent
        } else {
            break
        }
    }
    return current
}

// ── Preview ────────────────────────────────────────────────────────────────────
@Preview(
    showBackground = true,
    widthDp = 792,
    heightDp = 360
)
@Composable
private fun PreviewShareResultScreen() {
    val mockTeamA = listOf(
        TeamSlot("对抗路", "吕布", "https://example.com/lvbu.png", 123),
        TeamSlot("打野", "李白", "https://example.com/libai.png", 131),
        TeamSlot("中路", "貂蝉", "https://example.com/diaochan.png", 141),
        TeamSlot("发育路", "孙尚香", "https://example.com/sunshangxiang.png", 111),
        TeamSlot("游走", "瑶", "https://example.com/yao.png", 505)
    )
    val mockTeamB = listOf(
        TeamSlot("对抗路", "亚瑟", "https://example.com/yase.png", 166),
        TeamSlot("打野", "韩信", "https://example.com/hanxin.png", 150),
        TeamSlot("中路", "安琪拉", "https://example.com/anqila.png", 198),
        TeamSlot("发育路", "影流之主（坦克）", "https://example.com/luban.png", 112),
        TeamSlot("游走", "蔡文姬", "https://example.com/caiwenji.png", 184)
    )

    val mockCombos = listOf(
        HeroCombo(
            name = "测试羁绊",
            heroIds = listOf(123, 131, 141),
            minRequired = 3,
            borderColor = "#FFD700"
        )
    )

    ShareResultScreen(
        data = ShareResultData(
            mode = "role",
            teamA = mockTeamA,
            teamB = mockTeamB,
            banList = emptyList(),
            activeCombos = mockCombos
        )
    )
}

