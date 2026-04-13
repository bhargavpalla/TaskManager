package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.model.Task

internal object TaskValidation {
    fun validateForCreateOrUpdate(task: Task, nowMillis: Long) {
        require(task.title.isNotBlank()) { "Task title cannot be blank." }
        require(task.dueDate >= nowMillis) { "Due date cannot be in the past." }
    }
}
