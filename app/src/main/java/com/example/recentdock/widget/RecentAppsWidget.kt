package com.example.recentdock.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.example.recentdock.MainActivity
import com.example.recentdock.R
import com.example.recentdock.analytics.AnalyticsLaunchActivity
import com.example.recentdock.analytics.AnalyticsPreferences
import com.example.recentdock.data.AppCategory
import com.example.recentdock.data.AppIconCache
import com.example.recentdock.data.HiddenAppsStore
import com.example.recentdock.data.RecentApp
import com.example.recentdock.data.RecentAppsCache
import com.example.recentdock.data.RecentAppsRepository
import com.example.recentdock.data.UsageAccess
import com.example.recentdock.data.WidgetPreferences
import com.example.recentdock.ui.RecentAppsActivity
import com.example.recentdock.ui.UsageAccessActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Native RemoteViews widget tuned for Kvaesitso.
 *
 * Widget controls use explicit PendingIntents. Category/page changes stay inside the widget, while
 * the small lower-left RecentDock icon explicitly opens MainActivity. App tiles and the
 * usage-access prompt can also open Activities. The widget now expands into an optional second
 * app row when the launcher gives it enough height.
 */
class RecentAppsWidget {
    suspend fun updateAll(context: Context) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val component = ComponentName(appContext, RecentAppsWidgetReceiver::class.java)
        manager.getAppWidgetIds(component).forEach { widgetId ->
            runCatching { updateWidgetInternal(appContext, manager, widgetId) }
        }
    }

    suspend fun update(
        context: Context,
        widgetId: Int,
        @Suppress("UNUSED_PARAMETER") resetCollectionPosition: Boolean = false,
    ) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        updateWidgetInternal(appContext, manager, widgetId)
    }

    private suspend fun updateWidgetInternal(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
    ) {
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val preferences = WidgetPreferences(context)
        val cache = RecentAppsCache(context)
        val iconCache = AppIconCache(context)
        val category = preferences.category(widgetId)
        val usageGranted = UsageAccess.hasPermission(context)

        if (!cache.hasSnapshot(category) &&
            !(category == AppCategory.RECENTLY_OPENED && !usageGranted)
        ) {
            runCatching { RecentAppsRepository(context).refreshCache(cache) }
        }

        val hiddenPackages = HiddenAppsStore(context).getHiddenPackages(category)
        val rawApps = cache.get(category)
        val apps = rawApps
            .filterNot { it.packageName in hiddenPackages }
            .take(preferences.displayCount.coerceAtMost(MAX_BROWSE_APPS))
        val layoutSpec = layoutForOptions(manager.getAppWidgetOptions(widgetId))

        val pageCount = WidgetLayoutPolicy.pageCount(apps.size, layoutSpec.visibleSlots)
        val requestedPage = preferences.page(widgetId)
        val page = WidgetLayoutPolicy.forwardOnlyPage(requestedPage, pageCount)
        if (page != requestedPage) preferences.setPage(widgetId, page)
        preferences.setPageCount(widgetId, pageCount)

        val views = RemoteViews(context.packageName, R.layout.widget_recent_dock)
        bindLeftControls(context, views, widgetId, category)
        bindApps(
            context = context,
            views = views,
            widgetId = widgetId,
            category = category,
            apps = apps,
            hadAppsBeforeHidden = rawApps.isNotEmpty(),
            hasHiddenApps = hiddenPackages.isNotEmpty(),
            iconCache = iconCache,
            layoutSpec = layoutSpec,
            page = page,
            pageCount = pageCount,
            usageGranted = usageGranted,
        )
        manager.updateAppWidget(widgetId, views)
    }

    private fun bindLeftControls(
        context: Context,
        views: RemoteViews,
        widgetId: Int,
        category: AppCategory,
    ) {
        val categorySymbol = when (category) {
            AppCategory.RECENTLY_INSTALLED -> "↓"
            AppCategory.RECENTLY_OPENED -> "↺"
            AppCategory.STARRED -> "★"
        }
        val categoryLabel = when (category) {
            AppCategory.RECENTLY_INSTALLED -> context.getString(R.string.category_recently_installed)
            AppCategory.RECENTLY_OPENED -> context.getString(R.string.category_recently_opened)
            AppCategory.STARRED -> context.getString(R.string.category_starred)
        }

        views.setTextViewText(R.id.widget_category, categorySymbol)
        views.setContentDescription(R.id.widget_category, categoryLabel)
        views.setOnClickPendingIntent(
            R.id.widget_category,
            controlBroadcastPendingIntent(
                context,
                widgetId,
                RecentAppsWidgetReceiver.COMMAND_CYCLE_CATEGORY,
            ),
        )

        views.setContentDescription(
            R.id.widget_open_app,
            context.getString(R.string.widget_open_app),
        )
        views.setOnClickPendingIntent(
            R.id.widget_open_app,
            openMainAppPendingIntent(context, widgetId),
        )
    }

    private suspend fun bindApps(
        context: Context,
        views: RemoteViews,
        widgetId: Int,
        category: AppCategory,
        apps: List<RecentApp>,
        hadAppsBeforeHidden: Boolean,
        hasHiddenApps: Boolean,
        iconCache: AppIconCache,
        layoutSpec: WidgetLayoutPolicy.LayoutSpec,
        page: Int,
        pageCount: Int,
        usageGranted: Boolean,
    ) {
        SLOT_IDS.forEach { slot -> views.setViewVisibility(slot.container, View.GONE) }
        views.setViewVisibility(R.id.widget_empty, View.GONE)
        views.setViewVisibility(R.id.widget_app_rows, View.VISIBLE)
        views.setViewVisibility(R.id.widget_app_row_1, View.VISIBLE)
        views.setViewVisibility(R.id.widget_app_row_2, View.GONE)

        if (apps.isEmpty()) {
            views.setViewVisibility(R.id.widget_app_rows, View.GONE)
            views.setViewVisibility(R.id.widget_empty, View.VISIBLE)
            when {
                category == AppCategory.RECENTLY_OPENED && !usageGranted -> {
                    views.setTextViewText(R.id.widget_empty, context.getString(R.string.usage_access_widget))
                    views.setOnClickPendingIntent(
                        R.id.widget_empty,
                        usageAccessPendingIntent(context, widgetId),
                    )
                }

                category == AppCategory.STARRED -> {
                    views.setTextViewText(R.id.widget_empty, context.getString(R.string.empty_starred))
                    views.setOnClickPendingIntent(
                        R.id.widget_empty,
                        fullListPendingIntent(context, widgetId, category),
                    )
                }

                hadAppsBeforeHidden && hasHiddenApps -> {
                    views.setTextViewText(
                        R.id.widget_empty,
                        context.getString(R.string.widget_all_apps_hidden),
                    )
                    views.setOnClickPendingIntent(
                        R.id.widget_empty,
                        fullListPendingIntent(context, widgetId, category),
                    )
                }

                else -> {
                    views.setTextViewText(R.id.widget_empty, context.getString(R.string.empty_apps))
                    views.setOnClickPendingIntent(
                        R.id.widget_empty,
                        fullListPendingIntent(context, widgetId, category),
                    )
                }
            }
            bindPageControls(context, views, widgetId, page = 0, pageCount = 1)
            return
        }

        val start = page * layoutSpec.visibleSlots
        val endExclusive = (start + layoutSpec.visibleSlots).coerceAtMost(apps.size)
        val pageApps = if (start < endExclusive) apps.subList(start, endExclusive) else emptyList()
        val activeSlots = slotsForLayout(layoutSpec)
        val hasSecondRowContent = layoutSpec.rows > 1 && pageApps.size > layoutSpec.columnsPerRow
        views.setViewVisibility(
            R.id.widget_app_row_2,
            if (hasSecondRowContent) View.VISIBLE else View.GONE,
        )

        pageApps.forEachIndexed { index, app ->
            val slot = activeSlots[index]
            views.setViewVisibility(slot.container, View.VISIBLE)
            views.setTextViewText(slot.label, app.label)
            views.setContentDescription(slot.container, app.label)

            val icon = runCatching { iconCache.load(app, ICON_SIZE_PX) }.getOrNull()
            if (icon != null) {
                views.setImageViewBitmap(slot.icon, icon)
            } else {
                views.setImageViewResource(slot.icon, R.drawable.recentdock_app_icon)
            }

            views.setOnClickPendingIntent(
                slot.container,
                appLaunchPendingIntent(context, widgetId, app),
            )
        }

        bindPageControls(context, views, widgetId, page, pageCount)
    }

    private fun bindPageControls(
        context: Context,
        views: RemoteViews,
        widgetId: Int,
        @Suppress("UNUSED_PARAMETER") page: Int,
        pageCount: Int,
    ) {
        val canAdvance = pageCount > 1
        views.setViewVisibility(
            R.id.widget_page_controls,
            if (canAdvance) View.VISIBLE else View.INVISIBLE,
        )
        views.setViewVisibility(
            R.id.widget_page_single_down,
            if (canAdvance) View.VISIBLE else View.GONE,
        )

        if (canAdvance) {
            views.setContentDescription(
                R.id.widget_page_single_down,
                context.getString(R.string.widget_next_page),
            )
            views.setOnClickPendingIntent(
                R.id.widget_page_single_down,
                controlBroadcastPendingIntent(
                    context,
                    widgetId,
                    RecentAppsWidgetReceiver.COMMAND_NEXT_PAGE,
                ),
            )
        }
    }

    private fun controlBroadcastPendingIntent(
        context: Context,
        widgetId: Int,
        command: String,
    ): PendingIntent {
        val intent = Intent(context, RecentAppsWidgetReceiver::class.java).apply {
            action = RecentAppsWidgetReceiver.ACTION_WIDGET_CONTROL
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra(RecentAppsWidgetReceiver.EXTRA_COMMAND, command)
            data = android.net.Uri.parse("recentdock://widget-broadcast/$widgetId/$command")
        }
        return PendingIntent.getBroadcast(
            context,
            stableRequestCode(widgetId, command.hashCode()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun appLaunchPendingIntent(
        context: Context,
        widgetId: Int,
        app: RecentApp,
    ): PendingIntent {
        val launchIntent = if (AnalyticsPreferences(context).enabled) {
            Intent(context, AnalyticsLaunchActivity::class.java).apply {
                putExtra(AnalyticsLaunchActivity.EXTRA_COMPONENT, app.componentName.flattenToString())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                data = android.net.Uri.parse(
                    "recentdock://tracked-launch/$widgetId/${android.net.Uri.encode(app.componentName.flattenToString())}"
                )
            }
        } else {
            Intent(Intent.ACTION_MAIN).apply {
                component = app.componentName
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
        }
        return PendingIntent.getActivity(
            context,
            stableRequestCode(widgetId, app.componentName.flattenToShortString().hashCode()),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun openMainAppPendingIntent(context: Context, widgetId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data = android.net.Uri.parse("recentdock://open-main/$widgetId")
        }
        return PendingIntent.getActivity(
            context,
            stableRequestCode(widgetId, 0x0A770A77),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun fullListPendingIntent(
        context: Context,
        widgetId: Int,
        category: AppCategory,
    ): PendingIntent {
        val intent = Intent(context, RecentAppsActivity::class.java).apply {
            putExtra(RecentAppsActivity.EXTRA_CATEGORY, category.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            stableRequestCode(widgetId, 0xA110 + category.ordinal),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun usageAccessPendingIntent(context: Context, widgetId: Int): PendingIntent {
        val intent = Intent(context, UsageAccessActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            stableRequestCode(widgetId, 0xACCE55),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun stableRequestCode(widgetId: Int, salt: Int): Int =
        ((widgetId * 1103515245L + salt.toLong()) and 0x7fffffffL).toInt()

    companion object {
        internal const val MAX_BROWSE_APPS = 15
        internal const val ICON_SIZE_PX = 128
        private const val DEFAULT_WIDGET_WIDTH_DP = 300
        private const val DEFAULT_WIDGET_HEIGHT_DP = 68

        private val FIRST_ROW_SLOTS = listOf(
            SlotIds(R.id.widget_app_1, R.id.widget_icon_1, R.id.widget_label_1),
            SlotIds(R.id.widget_app_2, R.id.widget_icon_2, R.id.widget_label_2),
            SlotIds(R.id.widget_app_3, R.id.widget_icon_3, R.id.widget_label_3),
            SlotIds(R.id.widget_app_4, R.id.widget_icon_4, R.id.widget_label_4),
            SlotIds(R.id.widget_app_5, R.id.widget_icon_5, R.id.widget_label_5),
        )
        private val SECOND_ROW_SLOTS = listOf(
            SlotIds(R.id.widget_app_6, R.id.widget_icon_6, R.id.widget_label_6),
            SlotIds(R.id.widget_app_7, R.id.widget_icon_7, R.id.widget_label_7),
            SlotIds(R.id.widget_app_8, R.id.widget_icon_8, R.id.widget_label_8),
            SlotIds(R.id.widget_app_9, R.id.widget_icon_9, R.id.widget_label_9),
            SlotIds(R.id.widget_app_10, R.id.widget_icon_10, R.id.widget_label_10),
        )
        private val SLOT_IDS = FIRST_ROW_SLOTS + SECOND_ROW_SLOTS

        private fun slotsForLayout(spec: WidgetLayoutPolicy.LayoutSpec): List<SlotIds> = buildList {
            addAll(FIRST_ROW_SLOTS.take(spec.columnsPerRow))
            if (spec.rows > 1) addAll(SECOND_ROW_SLOTS.take(spec.columnsPerRow))
        }

        internal fun layoutForOptions(options: Bundle): WidgetLayoutPolicy.LayoutSpec {
            val width = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                DEFAULT_WIDGET_WIDTH_DP,
            ).takeIf { it > 0 } ?: DEFAULT_WIDGET_WIDTH_DP
            val height = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                DEFAULT_WIDGET_HEIGHT_DP,
            ).takeIf { it > 0 } ?: DEFAULT_WIDGET_HEIGHT_DP
            return WidgetLayoutPolicy.layoutFor(width.toFloat(), height.toFloat())
        }
    }

    private data class SlotIds(
        val container: Int,
        val icon: Int,
        val label: Int,
    )
}
