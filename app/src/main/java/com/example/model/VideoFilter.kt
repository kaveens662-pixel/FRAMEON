package com.example.model

import androidx.compose.ui.graphics.Color

enum class VideoFilter(
    val filterId: String,
    val label: String,
    val description: String,
    val previewTint: Color? = null,
    val saturation: Float = 1.0f,
    val contrast: Float = 1.0f
) {
    NORMAL("normal", "Original", "True natural colors", null, 1.0f, 1.0f),
    NOIR("noir", "Noir B&W", "High-contrast cinematic black & white", Color(0x66000000), 0.0f, 1.3f),
    WARM("warm", "Golden Hour", "Rich warm amber cinematic look", Color(0x2BFF9800), 1.15f, 1.05f),
    COLD("cold", "Arctic Teal", "Cool oceanic teal tone", Color(0x2B00E5FF), 1.1f, 1.0f),
    VIVID("vivid", "Vivid Chrome", "Punchy saturated colors", Color(0x1A3B82F6), 1.4f, 1.2f),
    RETRO("retro", "Vintage Film", "Subtle faded film grain tone", Color(0x33D97706), 0.8f, 0.9f);

    companion object {
        fun fromId(id: String): VideoFilter = entries.firstOrNull { it.filterId.equals(id, ignoreCase = true) } ?: NORMAL
    }
}
