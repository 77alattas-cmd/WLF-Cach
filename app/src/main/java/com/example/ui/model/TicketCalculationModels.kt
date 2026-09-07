package com.example.ui.model

import com.example.util.TafqeetHelper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Standard default categories for "سند" group: 500, 1000, 1500, 2000, 3000, 4000, 5000
 */
val DEFAULT_TICKET_CATEGORIES = listOf(500, 1000, 1500, 2000, 3000, 4000, 5000)
val DEFAULT_INTERNET_CATEGORIES = listOf(200, 500, 1000)
val DEFAULT_SHARABAT_CATEGORIES = listOf(1000, 1500, 2000)
val DEFAULT_GAME_CARDS_CATEGORIES = listOf(1000)
val AVAILABLE_DENOMINATION_OPTIONS = listOf(100, 200, 250, 500, 1000, 1500, 2000, 3000, 4000, 5000, 10000)

const val GROUP_SANAD_ID = "group_sanad"
const val GROUP_GAME_CARDS_ID = "group_game_cards"
const val GROUP_CHINI_ID = "group_chini"
const val GROUP_INTERNET_ID = "group_internet"
const val GROUP_SHARABAT_ID = "group_sharabat"

val DEFAULT_CASH_DENOMINATIONS_YER = listOf(100, 200, 500, 1000)
val DEFAULT_CASH_DENOMINATIONS_SAR = listOf(1, 5, 10, 20, 50, 100, 200, 500)
val DEFAULT_CASH_DENOMINATIONS = listOf(100, 200, 500, 1000)
const val CASH_GROUP_MAIN_ID = "cash_group_main"
const val CASH_GROUP_CURRENCIES_ID = "cash_group_currencies"
const val CASH_GROUP_EXPENSES_ID = "cash_group_expenses"

enum class SalesGroupType(val label: String) {
    DENOMINATIONS("سندات الفئات"),
    DIRECT_ENTRY("إدخال مباشر"),
    CUSTOM_FIELDS("حقول ومعادلات")
}

enum class CashGroupType(val label: String) {
    DENOMINATIONS("فئات النقد"),
    DIRECT_ENTRY("حسابات ومقبوضات مباشرة"),
    EXPENSES("مصاريف الصندوق"),
    DEPOSITS("إيداعات خارجية")
}

/**
 * Calculator Target Domain
 */
enum class CalcAppSection(val label: String) {
    SALES("المبيعات"),
    CASH_BOX("الصندوق")
}

/**
 * Field target identifier for numeric calculator navigation in Sales
 */
enum class TargetSalesField(val label: String) {
    GIVEN("المعطى"),
    ADDED("إضافة"),
    REMAINING("المتبقي")
}

/**
 * Field target identifier for numeric calculator navigation in Cash Box
 */
enum class TargetCashField(val label: String) {
    YER_CASH("النقد اليمني"),
    SAR_CASH("النقد السعودي"),
    EXCHANGE_RATE("سعر الصرف"),
    GROUP_DENOM_COUNT("عدد أوراق الفئة")
}

/**
 * Calculation Formula for Custom Groups and Rows
 */
enum class CalculationFormula(val label: String, val formulaText: String) {
    TICKET_STANDARD("حساب التذاكر القياسي", "(المعطى + الإضافة - المتبقي) × الفئة"),
    QUANTITY_PRICE("الكمية وسعر الوحدة", "الكمية × السعر"),
    GIVEN_MINUS_REMAINING("المعطى ناقص المتبقي", "(المعطى - المتبقي) × الفئة"),
    PERCENTAGE_COMMISSION("نسبة مئوية / عمولة", "المبلغ × (النسبة ÷ 100)"),
    SUM_DIRECT("تجميع مباشر", "المعطى + الإضافة")
}

/**
 * Audit log entry for tracking detailed inputs and changes
 */
data class AuditLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val section: String, // "المبيعات", "الصندوق", "الإعدادات"
    val groupName: String, // "سند", "نقدي", "مصاريف"
    val field: String, // "المعطى", "المتبقي", "المبلغ"
    val oldValue: String,
    val newValue: String,
    val details: String = ""
) {
    val formattedDateTime: String
        get() {
            val sdf = java.text.SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }

    val formattedDate: String
        get() {
            val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }

    val formattedTimeWithSeconds: String
        get() {
            val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
}

/**
 * Direct entry item for direct amount groups (no quantity column required)
 */
data class DirectEntryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val amountInput: String = "",
    val quantityInput: String = "1",
    val notes: String = "",
    val isDeposit: Boolean = false
) {
    val quantity: Int
        get() = quantityInput.trim().toIntOrNull() ?: 1

    val amount: Double
        get() = amountInput.trim().toDoubleOrNull() ?: 0.0

    val total: Double
        get() = amount

    val isFilled: Boolean
        get() = amountInput.isNotBlank() || title.isNotBlank()
}

