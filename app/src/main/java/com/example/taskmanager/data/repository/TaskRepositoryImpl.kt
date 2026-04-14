package com.example.taskmanager.data.repository

import com.example.taskmanager.data.local.dao.TaskDao
import com.example.taskmanager.data.mapper.toDomain
import com.example.taskmanager.data.mapper.toEntity
import com.example.taskmanager.domain.model.Task
import com.example.taskmanager.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TaskRepository {

    override fun observeActiveTasksByPriority(): Flow<List<Task>> =
        taskDao.observeActiveTasksByPriority()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override fun searchActiveTasksByPriority(query: String): Flow<List<Task>> =
        taskDao.searchActiveTasksByPriority(query)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override fun observeDeletedTasks(): Flow<List<Task>> =
        taskDao.observeDeletedTasks()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun createTask(task: Task): Int =
        withContext(ioDispatcher) {
            taskDao.insertTask(task.toEntity()).toInt()
        }

    override suspend fun updateTask(task: Task) {
        withContext(ioDispatcher) {
            taskDao.updateTask(task.toEntity())
        }
    }

    override suspend fun softDeleteTask(id: Int, deletedAt: Long, updatedAt: Long) {
        withContext(ioDispatcher) {
            taskDao.softDeleteTask(id = id, deletedAt = deletedAt, updatedAt = updatedAt)
        }
    }

    override suspend fun restoreTask(id: Int, updatedAt: Long) {
        withContext(ioDispatcher) {
            taskDao.restoreTask(id = id, updatedAt = updatedAt)
        }
    }

    override suspend fun deleteTaskPermanently(id: Int) {
        withContext(ioDispatcher) {
            taskDao.deleteTaskPermanently(id)
        }
    }

    override suspend fun deleteExpiredSoftDeletedTasks(expiryTime: Long): Int =
        withContext(ioDispatcher) {
            taskDao.deleteExpiredSoftDeletedTasks(expiryTime)
        }
}
