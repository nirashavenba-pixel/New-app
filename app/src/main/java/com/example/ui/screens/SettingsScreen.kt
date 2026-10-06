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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen

@Composable
fun SettingsScreen(
    totalSongs: Int,
    totalPlaylists: Int,
    isDarkTheme: Boolean?,
    onToggleTheme: (Boolean?) -> Unit,
    onNavigate: (Screen) -> Unit,
    onImportUris: (List<Uri>) -> Unit,
    modifier: Modifier = Modifier
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) onImportUris(uris)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                )
                Text(
                    text = "Audio preferences & library management",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Section: Audio
        item {
            SettingsSectionHeader(title = "Audio & Sound")
            SettingsItemCard(
                icon = Icons.Default.Equalizer,
                title = "Zero Equalizer & Effects",
                subtitle = "5-band frequency tuning & Lo-Fi bass boost",
                onClick = { onNavigate(Screen.EQUALIZER) },
                testTag = "settings_equalizer_item"
            )
            SettingsItemCard(
                icon = Icons.Default.Audiotrack,
                title = "Supported Formats",
                subtitle = "FLAC, MP3, WAV, AAC, M4A, OGG",
                onClick = {}
            )
        }

        // Section: Library
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SettingsSectionHeader(title = "Music Library")
            SettingsItemCard(
                icon = Icons.Default.FileDownload,
                title = "Import Audio Files",
                subtitle = "Import files from Downloads, Music or SD card",
                onClick = { filePickerLauncher.launch(arrayOf("audio/*")) },
                testTag = "settings_import_audio_item"
            )
            SettingsItemCard(
                icon = Icons.Default.LibraryMusic,
                title = "Library Status",
                subtitle = "$totalSongs tracks • $totalPlaylists playlists stored locally",
                onClick = { onNavigate(Screen.LIBRARY) }
            )
            SettingsItemCard(
                icon = Icons.Default.Storage,
                title = "Local Storage",
                subtitle = "High performance Room SQLite database",
                onClick = {}
            )
        }

        // Section: Appearance
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SettingsSectionHeader(title = "Appearance")
            SettingsItemCard(
                icon = Icons.Default.Palette,
                title = "Theme: ${if (isDarkTheme == true) "Anime Midnight (Dark)" else "Pastel Dream (Light)"}",
                subtitle = "Tap to switch between Pastel Light and Midnight Dark theme",
                onClick = { onToggleTheme(isDarkTheme != true) },
                testTag = "settings_theme_toggle"
            )
        }

        // Section: About
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SettingsSectionHeader(title = "About")
            SettingsItemCard(
                icon = Icons.Default.Info,
                title = "Zero Player v1.0",
                subtitle = "Anime music player for Android",
                onClick = {}
            )
            SettingsItemCard(
                icon = Icons.Default.Palette,
                title = "Design Style",
                subtitle = "Soft anime aesthetic with pastel gradients",
                onClick = {}
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun SettingsItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