/**
 * Live editable row for direct ticket sales (for DENOMINATIONS groups like "سند")
 */
data class DirectSalesRowUiState(
    val denomination: Int,
    val givenInput: String = "",
    val addedInput: String = "",
    val remainingInput: String = "",
    val notes: String = "",
    val customTitle: String = "",
    val isEnabled: Boolean = true,
    val formula: CalculationFormula = CalculationFormula.TICKET_STANDARD,
    val mergeAddedWithGiven: Boolean = false
) {
    val given: Int
        get() = givenInput.trim().toIntOrNull() ?: 0

    val added: Int
        get() = addedInput.trim().toIntOrNull() ?: 0

    val remaining: Int
        get() = remainingInput.trim().toIntOrNull() ?: 0

    // حساب المباع بناءً على المعادلة المحددة
    val sold: Int
        get() = when (formula) {
            CalculationFormula.TICKET_STANDARD -> {
                val totalGiven = given + added
                // احتساب المعطى والإضافي معاً في حالة وجود إضافي
                (totalGiven - remaining).coerceAtLeast(0)
            }
            CalculationFormula.GIVEN_MINUS_REMAINING -> ((given + added) - remaining).coerceAtLeast(0)
            CalculationFormula.SUM_DIRECT -> given + added
            CalculationFormula.QUANTITY_PRICE -> given
            CalculationFormula.PERCENTAGE_COMMISSION -> given
        }

    // حساب المجموع الإجمالي
    val total: Double
        get() = when (formula) {
            CalculationFormula.TICKET_STANDARD -> (sold * denomination).toDouble()
            CalculationFormula.GIVEN_MINUS_REMAINING -> (sold * denomination).toDouble()
            CalculationFormula.SUM_DIRECT -> (sold * denomination).toDouble()
            CalculationFormula.QUANTITY_PRICE -> (given * denomination).toDouble()
            CalculationFormula.PERCENTAGE_COMMISSION -> (given * (denomination / 100.0))
        }

    // التحقق من صحة المدخلات (المتبقي لا يتجاوز المعطى + الإضافي)
    val isRemainingExceeded: Boolean
        get() = if (formula == CalculationFormula.TICKET_STANDARD || formula == CalculationFormula.GIVEN_MINUS_REMAINING) {
            remaining > (given + added)
        } else false

    val isFilled: Boolean
        get() = givenInput.isNotBlank() || addedInput.isNotBlank() || remainingInput.isNotBlank()
}

/**
 * Represents a Sales Group in the top tab bar (e.g. "سند", "صيني", "انترنت", or custom groups)
 */
data class SalesGroupUiState(
    val id: String,
    val name: String,
    val type: SalesGroupType,
    val isEnabled: Boolean = true,
    val orderIndex: Int = 0,
    val isAddedFieldEnabled: Boolean = false, // حقل الإضافي معطّل تلقائياً واختياري
    val mergeAddedWithGiven: Boolean = false, // دمج قيم الإضافي مع المعطى
    val notes: String = "", // ملاحظات في الأعلى
    val rows: List<DirectSalesRowUiState> = DEFAULT_TICKET_CATEGORIES.map {
        DirectSalesRowUiState(denomination = it)
    },
    val directEntries: List<DirectEntryItem> = emptyList(),
    val isDefault: Boolean = false,
    val isExpanded: Boolean = true,
    val isExcludedFromBalance: Boolean = false,
    val addToReport: Boolean = true, // إضافة للتقرير
    val addToBalance: Boolean = !isExcludedFromBalance, // إضافة للرصيد والموازنة
    val givenLabel: String = "المعطى",
    val addedLabel: String = "إضافة",
    val remainingLabel: String = "المتبقي",
    val defaultFormula: CalculationFormula = CalculationFormula.TICKET_STANDARD,
    val color: Long? = null
) {
    /**
     * Common price/category for the entire group (e.g. for simple calculator)
     */
    val priceYer: Double
        get() = when(type) {
            SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> {
                activeRows.firstOrNull()?.denomination?.toDouble() ?: 0.0
            }
            SalesGroupType.DIRECT_ENTRY -> {
                directEntries.firstOrNull()?.amount ?: 0.0
            }
        }

    val activeRows: List<DirectSalesRowUiState>
        get() = rows.filter { it.isEnabled }

    val totalRevenue: Double
        get() = if (!isEnabled) 0.0 else when (type) {
            SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> activeRows.sumOf { it.total }
            SalesGroupType.DIRECT_ENTRY -> directEntries.sumOf { it.total }
        }

    val totalGiven: Int
        get() = if (!isEnabled) 0 else when (type) {
            SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> activeRows.sumOf { it.given }
            SalesGroupType.DIRECT_ENTRY -> 0
        }

    val totalAdded: Int
        get() = if (!isEnabled) 0 else when (type) {
            SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> activeRows.sumOf { it.added }
            SalesGroupType.DIRECT_ENTRY -> 0
        }

    val totalRemaining: Int
        get() = if (!isEnabled) 0 else when (type) {
            SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> activeRows.sumOf { it.remaining }
            SalesGroupType.DIRECT_ENTRY -> 0
        }

    val totalSold: Int
        get() = if (!isEnabled) 0 else when (type) {
            SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> activeRows.sumOf { it.sold }
            SalesGroupType.DIRECT_ENTRY -> directEntries.count { it.isFilled }
        }
}

