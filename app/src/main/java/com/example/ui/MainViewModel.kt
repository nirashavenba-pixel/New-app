package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ZeroPlayerApp
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.player.RepeatMode
import com.example.player.ZeroAudioPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    WELCOME,
    HOME,
    LIBRARY,
    SEARCH,
    SETTINGS,
    EQUALIZER,
    PLAYLIST_DETAIL
}

enum class LibraryTab {
    SONGS,
    ALBUMS,
    ARTISTS,
    GENRES,
    PLAYLISTS
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as ZeroPlayerApp
    private val repository = app.repository

    private val _isDarkTheme = MutableStateFlow<Boolean?>(false)
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    fun setDarkTheme(dark: Boolean?) {
        _isDarkTheme.value = dark
    }

    val player = ZeroAudioPlayer(application) { song ->
        viewModelScope.launch {
            repository.recordPlayed(song)
        }
    }

    private val _currentScreen = MutableStateFlow(Screen.WELCOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<Screen>()

    private val _libraryTab = MutableStateFlow(LibraryTab.SONGS)
    val libraryTab: StateFlow<LibraryTab> = _libraryTab.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Dialog states
    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _playlistToRename = MutableStateFlow<Playlist?>(null)
    val playlistToRename: StateFlow<Playlist?> = _playlistToRename.asStateFlow()

    private val _songToAddToPlaylist = MutableStateFlow<Song?>(null)
    val songToAddToPlaylist: StateFlow<Song?> = _songToAddToPlaylist.asStateFlow()

    private val _playlistToDelete = MutableStateFlow<Playlist?>(null)
    val playlistToDelete: StateFlow<Playlist?> = _playlistToDelete.asStateFlow()

    // Room reactive streams
    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = repository.recentlyPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<Song>> = repository.recentlyAdded
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArtists: StateFlow<List<String>> = repository.allArtists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlbums: StateFlow<List<String>> = repository.allAlbums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGenres: StateFlow<List<String>> = repository.allGenres
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active songs for the currently viewed playlist
    val currentPlaylistSongs: StateFlow<List<Song>> = _selectedPlaylist
        .flatMapLatest { playlist ->
            if (playlist != null) repository.getSongsForPlaylist(playlist.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search results
    val searchResults: StateFlow<List<Song>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else repository.searchSongs(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Player state delegates
    val currentSong: StateFlow<Song?> = player.currentSong
    val isPlaying: StateFlow<Boolean> = player.isPlaying
    val currentPositionMs: StateFlow<Long> = player.currentPositionMs
    val durationMs: StateFlow<Long> = player.durationMs
    val queue: StateFlow<List<Song>> = player.queue
    val queueIndex: StateFlow<Int> = player.queueIndex
    val isShuffleEnabled: StateFlow<Boolean> = player.isShuffleEnabled
    val repeatMode: StateFlow<RepeatMode> = player.repeatMode
    val volume: StateFlow<Float> = player.volume
    val isEqualizerEnabled: StateFlow<Boolean> = player.isEqualizerEnabled
    val equalizerPreset: StateFlow<String> = player.equalizerPreset
    val equalizerBands: StateFlow<List<Int>> = player.equalizerBands
    val bassBoostStrength: StateFlow<Short> = player.bassBoostStrength

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialSongs()
        }
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_isNowPlayingExpanded.value) {
            _isNowPlayingExpanded.value = false
            return true
        }
        if (_screenHistory.isNotEmpty()) {
            val prev = _screenHistory.removeAt(_screenHistory.lastIndex)
            _currentScreen.value = prev
            return true
        }
        return false
    }

    fun setLibraryTab(tab: LibraryTab) {
        _libraryTab.value = tab
    }

    fun openPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        navigateTo(Screen.PLAYLIST_DETAIL)
    }

    fun setSelectedCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playSong(song: Song, customQueue: List<Song>? = null) {
        val q = customQueue ?: allSongs.value
        player.playSong(song, q)
    }

    fun togglePlayPause() = player.togglePlayPause()
    fun playNext() = player.playNext()
    fun playPrevious() = player.playPrevious()
    fun seekTo(positionMs: Long) = player.seekTo(positionMs)
    fun toggleShuffle() = player.toggleShuffle()
    fun cycleRepeatMode() = player.cycleRepeatMode()
    fun setVolume(vol: Float) = player.setVolume(vol)

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }

    fun importAudioFiles(uris: List<Uri>) {
        viewModelScope.launch {
            val imported = repository.importAudioUris(uris)
            if (imported.isNotEmpty() && currentSong.value == null) {
                playSong(imported.first(), imported)
            }
        }
    }

    // Playlist Operations
    fun openCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = true
    }

    fun closeCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = false
    }

    fun createPlaylist(name: String, description: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name.trim(), description.trim())
            _showCreatePlaylistDialog.value = false
        }
    }

    fun openRenamePlaylistDialog(playlist: Playlist) {
        _playlistToRename.value = playlist
    }

    fun closeRenamePlaylistDialog() {
        _playlistToRename.value = null
    }

    fun renamePlaylist(newName: String) {
        val target = _playlistToRename.value ?: return
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.renamePlaylist(target, newName.trim())
            if (_selectedPlaylist.value?.id == target.id) {
                _selectedPlaylist.value = target.copy(name = newName.trim())
            }
            _playlistToRename.value = null
        }
    }

    fun promptDeletePlaylist(playlist: Playlist) {
        _playlistToDelete.value = playlist
    }

    fun closeDeletePlaylistDialog() {
        _playlistToDelete.value = null
    }

    fun confirmDeletePlaylist() {
        val target = _playlistToDelete.value ?: return
        viewModelScope.launch {
            repository.deletePlaylist(target)
            if (_selectedPlaylist.value?.id == target.id) {
                _selectedPlaylist.value = null
                navigateBack()
            }
            _playlistToDelete.value = null
        }
    }

    fun openAddToPlaylistDialog(song: Song) {
        _songToAddToPlaylist.value = song
    }

    fun closeAddToPlaylistDialog() {
        _songToAddToPlaylist.value = null
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            _songToAddToPlaylist.value = null
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun reorderSongsInPlaylist(playlistId: Long, orderedSongIds: List<Long>) {
        viewModelScope.launch {
            repository.reorderSongsInPlaylist(playlistId, orderedSongIds)
        }
    }

    fun moveSongInPlaylist(playlistId: Long, fromIndex: Int, toIndex: Int) {
        val currentSongs = currentPlaylistSongs.value.toMutableList()
        if (fromIndex in currentSongs.indices && toIndex in currentSongs.indices && fromIndex != toIndex) {
            val moved = currentSongs.removeAt(fromIndex)
            currentSongs.add(toIndex, moved)
            reorderSongsInPlaylist(playlistId, currentSongs.map { it.id })
        }
    }

    // Equalizer operations
    fun setEqualizerEnabled(enabled: Boolean) = player.setEqualizerEnabled(enabled)
    fun setEqualizerPreset(presetName: String) = player.setEqualizerPreset(presetName)
    fun setEqualizerBand(bandIndex: Int, levelMilliBels: Int) = player.setBandLevel(bandIndex, levelMilliBels)
    fun setBassBoost(strength: Short) = player.setBassBoost(strength)

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
