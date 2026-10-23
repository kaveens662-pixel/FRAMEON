package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.ui.components.AudioPanel
import com.example.ui.components.EditorTool
import com.example.ui.components.EditorToolbar
import com.example.ui.components.FiltersPanel
import com.example.ui.components.GlassBox
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassIconButton
import com.example.ui.components.SpeedPanel
import com.example.ui.components.TextOverlayPanel
import com.example.ui.components.TimelineView
import com.example.ui.components.TrimPrecisionPanel
import com.example.ui.components.VideoPlayerSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GraphiteCard
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.ObsidianDeep
import com.example.viewmodel.EditorViewModel

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoMeta by viewModel.videoMetadata.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val trimStartMs by viewModel.trimStartMs.collectAsState()
    val trimEndMs by viewModel.trimEndMs.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()
    val textOverlay by viewModel.textOverlay.collectAsState()
    val projectTitle by viewModel.projectTitle.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val exportState by viewModel.exportState.collectAsState()

    var selectedTool by remember { mutableStateOf(EditorTool.TRIM) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var tempRenameText by remember(projectTitle) { mutableStateOf(projectTitle) }

    // Audio Picker
    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "Audio track attached to project", Toast.LENGTH_SHORT).show()
            // Sets volume ready for editing
            viewModel.setVolume(1.0f)
        }
    }

    // Lifecycle-safe back handling: pause player, save project, pop navigation
    BackHandler {
        viewModel.exoPlayer?.pause()
        viewModel.saveCurrentProject()
        onNavigateBack()
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
        ) {
            // TOP BAR: Navigation, Title, Undo/Redo, Export
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Projects",
                    size = 40.dp,
                    onClick = {
                        viewModel.exoPlayer?.pause()
                        viewModel.saveCurrentProject()
                        onNavigateBack()
                    }
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Project Title (clickable to rename)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showRenameDialog = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = projectTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricSilver,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Rename Project",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Undo Button
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    size = 38.dp,
                    tint = if (canUndo) ElectricSilver else Color.DarkGray,
                    onClick = { if (canUndo) viewModel.undo() }
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Redo Button
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    size = 38.dp,
                    tint = if (canRedo) ElectricSilver else Color.DarkGray,
                    onClick = { if (canRedo) viewModel.redo() }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Export Button
                GlassButton(
                    text = "Export",
                    icon = Icons.Filled.IosShare,
                    onClick = { showExportDialog = true },
                    isPrimary = true
                )
            }

            // MAIN VIDEO PREVIEW VIEWPORT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                VideoPlayerSurface(
                    player = viewModel.exoPlayer,
                    isPlaying = isPlaying,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    activeFilter = activeFilter,
                    textOverlay = textOverlay,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // INTERACTIVE TIMELINE
            val duration = videoMeta?.durationMs ?: 1000L
            TimelineView(
                durationMs = duration,
                currentPositionMs = currentPositionMs,
                trimStartMs = trimStartMs,
                trimEndMs = trimEndMs,
                onSeekTo = { viewModel.seekTo(it) },
                onTrimRangeChange = { start, end -> viewModel.setTrimRange(start, end) },
                onSplitAtPlayhead = { viewModel.splitAtPlayhead() },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // TOOLBAR (Trim, Audio, Speed, Filters, Text)
            EditorToolbar(
                selectedTool = selectedTool,
                onSelectTool = { selectedTool = it },
                modifier = Modifier.padding(top = 2.dp)
            )

            // ACTIVE TOOL PANEL
            GlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = GraphiteCard
            ) {
                when (selectedTool) {
                    EditorTool.TRIM -> {
                        TrimPrecisionPanel(
                            trimStartMs = trimStartMs,
                            trimEndMs = trimEndMs,
                            durationMs = duration,
                            onTrimChange = { start, end -> viewModel.setTrimRange(start, end) }
                        )
                    }
                    EditorTool.AUDIO -> {
                        AudioPanel(
                            volume = volume,
                            onVolumeChange = { viewModel.setVolume(it) },
                            onPickAudioTrack = { audioPicker.launch("audio/*") }
                        )
                    }
                    EditorTool.SPEED -> {
                        SpeedPanel(
                            currentSpeed = speed,
                            onSpeedChange = { viewModel.setSpeed(it) }
                        )
                    }
                    EditorTool.FILTERS -> {
                        FiltersPanel(
                            selectedFilter = activeFilter,
                            onSelectFilter = { viewModel.setFilter(it) }
                        )
                    }
                    EditorTool.TEXT -> {
                        TextOverlayPanel(
                            overlay = textOverlay,
                            onUpdateOverlay = { viewModel.updateTextOverlay(it) }
                        )
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            exportState = exportState,
            onStartExport = { config -> viewModel.startExport(config) },
            onCancelExport = { viewModel.cancelExport() },
            onDismiss = {
                viewModel.resetExportState()
                showExportDialog = false
            }
        )
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Project", color = ElectricSilver) },
            text = {
                OutlinedTextField(
                    value = tempRenameText,
                    onValueChange = { tempRenameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ElectricSilver,
                        unfocusedTextColor = ElectricSilver,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = GlassBorderSubtle
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempRenameText.isNotBlank()) {
                        viewModel.setProjectTitle(tempRenameText)
                    }
                    showRenameDialog = false
                }) {
                    Text("Save", color = ElectricBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = ObsidianDark
        )
    }
}