/**
 * Live editable row for Cash Denominations count in Cash Box groups (عد أوراق النقد)
 */
data class CashDenomRowUiState(
    val denomination: Int,
    val countInput: String = "",
    val notes: String = "",
    val isEnabled: Boolean = true,
    val currency: String = "YER" // "YER" or "SAR"
) {
    val count: Int
        get() = countInput.trim().toIntOrNull() ?: 0

    val total: Double
        get() = (count * denomination).toDouble()

    val isFilled: Boolean
        get() = countInput.isNotBlank()
}

/**
 * Group Configuration snapshot in a Preset
 */
data class GroupPresetConfig(
    val groupId: String,
    val groupName: String,
    val isEnabled: Boolean = true,
    val enabledDenominations: List<Int> = emptyList()
)

/**
 * Saved Preset Configuration for Groups & Categories
 */
data class AccountingPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false,
    val groupConfigs: List<GroupPresetConfig> = emptyList()
)

/**
 * Dynamic custom currency model
 */
data class CustomCurrency(
    val code: String,
    val arabicName: String,
    val symbol: String,
    val defaultRateYer: Double,
    val flag: String = "🌐",
    val isMain: Boolean = false,
    val isCustom: Boolean = false
)

/**
 * Supported foreign currencies helper
 */
enum class AppCurrency(
    val code: String,
    val arabicName: String,
    val symbol: String,
    val defaultRateYer: Double,
    val flag: String
) {
    YER("YER", "ريال يمني", "ر.ي.", 1.0, "🇾🇪"),
    SAR("SAR", "ريال سعودي", "ر.س.", 380.0, "🇸🇦"),
    USD("USD", "دولار أمريكي", "$", 530.0, "🇺🇸"),
    AED("AED", "درهم إماراتي", "د.إ.", 144.0, "🇦🇪"),
    OMR("OMR", "ريال عماني", "ر.ع.", 1375.0, "🇴🇲"),
    KWD("KWD", "دينار كويتي", "د.ك.", 1720.0, "🇰🇼"),
    EUR("EUR", "يورو", "€", 570.0, "🇪🇺"),
    QAR("QAR", "ريال قطري", "ر.ق.", 145.0, "🇶🇦"),
    OTHER("OTHER", "عملة أخرى", "عملة", 1.0, "🌐");

    companion object {
        fun fromCode(code: String): AppCurrency {
            val upper = code.uppercase().trim()
            return entries.find { it.code == upper } ?: if (upper == "SAR") SAR else OTHER
        }
    }
}

/**
 * Direct entry item for Cash Box accounts (e.g. POS شبكة, حوالات, عملات أجنبية, درج إضافي)
 */
data class CashDirectEntryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val amountInput: String = "",
    val currencyCode: String = "YER", // YER, SAR, USD, AED, OMR, KWD, EUR, QAR, OTHER
    val isSar: Boolean = false,
    val customExchangeRateInput: String = "",
    val customExchangeRate: Double? = null,
    val notes: String = "",
    val isSuspended: Boolean = false,
    val deductFromCash: Boolean = true
) {
    val amount: Double
        get() = amountInput.trim().toDoubleOrNull() ?: 0.0

    val currency: AppCurrency
        get() = if (isSar && currencyCode == "YER") AppCurrency.SAR else AppCurrency.fromCode(currencyCode)

    val effectiveRate: Double?
        get() = customExchangeRateInput.trim().toDoubleOrNull() ?: customExchangeRate

    fun getTotalYer(defaultExchangeRate: Double): Double {
        if (isSuspended) return 0.0
        val curr = currency
        if (curr == AppCurrency.YER) return amount
        val baseRate = if (curr == AppCurrency.SAR) defaultExchangeRate else curr.defaultRateYer
        val appliedRate = effectiveRate ?: baseRate
        return amount * appliedRate
    }

    val isForeign: Boolean
        get() = currency != AppCurrency.YER

    val isFilled: Boolean
        get() = amountInput.isNotBlank() || title.isNotBlank()
}

