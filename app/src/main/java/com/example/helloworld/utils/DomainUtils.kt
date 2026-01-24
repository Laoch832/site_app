package com.example.helloworld.utils

import java.util.Calendar
import java.util.Locale

object DomainUtils {
    private val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    fun getDynamicAuthDomain(): String {
        val calendar = Calendar.getInstance()
        val month = months[calendar.get(Calendar.MONTH)]
        val day = calendar.get(Calendar.DAY_OF_MONTH) // 1-31
        return "$month.$day.auth.canz-monkey.cn"
    }

    fun getLoginUrl(redirectScheme: String = "app://callback"): String {
        val domain = getDynamicAuthDomain()
        // Ensure https protocol
        return "https://$domain/login?redirect=$redirectScheme"
    }
}
