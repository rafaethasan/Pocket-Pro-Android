package com.softx.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * The device's durable accounting journal. It deliberately stores money in
 * minor units, never floating point, so a local total cannot drift over time.
 *
 * This is an append-only cache of newly created entries. The server remains the
 * authority while legacy records are being migrated to the same ledger model.
 */
data class PocketLocalLedgerEntry(
    val clientTransactionId: String,
    val mode: String,
    val entryDate: String,
    val amountMinor: Long,
    val accountId: Int,
    val categoryKey: String,
    val paymentMethod: String,
    val partyName: String,
    val note: String,
    val dueDate: String,
    val syncState: String,
    val createdAtEpochMs: Long,
)

internal class PocketLedgerStore(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {
    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE local_ledger_entries (
                client_transaction_id TEXT NOT NULL PRIMARY KEY,
                entry_mode TEXT NOT NULL,
                entry_date TEXT NOT NULL,
                amount_minor INTEGER NOT NULL CHECK(amount_minor > 0),
                account_id INTEGER NOT NULL,
                category_key TEXT NOT NULL,
                payment_method TEXT NOT NULL,
                party_name TEXT NOT NULL DEFAULT '',
                note TEXT NOT NULL DEFAULT '',
                due_date TEXT NOT NULL DEFAULT '',
                sync_state TEXT NOT NULL CHECK(sync_state IN ('pending', 'synced')),
                created_at_epoch_ms INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        database.execSQL(
            "CREATE INDEX idx_local_ledger_entries_sync_state ON local_ledger_entries(sync_state, entry_date)",
        )
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun record(request: PocketNativeTransactionSaveRequest, syncState: String) {
        if (request.clientTransactionId.isBlank()) return
        val values = ContentValues().apply {
            put("client_transaction_id", request.clientTransactionId)
            put("entry_mode", request.mode.trim().lowercase())
            put("entry_date", request.entryDate)
            put("amount_minor", request.amount.toMinorUnits())
            put("account_id", request.accountId)
            put("category_key", request.categoryKey)
            put("payment_method", request.paymentMethod)
            put("party_name", request.partyName)
            put("note", request.note)
            put("due_date", request.dueDate)
            put("sync_state", syncState)
            put("created_at_epoch_ms", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(
            "local_ledger_entries",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE,
        )
    }

    fun markSynced(clientTransactionId: String) {
        if (clientTransactionId.isBlank()) return
        val values = ContentValues().apply { put("sync_state", "synced") }
        writableDatabase.update(
            "local_ledger_entries",
            values,
            "client_transaction_id = ?",
            arrayOf(clientTransactionId),
        )
    }

    fun exportEntries(): List<PocketLocalLedgerEntry> {
        return queryEntries(
            "SELECT client_transaction_id, entry_mode, entry_date, amount_minor, account_id, category_key, payment_method, party_name, note, due_date, sync_state, created_at_epoch_ms FROM local_ledger_entries ORDER BY created_at_epoch_ms ASC",
            null,
        )
    }

    fun entriesForMonth(monthValue: String): List<PocketLocalLedgerEntry> {
        val normalizedMonth = monthValue.trim().takeIf { it.matches(Regex("\\d{4}-\\d{2}")) }
        if (normalizedMonth == null) return exportEntries()
        return queryEntries(
            "SELECT client_transaction_id, entry_mode, entry_date, amount_minor, account_id, category_key, payment_method, party_name, note, due_date, sync_state, created_at_epoch_ms FROM local_ledger_entries WHERE entry_date LIKE ? ORDER BY entry_date DESC, created_at_epoch_ms DESC",
            arrayOf("$normalizedMonth%"),
        )
    }

    private fun queryEntries(sql: String, arguments: Array<String>?): List<PocketLocalLedgerEntry> {
        return readableDatabase.rawQuery(sql, arguments).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        PocketLocalLedgerEntry(
                            clientTransactionId = cursor.getString(0),
                            mode = cursor.getString(1),
                            entryDate = cursor.getString(2),
                            amountMinor = cursor.getLong(3),
                            accountId = cursor.getInt(4),
                            categoryKey = cursor.getString(5),
                            paymentMethod = cursor.getString(6),
                            partyName = cursor.getString(7),
                            note = cursor.getString(8),
                            dueDate = cursor.getString(9),
                            syncState = cursor.getString(10),
                            createdAtEpochMs = cursor.getLong(11),
                        ),
                    )
                }
            }
        }
    }

    fun restoreEntries(entries: List<PocketLocalLedgerEntry>) {
        if (entries.isEmpty()) return
        writableDatabase.beginTransaction()
        try {
            entries.forEach { item ->
                val values = ContentValues().apply {
                    put("client_transaction_id", item.clientTransactionId)
                    put("entry_mode", item.mode)
                    put("entry_date", item.entryDate)
                    put("amount_minor", item.amountMinor)
                    put("account_id", item.accountId)
                    put("category_key", item.categoryKey)
                    put("payment_method", item.paymentMethod)
                    put("party_name", item.partyName)
                    put("note", item.note)
                    put("due_date", item.dueDate)
                    put("sync_state", item.syncState)
                    put("created_at_epoch_ms", item.createdAtEpochMs)
                }
                writableDatabase.insertWithOnConflict(
                    "local_ledger_entries",
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE,
                )
            }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    private fun Double.toMinorUnits(): Long = BigDecimal.valueOf(this)
        .movePointRight(2)
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact()

    private companion object {
        const val DATABASE_NAME = "pocket-ledger.db"
        const val DATABASE_VERSION = 1
    }
}
