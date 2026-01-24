package com.example.helloworld.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageItem(
    val name: String,
    val url: String,
    val thumbnail: String? = null,
    var isFeatured: Boolean = false // Local state for UI
)

@Serializable
data class MusicItem(
    val id: String = "", // Added ID
    val name: String,
    val url: String,
    val cover: String? = null,
    val artist: String? = null
)

@Serializable
data class Note(
    val id: Int = 0, // Default for Supabase auto-increment
    val title: String,
    val content: String,
    val created_at: String? = null, // Allow null for new notes
    val is_published: Boolean = true,
    val user_id: String? = null
)
