package com.quickqr.scanner.util

import android.content.Context
import org.json.JSONArray

/**
 * Tiny SharedPreferences-backed recent-scan list (max 20).
 */
class ScanHistoryStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun add(raw: String) {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return
        val current = getAll().toMutableList()
        current.removeAll { it == trimmed }
        current.add(0, trimmed)
        while (current.size > MAX) current.removeAt(current.lastIndex)
        prefs.edit().putString(KEY, JSONArray(current).toString()).apply()
    }

    fun getAll(): List<String> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    add(arr.getString(i))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val PREFS = "quick_qr_history"
        private const val KEY = "items"
        private const val MAX = 20
    }
}
