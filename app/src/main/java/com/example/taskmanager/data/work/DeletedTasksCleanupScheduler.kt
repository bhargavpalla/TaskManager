package com.example.taskmanager.data.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object DeletedTasksCleanupScheduler {
    private const val PERIODIC_WORK_NAME = "deleted_tasks_periodic_cleanup"
    private const val ON_OPEN_WORK_NAME = "deleted_tasks_on_open_cleanup"

    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<DeletedTasksCleanupWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )

        val onOpenRequest = OneTimeWorkRequestBuilder<DeletedTasksCleanupWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            ON_OPEN_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            onOpenRequest
        )
    }
}
