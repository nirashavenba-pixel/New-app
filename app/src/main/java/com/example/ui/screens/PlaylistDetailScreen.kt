package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.components.AnimeArtwork

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist?,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddSongsClick: () -> Unit,
    onRemoveSong: (Long) -> Unit,
    onMoveSong: (fromIndex: Int, toIndex: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    if (playlist == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Playlist not found")
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlist_detail_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Navigation Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("playlist_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    IconButton(
                        onClick = onRenameClick,
                        modifier = Modifier.testTag("playlist_rename_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename Playlist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.testTag("playlist_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Playlist",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Playlist Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(160.dp)
                ) {
                    AnimeArtwork(
                        artworkUri = playlist.coverArtworkUri,
                        modifier = Modifier.size(160.dp),
                        cornerRadius = 28.dp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (playlist.description.isNotBlank()) playlist.description else "${songs.size} tracks",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Actions: Play All, Shuffle, Add Songs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("playlist_play_all"),
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Play",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onShuffleAll,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("playlist_shuffle"),
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Shuffle",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onAddSongsClick)
                            .testTag("playlist_add_songs_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add songs",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Tracks (${songs.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )
        }

        if (songs.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "This playlist is empty.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onAddSongsClick,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Add Songs to Playlist")
                    }
                }
            }
        } else {
            itemsIndexed(songs) { index, song ->
                PlaylistSongRow(
                    index = index + 1,
                    song = song,
                    isCurrentSong = currentSong?.id == song.id,
                    isPlaying = isPlaying && currentSong?.id == song.id,
                    canMoveUp = index > 0,
                    canMoveDown = index < songs.size - 1,
                    onClick = { onSongClick(song) },
                    onMoveUp = { onMoveSong(index, index - 1) },
                    onMoveDown = { onMoveSong(index, index + 1) },
                    onRemove = { onRemoveSong(song.id) }
                )
            }
        }
    }
}

@Composable
private fun PlaylistSongRow(
    index: Int,
    song: Song,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("playlist_track_$index"),
        color = if (isCurrentSong) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(22.dp)
            )

            Box(modifier = Modifier.size(44.dp)) {
                AnimeArtwork(
                    artworkUri = song.artworkUri,
                    modifier = Modifier.size(44.dp),
                    cornerRadius = 12.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Reorder Up
            IconButton(
                onClick = onMoveUp,
                enabled = canMoveUp,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("move_up_$index")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Move Up",
                    tint = if (canMoveUp) MaterialTheme.colorScheme.onSurfaceVariant else Color.LightGray.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Reorder Down
            IconButton(
                onClick = onMoveDown,
                enabled = canMoveDown,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("move_down_$index")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Move Down",
                    tint = if (canMoveDown) MaterialTheme.colorScheme.onSurfaceVariant else Color.LightGray.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Remove from Playlist
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("remove_from_playlist_$index")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from playlist",
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
