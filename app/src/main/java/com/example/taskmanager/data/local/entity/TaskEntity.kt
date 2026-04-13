package com.example.taskmanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["isDeleted", "updatedAt"]),
        Index(value = ["isDeleted", "priority"]),
        Index(value = ["dueDate"]),
        Index(value = ["title"]),
        Index(value = ["description"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
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
