package com.example.random.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.random.R
import com.example.random.ui.theme.*

// ── 分享底部弹窗（含图片预览 + QQ / 微信 / 更多）─────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    imageUri: Uri?,
    isQQInstalled: Boolean,
    isWeChatInstalled: Boolean,
    onShareToQQ: () -> Unit,
    onShareToWeChat: () -> Unit,
    onShareToSystem: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A2A3A),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── 图片预览 ──
            if (imageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D1F33)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "分享预览",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── 分享选项标题 ──
            Text(
                text = "分享到",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── 三个分享按钮 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShareOptionButton(
                    iconRes = R.drawable.ic_share_qq,
                    label = "QQ",
                    enabled = isQQInstalled,
                    onClick = {
                        if (isQQInstalled) {
                            onShareToQQ()
                            onDismiss()
                        }
                    }
                )
                ShareOptionButton(
                    iconRes = R.drawable.ic_share_wechat,
                    label = "微信",
                    enabled = isWeChatInstalled,
                    onClick = {
                        if (isWeChatInstalled) {
                            onShareToWeChat()
                            onDismiss()
                        }
                    }
                )
                ShareOptionButton(
                    iconRes = R.drawable.ic_share_more,
                    label = "更多",
                    enabled = true,
                    onClick = {
                        onShareToSystem()
                        onDismiss()
                    }
                )
            }
        }
    }
}

// ── 单个分享选项按钮 ────────────────────────────────────────────────────────

@Composable
private fun ShareOptionButton(
    iconRes: Int,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.35f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) Color.White.copy(alpha = 0.1f)
                    else Color.White.copy(alpha = 0.05f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(40.dp),
                alpha = alpha
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (enabled) label else "$label (未安装)",
            color = Color.White.copy(alpha = if (enabled) 0.85f else 0.35f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
