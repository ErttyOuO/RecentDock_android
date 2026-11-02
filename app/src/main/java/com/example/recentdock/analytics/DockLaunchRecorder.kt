package com.example.recentdock.analytics

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object DockLaunchRecorder {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun recordAsync(context: Context, packageName: String, source: String) {
        if (!AnalyticsPreferences(context).enabled) return
        val appContext = context.applicationContext
        scope.launch {
            runCatching { AnalyticsRepository(appContext).recordDockLaunch(packageName, source) }
        }
    }
}
