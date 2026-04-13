package com.example.taskmanager.data.mapper

import com.example.taskmanager.data.local.entity.TaskCategory as EntityTaskCategory
import com.example.taskmanager.data.local.entity.TaskEntity
import com.example.taskmanager.data.local.entity.TaskPriority as EntityTaskPriority
import com.example.taskmanager.data.local.entity.TaskStatus as EntityTaskStatus
import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.model.TaskCategory
import com.example.taskmanager.domain.model.TaskPriority
import com.example.taskmanager.domain.model.TaskStatus

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    category = when (category) {
        EntityTaskCategory.WORK -> TaskCategory.WORK
        EntityTaskCategory.PERSONAL -> TaskCategory.PERSONAL
        EntityTaskCategory.SHOPPING -> TaskCategory.SHOPPING
    },
    status = when (status) {
        EntityTaskStatus.PENDING -> TaskStatus.PENDING
        EntityTaskStatus.INPROGRESS -> TaskStatus.INPROGRESS
        EntityTaskStatus.COMPLETED -> TaskStatus.COMPLETED
    },
    dueDate = dueDate,
    priority = when (priority) {
        EntityTaskPriority.HIGH -> TaskPriority.HIGH
        EntityTaskPriority.MED -> TaskPriority.MED
        EntityTaskPriority.LOW -> TaskPriority.LOW
    },
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    category = when (category) {
        TaskCategory.WORK -> EntityTaskCategory.WORK
        TaskCategory.PERSONAL -> EntityTaskCategory.PERSONAL
        TaskCategory.SHOPPING -> EntityTaskCategory.SHOPPING
    },
    status = when (status) {
        TaskStatus.PENDING -> EntityTaskStatus.PENDING
        TaskStatus.INPROGRESS -> EntityTaskStatus.INPROGRESS
        TaskStatus.COMPLETED -> EntityTaskStatus.COMPLETED
    },
    dueDate = dueDate,
    priority = when (priority) {
        TaskPriority.HIGH -> EntityTaskPriority.HIGH
        TaskPriority.MED -> EntityTaskPriority.MED
        TaskPriority.LOW -> EntityTaskPriority.LOW
    },
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)
