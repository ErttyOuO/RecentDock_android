package com.example.recentdock.data

import android.content.Context

/**
 * Stores apps the user intentionally hides from a specific RecentDock category.
 *
 * Hidden state is category-scoped: hiding an app from Recently installed does not silently hide it
 * from Recently opened, and vice versa. Starred does not support hidden items by design.
 */
class HiddenAppsStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getHiddenPackages(category: AppCategory): Set<String> {
        if (!category.supportsHiddenApps()) return emptySet()
        return preferences.getStringSet(key(category), emptySet()).orEmpty().toSet()
    }

    fun getAll(): Map<AppCategory, Set<String>> =
        AppCategory.entries.associateWith(::getHiddenPackages)

    fun hide(category: AppCategory, packageName: String) {
        if (!category.supportsHiddenApps() || packageName.isBlank()) return
        val updated = getHiddenPackages(category).toMutableSet().apply { add(packageName) }
        preferences.edit().putStringSet(key(category), updated).apply()
    }

    fun unhide(category: AppCategory, packageName: String) {
        if (!category.supportsHiddenApps()) return
        val updated = getHiddenPackages(category).toMutableSet().apply { remove(packageName) }
        preferences.edit().putStringSet(key(category), updated).apply()
    }

    fun clear(category: AppCategory) {
        if (!category.supportsHiddenApps()) return
        preferences.edit().remove(key(category)).apply()
    }

    /** Removes hidden package names that are no longer installed/launchable. */
    fun retainInstalled(installedPackages: Set<String>) {
        supportedCategories.forEach { category ->
            val current = getHiddenPackages(category)
            val cleaned = current.intersect(installedPackages)
            if (cleaned != current) {
                preferences.edit().putStringSet(key(category), cleaned).apply()
            }
        }
    }

    private fun key(category: AppCategory): String = "hidden_${category.name.lowercase()}"

    companion object {
        private const val PREFS_NAME = "recent_dock_hidden_apps"
        private val supportedCategories = listOf(
            AppCategory.RECENTLY_INSTALLED,
            AppCategory.RECENTLY_OPENED,
        )
    }
}

fun AppCategory.supportsHiddenApps(): Boolean =
    this == AppCategory.RECENTLY_INSTALLED || this == AppCategory.RECENTLY_OPENED
