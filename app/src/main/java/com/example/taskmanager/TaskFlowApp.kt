package com.example.taskmanager

import android.app.Application
import androidx.work.Configuration
import com.example.taskmanager.data.local.db.DatabaseProvider
import com.example.taskmanager.data.repository.TaskRepositoryImpl
import com.example.taskmanager.data.work.TaskFlowWorkerFactory

class TaskFlowApp : Application(), Configuration.Provider {

    private val database by lazy(LazyThreadSafetyMode.NONE) {
        DatabaseProvider.getDatabase(this)
    }

    private val taskRepository by lazy(LazyThreadSafetyMode.NONE) {
        TaskRepositoryImpl(database.taskDao())
    }

    private val workerFactory by lazy(LazyThreadSafetyMode.NONE) {
        TaskFlowWorkerFactory(taskRepository)
    }

    private val appWorkManagerConfiguration by lazy(LazyThreadSafetyMode.NONE) {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override val workManagerConfiguration: Configuration
        get() = appWorkManagerConfiguration
}
