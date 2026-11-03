package com.example.recentdock.data

/** Pure helpers used by the persisted manual-order feature. */
internal fun reconcileManualOrder(
    savedOrder: List<String>,
    currentPackagesInDefaultOrder: List<String>,
): List<String> {
    val available = currentPackagesInDefaultOrder.toSet()
    val seen = mutableSetOf<String>()
    return buildList {
        savedOrder.forEach { packageName ->
            if (packageName in available && seen.add(packageName)) add(packageName)
        }
        currentPackagesInDefaultOrder.forEach { packageName ->
            if (seen.add(packageName)) add(packageName)
        }
    }
}

internal fun movePackageInOrder(
    packageNames: List<String>,
    fromPackage: String,
    toPackage: String,
): List<String> {
    val fromIndex = packageNames.indexOf(fromPackage)
    val toIndex = packageNames.indexOf(toPackage)
    if (fromIndex < 0 || toIndex < 0 || fromIndex == toIndex) return packageNames

    return packageNames.toMutableList().apply {
        val moving = removeAt(fromIndex)
        add(toIndex.coerceIn(0, size), moving)
    }
}

/**
 * Holds an in-progress drag reorder without changing persisted state until the gesture commits.
 */
internal data class ManualAppOrderDraft(
    val originalOrder: List<String>,
    val previewOrder: List<String> = originalOrder,
) {
    fun move(fromPackage: String, toPackage: String): ManualAppOrderDraft {
        val moved = movePackageInOrder(previewOrder, fromPackage, toPackage)
        return if (moved == previewOrder) this else copy(previewOrder = moved)
    }

    fun orderToCommit(): List<String>? = previewOrder.takeIf { it != originalOrder }
}
