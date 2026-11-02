package com.example.recentdock.analytics

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class AnalyticsPeriod { DAY, WEEK, MONTH }

data class TimeRange(val startInclusive: Long, val endExclusive: Long)

object AnalyticsPeriods {
    fun range(period: AnalyticsPeriod, nowMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): TimeRange {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        return rangeForStart(period, periodStart(period, today), zoneId)
    }

    fun history(
        period: AnalyticsPeriod,
        nowMillis: Long,
        count: Int,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<TimeRange> {
        if (count <= 0) return emptyList()
        val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        val currentStart = periodStart(period, today)
        return (count - 1 downTo 0).map { offset ->
            val start = when (period) {
                AnalyticsPeriod.DAY -> currentStart.minusDays(offset.toLong())
                AnalyticsPeriod.WEEK -> currentStart.minusWeeks(offset.toLong())
                AnalyticsPeriod.MONTH -> currentStart.minusMonths(offset.toLong())
            }
            rangeForStart(period, start, zoneId)
        }
    }

    private fun periodStart(period: AnalyticsPeriod, date: LocalDate): LocalDate = when (period) {
        AnalyticsPeriod.DAY -> date
        AnalyticsPeriod.WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        AnalyticsPeriod.MONTH -> date.withDayOfMonth(1)
    }

    private fun rangeForStart(period: AnalyticsPeriod, startDate: LocalDate, zoneId: ZoneId): TimeRange {
        val endDate = when (period) {
            AnalyticsPeriod.DAY -> startDate.plusDays(1)
            AnalyticsPeriod.WEEK -> startDate.plusWeeks(1)
            AnalyticsPeriod.MONTH -> startDate.plusMonths(1)
        }
        return TimeRange(
            startDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            endDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
        )
    }
}
