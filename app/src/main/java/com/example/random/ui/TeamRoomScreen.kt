package com.example.random.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgTop = Color(0xFF1F4D7A)
private val BgBottom = Color(0xFF2A6AA3)
private val Panel = Color(0xFF103A63)
private val PanelBorder = Color(0xFF2E86C1)
private val Accent = Color(0xFFF0C15C)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFB9D5EE)

@Preview(
    showBackground = true,
    device = Devices.AUTOMOTIVE_1024p  // 指定设备为横屏
)
@Composable
fun TeamRoomScreen(
    roomTitle: String = "5v5组队",
    serverName: String = "峡谷-征召",
    occupantName: String = "King....",
    spectatorCount: String = "0/5",
    onBackClick: (() -> Unit)? = null,
    onHomeClick: (() -> Unit)? = null,
    onStartClick: (() -> Unit)? = null,
) {
    val seats = List(10) { index ->
        if (index == 0) SeatState.Occupied(occupantName) else SeatState.Empty
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BgTop, BgBottom)
                )
            )
    ) {
        // Subtle star-like dots
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stepX = size.width / 14f
            val stepY = size.height / 10f
            for (i in 1..26) {
                val x = (i * 37 % 14) * stepX + stepX / 2
                val y = (i * 23 % 10) * stepY + stepY / 2
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = 1.8f,
                    center = Offset(x, y)
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                roomTitle = roomTitle,
                serverName = serverName,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                spectatorCount = spectatorCount
            )

            Spacer(modifier = Modifier.height(24.dp))

            TeamGrid(
                seats = seats,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            StartButton(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 24.dp, bottom = 28.dp),
                enabled = false,
                onClick = onStartClick
            )
        }
    }
}

@Composable
private fun TopBar(
    roomTitle: String,
    serverName: String,
    spectatorCount: String,
    onBackClick: (() -> Unit)?,
    onHomeClick: (() -> Unit)?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBackClick?.invoke() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = roomTitle,
                        color = TextPrimary,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "主页",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = serverName,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = spectatorCount,
                    color = TextSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "观战席",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TeamGrid(
    seats: List<SeatState>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        seats.chunked(5).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEachIndexed { index, seat ->
                    SeatCard(
                        seatState = seat,
                        modifier = Modifier
                            .weight(1f)
                            .padding(
                                start = if (index == 0) 0.dp else 10.dp,
                                end = if (index == row.lastIndex) 0.dp else 10.dp,
                                top = if (rowIndex == 0) 0.dp else 18.dp,
                                bottom = 0.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SeatCard(
    seatState: SeatState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF143E67),
                            Color(0xFF173E66)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = PanelBorder,
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when (seatState) {
                is SeatState.Occupied -> OccupiedSeatAvatar(name = seatState.name)
                SeatState.Empty -> EmptySeatPlaceholder()
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = when (seatState) {
                is SeatState.Occupied -> seatState.name
                SeatState.Empty -> ""
            },
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.heightIn(min = 18.dp)
        )
    }
}

@Composable
private fun OccupiedSeatAvatar(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "头像",
                tint = Color(0xFF2B2B2B),
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptySeatPlaceholder() {
    Text(
        text = "?",
        color = Color(0xFF5DA8E5),
        fontSize = 58.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun StartButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: (() -> Unit)?
) {
    Button(
        onClick = { if (enabled) onClick?.invoke() },
        enabled = enabled,
        modifier = modifier
            .width(150.dp)
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFB9A06A),
            disabledContainerColor = Color(0xFF7C7272),
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.75f)
        )
    ) {
        Text(text = "开始游戏", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

private sealed class SeatState {
    data class Occupied(val name: String) : SeatState()
    data object Empty : SeatState()
}
