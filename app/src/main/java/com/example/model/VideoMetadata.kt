package com.example.model

data class VideoMetadata(
    val uriString: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int = 0,
    val mimeType: String = "video/mp4",
    val title: String = "Video Clip"
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val millis = (durationMs % 1000) / 100
            return String.format("%02d:%02d.%d", minutes, seconds, millis)
        }
}
