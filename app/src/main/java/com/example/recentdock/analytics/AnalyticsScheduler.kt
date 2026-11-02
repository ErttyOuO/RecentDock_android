package com.example.recentdock.analytics

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object AnalyticsScheduler {
    private const val UNIQUE_WORK = "recentdock_analytics_ingest"

    fun applyEnabledState(context: Context) {
        if (AnalyticsPreferences(context).enabled) schedule(context) else cancel(context)
    }

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<AnalyticsWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            UNIQUE_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK)
    }
}
