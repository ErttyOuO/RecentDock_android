package com.example.recentdock.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppCategoryTest {
    @Test fun widgetCycleInstalledToOpened() =
        assertEquals(AppCategory.RECENTLY_OPENED, AppCategory.RECENTLY_INSTALLED.nextWidgetCategory())

    @Test fun widgetCycleOpenedToStarred() =
        assertEquals(AppCategory.STARRED, AppCategory.RECENTLY_OPENED.nextWidgetCategory())

    @Test fun widgetCycleStarredToInstalled() =
        assertEquals(AppCategory.RECENTLY_INSTALLED, AppCategory.STARRED.nextWidgetCategory())
}
