package com.example.taskmanager.domain.repository

import com.example.taskmanager.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeActiveTasksByPriority(): Flow<List<Task>>
    fun searchActiveTasksByPriority(query: String): Flow<List<Task>>
    fun observeDeletedTasks(): Flow<List<Task>>
    suspend fun createTask(task: Task): Int
    suspend fun updateTask(task: Task)
    suspend fun softDeleteTask(id: Int, deletedAt: Long, updatedAt: Long)
    suspend fun restoreTask(id: Int, updatedAt: Long)
    suspend fun deleteTaskPermanently(id: Int)
    suspend fun deleteExpiredSoftDeletedTasks(expiryTime: Long): Int
}
