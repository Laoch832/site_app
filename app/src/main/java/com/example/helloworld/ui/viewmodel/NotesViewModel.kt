package com.example.helloworld.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloworld.data.model.Note
import com.example.helloworld.domain.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    private val TAG = "NotesViewModel"

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Starting to load notes...")
            try {
                val result = repository.getNotes()
                Log.d(TAG, "Loaded ${result.size} notes successfully")
                _notes.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error loading notes: ${e.message}", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