/**
 * Expense or Deposit item in Cash Box (المصروفات المخصومة أو الإيداعات المضافة للصندوق)
 */
data class CashExpenseItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val amountInput: String = "",
    val currencyCode: String = "YER",
    val isSar: Boolean = false,
    val isDeposit: Boolean = false, // true: إيداع وإضافة للصندوق، false: مصروف وخصم من الصندوق
    val customExchangeRateInput: String = "",
    val customExchangeRate: Double? = null,
    val category: String = "عام",
    val notes: String = "",
    val isSuspended: Boolean = false, // موقوف (عند اختياره لا يخصم المبلغ من النقد الا عند إلغاء الاختيار)
    val deductFromCash: Boolean = true, // خصم من النقد (يخصم تلقائياً من النقد الصافي لضبط الموازنة)
    val timestamp: Long = System.currentTimeMillis()
) {
    val amount: Double
        get() = amountInput.trim().toDoubleOrNull() ?: 0.0

    val currency: AppCurrency
        get() = if (isSar && currencyCode == "YER") AppCurrency.SAR else AppCurrency.fromCode(currencyCode)

    val effectiveRate: Double?
        get() = customExchangeRateInput.trim().toDoubleOrNull() ?: customExchangeRate

    fun getTotalYer(defaultExchangeRate: Double): Double {
        if (isSuspended) return 0.0
        val curr = currency
        if (curr == AppCurrency.YER) return amount
        val baseRate = if (curr == AppCurrency.SAR) defaultExchangeRate else curr.defaultRateYer
        val appliedRate = effectiveRate ?: baseRate
        return amount * appliedRate
    }

    val isForeign: Boolean
        get() = currency != AppCurrency.YER

    val isFilled: Boolean
        get() = amountInput.isNotBlank() || title.isNotBlank()
}

/**
 * Represents a Cash Box Group in the Cash Box section (e.g. "نقدي", "مصاريف", "حوالات وشبكة")
 */
data class CashBoxGroupUiState(
    val id: String,
    val name: String,
    val type: CashGroupType,
    val isEnabled: Boolean = true,
    val isExpanded: Boolean = true,
    val orderIndex: Int = 0,
    val isDefault: Boolean = false,
    val isExcludedFromBalance: Boolean = false,
    val addToReport: Boolean = true, // إضافة للتقرير
    val addToBalance: Boolean = !isExcludedFromBalance, // إضافة للرصيد والموازنة
    val notes: String = "",
    val denomRows: List<CashDenomRowUiState> = DEFAULT_CASH_DENOMINATIONS.map { CashDenomRowUiState(denomination = it) },
    val directEntries: List<CashDirectEntryItem> = emptyList(),
    val color: Long? = null
) {
    val activeDenomRows: List<CashDenomRowUiState>
        get() = denomRows.filter { it.isEnabled }

    fun getTotalYer(exchangeRateSarToYer: Double, cashYerInput: Double = 0.0): Double {
        if (!isEnabled) return 0.0
        return when (type) {
            CashGroupType.DENOMINATIONS -> {
                val denomSum = activeDenomRows.sumOf {
                    if (it.currency == "SAR") it.total * exchangeRateSarToYer else it.total
                }
                if (id == CASH_GROUP_MAIN_ID && denomSum == 0.0 && cashYerInput > 0.0) cashYerInput else denomSum
            }
            CashGroupType.DIRECT_ENTRY -> directEntries.sumOf {
                it.getTotalYer(exchangeRateSarToYer)
            }
            CashGroupType.EXPENSES -> directEntries.sumOf {
                it.getTotalYer(exchangeRateSarToYer)
            }
            CashGroupType.DEPOSITS -> directEntries.sumOf {
                it.getTotalYer(exchangeRateSarToYer)
            }
        }
    }

    val totalCount: Int
        get() = if (!isEnabled) 0 else when (type) {
            CashGroupType.DENOMINATIONS -> activeDenomRows.sumOf { it.count }
            CashGroupType.DIRECT_ENTRY -> directEntries.count { it.isFilled }
            CashGroupType.EXPENSES -> directEntries.count { it.isFilled }
            CashGroupType.DEPOSITS -> directEntries.count { it.isFilled }
        }
}

