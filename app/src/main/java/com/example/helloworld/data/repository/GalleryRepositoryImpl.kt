package com.example.helloworld.data.repository

import android.util.Log
import com.example.helloworld.data.model.FeaturedImage
import com.example.helloworld.data.model.ImageItem
import com.example.helloworld.data.network.ApiService
import com.example.helloworld.domain.repository.GalleryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject

class GalleryRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val supabase: SupabaseClient
) : GalleryRepository {

    private val TAG = "GalleryRepository"

    override suspend fun getImages(): List<ImageItem> {
        try {
            // 1. Fetch all images from JSON API
            val images = apiService.getImages()
            Log.d(TAG, "Fetched ${images.size} images from JSON API")
            
            // 2. Fetch featured images from Supabase
            // Catch errors to ensure gallery still loads even if Supabase fails
            val featuredUrls = try {
                supabase.postgrest.from("featured_images")
                    .select()
                    .decodeList<FeaturedImage>()
                    .map { it.url }
                    .toSet()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch featured images: ${e.message}")
                emptySet<String>()
            }

            // 3. Merge data
            return images.map { image ->
                image.copy(isFeatured = featuredUrls.contains(image.url))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching images: ${e.message}", e)
            throw e
        }
    }

    override suspend fun toggleFeatured(url: String, isFeatured: Boolean) {
        if (isFeatured) {
            // Add to featured
            supabase.postgrest.from("featured_images").insert(FeaturedImage(url))
        } else {
            // Remove from featured
            supabase.postgrest.from("featured_images").delete {
                filter {
                    eq("url", url)
                }
            }
        }
    }
}
