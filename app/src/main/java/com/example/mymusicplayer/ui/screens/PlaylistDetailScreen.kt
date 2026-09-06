package com.example.mymusicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mymusicplayer.data.Playlist
import com.example.mymusicplayer.data.Song
import com.example.mymusicplayer.ui.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(viewModel: MusicViewModel, playlist: Playlist, onBack: () -> Unit) {
    val songs by viewModel.getSongsInPlaylist(playlist.id).collectAsState(emptyList())
    val showArtist by viewModel.showArtist.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(songs) { song ->
                var menuExpanded by remember { mutableStateOf(false) }
                val exists = remember(song.mediaUri) { viewModel.isFileExisting(song.mediaUri) }

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
                        if (!exists) {
                            Text(
                                text = "File not found on device",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    IconButton(onClick = { if (exists) viewModel.playSong(song, songs) }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = if (exists) LocalContentColor.current else Color.Gray
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Remove from Playlist") },
                                onClick = {
                                    viewModel.removeSongFromPlaylist(playlist, song)
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
