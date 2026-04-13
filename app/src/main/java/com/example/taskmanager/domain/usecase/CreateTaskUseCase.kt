package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.repository.TaskRepository

class CreateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        task: Task,
        nowMillis: Long = System.currentTimeMillis()
    ): Int {
        TaskValidation.validateForCreateOrUpdate(task, nowMillis)
        return taskRepository.createTask(task)
    }
}
