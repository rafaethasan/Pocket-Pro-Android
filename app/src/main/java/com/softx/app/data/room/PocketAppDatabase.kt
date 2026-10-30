package com.softx.app.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PocketLedgerEntity::class,
        PocketSyncQueueEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class PocketAppDatabase : RoomDatabase() {

    abstract fun ledgerDao(): PocketLedgerDao
    abstract fun syncQueueDao(): PocketSyncQueueDao

    companion object {
        const val DATABASE_NAME = "pocket-ledger.db"

        @Volatile
        private var INSTANCE: PocketAppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `offline_sync_queue` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `action_type` TEXT NOT NULL,
                        `payload_json` TEXT NOT NULL,
                        `client_transaction_id` TEXT NOT NULL,
                        `attempt_count` INTEGER NOT NULL DEFAULT 0,
                        `created_at_epoch_ms` INTEGER NOT NULL,
                        `last_error` TEXT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_offline_sync_queue_client_transaction_id` ON `offline_sync_queue` (`client_transaction_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_offline_sync_queue_created_at_epoch_ms` ON `offline_sync_queue` (`created_at_epoch_ms`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_local_ledger_entries_entry_date` ON `local_ledger_entries` (`entry_date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_local_ledger_entries_sync_state_entry_date` ON `local_ledger_entries` (`sync_state`, `entry_date`)")
            }
        }

        fun getInstance(context: Context): PocketAppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): PocketAppDatabase {
            return Room.databaseBuilder(
                context,
                PocketAppDatabase::class.java,
                DATABASE_NAME,
            )
                .addMigrations(MIGRATION_1_2)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
