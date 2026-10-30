package com.softx.app.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PocketLedgerDao {

    @Query("SELECT * FROM local_ledger_entries ORDER BY entry_date DESC, created_at_epoch_ms DESC")
    fun getAllFlow(): Flow<List<PocketLedgerEntity>>

    @Query("SELECT * FROM local_ledger_entries WHERE entry_date LIKE :monthPrefix || '%' ORDER BY entry_date DESC, created_at_epoch_ms DESC")
    fun getEntriesForMonthFlow(monthPrefix: String): Flow<List<PocketLedgerEntity>>

    @Query("SELECT * FROM local_ledger_entries WHERE entry_date LIKE :monthPrefix || '%' ORDER BY entry_date DESC, created_at_epoch_ms DESC")
    suspend fun getEntriesForMonth(monthPrefix: String): List<PocketLedgerEntity>

    @Query("SELECT * FROM local_ledger_entries ORDER BY created_at_epoch_ms ASC")
    suspend fun getAll(): List<PocketLedgerEntity>

    @Query("SELECT * FROM local_ledger_entries ORDER BY created_at_epoch_ms ASC")
    fun getAllSync(): List<PocketLedgerEntity>

    @Query("SELECT * FROM local_ledger_entries WHERE sync_state = 'pending' ORDER BY created_at_epoch_ms ASC")
    suspend fun getPendingSync(): List<PocketLedgerEntity>

    @Query("SELECT * FROM local_ledger_entries WHERE client_transaction_id = :id LIMIT 1")
    suspend fun getById(id: String): PocketLedgerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entry: PocketLedgerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<PocketLedgerEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllSync(entries: List<PocketLedgerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceSync(entry: PocketLedgerEntity)

    @Query("UPDATE local_ledger_entries SET sync_state = 'synced' WHERE client_transaction_id = :id")
    suspend fun markSynced(id: String)

    @Query("UPDATE local_ledger_entries SET sync_state = 'synced' WHERE client_transaction_id = :id")
    fun markSyncedSync(id: String)

    @Query("DELETE FROM local_ledger_entries WHERE client_transaction_id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM local_ledger_entries")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM local_ledger_entries")
    suspend fun count(): Int
}
