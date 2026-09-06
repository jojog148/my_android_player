package com.example.mymusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mymusicplayer.data.Playlist
import com.example.mymusicplayer.ui.MusicViewModel
import com.example.mymusicplayer.ui.screens.*
import com.example.mymusicplayer.ui.theme.MyMusicPlayerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkPermissions()

        setContent {
            MyMusicPlayerTheme {
                val navController = rememberNavController()
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Column {
                            NowPlayingBar(viewModel = viewModel)
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = {
                                        selectedTab = 0
                                        navController.navigate("songs")
                                    },
                                    icon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                                    label = { Text("Songs") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = {
                                        selectedTab = 1
                                        navController.navigate("playlists")
                                    },
                                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                                    label = { Text("Playlists") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "songs",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("songs") {
                            SongListScreen(viewModel = viewModel)
                        }
                        composable("playlists") {
                            PlaylistScreen(
                                viewModel = viewModel,
                                onPlaylistClick = { playlist ->
                                    navController.navigate("playlist/${playlist.id}/${playlist.name}")
                                }
                            )
                        }
                        composable("playlist/{id}/{name}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")?.toLong() ?: 0L
                            val name = backStackEntry.arguments?.getString("name") ?: ""
                            PlaylistDetailScreen(
                                viewModel = viewModel,
                                playlist = Playlist(id = id, name = name),
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission)
        } else {
            viewModel.refreshSongs()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.refreshSongs()
        }
    }
}
