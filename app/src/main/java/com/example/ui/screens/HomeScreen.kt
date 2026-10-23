package com.example.ui.screens

import android.net.Uri
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.ProjectEntity
import com.example.data.ProjectRepository
import com.example.ui.components.GlassBox
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.formatMsToTimecode
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricSilver
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GraphiteCard
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.ObsidianDeep
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun HomeScreen(
    onOpenNewVideo: (Uri) -> Unit,
    onOpenExistingProject: (ProjectEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { ProjectRepository(AppDatabase.getInstance(context).projectDao()) }
    val projects by repository.allProjects.collectAsState(initial = emptyList())

    var showAboutDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    // Modern Android Photo Picker (zero broad permissions needed)
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onOpenNewVideo(uri)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDeep),
        containerColor = ObsidianDeep
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Header: Brand & About
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(ElectricBlue, Color(0xFF1D4ED8))
                                )
                            )
                            .border(1.dp, GlassBorderHighlight, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Movie,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "FRAMEON",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricSilver,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Created by Kaveen Sandeepa",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                GlassIconButton(
                    icon = Icons.Filled.Info,
                    contentDescription = "About FRAMEON",
                    size = 42.dp,
                    onClick = { showAboutDialog = true }
                )
            }

            // Hero New Project Banner
            GlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0x1F2563EB)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start Video Project",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricSilver
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Import HD/4K videos with accurate timeline trimming & MP4 export.",
                                fontSize = 12.sp,
                                color = Color(0xFF93C5FD)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        GlassButton(
                            text = "Select Video",
                            icon = Icons.Filled.Add,
                            onClick = {
                                videoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            isPrimary = true
                        )
                    }
                }
            }

            // Projects Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT PROJECTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${projects.size} Projects",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Projects List or Empty State
            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(GraphiteCard)
                                .border(1.dp, GlassBorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VideoLibrary,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Projects Yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricSilver
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Tap 'Select Video' above to pick any video from your gallery and craft your first cut.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onOpenExistingProject(project) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail / Icon
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(GraphiteSurface)
                                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = project.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ElectricSilver,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${formatMsToTimecode(project.durationMs)} • ${DateFormat.format("MMM d, yyyy", Date(project.lastModified))}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    if (project.filterName != "Normal") {
                                        Text(
                                            text = "Filter: ${project.filterName}",
                                            fontSize = 10.sp,
                                            color = ElectricBlue
                                        )
                                    }
                                }

                                GlassIconButton(
                                    icon = Icons.Filled.Delete,
                                    contentDescription = "Delete project",
                                    size = 36.dp,
                                    tint = Color(0xFFEF4444),
                                    onClick = { projectToDelete = project }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    // Delete Confirmation Dialog
    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project?", color = ElectricSilver) },
            text = { Text("Are you sure you want to delete '${project.title}'? This cannot be undone.", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        repository.deleteProject(project.id)
                    }
                    projectToDelete = null
                }) {
                    Text("Delete", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = ObsidianDark
        )
    }
}
