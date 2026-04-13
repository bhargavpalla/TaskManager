package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.model.TaskCategory
import com.example.taskmanager.domain.model.TaskPriority
import com.example.taskmanager.domain.model.TaskStatus
import com.example.taskmanager.domain.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TaskQueryUseCasesTest {

    private val repository: TaskRepository = mockk(relaxed = true)
    private val getTasksUseCase = GetTasksUseCase(repository)
    private val getDeletedTasksUseCase = GetDeletedTasksUseCase(repository)
    private val filterTasksUseCase = FilterTasksUseCase()

    @Test
    fun getTasks_returnsRepositoryFlow() {
        val flow = flowOf(listOf(task(1)))
        every { repository.observeActiveTasksByPriority() } returns flow

        val result = getTasksUseCase()

        assertSame(flow, result)
        verify(exactly = 1) { repository.observeActiveTasksByPriority() }
    }

    @Test
    fun getDeletedTasks_returnsRepositoryFlow() {
        val flow = flowOf(listOf(task(2)))
        every { repository.observeDeletedTasks() } returns flow

        val result = getDeletedTasksUseCase()

        assertSame(flow, result)
        verify(exactly = 1) { repository.observeDeletedTasks() }
    }

    @Test
    fun searchTasks_usesGetTasksUseCase_whenQueryBlank() {
        val fallbackFlow: Flow<List<Task>> = flowOf(listOf(task(9)))
        val getTasks: GetTasksUseCase = mockk()
        every { getTasks.invoke() } returns fallbackFlow
        val searchUseCase = SearchTasksUseCase(repository, getTasks)

        val result = searchUseCase("   ")

        assertSame(fallbackFlow, result)
        verify(exactly = 1) { getTasks.invoke() }
        verify(exactly = 0) { repository.searchActiveTasksByPriority(any()) }
    }

    @Test
    fun searchTasks_normalizesQuery_andCallsRepository() {
        val repoFlow = flowOf(listOf(task(3)))
        every { repository.searchActiveTasksByPriority("hello* world*") } returns repoFlow
        val searchUseCase = SearchTasksUseCase(repository, getTasksUseCase)

        val result = searchUseCase(" hello   world ")

        assertSame(repoFlow, result)
        verify(exactly = 1) { repository.searchActiveTasksByPriority("hello* world*") }
    }

    @Test
    fun filterTasks_returnsAll_whenCategoryNull() {
        val tasks = listOf(task(1), task(2))

        val result = filterTasksUseCase(tasks, null)

        assertEquals(tasks, result)
    }

    @Test
    fun filterTasks_filtersByCategory_whenProvided() {
        val tasks = listOf(
            task(1, category = TaskCategory.WORK),
            task(2, category = TaskCategory.PERSONAL),
            task(3, category = TaskCategory.WORK)
        )

        val result = filterTasksUseCase(tasks, TaskCategory.WORK)

        assertEquals(listOf(1, 3), result.map { it.id })
    }

    private fun task(id: Int, category: TaskCategory = TaskCategory.WORK): Task {
        val now = System.currentTimeMillis()
        return Task(
            id = id,
            title = "Task $id",
            description = "desc",
            category = category,
            status = TaskStatus.PENDING,
            dueDate = now + 1000,
            priority = TaskPriority.MED,
            createdAt = now,
            updatedAt = now
        )
    }
}
