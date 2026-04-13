package com.example.taskmanager.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskmanager.domain.repository.TaskRepository
import java.util.concurrent.TimeUnit

class DeletedTasksCleanupWorker(
    appContext: Context,
    params: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val expiryTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            taskRepository.deleteExpiredSoftDeletedTasks(expiryTime)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
