package com.example.taskmanager.data.work

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.taskmanager.domain.repository.TaskRepository

class TaskFlowWorkerFactory(
    private val taskRepository: TaskRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        Log.d("WorkerFactory", "Trying to create: $workerClassName")
        return when (workerClassName) {
            DeletedTasksCleanupWorker::class.java.name ->
                DeletedTasksCleanupWorker(appContext, workerParameters, taskRepository)
            else -> null
        }
    }
}
