package com.example.taskmanager.data.local.converter

import androidx.room.TypeConverter
import com.example.taskmanager.data.local.entity.TaskCategory
import com.example.taskmanager.data.local.entity.TaskPriority
import com.example.taskmanager.data.local.entity.TaskStatus

class TaskTypeConverters {

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory = TaskCategory.valueOf(value)

    @TypeConverter
    fun fromTaskCategory(category: TaskCategory): String = category.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String = priority.name
}
