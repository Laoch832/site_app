package com.example.helloworld.domain.repository

import com.example.helloworld.data.model.ImageItem

interface GalleryRepository {
    suspend fun getImages(): List<ImageItem>
    suspend fun toggleFeatured(url: String, isFeatured: Boolean)
}
