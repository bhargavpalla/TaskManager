package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.model.TaskCategory

class FilterTasksUseCase {
    operator fun invoke(tasks: List<Task>, categoryFilter: TaskCategory?): List<Task> {
        return when (categoryFilter) {
            null -> tasks
            else -> tasks.filter { it.category == categoryFilter }
        }
    }
}
