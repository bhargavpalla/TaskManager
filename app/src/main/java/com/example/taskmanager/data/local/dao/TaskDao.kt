package com.example.taskmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskmanager.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query(
        """
        SELECT * FROM tasks
        WHERE isDeleted = 0
        ORDER BY 
            CASE priority
                WHEN 'HIGH' THEN 3
                WHEN 'MED' THEN 2
                WHEN 'LOW' THEN 1
                ELSE 0
            END DESC,
            updatedAt DESC
        """
    )
    fun observeActiveTasksByPriority(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE isDeleted = 0
          AND id IN (
            SELECT rowid
            FROM tasks_fts
            WHERE tasks_fts MATCH :query
          )
        ORDER BY
            CASE priority
                WHEN 'HIGH' THEN 3
                WHEN 'MED' THEN 2
                WHEN 'LOW' THEN 1
                ELSE 0
            END DESC,
            updatedAt DESC
        """
    )
    fun searchActiveTasksByPriority(query: String): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE isDeleted = 1
          AND deletedAt IS NOT NULL
        ORDER BY deletedAt DESC
        """
    )
    fun observeDeletedTasks(): Flow<List<TaskEntity>>

    @Query(
        """
        UPDATE tasks
        SET isDeleted = 1,
            deletedAt = :deletedAt,
            updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun softDeleteTask(id: Int, deletedAt: Long, updatedAt: Long)

    @Query(
        """
        UPDATE tasks
        SET isDeleted = 0,
            deletedAt = NULL,
            updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun restoreTask(id: Int, updatedAt: Long)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskPermanently(id: Int)

    @Query(
        """
        DELETE FROM tasks
        WHERE isDeleted = 1
          AND deletedAt IS NOT NULL
          AND deletedAt <= :expiryTime
        """
    )
    suspend fun deleteExpiredSoftDeletedTasks(expiryTime: Long): Int
}
