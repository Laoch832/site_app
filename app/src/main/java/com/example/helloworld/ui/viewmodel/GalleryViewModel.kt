package com.example.helloworld.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloworld.data.model.ImageItem
import com.example.helloworld.domain.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: GalleryRepository
) : ViewModel() {

    private val TAG = "GalleryViewModel"

    private val _images = MutableStateFlow<List<ImageItem>>(emptyList())
    val images: StateFlow<List<ImageItem>> = _images.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadImages()
    }

    fun uploadImage(byteArray: ByteArray, fileName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.uploadImage(byteArray, fileName)
                loadImages() // Refresh list after upload
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading image: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadImages() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Starting to load images...")
            try {
                val result = repository.getImages()
                Log.d(TAG, "Loaded ${result.size} images successfully")
                _images.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error loading images: ${e.message}", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFeatured(image: ImageItem) {
        viewModelScope.launch {
            Log.d(TAG, "Toggling featured for: ${image.name}, current: ${image.isFeatured}")
            try {
                val newStatus = !image.isFeatured
                repository.toggleFeatured(image.url, newStatus)
                // Update local state optimistically
                _images.value = _images.value.map {
                    if (it.url == image.url) it.copy(isFeatured = newStatus) else it
                }
                Log.d(TAG, "Featured status updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling featured: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
}
