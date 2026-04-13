package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class SearchTasksUseCase(
    private val taskRepository: TaskRepository,
    private val getTasksUseCase: GetTasksUseCase
) {
    operator fun invoke(rawQuery: String): Flow<List<Task>> {
        val normalizedQuery = normalizeQuery(rawQuery)
        if (normalizedQuery.isBlank()) {
            return getTasksUseCase()
        }
        return taskRepository.searchActiveTasksByPriority(normalizedQuery)
    }

    private fun normalizeQuery(query: String): String {
        return query.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(" ") { token -> "${token.replace("\"", "")}*" }
    }
}