/**
 * Balance Status enum: MATCHED (متطابق), DEFICIT (عجز), SURPLUS (فائض)
 */
enum class BalanceStatus(val label: String, val sign: String, val arrow: String) {
    MATCHED("متطابق", "", ""),
    DEFICIT("عجز", "-", "↓"),
    SURPLUS("فائض", "+", "↑");

    val arabicLabel: String
        get() = label
}

/**
 * Grand Totals across all enabled ticket/sales groups for the day
 */
data class DailySalesSummary(
    val totalGiven: Int = 0,
    val totalAdded: Int = 0,
    val totalRemaining: Int = 0,
    val totalSold: Int = 0,
    val totalRevenue: Double = 0.0, // مجموع المبيعات الداخلة في الموازنة بالريال اليمني (ر.ي.)
    val sideInternetRevenue: Double = 0.0, // مبيعات الإنترنت الجانبية المستقلة (ر.ي.)
    val totalSideRevenue: Double = 0.0, // إجمالي الإيرادات الجانبية المستقلة (ر.ي.)
    val cashInBoxYer: Double = 0.0, // النقد بالريال اليمني (ر.ي.)
    val cashInBoxSar: Double = 0.0, // النقد بالريال السعودي (ر.س.)
    val netCashYer: Double = 0.0, // النقد الصافي (النقد - المصروف) بالريال اليمني
    val exchangeRateSarToYer: Double = 380.0, // سعر الصرف 1 ر.س. = 380 ر.ي.
    val totalExpensesYer: Double = 0.0, // إجمالي المصروفات بالريال اليمني
    val totalExpensesSar: Double = 0.0, // إجمالي المصروفات بالريال السعودي
    val totalExpensesInYer: Double = 0.0, // إجمالي المصروفات محولة للريال اليمني
    val totalDepositsYer: Double = 0.0, // إجمالي الإيداعات بالريال اليمني
    val totalDepositsSar: Double = 0.0, // إجمالي الإيداعات بالريال السعودي
    val totalDepositsInYer: Double = 0.0, // إجمالي الإيداعات محولة للريال اليمني
    val otherCurrenciesTotalYer: Double = 0.0, // إجمالي العملات الأخرى محولة للريال اليمني (سعودي + فئات أخرى)
    val grossCashInBox: Double = 0.0, // إجمالي المقبوضات النقدية: ( (النقد - المصروف) + المصروف *2) + الإيداعات + العملات الأخرى
    val cashInBox: Double = 0.0, // صافي الصندوق الفعلي بعد خصم المصروفات: المقبوضات - المصروفات
    val balance: Double = 0.0, // المعادلة: مجموع المبيعات (الرئيسية) - الصندوق الفعلي
    val balanceStatus: BalanceStatus = BalanceStatus.MATCHED,
    val hasErrors: Boolean = false,
    val activeGroupsCount: Int = 0,
    val totalGroupsCount: Int = 0
) {
    val cashInBoxSaudi: Double
        get() = cashInBoxSar

    val cashInBoxSaudiEquivalentYer: Double
        get() = cashInBoxSar * exchangeRateSarToYer

    val cashInBoxSarInYer: Double
        get() = cashInBoxSar * exchangeRateSarToYer

    val balanceFormatted: String
        get() = "${balanceStatus.sign} ${AccountingFormatter.formatYer(balance)}"
}

/**
 * Category breakdown report entry for the day
 */
data class CategoryReportItem(
    val denomination: Int,
    val totalGiven: Int,
    val totalAdded: Int,
    val totalRemaining: Int,
    val totalSold: Int,
    val totalRevenue: Double,
    val groupName: String = "سند",
    val notes: String = "",
    val percentageOfRevenue: Double = 0.0
)

/**
 * Group Report Item for the summary breakdown
 */
data class GroupReportItem(
    val groupId: String,
    val groupName: String,
    val type: SalesGroupType,
    val isEnabled: Boolean,
    val totalRevenue: Double,
    val totalItemsOrTickets: Int,
    val percentageOfRevenue: Double = 0.0
)

/**
 * Position of the currency symbol relative to the number
 */
