package com.example.recentdock.data

import android.content.Context

class WidgetPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var displayCount: Int
        get() = preferences.getInt(KEY_DISPLAY_COUNT, DEFAULT_DISPLAY_COUNT)
            .coerceIn(MIN_DISPLAY_COUNT, MAX_DISPLAY_COUNT)
        set(value) {
            preferences.edit()
                .putInt(KEY_DISPLAY_COUNT, value.coerceIn(MIN_DISPLAY_COUNT, MAX_DISPLAY_COUNT))
                .apply()
        }

    fun category(widgetId: Int): AppCategory =
        AppCategory.fromStored(preferences.getString(categoryKey(widgetId), null))

    fun setCategory(widgetId: Int, category: AppCategory) {
        preferences.edit()
            .putString(categoryKey(widgetId), category.name)
            .putInt(pageKey(widgetId), 0)
            .apply()
    }

    fun cycleCategory(widgetId: Int): AppCategory {
        val next = category(widgetId).nextWidgetCategory()
        setCategory(widgetId, next)
        return next
    }

    fun page(widgetId: Int): Int = preferences.getInt(pageKey(widgetId), 0).coerceAtLeast(0)

    fun setPage(widgetId: Int, page: Int) {
        preferences.edit().putInt(pageKey(widgetId), page.coerceAtLeast(0)).apply()
    }

    fun pageCount(widgetId: Int): Int = preferences.getInt(pageCountKey(widgetId), 1).coerceAtLeast(1)

    fun setPageCount(widgetId: Int, pageCount: Int) {
        preferences.edit().putInt(pageCountKey(widgetId), pageCount.coerceAtLeast(1)).apply()
    }

    fun clearWidgetState(widgetId: Int) {
        preferences.edit()
            .remove(categoryKey(widgetId))
            .remove(pageKey(widgetId))
            .remove(pageCountKey(widgetId))
            .remove(legacyLastPrimaryKey(widgetId))
            .apply()
    }

    companion object {
        const val MIN_DISPLAY_COUNT = 5
        const val MAX_DISPLAY_COUNT = 15
        const val DEFAULT_DISPLAY_COUNT = 15

        private const val PREFS_NAME = "recent_dock_settings"
        private const val KEY_DISPLAY_COUNT = "display_count_v2"

        private fun categoryKey(widgetId: Int) = "widget_${widgetId}_category"
        private fun pageKey(widgetId: Int) = "widget_${widgetId}_page"
        private fun pageCountKey(widgetId: Int) = "widget_${widgetId}_page_count"
        private fun legacyLastPrimaryKey(widgetId: Int) = "widget_${widgetId}_last_primary"
    }
}
