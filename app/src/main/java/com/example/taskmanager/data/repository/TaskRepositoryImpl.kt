package com.example.taskmanager.data.repository

import com.example.taskmanager.data.local.dao.TaskDao
import com.example.taskmanager.data.mapper.toDomain
import com.example.taskmanager.data.mapper.toEntity
import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun observeActiveTasksByPriority(): Flow<List<Task>> =
        taskDao.observeActiveTasksByPriority()
            .map { entities -> entities.map { it.toDomain() } }

    override fun searchActiveTasksByPriority(query: String): Flow<List<Task>> =
        taskDao.searchActiveTasksByPriority(query)
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeDeletedTasks(): Flow<List<Task>> =
        taskDao.observeDeletedTasks().map { entities -> entities.map { it.toDomain() } }

    override suspend fun createTask(task: Task): Int =
        taskDao.insertTask(task.toEntity()).toInt()

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun softDeleteTask(id: Int, deletedAt: Long, updatedAt: Long) {
        taskDao.softDeleteTask(id = id, deletedAt = deletedAt, updatedAt = updatedAt)
    }

    override suspend fun restoreTask(id: Int, updatedAt: Long) {
        taskDao.restoreTask(id = id, updatedAt = updatedAt)
    }

    override suspend fun deleteTaskPermanently(id: Int) {
        taskDao.deleteTaskPermanently(id)
    }

    override suspend fun deleteExpiredSoftDeletedTasks(expiryTime: Long): Int =
        taskDao.deleteExpiredSoftDeletedTasks(expiryTime)
}
