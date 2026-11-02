package com.example.recentdock.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.recentdock.data.WidgetPreferences
import com.example.recentdock.worker.WidgetRefreshScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Native AppWidgetProvider. No Glance SessionWorker and no control Activity are involved. */
class RecentAppsWidgetReceiver : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_WIDGET_CONTROL) {
            handleWidgetControl(context, intent)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        renderAsync(context, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        renderAsync(context, intArrayOf(appWidgetId))
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefreshScheduler.schedule(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val preferences = WidgetPreferences(context)
        appWidgetIds.forEach(preferences::clearWidgetState)
        super.onDeleted(context, appWidgetIds)
    }

    override fun onDisabled(context: Context) {
        WidgetRefreshScheduler.cancel(context)
        super.onDisabled(context)
    }

    private fun handleWidgetControl(context: Context, intent: Intent) {
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )
        val command = intent.getStringExtra(EXTRA_COMMAND)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID || command.isNullOrBlank()) {
            Log.w(TAG, "ignored invalid broadcast widgetId=$widgetId command=$command")
            return
        }

        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, RecentAppsWidgetReceiver::class.java)
        if (widgetId !in manager.getAppWidgetIds(component)) {
            Log.w(TAG, "ignored control for unknown widgetId=$widgetId command=$command")
            return
        }

        // Mutate the tiny preference state immediately on the receiver thread so rapid repeated
        // taps are deterministic. Rendering itself remains asynchronous.
        val preferences = WidgetPreferences(context.applicationContext)
        when (command) {
            COMMAND_CYCLE_CATEGORY -> {
                val before = preferences.category(widgetId)
                val after = preferences.cycleCategory(widgetId)
                Log.d(TAG, "control=$command widgetId=$widgetId category=$before->$after")
            }


            COMMAND_NEXT_PAGE -> {
                val before = preferences.page(widgetId)
                val pageCount = preferences.pageCount(widgetId)
                val after = WidgetLayoutPolicy.nextForwardPage(before, pageCount)
                preferences.setPage(widgetId, after)
                Log.d(TAG, "control=$command widgetId=$widgetId page=$before->$after pageCount=$pageCount")
            }

            else -> {
                Log.w(TAG, "ignored unknown widget command=$command")
                return
            }
        }

        renderAsync(context, intArrayOf(widgetId))
    }

    private fun renderAsync(context: Context, ids: IntArray) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        renderScope.launch {
            try {
                renderMutex.withLock {
                    val widget = RecentAppsWidget()
                    ids.forEach { id ->
                        runCatching { widget.update(appContext, id) }
                            .onFailure { Log.e(TAG, "widget redraw failed id=$id", it) }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "RecentDockWidgetControl"
        private val renderScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val renderMutex = Mutex()

        const val ACTION_WIDGET_CONTROL = "com.example.recentdock.action.WIDGET_CONTROL"
        const val EXTRA_COMMAND = "com.example.recentdock.extra.WIDGET_COMMAND"

        const val COMMAND_CYCLE_CATEGORY = "cycle_category"
        const val COMMAND_NEXT_PAGE = "next_page"
    }
}
