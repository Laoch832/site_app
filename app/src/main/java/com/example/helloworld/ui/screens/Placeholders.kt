package com.example.helloworld.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.helloworld.data.model.ImageItem
import com.example.helloworld.data.model.MusicItem
import com.example.helloworld.ui.components.GlassCard
import com.example.helloworld.ui.components.LoadingIndicator
import com.example.helloworld.ui.viewmodel.GalleryViewModel
import com.example.helloworld.ui.viewmodel.MusicViewModel
import com.example.helloworld.ui.viewmodel.NotesViewModel

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val images by viewModel.images.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) {
        LoadingIndicator()
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            items(images) { image ->
                GalleryItemCard(
                    image = image,
                    onToggleFeatured = { viewModel.toggleFeatured(image) }
                )
            }
        }
    }
}

@Composable
fun GalleryItemCard(
    image: ImageItem,
    onToggleFeatured: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().wrapContentHeight()
    ) {
        Box {
            AsyncImage(
                model = image.thumbnail ?: image.url,
                contentDescription = image.name,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = onToggleFeatured,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (image.isFeatured) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Toggle Featured",
                    tint = if (image.isFeatured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MusicScreen(
    viewModel: MusicViewModel = hiltViewModel()
) {
    val musicList by viewModel.musicList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) {
        LoadingIndicator()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(musicList) { music ->
                MusicItemRow(music = music, onClick = { viewModel.playMusic(music) })
            }
        }
    }
}

@Composable
fun MusicItemRow(music: MusicItem, onClick: () -> Unit) {
    GlassCard(modifier = Modifier.padding(bottom = 8.dp)) {
        ListItem(
            headlineContent = { Text(music.name) },
            supportingContent = { Text(music.artist ?: "Unknown Artist") },
            leadingContent = {
                AsyncImage(
                    model = music.cover,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(0.15f),
                    contentScale = ContentScale.Crop
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { onClick() }
        )
    }
}

@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) {
        LoadingIndicator()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(notes) { note ->
                // Reuse DiaryCard from HomeScreen
                DiaryCard(note = note)
            }
        }
    }
}
