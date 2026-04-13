package com.example.taskmanager.presentation.task

import app.cash.turbine.test
import com.example.taskmanager.domain.model.GroupedTasks
import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.model.TaskCategory
import com.example.taskmanager.domain.model.TaskPriority
import com.example.taskmanager.domain.model.TaskStatus
import com.example.taskmanager.domain.usecase.CreateTaskUseCase
import com.example.taskmanager.domain.usecase.DeleteTaskPermanentlyUseCase
import com.example.taskmanager.domain.usecase.DeleteTaskUseCase
import com.example.taskmanager.domain.usecase.FilterTasksUseCase
import com.example.taskmanager.domain.usecase.GetDeletedTasksUseCase
import com.example.taskmanager.domain.usecase.GroupTasksUseCase
import com.example.taskmanager.domain.usecase.RestoreTaskUseCase
import com.example.taskmanager.domain.usecase.SearchTasksUseCase
import com.example.taskmanager.domain.usecase.UpdateTaskUseCase
import com.example.taskmanager.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createTaskUseCase: CreateTaskUseCase = mockk()
    private val updateTaskUseCase: UpdateTaskUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk()
    private val restoreTaskUseCase: RestoreTaskUseCase = mockk()
    private val deleteTaskPermanentlyUseCase: DeleteTaskPermanentlyUseCase = mockk()
    private val searchTasksUseCase: SearchTasksUseCase = mockk()
    private val getDeletedTasksUseCase: GetDeletedTasksUseCase = mockk()
    private val filterTasksUseCase: FilterTasksUseCase = mockk()
    private val groupTasksUseCase: GroupTasksUseCase = mockk()

    @Test
    fun queryChangeTriggersSearchUseCase_afterDebounce() = runTest {
        stubCommon(taskList = listOf(task(1)))
        val viewModel = createViewModel()

        viewModel.onSearchQueryChanged("work")
        advanceTimeBy(299)
        verify(exactly = 0) { searchTasksUseCase.invoke("work") }

        advanceTimeBy(1)
        verify(atLeast = 1) { searchTasksUseCase.invoke("work") }
    }

    @Test
    fun distinctUntilChanged_preventsDuplicateSearchCalls() = runTest {
        stubCommon(taskList = listOf(task(1)))
        val viewModel = createViewModel()

        viewModel.onSearchQueryChanged("dup")
        advanceTimeBy(300)
        viewModel.onSearchQueryChanged("dup")
        advanceTimeBy(300)

        verify(exactly = 1) { searchTasksUseCase.invoke("dup") }
    }

    @Test
    fun tasksUi_emitsSuccessWhenDataPresent() = runTest {
        val task = task(1)
        every { searchTasksUseCase.invoke(any()) } returns flowOf(listOf(task))
        every { filterTasksUseCase.invoke(any(), any()) } returns listOf(task)
        every { groupTasksUseCase.invoke(any()) } returns GroupedTasks(
            today = listOf(task),
            thisWeek = emptyList(),
            later = emptyList(),
            overdue = emptyList()
        )
        every { getDeletedTasksUseCase.invoke() } returns flowOf(emptyList())
        stubActionUseCases()

        val viewModel = createViewModel()
        viewModel.tasksUi.test {
            val initial = awaitItem()
            assert(initial.state is UiState.Loading)

            advanceTimeBy(300)
            val next = awaitItem()
            assert(next.state is UiState.Success)
            assertEquals(listOf(task.id), next.todayTasks.map { it.id })
        }
    }

    @Test
    fun tasksUi_emitsEmptyWhenNoData() = runTest {
        every { searchTasksUseCase.invoke(any()) } returns flowOf(emptyList())
        every { filterTasksUseCase.invoke(any(), any()) } returns emptyList()
        every { groupTasksUseCase.invoke(any()) } returns GroupedTasks(emptyList(), emptyList(), emptyList(), emptyList())
        every { getDeletedTasksUseCase.invoke() } returns flowOf(emptyList())
        stubActionUseCases()

        val viewModel = createViewModel()
        viewModel.tasksUi.test {
            awaitItem()
            advanceTimeBy(300)
            val state = awaitItem()
            assert(state.state is UiState.Empty)
        }
    }

    @Test
    fun tasksUi_emitsErrorWhenSearchFails() = runTest {
        every { searchTasksUseCase.invoke(any()) } returns flow { throw IllegalStateException("boom") }
        every { getDeletedTasksUseCase.invoke() } returns flowOf(emptyList())
        stubActionUseCases()

        val viewModel = createViewModel()
        viewModel.tasksUi.test {
            awaitItem()
            advanceTimeBy(300)
            val state = awaitItem()
            assert(state.state is UiState.Error)
        }
    }

    @Test
    fun filterAndGroupAreApplied_andReflectedInUiState() = runTest {
        val input = listOf(task(1), task(2))
        val filtered = listOf(task(2))
        every { searchTasksUseCase.invoke(any()) } returns flowOf(input)
        every { filterTasksUseCase.invoke(input, TaskCategory.WORK) } returns filtered
        every { filterTasksUseCase.invoke(input, null) } returns input
        every { groupTasksUseCase.invoke(filtered) } returns GroupedTasks(filtered, emptyList(), emptyList(), emptyList())
        every { groupTasksUseCase.invoke(input) } returns GroupedTasks(input, emptyList(), emptyList(), emptyList())
        every { getDeletedTasksUseCase.invoke() } returns flowOf(emptyList())
        stubActionUseCases()

        val viewModel = createViewModel()
        advanceTimeBy(300)
        viewModel.onFilterSelected(TaskFilter.WORK)

        verify(atLeast = 1) { filterTasksUseCase.invoke(input, TaskCategory.WORK) }
        verify(atLeast = 1) { groupTasksUseCase.invoke(filtered) }
    }

    @Test
    fun createTask_emitsEventAndInvokesUseCase() = runTest {
        stubCommon(taskList = listOf(task(1)))
        val viewModel = createViewModel()
        val task = task(100)

        viewModel.events.test {
            viewModel.createTask(task)
            assertEquals("Task created", awaitItem())
        }
        coVerify { createTaskUseCase.invoke(task, any()) }
    }

    @Test
    fun updateTask_emitsEventAndInvokesUseCase() = runTest {
        stubCommon(taskList = listOf(task(1)))
        val viewModel = createViewModel()
        val task = task(200)

        viewModel.events.test {
            viewModel.updateTask(task)
            assertEquals("Task updated", awaitItem())
        }
        coVerify { updateTaskUseCase.invoke(task, any()) }
    }

    @Test
    fun deleteRestorePermanentDelete_emitEventsAndInvokeUseCases() = runTest {
        stubCommon(taskList = listOf(task(1)))
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.deleteTask(10)
            assertEquals("Task moved to trash", awaitItem())

            viewModel.restoreTask(10)
            assertEquals("Task restored", awaitItem())

            viewModel.deleteTaskPermanently(10)
            assertEquals("Task deleted permanently", awaitItem())
        }

        coVerify { deleteTaskUseCase.invoke(10, any()) }
        coVerify { restoreTaskUseCase.invoke(10, any()) }
        coVerify { deleteTaskPermanentlyUseCase.invoke(10) }
    }

    @Test
    fun trashFlow_emitsSuccessAndEmptyStates() = runTest {
        every { searchTasksUseCase.invoke(any()) } returns flowOf(emptyList())
        every { filterTasksUseCase.invoke(any(), any()) } returns emptyList()
        every { groupTasksUseCase.invoke(any()) } returns GroupedTasks(emptyList(), emptyList(), emptyList(), emptyList())
        every { getDeletedTasksUseCase.invoke() } returns flowOf(listOf(task(9)), emptyList())
        stubActionUseCases()

        val viewModel = createViewModel()
        viewModel.trashUi.test {
            val first = awaitItem()
            assert(first is UiState.Success)
            val second = awaitItem()
            assert(second is UiState.Empty)
        }
    }

    @Test
    fun trashFlow_emitsErrorStateOnFailure() = runTest {
        every { searchTasksUseCase.invoke(any()) } returns flowOf(emptyList())
        every { filterTasksUseCase.invoke(any(), any()) } returns emptyList()
        every { groupTasksUseCase.invoke(any()) } returns GroupedTasks(emptyList(), emptyList(), emptyList(), emptyList())
        every { getDeletedTasksUseCase.invoke() } returns flow { throw IllegalStateException("trash failed") }
        stubActionUseCases()

        val viewModel = createViewModel()
        viewModel.trashUi.test {
            val first = awaitItem()
            assert(first is UiState.Error)
        }
    }

    private fun createViewModel(): TaskViewModel {
        return TaskViewModel(
            createTaskUseCase = createTaskUseCase,
            updateTaskUseCase = updateTaskUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            restoreTaskUseCase = restoreTaskUseCase,
            deleteTaskPermanentlyUseCase = deleteTaskPermanentlyUseCase,
            searchTasksUseCase = searchTasksUseCase,
            getDeletedTasksUseCase = getDeletedTasksUseCase,
            filterTasksUseCase = filterTasksUseCase,
            groupTasksUseCase = groupTasksUseCase
        )
    }

    private fun stubCommon(taskList: List<Task>) {
        every { searchTasksUseCase.invoke(any()) } returns flowOf(taskList)
        every { filterTasksUseCase.invoke(any(), any()) } answers { firstArg() }
        every { groupTasksUseCase.invoke(any()) } answers {
            val tasks = firstArg<List<Task>>()
            GroupedTasks(
                today = tasks,
                thisWeek = emptyList(),
                later = emptyList(),
                overdue = emptyList()
            )
        }
        every { getDeletedTasksUseCase.invoke() } returns flowOf(emptyList())
        stubActionUseCases()
    }

    private fun stubActionUseCases() {
        coEvery { createTaskUseCase.invoke(any(), any()) } returns 1
        coEvery { updateTaskUseCase.invoke(any(), any()) } just runs
        coEvery { deleteTaskUseCase.invoke(any(), any()) } just runs
        coEvery { restoreTaskUseCase.invoke(any(), any()) } just runs
        coEvery { deleteTaskPermanentlyUseCase.invoke(any()) } just runs
    }

    private fun task(id: Int): Task {
        val now = System.currentTimeMillis()
        return Task(
            id = id,
            title = "Task $id",
            description = "desc",
            category = TaskCategory.WORK,
            status = TaskStatus.PENDING,
            dueDate = now,
            priority = TaskPriority.MED,
            createdAt = now,
            updatedAt = now
        )
    }
}
