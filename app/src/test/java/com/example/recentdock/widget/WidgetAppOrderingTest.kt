package com.example.recentdock.widget

import android.content.ComponentName
import com.example.recentdock.data.AppCategory
import com.example.recentdock.data.RecentApp
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetAppOrderingTest {
    @Test
    fun widgetUsesLibraryManualOrderAndPinnedPartitionBeforeHidingAndLimiting() {
        val rawApps = listOf(
            app("a", 4),
            app("b", 3),
            app("c", 2),
            app("d", 1),
        )

        val result = orderedAppsForWidget(
            rawApps = rawApps,
            category = AppCategory.RECENTLY_INSTALLED,
            pinnedPackages = setOf("a", "c"),
            customOrder = listOf("d", "c", "b", "a"),
            hiddenPackages = setOf("c"),
            displayCount = 2,
        )

        assertEquals(listOf("a", "d"), result.map { it.packageName })
    }

    @Test
    fun widgetDefaultsToCategoryOrderAndStillPlacesPinnedAppsFirst() {
        val rawApps = listOf(
            app("a", 4),
            app("b", 3),
            app("c", 2),
            app("d", 1),
        )

        val result = orderedAppsForWidget(
            rawApps = rawApps,
            category = AppCategory.RECENTLY_INSTALLED,
            pinnedPackages = setOf("c"),
            customOrder = emptyList(),
            hiddenPackages = emptySet(),
            displayCount = 4,
        )

        assertEquals(listOf("c", "a", "b", "d"), result.map { it.packageName })
    }

    private fun app(packageName: String, installTime: Long) = RecentApp(
        packageName = packageName,
        label = packageName,
        componentName = ComponentName(packageName, "$packageName.Main"),
        firstInstallTime = installTime,
    )
}
