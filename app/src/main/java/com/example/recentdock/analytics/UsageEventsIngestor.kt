package com.example.recentdock.analytics

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.recentdock.data.UsageAccess
import kotlin.math.max
import java.util.concurrent.TimeUnit

class UsageEventsIngestor(private val context: Context) {
    private val appContext = context.applicationContext
    private val preferences = AnalyticsPreferences(appContext)
    private val database = AnalyticsDatabase.getInstance(appContext)

    fun ingestNow(now: Long = System.currentTimeMillis()) {
        if (!preferences.enabled || !UsageAccess.hasPermission(appContext)) return

        val startedAt = preferences.startedAt.takeIf { it > 0L } ?: now
        val overlapStart = preferences.lastUsageIngestAt.takeIf { it > 0L }?.minus(120_000L) ?: startedAt
        val start = max(startedAt, overlapStart)
        if (start >= now) {
            preferences.lastUsageIngestAt = now
            return
        }

        val manager = appContext.getSystemService(UsageStatsManager::class.java) ?: return
        val events = manager.queryEvents(start, now)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val launchEvent = event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
            if (launchEvent && event.packageName != null && event.timeStamp >= startedAt) {
                database.insertUsageLaunch(event.packageName, event.timeStamp)
            }
        }
        // The longest chart is 12 months. Keep a margin for calendar-month boundaries.
        database.pruneEventsBefore(now - TimeUnit.DAYS.toMillis(400))
        preferences.lastUsageIngestAt = now
    }
}
