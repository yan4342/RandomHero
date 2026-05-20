package com.example.random.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.random.data.GitHubUpdateChecker
import com.example.random.data.UpdateCheckResult
import com.example.random.ui.theme.LocalAppColors

@Composable
internal fun GitHubUpdateCard(
    currentVersion: String,
    isChecking: Boolean,
    onOpenRepository: () -> Unit,
    onCheckUpdate: () -> Unit
) {
    val appColors = LocalAppColors.current

    Card(
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        shape = appColors.cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (appColors.isDark) 0.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "GitHub 与更新",
                style = MaterialTheme.typography.titleMedium,
                color = appColors.textMain
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "yan4342/RandomHero",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = appColors.textMain
                )
                Text(
                    "当前版本 $currentVersion",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSub
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenRepository,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textMain)
                ) {
                    Text("打开仓库", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = onCheckUpdate,
                    enabled = !isChecking,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.gold,
                        contentColor = appColors.darkText,
                        disabledContainerColor = appColors.surfaceInput,
                        disabledContentColor = appColors.textSub
                    )
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = appColors.textSub
                        )
                    } else {
                        Text("检查更新", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
internal fun UpdateResultDialog(
    result: UpdateCheckResult,
    onDismiss: () -> Unit,
    onOpenRelease: (String) -> Unit
) {
    val appColors = LocalAppColors.current

    val title: String
    val message: String
    val releaseUrl: String?

    when (result) {
        is UpdateCheckResult.UpdateAvailable -> {
            title = "发现新版本"
            message = buildString {
                append("当前版本：${result.currentVersion}\n")
                append("最新版本：${result.release.tagName}")
                if (result.release.name != result.release.tagName) {
                    append("\n${result.release.name}")
                }
            }
            releaseUrl = result.release.htmlUrl
        }
        is UpdateCheckResult.UpToDate -> {
            title = "已是最新版本"
            message = "当前版本：${result.currentVersion}\n最新版本：${result.release.tagName}"
            releaseUrl = null
        }
        UpdateCheckResult.NoRelease -> {
            title = "未找到发布版本"
            message = "GitHub 仓库暂时没有可用于检查更新的 Release。"
            releaseUrl = GitHubUpdateChecker.REPOSITORY_URL
        }
        is UpdateCheckResult.NetworkError -> {
            title = "检查更新失败"
            message = result.message
            releaseUrl = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = appColors.textMain) },
        text = { Text(message, color = appColors.textSub) },
        confirmButton = {
            if (releaseUrl != null) {
                TextButton(onClick = { onOpenRelease(releaseUrl) }) {
                    Text("打开页面", color = appColors.gold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("确定", color = appColors.gold)
                }
            }
        },
        dismissButton = {
            if (releaseUrl != null) {
                TextButton(onClick = onDismiss) {
                    Text("取消", color = appColors.textSub)
                }
            }
        },
        containerColor = appColors.card
    )
}
