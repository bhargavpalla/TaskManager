package com.example.taskmanager.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.model.TaskCategory
import com.example.taskmanager.domain.usecase.CreateTaskUseCase
import com.example.taskmanager.domain.usecase.DeleteTaskPermanentlyUseCase
import com.example.taskmanager.domain.usecase.DeleteTaskUseCase
import com.example.taskmanager.domain.usecase.FilterTasksUseCase
import com.example.taskmanager.domain.usecase.GetDeletedTasksUseCase
import com.example.taskmanager.domain.usecase.GroupTasksUseCase
import com.example.taskmanager.domain.usecase.SearchTasksUseCase
import com.example.taskmanager.domain.usecase.RestoreTaskUseCase
import com.example.taskmanager.domain.usecase.UpdateTaskUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TaskViewModel(
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val restoreTaskUseCase: RestoreTaskUseCase,
    private val deleteTaskPermanentlyUseCase: DeleteTaskPermanentlyUseCase,
    private val searchTasksUseCase: SearchTasksUseCase,
    private val getDeletedTasksUseCase: GetDeletedTasksUseCase,
    private val filterTasksUseCase: FilterTasksUseCase,
    private val groupTasksUseCase: GroupTasksUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    /** Full list from search; category chips filter on top of this (not on already-filtered data). */
    private val _tasksMatchingSearch = MutableStateFlow<List<Task>>(emptyList())
    private val _tasksUi = MutableStateFlow(TaskListUiModel(query = _searchQuery.value))
    val tasksUi: StateFlow<TaskListUiModel> = _tasksUi.asStateFlow()

    private val _trashUi = MutableStateFlow<UiState<List<Task>>>(UiState.Loading)
    val trashUi: StateFlow<UiState<List<Task>>> = _trashUi.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private var tasksCollectorJob: Job? = null

    init {
        observeTasks()
        observeTrash()
    }

    private fun observeTasks() {
        tasksCollectorJob?.cancel()
        tasksCollectorJob = _searchQuery
        .debounce(300)
        .map { it.trim() }
        .distinctUntilChanged()
        .flatMapLatest { query -> searchTasksUseCase(query) }
        .onEach { tasksFromSearch ->
            _tasksMatchingSearch.value = tasksFromSearch
            val filtered = filterTasksUseCase(tasksFromSearch, selectedCategoryFilter())
            val groups = groupTasksUseCase(filtered)
            val allSectionsEmpty = groups.isCompletelyEmpty()
            _tasksUi.value = _tasksUi.value.copy(
                state = if (tasksFromSearch.isEmpty() || allSectionsEmpty) UiState.Empty else UiState.Success(filtered),
                todayTasks = groups.today,
                thisWeekTasks = groups.thisWeek,
                laterTasks = groups.later,
                overdueTasks = groups.overdue
            )
        }
        .catch { throwable ->
            _tasksUi.value = _tasksUi.value.copy(
                state = UiState.Error(throwable.message ?: "Unable to load tasks.")
            )
        }
        .launchIn(viewModelScope)
    }

    private fun observeTrash() {
        getDeletedTasksUseCase()
            .onEach { tasks ->
                _trashUi.value = if (tasks.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(tasks)
                }
            }
            .catch { throwable ->
                _trashUi.value = UiState.Error(
                    message = throwable.message ?: "Unable to load tasks."
                )
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _tasksUi.value = _tasksUi.value.copy(query = query, state = UiState.Loading)
    }

    fun onFilterSelected(filter: TaskFilter) {
        _selectedFilter.value = filter
        val source = _tasksMatchingSearch.value
        val filtered = filterTasksUseCase(source, selectedCategoryFilter())
        val groups = groupTasksUseCase(filtered)
        val allSectionsEmpty = groups.isCompletelyEmpty()
        _tasksUi.value = _tasksUi.value.copy(
            selectedFilter = filter,
            state = when {
                source.isEmpty() || allSectionsEmpty -> UiState.Empty
                else -> UiState.Success(filtered)
            },
            todayTasks = groups.today,
            thisWeekTasks = groups.thisWeek,
            laterTasks = groups.later,
            overdueTasks = groups.overdue
        )
    }

    fun createTask(task: Task) {
        runAction { createTaskUseCase(task); _events.emit("Task created") }
    }

    fun updateTask(task: Task) {
        runAction { updateTaskUseCase(task); _events.emit("Task updated") }
    }

    fun deleteTask(taskId: Int) {
        runAction { deleteTaskUseCase(taskId); _events.emit("Task moved to trash") }
    }

    fun restoreTask(taskId: Int) {
        runAction { restoreTaskUseCase(taskId); _events.emit("Task restored") }
    }

    fun deleteTaskPermanently(taskId: Int) {
        runAction { deleteTaskPermanentlyUseCase(taskId); _events.emit("Task deleted permanently") }
    }

    private fun runAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (throwable: Throwable) {
                _events.emit(throwable.message ?: "Something went wrong.")
            }
        }
    }

    private fun selectedCategoryFilter(): TaskCategory? {
        return when (_selectedFilter.value) {
            TaskFilter.ALL -> null
            TaskFilter.WORK -> TaskCategory.WORK
            TaskFilter.PERSONAL -> TaskCategory.PERSONAL
            TaskFilter.SHOPPING -> TaskCategory.SHOPPING
        }
    }
}
