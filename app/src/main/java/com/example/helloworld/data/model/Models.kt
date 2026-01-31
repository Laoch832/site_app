package com.example.helloworld.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.google.gson.annotations.SerializedName

@Serializable
data class FeaturedImage(
    @SerialName("url")
    @SerializedName("url")
    val url: String
)

@Serializable
data class ImageItem(
    @SerialName("name")
    @SerializedName("name")
    val name: String,
    @SerialName("url")
    @SerializedName("url")
    val url: String,
    @SerialName("thumbnail")
    @SerializedName("thumbnail")
    val thumbnail: String? = null,
    var isFeatured: Boolean = false // Local state for UI
)

@Serializable
data class MusicItem(
    @SerialName("id")
    @SerializedName("id")
    val id: String = "",
    @SerialName("filename")
    @SerializedName("filename")
    val filename: String = "",
    @SerialName("title")
    @SerializedName("title")
    val title: String,
    @SerialName("artist")
    @SerializedName("artist")
    val artist: String? = null,
    @SerialName("album")
    @SerializedName("album")
    val album: String? = null,
    @SerialName("url")
    @SerializedName("url")
    val url: String,
    @SerialName("cover")
    @SerializedName("cover")
    val cover: String? = null,
    @SerialName("uploadedAt")
    @SerializedName("uploadedAt")
    val uploadedAt: String? = null
)

@Serializable
data class QuoteItem(
    @SerialName("hitokoto")
    @SerializedName("hitokoto")
    val hitokoto: String,
    @SerialName("from")
    @SerializedName("from")
    val from: String
)

@Serializable
data class Note(
    @SerialName("id")
    @SerializedName("id")
    val id: String? = null, // Changed to String to support UUID, nullable for new notes
    @SerialName("title")
    @SerializedName("title")
    val title: String,
    @SerialName("content")
    @SerializedName("content")
    val content: String,
    @SerialName("created_at")
    @SerializedName("created_at")
    val created_at: String? = null, // Allow null for new notes
    @SerialName("is_published")
    @SerializedName("is_published")
    val is_published: Boolean = true,
    @SerialName("user_id")
    @SerializedName("user_id")
    val user_id: String? = null
)