enum class CurrencySymbolPosition(val id: String, val titleAr: String, val exampleUsd: String, val exampleYer: String) {
    AUTO("AUTO", "تلقائي حسب العملة", "$ 1,250.00", "1,250 ر.ي."),
    AFTER("AFTER", "بعد المبلغ (يسار / يمين الرقم)", "1,250.00 $", "1,250 ر.ي."),
    BEFORE("BEFORE", "قبل المبلغ (نمط الدولار)", "$ 1,250.00", "ر.ي. 1,250")
}

/**
 * Decimal fraction display rules
 */
enum class CurrencyDecimalMode(val id: String, val titleAr: String, val exampleUsd: String, val exampleYer: String) {
    AUTO("AUTO", "تلقائي حسب العملة (الدولار: .00 / الريال: أرقام صحيحة)", "$ 1,250.00", "1,250 ر.ي."),
    ALWAYS_TWO("ALWAYS_TWO", "خانتان عشريتان دائماً (.00)", "$ 1,250.00", "1,250.00 ر.ي."),
    INTEGER_ONLY("INTEGER_ONLY", "أرقام صحيحة فقط (بدون كسور)", "$ 1,250", "1,250 ر.ي."),
    DYNAMIC_IF_FRACTION("DYNAMIC_IF_FRACTION", "إظهار الكسور فقط عند وجودها", "$ 1,250 أو 1,250.50", "1,250 أو 1,250.5")
}

/**
 * Thousands separator format
 */
enum class CurrencyThousandsSeparator(val id: String, val separator: String, val titleAr: String, val example: String) {
    COMMA("COMMA", ",", "فاصلة عادية (,)", "1,250,000"),
    SPACE("SPACE", " ", "مسافة فاصلة ( )", "1 250 000"),
    NONE("NONE", "", "بدون فواصل", "1250000")
}

/**
 * Currency display label format (Symbol, Code, Full Name)
 */
enum class CurrencyDisplayType(val id: String, val titleAr: String, val exampleUsd: String, val exampleYer: String) {
    SYMBOL("SYMBOL", "رمز العملة المختصر", "$", "ر.ي."),
    CODE("CODE", "كود العملة الدولي", "USD", "YER"),
    NAME("NAME", "اسم العملة العربي الكامل", "دولار أمريكي", "ريال يمني")
}

/**
 * Accounting formatting utility: strictly formats all numbers, dates, times, and currencies
 * in standard 123 numerals with thousands separators and configurable Arabic currency labels.
 */
object AccountingFormatter {
    var mainCurrencyName: String = "ريال يمني"
    var mainCurrencySymbol: String = "ر.ي."
    var mainCurrencyCode: String = "YER"
    var mainCurrencyRateYer: Double = 1.0

    var symbolPosition: String = "AUTO" // "AUTO", "AFTER", "BEFORE"
    var decimalMode: String = "AUTO" // "AUTO", "ALWAYS_TWO", "INTEGER_ONLY", "DYNAMIC_IF_FRACTION"
    var thousandsSeparator: String = "," // ",", " ", ""
    var displayFormat: String = "SYMBOL" // "SYMBOL", "CODE", "NAME"
    var useEasternDigitsGlobal: Boolean = false

