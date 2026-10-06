package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uriString: String,
    val mimeType: String = "audio/mpeg",
    val formatDisplay: String = "MP3 320 kbps",
    val artworkUri: String? = null,
    val genre: String = "Anime OST",
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val fileSizeBytes: Long = 0L
)
