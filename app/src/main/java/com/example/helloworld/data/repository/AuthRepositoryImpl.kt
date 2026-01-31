package com.example.helloworld.data.repository

import com.example.helloworld.domain.repository.AuthRepository
import com.example.helloworld.utils.DomainUtils
import com.example.helloworld.utils.LogManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Github
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
        LogManager.d(TAG, "getCurrentUser: ID=${user?.id}, Email=${user?.email}")
        return user
    }

    override suspend fun logout() {
        LogManager.i(TAG, "Attempting to logout...")
        try {
            supabase.auth.signOut()
            LogManager.i(TAG, "User logged out successfully")
        } catch (e: Exception) {
            LogManager.e(TAG, "Logout operation failed: ${e.message}")
            throw e
        }
    }
    
    override suspend fun isUserLoggedIn(): Boolean {
        val session = supabase.auth.currentSessionOrNull()
        val loggedIn = session != null
        LogManager.d(TAG, "Checking login status: $loggedIn (Session=${if(loggedIn) "Valid" else "None"})")
        return loggedIn
    }

    override suspend fun loginWithEmail(email: String, password: String) {
        LogManager.i(TAG, "Attempting login for email: $email")
        try {
            supabase.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                this.email = email
                this.password = password
            }
            val user = supabase.auth.currentUserOrNull()
            LogManager.i(TAG, "Login successful! User ID: ${user?.id}")
        } catch (e: Exception) {
            LogManager.e(TAG, "Authentication failed for $email: ${e.message}")
            throw e
        }
    }

    override suspend fun loginWithGitHub() {
        try {
            supabase.auth.signInWith(Github)
            LogManager.i(TAG, "GitHub login initiated")
        } catch (e: Exception) {
            LogManager.e(TAG, "GitHub login failed: ${e.message}")
            throw e
        }
    }
}
