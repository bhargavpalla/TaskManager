package com.example.taskmanager.presentation.task

import com.example.taskmanager.domain.model.Task

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
}

enum class TaskFilter { ALL, WORK, PERSONAL, SHOPPING }

data class TaskListUiModel(
    val query: String = "",
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val state: UiState<List<Task>> = UiState.Loading,
    val todayTasks: List<Task> = emptyList(),
    val thisWeekTasks: List<Task> = emptyList(),
    val laterTasks: List<Task> = emptyList(),
    val overdueTasks: List<Task> = emptyList()
)
