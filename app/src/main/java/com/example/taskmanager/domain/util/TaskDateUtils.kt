package com.example.taskmanager.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

object TaskDateUtils {
    private fun toLocalDate(millis: Long, zoneId: ZoneId): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
    }

    fun startOfTodayMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun isOverdue(
        dueDateMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        return dueDateMillis < startOfTodayMillis(zoneId)
    }

    fun isToday(
        dueDateMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        val today = LocalDate.now(zoneId)
        return toLocalDate(dueDateMillis, zoneId) == today
    }

    fun isThisWeek(
        dueDateMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault()
    ): Boolean {
        val dueDate = toLocalDate(dueDateMillis, zoneId)
        val today = LocalDate.now(zoneId)
        val weekFields = WeekFields.of(locale)

        return dueDate.get(weekFields.weekOfWeekBasedYear()) == today.get(weekFields.weekOfWeekBasedYear()) &&
            dueDate.get(weekFields.weekBasedYear()) == today.get(weekFields.weekBasedYear())
    }

    fun isLater(
        dueDateMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault()
    ): Boolean {
        val dueDate = toLocalDate(dueDateMillis, zoneId)
        val today = LocalDate.now(zoneId)
        val weekFields = WeekFields.of(locale)
        val dueWeekYear = dueDate.get(weekFields.weekBasedYear())
        val todayWeekYear = today.get(weekFields.weekBasedYear())
        val dueWeek = dueDate.get(weekFields.weekOfWeekBasedYear())
        val todayWeek = today.get(weekFields.weekOfWeekBasedYear())

        return dueWeekYear > todayWeekYear || (dueWeekYear == todayWeekYear && dueWeek > todayWeek)
    }
}
