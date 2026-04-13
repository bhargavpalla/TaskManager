package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.repository.TaskRepository

class RestoreTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int, nowMillis: Long = System.currentTimeMillis()) {
        require(taskId > 0) { "Task id must be greater than 0 for restore." }
        taskRepository.restoreTask(id = taskId, updatedAt = nowMillis)
    }
}
