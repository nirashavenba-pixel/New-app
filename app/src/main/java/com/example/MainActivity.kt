package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.LibraryTab
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.BottomNavBar
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.DeletePlaylistConfirmDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.components.RenamePlaylistDialog
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.ZeroPlayerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val useDark = isDarkTheme ?: androidx.compose.foundation.isSystemInDarkTheme()
            ZeroPlayerTheme(darkTheme = useDark) {
                ZeroPlayerAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ZeroPlayerAppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val libraryTab by viewModel.libraryTab.collectAsStateWithLifecycle()
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val allPlaylists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val allArtists by viewModel.allArtists.collectAsStateWithLifecycle()
    val allAlbums by viewModel.allAlbums.collectAsStateWithLifecycle()
    val allGenres by viewModel.allGenres.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val currentPlaylistSongs by viewModel.currentPlaylistSongs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isEqualizerEnabled by viewModel.isEqualizerEnabled.collectAsStateWithLifecycle()
    val equalizerPreset by viewModel.equalizerPreset.collectAsStateWithLifecycle()
    val equalizerBands by viewModel.equalizerBands.collectAsStateWithLifecycle()
    val bassBoostStrength by viewModel.bassBoostStrength.collectAsStateWithLifecycle()

    // Dialog states
    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsStateWithLifecycle()
    val playlistToRename by viewModel.playlistToRename.collectAsStateWithLifecycle()
    val songToAddToPlaylist by viewModel.songToAddToPlaylist.collectAsStateWithLifecycle()
    val playlistToDelete by viewModel.playlistToDelete.collectAsStateWithLifecycle()

    BackHandler(enabled = currentScreen != Screen.WELCOME && !isNowPlayingExpanded) {
        if (!viewModel.navigateBack()) {
            // Let system handle default back
        }
    }

    if (currentScreen == Screen.WELCOME) {
        WelcomeScreen(
            onGetStarted = { viewModel.navigateTo(Screen.HOME) }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Mini player floating above bottom bar
                    if (!isNowPlayingExpanded && currentSong != null) {
                        MiniPlayer(
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            progressMs = currentPositionMs,
                            durationMs = durationMs,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onNextClick = { viewModel.playNext() },
                            onExpandClick = { viewModel.setNowPlayingExpanded(true) }
                        )
                    }

                    // Bottom navigation bar
                    BottomNavBar(
                        currentScreen = currentScreen,
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onCenterPlayClick = {
                            if (currentSong != null) {
                                viewModel.setNowPlayingExpanded(true)
                            } else if (allSongs.isNotEmpty()) {
                                viewModel.playSong(allSongs.first())
                                viewModel.setNowPlayingExpanded(true)
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                when (currentScreen) {
                    Screen.HOME -> HomeScreen(
                        allSongs = allSongs,
                        recentlyPlayed = recentlyPlayed,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song -> viewModel.playSong(song) },
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onSelectLibraryTab = { tab -> viewModel.setLibraryTab(tab) },
                        onImportUris = { uris -> viewModel.importAudioFiles(uris) }
                    )

                    Screen.LIBRARY -> LibraryScreen(
                        currentTab = libraryTab,
                        onTabSelected = { tab -> viewModel.setLibraryTab(tab) },
                        allSongs = allSongs,
                        allPlaylists = allPlaylists,
                        allArtists = allArtists,
                        allAlbums = allAlbums,
                        allGenres = allGenres,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song -> viewModel.playSong(song) },
                        onShufflePlayClick = {
                            if (allSongs.isNotEmpty()) {
                                if (!isShuffleEnabled) viewModel.toggleShuffle()
                                viewModel.playSong(allSongs.random())
                            }
                        },
                        onOpenPlaylist = { playlist -> viewModel.openPlaylist(playlist) },
                        onCreatePlaylistClick = { viewModel.openCreatePlaylistDialog() },
                        onFavoriteToggle = { song -> viewModel.toggleFavorite(song) },
                        onAddToPlaylistClick = { song -> viewModel.openAddToPlaylistDialog(song) },
                        onPlayNextClick = { song -> viewModel.playSong(song) },
                        onImportUris = { uris -> viewModel.importAudioFiles(uris) },
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )

                    Screen.PLAYLIST_DETAIL -> PlaylistDetailScreen(
                        playlist = selectedPlaylist,
                        songs = currentPlaylistSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song -> viewModel.playSong(song, currentPlaylistSongs) },
                        onPlayAll = {
                            if (currentPlaylistSongs.isNotEmpty()) {
                                viewModel.playSong(currentPlaylistSongs.first(), currentPlaylistSongs)
                            }
                        },
                        onShuffleAll = {
                            if (currentPlaylistSongs.isNotEmpty()) {
                                if (!isShuffleEnabled) viewModel.toggleShuffle()
                                viewModel.playSong(currentPlaylistSongs.random(), currentPlaylistSongs)
                            }
                        },
                        onRenameClick = { selectedPlaylist?.let { viewModel.openRenamePlaylistDialog(it) } },
                        onDeleteClick = { selectedPlaylist?.let { viewModel.promptDeletePlaylist(it) } },
                        onAddSongsClick = {
                            if (allSongs.isNotEmpty()) {
                                viewModel.openAddToPlaylistDialog(allSongs.first())
                            }
                        },
                        onRemoveSong = { songId ->
                            selectedPlaylist?.let { viewModel.removeSongFromPlaylist(it.id, songId) }
                        },
                        onMoveSong = { fromIndex, toIndex ->
                            selectedPlaylist?.let { viewModel.moveSongInPlaylist(it.id, fromIndex, toIndex) }
                        },
                        onBack = { viewModel.navigateBack() }
                    )

                    Screen.SEARCH -> SearchScreen(
                        query = searchQuery,
                        onQueryChange = { q -> viewModel.setSearchQuery(q) },
                        searchResults = searchResults,
                        allSongs = allSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song -> viewModel.playSong(song) },
                        onFavoriteToggle = { song -> viewModel.toggleFavorite(song) },
                        onAddToPlaylistClick = { song -> viewModel.openAddToPlaylistDialog(song) },
                        onPlayNextClick = { song -> viewModel.playSong(song) }
                    )

                    Screen.SETTINGS -> SettingsScreen(
                        totalSongs = allSongs.size,
                        totalPlaylists = allPlaylists.size,
                        isDarkTheme = viewModel.isDarkTheme.collectAsStateWithLifecycle().value,
                        onToggleTheme = { dark -> viewModel.setDarkTheme(dark) },
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onImportUris = { uris -> viewModel.importAudioFiles(uris) }
                    )

                    Screen.EQUALIZER -> EqualizerScreen(
                        isEnabled = isEqualizerEnabled,
                        currentPreset = equalizerPreset,
                        bands = equalizerBands,
                        bassBoostStrength = bassBoostStrength,
                        onToggleEnabled = { enabled -> viewModel.setEqualizerEnabled(enabled) },
                        onSelectPreset = { preset -> viewModel.setEqualizerPreset(preset) },
                        onBandChange = { index, level -> viewModel.setEqualizerBand(index, level) },
                        onBassBoostChange = { strength -> viewModel.setBassBoost(strength) },
                        onBack = { viewModel.navigateBack() }
                    )

                    Screen.WELCOME -> {}
                }
            }
        }

        // Full-screen Now Playing Overlay
        AnimatedVisibility(
            visible = isNowPlayingExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingScreen(
                song = currentSong,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                volume = volume,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onNextClick = { viewModel.playNext() },
                onPreviousClick = { viewModel.playPrevious() },
                onSeek = { pos -> viewModel.seekTo(pos) },
                onShuffleToggle = { viewModel.toggleShuffle() },
                onRepeatToggle = { viewModel.cycleRepeatMode() },
                onVolumeChange = { vol -> viewModel.setVolume(vol) },
                onFavoriteToggle = { currentSong?.let { viewModel.toggleFavorite(it) } },
                onCollapse = { viewModel.setNowPlayingExpanded(false) },
                onOpenEqualizer = {
                    viewModel.setNowPlayingExpanded(false)
                    viewModel.navigateTo(Screen.EQUALIZER)
                },
                onAddToPlaylistClick = {
                    currentSong?.let { viewModel.openAddToPlaylistDialog(it) }
                }
            )
        }

        // Dialogs
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { viewModel.closeCreatePlaylistDialog() },
                onConfirm = { name, desc -> viewModel.createPlaylist(name, desc) }
            )
        }

        playlistToRename?.let { target ->
            RenamePlaylistDialog(
                playlist = target,
                onDismiss = { viewModel.closeRenamePlaylistDialog() },
                onConfirm = { newName -> viewModel.renamePlaylist(newName) }
            )
        }

        playlistToDelete?.let { target ->
            DeletePlaylistConfirmDialog(
                playlist = target,
                onDismiss = { viewModel.closeDeletePlaylistDialog() },
                onConfirm = { viewModel.confirmDeletePlaylist() }
            )
        }

        songToAddToPlaylist?.let { targetSong ->
            AddToPlaylistDialog(
                song = targetSong,
                playlists = allPlaylists,
                onDismiss = { viewModel.closeAddToPlaylistDialog() },
                onSelectPlaylist = { pId -> viewModel.addSongToPlaylist(pId, targetSong.id) },
                onCreateNewPlaylistClick = { viewModel.openCreatePlaylistDialog() }
            )
        }
    }
}
