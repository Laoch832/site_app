package com.example.helloworld.data.network

import android.content.Context
import com.example.helloworld.ConfigManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    // Supabase Configuration
    const val SUPABASE_URL = "https://dsqvrjgrwkwaqdybxqgy.supabase.co"
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRzcXZyamdyd2t3YXFkeWJ4cWd5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjgwODk5NTYsImV4cCI6MjA4MzY2NTk1Nn0.bjWmKkloP7G9PYm2gwpGw_553oV-hOzmTWNpPgoPuh8"

    // Retrofit Configuration for Static JSONs
    fun provideRetrofit(context: Context): Retrofit {
        val baseUrl = ConfigManager.getSelectedDomain(context)
        // Ensure base URL ends with /
        val validBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        return Retrofit.Builder()
        .baseUrl(validBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    }
}
