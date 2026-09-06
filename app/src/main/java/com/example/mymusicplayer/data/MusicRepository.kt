package com.example.mymusicplayer.data

import android.content.ContentUris
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MusicRepository(private val context: Context, private val musicDao: MusicDao) {

    private val prefs = context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE)
    
    var musicRootDirectory: String
        get() = prefs.getString("root_dir", "Music/") ?: "Music/"
        set(value) {
            prefs.edit().putString("root_dir", value).apply()
        }

    var showArtist: Boolean
        get() = prefs.getBoolean("show_artist", true)
        set(value) {
            prefs.edit().putBoolean("show_artist", value).apply()
        }

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()
    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()

    suspend fun refreshSongs() {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.RELATIVE_PATH
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("$musicRootDirectory%")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                var artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                if (artist == "<unknown>") artist = "Unknown Artist"
                
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val relativePath = cursor.getString(relativePathColumn)
                
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                ).toString()

                val albumArtUri = ContentUris.withAppendedId(
                    android.net.Uri.parse("content://media/external/audio/albumart"),
                    albumId
                ).toString()

                if (isFileExisting(contentUri)) {
                    songs.add(Song(id.toString(), title, artist, contentUri, relativePath, duration, albumArtUri))
                }
            }
        }
        musicDao.insertSongs(songs)
        if (songs.isEmpty()) {
            musicDao.deleteAllSongs()
        } else {
            musicDao.deleteSongsNotInList(songs.map { it.id })
        }
    }

    private fun isFileExisting(uriString: String): Boolean {
        return try {
            val uri = android.net.Uri.parse(uriString)
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createPlaylist(name: String) {
        musicDao.createPlaylist(Playlist(name = name))
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        musicDao.updatePlaylist(playlist)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        musicDao.deletePlaylist(playlist)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        musicDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> {
        return musicDao.getSongsInPlaylist(playlistId)
    }
    
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }
}
