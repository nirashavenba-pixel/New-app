package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import com.example.player.AudioMetadataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MusicRepository(
    private val database: AppDatabase,
    private val context: Context
) {
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()

    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()
    val recentlyPlayed: Flow<List<Song>> = songDao.getRecentlyPlayed()
    val recentlyAdded: Flow<List<Song>> = songDao.getRecentlyAdded()
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    val allArtists: Flow<List<String>> = songDao.getAllArtists()
    val allAlbums: Flow<List<String>> = songDao.getAllAlbums()
    val allGenres: Flow<List<String>> = songDao.getAllGenres()

    suspend fun checkAndSeedInitialSongs() = withContext(Dispatchers.IO) {
        val count = songDao.getSongCount()
        if (count == 0) {
            val demoSongs = AudioMetadataHelper.getDefaultAnimeDemoSongs(context)
            songDao.insertSongs(demoSongs)

            // Seed initial playlists
            val animeFavoritesPlaylist = Playlist(
                name = "Anime Top Hits",
                description = "Iconic openings, endings and OSTs",
                coverArtworkUri = "img_cover_sunset"
            )
            val lofiVibesPlaylist = Playlist(
                name = "Tokyo Midnight Lo-Fi",
                description = "Chill anime beats for relaxing and studying",
                coverArtworkUri = "img_cover_city"
            )
            val p1Id = playlistDao.insertPlaylist(animeFavoritesPlaylist)
            val p2Id = playlistDao.insertPlaylist(lofiVibesPlaylist)

            // Add songs to initial playlists
            playlistDao.insertCrossRef(PlaylistSongCrossRef(playlistId = p1Id, songId = 1, orderIndex = 0))
            playlistDao.insertCrossRef(PlaylistSongCrossRef(playlistId = p1Id, songId = 2, orderIndex = 1))
            playlistDao.insertCrossRef(PlaylistSongCrossRef(playlistId = p1Id, songId = 3, orderIndex = 2))
            playlistDao.insertCrossRef(PlaylistSongCrossRef(playlistId = p2Id, songId = 4, orderIndex = 0))
            playlistDao.insertCrossRef(PlaylistSongCrossRef(playlistId = p2Id, songId = 5, orderIndex = 1))
        }
    }

    suspend fun importAudioUris(uris: List<Uri>): List<Song> = withContext(Dispatchers.IO) {
        val imported = mutableListOf<Song>()
        for (uri in uris) {
            val song = AudioMetadataHelper.extractMetadata(context, uri)
            if (song != null) {
                val id = songDao.insertSong(song)
                imported.add(song.copy(id = id))
            }
        }
        imported
    }

    suspend fun toggleFavorite(song: Song) = withContext(Dispatchers.IO) {
        songDao.updateFavorite(song.id, !song.isFavorite)
    }

    suspend fun recordPlayed(song: Song) = withContext(Dispatchers.IO) {
        songDao.recordPlayed(song.id, System.currentTimeMillis())
    }

    suspend fun deleteSong(song: Song) = withContext(Dispatchers.IO) {
        songDao.deleteSong(song)
    }

    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)

    fun getSongsByArtist(artist: String): Flow<List<Song>> = songDao.getSongsByArtist(artist)

    fun getSongsByAlbum(album: String): Flow<List<Song>> = songDao.getSongsByAlbum(album)

    fun getSongsByGenre(genre: String): Flow<List<Song>> = songDao.getSongsByGenre(genre)

    suspend fun createPlaylist(name: String, description: String = ""): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(Playlist(name = name, description = description, coverArtworkUri = "img_cover_sunset"))
    }

    suspend fun renamePlaylist(playlist: Playlist, newName: String) = withContext(Dispatchers.IO) {
        playlistDao.updatePlaylist(playlist.copy(name = newName))
    }

    suspend fun deletePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistWithSongs(playlist)
    }

    suspend fun reorderSongsInPlaylist(playlistId: Long, orderedSongIds: List<Long>) = withContext(Dispatchers.IO) {
        playlistDao.reorderSongs(playlistId, orderedSongIds)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        val currentCount = playlistDao.getSongCountForPlaylist(playlistId)
        playlistDao.insertCrossRef(PlaylistSongCrossRef(playlistId, songId, currentCount))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> = playlistDao.getSongsForPlaylist(playlistId)

    suspend fun getSongsListForPlaylist(playlistId: Long): List<Song> = withContext(Dispatchers.IO) {
        playlistDao.getSongsListForPlaylist(playlistId)
    }
}
