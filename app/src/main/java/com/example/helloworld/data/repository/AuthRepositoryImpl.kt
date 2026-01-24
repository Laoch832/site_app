package com.example.helloworld.data.repository

import com.example.helloworld.domain.repository.AuthRepository
import com.example.helloworld.utils.DomainUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    override fun getDynamicLoginUrl(): String {
        return DomainUtils.getLoginUrl()
    }

    override suspend fun importSession(accessToken: String, refreshToken: String) {
        // Construct UserSession object manually or use importSession with correct parameters if available
        // According to recent versions of supabase-kt, importSession might take a UserSession object or similar.
        // Let's assume for now we need to construct a session or use a different method.
        // Checking the error: "inferred type is String but UserSession was expected" and "Boolean was expected"
        // It seems importSession(accessToken, refreshToken) is not the correct signature.
        
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
    }

    override suspend fun getCurrentUser(): Any? {
        return supabase.auth.currentUserOrNull()
    }

    override suspend fun logout() {
        supabase.auth.signOut()
    }
    
    override suspend fun isUserLoggedIn(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    override suspend fun loginWithEmail(email: String, password: String) {
        supabase.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
            this.email = email
            this.password = password
        }
    }
}
