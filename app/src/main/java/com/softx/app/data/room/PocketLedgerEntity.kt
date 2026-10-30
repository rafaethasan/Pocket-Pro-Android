package com.softx.app.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.softx.app.data.PocketLocalLedgerEntry

/**
 * Durable accounting ledger entry backed by Room.
 * Uses exact table name and column names from legacy SQLite schema for seamless migration.
 * Monetary amounts are strictly stored in minor units (cents / poisha) to prevent floating-point drift.
 */
@Entity(
    tableName = "local_ledger_entries",
    indices = [
        Index(value = ["sync_state", "entry_date"]),
        Index(value = ["entry_date"]),
    ],
)
data class PocketLedgerEntity(
    @PrimaryKey
    @ColumnInfo(name = "client_transaction_id")
    val clientTransactionId: String,

    @ColumnInfo(name = "entry_mode")
    val mode: String,

    @ColumnInfo(name = "entry_date")
    val entryDate: String,

    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long,

    @ColumnInfo(name = "account_id")
    val accountId: Int,

    @ColumnInfo(name = "category_key")
    val categoryKey: String,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,

    @ColumnInfo(name = "party_name", defaultValue = "")
    val partyName: String = "",

    @ColumnInfo(name = "note", defaultValue = "")
    val note: String = "",

    @ColumnInfo(name = "due_date", defaultValue = "")
    val dueDate: String = "",

    @ColumnInfo(name = "sync_state")
    val syncState: String, // 'pending' | 'synced'

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
) {
    fun toLocalLedgerEntry(): PocketLocalLedgerEntry {
        return PocketLocalLedgerEntry(
            clientTransactionId = clientTransactionId,
            mode = mode,
            entryDate = entryDate,
            amountMinor = amountMinor,
            accountId = accountId,
            categoryKey = categoryKey,
            paymentMethod = paymentMethod,
            partyName = partyName,
            note = note,
            dueDate = dueDate,
            syncState = syncState,
            createdAtEpochMs = createdAtEpochMs,
        )
    }

    companion object {
        fun fromLocalLedgerEntry(entry: PocketLocalLedgerEntry): PocketLedgerEntity {
            return PocketLedgerEntity(
                clientTransactionId = entry.clientTransactionId,
                mode = entry.mode,
                entryDate = entryDateNormalize(entry.entryDate),
                amountMinor = entry.amountMinor,
                accountId = entry.accountId,
                categoryKey = entry.categoryKey,
                paymentMethod = entry.paymentMethod,
                partyName = entry.partyName,
                note = entry.note,
                dueDate = entry.dueDate,
                syncState = entry.syncState,
                createdAtEpochMs = entry.createdAtEpochMs,
            )
        }

        private fun entryDateNormalize(raw: String): String = raw.trim()
    }
}
