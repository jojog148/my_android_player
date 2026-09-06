package com.example.mymusicplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val mediaUri: String,
    val relativePath: String,
    val duration: Long,
    val albumArtUri: String? = null
)
