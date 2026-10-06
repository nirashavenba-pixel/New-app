package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.player.AudioMetadataHelper
import com.example.player.RepeatMode
import com.example.player.ZeroAudioPlayer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ZeroPlayerRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: MusicRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MusicRepository(database, context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSongInsertionAndFavorites() = runBlocking {
        val songDao = database.songDao()
        val song1 = Song(
            title = "Kaikai Kitan",
            artist = "Eve",
            album = "Smile",
            durationMs = 238000L,
            uriString = "android.resource://${context.packageName}/raw/kaikai_kitan",
            formatDisplay = "FLAC 96 kHz • 24 bit"
        )
        val id1 = songDao.insertSong(song1)
        assertTrue(id1 > 0)

        val retrieved = songDao.getSongById(id1)
        assertNotNull(retrieved)
        assertEquals("Kaikai Kitan", retrieved?.title)
        assertEquals("Eve", retrieved?.artist)
        assertFalse(retrieved?.isFavorite ?: true)

        // Toggle favorite
        songDao.updateFavorite(id1, true)
        val favs = songDao.getFavoriteSongs().first()
        assertEquals(1, favs.size)
        assertEquals("Kaikai Kitan", favs[0].title)
    }

    @Test
    fun testPlaylistCreationRenameAndDelete() = runBlocking {
        val playlistDao = database.playlistDao()

        // 1. Create Playlist
        val p1 = Playlist(name = "Anime High Energy", description = "For workout & focus")
        val p1Id = playlistDao.insertPlaylist(p1)
        assertTrue(p1Id > 0)

        val fetched = playlistDao.getPlaylistById(p1Id)
        assertNotNull(fetched)
        assertEquals("Anime High Energy", fetched?.name)

        // 2. Rename Playlist
        playlistDao.updatePlaylist(fetched!!.copy(name = "Anime Ultra Energy"))
        val renamed = playlistDao.getPlaylistById(p1Id)
        assertEquals("Anime Ultra Energy", renamed?.name)

        // 3. Delete Playlist
        playlistDao.deletePlaylistWithSongs(renamed!!)
        val deleted = playlistDao.getPlaylistById(p1Id)
        assertNull(deleted)
    }

    @Test
    fun testPlaylistAddRemoveAndReorderSongs() = runBlocking {
        val songDao = database.songDao()
        val playlistDao = database.playlistDao()

        // Insert 3 test songs
        val s1Id = songDao.insertSong(Song(title = "Song A", artist = "Artist A", album = "Album A", durationMs = 180000L, uriString = "uri://a"))
        val s2Id = songDao.insertSong(Song(title = "Song B", artist = "Artist B", album = "Album B", durationMs = 200000L, uriString = "uri://b"))
        val s3Id = songDao.insertSong(Song(title = "Song C", artist = "Artist C", album = "Album C", durationMs = 220000L, uriString = "uri://c"))

        // Create Playlist
        val playlistId = playlistDao.insertPlaylist(Playlist(name = "Chill Beats"))

        // Add songs with initial order
        playlistDao.insertCrossRef(PlaylistSongCrossRef(playlistId, s1Id, 0))
        playlistDao.insertCrossRef(PlaylistSongCrossRef(playlistId, s2Id, 1))
        playlistDao.insertCrossRef(PlaylistSongCrossRef(playlistId, s3Id, 2))

        var songsInPlaylist = playlistDao.getSongsListForPlaylist(playlistId)
        assertEquals(3, songsInPlaylist.size)
        assertEquals("Song A", songsInPlaylist[0].title)
        assertEquals("Song B", songsInPlaylist[1].title)
        assertEquals("Song C", songsInPlaylist[2].title)

        // Reorder songs: C first, then A, then B
        playlistDao.reorderSongs(playlistId, listOf(s3Id, s1Id, s2Id))
        songsInPlaylist = playlistDao.getSongsListForPlaylist(playlistId)
        assertEquals(3, songsInPlaylist.size)
        assertEquals("Song C", songsInPlaylist[0].title)
        assertEquals("Song A", songsInPlaylist[1].title)
        assertEquals("Song B", songsInPlaylist[2].title)

        // Remove Song A
        playlistDao.removeSongFromPlaylist(playlistId, s1Id)
        songsInPlaylist = playlistDao.getSongsListForPlaylist(playlistId)
        assertEquals(2, songsInPlaylist.size)
        assertEquals("Song C", songsInPlaylist[0].title)
        assertEquals("Song B", songsInPlaylist[1].title)
    }

    @Test
    fun testAudioPlayerControlsAndState() {
        var playedSong: Song? = null
        val player = ZeroAudioPlayer(context) { song ->
            playedSong = song
        }

        val testSong1 = Song(
            id = 1,
            title = "Kaikai Kitan",
            artist = "Eve",
            album = "Smile",
            durationMs = 238000L,
            uriString = "android.resource://${context.packageName}/raw/kaikai_kitan"
        )
        val testSong2 = Song(
            id = 2,
            title = "Lost in Paradise",
            artist = "ALI",
            album = "Jujutsu Kaisen ED",
            durationMs = 195000L,
            uriString = "android.resource://${context.packageName}/raw/lost_in_paradise"
        )

        // Play song
        player.playSong(testSong1, listOf(testSong1, testSong2))
        assertEquals(testSong1, player.currentSong.value)
        assertEquals(testSong1, playedSong)
        assertEquals(2, player.queue.value.size)

        // Repeat mode cycling
        assertEquals(RepeatMode.OFF, player.repeatMode.value)
        player.cycleRepeatMode()
        assertEquals(RepeatMode.ALL, player.repeatMode.value)
        player.cycleRepeatMode()
        assertEquals(RepeatMode.ONE, player.repeatMode.value)
        player.cycleRepeatMode()
        assertEquals(RepeatMode.OFF, player.repeatMode.value)

        // Shuffle toggle
        assertFalse(player.isShuffleEnabled.value)
        player.toggleShuffle()
        assertTrue(player.isShuffleEnabled.value)

        // Volume control
        player.setVolume(0.75f)
        assertEquals(0.75f, player.volume.value, 0.01f)

        // Equalizer preset
        player.setEqualizerPreset("Anime Vocal Boost")
        assertEquals("Anime Vocal Boost", player.equalizerPreset.value)
        assertEquals(listOf(300, 150, 400, 600, 500), player.equalizerBands.value)

        // Bass boost
        player.setBassBoost(600)
        assertEquals(600.toShort(), player.bassBoostStrength.value)

        player.release()
    }

    @Test
    fun testDefaultAnimeDemoSongs() {
        val songs = AudioMetadataHelper.getDefaultAnimeDemoSongs(context)
        assertTrue(songs.size >= 4)
        assertTrue(songs.any { it.title == "Kaikai Kitan" })
        assertTrue(songs.any { it.title == "Lost in Paradise" })
        assertTrue(songs.any { it.title == "Blue Bird" })
        assertTrue(songs.any { it.title == "Unravel" })
    }
}
