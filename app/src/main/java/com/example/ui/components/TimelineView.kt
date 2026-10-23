package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GraphiteCard
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.TrimHandleBorder
import com.example.ui.theme.TrimYellow
import kotlin.math.roundToInt

@Composable
fun TimelineView(
    durationMs: Long,
    currentPositionMs: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    onSeekTo: (Long) -> Unit,
    onTrimRangeChange: (Long, Long) -> Unit,
    onSplitAtPlayhead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safeDuration = durationMs.coerceAtLeast(1000L)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0C0D13))
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Quick Timeline Controls & Indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimecodeBadge(currentMs = currentPositionMs, totalMs = safeDuration)

            Spacer(modifier = Modifier.weight(1f))

            // Trim start here shortcut
            GlassIconButton(
                icon = Icons.Filled.FirstPage,
                contentDescription = "Set Trim Start at Playhead",
                size = 36.dp,
                onClick = {
                    if (currentPositionMs < trimEndMs - 500) {
                        onTrimRangeChange(currentPositionMs, trimEndMs)
                    }
                }
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Split shortcut
            GlassIconButton(
                icon = Icons.Filled.CallSplit,
                contentDescription = "Split Clip at Playhead",
                size = 36.dp,
                onClick = onSplitAtPlayhead
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Trim end here shortcut
            GlassIconButton(
                icon = Icons.Filled.LastPage,
                contentDescription = "Set Trim End at Playhead",
                size = 36.dp,
                onClick = {
                    if (currentPositionMs > trimStartMs + 500) {
                        onTrimRangeChange(trimStartMs, currentPositionMs)
                    }
                }
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Reset Trim
            GlassIconButton(
                icon = Icons.Filled.RestartAlt,
                contentDescription = "Reset Trim",
                size = 36.dp,
                onClick = {
                    onTrimRangeChange(0L, safeDuration)
                }
            )
        }

        // Timeline Ruler & Clip Track
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GraphiteCard)
        ) {
            val trackWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)

            val startRatio = (trimStartMs.toFloat() / safeDuration).coerceIn(0f, 1f)
            val endRatio = (trimEndMs.toFloat() / safeDuration).coerceIn(0f, 1f)
            val playheadRatio = (currentPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)

            val startPx = startRatio * trackWidthPx
            val endPx = endRatio * trackWidthPx
            val playheadPx = playheadRatio * trackWidthPx

            // Tap to seek on entire track
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(safeDuration) {
                        detectTapGestures { offset ->
                            val seekRatio = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                            val targetMs = (seekRatio * safeDuration).toLong()
                            onSeekTo(targetMs)
                        }
                    }
            )

            // Timeline ruler ticks at 0%, 25%, 50%, 75%, 100%
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { fraction ->
                    val tickMs = (fraction * safeDuration).toLong()
                    Text(
                        text = "${tickMs / 1000}s",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Inactive Start Dimmer
            if (startPx > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width((startRatio * this.maxWidth.value).dp)
                        .background(Color(0x88000000))
                )
            }

            // Active Clip Track Body
            val activeWidthRatio = (endRatio - startRatio).coerceAtLeast(0.01f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 4.dp)
                    .offset { IntOffset(startPx.roundToInt(), 0) }
                    .width((activeWidthRatio * this.maxWidth.value).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF263353),
                                Color(0xFF1E293B),
                                Color(0xFF263353)
                            )
                        )
                    )
                    .border(1.5.dp, TrimYellow, RoundedCornerShape(6.dp))
            ) {
                // Filmstrip decoration notches
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    repeat(8) {
                        Box(
                            modifier = Modifier
                                .size(width = 6.dp, height = 4.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color(0x40FFFFFF))
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Inactive End Dimmer
            val endDimmerWidthRatio = (1f - endRatio).coerceIn(0f, 1f)
            if (endDimmerWidthRatio > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .offset { IntOffset(endPx.roundToInt(), 0) }
                        .width((endDimmerWidthRatio * this.maxWidth.value).dp)
                        .background(Color(0x88000000))
                )
            }

            // Left Trim Handle (Start)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(22.dp)
                    .offset { IntOffset((startPx - 11).coerceAtLeast(0f).roundToInt(), 0) }
                    .pointerInput(safeDuration) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaMs = ((dragAmount.x / trackWidthPx) * safeDuration).toLong()
                            val newStart = (trimStartMs + deltaMs).coerceIn(0L, trimEndMs - 500L)
                            onTrimRangeChange(newStart, trimEndMs)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                        .background(TrimYellow)
                        .border(1.dp, TrimHandleBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀",
                        fontSize = 9.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right Trim Handle (End)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(22.dp)
                    .offset { IntOffset((endPx - 11).coerceAtMost(trackWidthPx - 11).roundToInt(), 0) }
                    .pointerInput(safeDuration) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaMs = ((dragAmount.x / trackWidthPx) * safeDuration).toLong()
                            val newEnd = (trimEndMs + deltaMs).coerceIn(trimStartMs + 500L, safeDuration)
                            onTrimRangeChange(trimStartMs, newEnd)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                        .background(TrimYellow)
                        .border(1.dp, TrimHandleBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        fontSize = 9.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Playhead scrubber
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(20.dp)
                    .offset { IntOffset((playheadPx - 10).roundToInt(), 0) }
                    .pointerInput(safeDuration) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaMs = ((dragAmount.x / trackWidthPx) * safeDuration).toLong()
                            val newPos = (currentPositionMs + deltaMs).coerceIn(0L, safeDuration)
                            onSeekTo(newPos)
                        }
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    // Playhead diamond head
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(ElectricSilver)
                            .border(2.dp, ElectricBlue, CircleShape)
                    )
                    // Playhead vertical line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(ElectricSilver)
                    )
                }
            }
        }
    }
}
