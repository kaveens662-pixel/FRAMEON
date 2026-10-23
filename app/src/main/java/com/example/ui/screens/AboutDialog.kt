package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GlassBox
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GraphiteCard
import com.example.ui.theme.ObsidianDark

@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
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
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Movie,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FRAMEON",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricSilver
                            )
                            Text(
                                text = "v1.0.0 • Native Android Editor",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    GlassIconButton(
                        icon = Icons.Filled.Close,
                        contentDescription = "Close",
                        size = 32.dp,
                        onClick = onDismiss
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Developer Credit Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(GraphiteCard)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Developer Credit",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Created by Kaveen Sandeepa",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricSilver
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Architecture Specifications
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(GraphiteCard)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "CORE ARCHITECTURE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("• Media3 ExoPlayer Timeline Preview", fontSize = 12.sp, color = ElectricSilver)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Media3 Transformer Hardware MP4 Export", fontSize = 12.sp, color = ElectricSilver)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Zero-Permission Android Photo Picker", fontSize = 12.sp, color = ElectricSilver)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Scoped Storage MediaStore Output", fontSize = 12.sp, color = ElectricSilver)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• iOS Liquid Glass Compose Design System", fontSize = 12.sp, color = ElectricSilver)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Sinhala (සිංහල) & English Unicode Text Engine", fontSize = 12.sp, color = ElectricSilver)
                }

                Spacer(modifier = Modifier.height(20.dp))

                GlassButton(
                    text = "Close",
                    onClick = onDismiss,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
