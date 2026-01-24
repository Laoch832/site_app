package com.example.helloworld.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val timestamp: String,
    val tag: String,
    val message: String,
    val level: LogLevel
)

enum class LogLevel {
    DEBUG, INFO, ERROR, WARN
}

object LogManager {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun log(level: LogLevel, tag: String, message: String) {
        val entry = LogEntry(
            timestamp = dateFormat.format(Date()),
            tag = tag,
            message = message,
            level = level
        )
        // Keep last 1000 logs
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(0, entry) // Add to top
        if (currentLogs.size > 1000) {
            currentLogs.removeAt(currentLogs.lastIndex)
        }
        _logs.value = currentLogs
    }

    fun d(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = log(LogLevel.WARN, tag, message)
    fun e(tag: String, message: String) = log(LogLevel.ERROR, tag, message)
    
    fun clear() {
        _logs.value = emptyList()
    }
}
