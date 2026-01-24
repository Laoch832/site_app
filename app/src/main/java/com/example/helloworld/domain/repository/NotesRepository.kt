package com.example.helloworld.domain.repository

import com.example.helloworld.data.model.Note

interface NotesRepository {
    suspend fun getNotes(): List<Note>
    suspend fun createNote(title: String, content: String)
    suspend fun deleteNote(id: Int)
}
