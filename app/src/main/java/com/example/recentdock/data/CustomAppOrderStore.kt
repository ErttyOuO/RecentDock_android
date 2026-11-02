package com.example.recentdock.data

import android.content.Context
import org.json.JSONArray

/**
 * Persists the user's manual app order independently for each library category.
 *
 * Snapshot/cache refreshes are intentionally kept separate from this store so a PackageManager or
 * UsageStats refresh cannot overwrite an order the user arranged by hand.
 */
class CustomAppOrderStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasCustomOrder(category: AppCategory): Boolean = preferences.contains(key(category))

    fun getOrder(category: AppCategory): List<String> {
        val raw = preferences.getString(key(category), null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            val seen = mutableSetOf<String>()
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    val packageName = array.optString(index).trim()
                    if (packageName.isNotEmpty() && seen.add(packageName)) add(packageName)
                }
            }
        }.getOrDefault(emptyList())
    }

    fun getAll(): Map<AppCategory, List<String>> =
        AppCategory.entries.associateWith(::getOrder)

    fun setOrder(category: AppCategory, packageNames: List<String>) {
        val distinct = packageNames.filter { it.isNotBlank() }.distinct()
        val json = JSONArray().apply { distinct.forEach { put(it) } }.toString()
        preferences.edit().putString(key(category), json).apply()
    }

    fun clear(category: AppCategory) {
        preferences.edit().remove(key(category)).apply()
    }

    /**
     * Removes packages that no longer exist and appends newly discovered packages to the end.
     * The append behavior protects the user's existing manual order when background snapshots
     * refresh or a new app is installed.
     */
    fun reconcile(
        category: AppCategory,
        currentPackagesInDefaultOrder: List<String>,
    ): List<String> {
        if (!hasCustomOrder(category)) return emptyList()

        val saved = getOrder(category)
        val merged = reconcileManualOrder(saved, currentPackagesInDefaultOrder)
        if (merged != saved) setOrder(category, merged)
        return merged
    }

    private fun key(category: AppCategory): String = "order_${category.name.lowercase()}"

    companion object {
        private const val PREFS_NAME = "recent_dock_custom_app_order"
    }
}
