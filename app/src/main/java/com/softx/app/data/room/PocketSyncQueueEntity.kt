package com.softx.app.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ACID-compliant offline sync queue. Replaces file-based JSON queue to guarantee
 * zero lost transactions even if the OS terminates the app during offline writes.
 */
@Entity(
    tableName = "offline_sync_queue",
    indices = [
        Index(value = ["client_transaction_id"]),
        Index(value = ["created_at_epoch_ms"]),
    ],
)
data class PocketSyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "action_type")
    val actionType: String, // 'save_transaction', 'delete_transaction', etc.

    @ColumnInfo(name = "payload_json")
    val payloadJson: String,

    @ColumnInfo(name = "client_transaction_id")
    val clientTransactionId: String,

    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int = 0,

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_error")
    val lastError: String? = null,
)
