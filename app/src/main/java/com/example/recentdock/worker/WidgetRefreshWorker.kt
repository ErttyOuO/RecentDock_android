package com.example.recentdock.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recentdock.data.RecentAppsCache
import com.example.recentdock.data.RecentAppsRepository
import com.example.recentdock.widget.RecentAppsWidget

class WidgetRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = runCatching {
        val cache = RecentAppsCache(applicationContext)
        RecentAppsRepository(applicationContext).refreshCache(cache)
        RecentAppsWidget().updateAll(applicationContext)
        Result.success()
    }.getOrElse {
        Result.retry()
    }
}
