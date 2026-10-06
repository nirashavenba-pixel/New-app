package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.net.Uri
import android.util.Log
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ZeroAudioPlayer(
    private val context: Context,
    private val onSongPlayed: (Song) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var positionTickerJob: Job? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isEqualizerEnabled = MutableStateFlow(true)
    val isEqualizerEnabled: StateFlow<Boolean> = _isEqualizerEnabled.asStateFlow()

    private val _equalizerPreset = MutableStateFlow("Anime Vocal Boost")
    val equalizerPreset: StateFlow<String> = _equalizerPreset.asStateFlow()

    // 5 band levels (-1000 to +1000 mB)
    private val _equalizerBands = MutableStateFlow(listOf(300, 150, 400, 600, 500))
    val equalizerBands: StateFlow<List<Int>> = _equalizerBands.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow<Short>(450)
    val bassBoostStrength: StateFlow<Short> = _bassBoostStrength.asStateFlow()

    init {
        startPositionTicker()
    }

    private fun startPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            _currentPositionMs.value = player.currentPosition.toLong()
                            if (player.duration > 0) {
                                _durationMs.value = player.duration.toLong()
                            }
                        }
                    } catch (_: Exception) {}
                }
                delay(200)
            }
        }
    }

    fun playSong(song: Song, newQueue: List<Song>? = null) {
        if (newQueue != null && newQueue.isNotEmpty()) {
            _queue.value = newQueue
            val index = newQueue.indexOfFirst { it.id == song.id }
            _queueIndex.value = if (index != -1) index else 0
        } else if (_queue.value.none { it.id == song.id }) {
            _queue.value = _queue.value + song
            _queueIndex.value = _queue.value.lastIndex
        }

        _currentSong.value = song
        _currentPositionMs.value = 0L

        setupAndPlay(song)
        onSongPlayed(song)
    }

    private fun setupAndPlay(song: Song) {
        releasePlayer()
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                val uri = Uri.parse(song.uriString)
                if (uri.scheme == "android.resource") {
                    val resName = uri.lastPathSegment
                    val resId = if (resName != null) {
                        context.resources.getIdentifier(resName, "raw", context.packageName)
                    } else 0

                    if (resId != 0) {
                        try {
                            val afd = context.resources.openRawResourceFd(resId)
                            if (afd != null) {
                                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                afd.close()
                            } else {
                                setDataSource(context, uri)
                            }
                        } catch (_: Exception) {
                            setDataSource(context, uri)
                        }
                    } else {
                        setDataSource(context, uri)
                    }
                } else {
                    setDataSource(context, uri)
                }

                setOnPreparedListener { mp ->
                    try {
                        mp.start()
                        _isPlaying.value = true
                        _durationMs.value = if (mp.duration > 0) mp.duration.toLong() else song.durationMs
                        applyVolume(_volume.value)
                        initAudioEffects(mp.audioSessionId)
                    } catch (e: Exception) {
                        Log.e("ZeroAudioPlayer", "Error in onPrepared: ${e.message}")
                    }
                }
                setOnCompletionListener {
                    handleTrackCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("ZeroAudioPlayer", "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("ZeroAudioPlayer", "Error preparing song: ${song.title}", e)
            _isPlaying.value = false
        }
    }

    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                try {
                    mediaPlayer?.seekTo(0)
                    mediaPlayer?.start()
                    _isPlaying.value = true
                } catch (_: Exception) {}
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                val q = _queue.value
                val curIndex = _queueIndex.value
                if (curIndex < q.size - 1) {
                    playNext()
                } else {
                    _isPlaying.value = false
                    try {
                        mediaPlayer?.seekTo(0)
                    } catch (_: Exception) {}
                    _currentPositionMs.value = 0L
                }
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: run {
            _currentSong.value?.let { playSong(it) }
            return
        }
        try {
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
            } else {
                player.start()
                _isPlaying.value = true
            }
        } catch (e: Exception) {
            Log.e("ZeroAudioPlayer", "togglePlayPause error", e)
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            try {
                player.seekTo(positionMs.toInt())
                _currentPositionMs.value = positionMs
            } catch (_: Exception) {}
        }
    }

    fun playNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        val nextIndex = if (_isShuffleEnabled.value) {
            q.indices.filter { it != _queueIndex.value }.randomOrNull() ?: 0
        } else {
            (_queueIndex.value + 1) % q.size
        }

        _queueIndex.value = nextIndex
        val nextSong = q[nextIndex]
        _currentSong.value = nextSong
        setupAndPlay(nextSong)
        onSongPlayed(nextSong)
    }

    fun playPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (_currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = if (_isShuffleEnabled.value) {
            q.indices.filter { it != _queueIndex.value }.randomOrNull() ?: 0
        } else {
            if (_queueIndex.value - 1 < 0) q.size - 1 else _queueIndex.value - 1
        }

        _queueIndex.value = prevIndex
        val prevSong = q[prevIndex]
        _currentSong.value = prevSong
        setupAndPlay(prevSong)
        onSongPlayed(prevSong)
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0.0f, 1.0f)
        _volume.value = clamped
        applyVolume(clamped)
    }

    private fun applyVolume(vol: Float) {
        mediaPlayer?.let {
            try {
                it.setVolume(vol, vol)
            } catch (_: Exception) {}
        }
    }

    private fun initAudioEffects(audioSessionId: Int) {
        try {
            equalizer?.release()
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _isEqualizerEnabled.value
                applyCurrentBandLevels(this)
            }

            bassBoost?.release()
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _isEqualizerEnabled.value
                setStrength(_bassBoostStrength.value)
            }
        } catch (e: Exception) {
            Log.w("ZeroAudioPlayer", "Audio effects init error: ${e.message}")
        }
    }

    private fun applyCurrentBandLevels(eq: Equalizer) {
        try {
            val numBands = eq.numberOfBands.toInt()
            val bands = _equalizerBands.value
            for (i in 0 until minOf(numBands, bands.size)) {
                eq.setBandLevel(i.toShort(), bands[i].toShort())
            }
        } catch (_: Exception) {}
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        _isEqualizerEnabled.value = enabled
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
        } catch (_: Exception) {}
    }

    fun setEqualizerPreset(presetName: String) {
        _equalizerPreset.value = presetName
        val newBands = when (presetName) {
            "Anime Vocal Boost" -> listOf(300, 150, 400, 600, 500)
            "Bass & Lo-Fi" -> listOf(700, 500, 100, -100, -200)
            "J-Rock Energetic" -> listOf(400, 200, -100, 400, 700)
            "Acoustic & Piano" -> listOf(100, 200, 300, 400, 300)
            "Club / Electronic" -> listOf(600, 300, 0, 300, 600)
            "Flat / Natural" -> listOf(0, 0, 0, 0, 0)
            else -> listOf(300, 150, 400, 600, 500)
        }
        _equalizerBands.value = newBands
        equalizer?.let { applyCurrentBandLevels(it) }
    }

    fun setBandLevel(bandIndex: Int, levelMilliBels: Int) {
        val current = _equalizerBands.value.toMutableList()
        if (bandIndex in current.indices) {
            current[bandIndex] = levelMilliBels
            _equalizerBands.value = current
            _equalizerPreset.value = "Custom"
            try {
                equalizer?.setBandLevel(bandIndex.toShort(), levelMilliBels.toShort())
            } catch (_: Exception) {}
        }
    }

    fun setBassBoost(strength: Short) {
        _bassBoostStrength.value = strength
        try {
            bassBoost?.setStrength(strength)
        } catch (_: Exception) {}
    }

    fun addToQueue(song: Song) {
        _queue.value = _queue.value + song
    }

    fun removeFromQueue(index: Int) {
        val current = _queue.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _queue.value = current
            if (index < _queueIndex.value) {
                _queueIndex.value = (_queueIndex.value - 1).coerceAtLeast(0)
            } else if (index == _queueIndex.value) {
                if (current.isNotEmpty()) {
                    val newIdx = index.coerceAtMost(current.lastIndex)
                    _queueIndex.value = newIdx
                    playSong(current[newIdx])
                } else {
                    try {
                        mediaPlayer?.stop()
                    } catch (_: Exception) {}
                    _isPlaying.value = false
                    _currentSong.value = null
                }
            }
        }
    }

    private fun releasePlayer() {
        try {
            equalizer?.release()
            equalizer = null
            bassBoost?.release()
            bassBoost = null
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
    }

    fun release() {
        positionTickerJob?.cancel()
        releasePlayer()
    }
}
