package com.example.recentdock.widget

import com.example.recentdock.data.AppCategory
import com.example.recentdock.data.AppSortMode
import com.example.recentdock.data.RecentApp
import com.example.recentdock.data.sortedForLibrary

/** Applies the same manual order and pinned partition as Library before hiding and paging apps. */
internal fun orderedAppsForWidget(
    rawApps: List<RecentApp>,
    category: AppCategory,
    pinnedPackages: Set<String>,
    customOrder: List<String>,
    hiddenPackages: Set<String>,
    displayCount: Int,
): List<RecentApp> = rawApps
    .sortedForLibrary(category, AppSortMode.CATEGORY_DEFAULT, pinnedPackages, customOrder)
    .filterNot { it.packageName in hiddenPackages }
    .take(displayCount.coerceAtLeast(0))
