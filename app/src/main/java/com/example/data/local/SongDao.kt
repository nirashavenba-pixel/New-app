package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: Long): Song?

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT 20")
    fun getRecentlyPlayed(): Flow<List<Song>>

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT 20")
    fun getRecentlyAdded(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchSongs(query: String): Flow<List<Song>>

    @Query("SELECT DISTINCT artist FROM songs ORDER BY artist ASC")
    fun getAllArtists(): Flow<List<String>>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY title ASC")
    fun getSongsByArtist(artist: String): Flow<List<Song>>

    @Query("SELECT DISTINCT album FROM songs ORDER BY album ASC")
    fun getAllAlbums(): Flow<List<String>>

    @Query("SELECT * FROM songs WHERE album = :album ORDER BY title ASC")
    fun getSongsByAlbum(album: String): Flow<List<Song>>

    @Query("SELECT DISTINCT genre FROM songs ORDER BY genre ASC")
    fun getAllGenres(): Flow<List<String>>

    @Query("SELECT * FROM songs WHERE genre = :genre ORDER BY title ASC")
    fun getSongsByGenre(genre: String): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>): List<Long>

    @Update
    suspend fun updateSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("UPDATE songs SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE songs SET lastPlayedTimestamp = :timestamp, playCount = playCount + 1 WHERE id = :id")
    suspend fun recordPlayed(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
}
