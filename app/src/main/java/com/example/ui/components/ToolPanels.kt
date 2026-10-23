package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TextOverlay
import com.example.model.VideoFilter
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassFillDark
import com.example.ui.theme.GlassFillLight
import com.example.ui.theme.GraphiteCard
import com.example.ui.theme.TrimYellow

enum class EditorTool(val label: String, val icon: ImageVector) {
    TRIM("Trim", Icons.Filled.ContentCut),
    AUDIO("Audio", Icons.Filled.MusicNote),
    SPEED("Speed", Icons.Filled.Speed),
    FILTERS("Filters", Icons.Filled.ColorLens),
    TEXT("Text", Icons.Filled.TextFields)
}

@Composable
fun EditorToolbar(
    selectedTool: EditorTool,
    onSelectTool: (EditorTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        EditorTool.entries.forEach { tool ->
            val isSelected = selectedTool == tool
            val shape = RoundedCornerShape(12.dp)

            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(if (isSelected) ElectricBlue.copy(alpha = 0.25f) else GlassFillDark)
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isSelected) ElectricBlue else GlassBorderSubtle
                        ),
                        shape
                    )
                    .clickable { onSelectTool(tool) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.label,
                        tint = if (isSelected) ElectricBlue else ElectricSilver,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tool.label,
                        color = if (isSelected) ElectricSilver else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun TrimPrecisionPanel(
    trimStartMs: Long,
    trimEndMs: Long,
    durationMs: Long,
    onTrimChange: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            text = "PRECISE TRIM ADJUSTMENT",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trim Start Controls
            Column {
                Text(
                    text = "Start: ${formatMsToTimecode(trimStartMs)}",
                    color = ElectricSilver,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    GlassIconButton(
                        icon = Icons.Filled.Remove,
                        contentDescription = "Step Start -100ms",
                        size = 36.dp,
                        onClick = {
                            val newStart = (trimStartMs - 100L).coerceAtLeast(0L)
                            onTrimChange(newStart, trimEndMs)
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    GlassIconButton(
                        icon = Icons.Filled.Add,
                        contentDescription = "Step Start +100ms",
                        size = 36.dp,
                        onClick = {
                            val newStart = (trimStartMs + 100L).coerceAtMost(trimEndMs - 300L)
                            onTrimChange(newStart, trimEndMs)
                        }
                    )
                }
            }

            // Trim End Controls
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "End: ${formatMsToTimecode(trimEndMs)}",
                    color = ElectricSilver,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    GlassIconButton(
                        icon = Icons.Filled.Remove,
                        contentDescription = "Step End -100ms",
                        size = 36.dp,
                        onClick = {
                            val newEnd = (trimEndMs - 100L).coerceAtLeast(trimStartMs + 300L)
                            onTrimChange(trimStartMs, newEnd)
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    GlassIconButton(
                        icon = Icons.Filled.Add,
                        contentDescription = "Step End +100ms",
                        size = 36.dp,
                        onClick = {
                            val newEnd = (trimEndMs + 100L).coerceAtMost(durationMs)
                            onTrimChange(trimStartMs, newEnd)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AudioPanel(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onPickAudioTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AUDIO VOLUME: ${(volume * 100).toInt()}%",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            GlassButton(
                text = "Add Audio Track",
                icon = Icons.Filled.MusicNote,
                onClick = onPickAudioTrack,
                isPrimary = false
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (volume <= 0.01f) Icons.Filled.VolumeMute else Icons.Filled.VolumeDown,
                contentDescription = null,
                tint = ElectricSilver,
                modifier = Modifier.size(20.dp)
            )

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..2f,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                colors = SliderDefaults.colors(
                    thumbColor = ElectricBlue,
                    activeTrackColor = ElectricBlue,
                    inactiveTrackColor = Color(0x33FFFFFF)
                )
            )

            Icon(
                imageVector = Icons.Filled.VolumeUp,
                contentDescription = null,
                tint = ElectricSilver,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SpeedPanel(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            text = "PLAYBACK & EXPORT SPEED",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            speeds.forEach { speed ->
                val isSelected = (currentSpeed - speed).let { if (it < 0) -it else it } < 0.05f
                val shape = RoundedCornerShape(10.dp)

                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(if (isSelected) ElectricBlue else GlassFillDark)
                        .border(1.dp, if (isSelected) Color.White else GlassBorderSubtle, shape)
                        .clickable { onSpeedChange(speed) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${speed}x",
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else ElectricSilver
                    )
                }
            }
        }
    }
}

@Composable
fun FiltersPanel(
    selectedFilter: VideoFilter,
    onSelectFilter: (VideoFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            text = "CINEMATIC COLOR GRADING",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VideoFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                val shape = RoundedCornerShape(12.dp)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(shape)
                        .background(if (isSelected) ElectricBlue.copy(alpha = 0.25f) else GlassFillDark)
                        .border(1.dp, if (isSelected) ElectricBlue else GlassBorderSubtle, shape)
                        .clickable { onSelectFilter(filter) }
                        .padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(filter.previewTint ?: Color(0xFF4B5563))
                            .border(1.dp, Color(0x40FFFFFF), CircleShape)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = filter.label,
                        color = if (isSelected) ElectricSilver else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun TextOverlayPanel(
    overlay: TextOverlay?,
    onUpdateOverlay: (TextOverlay) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var inputText by remember(overlay?.text) { mutableStateOf(overlay?.text ?: "FRAMEON") }
    val currentOverlay = overlay ?: TextOverlay()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TEXT OVERLAY (SINHALA & ENGLISH UNICODE)",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (currentOverlay.isVisible) "Enabled" else "Hidden",
                    color = ElectricSilver,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = currentOverlay.isVisible,
                    onCheckedChange = { onUpdateOverlay(currentOverlay.copy(isVisible = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ElectricBlue
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = {
                inputText = it
                onUpdateOverlay(currentOverlay.copy(text = it))
            },
            placeholder = { Text("Enter caption in English or සිංහල", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ElectricSilver,
                unfocusedTextColor = ElectricSilver,
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = GlassBorderSubtle,
                focusedContainerColor = GraphiteCard,
                unfocusedContainerColor = GraphiteCard
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Text Color Palette (White, Yellow, Cyan, Coral, Green)
        val colors = listOf("#FFFFFF", "#FBBF24", "#38BDF8", "#F87171", "#34D399")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Color:", color = Color.Gray, fontSize = 12.sp)
            colors.forEach { hex ->
                val parsedColor = Color(android.graphics.Color.parseColor(hex))
                val isSelected = currentOverlay.colorHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(parsedColor)
                        .border(
                            2.dp,
                            if (isSelected) ElectricBlue else Color(0x33FFFFFF),
                            CircleShape
                        )
                        .clickable { onUpdateOverlay(currentOverlay.copy(colorHex = hex)) }
                )
            }
        }
    }
}
