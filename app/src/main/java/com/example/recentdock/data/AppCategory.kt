package com.example.recentdock.data

enum class AppCategory {
    RECENTLY_INSTALLED,
    RECENTLY_OPENED,
    STARRED;

    /**
     * Widget category control cycles through all three user-facing lists.
     */
    fun nextWidgetCategory(): AppCategory = when (this) {
        RECENTLY_INSTALLED -> RECENTLY_OPENED
        RECENTLY_OPENED -> STARRED
        STARRED -> RECENTLY_INSTALLED
    }

    companion object {
        fun fromStored(value: String?): AppCategory =
            entries.firstOrNull { it.name == value } ?: RECENTLY_INSTALLED
    }
}
