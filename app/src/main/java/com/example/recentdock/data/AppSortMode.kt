package com.example.recentdock.data

enum class AppSortMode {
    CATEGORY_DEFAULT,
    NAME_ASC,
    NAME_DESC,
    NEWEST_INSTALL,
    OLDEST_INSTALL,
    LAST_USED,
}

fun List<RecentApp>.sortedFor(category: AppCategory, mode: AppSortMode): List<RecentApp> = when (mode) {
    AppSortMode.CATEGORY_DEFAULT -> when (category) {
        AppCategory.RECENTLY_INSTALLED -> sortedByDescending { it.firstInstallTime }
        AppCategory.RECENTLY_OPENED -> sortedByDescending { it.lastUsedTime }
        AppCategory.STARRED -> sortedWith(
            compareByDescending<RecentApp> { it.firstInstallTime }
                .thenBy { it.label.lowercase() }
        )
    }
    AppSortMode.NAME_ASC -> sortedBy { it.label.lowercase() }
    AppSortMode.NAME_DESC -> sortedByDescending { it.label.lowercase() }
    AppSortMode.NEWEST_INSTALL -> sortedByDescending { it.firstInstallTime }
    AppSortMode.OLDEST_INSTALL -> sortedBy { it.firstInstallTime }
    AppSortMode.LAST_USED -> sortedByDescending { it.lastUsedTime }
}

fun List<RecentApp>.sortedFor(
    category: AppCategory,
    mode: AppSortMode,
    pinnedPackages: Set<String>,
): List<RecentApp> {
    val sorted = sortedFor(category, mode)
    if (pinnedPackages.isEmpty()) return sorted
    val pinned = sorted.filter { it.packageName in pinnedPackages }
    val normal = sorted.filterNot { it.packageName in pinnedPackages }
    return pinned + normal
}

/**
 * Applies a persisted manual order only while the library is in CATEGORY_DEFAULT mode.
 *
 * Packages not yet present in [customOrder] fall back to the category default order. Pinning is
 * applied afterwards as a stable partition so pins remain above normal apps without destroying the
 * relative order the user arranged inside each group.
 */
fun List<RecentApp>.sortedForLibrary(
    category: AppCategory,
    mode: AppSortMode,
    pinnedPackages: Set<String> = emptySet(),
    customOrder: List<String> = emptyList(),
): List<RecentApp> {
    if (mode != AppSortMode.CATEGORY_DEFAULT || customOrder.isEmpty()) {
        return sortedFor(category, mode, pinnedPackages)
    }

    val defaultOrder = sortedFor(category, AppSortMode.CATEGORY_DEFAULT)
    val byPackage = defaultOrder.associateBy { it.packageName }
    val usedPackages = mutableSetOf<String>()
    val ordered = buildList {
        customOrder.forEach { packageName ->
            val app = byPackage[packageName]
            if (app != null && usedPackages.add(packageName)) add(app)
        }
        defaultOrder.forEach { app ->
            if (usedPackages.add(app.packageName)) add(app)
        }
    }

    if (pinnedPackages.isEmpty()) return ordered
    return ordered.filter { it.packageName in pinnedPackages } +
        ordered.filterNot { it.packageName in pinnedPackages }
}
