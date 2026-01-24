package com.example.helloworld.data.network

import com.example.helloworld.data.model.ImageItem
import com.example.helloworld.data.model.MusicItem
import retrofit2.http.GET

interface ApiService {
    @GET("images.json")
    suspend fun getImages(): List<ImageItem>

    @GET("music.json")
    suspend fun getMusic(): List<MusicItem>
}
