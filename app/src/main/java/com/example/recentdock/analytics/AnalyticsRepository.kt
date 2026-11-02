package com.example.recentdock.analytics

import android.content.Context
import com.example.recentdock.data.AppCategory
import com.example.recentdock.data.RecentAppsCache

data class AnalyticsRow(val packageName: String, val label: String, val count: Int)

data class AnalyticsTimelinePoint(
    val range: TimeRange,
    val usageCount: Int,
    val dockCount: Int,
)

data class AnalyticsSnapshot(
    val range: TimeRange,
    val usageRanking: List<AnalyticsRow>,
    val dockRanking: List<AnalyticsRow>,
    val installedInPeriod: Int,
    val totalLaunchableApps: Int,
    val startedAt: Long,
)

class AnalyticsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val database = AnalyticsDatabase.getInstance(appContext)
    private val preferences = AnalyticsPreferences(appContext)
    private val cache = RecentAppsCache(appContext)

    fun snapshot(period: AnalyticsPeriod, now: Long = System.currentTimeMillis()): AnalyticsSnapshot =
        snapshot(AnalyticsPeriods.range(period, now))

    fun snapshot(range: TimeRange): AnalyticsSnapshot {
        val installed = cache.get(AppCategory.RECENTLY_INSTALLED)
        val labels = installed.associate { it.packageName to it.label }
        fun map(rows: List<PackageCount>) = rows.map {
            AnalyticsRow(it.packageName, labels[it.packageName] ?: it.packageName, it.count)
        }
        return AnalyticsSnapshot(
            range = range,
            usageRanking = map(database.usageRanking(range.startInclusive, range.endExclusive)),
            dockRanking = map(database.dockRanking(range.startInclusive, range.endExclusive)),
            installedInPeriod = installed.count { it.firstInstallTime in range.startInclusive until range.endExclusive },
            totalLaunchableApps = installed.size,
            startedAt = preferences.startedAt,
        )
    }

    fun timeline(period: AnalyticsPeriod, now: Long = System.currentTimeMillis()): List<AnalyticsTimelinePoint> {
        val pointCount = when (period) {
            AnalyticsPeriod.DAY -> 14
            AnalyticsPeriod.WEEK -> 12
            AnalyticsPeriod.MONTH -> 12
        }
        return AnalyticsPeriods.history(period, now, pointCount).map { range ->
            AnalyticsTimelinePoint(
                range = range,
                usageCount = database.usageCount(range.startInclusive, range.endExclusive),
                dockCount = database.dockCount(range.startInclusive, range.endExclusive),
            )
        }
    }

    fun clearAnalyticsData() {
        database.clearAllEvents()
        preferences.resetCollectionWindow()
    }

    fun dockLaunchCount(packageName: String): Int = database.dockLaunchCountForPackage(packageName)

    fun recordDockLaunch(packageName: String, source: String) {
        if (!preferences.enabled) return
        database.insertDockLaunch(packageName, System.currentTimeMillis(), source)
    }
}
