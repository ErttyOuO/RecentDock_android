package com.example.recentdock.analytics

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class AnalyticsPeriodTest {
    private val zone = ZoneId.of("Asia/Taipei")
    private val now = ZonedDateTime.of(2026, 9, 15, 10, 34, 0, 0, zone).toInstant().toEpochMilli()

    @Test fun dayStartsAtLocalMidnight() = assertStart(AnalyticsPeriod.DAY, LocalDate.of(2026, 9, 15))
    @Test fun weekStartsOnMonday() = assertStart(AnalyticsPeriod.WEEK, LocalDate.of(2026, 9, 14))
    @Test fun monthStartsOnFirstDay() = assertStart(AnalyticsPeriod.MONTH, LocalDate.of(2026, 9, 1))

    private fun assertStart(period: AnalyticsPeriod, expected: LocalDate) {
        val range = AnalyticsPeriods.range(period, now, zone)
        val actual = Instant.ofEpochMilli(range.startInclusive).atZone(zone).toLocalDate()
        assertEquals(expected, actual)
    }
}
