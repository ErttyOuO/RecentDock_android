package com.example.recentdock.widget

/** Layout policy for the RemoteViews RecentDock widget. */
internal object WidgetLayoutPolicy {
    const val MAX_WIDGET_APPS = 15
    private const val TWO_ROW_MIN_HEIGHT_DP = 124f

    data class LayoutSpec(
        val columnsPerRow: Int,
        val rows: Int,
    ) {
        val visibleSlots: Int = columnsPerRow * rows
    }

    /**
     * Keep the icons large, but add a second row when the host gives enough height.
     * Width still determines how many items fit comfortably per row.
     */
    fun layoutFor(widthDp: Float, heightDp: Float): LayoutSpec {
        val columns = when {
            widthDp < 235f -> 2
            widthDp < 275f -> 3
            widthDp < 310f -> 4
            else -> 5
        }
        val rows = if (heightDp >= TWO_ROW_MIN_HEIGHT_DP) 2 else 1
        return LayoutSpec(columnsPerRow = columns, rows = rows)
    }

    fun pageCount(appCount: Int, visibleSlots: Int): Int {
        if (appCount <= 0) return 1
        val safeSlots = visibleSlots.coerceAtLeast(1)
        return (appCount + safeSlots - 1) / safeSlots
    }

    /**
     * Forward-only circular paging: 0 -> 1 -> ... -> last -> 0.
     * A requested page beyond the last page is treated as the wrapped first page.
     */
    fun forwardOnlyPage(requestedPage: Int, pageCount: Int): Int {
        if (pageCount <= 1) return 0
        val safePage = requestedPage.coerceAtLeast(0)
        return if (safePage >= pageCount) 0 else safePage
    }

    fun nextForwardPage(currentPage: Int, pageCount: Int): Int {
        if (pageCount <= 1) return 0
        val safeCurrent = forwardOnlyPage(currentPage, pageCount)
        return (safeCurrent + 1) % pageCount
    }
}
