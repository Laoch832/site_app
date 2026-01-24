package com.example.helloworld

import android.content.Context
import android.content.SharedPreferences

object ConfigManager {
    private const val PREF_NAME = "app_config"
    private const val KEY_SELECTED_DOMAIN_INDEX = "selected_domain_index"

    val DOMAINS = listOf(
        "https://mirror2-love.canz-monkey.cn", // Priority (Mirror)
        "https://love.canz-monkey.cn"          // Original
    )

    fun getSelectedDomain(context: Context): String {
        val prefs = getPrefs(context)
        val index = prefs.getInt(KEY_SELECTED_DOMAIN_INDEX, 0)
        return DOMAINS.getOrElse(index) { DOMAINS[0] }
    }

    fun getSelectedDomainIndex(context: Context): Int {
        return getPrefs(context).getInt(KEY_SELECTED_DOMAIN_INDEX, 0)
    }

    fun setDomainIndex(context: Context, index: Int) {
        if (index in DOMAINS.indices) {
            getPrefs(context).edit().putInt(KEY_SELECTED_DOMAIN_INDEX, index).apply()
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}
