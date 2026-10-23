package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.AppDatabase
import com.example.data.ProjectEntity
import com.example.data.ProjectRepository
import com.example.media.MediaMetadataHelper
import com.example.media.VideoExportManager
import com.example.model.EditorCommand
import com.example.model.ExportConfig
import com.example.model.ExportState
import com.example.model.TextOverlay
import com.example.model.VideoFilter
import com.example.model.VideoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(AppDatabase.getInstance(application).projectDao())
    val exportManager = VideoExportManager(application)
    val exportState: StateFlow<ExportState> = exportManager.exportState

    var exoPlayer: ExoPlayer? = null
        private set

    private val _videoMetadata = MutableStateFlow<VideoMetadata?>(null)
    val videoMetadata: StateFlow<VideoMetadata?> = _videoMetadata.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _trimStartMs = MutableStateFlow(0L)
    val trimStartMs: StateFlow<Long> = _trimStartMs.asStateFlow()

    private val _trimEndMs = MutableStateFlow(0L)
    val trimEndMs: StateFlow<Long> = _trimEndMs.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _speed = MutableStateFlow(1.0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    private val _activeFilter = MutableStateFlow(VideoFilter.NORMAL)
    val activeFilter: StateFlow<VideoFilter> = _activeFilter.asStateFlow()

    private val _textOverlay = MutableStateFlow(TextOverlay())
    val textOverlay: StateFlow<TextOverlay> = _textOverlay.asStateFlow()

    private val _projectTitle = MutableStateFlow("Untitled Project")
    val projectTitle: StateFlow<String> = _projectTitle.asStateFlow()

    private var currentProjectId: Long = 0L

    // Undo / Redo Stacks
    private val undoStack = mutableListOf<EditorCommand>()
    private val redoStack = mutableListOf<EditorCommand>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var positionTrackerJob: Job? = null

    init {
        initExoPlayer()
    }

    private fun initExoPlayer() {
        val player = ExoPlayer.Builder(getApplication()).build()
        player.repeatMode = Player.REPEAT_MODE_OFF

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    seekTo(_trimStartMs.value)
                }
            }
        })
        exoPlayer = player

        // Position tracker coroutine
        positionTrackerJob = viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                exoPlayer?.let { p ->
                    val pos = p.currentPosition
                    _currentPositionMs.value = pos

                    // If reached trim end while playing, loop back to trim start for accurate preview
                    if (pos >= _trimEndMs.value && _trimEndMs.value > 0L) {
                        p.seekTo(_trimStartMs.value)
                    }
                }
                delay(50)
            }
        }
    }

    fun loadVideo(uri: Uri, title: String? = null, existingProjectId: Long = 0L) {
        currentProjectId = existingProjectId
        viewModelScope.launch {
            val result = MediaMetadataHelper.extractMetadata(getApplication(), uri)
            result.onSuccess { meta ->
                _videoMetadata.value = meta
                _projectTitle.value = title ?: meta.title
                _trimStartMs.value = 0L
                _trimEndMs.value = meta.durationMs

                exoPlayer?.let { player ->
                    player.setMediaItem(MediaItem.fromUri(uri))
                    player.prepare()
                    player.seekTo(0L)
                    player.playWhenReady = true
                }

                // If new project, save to database
                if (currentProjectId == 0L) {
                    saveCurrentProject()
                }
            }
        }
    }

    fun loadExistingProject(project: ProjectEntity) {
        currentProjectId = project.id
        _projectTitle.value = project.title
        _trimStartMs.value = project.trimStartMs
        _trimEndMs.value = project.trimEndMs
        _volume.value = project.volume
        _speed.value = project.speed
        _activeFilter.value = VideoFilter.fromId(project.filterName)
        _textOverlay.value = TextOverlay(text = project.textOverlay, colorHex = project.textColorHex)

        val uri = Uri.parse(project.videoUri)
        viewModelScope.launch {
            val result = MediaMetadataHelper.extractMetadata(getApplication(), uri)
            result.onSuccess { meta ->
                _videoMetadata.value = meta
                exoPlayer?.let { player ->
                    player.setMediaItem(MediaItem.fromUri(uri))
                    player.prepare()
                    player.setPlaybackSpeed(project.speed)
                    player.volume = project.volume
                    player.seekTo(project.trimStartMs)
                    player.playWhenReady = false
                }
            }
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                if (player.currentPosition >= _trimEndMs.value) {
                    player.seekTo(_trimStartMs.value)
                }
                player.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceIn(0L, _videoMetadata.value?.durationMs ?: 0L)
        exoPlayer?.seekTo(bounded)
        _currentPositionMs.value = bounded
    }

    fun setTrimRange(newStartMs: Long, newEndMs: Long) {
        val oldStart = _trimStartMs.value
        val oldEnd = _trimEndMs.value
        if (oldStart == newStartMs && oldEnd == newEndMs) return

        pushCommand(EditorCommand.TrimChange(oldStart, oldEnd, newStartMs, newEndMs))
        _trimStartMs.value = newStartMs
        _trimEndMs.value = newEndMs

        // Adjust playhead if out of range
        val currentPos = _currentPositionMs.value
        if (currentPos < newStartMs || currentPos > newEndMs) {
            seekTo(newStartMs)
        }
        saveCurrentProject()
    }

    fun splitAtPlayhead() {
        val current = _currentPositionMs.value
        val start = _trimStartMs.value
        val end = _trimEndMs.value

        if (current in (start + 500L)..(end - 500L)) {
            // Split creates the first segment by trimming to playhead
            setTrimRange(start, current)
        }
    }

    fun setFilter(filter: VideoFilter) {
        val old = _activeFilter.value
        if (old == filter) return

        pushCommand(EditorCommand.FilterChange(old, filter))
        _activeFilter.value = filter
        saveCurrentProject()
    }

    fun setSpeed(speed: Float) {
        val old = _speed.value
        if (old == speed) return

        pushCommand(EditorCommand.SpeedChange(old, speed))
        _speed.value = speed
        exoPlayer?.setPlaybackSpeed(speed)
        saveCurrentProject()
    }

    fun setVolume(volume: Float) {
        val old = _volume.value
        if (old == volume) return

        pushCommand(EditorCommand.VolumeChange(old, volume))
        _volume.value = volume
        exoPlayer?.volume = volume
        saveCurrentProject()
    }

    fun updateTextOverlay(overlay: TextOverlay) {
        val old = _textOverlay.value
        if (old.text != overlay.text) {
            pushCommand(EditorCommand.TextChange(old.text, overlay.text))
        }
        _textOverlay.value = overlay
        saveCurrentProject()
    }

    fun setProjectTitle(newTitle: String) {
        _projectTitle.value = newTitle
        saveCurrentProject()
    }

    private fun pushCommand(command: EditorCommand) {
        undoStack.add(command)
        redoStack.clear()
        updateUndoRedoState()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val command = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(command)

        when (command) {
            is EditorCommand.TrimChange -> {
                _trimStartMs.value = command.oldStartMs
                _trimEndMs.value = command.oldEndMs
                seekTo(command.oldStartMs)
            }
            is EditorCommand.FilterChange -> _activeFilter.value = command.oldFilter
            is EditorCommand.SpeedChange -> {
                _speed.value = command.oldSpeed
                exoPlayer?.setPlaybackSpeed(command.oldSpeed)
            }
            is EditorCommand.VolumeChange -> {
                _volume.value = command.oldVolume
                exoPlayer?.volume = command.oldVolume
            }
            is EditorCommand.TextChange -> {
                _textOverlay.value = _textOverlay.value.copy(text = command.oldText)
            }
        }
        updateUndoRedoState()
        saveCurrentProject()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val command = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(command)

        when (command) {
            is EditorCommand.TrimChange -> {
                _trimStartMs.value = command.newStartMs
                _trimEndMs.value = command.newEndMs
                seekTo(command.newStartMs)
            }
            is EditorCommand.FilterChange -> _activeFilter.value = command.newFilter
            is EditorCommand.SpeedChange -> {
                _speed.value = command.newSpeed
                exoPlayer?.setPlaybackSpeed(command.newSpeed)
            }
            is EditorCommand.VolumeChange -> {
                _volume.value = command.newVolume
                exoPlayer?.volume = command.newVolume
            }
            is EditorCommand.TextChange -> {
                _textOverlay.value = _textOverlay.value.copy(text = command.newText)
            }
        }
        updateUndoRedoState()
        saveCurrentProject()
    }

    private fun updateUndoRedoState() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun saveCurrentProject() {
        val meta = _videoMetadata.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val entity = ProjectEntity(
                id = currentProjectId,
                title = _projectTitle.value,
                videoUri = meta.uriString,
                durationMs = meta.durationMs,
                trimStartMs = _trimStartMs.value,
                trimEndMs = _trimEndMs.value,
                volume = _volume.value,
                speed = _speed.value,
                filterName = _activeFilter.value.filterId,
                textOverlay = _textOverlay.value.text,
                textColorHex = _textOverlay.value.colorHex,
                lastModified = System.currentTimeMillis()
            )
            val savedId = repository.saveProject(entity)
            currentProjectId = savedId
        }
    }

    fun startExport(config: ExportConfig) {
        val meta = _videoMetadata.value ?: return
        exoPlayer?.pause()
        exportManager.startExport(
            scope = viewModelScope,
            inputUri = Uri.parse(meta.uriString),
            trimStartMs = _trimStartMs.value,
            trimEndMs = _trimEndMs.value,
            filter = _activeFilter.value,
            volume = _volume.value,
            speed = _speed.value,
            exportConfig = config
        )
    }

    fun cancelExport() {
        exportManager.cancelExport()
    }

    fun resetExportState() {
        exportManager.resetState()
    }

    override fun onCleared() {
        super.onCleared()
        positionTrackerJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
        exportManager.cancelExport()
    }
}
