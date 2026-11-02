package com.example.recentdock.data

import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import java.util.concurrent.TimeUnit

class RecentAppsRepository(
    private val context: Context,
    private val useIconCache: Boolean = true,
) {
    private val packageManager: PackageManager = context.packageManager
    private val iconCache by lazy { AppIconCache(context) }

    private fun getLaunchableApps(): List<RecentApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        @Suppress("DEPRECATION")
        val launchableActivities = packageManager.queryIntentActivities(launcherIntent, 0)

        return launchableActivities
            .asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                runCatching {
                    @Suppress("DEPRECATION")
                    val packageInfo = packageManager.getPackageInfo(packageName, 0)
                    RecentApp(
                        packageName = packageName,
                        label = resolveInfo.loadLabel(packageManager).toString()
                            .ifBlank { packageName.substringAfterLast('.') },
                        componentName = ComponentName(packageName, resolveInfo.activityInfo.name),
                        firstInstallTime = packageInfo.firstInstallTime,
                        lastUpdateTime = packageInfo.lastUpdateTime,
                    )
                }.getOrNull()
            }
            .toList()
    }

    /** Scans launcher packages once, then derives all three categories from the same base list. */
    fun scanAllCategories(): Map<AppCategory, List<RecentApp>> {
        val launchable = getLaunchableApps()
        val installed = launchable.sortedByDescending { it.firstInstallTime }
        val installedPackages = launchable.mapTo(mutableSetOf()) { it.packageName }
        StarredAppsStore(context).retainInstalled(installedPackages)
        PinnedAppsStore(context).retainInstalled(installedPackages)
        HiddenAppsStore(context).retainInstalled(installedPackages)

        val opened = if (UsageAccess.hasPermission(context)) {
            val now = System.currentTimeMillis()
            val begin = now - TimeUnit.DAYS.toMillis(30)
            val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
            val stats = usageStatsManager?.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                begin,
                now,
            ).orEmpty()
            val latestUseByPackage = stats
                .asSequence()
                .filter { it.lastTimeUsed > 0L }
                .groupBy { it.packageName }
                .mapValues { (_, entries) -> entries.maxOf { it.lastTimeUsed } }

            launchable.mapNotNull { app ->
                val lastUsed = latestUseByPackage[app.packageName] ?: return@mapNotNull null
                app.copy(lastUsedTime = lastUsed)
            }.sortedByDescending { it.lastUsedTime }
        } else {
            emptyList()
        }

        val starredPackages = StarredAppsStore(context).getStarredPackages()
        val starred = installed.filter { it.packageName in starredPackages }
            .sortedWith(
                compareByDescending<RecentApp> { it.firstInstallTime }
                    .thenBy { it.label.lowercase() }
            )

        return mapOf(
            AppCategory.RECENTLY_INSTALLED to installed,
            AppCategory.RECENTLY_OPENED to opened,
            AppCategory.STARRED to starred,
        )
    }

    fun refreshCache(cache: RecentAppsCache = RecentAppsCache(context)): Map<AppCategory, List<RecentApp>> =
        synchronized(REFRESH_LOCK) {
            val snapshots = scanAllCategories()
            cache.putAll(
                snapshots = snapshots,
                usageAccessGranted = UsageAccess.hasPermission(context),
            )
            snapshots
        }

    fun getRecentApps(limit: Int = Int.MAX_VALUE): List<RecentApp> =
        getLaunchableApps().sortedByDescending { it.firstInstallTime }.take(limit.coerceAtLeast(0))

    fun getRecentlyOpenedApps(limit: Int = Int.MAX_VALUE): List<RecentApp> =
        scanAllCategories()[AppCategory.RECENTLY_OPENED].orEmpty().take(limit.coerceAtLeast(0))

    fun getStarredApps(limit: Int = Int.MAX_VALUE): List<RecentApp> =
        scanAllCategories()[AppCategory.STARRED].orEmpty().take(limit.coerceAtLeast(0))

    fun getApps(category: AppCategory, limit: Int = Int.MAX_VALUE): List<RecentApp> =
        scanAllCategories()[category].orEmpty().take(limit.coerceAtLeast(0))

    suspend fun loadIconBitmap(app: RecentApp, sizePx: Int = 128): Bitmap =
        if (useIconCache) {
            iconCache.load(app, sizePx)
        } else {
            loadIconBitmapDirect(app, sizePx)
        }

    fun loadIconBitmapDirect(app: RecentApp, sizePx: Int = 128): Bitmap {
        val drawable = runCatching {
            packageManager.getActivityIcon(app.componentName)
        }.getOrElse {
            packageManager.getApplicationIcon(app.packageName)
        }
        return drawable.toBitmap(
            width = sizePx,
            height = sizePx,
            config = Bitmap.Config.ARGB_8888,
        )
    } 

    companion object {
        private val REFRESH_LOCK = Any()
    }

}
