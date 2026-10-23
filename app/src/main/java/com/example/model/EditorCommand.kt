package com.example.model

sealed interface EditorCommand {
    data class TrimChange(val oldStartMs: Long, val oldEndMs: Long, val newStartMs: Long, val newEndMs: Long) : EditorCommand
    data class FilterChange(val oldFilter: VideoFilter, val newFilter: VideoFilter) : EditorCommand
    data class SpeedChange(val oldSpeed: Float, val newSpeed: Float) : EditorCommand
    data class VolumeChange(val oldVolume: Float, val newVolume: Float) : EditorCommand
    data class TextChange(val oldText: String, val newText: String) : EditorCommand
}
