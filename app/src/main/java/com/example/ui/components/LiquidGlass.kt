package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricSilver
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassFillDark
import com.example.ui.theme.GlassFillLight
import com.example.ui.theme.GlassFillMedium
import com.example.ui.theme.GraphiteCard
import com.example.ui.theme.ObsidianDeep

@Composable
fun GlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = GlassFillMedium,
    borderColor: Color = GlassBorderSubtle,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = backgroundColor.alpha * 1.2f),
                        backgroundColor.copy(alpha = backgroundColor.alpha * 0.8f)
                    )
                )
            )
            .border(
                border = BorderStroke(
                    width = borderWidth,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlassBorderHighlight.copy(alpha = 0.35f),
                            borderColor.copy(alpha = 0.15f)
                        )
                    )
                ),
                shape = shape
            ),
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = ElectricBlue.copy(alpha = 0.3f)),
            role = Role.Button,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GraphiteCard.copy(alpha = 0.85f),
                        ObsidianDeep.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x40FFFFFF),
                            Color(0x10FFFFFF)
                        )
                    )
                ),
                shape = shape
            )
            .then(clickableModifier),
        content = content
    )
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = false,
    enabled: Boolean = true
) {
    val bgBrush = if (isPrimary) {
        Brush.horizontalGradient(listOf(ElectricBlue, Color(0xFF2563EB)))
    } else {
        Brush.verticalGradient(listOf(GlassFillLight, GlassFillDark))
    }

    val borderStroke = if (isPrimary) {
        BorderStroke(1.dp, Color(0x60FFFFFF))
    } else {
        BorderStroke(1.dp, GlassBorderSubtle)
    }

    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .background(if (enabled) bgBrush else Brush.linearGradient(listOf(Color(0x1AFFFFFF), Color(0x1AFFFFFF))))
            .border(borderStroke, shape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White),
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) ElectricSilver else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) ElectricSilver else Color.Gray,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = ElectricSilver,
    isActive: Boolean = false,
    size: Dp = 48.dp
) {
    val shape = CircleShape
    val backgroundBrush = if (isActive) {
        Brush.radialGradient(listOf(ElectricBlue.copy(alpha = 0.5f), GlassFillDark))
    } else {
        Brush.verticalGradient(listOf(GlassFillLight, GlassFillDark))
    }

    val borderStroke = if (isActive) {
        BorderStroke(1.5.dp, ElectricBlue)
    } else {
        BorderStroke(1.dp, GlassBorderSubtle)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(backgroundBrush)
            .border(borderStroke, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = ElectricBlue),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) ElectricBlue else tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun TimecodeBadge(
    currentMs: Long,
    totalMs: Long,
    modifier: Modifier = Modifier
) {
    val currentFormatted = formatMsToTimecode(currentMs)
    val totalFormatted = formatMsToTimecode(totalMs)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x990A0A0F))
            .border(BorderStroke(0.8.dp, GlassBorderSubtle), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$currentFormatted / $totalFormatted",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ElectricSilver
        )
    }
}

fun formatMsToTimecode(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val fraction = ((ms % 1000) / 100).coerceAtLeast(0)
    return String.format("%02d:%02d.%d", minutes, seconds, fraction)
}
