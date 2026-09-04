package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DailySalesDao
import com.example.data.model.CashBoxDenomItemEntity
import com.example.data.model.CashBoxDirectEntryEntity
import com.example.data.model.CashBoxGroupEntity
import com.example.data.model.CashExpenseEntity
import com.example.data.model.DailyCategoryItemEntity
import com.example.data.model.DailyDirectEntryItemEntity
import com.example.data.model.DailySalesEntity
import com.example.data.model.SalesGroupEntity
import com.example.data.model.DailyReportArchiveEntity
import com.example.data.model.BackupSnapshotEntity

@Database(
    entities = [
        DailySalesEntity::class,
        SalesGroupEntity::class,
        DailyCategoryItemEntity::class,
        DailyDirectEntryItemEntity::class,
        CashBoxGroupEntity::class,
        CashBoxDenomItemEntity::class,
        CashBoxDirectEntryEntity::class,
        CashExpenseEntity::class,
        DailyReportArchiveEntity::class,
        BackupSnapshotEntity::class
    ],
    version = 15,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailySalesDao(): DailySalesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `backup_snapshots` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `formattedDateTime` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `totalRevenue` REAL NOT NULL,
                        `cashInBoxYer` REAL NOT NULL,
                        `cashInBoxSar` REAL NOT NULL,
                        `payloadJson` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_10_12 = object : Migration(10, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `backup_snapshots` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `formattedDateTime` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `totalRevenue` REAL NOT NULL,
                        `cashInBoxYer` REAL NOT NULL,
                        `cashInBoxSar` REAL NOT NULL,
                        `payloadJson` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ticket_direct_sales_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

