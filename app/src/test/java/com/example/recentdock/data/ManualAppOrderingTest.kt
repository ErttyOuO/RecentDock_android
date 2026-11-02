package com.example.recentdock.data

import android.content.ComponentName
import org.junit.Assert.assertEquals
import org.junit.Test

class ManualAppOrderingTest {
    @Test
    fun reconcileDropsRemovedAndAppendsNewPackages() {
        assertEquals(
            listOf("c", "a", "d"),
            reconcileManualOrder(
                savedOrder = listOf("c", "gone", "a", "c"),
                currentPackagesInDefaultOrder = listOf("a", "c", "d"),
            ),
        )
    }

    @Test
    fun movePackageUsesTargetPosition() {
        assertEquals(
            listOf("a", "d", "b", "c"),
            movePackageInOrder(
                packageNames = listOf("a", "b", "c", "d"),
                fromPackage = "d",
                toPackage = "b",
            ),
        )
    }

    @Test
    fun manualOrderPreservesPinsAsStableTopPartition() {
        val apps = listOf(
            app("a", 4),
            app("b", 3),
            app("c", 2),
            app("d", 1),
        )
        val result = apps.sortedForLibrary(
            category = AppCategory.RECENTLY_INSTALLED,
            mode = AppSortMode.CATEGORY_DEFAULT,
            pinnedPackages = setOf("c", "d"),
            customOrder = listOf("b", "d", "a", "c"),
        )
        assertEquals(listOf("d", "c", "b", "a"), result.map { it.packageName })
    }

    private fun app(packageName: String, installTime: Long) = RecentApp(
        packageName = packageName,
        label = packageName,
        componentName = ComponentName(packageName, "$packageName.Main"),
        firstInstallTime = installTime,
    )
}
