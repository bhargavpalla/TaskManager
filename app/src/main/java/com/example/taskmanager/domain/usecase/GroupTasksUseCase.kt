package com.example.taskmanager.domain.usecase

import com.example.taskmanager.domain.model.GroupedTasks
import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.util.TaskDateUtils

class GroupTasksUseCase {
    operator fun invoke(tasks: List<Task>): GroupedTasks {
        val overdue = tasks.filter { TaskDateUtils.isOverdue(it.dueDate) }
        val notOverdue = tasks.filter { !TaskDateUtils.isOverdue(it.dueDate) }
        val today = notOverdue.filter { TaskDateUtils.isToday(it.dueDate) }
        val thisWeek = notOverdue.filter { !TaskDateUtils.isToday(it.dueDate) && TaskDateUtils.isThisWeek(it.dueDate) }
        val later = notOverdue.filter { TaskDateUtils.isLater(it.dueDate) }

        return GroupedTasks(
            today = today,
            thisWeek = thisWeek,
            later = later,
            overdue = overdue
        )
    }
}
