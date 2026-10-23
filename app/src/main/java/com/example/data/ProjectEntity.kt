package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val videoUri: String,
    val durationMs: Long,
    val trimStartMs: Long = 0,
    val trimEndMs: Long = durationMs,
    val volume: Float = 1.0f,
    val speed: Float = 1.0f,
    val filterName: String = "Normal",
    val textOverlay: String = "",
    val textColorHex: String = "#FFFFFF",
    val lastModified: Long = System.currentTimeMillis()
)
