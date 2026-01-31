package com.example.helloworld.data.cache

import android.content.Context
import com.example.helloworld.ConfigManager
import com.example.helloworld.data.network.ApiService
import com.example.helloworld.utils.LogManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ResourceCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val okHttpClient: OkHttpClient
) {
    private val TAG = "ResourceCacheManager"
    private val cacheDir = File(context.filesDir, "resource_cache").apply { if (!exists()) mkdirs() }
    
    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress = _syncProgress.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun startBackgroundSync() {
        if (_isSyncing.value) return
        scope.launch {
            _isSyncing.value = true
            try {
                syncResources()
            } catch (e: Exception) {
                LogManager.e(TAG, "Sync failed: ${e.message}")
            } finally {
                _isSyncing.value = false
                _syncProgress.value = 0f
            }
        }
    }

    private suspend fun syncResources() {
        LogManager.i(TAG, "Starting resource synchronization...")
        val baseUrl = ConfigManager.getSelectedDomain(context).removeSuffix("/")
        
        // 1. Fetch metadata
        val images = try { apiService.getImages() } catch (e: Exception) { emptyList() }
        val music = try { apiService.getMusic() } catch (e: Exception) { emptyList() }
        
        val allResources = mutableListOf<String>()
        images.forEach { 
            allResources.add(if (it.url.startsWith("http")) it.url else "$baseUrl/${it.url.removePrefix("/")}")
            it.thumbnail?.let { thumb ->
                allResources.add(if (thumb.startsWith("http")) thumb else "$baseUrl/${thumb.removePrefix("/")}")
            }
        }
        music.forEach {
            allResources.add(if (it.url.startsWith("http")) it.url else "$baseUrl/${it.url.removePrefix("/")}")
            it.cover?.let { cover ->
                allResources.add(if (cover.startsWith("http")) cover else "$baseUrl/${cover.removePrefix("/")}")
            }
        }

        val total = allResources.size
        if (total == 0) return

        LogManager.d(TAG, "Total resources to check: $total")
        
        // 2. Download concurrently with limit
        val dispatcher = Dispatchers.IO.limitedParallelism(3)
        var completed = 0
        
        allResources.chunked(10).forEach { batch ->
            coroutineScope {
                batch.map { url ->
                    launch(dispatcher) {
                        downloadIfChanged(url)
                        synchronized(this@ResourceCacheManager) {
                            completed++
                            _syncProgress.value = completed.toFloat() / total
                        }
                    }
                }.joinAll()
            }
            delay(150) // Update frequency control
        }
        
        LogManager.i(TAG, "Sync complete. All resources are up to date.")
    }

    private suspend fun downloadIfChanged(url: String) {
        val fileName = url.hashCode().toString()
        val targetFile = File(cacheDir, fileName)
        
        val requestBuilder = Request.Builder().url(url)
        
        // Incremental check: Use If-Modified-Since if file exists
        if (targetFile.exists()) {
            requestBuilder.header("If-Modified-Since", targetFile.lastModified().toString())
        }

        try {
            okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
                if (response.code == 304) {
                    LogManager.d(TAG, "Resource not changed: $url")
                    return
                }
                
                if (!response.isSuccessful) {
                    LogManager.e(TAG, "Failed to fetch $url: ${response.code}")
                    return
                }

                val body = response.body ?: return
                
                // Concurrent download control & Resumable support (simplified: overwrite if changed)
                targetFile.outputStream().use { output ->
                    body.byteStream().copyTo(output)
                }
                
                // Update last modified from server if possible
                response.header("Last-Modified")?.toLongOrNull()?.let {
                    targetFile.setLastModified(it)
                }
                
                LogManager.d(TAG, "Resource updated: $url")
            }
        } catch (e: Exception) {
            LogManager.e(TAG, "Error downloading $url: ${e.message}")
        }
    }

    fun getCachedFile(url: String): File? {
        val fileName = url.hashCode().toString()
        val file = File(cacheDir, fileName)
        return if (file.exists()) file else null
    }

    fun downloadToCache(url: String) {
        scope.launch {
            downloadIfChanged(url)
        }
    }
}
