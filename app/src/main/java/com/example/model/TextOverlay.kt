package com.example.model

data class TextOverlay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "FRAMEON",
    val xPercent: Float = 50f, // 0 to 100%
    val yPercent: Float = 75f,
    val fontSizeSp: Float = 24f,
    val colorHex: String = "#FFFFFF",
    val isVisible: Boolean = true
)
