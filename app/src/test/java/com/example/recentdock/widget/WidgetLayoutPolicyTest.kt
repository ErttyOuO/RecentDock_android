package com.example.recentdock.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetLayoutPolicyTest {
    @Test fun commonFourCellWidthUsesFiveColumns() =
        assertEquals(5, WidgetLayoutPolicy.layoutFor(350f, 68f).columnsPerRow)

    @Test fun userObservedCompactWidthStillUsesFiveColumns() =
        assertEquals(5, WidgetLayoutPolicy.layoutFor(312f, 68f).columnsPerRow)

    @Test fun justBelowFiveColumnThresholdUsesFour() =
        assertEquals(4, WidgetLayoutPolicy.layoutFor(300f, 68f).columnsPerRow)

    @Test fun narrowWidthDropsToThree() =
        assertEquals(3, WidgetLayoutPolicy.layoutFor(260f, 68f).columnsPerRow)

    @Test fun minimumResizeWidthCanDropToTwo() =
        assertEquals(2, WidgetLayoutPolicy.layoutFor(220f, 68f).columnsPerRow)

    @Test fun compactHeightKeepsSingleRow() =
        assertEquals(1, WidgetLayoutPolicy.layoutFor(350f, 123f).rows)

    @Test fun tallWidgetEnablesSecondRow() =
        assertEquals(2, WidgetLayoutPolicy.layoutFor(350f, 124f).rows)

    @Test fun tallFiveColumnWidgetShowsTenSlots() =
        assertEquals(10, WidgetLayoutPolicy.layoutFor(350f, 140f).visibleSlots)

    @Test fun tallFourColumnWidgetShowsEightSlots() =
        assertEquals(8, WidgetLayoutPolicy.layoutFor(300f, 140f).visibleSlots)

    @Test fun tenAppsFitOnOneTallFiveColumnPage() =
        assertEquals(1, WidgetLayoutPolicy.pageCount(10, 10))

    @Test fun elevenAppsNeedSecondTallPage() =
        assertEquals(2, WidgetLayoutPolicy.pageCount(11, 10))

    @Test fun tenAppsNeedTwoCompactPages() =
        assertEquals(2, WidgetLayoutPolicy.pageCount(10, 5))

    @Test fun forwardPagingKeepsFirstPage() =
        assertEquals(0, WidgetLayoutPolicy.forwardOnlyPage(0, 3))

    @Test fun forwardPagingKeepsMiddlePage() =
        assertEquals(1, WidgetLayoutPolicy.forwardOnlyPage(1, 3))

    @Test fun forwardPagingKeepsLastPage() =
        assertEquals(2, WidgetLayoutPolicy.forwardOnlyPage(2, 3))

    @Test fun forwardPagingWrapsAfterLastPage() =
        assertEquals(0, WidgetLayoutPolicy.forwardOnlyPage(3, 3))

    @Test fun singlePageAlwaysUsesFirstPage() =
        assertEquals(0, WidgetLayoutPolicy.forwardOnlyPage(8, 1))

    @Test fun nextForwardPageAdvancesFirstToSecond() =
        assertEquals(1, WidgetLayoutPolicy.nextForwardPage(0, 3))

    @Test fun nextForwardPageAdvancesSecondToThird() =
        assertEquals(2, WidgetLayoutPolicy.nextForwardPage(1, 3))

    @Test fun nextForwardPageWrapsThirdToFirst() =
        assertEquals(0, WidgetLayoutPolicy.nextForwardPage(2, 3))
}
