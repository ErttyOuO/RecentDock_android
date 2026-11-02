package com.example.recentdock.analytics

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalyticsWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!AnalyticsPreferences(applicationContext).enabled) return@withContext Result.success()
        runCatching { UsageEventsIngestor(applicationContext).ingestNow() }
            .fold(onSuccess = { Result.success() }, onFailure = { Result.retry() })
    }
}