    private val englishSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    private val currencyFormatterTwoDecimals = DecimalFormat("#,##0.00", englishSymbols)
    private val currencyFormatterDynamic = DecimalFormat("#,##0.##", englishSymbols)
    private val integerFormatter = DecimalFormat("#,##0", englishSymbols)
    private val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)
    private val shortDateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.US)

    // Converts any eastern Arabic digits (١٢٣٤٥٦٧٨٩٠) back to standard Western digits (123)
    fun toWesternArabicDigits(input: String): String {
        val easternDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val westernDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        var result = input
        for (i in easternDigits.indices) {
            result = result.replace(easternDigits[i], westernDigits[i])
        }
        return result
    }

    fun toEasternArabicDigits(input: String): String {
        val westernDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val easternDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        var result = input
        for (i in westernDigits.indices) {
            result = result.replace(westernDigits[i], easternDigits[i])
        }
        return result
    }

    fun applyDigits(formatted: String, useEasternDigits: Boolean = false): String {
        return if (useEasternDigits || useEasternDigitsGlobal) {
            toEasternArabicDigits(formatted)
        } else {
            toWesternArabicDigits(formatted)
        }
    }

    fun getCurrencyLabel(symbol: String = mainCurrencySymbol, code: String = mainCurrencyCode, name: String = mainCurrencyName): String {
        return when (displayFormat) {
            "CODE" -> code
            "NAME" -> name
            else -> symbol
        }
    }

    fun formatRawNumber(converted: Double): String {
        val raw = when (decimalMode) {
            "ALWAYS_TWO" -> currencyFormatterTwoDecimals.format(converted)
            "INTEGER_ONLY" -> integerFormatter.format(kotlin.math.round(converted).toLong())
            "DYNAMIC_IF_FRACTION" -> {
                if (kotlin.math.abs(converted - kotlin.math.round(converted)) > 0.0001) {
                    currencyFormatterDynamic.format(converted)
                } else {
                    integerFormatter.format(converted.toLong())
                }
            }
            else -> { // AUTO
                if (mainCurrencyCode in listOf("USD", "EUR", "GBP")) {
                    currencyFormatterTwoDecimals.format(converted)
                } else if (mainCurrencyCode == "YER") {
                    integerFormatter.format(kotlin.math.round(converted).toLong())
                } else {
                    if (kotlin.math.abs(converted - kotlin.math.round(converted)) > 0.0001) {
                        currencyFormatterDynamic.format(converted)
                    } else {
                        integerFormatter.format(converted.toLong())
                    }
                }
            }
        }
        return when (thousandsSeparator) {
            " " -> raw.replace(",", " ")
            "" -> raw.replace(",", "")
            else -> raw
        }
    }

    fun assembleCurrencyString(formattedNumber: String, currLabel: String = getCurrencyLabel()): String {
        val isBefore = when (symbolPosition) {
            "BEFORE" -> true
            "AFTER" -> false
            else -> (mainCurrencyCode in listOf("USD", "EUR", "GBP") || mainCurrencySymbol in listOf("$", "€", "£"))
        }
        return if (isBefore) {
            "$currLabel $formattedNumber"
        } else {
            "$formattedNumber $currLabel"
        }
    }

    fun formatMoney(amount: Double, useEasternDigits: Boolean = false): String {
        val converted = amount / mainCurrencyRateYer
        val numStr = applyDigits(formatRawNumber(converted), useEasternDigits)
        return assembleCurrencyString(numStr)
    }

    fun formatCurrency(amount: Double, currencyUnit: String = mainCurrencySymbol, useEasternDigits: Boolean = false): String {
        val numStr = applyDigits(formatRawNumber(amount), useEasternDigits)
        val isBefore = (currencyUnit == "$" || currencyUnit == "€" || currencyUnit == "USD" || currencyUnit == "EUR")
        return if (isBefore) "$currencyUnit $numStr" else "$numStr $currencyUnit"
    }

    fun formatYer(amount: Double, useEasternDigits: Boolean = false): String {
        return formatMoney(amount, useEasternDigits)
    }

    fun formatSar(amount: Double, useEasternDigits: Boolean = false): String {
        val num = currencyFormatterDynamic.format(amount)
        val withSep = when (thousandsSeparator) {
            " " -> num.replace(",", " ")
            "" -> num.replace(",", "")
            else -> num
        }
        return "${applyDigits(withSep, useEasternDigits)} ر.س."
    }

    fun formatNumber(number: Int, useEasternDigits: Boolean = false): String {
        val num = integerFormatter.format(number)
        val withSep = when (thousandsSeparator) {
            " " -> num.replace(",", " ")
            "" -> num.replace(",", "")
            else -> num
        }
        return applyDigits(withSep, useEasternDigits)
    }

    fun formatNumber(number: Long, useEasternDigits: Boolean = false): String {
        val num = integerFormatter.format(number)
        val withSep = when (thousandsSeparator) {
            " " -> num.replace(",", " ")
            "" -> num.replace(",", "")
            else -> num
        }
        return applyDigits(withSep, useEasternDigits)
    }

    fun formatNumber(number: Double, useEasternDigits: Boolean = false): String {
        val withSep = formatRawNumber(number)
        return applyDigits(withSep, useEasternDigits)
    }

    fun formatTafqeetMain(amount: Double, withPrefixAndSuffix: Boolean = true): String {
        val converted = amount / mainCurrencyRateYer
        return TafqeetHelper.convert(converted, currency = mainCurrencyCode, withPrefixAndSuffix = withPrefixAndSuffix)
    }

    fun formatTafqeetYer(amount: Double, withPrefixAndSuffix: Boolean = true): String {
        return formatTafqeetMain(amount, withPrefixAndSuffix)
    }

    fun formatTafqeetSar(amount: Double, withPrefixAndSuffix: Boolean = true): String {
        return TafqeetHelper.convert(amount, currency = "SAR", withPrefixAndSuffix = withPrefixAndSuffix)
    }

    fun formatTafqeetGeneric(amount: Double, withPrefixAndSuffix: Boolean = true): String {
        return TafqeetHelper.convert(amount, currency = "NONE", withPrefixAndSuffix = withPrefixAndSuffix)
    }

    fun formatDate(timestamp: Long, useEasternDigits: Boolean = false): String {
        return applyDigits(dateFormatter.format(Date(timestamp)))
    }

    fun formatDayDate(timestamp: Long, useEasternDigits: Boolean = false): String {
        val dayName = SimpleDateFormat("EEEE", Locale("ar")).format(Date(timestamp))
        val datePart = dateFormatter.format(Date(timestamp))
        return "$dayName - ${toWesternArabicDigits(datePart)}"
    }

    fun formatShortDate(timestamp: Long, useEasternDigits: Boolean = false): String {
        return applyDigits(shortDateFormatter.format(Date(timestamp)))
    }

    fun formatTimeAndDay(timestamp: Long, useEasternDigits: Boolean = false, use24Hour: Boolean = false): String {
        val dayName = SimpleDateFormat("EEEE", Locale("ar")).format(Date(timestamp))
        return if (use24Hour) {
            val timePart = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(timestamp))
            applyDigits("$timePart - $dayName", useEasternDigits)
        } else {
            val timePart = SimpleDateFormat("hh:mm:ss", Locale.US).format(Date(timestamp))
            val amPm = if (SimpleDateFormat("a", Locale.US).format(Date(timestamp)).equals("AM", ignoreCase = true)) "ص" else "م"
            applyDigits("$timePart $amPm - $dayName", useEasternDigits)
        }
    }

    fun formatTime(timestamp: Long, useEasternDigits: Boolean = false, use24Hour: Boolean = false): String {
        return if (use24Hour) {
            val timePart = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(timestamp))
            applyDigits(timePart, useEasternDigits)
        } else {
            val timePart = SimpleDateFormat("hh:mm:ss", Locale.US).format(Date(timestamp))
            val amPm = if (SimpleDateFormat("a", Locale.US).format(Date(timestamp)).equals("AM", ignoreCase = true)) "ص" else "م"
            applyDigits("$timePart $amPm", useEasternDigits)
        }
    }

    fun formatHijriDate(timestamp: Long, useEasternDigits: Boolean = false, adjustmentDays: Int = 0): String {
        return try {
            val instant = java.time.Instant.ofEpochMilli(timestamp)
            val zone = java.time.ZoneId.systemDefault()
            val localDate = instant.atZone(zone).toLocalDate().plusDays(adjustmentDays.toLong())
            val hijrahDate = java.time.chrono.HijrahDate.from(localDate)
            val dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd هـ", Locale.US)
            toWesternArabicDigits(dtf.format(hijrahDate))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDisplayDate(timestamp: Long, showHijri: Boolean, useEasternDigits: Boolean = false, adjustmentDays: Int = 0): String {
        val greg = formatDate(timestamp, useEasternDigits)
        if (!showHijri) return "$greg م"
        val hijri = formatHijriDate(timestamp, useEasternDigits, adjustmentDays)
        return if (hijri.isNotBlank()) "$greg م | $hijri" else "$greg م"
    }

    fun formatFullSingleLineHeader(timestamp: Long, adjustmentDays: Int = 0): String {
        val dayName = SimpleDateFormat("EEEE", Locale("ar")).format(Date(timestamp))
        val gregDate = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date(timestamp))
        val rawTimePart = SimpleDateFormat("hh:mm:ss", Locale.US).format(Date(timestamp))
        val amPm = if (SimpleDateFormat("a", Locale.US).format(Date(timestamp)).equals("AM", ignoreCase = true)) "ص" else "م"
        val timePart = "$rawTimePart $amPm"
        val hijriDate = formatHijriDate(timestamp, false, adjustmentDays)
        val hijriStr = if (hijriDate.isNotBlank()) " | هجري: $hijriDate" else ""
        return "اليوم: $dayName | ميلادي: $gregDate م$hijriStr | الوقت: $timePart"
    }

    fun formatBalanceFormatted(balance: Double, useEasternDigits: Boolean = false): String {
        val absVal = kotlin.math.abs(balance)
        val formatted = formatMoney(absVal, useEasternDigits)
        return when {
            balance > 0.001 -> "+ $formatted ↑"
            balance < -0.001 -> "- $formatted ↓"
            else -> "${formatMoney(0.0, useEasternDigits)} (متطابق)"
        }
    }
}

@Composable
fun rememberLiveTimeMillis(): Long {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000L)
        }
    }
    return currentTime
}
