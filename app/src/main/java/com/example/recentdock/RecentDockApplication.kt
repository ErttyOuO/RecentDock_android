package com.example.recentdock

import android.app.Application
import android.content.pm.LauncherApps
import android.os.UserHandle
import com.example.recentdock.data.RecentAppsCache
import com.example.recentdock.analytics.AnalyticsScheduler
import com.example.recentdock.data.RecentAppsRepository
import com.example.recentdock.widget.RecentAppsWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecentDockApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var refreshJob: Job? = null

    private val launcherAppsCallback = object : LauncherApps.Callback() {
        override fun onPackageAdded(packageName: String, user: UserHandle) = refreshDataAndWidgets()
        override fun onPackageRemoved(packageName: String, user: UserHandle) = refreshDataAndWidgets()
        override fun onPackageChanged(packageName: String, user: UserHandle) = refreshDataAndWidgets()
        override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = refreshDataAndWidgets()
        override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = refreshDataAndWidgets()
    }

    override fun onCreate() {
        super.onCreate()
        getSystemService(LauncherApps::class.java)?.registerCallback(launcherAppsCallback)
        AnalyticsScheduler.applyEnabledState(this)
    }

    private fun refreshDataAndWidgets() {
        refreshJob?.cancel()
        refreshJob = applicationScope.launch {
            // Package installs/updates can emit several callbacks in quick succession.
            // Coalesce them into one scan to avoid repeated PackageManager work.
            delay(350)
            val cache = RecentAppsCache(this@RecentDockApplication)
            cache.markStale()
            runCatching { RecentAppsRepository(this@RecentDockApplication).refreshCache(cache) }
            runCatching { RecentAppsWidget().updateAll(this@RecentDockApplication) }
        }
    }
}
