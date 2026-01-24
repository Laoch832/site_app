package com.example.helloworld.data.repository

import android.util.Log
import com.example.helloworld.data.model.Note
import com.example.helloworld.domain.repository.NotesRepository
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
        return try {
            supabase.postgrest.from("notes")
                .select {
                    filter {
                        eq("is_published", true)
                    }
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<Note>()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch notes from Supabase: ${e.message}. Using dummy data.")
            // Fallback to dummy data
            getDummyNotes()
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
        } catch (e: Exception) {
             Log.e(TAG, "Failed to create note: ${e.message}")
             // In a real app, we might save to local DB (Room) for sync later
             throw e
        }
    }

    override suspend fun deleteNote(id: Int) {
        try {
            supabase.postgrest.from("notes").delete {
                filter {
                    eq("id", id)
                }
            }
        } catch (e: Exception) {
             Log.e(TAG, "Failed to delete note: ${e.message}")
             throw e
        }
    }

    private fun getDummyNotes(): List<Note> {
        return listOf(
            Note(1, "初遇", "今天在图书馆看见了你，阳光洒在你身上...", "2023-05-20", true),
            Note(2, "心动", "你笑起来真好看，像春天的花...", "2023-05-21", true),
            Note(3, "暗恋", "默默地关注你，不敢打扰...", "2023-05-25", true),
            Note(4, "错在", "今天下雨了，没有带伞，就像我的心情...", "2023-06-01", true),
            Note(5, "Demo Note", "This is a demo note shown because Supabase is not configured.", "2026-01-17", true)
        )
    }
}
