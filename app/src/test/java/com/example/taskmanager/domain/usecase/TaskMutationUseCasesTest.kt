package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.model.TaskCategory
import com.example.taskmanager.domain.model.TaskPriority
import com.example.taskmanager.domain.model.TaskStatus
import com.example.taskmanager.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskMutationUseCasesTest {

    private val repository: TaskRepository = mockk(relaxed = true)
    private val createTaskUseCase = CreateTaskUseCase(repository)
    private val updateTaskUseCase = UpdateTaskUseCase(repository)
    private val deleteTaskUseCase = DeleteTaskUseCase(repository)
    private val restoreTaskUseCase = RestoreTaskUseCase(repository)
    private val deleteTaskPermanentlyUseCase = DeleteTaskPermanentlyUseCase(repository)

    @Test
    fun createTask_callsRepository_whenValid() = runTest {
        val task = task(id = 0, title = "Create", dueDate = nowPlusDays(1))
        coEvery { repository.createTask(task) } returns 55

        val id = createTaskUseCase(task, nowMillis = System.currentTimeMillis())

        assertEquals(55, id)
        coVerify(exactly = 1) { repository.createTask(task) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun createTask_throws_whenTitleBlank() = runTest {
        val task = task(id = 0, title = "  ", dueDate = nowPlusDays(1))
        createTaskUseCase(task, nowMillis = System.currentTimeMillis())
    }

    @Test
    fun updateTask_callsRepository_whenValid() = runTest {
        val task = task(id = 7, title = "Update", dueDate = nowPlusDays(1))
        coEvery { repository.updateTask(task) } just runs

        updateTaskUseCase(task, nowMillis = System.currentTimeMillis())

        coVerify(exactly = 1) { repository.updateTask(task) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateTask_throws_whenIdInvalid() = runTest {
        updateTaskUseCase(task(id = 0, title = "Nope", dueDate = nowPlusDays(1)))
    }

    @Test
    fun deleteTask_callsSoftDeleteWithNowMillis() = runTest {
        val now = 1234L
        coEvery { repository.softDeleteTask(id = 3, deletedAt = now, updatedAt = now) } just runs

        deleteTaskUseCase(taskId = 3, nowMillis = now)

        coVerify(exactly = 1) { repository.softDeleteTask(id = 3, deletedAt = now, updatedAt = now) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun deleteTask_throws_whenTaskIdInvalid() = runTest {
        deleteTaskUseCase(taskId = 0)
    }

    @Test
    fun restoreTask_callsRepositoryWithNowMillis() = runTest {
        val now = 9898L
        coEvery { repository.restoreTask(id = 4, updatedAt = now) } just runs

        restoreTaskUseCase(taskId = 4, nowMillis = now)

        coVerify(exactly = 1) { repository.restoreTask(id = 4, updatedAt = now) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun restoreTask_throws_whenTaskIdInvalid() = runTest {
        restoreTaskUseCase(taskId = -1)
    }

    @Test
    fun deleteTaskPermanently_callsRepository() = runTest {
        coEvery { repository.deleteTaskPermanently(5) } just runs

        deleteTaskPermanentlyUseCase(5)

        coVerify(exactly = 1) { repository.deleteTaskPermanently(5) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun deleteTaskPermanently_throws_whenTaskIdInvalid() = runTest {
        deleteTaskPermanentlyUseCase(0)
    }

    private fun task(id: Int, title: String, dueDate: Long): Task {
        val now = System.currentTimeMillis()
        return Task(
            id = id,
            title = title,
            description = "desc",
            category = TaskCategory.WORK,
            status = TaskStatus.PENDING,
            dueDate = dueDate,
            priority = TaskPriority.MED,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun nowPlusDays(days: Long): Long {
        return System.currentTimeMillis() + days * 24 * 60 * 60 * 1000
    }
}
