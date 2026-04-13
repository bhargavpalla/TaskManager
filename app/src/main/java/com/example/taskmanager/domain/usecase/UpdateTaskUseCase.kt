package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.repository.TaskRepository

class UpdateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        task: Task,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        validate(task, nowMillis)
        taskRepository.updateTask(task)
    }

    private fun validate(task: Task, nowMillis: Long) {
        require(task.id > 0) { "Task id must be greater than 0 for update." }
        TaskValidation.validateForCreateOrUpdate(task, nowMillis)
    }
}
