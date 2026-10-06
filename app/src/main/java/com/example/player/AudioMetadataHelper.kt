package com.example.player

import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.model.Song
import java.io.File
import java.io.FileOutputStream

object AudioMetadataHelper {
    private const val TAG = "AudioMetadataHelper"

    fun extractMetadata(context: Context, uri: Uri): Song? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)

            var title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            var artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            var album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: "Anime / J-Music"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            val bitrateKbps = bitrateStr?.toLongOrNull()?.let { it / 1000 }

            val mimeType = context.contentResolver.getType(uri)
                ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                ?: getMimeTypeFromUri(uri)

            // Resolve file name if title is missing
            val fileName = getFileName(context, uri)
            if (title.isNullOrBlank()) {
                title = fileName?.substringBeforeLast(".") ?: "Imported Track"
            }
            if (artist.isNullOrBlank()) {
                artist = "Unknown Artist"
            }
            if (album.isNullOrBlank()) {
                album = "Anime Collection"
            }

            // Extract embedded album art
            var artworkPath: String? = null
            val pictureBytes = retriever.embeddedPicture
            if (pictureBytes != null && pictureBytes.isNotEmpty()) {
                artworkPath = saveEmbeddedArtwork(context, pictureBytes, uri.hashCode())
            }

            // Format display (e.g. FLAC 96 kHz • 24 bit or MP3 320 kbps)
            val formatDisplay = buildFormatDisplay(mimeType, bitrateKbps, fileName)

            Song(
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                uriString = uri.toString(),
                mimeType = mimeType,
                formatDisplay = formatDisplay,
                artworkUri = artworkPath,
                genre = genre,
                dateAdded = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract metadata for uri: $uri", e)
            val fileName = getFileName(context, uri) ?: "Audio Track"
            Song(
                title = fileName.substringBeforeLast("."),
                artist = "Unknown Artist",
                album = "Imported Music",
                durationMs = 0L,
                uriString = uri.toString(),
                mimeType = getMimeTypeFromUri(uri),
                formatDisplay = "AUDIO • Local",
                artworkUri = null,
                genre = "Anime / J-Music",
                dateAdded = System.currentTimeMillis()
            )
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    private fun buildFormatDisplay(mimeType: String, bitrateKbps: Long?, fileName: String?): String {
        val ext = fileName?.substringAfterLast(".", "")?.uppercase() ?: ""
        return when {
            mimeType.contains("flac", ignoreCase = true) || ext == "FLAC" -> "FLAC 96 kHz • 24 bit"
            mimeType.contains("wav", ignoreCase = true) || ext == "WAV" -> "WAV Lossless • 44.1 kHz"
            mimeType.contains("ogg", ignoreCase = true) || ext == "OGG" -> "OGG Vorbis • Q8"
            mimeType.contains("aac", ignoreCase = true) || ext == "AAC" -> "AAC 256 kbps"
            mimeType.contains("m4a", ignoreCase = true) || ext == "M4A" -> "M4A ALAC • 24 bit"
            bitrateKbps != null && bitrateKbps > 0 -> "MP3 $bitrateKbps kbps"
            else -> "MP3 320 kbps"
        }
    }

    private fun getMimeTypeFromUri(uri: Uri): String {
        val path = uri.path?.lowercase() ?: ""
        return when {
            path.endsWith(".mp3") -> "audio/mpeg"
            path.endsWith(".flac") -> "audio/flac"
            path.endsWith(".wav") -> "audio/wav"
            path.endsWith(".ogg") -> "audio/ogg"
            path.endsWith(".m4a") -> "audio/mp4"
            path.endsWith(".aac") -> "audio/aac"
            else -> "audio/mpeg"
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        return it.getString(nameIndex)
                    }
                }
            }
        }
        return uri.lastPathSegment
    }

    private fun saveEmbeddedArtwork(context: Context, bytes: ByteArray, seed: Int): String? {
        return try {
            val dir = File(context.filesDir, "album_art")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "art_${System.currentTimeMillis()}_$seed.jpg")
            FileOutputStream(file).use { out ->
                out.write(bytes)
                out.flush()
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save artwork", e)
            null
        }
    }

    fun getDefaultAnimeDemoSongs(context: Context): List<Song> {
        val pkg = context.packageName
        return listOf(
            Song(
                title = "Kaikai Kitan",
                artist = "Eve",
                album = "Smile",
                durationMs = 238000L,
                uriString = "android.resource://$pkg/raw/kaikai_kitan",
                mimeType = "audio/flac",
                formatDisplay = "FLAC 96 kHz • 24 bit",
                artworkUri = "img_cover_sunset",
                genre = "Anime OST",
                isFavorite = true,
                playCount = 12
            ),
            Song(
                title = "Lost in Paradise",
                artist = "ALI",
                album = "Jujutsu Kaisen ED",
                durationMs = 195000L,
                uriString = "android.resource://$pkg/raw/lost_in_paradise",
                mimeType = "audio/mpeg",
                formatDisplay = "MP3 320 kbps",
                artworkUri = "img_cover_city",
                genre = "J-Pop",
                isFavorite = false,
                playCount = 8
            ),
            Song(
                title = "Blue Bird",
                artist = "Ikimono Gakari",
                album = "My Song Your Song",
                durationMs = 216000L,
                uriString = "android.resource://$pkg/raw/blue_bird",
                mimeType = "audio/flac",
                formatDisplay = "FLAC 44.1 kHz • 16 bit",
                artworkUri = "img_cover_torii",
                genre = "Anime Opening",
                isFavorite = true,
                playCount = 15
            ),
            Song(
                title = "Unravel",
                artist = "TK from Ling tosite sigure",
                album = "Fantastic Magic",
                durationMs = 240000L,
                uriString = "android.resource://$pkg/raw/unravel",
                mimeType = "audio/flac",
                formatDisplay = "FLAC 96 kHz • 24 bit",
                artworkUri = "img_cover_city",
                genre = "J-Rock",
                isFavorite = true,
                playCount = 20
            ),
            Song(
                title = "Gurenge",
                artist = "LiSA",
                album = "LEO-NiNE",
                durationMs = 237000L,
                uriString = "android.resource://$pkg/raw/gurenge",
                mimeType = "audio/mpeg",
                formatDisplay = "MP3 320 kbps",
                artworkUri = "img_cover_sunset",
                genre = "Anime Opening",
                isFavorite = false,
                playCount = 5
            ),
            Song(
                title = "A Cruel Angel's Thesis",
                artist = "Yoko Takahashi",
                album = "Neon Genesis Evangelion",
                durationMs = 244000L,
                uriString = "android.resource://$pkg/raw/cruel_angel_thesis",
                mimeType = "audio/wav",
                formatDisplay = "WAV Lossless • 44.1 kHz",
                artworkUri = "img_cover_torii",
                genre = "Anime Classic",
                isFavorite = false,
                playCount = 4
            )
        )
    }
}
