package com.example.recentdock.analytics

import android.content.Context

class AnalyticsPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ENABLED, value).apply()
            if (value && startedAt == 0L) startedAt = System.currentTimeMillis()
        }

    var startedAt: Long
        get() = prefs.getLong(KEY_STARTED_AT, 0L)
        private set(value) = prefs.edit().putLong(KEY_STARTED_AT, value).apply()

    fun resetCollectionWindow(now: Long = System.currentTimeMillis()) {
        prefs.edit()
            .putLong(KEY_STARTED_AT, if (enabled) now else 0L)
            .putLong(KEY_LAST_INGEST_AT, now)
            .apply()
    }

    var lastUsageIngestAt: Long
        get() = prefs.getLong(KEY_LAST_INGEST_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_INGEST_AT, value).apply()

    companion object {
        private const val PREFS_NAME = "analytics_preferences"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_STARTED_AT = "started_at"
        private const val KEY_LAST_INGEST_AT = "last_usage_ingest_at"
    }
}
