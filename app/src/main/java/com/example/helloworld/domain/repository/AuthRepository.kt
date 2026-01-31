package com.example.helloworld.domain.repository

interface AuthRepository {
    fun getDynamicLoginUrl(): String
    suspend fun importSession(accessToken: String, refreshToken: String)
    suspend fun getCurrentUser(): Any? // Replace Any? with actual User type if available
    suspend fun logout()
    suspend fun isUserLoggedIn(): Boolean
    suspend fun loginWithEmail(email: String, password: String)
    suspend fun loginWithGitHub()
}
