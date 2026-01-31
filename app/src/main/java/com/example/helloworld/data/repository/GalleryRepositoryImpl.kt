package com.example.helloworld.data.repository

import android.content.Context
import com.example.helloworld.ConfigManager
import com.example.helloworld.data.model.FeaturedImage
import com.example.helloworld.data.model.ImageItem
import com.example.helloworld.data.network.ApiService
import com.example.helloworld.domain.repository.GalleryRepository
import com.example.helloworld.utils.LogManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject

import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.gotrue.auth

class GalleryRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val supabase: SupabaseClient,
    @ApplicationContext private val context: Context
) : GalleryRepository {

    private val TAG = "GalleryRepository"
    private val BUCKET_NAME = "cloudflare_photoREPO_zrl"

    override suspend fun getImages(): List<ImageItem> {
        LogManager.i(TAG, "Fetching all images from JSON API and featured status from Supabase...")
        try {
            // 1. Fetch all images from JSON API
            val images = apiService.getImages()
            LogManager.d(TAG, "Successfully fetched ${images.size} images from JSON API")
            
            // 2. Fetch featured images from Supabase
            val featuredUrls = try {
                LogManager.d(TAG, "Fetching featured images from 'featured_images' table...")
                val features = supabase.postgrest.from("featured_images")
                    .select()
                    .decodeList<FeaturedImage>()
                LogManager.d(TAG, "Found ${features.size} featured images in Supabase")
                features.map { it.url }.toSet()
            } catch (e: Exception) {
                LogManager.e(TAG, "Failed to fetch featured images from Supabase: ${e.message}")
                emptySet<String>()
            }

            // 3. Merge data and fix URLs
            val baseUrl = ConfigManager.getSelectedDomain(context).removeSuffix("/")
            val merged = images.map { image ->
                val isFeatured = featuredUrls.contains(image.url)
                
                // Ensure URLs are absolute
                val absoluteUrl = if (image.url.startsWith("http")) image.url 
                                 else "$baseUrl/${image.url.removePrefix("/")}"
                val absoluteThumbnail = if (image.thumbnail == null) null 
                                       else if (image.thumbnail.startsWith("http")) image.thumbnail 
                                       else "$baseUrl/${image.thumbnail.removePrefix("/")}"

                if (isFeatured) LogManager.d(TAG, "Image marked as featured: ${image.name}")
                
                image.copy(
                    url = absoluteUrl,
                    thumbnail = absoluteThumbnail,
                    isFeatured = isFeatured
                )
            }
            LogManager.i(TAG, "Gallery data merge complete. Total: ${merged.size}")
            return merged
        } catch (e: Exception) {
            LogManager.e(TAG, "Critical error fetching gallery images: ${e.message}")
            throw e
        }
    }

    override suspend fun toggleFeatured(url: String, isFeatured: Boolean) {
        try {
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
            LogManager.i(TAG, "Toggled featured status for $url to $isFeatured")
        } catch (e: Exception) {
            LogManager.e(TAG, "Failed to toggle featured: ${e.message}")
            throw e
        }
    }

    override suspend fun uploadImage(byteArray: ByteArray, fileName: String): String {
        try {
            val user = supabase.auth.currentUserOrNull() ?: throw IllegalStateException("User not logged in")
            val filePath = "${user.id}/$fileName"
            val bucket = supabase.storage.from(BUCKET_NAME)
            bucket.upload(filePath, byteArray)
            val publicUrl = bucket.publicUrl(filePath)
            LogManager.i(TAG, "Image uploaded successfully: $publicUrl")
            return publicUrl
        } catch (e: Exception) {
            LogManager.e(TAG, "Failed to upload image: ${e.message}")
            throw e
        }
    }
}
