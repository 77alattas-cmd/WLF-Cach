package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing the single active daily direct sales record (سجل المبيعات المباشرة اليومي)
 */
@Entity(tableName = "daily_sales")
data class DailySalesEntity(
    @PrimaryKey
    val id: Long = 1L, // Single active day record ID
    val dateTimestamp: Long = System.currentTimeMillis(),
    val sellerName: String = "",
    val notes: String = "",
    val cashInBox: Double = 0.0, // مبلغ الصندوق بالريال اليمني
    val cashInBoxSaudi: Double = 0.0, // مبلغ الصندوق بالريال السعودي
    val exchangeRateSarToYer: Double = 380.0, // سعر صرف الريال السعودي إلى الريال اليمني (الافتراضي 380)
    val totalRevenue: Double = 0.0,
    val showCashDenominationsTable: Boolean = false, // خيار إظهار/إلغاء جدول فئات النقد بالصندوق
    val useEasternArabicNumerals: Boolean = false, // خيار نمط الأرقام (123 مقابل ١٢٣)
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Entity representing a Sales Group (e.g. "سند", "صيني", "انترنت", or custom groups)
 */
@Entity(tableName = "sales_groups")
data class SalesGroupEntity(
    @PrimaryKey
    val id: String, // e.g. "group_sanad", "group_chini", "group_internet", or UUID
    val name: String,
    val type: String, // "DENOMINATIONS", "DIRECT_ENTRY", or "CUSTOM_FIELDS"
    val isEnabled: Boolean = true,
    val orderIndex: Int = 0,
    val isDefault: Boolean = false,
    val isExcludedFromBalance: Boolean = false,
    val givenLabel: String = "المعطى",
    val addedLabel: String = "إضافة",
    val remainingLabel: String = "المتبقي",
    val defaultFormula: String = "TICKET_STANDARD"
)

/**
 * Entity representing each ticket category row in a DENOMINATIONS/CUSTOM_FIELDS group
 */
@Entity(
    tableName = "daily_sales_items",
    indices = [Index(value = ["groupId"])]
)
data class DailyCategoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: String = "group_sanad",
    val denomination: Int, // 500, 1000, 1500, 2000, 3000, 4000, 5000, etc.
    val given: Int = 0,     // المعطى
    val added: Int = 0,     // إضافة
    val remaining: Int = 0, // المتبقي
    val sold: Int = 0,      // المباع = (المعطى + إضافة) - المتبقي
    val total: Double = 0.0, // المجموع = المباع * الفئة
    val notes: String = "",  // ملاحظة خاصة بالفئة
    val customTitle: String = "",
    val formula: String = "TICKET_STANDARD",
    val orderIndex: Int = 0
)

/**
 * Entity representing a direct entry row in a DIRECT_ENTRY group (like "صيني")
 */
@Entity(
    tableName = "daily_direct_entries",
    indices = [Index(value = ["groupId"])]
)
data class DailyDirectEntryItemEntity(
    @PrimaryKey
    val id: String,
    val groupId: String = "group_chini",
    val title: String = "",
    val amount: Double = 0.0,
    val quantity: Int = 1,
    val notes: String = "",
    val orderIndex: Int = 0
)

/**
 * Entity representing a Cash Box Group (e.g. "فئات النقد", "حوالات وشبكة", or custom cash groups)
 */
@Entity(tableName = "cash_box_groups")
data class CashBoxGroupEntity(
    @PrimaryKey
    val id: String, // e.g. "cash_group_main" or UUID
    val name: String,
    val type: String, // "DENOMINATIONS", "DIRECT_ENTRY", or "EXPENSES"
    val isEnabled: Boolean = true,
    val orderIndex: Int = 0,
    val isDefault: Boolean = false
)

/**
 * Entity representing cash denomination counts in a Cash Box group
 */
@Entity(
    tableName = "cash_box_denom_items",
    indices = [Index(value = ["groupId"])]
)
data class CashBoxDenomItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: String,
    val denomination: Int,
    val count: Int = 0,
    val total: Double = 0.0,
    val notes: String = "",
    val orderIndex: Int = 0
)

/**
 * Entity representing direct cash entry items in a Cash Box group
 */
@Entity(
    tableName = "cash_box_direct_entries",
    indices = [Index(value = ["groupId"])]
)
data class CashBoxDirectEntryEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val title: String = "",
    val amount: Double = 0.0,
    val currencyCode: String = "YER",
    val isSar: Boolean = false,
    val customExchangeRate: Double? = null,
    val notes: String = "",
    val orderIndex: Int = 0
)

/**
 * Entity representing Cash Expenses/Deposits in Cash Box (المصروفات المخصومة أو الإيداعات المضافة للصندوق)
 */
@Entity(
    tableName = "cash_expenses",
    indices = [Index(value = ["timestamp"])]
)
data class CashExpenseEntity(
    @PrimaryKey
    val id: String,
    val title: String = "",
    val amount: Double = 0.0,
    val currencyCode: String = "YER",
    val isSar: Boolean = false,
    val isDeposit: Boolean = false, // true: إيداع/إضافة للصندوق، false: مصروف/خصم من الصندوق
    val customExchangeRate: Double? = null,
    val category: String = "عام",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0
)

/**
 * Entity representing archived daily sales reports for history and review
 */
@Entity(tableName = "daily_report_archives")
data class DailyReportArchiveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val dateString: String = "",
    val sellerName: String = "",
    val totalRevenue: Double = 0.0,
    val totalSoldTickets: Int = 0,
    val cashInBoxYer: Double = 0.0,
    val cashInBoxSar: Double = 0.0,
    val netCashYer: Double = 0.0,
    val otherCurrenciesTotalYer: Double = 0.0,
    val grossCashInBox: Double = 0.0,
    val notes: String = "",
    val detailsString: String = "" // Semicolon and comma separated details of categories
)

/**
 * Entity representing a timestamped backup snapshot that can be restored anytime
 */
@Entity(tableName = "backup_snapshots")
data class BackupSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDateTime: String = "", // e.g. "2026/08/29 - 09:42 ص"
    val title: String = "نسخة احتياطية",
    val notes: String = "",
    val totalRevenue: Double = 0.0,
    val cashInBoxYer: Double = 0.0,
    val cashInBoxSar: Double = 0.0,
    val payloadJson: String = "" // Serialized JSON of all tables & state
)

