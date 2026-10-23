package com.example.model

sealed interface ExportState {
    data object Idle : ExportState
    data class InProgress(val progress: Float, val statusMessage: String = "Encoding video...") : ExportState
    data class Completed(
        val outputUri: String,
        val outputDisplayName: String,
        val fileSizeBytes: Long
    ) : ExportState
    data class Failed(val errorMessage: String) : ExportState
}
