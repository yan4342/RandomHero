package com.example.random.ui

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.random.R
import com.example.random.ui.components.*
import com.example.random.ui.theme.*
import com.example.random.viewmodel.RandomHeroViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RandomHeroApp() {
    val viewModel: RandomHeroViewModel = viewModel()
    val appColors = LocalAppColors.current
    val activeCombos by viewModel.activeCombos.collectAsState()
    val showShareScreen by viewModel.showShareScreen.collectAsState()
    val expandBanBar by viewModel.expandBanBar.collectAsState()
    val autoShare by viewModel.autoShare.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // 分享底部弹窗状态（主页快捷分享）
    var showMainShareSheet by remember { mutableStateOf(false) }
    var capturedShareUri by remember { mutableStateOf<Uri?>(null) }

    // Background capture of ShareResultScreen → 显示分享底部弹窗
    LaunchedEffect(autoShare) {
        if (autoShare) {
            kotlinx.coroutines.delay(100)
            val uri = captureShareInBackground(context, viewModel.getShareData())
            capturedShareUri = uri
            viewModel.closeShareScreen()
            showMainShareSheet = true
        }
    }

    // 分享底部弹窗（主页快捷分享）
    if (showMainShareSheet) {
        ShareBottomSheet(
            imageUri = capturedShareUri,
            isQQInstalled = isAppInstalled(context, PACKAGE_QQ),
            isWeChatInstalled = isAppInstalled(context, PACKAGE_WECHAT),
            onShareToQQ = { capturedShareUri?.let { shareToQQ(context, it) } },
            onShareToWeChat = { capturedShareUri?.let { shareToWeChat(context, it) } },
            onShareToSystem = { capturedShareUri?.let { shareToSystem(context, it) } },
            onDismiss = { showMainShareSheet = false }
        )
    }

    // Navigate to settings screen
    if (showSettings) {
        SettingsScreen(
            onBackClick = { viewModel.closeSettings() }
        )
        return
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
            .background(appColors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Top bar: Mode Selection + Settings icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeSelector(viewModel, modifier = Modifier.weight(1f))
                // Settings icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .noRippleClickable { viewModel.openSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "设置",
                        modifier = Modifier.size(22.dp),
                        tint = appColors.textSub
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ban Section
            BanSection(viewModel, activeCombos)

            Spacer(modifier = Modifier.height(12.dp))

            // Teams side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeamColumn(
                    title = "队伍上",
                    team = viewModel.teamA.collectAsState().value,
                    titleColor = appColors.teamA,
                    onReRoll = { index -> viewModel.reRollOne("A", index) },
                    activeCombos = activeCombos,
                    modifier = Modifier.weight(1f)
                )

                TeamColumn(
                    title = "队伍下",
                    team = viewModel.teamB.collectAsState().value,
                    titleColor = appColors.teamB,
                    onReRoll = { index -> viewModel.reRollOne("B", index) },
                    activeCombos = activeCombos,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Randomize Button with button.png pattern
                Button(
                    onClick = { viewModel.randomize() },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.gold,
                        contentColor = appColors.darkText
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
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
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Text(
                            "重新抽取",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp),
                            fontFamily = FzmeiheiFont
                        )
                    }
                }

                // Share Icon - click to share, long press for menu
                var showShareMenu by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        shape = CircleShape,
                        color = appColors.surfaceInput,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .combinedClickable(
                                onClick = { viewModel.openShareScreen(expandBan = true, autoShare = true) },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showShareMenu = true
                                }
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "分享",
                                modifier = Modifier.size(22.dp),
                                tint = appColors.textMain
                            )
                        }
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

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom padding for navigation bar
            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
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
