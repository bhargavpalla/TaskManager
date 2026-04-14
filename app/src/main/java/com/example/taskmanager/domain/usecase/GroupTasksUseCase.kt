package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.model.GroupedTasks
import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.util.TaskDateUtils

class GroupTasksUseCase {
    operator fun invoke(tasks: List<Task>): GroupedTasks {
        val overdue = mutableListOf<Task>()
        val today = mutableListOf<Task>()
        val thisWeek = mutableListOf<Task>()
        val later = mutableListOf<Task>()

        tasks.forEach { task ->
             when {
                TaskDateUtils.isOverdue(task.dueDate) -> {
                    overdue += task
                    "overdue"
                }
                TaskDateUtils.isToday(task.dueDate) -> {
                    today += task
                    "today"
                }
                TaskDateUtils.isThisWeek(task.dueDate) -> {
                    thisWeek += task
                    "thisWeek"
                }
                else -> {
                    later += task
                    "later"
                }
            }
        }

        return GroupedTasks(
            today = today,
            thisWeek = thisWeek,
            later = later,
            overdue = overdue
        )
    }
}
