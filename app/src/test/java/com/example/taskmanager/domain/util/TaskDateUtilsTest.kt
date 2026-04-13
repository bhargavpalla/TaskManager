package com.example.taskmanager.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

class TaskDateUtilsTest {

    private val zoneId = ZoneId.systemDefault()
    private val locale = Locale.getDefault()

    @Test
    fun isToday_positive_forCurrentDay() {
        val todayMillis = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()

        assertTrue(TaskDateUtils.isToday(todayMillis, zoneId))
    }

    @Test
    fun isToday_negative_forTomorrow() {
        val tomorrowMillis = LocalDate.now(zoneId).plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        assertFalse(TaskDateUtils.isToday(tomorrowMillis, zoneId))
    }

    @Test
    fun isOverdue_positive_forYesterday() {
        val yesterdayMillis = LocalDate.now(zoneId).minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        assertTrue(TaskDateUtils.isOverdue(yesterdayMillis, zoneId))
    }

    @Test
    fun isOverdue_negative_forToday() {
        val todayMillis = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()

        assertFalse(TaskDateUtils.isOverdue(todayMillis, zoneId))
    }

    @Test
    fun isThisWeek_positive_forNextDayInSameWeek() {
        val sameWeekDate = findDate(daysRange = 1..7) {
            TaskDateUtils.isThisWeek(it, zoneId, locale)
        }

        assertTrue(TaskDateUtils.isThisWeek(sameWeekDate, zoneId, locale))
    }

    @Test
    fun isThisWeek_negative_forNextWeekDate() {
        val nextWeekDate = findDate(daysRange = 1..21) {
            TaskDateUtils.isLater(it, zoneId, locale)
        }

        assertFalse(TaskDateUtils.isThisWeek(nextWeekDate, zoneId, locale))
    }

    @Test
    fun isLater_positive_forFutureWeekDate() {
        val nextWeekDate = findDate(daysRange = 1..21) {
            TaskDateUtils.isLater(it, zoneId, locale)
        }

        assertTrue(TaskDateUtils.isLater(nextWeekDate, zoneId, locale))
    }

    @Test
    fun isLater_negative_forCurrentWeekDate() {
        val sameWeekDate = findDate(daysRange = 0..7) {
            TaskDateUtils.isThisWeek(it, zoneId, locale)
        }

        assertFalse(TaskDateUtils.isLater(sameWeekDate, zoneId, locale))
    }

    private fun findDate(daysRange: IntRange, predicate: (Long) -> Boolean): Long {
        for (days in daysRange) {
            val millis = LocalDate.now(zoneId).plusDays(days.toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
            if (predicate(millis)) return millis
        }
        throw AssertionError("Could not find matching date for predicate in range $daysRange")
    }
}
