package com.example.recentdock.data

import android.content.ComponentName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun movePackageUsesTargetPositionWhenMovingDown() {
        assertEquals(
            listOf("a", "c", "d", "b"),
            movePackageInOrder(
                packageNames = listOf("a", "b", "c", "d"),
                fromPackage = "b",
                toPackage = "d",
            ),
        )
    }

    @Test
    fun manualOrderDraftPreviewsWithoutMutatingOriginalAndOnlyCommitsChanges() {
        val original = listOf("a", "b", "c", "d")
        val draft = ManualAppOrderDraft(original)

        assertNull(draft.orderToCommit())

        val preview = draft.move("b", "d")
        assertEquals(listOf("a", "c", "d", "b"), preview.previewOrder)
        assertEquals(original, draft.previewOrder)
        assertEquals(listOf("a", "c", "d", "b"), preview.orderToCommit())

        val restored = preview.move("b", "c")
        assertEquals(original, restored.previewOrder)
        assertNull(restored.orderToCommit())
    }

    @Test
    fun dragDraftUsesDisplayedPinnedOrderAsItsStartingOrder() {
        val apps = listOf(
            app("a", 4),
            app("b", 3),
            app("c", 2),
            app("d", 1),
        )
        val pinned = setOf("c", "d")
        val displayedOrder = apps.sortedForLibrary(
            category = AppCategory.RECENTLY_INSTALLED,
            mode = AppSortMode.CATEGORY_DEFAULT,
            pinnedPackages = pinned,
            customOrder = listOf("b", "d", "a", "c"),
        ).map { it.packageName }

        val movedOrder = ManualAppOrderDraft(displayedOrder)
            .move("c", "d")
            .previewOrder

        assertEquals(listOf("c", "d", "b", "a"), movedOrder)
        assertEquals(
            movedOrder,
            apps.sortedForLibrary(
                category = AppCategory.RECENTLY_INSTALLED,
                mode = AppSortMode.CATEGORY_DEFAULT,
                pinnedPackages = pinned,
                customOrder = movedOrder,
            ).map { it.packageName },
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
