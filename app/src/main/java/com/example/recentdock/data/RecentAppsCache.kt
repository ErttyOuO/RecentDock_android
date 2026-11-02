package com.example.recentdock.data

import android.content.ComponentName
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Small persistent snapshot cache used by both the app and the widget.
 *
 * The UI reads this first (stale-while-revalidate) and a background scan replaces
 * it only when the package/usage snapshot actually changes.
 */
class RecentAppsCache(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val updates: StateFlow<Long> = MemoryCache.revision.asStateFlow()

    fun get(category: AppCategory): List<RecentApp> {
        MemoryCache.snapshots[category]?.let { return it }
        val decoded = decode(preferences.getString(key(category), null))
        MemoryCache.snapshots[category] = decoded
        return decoded
    }

    fun getAll(): Map<AppCategory, List<RecentApp>> =
        AppCategory.entries.associateWith(::get)

    fun hasSnapshot(category: AppCategory): Boolean =
        preferences.contains(key(category))

    fun isInitialized(): Boolean = AppCategory.entries.all(::hasSnapshot)

    fun usageAccessGrantedAtLastRefresh(): Boolean =
        preferences.getBoolean(KEY_USAGE_ACCESS_GRANTED, false)

    fun put(category: AppCategory, apps: List<RecentApp>): Boolean {
        val normalized = apps.toList()
        val old = get(category)
        if (old == normalized) return false
        MemoryCache.snapshots[category] = normalized
        preferences.edit().putString(key(category), encode(normalized)).apply()
        MemoryCache.revision.value = MemoryCache.revision.value + 1L
        return true
    }

    fun putAll(
        snapshots: Map<AppCategory, List<RecentApp>>,
        usageAccessGranted: Boolean,
    ): Boolean {
        var changed = false
        val editor = preferences.edit()

        snapshots.forEach { (category, apps) ->
            val normalized = apps.toList()
            val old = get(category)
            if (old != normalized) {
                changed = true
                MemoryCache.snapshots[category] = normalized
                editor.putString(key(category), encode(normalized))
            }
        }

        editor
            .putLong(KEY_LAST_REFRESH, System.currentTimeMillis())
            .putBoolean(KEY_USAGE_ACCESS_GRANTED, usageAccessGranted)
            .apply()

        if (changed) {
            MemoryCache.revision.value = MemoryCache.revision.value + 1L
        }
        return changed
    }

    fun rebuildStarred(starredPackages: Set<String>): List<RecentApp> {
        val source = get(AppCategory.RECENTLY_INSTALLED)
        val starred = source.filter { it.packageName in starredPackages }
            .sortedWith(
                compareByDescending<RecentApp> { it.firstInstallTime }
                    .thenBy { it.label.lowercase() }
            )
        put(AppCategory.STARRED, starred)
        return starred
    }

    fun isStale(maxAgeMillis: Long): Boolean {
        val last = preferences.getLong(KEY_LAST_REFRESH, 0L)
        return last == 0L || System.currentTimeMillis() - last >= maxAgeMillis
    }

    fun markStale() {
        preferences.edit().putLong(KEY_LAST_REFRESH, 0L).apply()
    }

    private fun encode(apps: List<RecentApp>): String {
        val array = JSONArray()
        apps.forEach { app ->
            array.put(
                JSONObject()
                    .put("package", app.packageName)
                    .put("label", app.label)
                    .put("class", app.componentName.className)
                    .put("installed", app.firstInstallTime)
                    .put("updated", app.lastUpdateTime)
                    .put("used", app.lastUsedTime)
            )
        }
        return array.toString()
    }

    private fun decode(raw: String?): List<RecentApp> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val packageName = item.getString("package")
                    add(
                        RecentApp(
                            packageName = packageName,
                            label = item.optString("label", packageName.substringAfterLast('.')),
                            componentName = ComponentName(
                                packageName,
                                item.getString("class"),
                            ),
                            firstInstallTime = item.optLong("installed", 0L),
                            lastUpdateTime = item.optLong("updated", 0L),
                            lastUsedTime = item.optLong("used", 0L),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun key(category: AppCategory) = "snapshot_${category.name.lowercase()}"

    private object MemoryCache {
        val snapshots = ConcurrentHashMap<AppCategory, List<RecentApp>>()
        val revision = MutableStateFlow(0L)
    }

    companion object {
        private const val PREFS_NAME = "recent_dock_snapshot_cache_v1"
        private const val KEY_LAST_REFRESH = "last_refresh"
        private const val KEY_USAGE_ACCESS_GRANTED = "usage_access_granted"
    }
}
