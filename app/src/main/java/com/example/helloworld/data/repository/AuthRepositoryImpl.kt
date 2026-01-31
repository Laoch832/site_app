package com.example.helloworld.data.repository

import com.example.helloworld.domain.repository.AuthRepository
import com.example.helloworld.utils.DomainUtils
import com.example.helloworld.utils.LogManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    private val TAG = "AuthRepository"

    override fun getDynamicLoginUrl(): String {
        return DomainUtils.getLoginUrl()
    }

    override suspend fun importSession(accessToken: String, refreshToken: String) {
        try {
            // Correct approach for supabase-kt:
            supabase.auth.importSession(
                UserSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = 3600, // Default or parsed
                    tokenType = "bearer",
                    user = null // User object might be optional or fetched later
                )
            )
            LogManager.i(TAG, "Session imported successfully")
        } catch (e: Exception) {
            LogManager.e(TAG, "Failed to import session: ${e.message}")
            throw e
        }
    }

    override suspend fun getCurrentUser(): Any? {
        val user = supabase.auth.currentUserOrNull()
        LogManager.d(TAG, "getCurrentUser: ${user?.id}")
        return user
    }

    override suspend fun logout() {
        try {
            supabase.auth.signOut()
            LogManager.i(TAG, "Logged out successfully")
        } catch (e: Exception) {
            LogManager.e(TAG, "Logout failed: ${e.message}")
            throw e
        }
    }
    
    override suspend fun isUserLoggedIn(): Boolean {
        val session = supabase.auth.currentSessionOrNull()
        val loggedIn = session != null
        LogManager.d(TAG, "isUserLoggedIn: $loggedIn")
        return loggedIn
    }

    override suspend fun loginWithEmail(email: String, password: String) {
        try {
            supabase.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                this.email = email
                this.password = password
            }
            LogManager.i(TAG, "Login successful for $email")
        } catch (e: Exception) {
            LogManager.e(TAG, "Login failed for $email: ${e.message}")
            throw e
        }
    }
}
