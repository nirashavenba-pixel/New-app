package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import java.io.File

@Composable
fun AnimeArtwork(
    artworkUri: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    contentDescription: String = "Album Artwork"
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF8E9AFF),
                        Color(0xFFFF94B4),
                        Color(0xFF5CE1E6)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            artworkUri == "img_cover_sunset" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_cover_sunset),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            artworkUri == "img_cover_torii" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_cover_torii),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            artworkUri == "img_cover_city" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_cover_city),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            artworkUri != null && File(artworkUri).exists() -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(artworkUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                // Beautiful anime gradient fallback with music icon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF6C7AF8),
                                    Color(0xFFFF8DA8),
                                    Color(0xFF7CE4EB),
                                    Color(0xFF6C7AF8)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = contentDescription,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
