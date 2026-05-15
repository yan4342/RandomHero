package com.example.random.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import com.example.random.model.ShareResultData

// ── Background capture of ShareResultScreen ────────────────────────────────────

internal fun cleanOldShareFiles(context: android.content.Context) {
    context.cacheDir.listFiles()?.forEach { file ->
        if (file.name.startsWith("share_result_") && file.name.endsWith(".png")) {
            file.delete()
        }
    }
}

internal suspend fun captureShareInBackground(context: android.content.Context, shareData: ShareResultData) {
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
