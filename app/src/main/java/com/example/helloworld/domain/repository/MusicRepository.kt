package com.example.helloworld.domain.repository

import com.example.helloworld.data.model.MusicItem
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getMusicList(): List<MusicItem>
}
