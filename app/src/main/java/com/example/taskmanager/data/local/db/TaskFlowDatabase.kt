package com.example.taskmanager.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.taskmanager.data.local.converter.TaskTypeConverters
import com.example.taskmanager.data.local.dao.TaskDao
import com.example.taskmanager.data.local.entity.TaskEntity
import com.example.taskmanager.data.local.entity.TaskFtsEntity

@Database(
    entities = [TaskEntity::class, TaskFtsEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(TaskTypeConverters::class)
abstract class TaskFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
