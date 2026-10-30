package com.softx.app.data

import android.content.Context
import com.softx.app.data.room.PocketAppDatabase
import com.softx.app.data.room.PocketLedgerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * The device's durable accounting journal backed by Room Database.
 * Stores money in minor units (poisha/cents), never floating point,
 * preventing rounding drift over time.
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

internal class PocketLedgerStore(context: Context) {
    private val database = PocketAppDatabase.getInstance(context)
    private val dao = database.ledgerDao()

    fun record(request: PocketNativeTransactionSaveRequest, syncState: String) {
        if (request.clientTransactionId.isBlank()) return
        val entity = PocketLedgerEntity(
            clientTransactionId = request.clientTransactionId,
            mode = request.mode.trim().lowercase(),
            entryDate = request.entryDate,
            amountMinor = request.amount.toMinorUnits(),
            accountId = request.accountId,
            categoryKey = request.categoryKey,
            paymentMethod = request.paymentMethod,
            partyName = request.partyName,
            note = request.note,
            dueDate = request.dueDate,
            syncState = syncState,
            createdAtEpochMs = System.currentTimeMillis(),
        )
        dao.insertOrReplaceSync(entity)
    }

    fun markSynced(clientTransactionId: String) {
        if (clientTransactionId.isBlank()) return
        dao.markSyncedSync(clientTransactionId)
    }

    fun exportEntries(): List<PocketLocalLedgerEntry> {
        return dao.getAllSync().map { it.toLocalLedgerEntry() }
    }

    fun entriesForMonth(monthValue: String): List<PocketLocalLedgerEntry> {
        val normalizedMonth = monthValue.trim().takeIf { it.matches(Regex("\\d{4}-\\d{2}")) }
        if (normalizedMonth == null) return exportEntries()
        return dao.getAllSync()
            .filter { it.entryDate.startsWith(normalizedMonth) }
            .sortedWith(
                compareByDescending<PocketLedgerEntity> { it.entryDate }
                    .thenByDescending { it.createdAtEpochMs },
            )
            .map { it.toLocalLedgerEntry() }
    }

    fun restoreEntries(entries: List<PocketLocalLedgerEntry>) {
        if (entries.isEmpty()) return
        val entities = entries.map { PocketLedgerEntity.fromLocalLedgerEntry(it) }
        dao.insertAllSync(entities)
    }

    fun ledgerFlow(): Flow<List<PocketLocalLedgerEntry>> {
        return dao.getAllFlow().map { list -> list.map { it.toLocalLedgerEntry() } }
    }

    private fun Double.toMinorUnits(): Long = BigDecimal.valueOf(this)
        .movePointRight(2)
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact()
}
