package com.example.random.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

import androidx.compose.runtime.Immutable

@Immutable
data class HeroAvatarState(val imageUrl: String)

@Composable
fun HeroAvatar(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val request = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .allowHardware(false)
            // 保持 allowHardware(false)：分享截图使用软件 Canvas，硬件 Bitmap 会导致崩溃
            .build()
    }

    AsyncImage(
        model = request,
        contentDescription = null,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp)),
        contentScale = ContentScale.Crop
    )
}

// Utility modifier to remove ripple/shadow effect on click
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}