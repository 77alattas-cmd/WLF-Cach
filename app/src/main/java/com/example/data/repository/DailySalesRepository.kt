package com.example.data.repository

import com.example.data.dao.DailySalesDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class DailySalesRepository(private val dao: DailySalesDao) {

    val dailySales: Flow<DailySalesEntity?> = dao.getDailySales()
    val salesGroups: Flow<List<SalesGroupEntity>> = dao.getSalesGroups()
    val categoryItems: Flow<List<DailyCategoryItemEntity>> = dao.getCategoryItems()
    val directEntries: Flow<List<DailyDirectEntryItemEntity>> = dao.getDirectEntries()
    val cashBoxGroups: Flow<List<CashBoxGroupEntity>> = dao.getCashBoxGroups()
    val cashDenomItems: Flow<List<CashBoxDenomItemEntity>> = dao.getCashDenomItems()
    val cashDirectEntries: Flow<List<CashBoxDirectEntryEntity>> = dao.getCashDirectEntries()
    val cashExpenses: Flow<List<CashExpenseEntity>> = dao.getCashExpenses()

    suspend fun insertOrUpdateDailySales(sales: DailySalesEntity) = dao.insertDailySales(sales)
    suspend fun insertOrUpdateSalesGroups(groups: List<SalesGroupEntity>) = dao.insertSalesGroups(groups)
    suspend fun insertOrUpdateCategoryItems(items: List<DailyCategoryItemEntity>) = dao.insertDailyItems(items)
    suspend fun insertOrUpdateDirectEntries(entries: List<DailyDirectEntryItemEntity>) = dao.insertDirectEntries(entries)
    suspend fun insertOrUpdateCashBoxGroups(groups: List<CashBoxGroupEntity>) = dao.insertCashBoxGroups(groups)
    suspend fun insertOrUpdateCashDenoms(items: List<CashBoxDenomItemEntity>) = dao.insertCashDenomItems(items)
    suspend fun insertOrUpdateCashDirects(entries: List<CashBoxDirectEntryEntity>) = dao.insertCashDirectEntries(entries)
    suspend fun insertOrUpdateExpenses(expenses: List<CashExpenseEntity>) = dao.insertCashExpenses(expenses)

    suspend fun saveDailySales(
        sales: DailySalesEntity,
        groups: List<SalesGroupEntity>,
        items: List<DailyCategoryItemEntity>,
        directEntries: List<DailyDirectEntryItemEntity>,
        cashGroups: List<CashBoxGroupEntity> = emptyList(),
        cashDenoms: List<CashBoxDenomItemEntity> = emptyList(),
        cashDirects: List<CashBoxDirectEntryEntity> = emptyList(),
        expenses: List<CashExpenseEntity> = emptyList()
    ) {
        dao.saveFullDailySales(sales, groups, items, directEntries, cashGroups, cashDenoms, cashDirects, expenses)
    }

    suspend fun resetDay(
        defaultDenominations: List<Int>,
        defaultCashDenominations: List<Int> = listOf(100, 200, 250, 500, 1000, 5000)
    ) {
        dao.resetDay(defaultDenominations, defaultCashDenominations)
    }

    val dailyReportArchives: Flow<List<DailyReportArchiveEntity>> = dao.getDailyReportArchives()

    suspend fun insertDailyReportArchive(archive: DailyReportArchiveEntity) {
        dao.insertDailyReportArchive(archive)
    }

    suspend fun deleteDailyReportArchive(id: Long) {
        dao.deleteDailyReportArchive(id)
    }

    suspend fun clearDailyReportArchives() {
        dao.clearDailyReportArchives()
    }

    val backupSnapshots: Flow<List<BackupSnapshotEntity>> = dao.getBackupSnapshots()

    suspend fun insertBackupSnapshot(snapshot: BackupSnapshotEntity): Long {
        return dao.insertBackupSnapshot(snapshot)
    }

    suspend fun deleteBackupSnapshot(id: Long) {
        dao.deleteBackupSnapshot(id)
    }

    suspend fun updateBackupSnapshotTitle(id: Long, newTitle: String) {
        dao.updateBackupSnapshotTitle(id, newTitle)
    }

    suspend fun deleteBackupSnapshotsOlderThan(cutoffTimestamp: Long) {
        dao.deleteBackupSnapshotsOlderThan(cutoffTimestamp)
    }

    suspend fun deleteCrashBackups() {
        dao.deleteCrashBackups()
    }

    suspend fun clearAllBackupSnapshots() {
        dao.clearAllBackupSnapshots()
    }
}
