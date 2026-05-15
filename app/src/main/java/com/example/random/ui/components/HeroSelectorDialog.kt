package com.example.random.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.random.data.HeroRepository
import com.example.random.data.ImagePreloader
import com.example.random.model.Hero
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.derivedStateOf

// 分页配置
private const val PAGE_SIZE = 35 // 每页加载数量
private const val LOAD_MORE_THRESHOLD = 6 // 距离底部多少项时触发加载

@Composable
fun HeroSelectorDialog(
    heroes: List<Hero>,
    onDismiss: () -> Unit,
    onHeroSelected: (Hero) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<Int?>(null) } // null means 'All'
    
    val filteredHeroes = remember(searchText, selectedRole, heroes) {
        var result = heroes
        if (selectedRole != null) {
            result = result.filter { it.heroType == selectedRole || it.heroType2 == selectedRole }
        }
        if (searchText.isNotEmpty()) {
            result = result.filter { hero ->
                hero.cname.contains(searchText, ignoreCase = true) || hero.title.contains(searchText, ignoreCase = true)
            }
        }
        result
    }
    
    // 分页状态
    var displayedCount by remember { mutableIntStateOf(PAGE_SIZE) }
    var isLoadingMore by remember { mutableStateOf(false) }
    
    // 当筛选条件变化时重置分页
    LaunchedEffect(searchText, selectedRole) {
        displayedCount = PAGE_SIZE
    }
    
    // 当前显示的英雄列表（分页截取）
    val displayedHeroes = remember(filteredHeroes, displayedCount) {
        filteredHeroes.take(displayedCount)
    }
    
    // 是否还有更多数据
    val hasMore = remember(filteredHeroes, displayedCount) {
        displayedCount < filteredHeroes.size
    }
    
    val isDark = isSystemInDarkTheme()
    val dialogBg = remember(isDark) { if (isDark) Color(0xFF1E1E1E) else Color.White }
    val textColor = remember(isDark) { if (isDark) Color(0xFFDDDDDD) else Color(0xFF333333) }
    val divColor = remember(isDark) { if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE) }
    val inputBg = remember(isDark) { if (isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5) }
    val avatarBg = remember(isDark) { if (isDark) Color(0xFF2C2C2C) else Color(0xFFEEEEEE) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // 在对话框显示时预加载图片
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // 预加载所有英雄的头像
            ImagePreloader.preloadAllHeroAvatars(context)
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // 禁用默认的平台宽度限制，自己控制更精确
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f) // 使用固定比例而不是随内容撑开
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(10.dp),
            color = dialogBg
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp)
                ) {
                    Text(
                        "选择禁用英雄",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color(0xFF999999)
                        )
                    }
                }
                
                HorizontalDivider(color = divColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))
                
                // Search Input
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("搜索英雄名称", fontSize = 14.sp, color = Color(0xFF999999)) },
                    textStyle = TextStyle(fontSize = 14.sp, color = textColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputBg,
                        unfocusedContainerColor = inputBg,
                        disabledContainerColor = inputBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = textColor
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Role Filter Bar
                val roles = remember {
                    listOf(null to "全部") + Hero.ROLES.toList()
                }
                
                // Cache colors to avoid recreation
                val selectedBgColor = remember(isDark) { Color(0xFFBCA676) }
                val selectedTextColor = remember(isDark) { Color(0xFF30210D) }
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 5.dp)
                ) {
                    items(roles) { (roleId, roleName) ->
                        RoleFilterItem(
                            roleId = roleId,
                            roleName = roleName,
                            isSelected = selectedRole == roleId,
                            onSelected = { selectedRole = roleId },
                            selectedBgColor = selectedBgColor,
                            selectedTextColor = selectedTextColor,
                            unselectedBgColor = inputBg,
                            unselectedTextColor = textColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Hero Grid with pagination
                val gridState = rememberLazyGridState()
                
                // 检测是否需要加载更多
                val shouldLoadMore by remember {
                    derivedStateOf {
                        val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisibleItem >= (displayedCount - LOAD_MORE_THRESHOLD) && hasMore && !isLoadingMore
                    }
                }
                
                // 触发加载更多
                LaunchedEffect(shouldLoadMore) {
                    if (shouldLoadMore) {
                        isLoadingMore = true
                        displayedCount = (displayedCount + PAGE_SIZE).coerceAtMost(filteredHeroes.size)
                        isLoadingMore = false
                    }
                }
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5), // 使用固定列数减少计算
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(5.dp),
                    state = gridState
                ) {
                    items(
                        items = displayedHeroes,
                        key = { it.ename },
                        contentType = { "hero" } // 提示 Compose 这里的项类型相同，优化重用
                    ) { hero ->
                        HeroGridItem(
                            hero = hero,
                            onClick = { onHeroSelected(hero) },
                            avatarBg = avatarBg
                        )
                    }
                    
                    // 底部加载指示器
                    if (isLoadingMore) {
                        item(
                            span = { GridItemSpan(maxLineSpan) },
                            contentType = "loading"
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = selectedBgColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroGridItem(
    hero: Hero,
    onClick: () -> Unit,
    avatarBg: Color
) {
    val imageUrl = remember(hero.ename) {
        HeroRepository.getHeroAvatarUrl(hero.ename)
    }
    val updatedOnClick by rememberUpdatedState(newValue = onClick)
    
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(avatarBg)
            .noRippleClickable { updatedOnClick() }
    ) {
        HeroAvatar(
            imageUrl = imageUrl,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun RoleFilterItem(
    roleId: Int?,
    roleName: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    selectedBgColor: Color,
    selectedTextColor: Color,
    unselectedBgColor: Color,
    unselectedTextColor: Color
) {
    val bgColor = if (isSelected) selectedBgColor else unselectedBgColor
    val txtColor = if (isSelected) selectedTextColor else unselectedTextColor
    val updatedOnSelected by rememberUpdatedState(newValue = onSelected)
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .background(bgColor)
            .noRippleClickable(onClick = updatedOnSelected)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = roleName,
            fontSize = 13.sp,
            color = txtColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}