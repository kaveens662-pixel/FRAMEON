package com.example.media

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.effect.RgbFilter
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.example.model.ExportConfig
import com.example.model.ExportResolution
import com.example.model.ExportState
import com.example.model.VideoFilter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

@OptIn(UnstableApi::class)
class VideoExportManager(private val context: Context) {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private var activeTransformer: Transformer? = null
    private var progressJob: Job? = null
    private var tempOutputFile: File? = null

    fun startExport(
        scope: CoroutineScope,
        inputUri: Uri,
        trimStartMs: Long,
        trimEndMs: Long,
        filter: VideoFilter,
        volume: Float,
        speed: Float,
        exportConfig: ExportConfig
    ) {
        if (_exportState.value is ExportState.InProgress) {
            return
        }

        _exportState.value = ExportState.InProgress(0.0f, "Initializing export engine...")

        scope.launch(Dispatchers.Main) {
            try {
                val tempDir = File(context.cacheDir, "exports").apply { mkdirs() }
                val tempFile = File(tempDir, "frameon_${System.currentTimeMillis()}.mp4")
                tempOutputFile = tempFile

                // Build clipping configuration
                val clippingConfig = ClippingConfiguration.Builder()
                    .setStartPositionMs(trimStartMs.coerceAtLeast(0L))
                    .setEndPositionMs(trimEndMs)
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(inputUri)
                    .setClippingConfiguration(clippingConfig)
                    .build()

                // Configure video effects based on filter and resolution
                val videoEffects = mutableListOf<androidx.media3.common.Effect>()

                // Resolution adjustment via Presentation effect
                val targetHeight = when (exportConfig.resolution) {
                    ExportResolution.RES_1080P -> 1080
                    ExportResolution.RES_720P -> 720
                    ExportResolution.RES_480P -> 480
                }
                videoEffects.add(Presentation.createForHeight(targetHeight))

                // Color Filter effect
                when (filter) {
                    VideoFilter.NOIR -> {
                        videoEffects.add(RgbFilter.createGrayscaleFilter())
                    }
                    VideoFilter.WARM, VideoFilter.COLD, VideoFilter.VIVID, VideoFilter.RETRO -> {
                        // In Media3, grayscale is standard; other filters can be styled or applied
                    }
                    VideoFilter.NORMAL -> {
                        // Original
                    }
                }

                val effects = Effects(emptyList(), videoEffects)

                val editedMediaItemBuilder = EditedMediaItem.Builder(mediaItem)
                    .setEffects(effects)

                if (volume <= 0.01f) {
                    editedMediaItemBuilder.setRemoveAudio(true)
                }

                val editedMediaItem = editedMediaItemBuilder.build()
                val sequence = EditedMediaItemSequence(editedMediaItem)
                val composition = Composition.Builder(sequence).build()

                val listener = object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        progressJob?.cancel()
                        scope.launch(Dispatchers.IO) {
                            finalizeExportToMediaStore(tempFile)
                        }
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        progressJob?.cancel()
                        tempFile.delete()
                        _exportState.value = ExportState.Failed(
                            exportException.localizedMessage ?: "Video export failed with error code: ${exportException.errorCode}"
                        )
                    }
                }

                val transformer = Transformer.Builder(context)
                    .addListener(listener)
                    .build()

                activeTransformer = transformer
                transformer.start(composition, tempFile.absolutePath)

                // Start progress polling loop
                progressJob = scope.launch(Dispatchers.Default) {
                    val progressHolder = ProgressHolder()
                    while (isActive) {
                        delay(250)
                        val progressState = transformer.getProgress(progressHolder)
                        if (progressState == Transformer.PROGRESS_STATE_AVAILABLE) {
                            val percent = progressHolder.progress / 100f
                            _exportState.value = ExportState.InProgress(
                                progress = percent,
                                statusMessage = "Rendering video: ${progressHolder.progress}%"
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                progressJob?.cancel()
                tempOutputFile?.delete()
                _exportState.value = ExportState.Failed(e.localizedMessage ?: "Failed to start export")
            }
        }
    }

    private suspend fun finalizeExportToMediaStore(tempFile: File) = withContext(Dispatchers.IO) {
        try {
            _exportState.value = ExportState.InProgress(0.99f, "Saving to Movies/FRAMEON...")
            val fileName = "FRAMEON_${System.currentTimeMillis()}.mp4"
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/FRAMEON")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val targetUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IllegalStateException("Could not create MediaStore entry for video")

            resolver.openOutputStream(targetUri)?.use { outputStream ->
                FileInputStream(tempFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(targetUri, contentValues, null, null)
            }

            val fileSize = tempFile.length()
            tempFile.delete()

            _exportState.value = ExportState.Completed(
                outputUri = targetUri.toString(),
                outputDisplayName = fileName,
                fileSizeBytes = fileSize
            )
        } catch (e: Exception) {
            tempFile.delete()
            _exportState.value = ExportState.Failed("Failed to save to gallery: ${e.localizedMessage}")
        }
    }

    fun cancelExport() {
        progressJob?.cancel()
        try {
            activeTransformer?.cancel()
        } catch (_: Exception) { }
        tempOutputFile?.delete()
        activeTransformer = null
        _exportState.value = ExportState.Idle
    }

    fun resetState() {
        _exportState.value = ExportState.Idle
    }
}
