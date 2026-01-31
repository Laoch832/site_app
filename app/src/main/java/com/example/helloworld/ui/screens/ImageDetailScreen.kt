package com.example.helloworld.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.helloworld.data.cache.ResourceCacheManager
import com.example.helloworld.data.model.ImageItem
import com.example.helloworld.utils.LogManager
import kotlinx.coroutines.launch
import java.io.File

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(
    image: ImageItem,
    onBack: () -> Unit,
    cacheManager: ResourceCacheManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

    // Zoom and Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    
                    // Allow panning only when zoomed in
                    if (scale > 1f) {
                        val newOffset = offset + pan
                        // Calculate bounds (simplified for Fit scale)
                        // For a production app, you'd calculate exact bounds based on image aspect ratio
                        offset = newOffset
                    } else {
                        offset = Offset.Zero
                    }
                }
            }
    ) {
        // High-def Image
        AsyncImage(
            model = image.url,
            contentDescription = image.name,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )

        // Top Bar
        TopAppBar(
            title = { Text(image.name, color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Bottom Actions
        // Hide actions when zoomed in to avoid blocking view
        if (scale <= 1.1f) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Share Button
                FilledTonalButton(
                    onClick = { shareImage(context, image, cacheManager) },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                    Spacer(Modifier.width(8.dp))
                    Text("分享")
                }

                // Download Button
                Button(
                    onClick = {
                        scope.launch {
                            isDownloading = true
                            cacheManager.downloadToCache(image.url)
                            isDownloading = false
                        }
                    },
                    enabled = !isDownloading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Download, contentDescription = "Download")
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("下载")
                }
            }
        }
    }
}

private fun shareImage(context: Context, image: ImageItem, cacheManager: ResourceCacheManager) {
    val file = cacheManager.getCachedFile(image.url)
    if (file == null || !file.exists()) {
        // Fallback: If not in cache, we might need to download or share the URL
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "分享图片: ${image.url}")
        }
        context.startActivity(Intent.createChooser(intent, "分享图片"))
        return
    }

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享图片"))
}
