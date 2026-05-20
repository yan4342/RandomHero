package com.example.random.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import com.example.random.model.ShareResultData

// ── 平台检测 ────────────────────────────────────────────────────────────────────

const val PACKAGE_QQ = "com.tencent.mobileqq"
const val PACKAGE_WECHAT = "com.tencent.mm"

fun isAppInstalled(context: Context, packageName: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (_: Exception) {
        false
    }
}

// ── 定向分享到指定 App ────────────────────────────────────────────────────────

fun shareToApp(context: Context, uri: Uri, packageName: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    // 显式指定目标 Activity Component，避免系统再弹选择器
    val resolveInfo = context.packageManager.resolveActivity(intent, 0)
    val matched = resolveInfo?.activityInfo
    if (matched != null && matched.packageName == packageName) {
        intent.setClassName(matched.packageName, matched.name)
    } else {
        // fallback: 查询所有可处理的 Activity，找目标包名
        val candidates = context.packageManager.queryIntentActivities(intent, 0)
        val target = candidates.firstOrNull { it.activityInfo.packageName == packageName }
        if (target != null) {
            intent.setClassName(target.activityInfo.packageName, target.activityInfo.name)
        } else {
            intent.`package` = packageName
        }
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        shareToSystem(context, uri)
    }
}

fun shareToQQ(context: Context, uri: Uri) = shareToApp(context, uri, PACKAGE_QQ)
fun shareToWeChat(context: Context, uri: Uri) = shareToApp(context, uri, PACKAGE_WECHAT)

fun shareToSystem(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享抽取结果"))
}

// ── 截图并返回 Uri（不触发分享）─────────────────────────────────────────────

fun captureToUri(view: View, context: Context): Uri? {
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

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
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

// ── Background capture of ShareResultScreen ────────────────────────────────────

internal fun cleanOldShareFiles(context: android.content.Context) {
    context.cacheDir.listFiles()?.forEach { file ->
        if (file.name.startsWith("share_result_") && file.name.endsWith(".png")) {
            file.delete()
        }
    }
}

internal suspend fun captureShareInBackground(context: android.content.Context, shareData: ShareResultData): Uri? {
    return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
        val activity = context as? Activity ?: return@withContext null
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

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
