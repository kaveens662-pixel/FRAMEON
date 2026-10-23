package com.example.model

enum class ExportResolution(
    val label: String,
    val width: Int,
    val height: Int,
    val estimatedBitrateBps: Int
) {
    RES_1080P("1080p Full HD", 1920, 1080, 8_000_000),
    RES_720P("720p HD", 1280, 720, 4_500_000),
    RES_480P("480p SD (Fast)", 854, 480, 2_000_000);
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.RES_1080P,
    val frameRate: Int = 30,
    val outputFormat: String = "video/mp4",
    val audioVolume: Float = 1.0f,
    val speed: Float = 1.0f
)
