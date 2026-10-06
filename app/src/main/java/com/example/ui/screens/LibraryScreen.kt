package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.ui.LibraryTab
import com.example.ui.Screen
import com.example.ui.components.AnimeArtwork
import com.example.ui.components.SongListItem

@Composable
fun LibraryScreen(
    currentTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    allSongs: List<Song>,
    allPlaylists: List<Playlist>,
    allArtists: List<String>,
    allAlbums: List<String>,
    allGenres: List<String>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onShufflePlayClick: () -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onPlayNextClick: (Song) -> Unit,
    onImportUris: (List<Uri>) -> Unit,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onImportUris(uris)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Library",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            filePickerLauncher.launch(arrayOf("audio/*"))
                        },
                        modifier = Modifier.testTag("library_import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Import Audio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(Screen.SETTINGS) },
                        modifier = Modifier.testTag("library_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Tabs Pills Row
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    TabPill(
                        label = "Songs (${allSongs.size})",
                        selected = currentTab == LibraryTab.SONGS,
                        onClick = { onTabSelected(LibraryTab.SONGS) },
                        testTag = "tab_songs"
                    )
                }
                item {
                    TabPill(
                        label = "Playlists (${allPlaylists.size})",
                        selected = currentTab == LibraryTab.PLAYLISTS,
                        onClick = { onTabSelected(LibraryTab.PLAYLISTS) },
                        testTag = "tab_playlists"
                    )
                }
                item {
                    TabPill(
                        label = "Albums (${allAlbums.size})",
                        selected = currentTab == LibraryTab.ALBUMS,
                        onClick = { onTabSelected(LibraryTab.ALBUMS) },
                        testTag = "tab_albums"
                    )
                }
                item {
                    TabPill(
                        label = "Artists (${allArtists.size})",
                        selected = currentTab == LibraryTab.ARTISTS,
                        onClick = { onTabSelected(LibraryTab.ARTISTS) },
                        testTag = "tab_artists"
                    )
                }
                item {
                    TabPill(
                        label = "Genres (${allGenres.size})",
                        selected = currentTab == LibraryTab.GENRES,
                        onClick = { onTabSelected(LibraryTab.GENRES) },
                        testTag = "tab_genres"
                    )
                }
            }
        }

        // Shuffle Play Action Banner
        if (allSongs.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onShufflePlayClick)
                        .testTag("library_shuffle_play"),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Shuffle Play",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Tab Content
        when (currentTab) {
            LibraryTab.SONGS -> {
                if (allSongs.isEmpty()) {
                    item {
                        EmptyLibraryState(
                            message = "No songs found in your library.",
                            actionLabel = "Import Audio Files",
                            onAction = { filePickerLauncher.launch(arrayOf("audio/*")) }
                        )
                    }
                } else {
                    items(allSongs) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = isPlaying,
                            isCurrentSong = currentSong?.id == song.id,
                            onClick = { onSongClick(song) },
                            onFavoriteToggle = { onFavoriteToggle(song) },
                            onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                            onPlayNextClick = { onPlayNextClick(song) }
                        )
                    }
                }
            }

            LibraryTab.PLAYLISTS -> {
                item {
                    // Create Playlist Button Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onCreatePlaylistClick)
                            .testTag("create_playlist_button"),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Playlist",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Create New Playlist",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Organize your favorite anime tracks",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }
                    }
                }

                items(allPlaylists) { playlist ->
                    PlaylistItemRow(
                        playlist = playlist,
                        onClick = { onOpenPlaylist(playlist) }
                    )
                }
            }

            LibraryTab.ALBUMS -> {
                items(allAlbums) { album ->
                    AlbumItemRow(
                        album = album,
                        songs = allSongs.filter { it.album == album },
                        onSongClick = onSongClick
                    )
                }
            }

            LibraryTab.ARTISTS -> {
                items(allArtists) { artist ->
                    ArtistItemRow(
                        artist = artist,
                        songs = allSongs.filter { it.artist == artist },
                        onSongClick = onSongClick
                    )
                }
            }

            LibraryTab.GENRES -> {
                items(allGenres) { genre ->
                    GenreItemRow(
                        genre = genre,
                        songs = allSongs.filter { it.genre == genre },
                        onSongClick = onSongClick
                    )
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = null,
        modifier = Modifier.testTag(testTag)
    )
}

@Composable
private fun PlaylistItemRow(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("playlist_item_${playlist.id}"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(54.dp)) {
                AnimeArtwork(
                    artworkUri = playlist.coverArtworkUri,
                    modifier = Modifier.size(54.dp),
                    cornerRadius = 16.dp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (playlist.description.isNotBlank()) playlist.description else "Custom Playlist",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AlbumItemRow(
    album: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { songs.firstOrNull()?.let { onSongClick(it) } }
            .padding(horizontal = 20.dp, vertical = 6.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(54.dp)) {
                AnimeArtwork(
                    artworkUri = songs.firstOrNull()?.artworkUri,
                    modifier = Modifier.size(54.dp),
                    cornerRadius = 16.dp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = "${songs.firstOrNull()?.artist ?: "Various"} • ${songs.size} tracks",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun ArtistItemRow(
    artist: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { songs.firstOrNull()?.let { onSongClick(it) } }
            .padding(horizontal = 20.dp, vertical = 6.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = "${songs.size} songs",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun GenreItemRow(
    genre: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { songs.firstOrNull()?.let { onSongClick(it) } }
            .padding(horizontal = 20.dp, vertical = 6.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = genre,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = "${songs.size} tracks",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyLibraryState(
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlaylistPlay,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onAction)
        ) {
            Text(
                text = actionLabel,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }
}
