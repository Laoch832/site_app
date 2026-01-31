package com.example.helloworld.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.example.helloworld.ConfigManager
import com.example.helloworld.data.network.ApiService
import com.example.helloworld.utils.LogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "HomeViewModel"

    private val _quote = MutableStateFlow("朝夕藏念，静待花开")
    val quote: StateFlow<String> = _quote.asStateFlow()

    private val _author = MutableStateFlow("站点")
    val author: StateFlow<String> = _author.asStateFlow()

    private val _backgroundImageUrl = MutableStateFlow("")
    val backgroundImageUrl: StateFlow<String> = _backgroundImageUrl.asStateFlow()

    init {
        LogManager.i(TAG, "Initializing HomeViewModel")
        fetchQuote()
        refreshBackground()
    }

    fun fetchQuote() {
        viewModelScope.launch {
            LogManager.d(TAG, "Fetching quote from server...")
            try {
                val quotes = apiService.getQuotes()
                if (quotes.isNotEmpty()) {
                    val randomQuote = quotes.random()
                    _quote.value = randomQuote.hitokoto
                    _author.value = randomQuote.from
                    LogManager.i(TAG, "Quote updated: ${randomQuote.hitokoto}")
                }
            } catch (e: Exception) {
                LogManager.e(TAG, "Failed to fetch quote: ${e.message}")
            }
        }
    }

    fun refreshBackground() {
        viewModelScope.launch {
            LogManager.d(TAG, "Refreshing background image...")
            try {
                val images = apiService.getImages()
                if (images.isNotEmpty()) {
                    val randomImage = images.random()
                    val baseUrl = ConfigManager.getSelectedDomain(context)
                    val imageUrl = if (randomImage.url.startsWith("http")) {
                        randomImage.url
                    } else {
                        "${baseUrl.removeSuffix("/")}/${randomImage.url.removePrefix("/")}"
                    }
                    _backgroundImageUrl.value = imageUrl
                    LogManager.i(TAG, "Background updated: $imageUrl")
                }
            } catch (e: Exception) {
                LogManager.e(TAG, "Failed to refresh background: ${e.message}")
            }
        }
    }
}
