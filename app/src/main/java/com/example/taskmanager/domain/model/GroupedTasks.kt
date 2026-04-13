package com.example.taskmanager.domain.model

data class GroupedTasks(
    val today: List<Task>,
    val thisWeek: List<Task>,
    val later: List<Task>,
    val overdue: List<Task>
) {
    fun isCompletelyEmpty(): Boolean {
        return today.isEmpty() && thisWeek.isEmpty() && later.isEmpty() && overdue.isEmpty()
    }
}
