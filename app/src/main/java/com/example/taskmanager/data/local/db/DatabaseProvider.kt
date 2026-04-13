package com.example.taskmanager.data.local.db

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: TaskFlowDatabase? = null

    fun getDatabase(context: Context): TaskFlowDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                TaskFlowDatabase::class.java,
                "taskflow.db"
            ).build().also { db ->
                instance = db
            }
        }
    }
}
