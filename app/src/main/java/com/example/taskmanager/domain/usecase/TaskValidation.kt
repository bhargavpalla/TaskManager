package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.util.TaskDateUtils

internal object TaskValidation {
    fun validateForCreateOrUpdate(task: Task, @Suppress("UNUSED_PARAMETER") nowMillis: Long) {
        require(task.title.isNotBlank()) { "Task title cannot be blank." }
        val startOfToday = TaskDateUtils.startOfTodayMillis()
        require(task.dueDate >= startOfToday) { "Due date cannot be in the past." }
    }
}
