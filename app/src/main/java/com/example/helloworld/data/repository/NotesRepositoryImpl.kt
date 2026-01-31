package com.example.helloworld.data.repository

import com.example.helloworld.data.model.Note
import com.example.helloworld.domain.repository.NotesRepository
import com.example.helloworld.utils.LogManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : NotesRepository {

    private val TAG = "NotesRepository"

    override suspend fun getNotes(): List<Note> {
        LogManager.i(TAG, "Fetching notes from Supabase...")
        return try {
            val notes = supabase.postgrest.from("notes")
                .select {
                    filter {
                        eq("is_published", true)
                    }
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<Note>()
            LogManager.i(TAG, "Successfully fetched ${notes.size} notes from Supabase")
            notes
        } catch (e: Exception) {
            LogManager.e(TAG, "Critical error fetching notes: ${e.message}")
            if (e.message?.contains("Serializer") == true) {
                LogManager.e(TAG, "Serialization error detected! Check if Note is marked @Serializable and plugin is active.")
            }
            emptyList()
        }
    }

    override suspend fun createNote(title: String, content: String) {
        try {
            val user = supabase.auth.currentUserOrNull() ?: throw IllegalStateException("User not logged in")
            val note = Note(
                title = title,
                content = content,
                is_published = true,
                user_id = user.id
            )
            supabase.postgrest.from("notes").insert(note)
            LogManager.i(TAG, "Note created successfully")
        } catch (e: Exception) {
             LogManager.e(TAG, "Failed to create note: ${e.message}")
             throw e
        }
    }

    override suspend fun deleteNote(id: String) {
        try {
            supabase.postgrest.from("notes").delete {
                filter {
                    eq("id", id)
                }
            }
            LogManager.i(TAG, "Note deleted successfully")
        } catch (e: Exception) {
             LogManager.e(TAG, "Failed to delete note: ${e.message}")
             throw e
        }
    }
}
