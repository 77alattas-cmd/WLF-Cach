package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySalesDao {

    @Query("SELECT * FROM daily_sales WHERE id = 1 LIMIT 1")
    fun getDailySales(): Flow<DailySalesEntity?>

    @Query("SELECT * FROM sales_groups ORDER BY orderIndex ASC")
    fun getSalesGroups(): Flow<List<SalesGroupEntity>>

    @Query("SELECT * FROM daily_sales_items ORDER BY orderIndex ASC")
    fun getCategoryItems(): Flow<List<DailyCategoryItemEntity>>

    @Query("SELECT * FROM daily_direct_entries ORDER BY orderIndex ASC")
    fun getDirectEntries(): Flow<List<DailyDirectEntryItemEntity>>

    @Query("SELECT * FROM cash_box_groups ORDER BY orderIndex ASC")
    fun getCashBoxGroups(): Flow<List<CashBoxGroupEntity>>

    @Query("SELECT * FROM cash_box_denom_items ORDER BY orderIndex ASC")
    fun getCashDenomItems(): Flow<List<CashBoxDenomItemEntity>>

    @Query("SELECT * FROM cash_box_direct_entries ORDER BY orderIndex ASC")
    fun getCashDirectEntries(): Flow<List<CashBoxDirectEntryEntity>>

    @Query("SELECT * FROM cash_expenses ORDER BY orderIndex ASC, timestamp ASC")
    fun getCashExpenses(): Flow<List<CashExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySales(sales: DailySalesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesGroups(groups: List<SalesGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyItems(items: List<DailyCategoryItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDirectEntries(entries: List<DailyDirectEntryItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashBoxGroups(groups: List<CashBoxGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashDenomItems(items: List<CashBoxDenomItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashDirectEntries(entries: List<CashBoxDirectEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashExpenses(expenses: List<CashExpenseEntity>)

    @Query("DELETE FROM sales_groups")
    suspend fun clearSalesGroups()

    @Query("DELETE FROM daily_sales_items")
    suspend fun clearDailyItems()

    @Query("DELETE FROM daily_direct_entries")
    suspend fun clearDirectEntries()

    @Query("DELETE FROM cash_box_groups")
    suspend fun clearCashBoxGroups()

    @Query("DELETE FROM cash_box_denom_items")
    suspend fun clearCashDenomItems()

    @Query("DELETE FROM cash_box_direct_entries")
    suspend fun clearCashDirectEntries()

    @Query("DELETE FROM cash_expenses")
    suspend fun clearCashExpenses()

    @Transaction
    suspend fun saveFullDailySales(
        sales: DailySalesEntity,
        groups: List<SalesGroupEntity>,
        items: List<DailyCategoryItemEntity>,
        directEntries: List<DailyDirectEntryItemEntity>,
        cashGroups: List<CashBoxGroupEntity> = emptyList(),
        cashDenoms: List<CashBoxDenomItemEntity> = emptyList(),
        cashDirects: List<CashBoxDirectEntryEntity> = emptyList(),
        expenses: List<CashExpenseEntity> = emptyList()
    ) {
        insertDailySales(sales)
        clearSalesGroups()
        insertSalesGroups(groups)
        clearDailyItems()
        insertDailyItems(items)
        clearDirectEntries()
        insertDirectEntries(directEntries)
        clearCashBoxGroups()
        insertCashBoxGroups(cashGroups)
        clearCashDenomItems()
        insertCashDenomItems(cashDenoms)
        clearCashDirectEntries()
        insertCashDirectEntries(cashDirects)
        clearCashExpenses()
        insertCashExpenses(expenses)
    }

    @Transaction
    suspend fun resetDay(
        defaultDenominations: List<Int>,
        defaultCashDenominations: List<Int> = listOf(100, 200, 250, 500, 1000, 5000)
    ) {
        val newSales = DailySalesEntity(
            id = 1L,
            dateTimestamp = System.currentTimeMillis(),
            sellerName = "",
            notes = "",
            cashInBox = 0.0,
            totalRevenue = 0.0,
            showCashDenominationsTable = false,
            useEasternArabicNumerals = false,
            lastUpdated = System.currentTimeMillis()
        )
        insertDailySales(newSales)
        clearSalesGroups()
        
        val defaultGroups = listOf(
            SalesGroupEntity(
                id = "group_sanad",
                name = "سند",
                type = "DENOMINATIONS",
                isEnabled = true,
                orderIndex = 0,
                isDefault = true,
                isExcludedFromBalance = false,
                givenLabel = "المعطى",
                addedLabel = "إضافة",
                remainingLabel = "المتبقي",
                defaultFormula = "TICKET_STANDARD"
            ),
            SalesGroupEntity(
                id = "group_game_cards",
                name = "بطائق العاب",
                type = "DENOMINATIONS",
                isEnabled = false,
                orderIndex = 1,
                isDefault = true,
                isExcludedFromBalance = false,
                givenLabel = "المعطى",
                addedLabel = "إضافة",
                remainingLabel = "المتبقي",
                defaultFormula = "TICKET_STANDARD"
            ),
            SalesGroupEntity(
                id = "group_chini",
                name = "صيني",
                type = "DIRECT_ENTRY",
                isEnabled = false,
                orderIndex = 2,
                isDefault = true,
                isExcludedFromBalance = false
            ),
            SalesGroupEntity(
                id = "group_internet",
                name = "انترنت",
                type = "DENOMINATIONS",
                isEnabled = false,
                orderIndex = 3,
                isDefault = true,
                isExcludedFromBalance = true,
                givenLabel = "المعطى",
                addedLabel = "إضافة",
                remainingLabel = "المتبقي",
                defaultFormula = "TICKET_STANDARD"
            )
        )
        insertSalesGroups(defaultGroups)
        
        clearDailyItems()
        val defaultSanadItems = defaultDenominations.mapIndexed { index, denom ->
            DailyCategoryItemEntity(
                groupId = "group_sanad",
                denomination = denom,
                given = 0,
                added = 0,
                remaining = 0,
                sold = 0,
                total = 0.0,
                orderIndex = index
            )
        }
        val defaultGameCardItems = listOf(1000).mapIndexed { index, denom ->
            DailyCategoryItemEntity(
                groupId = "group_game_cards",
                denomination = denom,
                given = 0,
                added = 0,
                remaining = 0,
                sold = 0,
                total = 0.0,
                orderIndex = index
            )
        }
        val defaultInternetItems = defaultDenominations.mapIndexed { index, denom ->
            DailyCategoryItemEntity(
                groupId = "group_internet",
                denomination = denom,
                given = 0,
                added = 0,
                remaining = 0,
                sold = 0,
                total = 0.0,
                orderIndex = index
            )
        }
        insertDailyItems(defaultSanadItems + defaultGameCardItems + defaultInternetItems)

        clearDirectEntries()

        clearCashBoxGroups()
        clearCashDenomItems()
        clearCashDirectEntries()
        clearCashExpenses()
    }

    @Query("SELECT * FROM daily_report_archives ORDER BY dateTimestamp DESC")
    fun getDailyReportArchives(): Flow<List<DailyReportArchiveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyReportArchive(archive: DailyReportArchiveEntity)

    @Query("DELETE FROM daily_report_archives WHERE id = :id")
    suspend fun deleteDailyReportArchive(id: Long)

    @Query("DELETE FROM daily_report_archives")
    suspend fun clearDailyReportArchives()

    @Query("SELECT * FROM backup_snapshots ORDER BY timestamp DESC")
    fun getBackupSnapshots(): Flow<List<BackupSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupSnapshot(snapshot: BackupSnapshotEntity): Long

    @Query("DELETE FROM backup_snapshots WHERE id = :id")
    suspend fun deleteBackupSnapshot(id: Long)

    @Query("UPDATE backup_snapshots SET title = :newTitle WHERE id = :id")
    suspend fun updateBackupSnapshotTitle(id: Long, newTitle: String)

    @Query("DELETE FROM backup_snapshots WHERE timestamp < :cutoffTimestamp AND title NOT LIKE 'قبل %'")
    suspend fun deleteBackupSnapshotsOlderThan(cutoffTimestamp: Long)

    @Query("DELETE FROM backup_snapshots WHERE title LIKE 'نسخة طوارئ مؤقتة%'")
    suspend fun deleteCrashBackups()

    @Query("DELETE FROM backup_snapshots")
    suspend fun clearAllBackupSnapshots()
}
