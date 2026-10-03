package com.example.mymusicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mymusicplayer.data.Song
import com.example.mymusicplayer.ui.MusicViewModel

@Composable
fun SongListScreen(viewModel: MusicViewModel) {
    val songs by viewModel.allSongs.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()
    val savedRootDir by viewModel.currentRootDirectory.collectAsState()
    val showArtist by viewModel.showArtist.collectAsState()
    val isRandomMode by viewModel.isRandomMode.collectAsState()
    
    var songsToAdd by remember { mutableStateOf<List<Song>?>(null) }
    var rootDir by remember { mutableStateOf("") }
    
    var isDirectoryMode by remember { mutableStateOf(false) }
    var selectedDirectory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(savedRootDir) {
        rootDir = savedRootDir
    }

    if (isRandomMode) {
        val currentSong by viewModel.currentSong.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Random Player Mode",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (songs.isNotEmpty()) {
                            viewModel.playSong(songs.random(), songs)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Random Player", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(24.dp))

                if (currentSong != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Now Playing",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = currentSong!!.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )
                            if (showArtist && currentSong!!.artist.isNotBlank() && currentSong!!.artist != "Unknown Artist") {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = currentSong!!.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No music playing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    val displaySongs = if (selectedDirectory != null) {
        songs.filter { it.relativePath == selectedDirectory }
    } else {
        songs
    }

    val directories = remember(songs) {
        songs.map { it.relativePath }.distinct().sorted()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = rootDir,
                onValueChange = { rootDir = it },
                label = { Text("Root Directory") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { 
                viewModel.setMusicRootDirectory(rootDir)
                selectedDirectory = null
            }) {
                Text("Set")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.refreshSongs() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Refresh Music Library")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Directory Mode", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = isDirectoryMode,
                    onCheckedChange = { 
                        isDirectoryMode = it
                        if (!it) selectedDirectory = null
                    }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Show Artist", style = MaterialTheme.typography.bodyMedium)
                Checkbox(
                    checked = showArtist,
                    onCheckedChange = { viewModel.setShowArtist(it) }
                )
            }
        }

        if (isDirectoryMode && selectedDirectory == null) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(directories) { directory ->
                    val songsInDir = songs.filter { it.relativePath == directory }
                    ListItem(
                        headlineContent = { Text(directory) },
                        leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                        trailingContent = {
                            IconButton(onClick = { songsToAdd = songsInDir }) {
                                Icon(Icons.Default.Add, contentDescription = "Add all to playlist")
                            }
                        },
                        modifier = Modifier.clickable { selectedDirectory = directory }
                    )
                }
            }
        } else {
            if (selectedDirectory != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedDirectory = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(text = selectedDirectory!!, style = MaterialTheme.typography.titleSmall)
                }
            }
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(displaySongs) { song ->
                    val exists = remember(song.mediaUri) { viewModel.isFileExisting(song.mediaUri) }
                    SongItem(
                        song = song,
                        exists = exists,
                        showArtist = showArtist,
                        onPlayClick = { if (exists) viewModel.playSong(song) },
                        onAddToPlaylistClick = { songsToAdd = listOf(song) }
                    )
                }
            }
        }
    }

    if (songsToAdd != null) {
        AlertDialog(
            onDismissRequest = { songsToAdd = null },
            title = { Text("Add to Playlist") },
            text = {
                LazyColumn {
                    items(playlists) { playlist ->
                        Text(
                            text = playlist.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.addSongsToPlaylist(playlist, songsToAdd!!)
                                    songsToAdd = null
                                }
                                .padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { songsToAdd = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SongItem(song: Song, exists: Boolean, showArtist: Boolean, onPlayClick: () -> Unit, onAddToPlaylistClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = song.title, style = MaterialTheme.typography.titleMedium)
                if (!exists) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "File missing",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (showArtist && song.artist.isNotBlank() && song.artist != "Unknown Artist") {
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        IconButton(onClick = onPlayClick) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = if (exists) LocalContentColor.current else Color.Gray
            )
        }
        IconButton(onClick = onAddToPlaylistClick) {
            Icon(Icons.Default.Add, contentDescription = "Add to Playlist")
        }
    }
}
