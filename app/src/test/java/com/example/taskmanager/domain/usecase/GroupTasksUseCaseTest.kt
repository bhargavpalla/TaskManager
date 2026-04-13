package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.model.TaskCategory
import com.example.taskmanager.domain.model.TaskPriority
import com.example.taskmanager.domain.model.TaskStatus
import com.example.taskmanager.domain.util.TaskDateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class GroupTasksUseCaseTest {

    private val useCase = GroupTasksUseCase()
    private val zoneId = ZoneId.systemDefault()
    private val locale = Locale.getDefault()

    @Test
    fun groupsTasksIntoExpectedBuckets() {
        val todayTask = task(id = 1, dueDate = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli())
        val overdueTask = task(id = 2, dueDate = LocalDate.now(zoneId).minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli())
        val thisWeekTask = task(id = 3, dueDate = findDate(1..7) { TaskDateUtils.isThisWeek(it, zoneId, locale) && !TaskDateUtils.isToday(it, zoneId) })
        val laterTask = task(id = 4, dueDate = findDate(1..21) { TaskDateUtils.isLater(it, zoneId, locale) })

        val grouped = useCase(listOf(todayTask, overdueTask, thisWeekTask, laterTask))

        assertEquals(listOf(todayTask.id), grouped.today.map { it.id })
        assertEquals(listOf(thisWeekTask.id), grouped.thisWeek.map { it.id })
        assertEquals(listOf(laterTask.id), grouped.later.map { it.id })
        assertEquals(listOf(overdueTask.id), grouped.overdue.map { it.id })
    }

    @Test
    fun eachTaskAppearsInOnlyOneGroup() {
        val tasks = listOf(
            task(id = 10, dueDate = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()),
            task(id = 11, dueDate = LocalDate.now(zoneId).minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()),
            task(id = 12, dueDate = findDate(1..7) { TaskDateUtils.isThisWeek(it, zoneId, locale) && !TaskDateUtils.isToday(it, zoneId) }),
            task(id = 13, dueDate = findDate(1..21) { TaskDateUtils.isLater(it, zoneId, locale) })
        )

        val grouped = useCase(tasks)
        val allIds = (grouped.today + grouped.thisWeek + grouped.later + grouped.overdue).map { it.id }
        val uniqueIds = allIds.toSet()

        assertEquals(allIds.size, uniqueIds.size)
        assertEquals(tasks.map { it.id }.toSet(), uniqueIds)
    }

    @Test
    fun returnsEmptyGroups_whenInputIsEmpty() {
        val grouped = useCase(emptyList())

        assertTrue(grouped.today.isEmpty())
        assertTrue(grouped.thisWeek.isEmpty())
        assertTrue(grouped.later.isEmpty())
        assertTrue(grouped.overdue.isEmpty())
    }

    private fun task(id: Int, dueDate: Long): Task {
        val now = System.currentTimeMillis()
        return Task(
            id = id,
            title = "Task $id",
            description = "desc",
            category = TaskCategory.WORK,
            status = TaskStatus.PENDING,
            dueDate = dueDate,
            priority = TaskPriority.MED,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun findDate(daysRange: IntRange, predicate: (Long) -> Boolean): Long {
        for (days in daysRange) {
            val millis = LocalDate.now(zoneId).plusDays(days.toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
            if (predicate(millis)) return millis
        }
        throw AssertionError("No matching date in range: $daysRange")
    }
}
