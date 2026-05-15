package com.example.random.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    // Background capture of ShareResultScreen (no visible transition)
    LaunchedEffect(autoShare) {
        if (autoShare) {
            kotlinx.coroutines.delay(100)
            captureShareInBackground(context, viewModel.getShareData())
            viewModel.closeShareScreen()
        }
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
            .background(if (appColors.isDark) Color.Black else appColors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(5.dp))

            // Top bar: Mode Selection + Settings icon
            Box(modifier = Modifier.fillMaxWidth()) {
                ModeSelector(viewModel)
                // Settings icon in top-right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
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
                        containerColor = appColors.gold,
                        contentColor = appColors.darkText
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
                            tint = appColors.textMain
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
