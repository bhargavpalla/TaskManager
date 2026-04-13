package com.example.taskmanager.domain.model

data class Task(
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: TaskCategory,
    val status: TaskStatus,
    val dueDate: Long,
    val priority: TaskPriority,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

enum class TaskCategory {
    WORK,
    PERSONAL,
    SHOPPING
}

enum class TaskStatus {
    PENDING,
    INPROGRESS,
    COMPLETED
}

enum class TaskPriority {
    HIGH,
    MED,
    LOW
}
