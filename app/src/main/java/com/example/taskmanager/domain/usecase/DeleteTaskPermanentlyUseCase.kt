package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.repository.TaskRepository

class DeleteTaskPermanentlyUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int) {
        require(taskId > 0) { "Task id must be greater than 0 for permanent delete." }
        taskRepository.deleteTaskPermanently(taskId)
    }
}
