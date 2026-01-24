package com.example.helloworld.data.repository

import android.content.Context
import com.example.helloworld.ConfigManager
import com.example.helloworld.data.model.MusicItem
import com.example.helloworld.data.network.ApiService
import com.example.helloworld.domain.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : MusicRepository {

    override suspend fun getMusicList(): List<MusicItem> {
        val list = apiService.getMusic()
        val baseUrl = ConfigManager.getSelectedDomain(context)
        // Ensure baseUrl does not end with / if paths start with /, or handle it.
        // ConfigManager says: "https://..."
        // JSON paths usually "/music/..."
        // So "$baseUrl$path" is safe if baseUrl doesn't have trailing slash.
        // ConfigManager: "https://mirror2-love.canz-monkey.cn" (no trailing slash)
        
        return list.map { item ->
            item.copy(
                url = if (item.url.startsWith("http")) item.url else "$baseUrl${item.url}",
                cover = if (item.cover?.startsWith("http") == true) item.cover else if (item.cover != null) "$baseUrl${item.cover}" else null
            )
        }
    }
}
