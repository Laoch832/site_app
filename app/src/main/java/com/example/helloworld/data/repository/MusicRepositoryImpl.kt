package com.example.helloworld.data.repository

import android.content.Context
import com.example.helloworld.ConfigManager
import com.example.helloworld.data.model.MusicItem
import com.example.helloworld.data.network.ApiService
import com.example.helloworld.domain.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

import com.example.helloworld.utils.LogManager
import io.sentry.Sentry
import retrofit2.HttpException

class MusicRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : MusicRepository {

    override suspend fun getMusicList(): List<MusicItem> {
        LogManager.i("MusicRepo", "Fetching music list...")
        return try {
            val list = apiService.getMusic()
            
            // Validate required fields
            list.forEach { item ->
                if (item.title.isEmpty() || item.url.isEmpty()) {
                    val errorMsg = "Music item missing required fields: title or url. Item: $item"
                    LogManager.e("MusicRepo", errorMsg)
                    Sentry.captureMessage(errorMsg)
                }
            }

            val baseUrl = ConfigManager.getSelectedDomain(context)
            
            list.map { item ->
                item.copy(
                    url = if (item.url.startsWith("http")) item.url else "$baseUrl${item.url}",
                    cover = if (item.cover?.startsWith("http") == true) item.cover else if (item.cover != null) "$baseUrl${item.cover}" else null
                )
            }
        } catch (e: HttpException) {
            val errorMsg = "Music API returned error: ${e.code()} ${e.message()}"
            LogManager.e("MusicRepo", errorMsg)
            Sentry.captureException(e)
            throw e
        } catch (e: Exception) {
            val errorMsg = "Music network error: ${e.message}"
            LogManager.e("MusicRepo", errorMsg)
            Sentry.captureException(e)
            throw e
        }
    }
}
