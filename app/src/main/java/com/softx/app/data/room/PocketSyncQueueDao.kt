package com.softx.app.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PocketSyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: PocketSyncQueueEntity): Long

    @Query("SELECT * FROM offline_sync_queue ORDER BY id ASC")
    suspend fun getPendingItems(): List<PocketSyncQueueEntity>

    @Query("DELETE FROM offline_sync_queue WHERE id = :id")
    suspend fun remove(id: Long)

    @Query("DELETE FROM offline_sync_queue WHERE client_transaction_id = :clientTransactionId")
    suspend fun removeByClientTxId(clientTransactionId: String)

    @Query("UPDATE offline_sync_queue SET attempt_count = attempt_count + 1, last_error = :error WHERE id = :id")
    suspend fun recordFailure(id: Long, error: String)

    @Query("SELECT COUNT(*) FROM offline_sync_queue")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM offline_sync_queue")
    suspend fun getPendingCount(): Int

    @Query("DELETE FROM offline_sync_queue")
    suspend fun clearQueue()
}
