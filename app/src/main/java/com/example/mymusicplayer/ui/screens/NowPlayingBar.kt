package com.example.mymusicplayer.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mymusicplayer.ui.MusicViewModel

@Composable
fun NowPlayingBar(viewModel: MusicViewModel) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val showArtist by viewModel.showArtist.collectAsState()

    if (currentSong != null) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong!!.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                    if (showArtist && currentSong!!.artist.isNotBlank() && currentSong!!.artist != "Unknown Artist") {
                        Text(
                            text = currentSong!!.artist,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }
                IconButton(onClick = { viewModel.skipToPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.togglePlayPause() }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }
                    MusicPlayingAnimation(isPlaying = isPlaying)
                }
                IconButton(onClick = { viewModel.skipToNext() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next")
                }
            }
        }
    }
}

@Composable
fun MusicPlayingAnimation(isPlaying: Boolean) {
    if (!isPlaying) return

    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 4.dp.value,
        targetValue = 16.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 8.dp.value,
        targetValue = 20.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 6.dp.value,
        targetValue = 14.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = Modifier
            .padding(start = 2.dp, end = 4.dp)
            .height(20.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar1Height.dp)
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar2Height.dp)
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar3Height.dp)
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
        )
    }
}
