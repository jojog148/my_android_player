package com.example.mymusicplayer.ui

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.mymusicplayer.data.MusicDatabase
import com.example.mymusicplayer.data.MusicRepository
import com.example.mymusicplayer.data.Playlist
import com.example.mymusicplayer.data.Song
import com.example.mymusicplayer.playback.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MusicRepository
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    val allSongs: StateFlow<List<Song>>
    val allPlaylists: StateFlow<List<Playlist>>

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentRootDirectory = MutableStateFlow("")
    val currentRootDirectory: StateFlow<String> = _currentRootDirectory.asStateFlow()

    private val _showArtist = MutableStateFlow(true)
    val showArtist: StateFlow<Boolean> = _showArtist.asStateFlow()

    init {
        val database = MusicDatabase.getDatabase(application)
        repository = MusicRepository(application, database.musicDao())
        
        _currentRootDirectory.value = repository.musicRootDirectory
        _showArtist.value = repository.showArtist
        
        allSongs = repository.allSongs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allPlaylists = repository.allPlaylists.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        setupMediaController()
    }

    private fun setupMediaController() {
        val sessionToken = SessionToken(getApplication(), ComponentName(getApplication(), PlaybackService::class.java))
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val songId = mediaItem?.mediaId
                    _currentSong.value = allSongs.value.find { it.id == songId }
                }
            })
        }, MoreExecutors.directExecutor())
    }

    fun playSong(song: Song, playlist: List<Song> = allSongs.value) {
        val mediaItems = playlist.map { s ->
            val metadata = androidx.media3.common.MediaMetadata.Builder()
                .setTitle(s.title)
                .setArtist(s.artist)
                .setDisplayTitle(s.title)
                .setArtworkUri(s.albumArtUri?.let { android.net.Uri.parse(it) })
                .build()
            MediaItem.Builder()
                .setMediaId(s.id)
                .setUri(s.mediaUri)
                .setMediaMetadata(metadata)
                .build()
        }
        
        val startIndex = playlist.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        
        mediaController?.setMediaItems(mediaItems, startIndex, 0L)
        mediaController?.prepare()
        mediaController?.play()
    }

    fun togglePlayPause() {
        if (mediaController?.isPlaying == true) {
            mediaController?.pause()
        } else {
            mediaController?.play()
        }
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun refreshSongs() {
        viewModelScope.launch {
            repository.refreshSongs()
        }
    }

    fun setMusicRootDirectory(path: String) {
        // Clean leading/trailing slashes for consistency
        var cleanPath = path.trim()
        if (cleanPath.startsWith("/")) cleanPath = cleanPath.substring(1)
        if (cleanPath.isNotEmpty() && !cleanPath.endsWith("/")) cleanPath = "$cleanPath/"
        
        repository.musicRootDirectory = cleanPath
        _currentRootDirectory.value = cleanPath
        refreshSongs()
    }

    fun setShowArtist(show: Boolean) {
        repository.showArtist = show
        _showArtist.value = show
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.updatePlaylist(playlist)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    fun addSongToPlaylist(playlist: Playlist, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlist.id, song.id)
        }
    }

    fun addSongsToPlaylist(playlist: Playlist, songs: List<Song>) {
        viewModelScope.launch {
            songs.forEach { song ->
                repository.addSongToPlaylist(playlist.id, song.id)
            }
        }
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> {
        return repository.getSongsInPlaylist(playlistId)
    }

    fun removeSongFromPlaylist(playlist: Playlist, song: Song) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlist.id, song.id)
        }
    }

    fun isFileExisting(uriString: String): Boolean {
        return try {
            val uri = android.net.Uri.parse(uriString)
            getApplication<Application>().contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}
