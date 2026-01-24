package com.example.helloworld.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.helloworld.data.model.MusicItem
import com.example.helloworld.domain.repository.MusicRepository
import com.example.helloworld.service.MediaPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "MusicViewModel"

    private val _musicList = MutableStateFlow<List<MusicItem>>(emptyList())
    val musicList: StateFlow<List<MusicItem>> = _musicList.asStateFlow()

    private val _currentTrack = MutableStateFlow<MusicItem?>(null)
    val currentTrack: StateFlow<MusicItem?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    init {
        loadMusic()
        initializeMediaController()
    }

    private fun loadMusic() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Starting to load music...")
            try {
                val result = repository.getMusicList()
                Log.d(TAG, "Loaded ${result.size} music items successfully")
                _musicList.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error loading music: ${e.message}", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaPlaybackService::class.java)
        )
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture?.addListener(
            {
                try {
                    mediaController = mediaControllerFuture?.get()
                    mediaController?.addListener(object : Player.Listener {
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            val id = mediaItem?.mediaId
                            _currentTrack.value = _musicList.value.find { it.url == id }
                        }
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                        }
                    })
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to connect to MediaSession", e)
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    fun playMusic(music: MusicItem) {
        val controller = mediaController ?: return
        val list = _musicList.value
        val index = list.indexOf(music)
        if (index == -1) return

        val mediaItems = list.map { item ->
            MediaItem.Builder()
                .setUri(item.url)
                .setMediaId(item.url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(item.name)
                        .setArtist(item.artist)
                        .setArtworkUri(if (item.cover != null) Uri.parse(item.cover) else null)
                        .build()
                )
                .build()
        }

        controller.setMediaItems(mediaItems, index, 0)
        controller.prepare()
        controller.play()
    }

    fun togglePlay() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaControllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}
