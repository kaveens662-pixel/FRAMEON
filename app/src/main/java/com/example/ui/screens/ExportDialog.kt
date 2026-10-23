package com.example.ui.screens

import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ExportConfig
import com.example.model.ExportResolution
import com.example.model.ExportState
import com.example.ui.components.GlassBox
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassFillDark
import com.example.ui.theme.GraphiteCard
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SuccessEmerald

@Composable
fun ExportDialog(
    exportState: ExportState,
    onStartExport: (ExportConfig) -> Unit,
    onCancelExport: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedResolution by remember { mutableStateOf(ExportResolution.RES_1080P) }

    Dialog(onDismissRequest = {
        if (exportState !is ExportState.InProgress) {
            onDismiss()
        }
    }) {
        GlassBox(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            backgroundColor = ObsidianDark,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Movie,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export Studio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricSilver
                        )
                    }

                    if (exportState !is ExportState.InProgress) {
                        GlassIconButton(
                            icon = Icons.Filled.Close,
                            contentDescription = "Close Dialog",
                            size = 32.dp,
                            onClick = onDismiss
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (exportState) {
                    is ExportState.Idle -> {
                        Text(
                            text = "SELECT EXPORT RESOLUTION",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ExportResolution.entries.forEach { res ->
                            val isSelected = selectedResolution == res
                            val shape = RoundedCornerShape(12.dp)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(shape)
                                    .background(if (isSelected) ElectricBlue.copy(alpha = 0.2f) else GlassFillDark)
                                    .border(
                                        1.dp,
                                        if (isSelected) ElectricBlue else GlassBorderSubtle,
                                        shape
                                    )
                                    .clickable { selectedResolution = res }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = res.label,
                                            color = if (isSelected) Color.White else ElectricSilver,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${res.width} × ${res.height} MP4",
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Destination: Device Gallery > Movies/FRAMEON",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GlassButton(
                            text = "Start MP4 Export",
                            icon = Icons.Filled.IosShare,
                            onClick = {
                                onStartExport(ExportConfig(resolution = selectedResolution))
                            },
                            isPrimary = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is ExportState.InProgress -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                progress = { exportState.progress },
                                color = ElectricBlue,
                                trackColor = Color(0x33FFFFFF),
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "${(exportState.progress * 100).toInt()}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricSilver
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = exportState.statusMessage,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            GlassButton(
                                text = "Cancel Export",
                                onClick = onCancelExport,
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    is ExportState.Completed -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = SuccessEmerald,
                                modifier = Modifier.size(56.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Video Saved Successfully!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricSilver
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${exportState.outputDisplayName} • ${Formatter.formatFileSize(context, exportState.fileSizeBytes)}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Saved in Movies/FRAMEON",
                                fontSize = 11.sp,
                                color = ElectricBlue
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            GlassButton(
                                text = "Done",
                                onClick = onDismiss,
                                isPrimary = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    is ExportState.Failed -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(56.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Export Failed",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricSilver
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = exportState.errorMessage,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            GlassButton(
                                text = "Try Again",
                                onClick = {
                                    onStartExport(ExportConfig(resolution = selectedResolution))
                                },
                                isPrimary = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
