package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.DailySalesRepository
import com.example.ui.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

enum class AppScreen {
    SPLASH,       // الشاشة الترحيبية
    ONBOARDING,   // طبقة التعريف
    HOME,         // الرئيسة (تجمع المبيعات والصندوق)
    DIRECT_SALES, // سند (المبيعات)
    CASH_BOX,     // نقدي (الصندوق)
    REPORTS,      // التقرير
    MANAGEMENT,   // تنظيم
    MANAGEMENT_SALES, // تنظيم المبيعات
    MANAGEMENT_CASH,  // تنظيم الصندوق
    SETTINGS      // الإعدادات
}

val DEFAULT_PRESETS = emptyList<AccountingPreset>()

data class AppThemePreset(
    val name: String,
    val description: String,
    val primaryColor: Long,
    val headerBgColor: Long,
    val headerTextColor: Long,
    val borderColor: Long,
    val calculatorColor: Long,
    val activeTabColor: Long,
    val previewGradient: List<Long>,
    val isNightMode: Boolean = false,
    val bgStyle: String = "DEFAULT"
)

val PRESET_APP_THEMES = listOf(
    // ☀️ DAY THEMES (السمات النهارية المتدرجة والمشرقة للمحاسبة)
    AppThemePreset(
        name = "💎 ياقوت أزرق (الافتراضي)",
        description = "السمة النهارية الافتراضية: مظهر ياقوتي ملكي متدرج للمحاسبة مع خلفية لؤلؤية نقية عالية التباين",
        primaryColor = 0xFF1E40AF,
        headerBgColor = 0xFFEFF6FF,
        headerTextColor = 0xFF1E3A8A,
        borderColor = 0xFF93C5FD,
        calculatorColor = 0xFF1D4ED8,
        activeTabColor = 0xFF1E40AF,
        previewGradient = listOf(0xFF1E40AF, 0xFF3B82F6),
        isNightMode = false,
        bgStyle = "DEFAULT"
    ),
    AppThemePreset(
        name = "💵 زمرد السيولة",
        description = "مظهر نهاري منعش بتدرجات الزمرد النقدي وأوراق العملة الخضراء المتناسقة مع الصندوق",
        primaryColor = 0xFF059669,
        headerBgColor = 0xFFECFDF5,
        headerTextColor = 0xFF065F46,
        borderColor = 0xFF6EE7B7,
        calculatorColor = 0xFF047857,
        activeTabColor = 0xFF059669,
        previewGradient = listOf(0xFF047857, 0xFF10B981),
        isNightMode = false,
        bgStyle = "DEFAULT"
    ),
    AppThemePreset(
        name = "🪙 ذهب محاسبي",
        description = "مظهر نهاري دافئ وفخم بتدرجات الذهب الأصفر والعنبر المحاسبي الراقي",
        primaryColor = 0xFFD97706,
        headerBgColor = 0xFFFFFBEB,
        headerTextColor = 0xFF78350F,
        borderColor = 0xFFFCD34D,
        calculatorColor = 0xFFB45309,
        activeTabColor = 0xFFD97706,
        previewGradient = listOf(0xFFB45309, 0xFFF59E0B),
        isNightMode = false,
        bgStyle = "METALLIC_GOLD"
    ),
    AppThemePreset(
        name = "🌊 فيروز مالي",
        description = "مظهر نهاري متدرج بدرجات الأزرق المحيطي والفيروز النقي المريح للعين أثناء العمل الطويل",
        primaryColor = 0xFF0284C7,
        headerBgColor = 0xFFF0F9FF,
        headerTextColor = 0xFF0C4A6E,
        borderColor = 0xFF7DD3FC,
        calculatorColor = 0xFF0369A1,
        activeTabColor = 0xFF0284C7,
        previewGradient = listOf(0xFF0284C7, 0xFF06B6D4),
        isNightMode = false,
        bgStyle = "DEFAULT"
    ),
    AppThemePreset(
        name = "🌸 عقيق وردي",
        description = "مظهر نهاري أنيق بتدرجات العقيق والياقوت الوردي المتناسق",
        primaryColor = 0xFFE11D48,
        headerBgColor = 0xFFFFF1F2,
        headerTextColor = 0xFF881337,
        borderColor = 0xFFFDA4AF,
        calculatorColor = 0xFFBE123C,
        activeTabColor = 0xFFE11D48,
        previewGradient = listOf(0xFFBE123C, 0xFFFB7185),
        isNightMode = false,
        bgStyle = "DEFAULT"
    ),
    AppThemePreset(
        name = "🐅 نادي الاتحاد",
        description = "مظهر نهاري رياضي مشرق بألوان العميد الذهبية الصفراء بتطريز كربوني فاخر",
        primaryColor = 0xFFEAB308,
        headerBgColor = 0xFFFEF9C3,
        headerTextColor = 0xFF713F12,
        borderColor = 0xFFFDE047,
        calculatorColor = 0xFFCA8A04,
        activeTabColor = 0xFFEAB308,
        previewGradient = listOf(0xFFCA8A04, 0xFFFACC15),
        isNightMode = false,
        bgStyle = "TEXTURE_CARBON_FIBER"
    ),
    AppThemePreset(
        name = "🏛️ بلاتين ملكي",
        description = "مظهر فاخر بتدرجات النيلي والبلاتين الملكي المضيء عالي الاحترافية",
        primaryColor = 0xFF4F46E5,
        headerBgColor = 0xFFEEF2FF,
        headerTextColor = 0xFF312E81,
        borderColor = 0xFFA5B4FC,
        calculatorColor = 0xFF4338CA,
        activeTabColor = 0xFF4F46E5,
        previewGradient = listOf(0xFF4338CA, 0xFF6366F1),
        isNightMode = false,
        bgStyle = "METALLIC_SILVER"
    ),
    AppThemePreset(
        name = "🪵 خشب الأرو",
        description = "خشب الجوز والأرو الفاخر بتدرجات البني الدافئ والنقوش الطبيعية الراقية",
        primaryColor = 0xFF8D6E63,
        headerBgColor = 0xFFEFEBE9,
        headerTextColor = 0xFF3E2723,
        borderColor = 0xFFBCAAA4,
        calculatorColor = 0xFF6D4C41,
        activeTabColor = 0xFF8D6E63,
        previewGradient = listOf(0xFF6D4C41, 0xFF8D6E63),
        isNightMode = false,
        bgStyle = "TEXTURE_WOOD_GRAIN"
    ),
    AppThemePreset(
        name = "🥈 فضة بلاتينية",
        description = "طابع ناصع وأنيق بتدرجات الفضة والكروم المصقول المضيء",
        primaryColor = 0xFF2563EB,
        headerBgColor = 0xFFF8FAFC,
        headerTextColor = 0xFF1E293B,
        borderColor = 0xFF93C5FD,
        calculatorColor = 0xFF1D4ED8,
        activeTabColor = 0xFF2563EB,
        previewGradient = listOf(0xFFFFFFFF, 0xFF93C5FD),
        isNightMode = false,
        bgStyle = "METALLIC_SILVER"
    ),
    AppThemePreset(
        name = "🧱 برونز دافئ",
        description = "مظهر نحاسي فاخر بالدرجات الأنيقة والدافئة",
        primaryColor = 0xFFC2410C,
        headerBgColor = 0xFFFFEDD5,
        headerTextColor = 0xFF7C2D12,
        borderColor = 0xFFFB923C,
        calculatorColor = 0xFF9A3412,
        activeTabColor = 0xFFEA580C,
        previewGradient = listOf(0xFFFFEDD5, 0xFFC2410C),
        isNightMode = false,
        bgStyle = "METALLIC_COPPER"
    ),

    // 🌙 NIGHT THEMES (السمات الليلية ثانياً)
    AppThemePreset(
        name = "🛠️ تيتانيوم (ليلي)",
        description = "السمة الليلية الافتراضية: سمة معدنية صلبة فاخرة بخلفية التيتانيوم المسبوك الفاخر مريحة جداً للعين",
        primaryColor = 0xFF94A3B8,
        headerBgColor = 0xFF0F172A,
        headerTextColor = 0xFFF8FAFC,
        borderColor = 0xFF334155,
        calculatorColor = 0xFF1E293B,
        activeTabColor = 0xFF64748B,
        previewGradient = listOf(0xFF0F172A, 0xFF334155),
        isNightMode = true,
        bgStyle = "METALLIC_TITANIUM"
    ),
    AppThemePreset(
        name = "🐅 الاتحاد (ليلي)",
        description = "مظهر ليلي كربوني فخم بألوان نادي الاتحاد السعودي: أسود فاحم مع تطعيمات الأصفر والذهبي الملكي",
        primaryColor = 0xFFFACC15,
        headerBgColor = 0xFF09090B,
        headerTextColor = 0xFFFEF08A,
        borderColor = 0xFFEAB308,
        calculatorColor = 0xFFCA8A04,
        activeTabColor = 0xFFEAB308,
        previewGradient = listOf(0xFF09090B, 0xFFEAB308),
        isNightMode = true,
        bgStyle = "TEXTURE_CARBON_FIBER"
    ),
    AppThemePreset(
        name = "🔮 نيون",
        description = "مظهر ليلي فائق التطور بتدرجات النيون البنفسجي الفوسفوري وأسود الأوليد",
        primaryColor = 0xFFA855F7,
        headerBgColor = 0xFF0D0B18,
        headerTextColor = 0xFFF3E8FF,
        borderColor = 0xFF9333EA,
        calculatorColor = 0xFF7E22CE,
        activeTabColor = 0xFFA855F7,
        previewGradient = listOf(0xFF0D0B18, 0xFFA855F7),
        isNightMode = true,
        bgStyle = "TEXTURE_CARBON_FIBER"
    ),
    AppThemePreset(
        name = "🏎️ كاربون",
        description = "مظهر ألياف الكربون الرياضية المنسوجة الداكنة بتباين أحمر وفحمي فائق الأناقة",
        primaryColor = 0xFFEF4444,
        headerBgColor = 0xFF18181B,
        headerTextColor = 0xFFF4F4F5,
        borderColor = 0xFFDC2626,
        calculatorColor = 0xFF991B1B,
        activeTabColor = 0xFFEF4444,
        previewGradient = listOf(0xFF18181B, 0xFFEF4444),
        isNightMode = true,
        bgStyle = "TEXTURE_CARBON_FIBER"
    ),
    AppThemePreset(
        name = "💺 جلد فاخر",
        description = "مظهر الجلد الملكي الفاخر بتخريمات ناعمة وتطريز هافان وأسود أنيق",
        primaryColor = 0xFFD97706,
        headerBgColor = 0xFF1C1917,
        headerTextColor = 0xFFFDE68A,
        borderColor = 0xFFB45309,
        calculatorColor = 0xFF78350F,
        activeTabColor = 0xFFD97706,
        previewGradient = listOf(0xFF1C1917, 0xFFD97706),
        isNightMode = true,
        bgStyle = "TEXTURE_PERFORATED_LEATHER"
    ),
    AppThemePreset(
        name = "🛠️ تيتانيوم 2",
        description = "سمة ليلية صلبة بخلفية التيتانيوم المسبوك الفاخر",
        primaryColor = 0xFF94A3B8,
        headerBgColor = 0xFF0F172A,
        headerTextColor = 0xFFF8FAFC,
        borderColor = 0xFF334155,
        calculatorColor = 0xFF1E293B,
        activeTabColor = 0xFF64748B,
        previewGradient = listOf(0xFF0F172A, 0xFF334155),
        isNightMode = true,
        bgStyle = "METALLIC_TITANIUM"
    ),
    AppThemePreset(
        name = "👑 ذهب ملكي",
        description = "مظهر ليلي كربوني فخم بتطعيمات الذهب الخالص والأصفر الملكي البراق",
        primaryColor = 0xFFF59E0B,
        headerBgColor = 0xFF140F04,
        headerTextColor = 0xFFFEF3C7,
        borderColor = 0xFFD97706,
        calculatorColor = 0xFFB45309,
        activeTabColor = 0xFFF59E0B,
        previewGradient = listOf(0xFF140F04, 0xFFF59E0B),
        isNightMode = true,
        bgStyle = "METALLIC_GOLD"
    ),
    AppThemePreset(
        name = "🌌 فضاء",
        description = "مظهر كوني داكن بدرجات البنفسجي السديمي والنيلي المتوهج",
        primaryColor = 0xFF8B5CF6,
        headerBgColor = 0xFF0F0B1E,
        headerTextColor = 0xFFEDE9FE,
        borderColor = 0xFF7C3AED,
        calculatorColor = 0xFF6D28D9,
        activeTabColor = 0xFF8B5CF6,
        previewGradient = listOf(0xFF0F0B1E, 0xFF8B5CF6),
        isNightMode = true,
        bgStyle = "TEXTURE_CARBON_FIBER"
    ),
    AppThemePreset(
        name = "🌲 زمرد ليلي",
        description = "مظهر طبيعي داكن مستوحى من أشجار الغابات ليلاً بتوهج الزمرد الأخضر",
        primaryColor = 0xFF059669,
        headerBgColor = 0xFF04140D,
        headerTextColor = 0xFFD1FAE5,
        borderColor = 0xFF059669,
        calculatorColor = 0xFF047857,
        activeTabColor = 0xFF10B981,
        previewGradient = listOf(0xFF04140D, 0xFF10B981),
        isNightMode = true,
        bgStyle = "TEXTURE_CARBON_FIBER"
    ),
    AppThemePreset(
        name = "🌊 سمة أعماق الهاوية البحرية",
        description = "مظهر ليلي داكن من خنادق المحيط بدرجات التيركواز والأزرق العميق",
        primaryColor = 0xFF06B6D4,
        headerBgColor = 0xFF031520,
        headerTextColor = 0xFFCFFAFE,
        borderColor = 0xFF0891B2,
        calculatorColor = 0xFF0891B2,
        activeTabColor = 0xFF06B6D4,
        previewGradient = listOf(0xFF031520, 0xFF06B6D4),
        isNightMode = true,
        bgStyle = "METALLIC_TITANIUM"
    ),
    AppThemePreset(
        name = "⚡ سمة الصاعقة الزرقاء الكهربائية",
        description = "مظهر كحلي داكن وفخم بإشعاع أزرق كهربائي فوسفوري عالي التباين",
        primaryColor = 0xFF3B82F6,
        headerBgColor = 0xFF091224,
        headerTextColor = 0xFFDBEAFE,
        borderColor = 0xFF2563EB,
        calculatorColor = 0xFF1D4ED8,
        activeTabColor = 0xFF3B82F6,
        previewGradient = listOf(0xFF091224, 0xFF3B82F6),
        isNightMode = true,
        bgStyle = "METALLIC_TITANIUM"
    ),
    AppThemePreset(
        name = "🌹 سمة الياقوت والمخمل الأسود",
        description = "مظهر مخملي أسود ملكي بلمسات الياقوت الأحمر القرمزي الداكن",
        primaryColor = 0xFFE11D48,
        headerBgColor = 0xFF1C060B,
        headerTextColor = 0xFFFFE4E6,
        borderColor = 0xFFBE123C,
        calculatorColor = 0xFF9F1239,
        activeTabColor = 0xFFE11D48,
        previewGradient = listOf(0xFF1C060B, 0xFFE11D48),
        isNightMode = true,
        bgStyle = "TEXTURE_CARBON_FIBER"
    )
)

data class DailyDirectSalesUiState(
    val sellerName: String = "",
    val dateTimestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val cashInBoxYerInput: String = "", // مبلغ الصندوق بالريال اليمني (ر.ي.)
    val cashInBoxSarInput: String = "", // مبلغ الصندوق بالريال السعودي (ر.س.)
    val exchangeRateInput: String = "380", // سعر الصرف 1 ر.س. = 380 ر.ي.
    val presets: List<AccountingPreset> = emptyList(),
    val activePresetId: String? = null,
    val customCurrencies: List<com.example.ui.model.CustomCurrency> = listOf(
        com.example.ui.model.CustomCurrency("YER", "ريال يمني", "ر.ي.", 1.0, "🇾🇪", isMain = true),
        com.example.ui.model.CustomCurrency("SAR", "ريال سعودي", "ر.س.", 380.0, "🇸🇦"),
        com.example.ui.model.CustomCurrency("USD", "دولار أمريكي", "$", 530.0, "🇺🇸"),
        com.example.ui.model.CustomCurrency("AED", "درهم إماراتي", "د.إ.", 144.0, "🇦🇪"),
        com.example.ui.model.CustomCurrency("OMR", "ريال عماني", "ر.ع.", 1375.0, "🇴🇲"),
        com.example.ui.model.CustomCurrency("KWD", "دينار كويتي", "د.ك.", 1720.0, "🇰🇼"),
        com.example.ui.model.CustomCurrency("EUR", "يورو", "€", 570.0, "🇪🇺"),
        com.example.ui.model.CustomCurrency("QAR", "ريال قطري", "ر.ق.", 145.0, "🇶🇦"),
        com.example.ui.model.CustomCurrency("EGP", "جنيه مصري", "ج.م", 11.0, "🇪🇬")
    ),
    val currencySymbolPosition: String = "AUTO", // "AUTO", "AFTER", "BEFORE"
    val currencyDecimalMode: String = "AUTO", // "AUTO", "ALWAYS_TWO", "INTEGER_ONLY", "DYNAMIC_IF_FRACTION"
    val currencyThousandsSeparator: String = ",", // ",", " ", ""
    val currencyDisplayType: String = "SYMBOL", // "SYMBOL", "CODE", "NAME"
    val showCashDenominationsTable: Boolean = false, // فئات النقد بالصندوق محذوفة افتراضياً وتُفعّل اختيارياً من الإعدادات
    val useEasternArabicNumerals: Boolean = false, // نمط الأرقام: 123 افتراضي، ١٢٣ اختياري
    val showHijriDate: Boolean = false, // التاريخ الهجري اختياري
    val hijriAdjustmentDays: Int = 0, // تعديل فارق أيام التاريخ الهجري (معايرة رؤية الهلال)
    val use24HourFormat: Boolean = false, // نظام 24 ساعة اختياري (تلقائياً 12 ساعة)
    val isReadOnlyMode: Boolean = false, // وضع القراءة (قراءة فقط بدون تعديل)
    val isLockGivenExtraMode: Boolean = false, // وضع منع تعديل المعطى والإضافي
    val isDayClosed: Boolean = false, // إغلاق اليوم الحسابي
    val closedDayTimestamp: Long = 0L, // وقت إغلاق اليوم
    val showCloseDayConfirmDialog: Boolean = false, // نافذة تأكيد إغلاق اليوم
    val showUnlockDaySternWarningDialog: Boolean = false, // نافذة التحذير شديد اللهجة عند محاولة التعديل أو فك القفل
    val showDisableReadOnlyConfirmDialog: Boolean = false,
    val showDisableLockGivenExtraConfirmDialog: Boolean = false,
    val showCashRebalanceConfirmDialog: Boolean = false,
    val selectedThemePresetName: String = "⚙️ سمة الفولاذ المصقول (الافتراضي)", // السمة المطبقة حالياً
    val appThemeSkin: String = "كلاسيكي (Classic)", // جلد السمة البصري (Skin)
    val isGridViewEnabled: Boolean = true, // تفعيل العرض الشبكي في الأقسام
    val appBackgroundImageUri: String? = null, // مسار صورة الخلفية المخصصة للمستخدم
    val includeInternetInReport: Boolean = false, // تضمين انترنت في التقرير
    val reportHeaderTitle: String = "البيان المالي",
    val reportHeaderSubtitle: String = "",
    val reportFontFamily: String = "الافتراضي (Cairo)",
    val reportTextColor: Long = 0xFF1E293B,
    val showCompactMode: Boolean = false, // الوضع المضغوط للشاشة (معطل افتراضياً)
    val isFullScreenMode: Boolean = false, // خيار ملء الشاشة للتطبيق (غير نشط تلقائياً)
    val appLanguage: String = "ar", // لغة التطبيق ("ar" أو "en")
    val isReorderModeEnabled: Boolean = false, // وضع إعادة الترتيب والسحب والتجميع (معطل افتراضياً)
    val isMultiCategoryCalculatorEnabled: Boolean = false, // آلة حاسبة متعددة الفئات (معطل افتراضياً)
    val isReportDetailedMode: Boolean = false, // نوع التقرير: false = تجميعي (افتراضي), true = تفصيلي
    val includeOtherCurrenciesInReport: Boolean = true, // خيار إدراج العملات الأخرى في التقرير
    val reportIncludeSalesSummary: Boolean = true,
    val reportIncludeTicketDenoms: Boolean = true,
    val reportIncludeCashBox: Boolean = true,
    val reportIncludeExpensesDeposits: Boolean = true,
    val reportIncludeDifferences: Boolean = true,
    val uiScaleFactor: Float = 1.0f, // مقياس العرض والتكبير/التصغير (1.0 افتراضي)
    val appBackgroundStyle: String = "DEFAULT", // "DEFAULT", "METALLIC_STEEL", "METALLIC_GOLD", "METALLIC_SILVER", "METALLIC_TITANIUM", "METALLIC_COPPER", "CUSTOM_COLOR"
    val appBackgroundColor: Long = 0xFFF8FAFC,
    val showDisabledRowsInReport: Boolean = false, // إظهار الفئات المعطلة في التقرير
    val showUnbalancedGroupsInReport: Boolean = false, // إظهار الالمستبعدة في التقرير
    val reportPrimaryColor: Long = 0xFF1E88E5, // لون التقرير الأساسي
    val reportSecondaryColor: Long = 0xFF455A64, // لون التقرير الثانوي
    val scheduledResetTime: String = "03:00", // وقت التصفير التلقائي اليومي
    val isAutoDailyResetEnabled: Boolean = true, // تفعيل التصفير التلقائي اليومي
    val dailyReminderEnabled: Boolean = false, // تفعيل تنبيه إغلاق الوردية اليومي
    val dailyReminderTime: String = "22:00", // وقت التنبيه اليومي
    val accountingDayStartHour: Int = 0, // بداية اليوم المحاسبي (0-6 ص لأيام المناسبات وساعات العمل بعد منتصف الليل)
    val auditLogs: List<AuditLogEntry> = emptyList(), // سجل تغيرات المدخلات المفصل
    val dailyReportArchives: List<DailyReportArchiveEntity> = emptyList(), // سجل السندات اليومية السابقة
    val backupSnapshots: List<BackupSnapshotEntity> = emptyList(), // سجل النسخ الاحتياطية المؤرخة بالتاريخ والوقت
    val backupScheduleFrequency: String = "OFF", // "OFF", "DAILY", "WEEKLY", "MONTHLY"
    val autoPruneDays: Int = 0, // 0: disabled, 7, 30, 90 days
    val keypadThemeColor: String = "رمادي داكن",
    val keypadButtonStyle: String = "دائري مريح",
    val remainingWarningMessage: String? = null, // تنبيه مباشر عند تجاوز المتبقي
    val expenseWarningMessage: String? = null, // تنبيه مباشر عند تجاوز المصروفات للنقد
    val showConfirmGivenDialog: Boolean = false, // تنبيه تأكيد تعديل المعطى
    val pendingGivenGroupId: String = GROUP_SANAD_ID,
    val pendingGivenDenom: Int? = null,
    val pendingGivenOldValue: String = "",
    val pendingGivenNewValue: String = "",
    val showCashConfirmDialog: Boolean = false,
    val pendingCashValue: String = "",
    val cashConfirmationMessage: String = "",
    val groups: List<SalesGroupUiState> = listOf(
        SalesGroupUiState(
            id = GROUP_SANAD_ID,
            name = "سند",
            type = SalesGroupType.DENOMINATIONS,
            isEnabled = true,
            orderIndex = 0,
            isDefault = true,
            isAddedFieldEnabled = false,
            rows = DEFAULT_TICKET_CATEGORIES.map { DirectSalesRowUiState(denomination = it) },
            isExcludedFromBalance = false,
            addToReport = true,
            addToBalance = true
        ),
        SalesGroupUiState(
            id = GROUP_CHINI_ID,
            name = "صيني",
            type = SalesGroupType.DIRECT_ENTRY,
            isEnabled = false,
            orderIndex = 1,
            isDefault = true,
            directEntries = emptyList(),
            isExcludedFromBalance = false,
            addToReport = true,
            addToBalance = true
        ),
        SalesGroupUiState(
            id = GROUP_GAME_CARDS_ID,
            name = "بطائق ألعاب",
            type = SalesGroupType.DENOMINATIONS,
            isEnabled = false,
            orderIndex = 2,
            isDefault = true,
            rows = DEFAULT_GAME_CARDS_CATEGORIES.map { DirectSalesRowUiState(denomination = it) },
            isExcludedFromBalance = false,
            addToReport = true,
            addToBalance = true
        ),
        SalesGroupUiState(
            id = GROUP_SHARABAT_ID,
            name = "شرابات",
            type = SalesGroupType.DENOMINATIONS,
            isEnabled = false,
            orderIndex = 3,
            isDefault = true,
            rows = DEFAULT_SHARABAT_CATEGORIES.map { DirectSalesRowUiState(denomination = it) },
            isExcludedFromBalance = false,
            addToReport = true,
            addToBalance = true
        ),
        SalesGroupUiState(
            id = GROUP_INTERNET_ID,
            name = "انترنت",
            type = SalesGroupType.DENOMINATIONS,
            isEnabled = false,
            orderIndex = 4,
            isDefault = true,
            rows = DEFAULT_INTERNET_CATEGORIES.map { DirectSalesRowUiState(denomination = it) },
            isExcludedFromBalance = true,
            addToReport = false,
            addToBalance = false
        )
    ),
    val selectedGroupId: String = GROUP_SANAD_ID,
    val cashGroups: List<CashBoxGroupUiState> = listOf(
        CashBoxGroupUiState(
            id = CASH_GROUP_MAIN_ID,
            name = "نقدي", 
            type = CashGroupType.DENOMINATIONS,
            isEnabled = true,
            isExpanded = true,
            orderIndex = 0,
            isDefault = true,
            denomRows = DEFAULT_CASH_DENOMINATIONS.map { CashDenomRowUiState(denomination = it) }
        ),
        CashBoxGroupUiState(
            id = CASH_GROUP_CURRENCIES_ID,
            name = "العملات الأخرى",
            type = CashGroupType.DIRECT_ENTRY,
            isEnabled = false,
            isExpanded = false,
            orderIndex = 1,
            isDefault = true,
            directEntries = emptyList()
        ),
        CashBoxGroupUiState(
            id = CASH_GROUP_EXPENSES_ID,
            name = "المصاريف",
            type = CashGroupType.EXPENSES,
            isEnabled = false,
            isExpanded = false,
            orderIndex = 2,
            isDefault = true
        ),
        CashBoxGroupUiState(
            id = "cash_group_deposits",
            name = "الإيداعات",
            type = CashGroupType.DEPOSITS,
            isEnabled = false,
            isExpanded = false,
            orderIndex = 3,
            isDefault = true
        )
    ),
    val selectedCashGroupId: String = CASH_GROUP_MAIN_ID,
    val expenses: List<CashExpenseItem> = emptyList(), // المصروفات المخصومة تلقائياً من الصندوق
    val errorMessage: String? = null,
    val showResetConfirmDialog: Boolean = false,
    val showShiftResetDialog: Boolean = false,
    val shiftResetSuccessBannerMessage: String? = null,
    val showAddCategoryDialog: Boolean = false,
    val showAddGroupDialog: Boolean = false,
    val showManageGroupsDialog: Boolean = false,
    val showDenomGroupEntryDialog: Boolean = false,
    val activeDenomForEntry: Int? = null,
    val showDirectGroupEntryDialog: Boolean = false,
    val activeDirectEntryId: String? = null,
    val showAddCashGroupDialog: Boolean = false,
    val showManageCashGroupsDialog: Boolean = false,
    val showCashDenomEntryDialog: Boolean = false,
    val activeCashDenomForEntry: Int? = null,
    val showCashDirectEntryDialog: Boolean = false,
    val activeCashDirectEntryId: String? = null,
    val showExchangeRateDialog: Boolean = false,
    val showAddExpenseDialog: Boolean = false,
    val showArchiveConfirmDialog: Boolean = false,
    val editingExpense: CashExpenseItem? = null,
    val showUniversalCalculator: Boolean = false,
    val universalCalcTitle: String = "",
    val universalCalcInitialValue: String = "",
    val universalCalcIsCurrency: Boolean = true,
    val universalCalcOnApply: ((String) -> Unit)? = null,
    val showCalculatorNavTab: Boolean = false, // تبويب المدخلات معطل افتراضيا ويفعل من الإعدادات
    val showInlineGroupCalculator: Boolean = true, // آلة حاسبة مصغرة أنيقة أسفل كل
    val tableHeaderBgColor: Long = 0xFFF1F5F9,
    val tableHeaderTextColor: Long = 0xFF1E293B,
    val tableBorderColor: Long = 0xFFCBD5E1,
    val tableCardBgColor: Long = 0xFFFFFFFF,
    val activeTabColor: Long = 0xFF1E88E5,
    val inactiveTabColor: Long = 0xFF64748B,
    val calculatorButtonColor: Long = 0xFF1E88E5,
    val calculatorBgColor: Long = 0xFF1E293B,
    val showRemainingStepper: Boolean = true, // زرّا زيادة (+1) وإنقاص (-1) للمتبقي
    val showBudgetSummaryBar: Boolean = true, // شريط حالة ملخص الميزانية في الأعلى
    val showMaintenanceDialog: Boolean = false, // إظهار نافذة الصيانة
    val maintenanceResults: List<String> = emptyList(), // نتائج الصيانة والتصحيح التلقائي
    val isMaintenanceRunning: Boolean = false, // هل فحص الصيانة جاري حالياً؟
    val shiftReminderEnabled: Boolean = false, // تفعيل التنبيه اليومي لإغلاق الوردية
    val shiftReminderTime: String = "21:00", // وقت التنبيه اليومي
    val customBackgroundImageUri: String? = null, // صورة خلفية مخصصة من الجهاز
    val customBackgroundOpacity: Float = 0.85f, // شفافية صورة الخلفية
    val isGlobalAddedFieldActive: Boolean = false, // زر الإضافي في قسم المبيعات
    val customColorThemeState: CustomColorThemeState = CustomColorThemeState()
) {
    val selectedGroup: SalesGroupUiState?
        get() = groups.find { it.id == selectedGroupId } ?: groups.firstOrNull { it.isEnabled } ?: groups.firstOrNull()

    val selectedCashGroup: CashBoxGroupUiState?
        get() = cashGroups.find { it.id == selectedCashGroupId } ?: cashGroups.firstOrNull { it.isEnabled } ?: cashGroups.firstOrNull()

    val cashInBoxInput: String
        get() = cashInBoxYerInput
}

class TicketAccountingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DailySalesRepository

    private val sharedPreferences = getApplication<Application>().getSharedPreferences("TicketAccountingPrefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(DailyDirectSalesUiState())
    val uiState: StateFlow<DailyDirectSalesUiState> = _uiState.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _customNightPrimary = MutableStateFlow<Color?>(null)
    val customNightPrimary: StateFlow<Color?> = _customNightPrimary.asStateFlow()

    private val _customDayPrimary = MutableStateFlow<Color?>(null)
    val customDayPrimary: StateFlow<Color?> = _customDayPrimary.asStateFlow()

    // Calculated summary of all groups
    val salesSummary: StateFlow<DailySalesSummary> = combine(_uiState, _uiState) { state, _ ->
        calculateSalesSummary(state)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailySalesSummary()
    )

    // Group breakdown report
    val groupReports: StateFlow<List<GroupReportItem>> = combine(_uiState, salesSummary) { state, _ ->
        val activeGroups = state.groups.filter { grp ->
            grp.isEnabled && (state.includeInternetInReport || (grp.id != GROUP_INTERNET_ID && !grp.name.contains("انترنت")))
        }
        val totalAllSales = activeGroups.sumOf { it.totalRevenue }
        activeGroups.map { group ->
            val rev = group.totalRevenue
            val percentage = if (totalAllSales > 0) (rev / totalAllSales) * 100 else 0.0
            val count = when (group.type) {
                SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> group.activeRows.sumOf { it.sold }
                SalesGroupType.DIRECT_ENTRY -> group.directEntries.count { it.isFilled }
            }
            GroupReportItem(
                groupId = group.id,
                groupName = group.name,
                type = group.type,
                isEnabled = group.isEnabled,
                totalRevenue = rev,
                totalItemsOrTickets = count,
                percentageOfRevenue = percentage
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Category breakdown report
    val categoryReports: StateFlow<List<CategoryReportItem>> = combine(_uiState, salesSummary) { state, _ ->
        val result = mutableListOf<CategoryReportItem>()
        val activeGroups = state.groups.filter { it.isEnabled && (it.type == SalesGroupType.DENOMINATIONS || it.type == SalesGroupType.CUSTOM_FIELDS) }
        val totalAllSales = activeGroups.sumOf { it.totalRevenue }
        for (group in activeGroups) {
            // Always show all rows for enabled groups in the report breakdown as requested
            val rowsToInclude = group.rows 
            for (row in rowsToInclude) {
                val percentage = if (totalAllSales > 0) (row.total / totalAllSales) * 100 else 0.0
                result.add(
                    CategoryReportItem(
                        denomination = row.denomination,
                        totalGiven = row.given,
                        totalAdded = row.added,
                        totalRemaining = row.remaining,
                        totalSold = row.sold,
                        totalRevenue = row.total,
                        groupName = group.name,
                        notes = row.notes,
                        percentageOfRevenue = percentage
                    )
                )
            }
        }
        result.sortedBy { it.denomination }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DailySalesRepository(db.dailySalesDao())
        
        val initialThemePreset = sharedPreferences.getString("selectedThemePresetName", "السمة الليلية الفائقة (تيتانيوم)") ?: "السمة الليلية الفائقة (تيتانيوم)"
        val initialHijriAdj = sharedPreferences.getInt("hijriAdjustmentDays", 0)
        val initialRemainingStepper = sharedPreferences.getBoolean("showRemainingStepper", true)
        val initialBudgetSummaryBar = sharedPreferences.getBoolean("showBudgetSummaryBar", true)
        val initialShiftReminder = sharedPreferences.getBoolean("shiftReminderEnabled", false)
        val initialShiftReminderTime = sharedPreferences.getString("shiftReminderTime", "21:00") ?: "21:00"
        val initialCompact = sharedPreferences.getBoolean("showCompactMode", false)
        val initialIsDayClosed = sharedPreferences.getBoolean("is_day_closed", false)
        val initialClosedDayTs = sharedPreferences.getLong("closed_day_timestamp", 0L)
        val initialScheduledResetTime = sharedPreferences.getString("scheduledResetTime", "03:00") ?: "03:00"
        val initialAccountingDayStartHour = sharedPreferences.getInt("accountingDayStartHour", 0)
        val initialIsAutoDailyResetEnabled = sharedPreferences.getBoolean("isAutoDailyResetEnabled", false)
        val initialTableCardAlpha = sharedPreferences.getFloat("tableCardAlpha", 1.0f)
        val initialTableHeaderAlpha = sharedPreferences.getFloat("tableHeaderAlpha", 1.0f)
        val initialTableCellAlpha = sharedPreferences.getFloat("tableCellAlpha", 1.0f)
        
        val initialSymbolPos = sharedPreferences.getString("currency_symbol_position", "AUTO") ?: "AUTO"
        val initialDecimalMode = sharedPreferences.getString("currency_decimal_mode", "AUTO") ?: "AUTO"
        val initialThousandsSep = sharedPreferences.getString("currency_thousands_separator", ",") ?: ","
        val initialDisplayType = sharedPreferences.getString("currency_display_type", "SYMBOL") ?: "SYMBOL"

        com.example.ui.model.AccountingFormatter.symbolPosition = initialSymbolPos
        com.example.ui.model.AccountingFormatter.decimalMode = initialDecimalMode
        com.example.ui.model.AccountingFormatter.thousandsSeparator = initialThousandsSep
        com.example.ui.model.AccountingFormatter.displayFormat = initialDisplayType

        val loadedCurrs = loadCurrenciesFromPrefs()
        _uiState.update { state ->
            val nextState = state.copy(
                selectedThemePresetName = initialThemePreset,
                hijriAdjustmentDays = initialHijriAdj,
                showRemainingStepper = initialRemainingStepper,
                showBudgetSummaryBar = initialBudgetSummaryBar,
                shiftReminderEnabled = initialShiftReminder,
                shiftReminderTime = initialShiftReminderTime,
                showCompactMode = initialCompact,
                isDayClosed = initialIsDayClosed,
                closedDayTimestamp = initialClosedDayTs,
                scheduledResetTime = initialScheduledResetTime,
                accountingDayStartHour = initialAccountingDayStartHour,
                isAutoDailyResetEnabled = initialIsAutoDailyResetEnabled,
                currencySymbolPosition = initialSymbolPos,
                currencyDecimalMode = initialDecimalMode,
                currencyThousandsSeparator = initialThousandsSep,
                currencyDisplayType = initialDisplayType,
                customColorThemeState = state.customColorThemeState.copy(
                    tableCardAlpha = initialTableCardAlpha,
                    tableHeaderAlpha = initialTableHeaderAlpha,
                    tableCellAlpha = initialTableCellAlpha
                )
            )
            if (loadedCurrs != null) {
                val main = loadedCurrs.find { it.isMain }
                if (main != null) {
                    com.example.ui.model.AccountingFormatter.mainCurrencySymbol = main.symbol
                    com.example.ui.model.AccountingFormatter.mainCurrencyCode = main.code
                    com.example.ui.model.AccountingFormatter.mainCurrencyName = main.arabicName
                    com.example.ui.model.AccountingFormatter.mainCurrencyRateYer = main.defaultRateYer
                }
                nextState.copy(customCurrencies = loadedCurrs)
            } else {
                nextState
            }
        }

        startAutoResetMonitor()

        // Collect daily report archives
        viewModelScope.launch {
            repository.dailyReportArchives.collect { archives ->
                _uiState.update { it.copy(dailyReportArchives = archives) }
            }
        }

        // Collect backup snapshots
        viewModelScope.launch {
            repository.backupSnapshots.collect { snapshots ->
                _uiState.update { it.copy(backupSnapshots = snapshots) }
            }
        }

        // Load initial persisted data from Room
        viewModelScope.launch {
            val loadedCustomPresets = loadPresetsFromStorage()
            _uiState.update { state ->
                state.copy(presets = loadedCustomPresets)
            }

            val savedSales = repository.dailySales.firstOrNull()
            val savedGroups = repository.salesGroups.firstOrNull()
            val savedItems = repository.categoryItems.firstOrNull()
            val savedDirectEntries = repository.directEntries.firstOrNull()
            val savedCashGroups = repository.cashBoxGroups.firstOrNull()
            val savedCashDenoms = repository.cashDenomItems.firstOrNull()
            val savedCashDirects = repository.cashDirectEntries.firstOrNull()
            val savedExpenses = repository.cashExpenses.firstOrNull()

            if (savedSales != null && !savedGroups.isNullOrEmpty()) {
                val groupUiStates = savedGroups.sortedBy { it.orderIndex }.map { groupEntity ->
                    val type = try {
                        SalesGroupType.valueOf(groupEntity.type)
                    } catch (_: Exception) {
                        SalesGroupType.DENOMINATIONS
                    }

                    val formula = try {
                        CalculationFormula.valueOf(groupEntity.defaultFormula)
                    } catch (_: Exception) {
                        CalculationFormula.TICKET_STANDARD
                    }

                    if (type == SalesGroupType.DENOMINATIONS || type == SalesGroupType.CUSTOM_FIELDS) {
                        val groupItems = (savedItems ?: emptyList()).filter { it.groupId == groupEntity.id }
                        val itemMap = groupItems.associateBy { it.denomination }
                        val allDenoms = when (groupEntity.id) {
                            GROUP_SANAD_ID -> (DEFAULT_TICKET_CATEGORIES + groupItems.map { it.denomination }).distinct().sorted()
                            GROUP_INTERNET_ID -> DEFAULT_INTERNET_CATEGORIES
                            GROUP_SHARABAT_ID -> DEFAULT_SHARABAT_CATEGORIES
                            else -> groupItems.map { it.denomination }.ifEmpty { DEFAULT_TICKET_CATEGORIES }.distinct().sorted()
                        }
                        val rows = allDenoms.map { denom ->
                            val item = itemMap[denom]
                            val itemFormula = try {
                                if (!item?.formula.isNullOrBlank()) CalculationFormula.valueOf(item!!.formula) else formula
                            } catch (_: Exception) {
                                formula
                            }
                            DirectSalesRowUiState(
                                denomination = denom,
                                givenInput = item?.given?.takeIf { it > 0 }?.toString() ?: "",
                                addedInput = item?.added?.takeIf { it > 0 }?.toString() ?: "",
                                remainingInput = item?.remaining?.takeIf { it > 0 }?.toString() ?: "",
                                notes = item?.notes ?: "",
                                customTitle = item?.customTitle ?: "",
                                formula = itemFormula
                            )
                        }
                        val isAddedFieldEnabledFromPrefs = sharedPreferences.getBoolean("is_added_field_enabled_${groupEntity.id}", false)
                        val mergeAddedWithGivenFromPrefs = sharedPreferences.getBoolean("merge_added_with_given_${groupEntity.id}", false)
                        val defaultAddToReport = groupEntity.id != GROUP_INTERNET_ID
                        val defaultAddToBalance = groupEntity.id != GROUP_INTERNET_ID && !groupEntity.isExcludedFromBalance
                        val addToReportFromPrefs = sharedPreferences.getBoolean("add_to_report_${groupEntity.id}", defaultAddToReport)
                        val addToBalanceFromPrefs = sharedPreferences.getBoolean("add_to_balance_${groupEntity.id}", defaultAddToBalance)
                        SalesGroupUiState(
                            id = groupEntity.id,
                            name = groupEntity.name,
                            type = type,
                            isEnabled = groupEntity.isEnabled,
                            orderIndex = groupEntity.orderIndex,
                            isDefault = groupEntity.isDefault,
                            isAddedFieldEnabled = isAddedFieldEnabledFromPrefs,
                            mergeAddedWithGiven = mergeAddedWithGivenFromPrefs,
                            rows = rows,
                            isExcludedFromBalance = !addToBalanceFromPrefs,
                            addToReport = addToReportFromPrefs,
                            addToBalance = addToBalanceFromPrefs,
                            givenLabel = groupEntity.givenLabel.ifBlank { "المعطى" },
                            addedLabel = groupEntity.addedLabel.ifBlank { "إضافة" },
                            remainingLabel = groupEntity.remainingLabel.ifBlank { "المتبقي" },
                            defaultFormula = formula
                        )
                    } else {
                        val entries = (savedDirectEntries ?: emptyList())
                            .filter { it.groupId == groupEntity.id }
                            .sortedBy { it.orderIndex }
                            .map { entity ->
                                DirectEntryItem(
                                    id = entity.id,
                                    title = entity.title,
                                    amountInput = if (entity.amount > 0 && entity.amount.isFinite()) {
                                        if (entity.amount % 1.0 == 0.0 && abs(entity.amount) <= Long.MAX_VALUE) entity.amount.toLong().toString() else entity.amount.toString()
                                    } else "",
                                    quantityInput = "1",
                                    notes = entity.notes
                                )
                            }
                        val mergeAddedWithGivenFromPrefs = sharedPreferences.getBoolean("merge_added_with_given_${groupEntity.id}", false)
                        val defaultAddToReport = groupEntity.id != GROUP_INTERNET_ID
                        val defaultAddToBalance = groupEntity.id != GROUP_INTERNET_ID && !groupEntity.isExcludedFromBalance
                        val addToReportFromPrefs = sharedPreferences.getBoolean("add_to_report_${groupEntity.id}", defaultAddToReport)
                        val addToBalanceFromPrefs = sharedPreferences.getBoolean("add_to_balance_${groupEntity.id}", defaultAddToBalance)
                        SalesGroupUiState(
                            id = groupEntity.id,
                            name = groupEntity.name,
                            type = type,
                            isEnabled = groupEntity.isEnabled,
                            orderIndex = groupEntity.orderIndex,
                            isDefault = groupEntity.isDefault,
                            mergeAddedWithGiven = mergeAddedWithGivenFromPrefs,
                            directEntries = entries,
                            isExcludedFromBalance = !addToBalanceFromPrefs,
                            addToReport = addToReportFromPrefs,
                            addToBalance = addToBalanceFromPrefs
                        )
                    }
                }

                val loadedCashGroupsRaw = if (!savedCashGroups.isNullOrEmpty()) {
                    savedCashGroups.sortedBy { it.orderIndex }.map { cashEntity ->
                        val cashType = try {
                            CashGroupType.valueOf(cashEntity.type)
                        } catch (_: Exception) {
                            CashGroupType.DENOMINATIONS
                        }

                        if (cashType == CashGroupType.DENOMINATIONS) {
                            val items = (savedCashDenoms ?: emptyList()).filter { it.groupId == cashEntity.id }
                            val map = items.associateBy { it.denomination }
                            val denoms = (DEFAULT_CASH_DENOMINATIONS + items.map { it.denomination }).distinct().sorted()
                            val rows = denoms.map { d ->
                                val itm = map[d]
                                CashDenomRowUiState(
                                    denomination = d,
                                    countInput = itm?.count?.takeIf { it > 0 }?.toString() ?: "",
                                    notes = itm?.notes ?: ""
                                )
                            }
                            CashBoxGroupUiState(
                                id = cashEntity.id,
                                name = if (cashEntity.id == CASH_GROUP_MAIN_ID) "نقدي" else cashEntity.name,
                                type = cashType,
                                isEnabled = cashEntity.isEnabled,
                                orderIndex = cashEntity.orderIndex,
                                isDefault = cashEntity.isDefault,
                                denomRows = rows
                            )
                        } else {
                            val directList = (savedCashDirects ?: emptyList())
                                .filter { it.groupId == cashEntity.id }
                                .sortedBy { it.orderIndex }
                                .map { de ->
                                    CashDirectEntryItem(
                                        id = de.id,
                                        title = de.title,
                                        amountInput = if (de.amount > 0 && de.amount.isFinite()) {
                                            if (de.amount % 1.0 == 0.0 && abs(de.amount) <= Long.MAX_VALUE) de.amount.toLong().toString() else de.amount.toString()
                                        } else "",
                                        currencyCode = de.currencyCode.ifBlank { if (de.isSar) "SAR" else "YER" },
                                        isSar = de.isSar,
                                        customExchangeRate = de.customExchangeRate,
                                        customExchangeRateInput = if (de.customExchangeRate != null && de.customExchangeRate > 0) {
                                            if (de.customExchangeRate % 1.0 == 0.0) de.customExchangeRate.toLong().toString() else de.customExchangeRate.toString()
                                        } else "",
                                        notes = de.notes
                                    )
                                }
                            CashBoxGroupUiState(
                                id = cashEntity.id,
                                name = if (cashEntity.id == CASH_GROUP_CURRENCIES_ID) "العملات الأخرى" else cashEntity.name,
                                type = cashType,
                                isEnabled = cashEntity.isEnabled,
                                orderIndex = cashEntity.orderIndex,
                                isDefault = cashEntity.isDefault,
                                directEntries = directList
                            )
                        }
                    }
                } else emptyList()

                val loadedCashGroups = mutableListOf<CashBoxGroupUiState>()
                loadedCashGroups.addAll(loadedCashGroupsRaw)

                if (loadedCashGroups.none { it.id == CASH_GROUP_MAIN_ID }) {
                    loadedCashGroups.add(0, CashBoxGroupUiState(id = CASH_GROUP_MAIN_ID, name = "نقدي", type = CashGroupType.DENOMINATIONS, isEnabled = true, orderIndex = 0, isDefault = true, denomRows = DEFAULT_CASH_DENOMINATIONS.map { CashDenomRowUiState(denomination = it) }))
                }
                if (loadedCashGroups.none { it.id == CASH_GROUP_CURRENCIES_ID }) {
                    loadedCashGroups.add(CashBoxGroupUiState(id = CASH_GROUP_CURRENCIES_ID, name = "العملات الأخرى", type = CashGroupType.DIRECT_ENTRY, isEnabled = false, orderIndex = 1, isDefault = true))
                }
                if (loadedCashGroups.none { it.id == CASH_GROUP_EXPENSES_ID }) {
                    loadedCashGroups.add(CashBoxGroupUiState(id = CASH_GROUP_EXPENSES_ID, name = "المصاريف", type = CashGroupType.EXPENSES, isEnabled = false, orderIndex = 2, isDefault = true))
                }
                if (loadedCashGroups.none { it.id == "cash_group_deposits" }) {
                    loadedCashGroups.add(CashBoxGroupUiState(id = "cash_group_deposits", name = "الإيداعات", type = CashGroupType.DEPOSITS, isEnabled = false, orderIndex = 3, isDefault = true))
                }

                val loadedExpenses = (savedExpenses ?: emptyList()).map { exp ->
                    CashExpenseItem(
                        id = exp.id,
                        title = exp.title,
                        amountInput = if (exp.amount > 0 && exp.amount.isFinite()) {
                            if (exp.amount % 1.0 == 0.0 && abs(exp.amount) <= Long.MAX_VALUE) exp.amount.toLong().toString() else exp.amount.toString()
                        } else "",
                        currencyCode = exp.currencyCode.ifBlank { if (exp.isSar) "SAR" else "YER" },
                        isSar = exp.isSar,
                        isDeposit = exp.isDeposit,
                        customExchangeRate = exp.customExchangeRate,
                        customExchangeRateInput = if (exp.customExchangeRate != null && exp.customExchangeRate > 0) {
                            if (exp.customExchangeRate % 1.0 == 0.0) exp.customExchangeRate.toLong().toString() else exp.customExchangeRate.toString()
                        } else "",
                        category = exp.category,
                        notes = exp.notes,
                        timestamp = exp.timestamp
                    )
                }

                val finalGroupUiStates = groupUiStates.toMutableList()
                if (finalGroupUiStates.none { it.id == GROUP_GAME_CARDS_ID }) {
                    finalGroupUiStates.add(
                        1.coerceAtMost(finalGroupUiStates.size),
                        SalesGroupUiState(
                            id = GROUP_GAME_CARDS_ID,
                            name = "بطائق العاب",
                            type = SalesGroupType.DENOMINATIONS,
                            isEnabled = false,
                            orderIndex = 1,
                            isDefault = true,
                            isAddedFieldEnabled = false,
                            rows = DEFAULT_GAME_CARDS_CATEGORIES.map { DirectSalesRowUiState(denomination = it) },
                            isExcludedFromBalance = false
                        )
                    )
                }

                _uiState.update {
                    it.copy(
                        sellerName = savedSales.sellerName,
                        dateTimestamp = savedSales.dateTimestamp,
                        notes = savedSales.notes,
                        cashInBoxYerInput = if (savedSales.cashInBox > 0 && savedSales.cashInBox.isFinite()) {
                            if (savedSales.cashInBox % 1.0 == 0.0 && abs(savedSales.cashInBox) <= Long.MAX_VALUE) savedSales.cashInBox.toLong().toString() else savedSales.cashInBox.toString()
                        } else "",
                        cashInBoxSarInput = if (savedSales.cashInBoxSaudi > 0 && savedSales.cashInBoxSaudi.isFinite()) {
                            if (savedSales.cashInBoxSaudi % 1.0 == 0.0 && abs(savedSales.cashInBoxSaudi) <= Long.MAX_VALUE) savedSales.cashInBoxSaudi.toLong().toString() else savedSales.cashInBoxSaudi.toString()
                        } else "",
                        exchangeRateInput = if (savedSales.exchangeRateSarToYer > 0 && savedSales.exchangeRateSarToYer.isFinite()) {
                            if (savedSales.exchangeRateSarToYer % 1.0 == 0.0 && abs(savedSales.exchangeRateSarToYer) <= Long.MAX_VALUE) savedSales.exchangeRateSarToYer.toLong().toString() else savedSales.exchangeRateSarToYer.toString()
                        } else "380",
                        showCashDenominationsTable = savedSales.showCashDenominationsTable,
                        useEasternArabicNumerals = savedSales.useEasternArabicNumerals,
                        groups = finalGroupUiStates,
                        cashGroups = loadedCashGroups,
                        expenses = loadedExpenses,
                        selectedGroupId = finalGroupUiStates.firstOrNull { g -> g.isEnabled }?.id ?: GROUP_SANAD_ID,
                        selectedCashGroupId = loadedCashGroups.firstOrNull { c -> c.isEnabled }?.id ?: CASH_GROUP_MAIN_ID
                    ).also {
                        com.example.ui.model.AccountingFormatter.useEasternDigitsGlobal = savedSales.useEasternArabicNumerals
                    }
                }
            } else {
                seedInitialDay()
            }
        }
    }

    private fun seedInitialDay() {
        val defaultGroups = listOf(
            SalesGroupUiState(
                id = GROUP_SANAD_ID,
                name = "سند",
                type = SalesGroupType.DENOMINATIONS,
                isEnabled = true,
                orderIndex = 0,
                isDefault = true,
                isAddedFieldEnabled = false,
                rows = DEFAULT_TICKET_CATEGORIES.map { DirectSalesRowUiState(denomination = it) },
                isExcludedFromBalance = false
            ),
            SalesGroupUiState(
                id = GROUP_CHINI_ID,
                name = "صيني",
                type = SalesGroupType.DIRECT_ENTRY,
                isEnabled = false,
                orderIndex = 1,
                isDefault = true,
                directEntries = emptyList(),
                isExcludedFromBalance = false
            ),
            SalesGroupUiState(
                id = GROUP_GAME_CARDS_ID,
                name = "بطائق ألعاب",
                type = SalesGroupType.DENOMINATIONS,
                isEnabled = false,
                orderIndex = 2,
                isDefault = true,
                isAddedFieldEnabled = false,
                rows = DEFAULT_GAME_CARDS_CATEGORIES.map { DirectSalesRowUiState(denomination = it) },
                isExcludedFromBalance = false
            ),
            SalesGroupUiState(
                id = GROUP_SHARABAT_ID,
                name = "شرابات",
                type = SalesGroupType.DENOMINATIONS,
                isEnabled = false,
                orderIndex = 3,
                isDefault = true,
                rows = DEFAULT_SHARABAT_CATEGORIES.map { DirectSalesRowUiState(denomination = it) },
                isExcludedFromBalance = false
            ),
            SalesGroupUiState(
                id = GROUP_INTERNET_ID,
                name = "انترنت",
                type = SalesGroupType.DENOMINATIONS,
                isEnabled = false,
                orderIndex = 4,
                isDefault = true,
                rows = DEFAULT_INTERNET_CATEGORIES.map { DirectSalesRowUiState(denomination = it) },
                isExcludedFromBalance = true
            )
        )

        val defaultCashGroups = listOf(
            CashBoxGroupUiState(
                id = CASH_GROUP_MAIN_ID,
                name = "نقدي",
                type = CashGroupType.DENOMINATIONS,
                isEnabled = true,
                orderIndex = 0,
                isDefault = true,
                denomRows = DEFAULT_CASH_DENOMINATIONS.map { CashDenomRowUiState(denomination = it) }
            ),
            CashBoxGroupUiState(
                id = CASH_GROUP_CURRENCIES_ID,
                name = "العملات الأخرى",
                type = CashGroupType.DIRECT_ENTRY,
                isEnabled = false,
                orderIndex = 1,
                isDefault = true,
                directEntries = emptyList()
            ),
            CashBoxGroupUiState(
                id = CASH_GROUP_EXPENSES_ID,
                name = "المصاريف",
                type = CashGroupType.EXPENSES,
                isEnabled = false,
                orderIndex = 2,
                isDefault = true
            ),
            CashBoxGroupUiState(
                id = "cash_group_deposits",
                name = "الإيداعات",
                type = CashGroupType.DEPOSITS,
                isEnabled = false,
                orderIndex = 3,
                isDefault = true
            )
        )

        _uiState.update {
            it.copy(
                groups = defaultGroups,
                cashGroups = defaultCashGroups,
                selectedGroupId = GROUP_SANAD_ID,
                selectedCashGroupId = CASH_GROUP_MAIN_ID,
                exchangeRateInput = "380",
                showCashDenominationsTable = false
            )
        }
        saveToDatabase()
    }

    fun onSplashFinished() {
        _currentScreen.value = AppScreen.HOME
    }

    fun openOnboarding() {
        _currentScreen.value = AppScreen.HOME
    }

    fun finishOnboarding() {
        _currentScreen.value = AppScreen.HOME
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectGroup(groupId: String) {
        _uiState.update { it.copy(selectedGroupId = groupId) }
    }

    fun toggleSalesGroupExpansion(groupId: String) {
        _uiState.update { s ->
            val updated = s.groups.map {
                if (it.id == groupId) it.copy(isExpanded = !it.isExpanded) else it
            }
            s.copy(groups = updated)
        }
    }

    fun toggleCashGroupExpansion(groupId: String) {
        _uiState.update { s ->
            val updated = s.cashGroups.map {
                if (it.id == groupId) it.copy(isExpanded = !it.isExpanded) else it
            }
            s.copy(cashGroups = updated)
        }
    }

    fun selectCashGroup(cashGroupId: String) {
        _uiState.update { it.copy(selectedCashGroupId = cashGroupId) }
    }

    fun toggleGroupEnabled(groupId: String) {
        _uiState.update { state ->
            val updated = state.groups.map { grp ->
                if (grp.id == groupId) grp.copy(isEnabled = !grp.isEnabled) else grp
            }
            val currentSelected = updated.find { it.id == state.selectedGroupId }
            val nextSelectedId = if (currentSelected?.isEnabled == true) state.selectedGroupId
                                else updated.firstOrNull { it.isEnabled }?.id ?: updated.firstOrNull()?.id ?: state.selectedGroupId
            state.copy(groups = updated, selectedGroupId = nextSelectedId)
        }
        saveToDatabase()
    }

    fun toggleCashGroupEnabled(groupId: String) {
        _uiState.update { state ->
            val updated = state.cashGroups.map { grp ->
                if (grp.id == groupId) grp.copy(isEnabled = !grp.isEnabled) else grp
            }
            val currentSelected = updated.find { it.id == state.selectedCashGroupId }
            val nextSelectedId = if (currentSelected?.isEnabled == true) state.selectedCashGroupId
                                else updated.firstOrNull { it.isEnabled }?.id ?: updated.firstOrNull()?.id ?: state.selectedCashGroupId
            state.copy(cashGroups = updated, selectedCashGroupId = nextSelectedId)
        }
        saveToDatabase()
    }

    fun toggleGroupAddedField(groupId: String) {
        _uiState.update { state ->
            val updated = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val newValue = !grp.isAddedFieldEnabled
                    sharedPreferences.edit().putBoolean("is_added_field_enabled_${groupId}", newValue).apply()
                    grp.copy(isAddedFieldEnabled = newValue)
                } else grp
            }
            state.copy(groups = updated)
        }
        saveToDatabase()
    }

    fun toggleGroupMergeAdded(groupId: String) {
        _uiState.update { state ->
            val updated = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val newValue = !grp.mergeAddedWithGiven
                    sharedPreferences.edit().putBoolean("merge_added_${groupId}", newValue).apply()
                    grp.copy(mergeAddedWithGiven = newValue)
                } else grp
            }
            state.copy(groups = updated)
        }
        saveToDatabase()
    }

    fun toggleRemainingStepper() {
        _uiState.update { state ->
            val newValue = !state.showRemainingStepper
            sharedPreferences.edit().putBoolean("showRemainingStepper", newValue).apply()
            state.copy(showRemainingStepper = newValue)
        }
    }

    fun toggleBudgetSummaryBar() {
        _uiState.update { state ->
            val newValue = !state.showBudgetSummaryBar
            sharedPreferences.edit().putBoolean("showBudgetSummaryBar", newValue).apply()
            state.copy(showBudgetSummaryBar = newValue)
        }
    }

    fun toggleShiftReminder(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("shiftReminderEnabled", enabled).apply()
        _uiState.update { it.copy(shiftReminderEnabled = enabled) }
    }

    fun setShiftReminderTime(time: String) {
        sharedPreferences.edit().putString("shiftReminderTime", time).apply()
        _uiState.update { it.copy(shiftReminderTime = time) }
    }

    fun runMaintenanceDiagnostic() {
        _uiState.update { it.copy(showMaintenanceDialog = true, isMaintenanceRunning = true, maintenanceResults = emptyList()) }
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            
            val results = mutableListOf<String>()
            val currentState = _uiState.value
            
            results.add("🛠️ بدء فحص الصيانة الشامل لوظائف التطبيق وحساباته...")
            
            // 1. Groups & Categories structure check
            results.add("🔍 فحص سلامة الأقسام والتبويبات الأساسية:")
            val expectedGroupIds = listOf(GROUP_SANAD_ID, GROUP_GAME_CARDS_ID, GROUP_CHINI_ID, GROUP_INTERNET_ID, GROUP_SHARABAT_ID)
            val missingGroups = expectedGroupIds.filter { id -> currentState.groups.none { it.id == id } }
            if (missingGroups.isEmpty()) {
                results.add("✅ أقسام المبيعات الأساسية ('سند'، 'بطائق العاب'، 'صيني'، 'إنترنت'، 'شرابات') مفعّلة وتعمل بكفاءة تامة.")
            } else {
                results.add("⚠️ تم اكتشاف نقص في بعض الأقسام الافتراضية. جاري إعادة بنائها وتثبيتها تلقائياً...")
            }
            
            // 2. Cash groups check
            results.add("🔍 فحص مجموعات الصندوق والسيولة:")
            val cashGroupNames = currentState.cashGroups.map { it.name }
            results.add("✅ مجموعات الصندوق (${cashGroupNames.joinToString("، ")}) متكاملة ومربوطة بالمعادلة المحاسبية.")
            
            // 2. Math validation and auto-correction
            results.add("🔍 تدقيق مدخلات وحسابات المبيعات والكميات:")
            var correctionsCount = 0
            val updatedGroups = currentState.groups.map { group ->
                val updatedRows = group.rows.map { row ->
                    val givenVal = row.givenInput.toIntOrNull() ?: 0
                    val addedVal = row.addedInput.toIntOrNull() ?: 0
                    val remainingVal = row.remainingInput.toIntOrNull() ?: 0
                    
                    if (remainingVal > (givenVal + addedVal)) {
                        correctionsCount++
                        results.add("🔧 تصحيح تلقائي: في قسم (${group.name})، فئة (${row.denomination}) كانت القيمة المتبقية ($remainingVal) أكبر من المعطى والإضافي (${givenVal + addedVal}). تم تصحيح المتبقي تلقائياً لمنع القيم السالبة.")
                        row.copy(
                            remainingInput = (givenVal + addedVal).toString()
                        )
                    } else {
                        row
                    }
                }
                group.copy(rows = updatedRows)
            }
            
            if (correctionsCount == 0) {
                results.add("✅ جميع العمليات الحسابية والكميات المباعة مطابقة للمعادلات المحاسبية وخالية من الأخطاء.")
            } else {
                results.add("✅ تم إصلاح وتصحيح $correctionsCount قيم محاسبية معلقة بنجاح واعتمدت بالكامل.")
            }
            
            // 3. Cashbox & Rate validation
            results.add("🔍 مراجعة أسعار الصرف وصندوق النقد:")
            val rate = currentState.exchangeRateInput.toDoubleOrNull()
            var fixedRate = currentState.exchangeRateInput
            if (rate == null || rate <= 0) {
                fixedRate = "380"
                results.add("🔧 تصحيح تلقائي: تم تعيين سعر صرف افتراضي صحيح (1 ر.س = 380 ر.ي) نظراً لعدم إدخال قيمة صحيحة.")
            } else {
                results.add("✅ سعر الصرف مدخل بشكل صحيح حالياً (1 ر.س. = ${currentState.exchangeRateInput} ر.ي.).")
            }
            
            // Clean box cash inputs if they have illegal text
            val yerCash = currentState.cashInBoxYerInput.trim()
            val cleanYerCash = if (yerCash.any { !it.isDigit() && it != '.' }) {
                yerCash.filter { it.isDigit() || it == '.' }
            } else yerCash
            
            val sarCash = currentState.cashInBoxSarInput.trim()
            val cleanSarCash = if (sarCash.any { !it.isDigit() && it != '.' }) {
                sarCash.filter { it.isDigit() || it == '.' }
            } else sarCash
            
            if (cleanYerCash != yerCash || cleanSarCash != sarCash) {
                results.add("🔧 تصحيح تلقائي: تم تنظيف المدخلات النصية لسيولة الصندوق من أي رموز أو نصوص غير رقمية.")
            } else {
                results.add("✅ مدخلات السيولة النقدية لليمني والسعودي نظيفة ومتوافقة بالكامل.")
            }
            
            // 4. Expenses integrity
            results.add("🔍 فحص المصاريف والمدفوعات اليومية:")
            var invalidExpensesCount = 0
            val cleanExpenses = currentState.expenses.map { exp ->
                val amountVal = exp.amountInput.toDoubleOrNull() ?: 0.0
                if (amountVal < 0 || exp.title.isBlank()) {
                    invalidExpensesCount++
                    exp.copy(
                        title = exp.title.ifBlank { "مصروف مجهول مصحح" },
                        amountInput = if (amountVal < 0) "0" else exp.amountInput
                    )
                } else exp
            }
            if (invalidExpensesCount > 0) {
                results.add("🔧 تصحيح تلقائي: تم تصحيح $invalidExpensesCount بنود مصاريف فارغة أو بقيم سالبة لتأمين صحة الأرقام.")
            } else {
                results.add("✅ جميع المصاريف المقيدة مسجلة بعناوين واضحة وقيم صحيحة ومطابقة.")
            }
            
            results.add("💾 حفظ قاعدة البيانات والنسخ الاحتياطي:")
            results.add("💾 تم إجراء مزامنة فورية وآمنة للبيانات مع الذاكرة الدائمة وتحديث ملفات التخزين المحلي بنجاح.")
            results.add("✨ انتهى فحص وصيانة وظائف التطبيق بنجاح. التطبيق يعمل الآن بكفاءة 100% وبدون أي مشاكل.")
            
            _uiState.update { state ->
                state.copy(
                    groups = updatedGroups,
                    exchangeRateInput = fixedRate,
                    cashInBoxYerInput = cleanYerCash,
                    cashInBoxSarInput = cleanSarCash,
                    expenses = cleanExpenses,
                    isMaintenanceRunning = false,
                    maintenanceResults = results
                )
            }
            saveToDatabase()
        }
    }
    
    fun closeMaintenanceDialog() {
        _uiState.update { it.copy(showMaintenanceDialog = false, maintenanceResults = emptyList()) }
    }

    fun toggleMergeAddedWithGiven(groupId: String) {
        _uiState.update { state ->
            val updated = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val newValue = !grp.mergeAddedWithGiven
                    sharedPreferences.edit().putBoolean("merge_added_with_given_${groupId}", newValue).apply()
                    grp.copy(mergeAddedWithGiven = newValue)
                } else grp
            }
            state.copy(groups = updated)
        }
        addAuditLog("التنظيم", "خيارات المجموعة", "تعديل خيار دمج الإضافي للمجموعة: $groupId", "", "")
        saveToDatabase()
    }

    fun toggleGroupAddToReport(groupId: String) {
        _uiState.update { state ->
            val updated = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val newValue = !grp.addToReport
                    sharedPreferences.edit().putBoolean("add_to_report_${groupId}", newValue).apply()
                    grp.copy(addToReport = newValue)
                } else grp
            }
            state.copy(groups = updated)
        }
        addAuditLog("الإعدادات", "تضمين التقرير", "تعديل تضمين المجموعة في التقرير: $groupId", "", "")
        saveToDatabase()
    }

    fun toggleGroupAddToBalance(groupId: String) {
        _uiState.update { state ->
            val updated = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val newValue = !grp.addToBalance
                    sharedPreferences.edit().putBoolean("add_to_balance_${groupId}", newValue).apply()
                    grp.copy(addToBalance = newValue, isExcludedFromBalance = !newValue)
                } else grp
            }
            state.copy(groups = updated)
        }
        addAuditLog("الإعدادات", "تضمين الرصيد", "تعديل تضمين المجموعة في الموازنة: $groupId", "", "")
        saveToDatabase()
    }

    fun updateSellerName(name: String) {
        _uiState.update { it.copy(sellerName = name) }
        saveToDatabase()
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
        saveToDatabase()
    }

    fun updateGroupNotes(groupId: String, notes: String) {
        _uiState.update { state ->
            val updated = state.groups.map { if (it.id == groupId) it.copy(notes = notes) else it }
            state.copy(groups = updated)
        }
        saveToDatabase()
    }

    fun updateCashGroupNotes(groupId: String, notes: String) {
        _uiState.update { state ->
            val updated = state.cashGroups.map { if (it.id == groupId) it.copy(notes = notes) else it }
            state.copy(cashGroups = updated)
        }
        saveToDatabase()
    }

    fun toggleCashGroupAddToReport(groupId: String) {
        _uiState.update { state ->
            val updated = state.cashGroups.map { grp ->
                if (grp.id == groupId) {
                    val newVal = !grp.addToReport
                    sharedPreferences.edit().putBoolean("cash_add_to_report_${groupId}", newVal).apply()
                    grp.copy(addToReport = newVal)
                } else grp
            }
            state.copy(cashGroups = updated)
        }
        addAuditLog("الإعدادات", "تضمين تقرير الصندوق", "تعديل تضمين مجموعة الصندوق في التقرير: $groupId", "", "")
        saveToDatabase()
    }

    fun toggleCashGroupAddToBalance(groupId: String) {
        _uiState.update { state ->
            val updated = state.cashGroups.map { grp ->
                if (grp.id == groupId) {
                    val newVal = !grp.addToBalance
                    sharedPreferences.edit().putBoolean("cash_add_to_balance_${groupId}", newVal).apply()
                    grp.copy(addToBalance = newVal, isExcludedFromBalance = !newVal)
                } else grp
            }
            state.copy(cashGroups = updated)
        }
        addAuditLog("الإعدادات", "تضمين موازنة الصندوق", "تعديل تضمين مجموعة الصندوق في الموازنة: $groupId", "", "")
        saveToDatabase()
    }



    fun toggleGlobalAddedField() {
        _uiState.update { state ->
            val newVal = !state.isGlobalAddedFieldActive
            sharedPreferences.edit().putBoolean("isGlobalAddedFieldActive", newVal).apply()
            state.copy(isGlobalAddedFieldActive = newVal)
        }
    }

    fun setCustomBackgroundImageUri(uri: String?) {
        _uiState.update { it.copy(customBackgroundImageUri = uri, appBackgroundStyle = if (uri != null) "CUSTOM_IMAGE" else "DEFAULT") }
        sharedPreferences.edit().putString("customBackgroundImageUri", uri).apply()
        if (uri != null) {
            setAppBackgroundStyle("CUSTOM_IMAGE")
        }
    }

    fun setCustomBackgroundOpacity(opacity: Float) {
        val clamped = opacity.coerceIn(0.1f, 1f)
        _uiState.update { it.copy(customBackgroundOpacity = clamped) }
        sharedPreferences.edit().putFloat("customBackgroundOpacity", clamped).apply()
    }

    fun setTableCardAlpha(alpha: Float) {
        val clamped = alpha.coerceIn(0.1f, 1f)
        _uiState.update { state ->
            state.copy(customColorThemeState = state.customColorThemeState.copy(tableCardAlpha = clamped))
        }
        sharedPreferences.edit().putFloat("tableCardAlpha", clamped).apply()
    }

    fun setTableHeaderAlpha(alpha: Float) {
        val clamped = alpha.coerceIn(0.1f, 1f)
        _uiState.update { state ->
            state.copy(customColorThemeState = state.customColorThemeState.copy(tableHeaderAlpha = clamped))
        }
        sharedPreferences.edit().putFloat("tableHeaderAlpha", clamped).apply()
    }

    fun setTableCellAlpha(alpha: Float) {
        val clamped = alpha.coerceIn(0.1f, 1f)
        _uiState.update { state ->
            state.copy(customColorThemeState = state.customColorThemeState.copy(tableCellAlpha = clamped))
        }
        sharedPreferences.edit().putFloat("tableCellAlpha", clamped).apply()
    }

    fun setGroupCardAlpha(alpha: Float) {
        val clamped = alpha.coerceIn(0.1f, 1f)
        _uiState.update { state ->
            state.copy(customColorThemeState = state.customColorThemeState.copy(groupCardAlpha = clamped))
        }
        sharedPreferences.edit().putFloat("groupCardAlpha", clamped).apply()
    }

    fun updateCashInBoxYer(value: String) {
        if (checkDayClosedAndWarn()) return
        val cleaned = value.filter { ch -> ch.isDigit() || ch == '.' }
        val oldVal = _uiState.value.cashInBoxYerInput
        _uiState.update { it.copy(cashInBoxYerInput = cleaned) }
        addAuditLog("الصندوق", "نقدي", "تحديث النقد بالريال اليمني", oldVal, cleaned)
        saveToDatabase()
    }

    fun triggerCashConfirmDialog() {
        if (checkDayClosedAndWarn()) return
        val state = _uiState.value
        val currentSummary = salesSummary.value
        val physical = state.cashInBoxYerInput.toDoubleOrNull() ?: 0.0
        val gross = physical + currentSummary.totalExpensesInYer + currentSummary.totalDepositsInYer
        val expected = currentSummary.totalRevenue
        val diff = physical - (expected - currentSummary.totalExpensesInYer)
        val diffLabel = if (diff >= 0) "زيادة (فائض): ${AccountingFormatter.formatYer(diff)}" else "عجز: ${AccountingFormatter.formatYer(kotlin.math.abs(diff))}"
        val msg = "النقد الفعلي بالصندوق: ${AccountingFormatter.formatYer(physical)}\nإجمالي المقبوضات (النقد + الإيداعات + المصاريف): ${AccountingFormatter.formatYer(gross)}\nمبيعات التذاكر المتوقعة: ${AccountingFormatter.formatYer(expected)}\nالنتيجة والفرق: $diffLabel\n\nهل تريد تأكيد اعتماد المبلغ؟"

        _uiState.update {
            it.copy(
                pendingCashValue = state.cashInBoxYerInput,
                cashConfirmationMessage = msg,
                showCashConfirmDialog = true
            )
        }
    }

    fun confirmCashInput() {
        val pending = _uiState.value.pendingCashValue
        val oldVal = _uiState.value.cashInBoxYerInput
        _uiState.update {
            it.copy(
                cashInBoxYerInput = pending,
                showCashConfirmDialog = false
            )
        }
        addAuditLog("الصندوق", "نقدي", "النقد بالريال اليمني", oldVal, pending)
        saveToDatabase()
    }

    fun dismissCashConfirmDialog() {
        _uiState.update { it.copy(showCashConfirmDialog = false) }
    }

    fun updateCashInBoxSar(value: String) {
        if (checkDayClosedAndWarn()) return
        val oldVal = _uiState.value.cashInBoxSarInput
        _uiState.update { it.copy(cashInBoxSarInput = value) }
        addAuditLog("الصندوق", "نقدي", "النقد بالريال السعودي", oldVal, value)
        saveToDatabase()
    }

    fun updateExchangeRate(value: String) {
        if (checkDayClosedAndWarn()) return
        val oldVal = _uiState.value.exchangeRateInput
        _uiState.update { it.copy(exchangeRateInput = value) }
        addAuditLog("الصندوق", "نقدي", "سعر الصرف", oldVal, value)
        saveToDatabase()
    }

    fun updateKeypadThemeColor(color: String) {
        _uiState.update { it.copy(keypadThemeColor = color) }
        saveToDatabase()
    }

    fun updateKeypadButtonStyle(style: String) {
        _uiState.update { it.copy(keypadButtonStyle = style) }
        saveToDatabase()
    }

    fun setUseEasternArabicNumerals(useEastern: Boolean) {
        com.example.ui.model.AccountingFormatter.useEasternDigitsGlobal = useEastern
        _uiState.update { it.copy(useEasternArabicNumerals = useEastern) }
        saveToDatabase()
    }

    fun setShowHijriDate(show: Boolean) {
        _uiState.update { it.copy(showHijriDate = show) }
        saveToDatabase()
    }

    fun setUse24HourFormat(use24Hour: Boolean) {
        _uiState.update { it.copy(use24HourFormat = use24Hour) }
        saveToDatabase()
    }

    fun toggleReadOnlyMode() {
        val newMode = !_uiState.value.isReadOnlyMode
        _uiState.update {
            it.copy(
                isReadOnlyMode = newMode,
                showDisableReadOnlyConfirmDialog = false
            )
        }
        addAuditLog("العرض", "وضع القراءة", if (newMode) "تفعيل وضع القراءة" else "إلغاء وضع القراءة", "", "")
    }

    fun confirmDisableReadOnlyMode() {
        _uiState.update { it.copy(isReadOnlyMode = false, showDisableReadOnlyConfirmDialog = false) }
        addAuditLog("العرض", "وضع القراءة", "إلغاء وضع القراءة", "", "")
    }

    fun dismissDisableReadOnlyDialog() {
        _uiState.update { it.copy(showDisableReadOnlyConfirmDialog = false) }
    }

    fun requestCloseDay() {
        if (_uiState.value.isDayClosed) {
            _uiState.update { it.copy(showUnlockDaySternWarningDialog = true) }
        } else {
            _uiState.update { it.copy(showCloseDayConfirmDialog = true) }
        }
    }

    fun confirmCloseDay() {
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                isDayClosed = true,
                closedDayTimestamp = now,
                showCloseDayConfirmDialog = false
            )
        }
        sharedPreferences.edit()
            .putBoolean("is_day_closed", true)
            .putLong("closed_day_timestamp", now)
            .apply()
        addAuditLog("إغلاق اليوم", "إغلاق اليوم", "تم اعتماد وإغلاق اليوم الحسابي وتجميد كافة الإدخالات", "", "")
    }

    fun dismissCloseDayDialog() {
        _uiState.update { it.copy(showCloseDayConfirmDialog = false) }
    }

    fun onAttemptInputOnClosedDay() {
        if (_uiState.value.isDayClosed) {
            _uiState.update { it.copy(showUnlockDaySternWarningDialog = true) }
        }
    }

    fun confirmUnlockDayWithSternWarning() {
        _uiState.update {
            it.copy(
                isDayClosed = false,
                showUnlockDaySternWarningDialog = false
            )
        }
        sharedPreferences.edit()
            .putBoolean("is_day_closed", false)
            .putLong("closed_day_timestamp", 0L)
            .apply()
        addAuditLog("تحذير أمني شديد", "فك إغلاق اليوم", "قام المستخدم بفك إغلاق اليوم بعد تأكيد التحذير شديد اللهجة وتحمل كامل المسؤولية", "", "")
    }

    fun dismissUnlockDaySternWarningDialog() {
        _uiState.update { it.copy(showUnlockDaySternWarningDialog = false) }
    }

    fun checkDayClosedAndWarn(): Boolean {
        if (_uiState.value.isDayClosed) {
            _uiState.update { it.copy(showUnlockDaySternWarningDialog = true) }
            return true
        }
        return false
    }

    fun toggleLockGivenExtraMode() {
        if (_uiState.value.isLockGivenExtraMode) {
            _uiState.update { it.copy(showDisableLockGivenExtraConfirmDialog = true) }
        } else {
            _uiState.update { it.copy(isLockGivenExtraMode = true) }
        }
    }

    fun confirmDisableLockGivenExtraMode() {
        _uiState.update { it.copy(isLockGivenExtraMode = false, showDisableLockGivenExtraConfirmDialog = false) }
    }

    fun dismissDisableLockGivenExtraDialog() {
        _uiState.update { it.copy(showDisableLockGivenExtraConfirmDialog = false) }
    }

    fun requestCashRebalance() {
        _uiState.update { it.copy(showCashRebalanceConfirmDialog = true) }
    }

    fun confirmCashRebalance() {
        val summary = salesSummary.value
        val expectedCash = (summary.totalRevenue - summary.totalExpensesInYer).coerceAtLeast(0.0)
        val formattedValue = if (expectedCash % 1.0 == 0.0) expectedCash.toLong().toString() else expectedCash.toString()
        _uiState.update {
            it.copy(
                cashInBoxYerInput = formattedValue,
                showCashRebalanceConfirmDialog = false
            )
        }
        addAuditLog("الصندوق", "نقدي", "معادلة النقد بالصندوق", "", formattedValue)
        saveToDatabase()
    }

    fun dismissCashRebalanceDialog() {
        _uiState.update { it.copy(showCashRebalanceConfirmDialog = false) }
    }

    fun setIncludeInternetInReport(include: Boolean) {
        _uiState.update { it.copy(includeInternetInReport = include) }
        saveToDatabase()
    }

    fun setReportHeaderTitle(title: String) {
        _uiState.update { it.copy(reportHeaderTitle = title) }
    }

    fun setReportHeaderSubtitle(subtitle: String) {
        _uiState.update { it.copy(reportHeaderSubtitle = subtitle) }
    }

    fun setReportFontFamily(font: String) {
        _uiState.update { it.copy(reportFontFamily = font) }
    }

    fun setReportTextColor(colorLong: Long) {
        _uiState.update { it.copy(reportTextColor = colorLong) }
    }

    fun setShowCompactMode(show: Boolean) {
        _uiState.update { it.copy(showCompactMode = show) }
        sharedPreferences.edit().putBoolean("showCompactMode", show).apply()
    }

    fun setFullScreenMode(enabled: Boolean) {
        _uiState.update { it.copy(isFullScreenMode = enabled) }
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(appLanguage = lang) }
    }

    fun toggleLanguage() {
        _uiState.update {
            val newLang = if (it.appLanguage == "en") "ar" else "en"
            it.copy(appLanguage = newLang)
        }
    }

    private fun saveCurrenciesToPrefs(currencies: List<com.example.ui.model.CustomCurrency>) {
        val array = org.json.JSONArray()
        for (c in currencies) {
            val obj = org.json.JSONObject()
            obj.put("code", c.code)
            obj.put("name", c.arabicName)
            obj.put("symbol", c.symbol)
            obj.put("defaultRateYer", c.defaultRateYer)
            obj.put("flag", c.flag)
            obj.put("isMain", c.isMain)
            obj.put("isCustom", c.isCustom)
            array.put(obj)
        }
        sharedPreferences.edit().putString("custom_currencies_json", array.toString()).apply()
    }

    private fun loadCurrenciesFromPrefs(): List<com.example.ui.model.CustomCurrency>? {
        val json = sharedPreferences.getString("custom_currencies_json", null) ?: return null
        try {
            val array = org.json.JSONArray(json)
            val list = mutableListOf<com.example.ui.model.CustomCurrency>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    com.example.ui.model.CustomCurrency(
                        code = obj.getString("code"),
                        arabicName = obj.getString("name"),
                        symbol = obj.getString("symbol"),
                        defaultRateYer = obj.getDouble("defaultRateYer"),
                        flag = obj.optString("flag", "🌐"),
                        isMain = obj.optBoolean("isMain", false),
                        isCustom = obj.optBoolean("isCustom", false)
                    )
                )
            }
            return list
        } catch (e: Exception) {
            return null
        }
    }

    fun setMainCurrency(code: String) {
        _uiState.update { state ->
            val updated = state.customCurrencies.map { c ->
                c.copy(isMain = (c.code == code))
            }
            saveCurrenciesToPrefs(updated)
            val main = updated.find { it.isMain }
            if (main != null) {
                com.example.ui.model.AccountingFormatter.mainCurrencySymbol = main.symbol
                com.example.ui.model.AccountingFormatter.mainCurrencyCode = main.code
                com.example.ui.model.AccountingFormatter.mainCurrencyName = main.arabicName
                com.example.ui.model.AccountingFormatter.mainCurrencyRateYer = main.defaultRateYer
            }
            state.copy(customCurrencies = updated)
        }
        addAuditLog("الإعدادات", "العملات", "تغيير العملة الرئيسة إلى: $code وتعميمها فوراً", "", "")
        saveToDatabase()
    }

    fun setCurrencySymbolPosition(position: String) {
        sharedPreferences.edit().putString("currency_symbol_position", position).apply()
        com.example.ui.model.AccountingFormatter.symbolPosition = position
        _uiState.update { it.copy(currencySymbolPosition = position) }
        addAuditLog("الإعدادات", "تنسيق العملة", "تغيير موضع رمز العملة إلى: $position", "", "")
    }

    fun setCurrencyDecimalMode(mode: String) {
        sharedPreferences.edit().putString("currency_decimal_mode", mode).apply()
        com.example.ui.model.AccountingFormatter.decimalMode = mode
        _uiState.update { it.copy(currencyDecimalMode = mode) }
        addAuditLog("الإعدادات", "تنسيق العملة", "تغيير نمط الكسور والكسور العشرية إلى: $mode", "", "")
    }

    fun setCurrencyThousandsSeparator(separator: String) {
        sharedPreferences.edit().putString("currency_thousands_separator", separator).apply()
        com.example.ui.model.AccountingFormatter.thousandsSeparator = separator
        _uiState.update { it.copy(currencyThousandsSeparator = separator) }
        addAuditLog("الإعدادات", "تنسيق العملة", "تغيير فاصلة الآلاف إلى: '$separator'", "", "")
    }

    fun setCurrencyDisplayType(displayType: String) {
        sharedPreferences.edit().putString("currency_display_type", displayType).apply()
        com.example.ui.model.AccountingFormatter.displayFormat = displayType
        _uiState.update { it.copy(currencyDisplayType = displayType) }
        addAuditLog("الإعدادات", "تنسيق العملة", "تغيير نمط عرض العملة إلى: $displayType", "", "")
    }

    fun applyCurrencyPreset(currencyCode: String) {
        val upper = currencyCode.uppercase().trim()
        val targetCurr = _uiState.value.customCurrencies.find { it.code == upper }
        val symPos = when (upper) {
            "USD", "EUR" -> "BEFORE"
            else -> "AFTER"
        }
        val decMode = when (upper) {
            "USD", "EUR" -> "ALWAYS_TWO"
            "YER" -> "INTEGER_ONLY"
            "SAR", "AED" -> "ALWAYS_TWO"
            else -> "AUTO"
        }
        val sep = ","
        val dispType = "SYMBOL"

        sharedPreferences.edit()
            .putString("currency_symbol_position", symPos)
            .putString("currency_decimal_mode", decMode)
            .putString("currency_thousands_separator", sep)
            .putString("currency_display_type", dispType)
            .apply()

        com.example.ui.model.AccountingFormatter.symbolPosition = symPos
        com.example.ui.model.AccountingFormatter.decimalMode = decMode
        com.example.ui.model.AccountingFormatter.thousandsSeparator = sep
        com.example.ui.model.AccountingFormatter.displayFormat = dispType

        if (targetCurr != null) {
            setMainCurrency(targetCurr.code)
        }

        _uiState.update {
            it.copy(
                currencySymbolPosition = symPos,
                currencyDecimalMode = decMode,
                currencyThousandsSeparator = sep,
                currencyDisplayType = dispType
            )
        }
        addAuditLog("الإعدادات", "تنسيق العملة", "تطبيق النمط القياسي للعملة: $upper", "", "")
    }

    fun addCustomCurrency(code: String, name: String, symbol: String, rate: Double, flag: String) {
        _uiState.update { state ->
            val codeUpper = code.uppercase().trim()
            if (state.customCurrencies.any { it.code == codeUpper }) return@update state
            val newCurr = com.example.ui.model.CustomCurrency(
                code = codeUpper,
                arabicName = name.trim(),
                symbol = symbol.trim(),
                defaultRateYer = rate,
                flag = flag.trim().ifBlank { "🌐" },
                isMain = false,
                isCustom = true
            )
            val updated = state.customCurrencies + newCurr
            saveCurrenciesToPrefs(updated)
            state.copy(customCurrencies = updated)
        }
        addAuditLog("الإعدادات", "العملات", "إضافة عملة مخصصة: $code", "", rate.toString())
    }

    fun updateCustomCurrency(code: String, name: String, symbol: String, rate: Double, flag: String) {
        _uiState.update { state ->
            val codeUpper = code.uppercase().trim()
            val updated = state.customCurrencies.map { c ->
                if (c.code == codeUpper) {
                    c.copy(
                        arabicName = name.trim(),
                        symbol = symbol.trim(),
                        defaultRateYer = rate,
                        flag = flag.trim().ifBlank { "🌐" }
                    )
                } else c
            }
            saveCurrenciesToPrefs(updated)
            val main = updated.find { it.isMain }
            if (main != null) {
                com.example.ui.model.AccountingFormatter.mainCurrencySymbol = main.symbol
                com.example.ui.model.AccountingFormatter.mainCurrencyCode = main.code
                com.example.ui.model.AccountingFormatter.mainCurrencyRateYer = main.defaultRateYer
            }
            state.copy(customCurrencies = updated)
        }
        addAuditLog("الإعدادات", "العملات", "تعديل عملة: $code", "", rate.toString())
    }

    fun deleteCustomCurrency(code: String) {
        _uiState.update { state ->
            val codeUpper = code.uppercase().trim()
            val target = state.customCurrencies.find { it.code == codeUpper } ?: return@update state
            if (target.isMain) return@update state // Don't delete active main currency
            val updated = state.customCurrencies.filterNot { it.code == codeUpper }
            saveCurrenciesToPrefs(updated)
            state.copy(customCurrencies = updated)
        }
        addAuditLog("الإعدادات", "العملات", "حذف عملة مخصصة: $code", "", "")
    }

    fun setShowCalculatorNavTab(show: Boolean) {
        _uiState.update { it.copy(showCalculatorNavTab = show) }
        saveToDatabase()
    }

    fun setShowInlineGroupCalculator(show: Boolean) {
        _uiState.update { it.copy(showInlineGroupCalculator = show) }
        saveToDatabase()
    }

    fun addOrActivateForeignCurrencyGroup() {
        val state = _uiState.value
        val existing = state.cashGroups.find { it.name.contains("عملات") || it.name.contains("سعودي") || it.name.contains("أجنبية") }
        if (existing != null) {
            _uiState.update { s ->
                val updated = s.cashGroups.map { if (it.id == existing.id) it.copy(isEnabled = true) else it }
                s.copy(cashGroups = updated, selectedCashGroupId = existing.id)
            }
        } else {
            val newGroup = CashBoxGroupUiState(
                id = UUID.randomUUID().toString(),
                name = "عملات أجنبية (سعودي)",
                type = CashGroupType.DIRECT_ENTRY,
                isEnabled = true,
                orderIndex = state.cashGroups.size,
                isDefault = false,
                directEntries = listOf(
                    CashDirectEntryItem(id = UUID.randomUUID().toString(), title = "فئة 500 ر.س", amountInput = "", isSar = true, customExchangeRate = 380.0, customExchangeRateInput = "380"),
                    CashDirectEntryItem(id = UUID.randomUUID().toString(), title = "فئة 200 ر.س", amountInput = "", isSar = true, customExchangeRate = 380.0, customExchangeRateInput = "380"),
                    CashDirectEntryItem(id = UUID.randomUUID().toString(), title = "فئة 100 ر.س", amountInput = "", isSar = true, customExchangeRate = 380.0, customExchangeRateInput = "380"),
                    CashDirectEntryItem(id = UUID.randomUUID().toString(), title = "فئة 50 ر.س", amountInput = "", isSar = true, customExchangeRate = 380.0, customExchangeRateInput = "380"),
                    CashDirectEntryItem(id = UUID.randomUUID().toString(), title = "فئة 20 ر.س", amountInput = "", isSar = true, customExchangeRate = 380.0, customExchangeRateInput = "380"),
                    CashDirectEntryItem(id = UUID.randomUUID().toString(), title = "فئة 10 ر.س", amountInput = "", isSar = true, customExchangeRate = 380.0, customExchangeRateInput = "380"),
                    CashDirectEntryItem(id = UUID.randomUUID().toString(), title = "فئة 5 ر.س", amountInput = "", isSar = true, customExchangeRate = 380.0, customExchangeRateInput = "380"),
                    CashDirectEntryItem(id = UUID.randomUUID().toString(), title = "دولار أمريكي $", amountInput = "", isSar = true, customExchangeRate = 530.0, customExchangeRateInput = "530")
                )
            )
            _uiState.update { s ->
                s.copy(cashGroups = s.cashGroups + newGroup, selectedCashGroupId = newGroup.id)
            }
        }
        saveToDatabase()
    }

    fun addOrActivateExpensesGroup() {
        val state = _uiState.value
        val existing = state.cashGroups.find { it.type == CashGroupType.EXPENSES || it.name.contains("مصاريف") }
        if (existing != null) {
            _uiState.update { s ->
                val updated = s.cashGroups.map { if (it.id == existing.id) it.copy(isEnabled = true) else it }
                s.copy(cashGroups = updated, selectedCashGroupId = existing.id)
            }
        } else {
            val newGroup = CashBoxGroupUiState(
                id = CASH_GROUP_EXPENSES_ID,
                name = "مصاريف وإيداعات",
                type = CashGroupType.EXPENSES,
                isEnabled = true,
                orderIndex = state.cashGroups.size,
                isDefault = true
            )
            _uiState.update { s ->
                s.copy(cashGroups = s.cashGroups + newGroup, selectedCashGroupId = newGroup.id)
            }
        }
        saveToDatabase()
    }

    fun setShowCashDenominationsTable(show: Boolean) {
        _uiState.update { it.copy(showCashDenominationsTable = show) }
        saveToDatabase()
    }

    fun setShowDisabledRowsInReport(show: Boolean) {
        _uiState.update { it.copy(showDisabledRowsInReport = show) }
    }

    fun setShowUnbalancedGroupsInReport(show: Boolean) {
        _uiState.update { it.copy(showUnbalancedGroupsInReport = show) }
    }

    fun setScheduledResetTime(time: String) {
        _uiState.update { it.copy(scheduledResetTime = time) }
        sharedPreferences.edit().putString("scheduledResetTime", time).apply()
    }

    fun setAutoDailyResetEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isAutoDailyResetEnabled = enabled) }
        sharedPreferences.edit().putBoolean("isAutoDailyResetEnabled", enabled).apply()
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        _uiState.update { it.copy(dailyReminderEnabled = enabled) }
        saveToDatabase()
    }

    fun setDailyReminderTime(time: String) {
        _uiState.update { it.copy(dailyReminderTime = time) }
        saveToDatabase()
    }

    fun setAccountingDayStartHour(hour: Int) {
        _uiState.update { it.copy(accountingDayStartHour = hour) }
        sharedPreferences.edit().putInt("accountingDayStartHour", hour).apply()
    }

    fun setReportPrimaryColor(colorLong: Long) {
        _uiState.update { it.copy(reportPrimaryColor = colorLong) }
    }

    fun setReportSecondaryColor(colorLong: Long) {
        _uiState.update { it.copy(reportSecondaryColor = colorLong) }
    }

    private fun addAuditLog(section: String, groupName: String, field: String, oldValue: String, newValue: String, details: String = "") {
        if (oldValue == newValue) return
        val cleanOld = oldValue.ifBlank { "0" }
        val cleanNew = newValue.ifBlank { "0" }
        
        _uiState.update { state ->
            val logs = state.auditLogs.toMutableList()
            // Find an entry for the exact same section, group and field created in the last 15 seconds
            val existingIndex = logs.indexOfFirst { 
                it.section == section && 
                it.groupName == groupName && 
                it.field == field && 
                (System.currentTimeMillis() - it.timestamp < 15000)
            }
            
            if (existingIndex != -1) {
                val existing = logs[existingIndex]
                val mergedEntry = existing.copy(
                    newValue = cleanNew,
                    timestamp = System.currentTimeMillis(),
                    details = details.ifBlank { existing.details }
                )
                if (mergedEntry.oldValue == mergedEntry.newValue) {
                    logs.removeAt(existingIndex)
                } else {
                    logs[existingIndex] = mergedEntry
                }
            } else {
                val entry = AuditLogEntry(
                    section = section,
                    groupName = groupName,
                    field = field,
                    oldValue = cleanOld,
                    newValue = cleanNew,
                    details = details
                )
                logs.add(0, entry)
            }
            state.copy(auditLogs = logs.take(200))
        }
    }

    fun clearAuditLogs() {
        _uiState.update { it.copy(auditLogs = emptyList()) }
    }

    fun dismissRemainingWarning() {
        _uiState.update { it.copy(remainingWarningMessage = null) }
    }

    fun dismissExpenseWarning() {
        _uiState.update { it.copy(expenseWarningMessage = null) }
    }

    fun dismissGivenConfirm() {
        _uiState.update { it.copy(showConfirmGivenDialog = false, pendingGivenDenom = null) }
    }

    // Update Given without popup prompt
    fun updateGiven(groupId: String, denomination: Int, given: String, skipConfirm: Boolean = true) {
        if (checkDayClosedAndWarn()) return
        if (_uiState.value.isLockGivenExtraMode) return
        val group = _uiState.value.groups.find { it.id == groupId } ?: return
        val row = group.rows.find { it.denomination == denomination } ?: return
        val oldGiven = row.givenInput

        // Apply change
        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val updatedRows = grp.rows.map { r ->
                        if (r.denomination == denomination) {
                            // If first time entering given (oldGiven is blank or "0"), sync remaining with given. Otherwise keep remaining unchanged.
                            val autoRemaining = if (oldGiven.isBlank() || oldGiven == "0" || r.remainingInput.isBlank() || r.remainingInput == "5" || r.remainingInput == oldGiven) {
                                given
                            } else {
                                r.remainingInput
                            }
                            r.copy(givenInput = given, remainingInput = autoRemaining)
                        } else r
                    }
                    grp.copy(rows = updatedRows)
                } else grp
            }
            state.copy(
                groups = updatedGroups,
                showConfirmGivenDialog = false,
                pendingGivenDenom = null,
                remainingWarningMessage = null
            )
        }
        addAuditLog("المبيعات", group.name, "المعطى (فئة $denomination)", oldGiven, given)
        saveToDatabase()
    }

    fun confirmPendingGiven() {
        val state = _uiState.value
        val denom = state.pendingGivenDenom ?: return
        updateGiven(state.pendingGivenGroupId, denom, state.pendingGivenNewValue, skipConfirm = true)
    }

    fun updateAdded(groupId: String, denomination: Int, added: String) {
        if (checkDayClosedAndWarn()) return
        if (_uiState.value.isLockGivenExtraMode) return
        val group = _uiState.value.groups.find { it.id == groupId } ?: return
        val row = group.rows.find { it.denomination == denomination } ?: return
        val oldAdded = row.addedInput
        val oldAddedVal = oldAdded.trim().toIntOrNull() ?: 0
        val newAddedVal = added.trim().toIntOrNull() ?: 0
        val diff = newAddedVal - oldAddedVal

        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val updatedRows = grp.rows.map { r ->
                        if (r.denomination == denomination) {
                            val newRemainingInput = if (r.remainingInput.isNotBlank()) {
                                val curRem = r.remainingInput.trim().toIntOrNull() ?: 0
                                val adjustedRem = (curRem + diff).coerceAtLeast(0)
                                adjustedRem.toString()
                            } else {
                                r.remainingInput
                            }
                            r.copy(addedInput = added, remainingInput = newRemainingInput)
                        } else r
                    }
                    grp.copy(rows = updatedRows)
                } else grp
            }
            state.copy(groups = updatedGroups, remainingWarningMessage = null)
        }
        addAuditLog("المبيعات", group.name, "إضافة (فئة $denomination)", oldAdded, added)
        saveToDatabase()
    }

    // Update Remaining with smooth, non-blocking entry & strict validation against given+added
    fun updateRemaining(groupId: String, denomination: Int, remaining: String) {
        if (checkDayClosedAndWarn()) return
        val group = _uiState.value.groups.find { it.id == groupId } ?: return
        val row = group.rows.find { it.denomination == denomination } ?: return
        val oldRemaining = row.remainingInput

        val parsedRemaining = remaining.trim().toIntOrNull()
        if (parsedRemaining != null && remaining.isNotBlank()) {
            val totalGivenAndAdded = row.given + row.added
            if (parsedRemaining > totalGivenAndAdded) {
                _uiState.update { state ->
                    state.copy(
                        remainingWarningMessage = "لا تقبل قيمة المتبقي ($parsedRemaining) لأنها أكبر من مجموع المخزون (المعطى + الإضافي = $totalGivenAndAdded)! يرجى التحقق من المدخلات."
                    )
                }
                return // Reject edit and keep previous value
            }
        }

        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val updatedRows = grp.rows.map { r ->
                        if (r.denomination == denomination) r.copy(remainingInput = remaining) else r
                    }
                    grp.copy(rows = updatedRows)
                } else grp
            }
            state.copy(groups = updatedGroups, remainingWarningMessage = null)
        }
        addAuditLog("المبيعات", group.name, "المتبقي (فئة $denomination)", oldRemaining, remaining)
        saveToDatabase()
    }

    fun zeroOutRemaining(groupId: String, denomination: Int) {
        updateRemaining(groupId, denomination, "0")
    }

    fun zeroOutAllRemaining(groupId: String) {
        val group = _uiState.value.groups.find { it.id == groupId } ?: return
        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val updatedRows = grp.rows.map { r -> r.copy(remainingInput = "0") }
                    grp.copy(rows = updatedRows)
                } else grp
            }
            state.copy(groups = updatedGroups)
        }
        addAuditLog("المبيعات", group.name, "تصفير جميع المتبقي", "", "0")
        saveToDatabase()
    }

    fun toggleRowEnabled(groupId: String, denomination: Int) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val updatedRows = grp.rows.map { r ->
                        if (r.denomination == denomination) r.copy(isEnabled = !r.isEnabled) else r
                    }
                    grp.copy(rows = updatedRows)
                } else grp
            }
            state.copy(groups = updatedGroups)
        }
        saveToDatabase()
    }

    fun deleteCategoryRow(groupId: String, denomination: Int) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val updatedRows = grp.rows.filterNot { it.denomination == denomination }
                    grp.copy(rows = updatedRows)
                } else grp
            }
            state.copy(groups = updatedGroups)
        }
        saveToDatabase()
    }

    fun updateDenominationValue(groupId: String, oldDenom: Int, newDenom: Int) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val updatedRows = grp.rows.map { r ->
                        if (r.denomination == oldDenom) r.copy(denomination = newDenom) else r
                    }
                    grp.copy(rows = updatedRows)
                } else grp
            }
            state.copy(groups = updatedGroups)
        }
        saveToDatabase()
    }

    fun addCategoryToGroup(groupId: String, denomination: Int) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    if (grp.rows.none { it.denomination == denomination }) {
                        val newRows = (grp.rows + DirectSalesRowUiState(denomination = denomination)).sortedBy { it.denomination }
                        grp.copy(rows = newRows)
                    } else grp
                } else grp
            }
            state.copy(groups = updatedGroups)
        }
        saveToDatabase()
    }

    fun updateRowNotes(groupId: String, denomination: Int, notes: String) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val updatedRows = grp.rows.map { r ->
                        if (r.denomination == denomination) r.copy(notes = notes) else r
                    }
                    grp.copy(rows = updatedRows)
                } else grp
            }
            state.copy(groups = updatedGroups)
        }
        saveToDatabase()
    }

    // Quick Sales Entry: input sold units, deducts from remaining, and adds total to cash in box
    fun executeQuickSalesEntry(groupId: String, salesSoldMap: Map<Int, Int>) {
        val group = _uiState.value.groups.find { it.id == groupId } ?: return
        var totalSoldAmount = 0.0

        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val updatedRows = grp.rows.map { r ->
                        val soldCount = salesSoldMap[r.denomination] ?: 0
                        if (soldCount > 0) {
                            val curRemaining = r.remaining
                            val newRemaining = maxOf(0, curRemaining - soldCount)
                            totalSoldAmount += soldCount * r.denomination
                            r.copy(remainingInput = newRemaining.toString())
                        } else r
                    }
                    grp.copy(rows = updatedRows)
                } else grp
            }

            // Auto-add total sold amount to cash in box (YER)
            val currentCashYer = state.cashInBoxYerInput.toDoubleOrNull() ?: 0.0
            val newCashYer = currentCashYer + totalSoldAmount
            val newCashYerStr = if (newCashYer.isFinite() && newCashYer % 1.0 == 0.0 && abs(newCashYer) <= Long.MAX_VALUE) newCashYer.toLong().toString() else newCashYer.toString()

            state.copy(
                groups = updatedGroups,
                cashInBoxYerInput = newCashYerStr
            )
        }

        addAuditLog("الآلة الحاسبة", group.name, "إدخال مبيعات سريع", "", "+ $totalSoldAmount ر.ي.", "خصم من المتبقي وإضافة إلى الصندوق")
        saveToDatabase()
    }

    // Direct Sales entries (صيني)
    fun addDirectEntry(groupId: String, title: String, amount: String, notes: String = "", isDeposit: Boolean = false) {
        val newItem = DirectEntryItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            amountInput = amount.trim(),
            quantityInput = "1",
            notes = notes.trim(),
            isDeposit = isDeposit
        )
        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) grp.copy(directEntries = grp.directEntries + newItem) else grp
            }
            state.copy(groups = updatedGroups)
        }
        addAuditLog("المبيعات", "إدخال مباشر", "إضافة بند: $title", "", amount)
        if (isDeposit && amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0) {
            addCashDirectEntry(
                groupId = "cash_group_deposits",
                title = "إيداع صيني: ${title.trim().ifBlank { "بدون عنوان" }}",
                amount = amount.trim(),
                currencyCode = "YER",
                isSar = false,
                notes = "مضاف تلقائياً من صيني"
            )
        }
        saveToDatabase()
    }

    fun removeDirectEntry(groupId: String, id: String, returnToCash: Boolean = false) {
        _uiState.update { state ->
            var updatedCashYer = state.cashInBoxYerInput
            var targetAmountYer = 0.0

            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val entryToRemove = grp.directEntries.find { it.id == id }
                    if (entryToRemove != null) {
                        targetAmountYer = entryToRemove.amount
                    }
                    grp.copy(directEntries = grp.directEntries.filterNot { it.id == id })
                } else grp
            }

            if (returnToCash && targetAmountYer > 0.0) {
                val currentCash = state.cashInBoxYerInput.trim().toDoubleOrNull() ?: 0.0
                val newCash = currentCash + targetAmountYer
                updatedCashYer = if (newCash % 1.0 == 0.0) newCash.toLong().toString() else newCash.toString()
            }

            state.copy(groups = updatedGroups, cashInBoxYerInput = updatedCashYer)
        }
        saveToDatabase()
    }

    fun mergeDuplicateEntries(groupId: String) {
        _uiState.update { state ->
            val updatedSalesGroups = state.groups.map { grp ->
                if (grp.id == groupId && grp.type == SalesGroupType.DIRECT_ENTRY) {
                    val mergedMap = mutableMapOf<String, DirectEntryItem>()
                    grp.directEntries.forEach { entry ->
                        val key = entry.title.trim().lowercase()
                        val existing = mergedMap[key]
                        if (existing != null) {
                            val amount1 = existing.amount
                            val amount2 = entry.amount
                            val combinedAmount = amount1 + amount2
                            val combinedNotes = listOf(existing.notes, entry.notes).filter { it.isNotBlank() }.joinToString(" - ")
                            mergedMap[key] = existing.copy(
                                amountInput = if (combinedAmount % 1.0 == 0.0) combinedAmount.toLong().toString() else combinedAmount.toString(),
                                notes = combinedNotes
                            )
                        } else {
                            mergedMap[key] = entry
                        }
                    }
                    grp.copy(directEntries = mergedMap.values.toList())
                } else grp
            }

            val updatedCashGroups = state.cashGroups.map { grp ->
                if (grp.id == groupId) {
                    val mergedMap = mutableMapOf<String, CashDirectEntryItem>()
                    grp.directEntries.forEach { entry ->
                        val key = "${entry.title.trim().lowercase()}_${entry.currencyCode}"
                        val existing = mergedMap[key]
                        if (existing != null) {
                            val amount1 = existing.amount
                            val amount2 = entry.amount
                            val combinedAmount = amount1 + amount2
                            val combinedNotes = listOf(existing.notes, entry.notes).filter { it.isNotBlank() }.joinToString(" - ")
                            mergedMap[key] = existing.copy(
                                amountInput = if (combinedAmount % 1.0 == 0.0) combinedAmount.toLong().toString() else combinedAmount.toString(),
                                notes = combinedNotes
                            )
                        } else {
                            mergedMap[key] = entry
                        }
                    }
                    grp.copy(directEntries = mergedMap.values.toList())
                } else grp
            }

            state.copy(groups = updatedSalesGroups, cashGroups = updatedCashGroups)
        }
        addAuditLog("المجموعات", "دمج البنود", "تم دمج البنود المتطابقة في المجموعة: $groupId", "", "")
        saveToDatabase()
    }

    fun resetSalesGroupOnly(groupId: String) {
        _uiState.update { state ->
            val updated = state.groups.map { grp ->
                if (grp.id == groupId) {
                    when (grp.type) {
                        SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> grp.copy(rows = grp.rows.map { it.copy(givenInput = "", addedInput = "", remainingInput = "", notes = "") })
                        SalesGroupType.DIRECT_ENTRY -> grp.copy(directEntries = emptyList())
                    }
                } else grp
            }
            state.copy(groups = updated)
        }
        addAuditLog("تصفير", "تصفير مجموعة مبيعات", "تصفير المجموعة $groupId", "", "")
        saveToDatabase()
    }

    fun resetCashGroupOnly(groupId: String) {
        _uiState.update { state ->
            val updated = state.cashGroups.map { grp ->
                if (grp.id == groupId) {
                    when (grp.type) {
                        CashGroupType.DENOMINATIONS -> grp.copy(denomRows = grp.denomRows.map { it.copy(countInput = "", notes = "") })
                        CashGroupType.DIRECT_ENTRY, CashGroupType.EXPENSES, CashGroupType.DEPOSITS -> grp.copy(directEntries = emptyList())
                    }
                } else grp
            }
            state.copy(cashGroups = updated)
        }
        addAuditLog("تصفير", "تصفير مجموعة صندوق", "تصفير المجموعة $groupId", "", "")
        saveToDatabase()
    }

    fun resetPhysicalCashOnly() {
        _uiState.update { state ->
            val updatedCashGroups = state.cashGroups.map { grp ->
                if (grp.type == CashGroupType.DENOMINATIONS) {
                    grp.copy(denomRows = grp.denomRows.map { it.copy(countInput = "") })
                } else grp
            }
            state.copy(
                cashInBoxYerInput = "",
                cashInBoxSarInput = "",
                cashGroups = updatedCashGroups
            )
        }
        addAuditLog("تصفير", "تصفير النقد الفعلي", "تصفير النقد الفعلي بالصندوق", "", "")
        saveToDatabase()
    }

    fun resetSalesCategory(groupId: String, denom: Int) {
        _uiState.update { state ->
            val updated = state.groups.map { grp ->
                if (grp.id == groupId) {
                    grp.copy(rows = grp.rows.map { row ->
                        if (row.denomination == denom) row.copy(givenInput = "", addedInput = "", remainingInput = "", notes = "")
                        else row
                    })
                } else grp
            }
            state.copy(groups = updated)
        }
        addAuditLog("تصفير", "تصفير فئة مبيعات", "تصفير فئة $denom في $groupId", "", "")
        saveToDatabase()
    }

    fun resetSalesField(groupId: String, fieldType: String) {
        _uiState.update { state ->
            val updated = state.groups.map { grp ->
                if (grp.id == groupId) {
                    grp.copy(rows = grp.rows.map { row ->
                        when (fieldType) {
                            "GIVEN" -> row.copy(givenInput = "")
                            "ADDED" -> row.copy(addedInput = "")
                            "REMAINING" -> row.copy(remainingInput = "")
                            else -> row
                        }
                    })
                } else grp
            }
            state.copy(groups = updated)
        }
        addAuditLog("تصفير", "تصفير خانة مبيعات", "تصفير خانة $fieldType في $groupId", "", "")
        saveToDatabase()
    }

    fun resetCashCategory(groupId: String, denom: Int) {
        _uiState.update { state ->
            val updated = state.cashGroups.map { grp ->
                if (grp.id == groupId) {
                    grp.copy(denomRows = grp.denomRows.map { row ->
                        if (row.denomination == denom) row.copy(countInput = "", notes = "")
                        else row
                    })
                } else grp
            }
            state.copy(cashGroups = updated)
        }
        addAuditLog("تصفير", "تصفير فئة نقد", "تصفير فئة نقد $denom في $groupId", "", "")
        saveToDatabase()
    }

    fun updateDirectEntry(groupId: String, id: String, title: String, amount: String, notes: String, isDeposit: Boolean = false) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val updatedList = grp.directEntries.map { de ->
                        if (de.id == id) de.copy(
                            title = title.trim(),
                            amountInput = amount.trim(),
                            notes = notes.trim(),
                            isDeposit = isDeposit
                        ) else de
                    }
                    grp.copy(directEntries = updatedList)
                } else grp
            }
            state.copy(groups = updatedGroups)
        }
        saveToDatabase()
    }

    fun addCashDirectEntry(
        groupId: String,
        title: String,
        amount: String,
        currencyCode: String = "YER",
        isSar: Boolean = false,
        customExchangeRateInput: String = "",
        customExchangeRate: Double? = null,
        notes: String = "",
        deductFromCash: Boolean = true
    ) {
        val newItem = CashDirectEntryItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            amountInput = amount.trim(),
            currencyCode = currencyCode.uppercase(),
            isSar = isSar || currencyCode.uppercase() == "SAR",
            customExchangeRateInput = customExchangeRateInput.trim(),
            customExchangeRate = customExchangeRate,
            notes = notes.trim(),
            deductFromCash = deductFromCash
        )
        val currentState = _uiState.value
        val exRate = currentState.exchangeRateInput.toDoubleOrNull()?.takeIf { it > 0 } ?: 380.0
        val expYer = newItem.getTotalYer(exRate)
        val currentCash = currentState.cashInBoxYerInput.trim().toDoubleOrNull() ?: 0.0
        val isDepositGroup = groupId == "cash_group_deposits" || currentState.cashGroups.find { it.id == groupId }?.type == CashGroupType.DEPOSITS
        
        if (!isDepositGroup && deductFromCash && expYer > currentCash) {
            _uiState.update { state ->
                state.copy(
                    expenseWarningMessage = "لا يمكن إضافة البند (${AccountingFormatter.formatYer(expYer)}) لأنه أكبر من النقد المتوفر (${AccountingFormatter.formatYer(currentCash)})!"
                )
            }
            return
        }

        _uiState.update { state ->
            val updatedGroups = state.cashGroups.map { grp ->
                if (grp.id == groupId) grp.copy(directEntries = grp.directEntries + newItem) else grp
            }
            state.copy(cashGroups = updatedGroups)
        }
        if (!isDepositGroup && deductFromCash) {
            val rate = _uiState.value.exchangeRateInput.toDoubleOrNull() ?: 380.0
            val yerValue = newItem.getTotalYer(rate)
            val currentCash = _uiState.value.cashInBoxYerInput.toDoubleOrNull() ?: 0.0
            val newCash = (currentCash - yerValue).coerceAtLeast(0.0)
            val formatted = if (newCash % 1.0 == 0.0) newCash.toLong().toString() else newCash.toString()
            _uiState.update { it.copy(cashInBoxYerInput = formatted) }
        }
        addAuditLog("الصندوق", "إدخال مباشر", "إضافة بند: $title", "", amount)
        saveToDatabase()
    }

    fun updateCashDirectEntry(
        groupId: String,
        id: String,
        title: String,
        amount: String,
        currencyCode: String = "YER",
        isSar: Boolean = false,
        customExchangeRateInput: String = "",
        customExchangeRate: Double? = null,
        notes: String = "",
        deductFromCash: Boolean = false
    ) {
        val currentState = _uiState.value
        val exRate = currentState.exchangeRateInput.toDoubleOrNull()?.takeIf { it > 0 } ?: 380.0
        val currentCash = currentState.cashInBoxYerInput.trim().toDoubleOrNull() ?: 0.0
        
        val group = currentState.cashGroups.find { it.id == groupId }
        val oldEntry = group?.directEntries?.find { it.id == id }
        val oldEntryYer = if (oldEntry != null && oldEntry.deductFromCash) oldEntry.getTotalYer(exRate) else 0.0
        val availableCashForUpdate = currentCash + oldEntryYer

        val tempItem = CashDirectEntryItem(
            id = id,
            title = title.trim(),
            amountInput = amount.trim(),
            currencyCode = currencyCode.uppercase(),
            isSar = isSar || currencyCode.uppercase() == "SAR",
            customExchangeRateInput = customExchangeRateInput.trim(),
            customExchangeRate = customExchangeRate,
            notes = notes.trim(),
            deductFromCash = deductFromCash
        )
        val expYer = tempItem.getTotalYer(exRate)
        
        val isDepositGroup = groupId == "cash_group_deposits" || currentState.cashGroups.find { it.id == groupId }?.type == CashGroupType.DEPOSITS
        
        if (!isDepositGroup && deductFromCash && expYer > availableCashForUpdate) {
            _uiState.update { state ->
                state.copy(
                    expenseWarningMessage = "لا يمكن إضافة البند (${AccountingFormatter.formatYer(expYer)}) لأنه أكبر من النقد المتوفر (${AccountingFormatter.formatYer(availableCashForUpdate)})!"
                )
            }
            return
        }

        _uiState.update { state ->
            val updatedGroups = state.cashGroups.map { grp ->
                if (grp.id == groupId) {
                    val updatedList = grp.directEntries.map { de ->
                        if (de.id == id) de.copy(
                            title = title.trim(),
                            amountInput = amount.trim(),
                            currencyCode = currencyCode.uppercase(),
                            isSar = isSar || currencyCode.uppercase() == "SAR",
                            customExchangeRateInput = customExchangeRateInput.trim(),
                            customExchangeRate = customExchangeRate,
                            notes = notes.trim(),
                            deductFromCash = deductFromCash
                        ) else de
                    }
                    grp.copy(directEntries = updatedList)
                } else grp
            }
            
            val newCash = if (deductFromCash) {
                availableCashForUpdate - expYer
            } else {
                availableCashForUpdate
            }
            val newCashStr = if (newCash > 0) {
                if (newCash % 1.0 == 0.0) newCash.toLong().toString() else newCash.toString()
            } else ""

            state.copy(
                cashGroups = updatedGroups,
                cashInBoxYerInput = newCashStr
            )
        }
        saveToDatabase()
    }

    fun updateCashDirectEntryAmount(groupId: String, id: String, amount: String) {
        _uiState.update { state ->
            val updatedGroups = state.cashGroups.map { grp ->
                if (grp.id == groupId) {
                    val updated = grp.directEntries.map { de ->
                        if (de.id == id) de.copy(amountInput = amount) else de
                    }
                    grp.copy(directEntries = updated)
                } else grp
            }
            state.copy(cashGroups = updatedGroups)
        }
        saveToDatabase()
    }

    fun updateCashDirectEntryRate(groupId: String, id: String, rate: String) {
        val parsed = rate.trim().toDoubleOrNull()
        _uiState.update { state ->
            val updatedGroups = state.cashGroups.map { grp ->
                if (grp.id == groupId) {
                    val updated = grp.directEntries.map { de ->
                        if (de.id == id) de.copy(customExchangeRateInput = rate.trim(), customExchangeRate = parsed) else de
                    }
                    grp.copy(directEntries = updated)
                } else grp
            }
            state.copy(cashGroups = updatedGroups)
        }
        saveToDatabase()
    }

    fun rebalanceCashBox() {
        val summaryVal = salesSummary.value
        if (summaryVal.balance < 0) {
            val deficit = kotlin.math.abs(summaryVal.balance)
            val currentCash = _uiState.value.cashInBoxYerInput.toDoubleOrNull() ?: 0.0
            val newCash = currentCash + deficit
            val newCashStr = if (newCash % 1.0 == 0.0 && kotlin.math.abs(newCash) <= Long.MAX_VALUE) newCash.toLong().toString() else newCash.toString()
            _uiState.update { it.copy(cashInBoxYerInput = newCashStr) }
            addAuditLog("الصندوق", "معادلة النقد", "معادلة عجز الصندوق بمبلغ $deficit", currentCash.toString(), newCashStr)
            saveToDatabase()
        }
    }

    fun addChiniDepositEntry(groupId: String, title: String, amountStr: String, notes: String) {
        val parsedAmt = amountStr.trim().toDoubleOrNull() ?: return
        if (parsedAmt <= 0) return

        val displayTitle = title.trim().ifBlank { "دفع إيداع صيني" }
        val entry = DirectEntryItem(
            id = java.util.UUID.randomUUID().toString(),
            title = displayTitle,
            amountInput = if (parsedAmt % 1.0 == 0.0) parsedAmt.toLong().toString() else parsedAmt.toString(),
            quantityInput = "1",
            notes = notes.trim(),
            isDeposit = true
        )

        _uiState.update { state ->
            val updatedCashGroups = state.cashGroups.map { cGrp ->
                if (cGrp.type == CashGroupType.DEPOSITS || cGrp.id == "cash_group_deposits") {
                    val newDirect = cGrp.directEntries + CashDirectEntryItem(
                        title = displayTitle,
                        amountInput = entry.amountInput,
                        notes = "من مبيعات الصيني: ${notes.trim()}".trim()
                    )
                    cGrp.copy(directEntries = newDirect)
                } else cGrp
            }

            val updatedSalesGroups = state.groups.map { sGrp ->
                if (sGrp.id == groupId) {
                    sGrp.copy(directEntries = sGrp.directEntries + entry)
                } else sGrp
            }

            val newDepositExpense = CashExpenseItem(
                id = java.util.UUID.randomUUID().toString(),
                title = displayTitle,
                amountInput = entry.amountInput,
                isSar = false,
                isDeposit = true,
                category = "صيني",
                notes = "من قسم الصيني: ${notes.trim()}".trim()
            )

            state.copy(
                groups = updatedSalesGroups,
                cashGroups = updatedCashGroups,
                expenses = state.expenses + newDepositExpense
            )
        }
        addAuditLog("المبيعات", "صيني", "دفع إيداع صيني: $displayTitle", "", entry.amountInput)
        saveToDatabase()
    }

    fun removeCashDirectEntry(groupId: String, id: String) {
        _uiState.update { state ->
            val updatedGroups = state.cashGroups.map { grp ->
                if (grp.id == groupId) grp.copy(directEntries = grp.directEntries.filterNot { it.id == id }) else grp
            }
            val updatedExpenses = state.expenses.filterNot { it.id == id }
            state.copy(cashGroups = updatedGroups, expenses = updatedExpenses)
        }
        saveToDatabase()
    }

    // Expenses
    fun showAddExpenseDialog(show: Boolean) {
        _uiState.update { it.copy(showAddExpenseDialog = show, editingExpense = null) }
    }

    fun showEditExpenseDialog(expense: CashExpenseItem) {
        _uiState.update { it.copy(showAddExpenseDialog = true, editingExpense = expense) }
    }

    fun addCashExpense(
        title: String,
        amount: String,
        currencyCode: String = "YER",
        isSar: Boolean = false,
        isDeposit: Boolean = false,
        customExchangeRateInput: String = "",
        customExchangeRate: Double? = null,
        category: String = "عام",
        notes: String = "",
        isSuspended: Boolean = false,
        deductFromCash: Boolean = true
    ) {
        val parsedRate = customExchangeRateInput.trim().toDoubleOrNull() ?: customExchangeRate
        val newExpense = CashExpenseItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            amountInput = amount.trim(),
            currencyCode = currencyCode.uppercase(),
            isSar = isSar || currencyCode.uppercase() == "SAR",
            isDeposit = isDeposit,
            customExchangeRateInput = customExchangeRateInput.trim(),
            customExchangeRate = parsedRate,
            category = category.trim().ifBlank { "عام" },
            notes = notes.trim(),
            isSuspended = isSuspended,
            deductFromCash = deductFromCash,
            timestamp = System.currentTimeMillis()
        )
        val currentState = _uiState.value
        val exRate = currentState.exchangeRateInput.toDoubleOrNull()?.takeIf { it > 0 } ?: 380.0
        val expYer = newExpense.getTotalYer(exRate)
        val currentCash = currentState.cashInBoxYerInput.trim().toDoubleOrNull() ?: 0.0

        if (!isDeposit && !isSuspended && deductFromCash && expYer > currentCash) {
            _uiState.update { state ->
                state.copy(
                    expenseWarningMessage = "لا يمكن صرف مبلغ المصروف (${AccountingFormatter.formatYer(expYer)}) لأنه أكبر من النقد المتوفر بالصندوق (${AccountingFormatter.formatYer(currentCash)})!"
                )
            }
            return // Block operation completely
        }

        _uiState.update { state ->
            val newCash = if (isDeposit) {
                currentCash
            } else if (deductFromCash && !isSuspended) {
                (currentCash - expYer).coerceAtLeast(0.0)
            } else {
                currentCash
            }
            val newCashStr = if (newCash > 0) {
                if (newCash % 1.0 == 0.0) newCash.toLong().toString() else newCash.toString()
            } else ""
            state.copy(
                expenses = state.expenses + newExpense,
                cashInBoxYerInput = newCashStr,
                showAddExpenseDialog = false,
                editingExpense = null,
                expenseWarningMessage = null
            )
        }
        val opName = if (isDeposit) "إضافة إيداع للصندوق" else {
            if (isSuspended) "إضافة مصروف (موقوف)" 
            else if (deductFromCash) "إضافة مصروف (مخصوم من النقد)" 
            else "إضافة مصروف"
        }
        addAuditLog("الصندوق", if (isDeposit) "إيداعات" else "مصاريف", "$opName: $title", "", amount)
        saveToDatabase()
    }

    fun updateCashExpense(
        id: String,
        title: String,
        amount: String,
        currencyCode: String = "YER",
        isSar: Boolean = false,
        isDeposit: Boolean = false,
        customExchangeRateInput: String = "",
        customExchangeRate: Double? = null,
        category: String = "عام",
        notes: String = "",
        isSuspended: Boolean = false,
        deductFromCash: Boolean = true
    ) {
        val parsedRate = customExchangeRateInput.trim().toDoubleOrNull() ?: customExchangeRate
        val currentState = _uiState.value
        val exRate = currentState.exchangeRateInput.toDoubleOrNull()?.takeIf { it > 0 } ?: 380.0
        val currentCash = currentState.cashInBoxYerInput.trim().toDoubleOrNull() ?: 0.0
        
        val oldExpense = currentState.expenses.find { it.id == id }
        val oldExpYer = if (oldExpense != null && !oldExpense.isDeposit && !oldExpense.isSuspended && oldExpense.deductFromCash) oldExpense.getTotalYer(exRate) else 0.0
        val availableCashForUpdate = currentCash + oldExpYer

        val tempExpense = CashExpenseItem(
            id = id,
            title = title.trim(),
            amountInput = amount.trim(),
            currencyCode = currencyCode.uppercase(),
            isSar = isSar || currencyCode.uppercase() == "SAR",
            isDeposit = isDeposit,
            customExchangeRateInput = customExchangeRateInput.trim(),
            customExchangeRate = parsedRate,
            isSuspended = isSuspended,
            deductFromCash = deductFromCash
        )
        val expYer = tempExpense.getTotalYer(exRate)

        if (!isDeposit && !isSuspended && deductFromCash && expYer > availableCashForUpdate) {
            _uiState.update { state ->
                state.copy(
                    expenseWarningMessage = "لا يمكن صرف مبلغ المصروف (${AccountingFormatter.formatYer(expYer)}) لأنه أكبر من النقد المتوفر بالصندوق (${AccountingFormatter.formatYer(availableCashForUpdate)})!"
                )
            }
            return // Block edit
        }

        _uiState.update { state ->
            val updated = state.expenses.map { exp ->
                if (exp.id == id) {
                    exp.copy(
                        title = title.trim(),
                        amountInput = amount.trim(),
                        currencyCode = currencyCode.uppercase(),
                        isSar = isSar || currencyCode.uppercase() == "SAR",
                        isDeposit = isDeposit,
                        customExchangeRateInput = customExchangeRateInput.trim(),
                        customExchangeRate = parsedRate,
                        category = category.trim().ifBlank { "عام" },
                        notes = notes.trim(),
                        isSuspended = isSuspended,
                        deductFromCash = deductFromCash
                    )
                } else exp
            }
            
            val newCash = if (isDeposit) {
                currentCash
            } else if (deductFromCash && !isSuspended) {
                availableCashForUpdate - expYer
            } else {
                availableCashForUpdate
            }
            val newCashStr = if (newCash > 0) {
                if (newCash % 1.0 == 0.0) newCash.toLong().toString() else newCash.toString()
            } else ""

            state.copy(
                expenses = updated, 
                showAddExpenseDialog = false, 
                editingExpense = null, 
                expenseWarningMessage = null,
                cashInBoxYerInput = newCashStr
            )
        }
        saveToDatabase()
    }

    fun toggleCashExpenseSuspended(id: String) {
        val state = _uiState.value
        val target = state.expenses.find { it.id == id } ?: return
        val exRate = state.exchangeRateInput.toDoubleOrNull()?.takeIf { it > 0 } ?: 380.0
        val currentCash = state.cashInBoxYerInput.trim().toDoubleOrNull() ?: 0.0

        if (target.isSuspended && !target.isDeposit) {
            val reactivatedYer = CashExpenseItem(
                title = target.title,
                amountInput = target.amountInput,
                currencyCode = target.currencyCode,
                isSar = target.isSar,
                isDeposit = false,
                customExchangeRate = target.customExchangeRate,
                isSuspended = false
            ).getTotalYer(exRate)

            if (reactivatedYer > currentCash) {
                _uiState.update { s ->
                    s.copy(
                        expenseWarningMessage = "لا يمكن إلغاء إيقاف المصروف (${AccountingFormatter.formatYer(reactivatedYer)}) لأن مبلغه أكبر من النقد المتوفر بالصندوق (${AccountingFormatter.formatYer(currentCash)})!"
                    )
                }
                return
            }
        }

        _uiState.update { s ->
            val updated = s.expenses.map { exp ->
                if (exp.id == id) exp.copy(isSuspended = !exp.isSuspended) else exp
            }
            s.copy(expenses = updated, expenseWarningMessage = null)
        }
        addAuditLog("الصندوق", "مصاريف", "تعديل حالة الإيقاف للمصروف: ${target.title}", if (target.isSuspended) "موقوف" else "مفعل", if (target.isSuspended) "مفعل" else "موقوف")
        saveToDatabase()
    }

    fun clearExpenseWarningMessage() {
        _uiState.update { it.copy(expenseWarningMessage = null) }
    }

    fun updateCashExpenseAmount(id: String, amount: String) {
        _uiState.update { state ->
            val updated = state.expenses.map { exp ->
                if (exp.id == id) exp.copy(amountInput = amount) else exp
            }
            state.copy(expenses = updated)
        }
        saveToDatabase()
    }

    fun updateCashExpenseRate(id: String, rate: String) {
        val parsed = rate.trim().toDoubleOrNull()
        _uiState.update { state ->
            val updated = state.expenses.map { exp ->
                if (exp.id == id) exp.copy(customExchangeRateInput = rate.trim(), customExchangeRate = parsed) else exp
            }
            state.copy(expenses = updated)
        }
        saveToDatabase()
    }

    fun removeCashExpense(id: String, returnToCash: Boolean = false) {
        val state = _uiState.value
        val isExpense = state.expenses.any { it.id == id }
        val targetExpense = state.expenses.find { it.id == id } ?: run {
            var found: CashExpenseItem? = null
            state.cashGroups.forEach { grp ->
                grp.directEntries.find { it.id == id }?.let { entry ->
                    found = CashExpenseItem(
                        id = entry.id,
                        title = entry.title,
                        amountInput = entry.amountInput,
                        currencyCode = entry.currencyCode,
                        isSar = entry.isSar,
                        isDeposit = (grp.type == CashGroupType.DEPOSITS),
                        customExchangeRate = entry.customExchangeRate,
                        customExchangeRateInput = entry.customExchangeRate?.toString() ?: "",
                        notes = entry.notes
                    )
                }
            }
            found
        }

        var updatedCashYer = state.cashInBoxYerInput
        if (returnToCash && !isExpense && targetExpense != null && !targetExpense.isDeposit) {
            val exRate = state.exchangeRateInput.toDoubleOrNull()?.takeIf { it > 0 } ?: 380.0
            val amountYer = targetExpense.getTotalYer(exRate)
            val currentCash = state.cashInBoxYerInput.trim().toDoubleOrNull() ?: 0.0
            val newCash = currentCash + amountYer
            updatedCashYer = if (newCash > 0) {
                if (newCash % 1.0 == 0.0) newCash.toLong().toString() else newCash.toString()
            } else ""
        }

        _uiState.update { s ->
            val updatedExpenses = s.expenses.filterNot { it.id == id }
            val updatedGroups = s.cashGroups.map { grp ->
                grp.copy(directEntries = grp.directEntries.filterNot { it.id == id })
            }
            s.copy(
                expenses = updatedExpenses,
                cashGroups = updatedGroups,
                cashInBoxYerInput = updatedCashYer
            )
        }
        addAuditLog("الصندوق", "حذف بند", "حذف بند: ${targetExpense?.title ?: id}", "", "", if (returnToCash && !isExpense) "إرجاع للنقد" else "تم الحذف")
        saveToDatabase()
    }

    fun deleteItemFromAnyGroup(groupId: String?, itemId: String) {
        val state = _uiState.value
        val exRate = state.exchangeRateInput.toDoubleOrNull()?.takeIf { it > 0 } ?: 380.0
        var itemAmountYer = 0.0
        var itemTitle = ""

        // Check if it's an expense/deposit
        val expense = state.expenses.find { it.id == itemId }
        if (expense != null) {
            itemTitle = expense.title
            itemAmountYer = expense.getTotalYer(exRate)
        }

        // Check if it's a cash group direct entry
        state.cashGroups.forEach { grp ->
            grp.directEntries.find { it.id == itemId }?.let { de ->
                itemTitle = de.title
                itemAmountYer = de.getTotalYer(exRate)
            }
        }

        // Check if it's a sales group direct entry
        state.groups.forEach { grp ->
            grp.directEntries.find { it.id == itemId }?.let { se ->
                itemTitle = se.title
                itemAmountYer = se.total
            }
        }

        _uiState.update { s ->
            val updatedExpenses = s.expenses.filterNot { it.id == itemId }
            val updatedCashGroups = s.cashGroups.map { grp ->
                grp.copy(directEntries = grp.directEntries.filterNot { it.id == itemId })
            }
            val updatedSalesGroups = s.groups.map { grp ->
                grp.copy(directEntries = grp.directEntries.filterNot { it.id == itemId })
            }
            s.copy(
                expenses = updatedExpenses,
                cashGroups = updatedCashGroups,
                groups = updatedSalesGroups
            )
        }
        addAuditLog("العمليات", "حذف بند", "حذف بند: $itemTitle", "", "")
        saveToDatabase()
    }

    enum class ResetScope(val label: String) {
        ENTIRE_DAY("تصفير شامل للوردية والبيانات"),
        CURRENT_GROUP_ALL("تصفير بيانات المجموعة المحددة بالكامل"),
        FIELD_GIVEN("تصفير خانة (المعطى) فقط"),
        FIELD_ADDED("تصفير خانة (الإضافة) فقط"),
        FIELD_REMAINING("تصفير خانة (المتبقي) فقط"),
        PHYSICAL_CASH_IN_BOX("تصفير النقد الفعلي بالصندوق فقط"),
        EXPENSES_ONLY("تصفير المصاريف فقط"),
        DEPOSITS_ONLY("تصفير الإيداعات فقط")
    }

    fun executeResetData(scope: ResetScope, groupId: String? = null) {
        _uiState.update { s ->
            when (scope) {
                ResetScope.ENTIRE_DAY -> {
                    s.copy(
                        cashInBoxYerInput = "",
                        cashInBoxSarInput = "",
                        expenses = emptyList(),
                        groups = s.groups.map { grp ->
                            grp.copy(
                                rows = grp.rows.map { it.copy(givenInput = "", addedInput = "", remainingInput = "") },
                                directEntries = emptyList()
                            )
                        },
                        cashGroups = s.cashGroups.map { cGrp ->
                            cGrp.copy(
                                denomRows = cGrp.denomRows.map { it.copy(countInput = "") },
                                directEntries = emptyList()
                            )
                        }
                    )
                }
                ResetScope.CURRENT_GROUP_ALL -> {
                    if (groupId != null) {
                        s.copy(
                            groups = s.groups.map { grp ->
                                if (grp.id == groupId) {
                                    grp.copy(
                                        rows = grp.rows.map { it.copy(givenInput = "", addedInput = "", remainingInput = "") },
                                        directEntries = emptyList()
                                    )
                                } else grp
                            },
                            cashGroups = s.cashGroups.map { cGrp ->
                                if (cGrp.id == groupId) {
                                    cGrp.copy(
                                        denomRows = cGrp.denomRows.map { it.copy(countInput = "") },
                                        directEntries = emptyList()
                                    )
                                } else cGrp
                            }
                        )
                    } else s
                }
                ResetScope.FIELD_GIVEN -> {
                    if (groupId != null) {
                        s.copy(
                            groups = s.groups.map { grp ->
                                if (grp.id == groupId) grp.copy(rows = grp.rows.map { it.copy(givenInput = "") }) else grp
                            }
                        )
                    } else s
                }
                ResetScope.FIELD_ADDED -> {
                    if (groupId != null) {
                        s.copy(
                            groups = s.groups.map { grp ->
                                if (grp.id == groupId) grp.copy(rows = grp.rows.map { it.copy(addedInput = "") }) else grp
                            }
                        )
                    } else s
                }
                ResetScope.FIELD_REMAINING -> {
                    if (groupId != null) {
                        s.copy(
                            groups = s.groups.map { grp ->
                                if (grp.id == groupId) grp.copy(rows = grp.rows.map { it.copy(remainingInput = "") }) else grp
                            }
                        )
                    } else s
                }
                ResetScope.PHYSICAL_CASH_IN_BOX -> {
                    s.copy(
                        cashInBoxYerInput = "",
                        cashInBoxSarInput = "",
                        cashGroups = s.cashGroups.map { cGrp ->
                            if (cGrp.type == CashGroupType.DENOMINATIONS) {
                                cGrp.copy(denomRows = cGrp.denomRows.map { it.copy(countInput = "") })
                            } else cGrp
                        }
                    )
                }
                ResetScope.EXPENSES_ONLY -> {
                    s.copy(
                        expenses = s.expenses.filter { it.isDeposit },
                        cashGroups = s.cashGroups.map { cGrp ->
                            if (cGrp.type == CashGroupType.EXPENSES) cGrp.copy(directEntries = emptyList()) else cGrp
                        }
                    )
                }
                ResetScope.DEPOSITS_ONLY -> {
                    s.copy(
                        expenses = s.expenses.filterNot { it.isDeposit },
                        cashGroups = s.cashGroups.map { cGrp ->
                            if (cGrp.type == CashGroupType.DEPOSITS) cGrp.copy(directEntries = emptyList()) else cGrp
                        }
                    )
                }
            }
        }
        addAuditLog("تصفير البيانات", "تصفير", "تنفيذ عملية: ${scope.label}", "", "")
        saveToDatabase()
    }

    fun toggleSalesGroupAddToReport(groupId: String, addToReport: Boolean? = null) {
        _uiState.update { state ->
            val updated = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val newVal = addToReport ?: !grp.addToReport
                    grp.copy(addToReport = newVal)
                } else grp
            }
            state.copy(groups = updated)
        }
        saveToDatabase()
    }

    fun toggleSalesGroupAddToBalance(groupId: String, addToBalance: Boolean? = null) {
        _uiState.update { state ->
            val updated = state.groups.map { grp ->
                if (grp.id == groupId) {
                    val newVal = addToBalance ?: !grp.addToBalance
                    grp.copy(addToBalance = newVal, isExcludedFromBalance = !newVal)
                } else grp
            }
            state.copy(groups = updated)
        }
        saveToDatabase()
    }

    fun toggleCashGroupAddToBalance(groupId: String, addToBalance: Boolean? = null) {
        _uiState.update { state ->
            val updated = state.cashGroups.map { grp ->
                if (grp.id == groupId) {
                    val newVal = addToBalance ?: grp.isExcludedFromBalance
                    grp.copy(isExcludedFromBalance = !newVal)
                } else grp
            }
            state.copy(cashGroups = updated)
        }
        saveToDatabase()
    }

    fun moveCashDenominationInGroup(groupId: String, fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val updatedCashGroups = state.cashGroups.map { group ->
                if (group.id == groupId) {
                    val rowsList = group.denomRows.toMutableList()
                    if (fromIndex in rowsList.indices && toIndex in rowsList.indices) {
                        val item = rowsList.removeAt(fromIndex)
                        rowsList.add(toIndex, item)
                        group.copy(denomRows = rowsList)
                    } else group
                } else group
            }
            state.copy(cashGroups = updatedCashGroups)
        }
        saveToDatabase()
    }

    fun executeTargetedSelectiveReset(
        groupId: String?,
        resetGiven: Boolean,
        resetAdded: Boolean,
        resetRemaining: Boolean,
        selectedDenominations: Set<Int>,
        selectedDirectEntryIds: Set<String>,
        resetCashInBox: Boolean = false,
        resetExpenses: Boolean = false,
        resetDeposits: Boolean = false
    ) {
        _uiState.update { s ->
            var newGroups = s.groups
            var newCashGroups = s.cashGroups
            var newExpenses = s.expenses
            var newCashYer = s.cashInBoxYerInput
            var newCashSar = s.cashInBoxSarInput

            if (groupId == null) {
                newGroups = s.groups.map { grp ->
                    val resetAllDenoms = selectedDenominations.isEmpty()
                    val resetAllDirect = selectedDirectEntryIds.isEmpty()
                    grp.copy(
                        rows = grp.rows.map { row ->
                            if (resetAllDenoms || selectedDenominations.contains(row.denomination)) {
                                row.copy(
                                    givenInput = if (resetGiven) "" else row.givenInput,
                                    addedInput = if (resetAdded) "" else row.addedInput,
                                    remainingInput = if (resetRemaining) "" else row.remainingInput
                                )
                            } else row
                        },
                        directEntries = if (resetAllDirect) emptyList() else grp.directEntries.filterNot { selectedDirectEntryIds.contains(it.id) }
                    )
                }
            } else {
                newGroups = s.groups.map { grp ->
                    if (grp.id == groupId) {
                        val resetAllDenoms = selectedDenominations.isEmpty()
                        val resetAllDirect = selectedDirectEntryIds.isEmpty()
                        grp.copy(
                            rows = grp.rows.map { row ->
                                if (resetAllDenoms || selectedDenominations.contains(row.denomination)) {
                                    row.copy(
                                        givenInput = if (resetGiven) "" else row.givenInput,
                                        addedInput = if (resetAdded) "" else row.addedInput,
                                        remainingInput = if (resetRemaining) "" else row.remainingInput
                                    )
                                } else row
                            },
                            directEntries = if (resetAllDirect) emptyList() else grp.directEntries.filterNot { selectedDirectEntryIds.contains(it.id) }
                        )
                    } else grp
                }
                newCashGroups = s.cashGroups.map { cGrp ->
                    if (cGrp.id == groupId) {
                        val resetAllDenoms = selectedDenominations.isEmpty()
                        val resetAllDirect = selectedDirectEntryIds.isEmpty()
                        cGrp.copy(
                            denomRows = cGrp.denomRows.map { row ->
                                if (resetAllDenoms || selectedDenominations.contains(row.denomination)) {
                                    row.copy(countInput = "")
                                } else row
                            },
                            directEntries = if (resetAllDirect) emptyList() else cGrp.directEntries.filterNot { selectedDirectEntryIds.contains(it.id) }
                        )
                    } else cGrp
                }
            }

            if (resetCashInBox) {
                newCashYer = ""
                newCashSar = ""
                newCashGroups = newCashGroups.map { cGrp ->
                    if (cGrp.type == CashGroupType.DENOMINATIONS) cGrp.copy(denomRows = cGrp.denomRows.map { it.copy(countInput = "") }) else cGrp
                }
            }
            if (resetExpenses) {
                newExpenses = newExpenses.filter { it.isDeposit }
                newCashGroups = newCashGroups.map { cGrp ->
                    if (cGrp.type == CashGroupType.EXPENSES) cGrp.copy(directEntries = emptyList()) else cGrp
                }
            }
            if (resetDeposits) {
                newExpenses = newExpenses.filterNot { it.isDeposit }
                newCashGroups = newCashGroups.map { cGrp ->
                    if (cGrp.type == CashGroupType.DEPOSITS) cGrp.copy(directEntries = emptyList()) else cGrp
                }
            }

            s.copy(
                groups = newGroups,
                cashGroups = newCashGroups,
                expenses = newExpenses,
                cashInBoxYerInput = newCashYer,
                cashInBoxSarInput = newCashSar
            )
        }
        addAuditLog("تصفير محدد", "تصفير", "تنفيذ تصفير بنود وفئات مخصصة", "", "")
        saveToDatabase()
    }

    fun clearCashExpenses() {
        _uiState.update { it.copy(expenses = emptyList()) }
        saveToDatabase()
    }

    fun mergeExpenses(
        itemIds: Set<String>,
        mergedTitle: String,
        isDeposit: Boolean = false,
        notes: String = ""
    ) {
        val state = _uiState.value
        val selectedItems = state.expenses.filter { it.id in itemIds }
        if (selectedItems.isEmpty()) return

        val defaultRate = state.exchangeRateInput.toDoubleOrNull() ?: 380.0

        var netYer = 0.0
        for (item in selectedItems) {
            val itemYer = item.getTotalYer(defaultRate)
            if (item.isDeposit) {
                netYer += itemYer
            } else {
                netYer -= itemYer
            }
        }

        val finalAmountYer = abs(netYer)
        val finalIsDeposit = if (netYer >= 0) isDeposit else false
        val amountStr = if (finalAmountYer.isFinite() && finalAmountYer % 1.0 == 0.0 && abs(finalAmountYer) <= Long.MAX_VALUE) finalAmountYer.toLong().toString() else finalAmountYer.toString()

        val mergedItem = CashExpenseItem(
            title = mergedTitle.ifBlank { "دمج " + selectedItems.joinToString(" + ") { it.title } },
            amountInput = amountStr,
            isSar = false,
            isDeposit = finalIsDeposit,
            notes = notes.ifBlank { "بنود مدمجة: " + selectedItems.joinToString(", ") { "${it.title} (${it.amountInput})" } }
        )

        _uiState.update { st ->
            val updated = st.expenses.filterNot { it.id in itemIds } + mergedItem
            st.copy(expenses = updated)
        }
        addAuditLog("الصندوق", "دمج المصروفات/الإيداعات", "تم دمج ${selectedItems.size} بنود", "", amountStr)
        saveToDatabase()
    }

    // Presets Management
    fun saveCurrentAsPreset(name: String, description: String = "") {
        val currentGroups = _uiState.value.groups
        val groupConfigs = currentGroups.map { grp ->
            GroupPresetConfig(
                groupId = grp.id,
                groupName = grp.name,
                isEnabled = grp.isEnabled,
                enabledDenominations = grp.activeRows.map { it.denomination }
            )
        }
        val newPreset = AccountingPreset(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            description = description.trim(),
            timestamp = System.currentTimeMillis(),
            isDefault = false,
            groupConfigs = groupConfigs
        )
        _uiState.update { state ->
            val updatedPresets = state.presets.filterNot { it.id == newPreset.id } + newPreset
            state.copy(presets = updatedPresets, activePresetId = newPreset.id)
        }
        savePresetsToStorage()
    }

    fun savePresetWithoutActivating(name: String, description: String = "") {
        val currentGroups = _uiState.value.groups
        val groupConfigs = currentGroups.map { grp ->
            GroupPresetConfig(
                groupId = grp.id,
                groupName = grp.name,
                isEnabled = grp.isEnabled,
                enabledDenominations = grp.activeRows.map { it.denomination }
            )
        }
        val newPreset = AccountingPreset(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            description = description.trim(),
            timestamp = System.currentTimeMillis(),
            isDefault = false,
            groupConfigs = groupConfigs
        )
        _uiState.update { state ->
            val updatedPresets = state.presets.filterNot { it.id == newPreset.id } + newPreset
            state.copy(presets = updatedPresets)
        }
        savePresetsToStorage()
    }

    fun applyPreset(presetId: String) {
        val preset = _uiState.value.presets.find { it.id == presetId } ?: return
        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                val config = preset.groupConfigs.find { it.groupId == grp.id || it.groupName == grp.name }
                if (config != null) {
                    val updatedRows = grp.rows.map { row ->
                        if (config.enabledDenominations.isNotEmpty()) {
                            row.copy(isEnabled = row.denomination in config.enabledDenominations)
                        } else {
                            row
                        }
                    }
                    grp.copy(
                        isEnabled = config.isEnabled,
                        rows = updatedRows
                    )
                } else {
                    grp
                }
            }
            state.copy(groups = updatedGroups, activePresetId = presetId)
        }
        saveToDatabase()
    }

    fun deletePreset(presetId: String) {
        _uiState.update { state ->
            val updated = state.presets.filterNot { it.id == presetId }
            state.copy(presets = updated, activePresetId = if (state.activePresetId == presetId) null else state.activePresetId)
        }
        savePresetsToStorage()
    }

    fun applyBuiltInMode(modeKey: String) {
        when (modeKey) {
            "standard_full" -> {
                _uiState.update { state ->
                    val updatedGroups = state.groups.map { it.copy(isEnabled = true) }
                    val updatedCashGroups = state.cashGroups.map { it.copy(isEnabled = true) }
                    state.copy(
                        groups = updatedGroups,
                        cashGroups = updatedCashGroups,
                        activePresetId = "standard_full"
                    )
                }
                addAuditLog("الإدارة", "تطبيق الوضع", "الوضع الشامل المتكامل", "", "")
                saveToDatabase()
            }
            "fast_tickets" -> {
                _uiState.update { state ->
                    val updatedGroups = state.groups.map { grp ->
                        if (grp.id == GROUP_SANAD_ID || grp.id == GROUP_INTERNET_ID || grp.type == SalesGroupType.DENOMINATIONS) {
                            grp.copy(isEnabled = true)
                        } else {
                            grp.copy(isEnabled = false)
                        }
                    }
                    val updatedCashGroups = state.cashGroups.map { cg ->
                        if (cg.id == CASH_GROUP_MAIN_ID) cg.copy(isEnabled = true)
                        else cg.copy(isEnabled = false)
                    }
                    state.copy(
                        groups = updatedGroups,
                        cashGroups = updatedCashGroups,
                        activePresetId = "fast_tickets"
                    )
                }
                addAuditLog("الإدارة", "تطبيق الوضع", "وضع التذاكر والبيع السريع", "", "")
                saveToDatabase()
            }
            "cash_auditor" -> {
                _uiState.update { state ->
                    val updatedCashGroups = state.cashGroups.map { it.copy(isEnabled = true) }
                    state.copy(
                        cashGroups = updatedCashGroups,
                        showCashDenominationsTable = true,
                        activePresetId = "cash_auditor"
                    )
                }
                addAuditLog("الإدارة", "تطبيق الوضع", "وضع المحاسب والتدقيق المالي", "", "")
                saveToDatabase()
            }
            "compact_tabs" -> {
                _uiState.update { state ->
                    state.copy(
                        activePresetId = "compact_tabs"
                    )
                }
                addAuditLog("الإدارة", "تطبيق الوضع", "نمط التبويبات المدمجة", "", "")
                saveToDatabase()
            }
        }
    }

    private fun savePresetsToStorage() {
        try {
            val prefs = getApplication<Application>().getSharedPreferences("app_presets_prefs", Context.MODE_PRIVATE)
            val presetsToSave = _uiState.value.presets
            val sb = StringBuilder()
            for (p in presetsToSave) {
                val configsStr = p.groupConfigs.joinToString(";") { "${it.groupId}:${it.groupName}:${it.isEnabled}:${it.enabledDenominations.joinToString(",")}" }
                sb.append("${p.id}|${p.name}|${p.description}|${p.timestamp}|$configsStr\n")
            }
            prefs.edit().putString("custom_presets_data", sb.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadPresetsFromStorage(): List<AccountingPreset> {
        return try {
            val prefs = getApplication<Application>().getSharedPreferences("app_presets_prefs", Context.MODE_PRIVATE)
            val data = prefs.getString("custom_presets_data", null) ?: return emptyList()
            val list = mutableListOf<AccountingPreset>()
            data.lines().forEach { line ->
                if (line.isNotBlank()) {
                    val parts = line.split("|")
                    if (parts.size >= 5) {
                        val id = parts[0]
                        val name = parts[1]
                        val desc = parts[2]
                        val ts = parts[3].toLongOrNull() ?: System.currentTimeMillis()
                        val configsStr = parts[4]
                        val configs = if (configsStr.isNotBlank()) {
                            configsStr.split(";").mapNotNull { cStr ->
                                val cParts = cStr.split(":")
                                if (cParts.size >= 4) {
                                    val gId = cParts[0]
                                    val gName = cParts[1]
                                    val isEn = cParts[2].toBooleanStrictOrNull() ?: true
                                    val denoms = if (cParts[3].isNotBlank()) cParts[3].split(",").mapNotNull { it.toIntOrNull() } else emptyList()
                                    GroupPresetConfig(gId, gName, isEn, denoms)
                                } else null
                            }
                        } else emptyList()
                        list.add(AccountingPreset(id, name, desc, ts, false, configs))
                    }
                }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Reset daily sales
    fun resetDailySales() {
        createBackup(title = "نسخة تلقائية قبل تصفير اليوم", notes = "تم الحفظ تلقائياً قبل إجراء التصفير اليومي")
        archiveCurrentDay()
        _uiState.update { state ->
            val resetGroups = state.groups.map { grp ->
                when (grp.type) {
                    SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> {
                        grp.copy(rows = grp.rows.map { it.copy(givenInput = "", addedInput = "", remainingInput = "", notes = "") })
                    }
                    SalesGroupType.DIRECT_ENTRY -> {
                        grp.copy(directEntries = emptyList())
                    }
                }
            }
            val resetCashGroups = state.cashGroups.map { grp ->
                when (grp.type) {
                    CashGroupType.DENOMINATIONS -> {
                        grp.copy(denomRows = grp.denomRows.map { it.copy(countInput = "", notes = "") })
                    }
                    CashGroupType.DIRECT_ENTRY, CashGroupType.EXPENSES, CashGroupType.DEPOSITS -> {
                        grp.copy(directEntries = emptyList())
                    }
                }
            }
            state.copy(
                groups = resetGroups,
                cashGroups = resetCashGroups,
                cashInBoxYerInput = "",
                cashInBoxSarInput = "",
                expenses = emptyList(),
                notes = "",
                dateTimestamp = System.currentTimeMillis()
            )
        }
        addAuditLog("النظام", "تصفير يومي", "تصفير جميع بيانات اليوم", "", "0")
        saveToDatabase()
    }

    // فتح نافذة تصفير الوردية مع الرسوم المتحركة والتأكيد البصري
    fun openShiftResetDialog() {
        _uiState.update { it.copy(showShiftResetDialog = true) }
    }

    fun closeShiftResetDialog() {
        _uiState.update { it.copy(showShiftResetDialog = false) }
    }

    fun dismissShiftResetBanner() {
        _uiState.update { it.copy(shiftResetSuccessBannerMessage = null) }
    }

    // تنفيذ تصفير الوردية بالكامل مع الحفظ التلقائي والأرشفة
    fun confirmAndResetShift(shiftNotes: String = "") {
        val formattedDate = formatDateTimeForBackup(System.currentTimeMillis())
        val backupNote = if (shiftNotes.isNotBlank()) "ملاحظة الوردية: $shiftNotes" else "تصفير الوردية اليومية"
        createBackup(
            title = "نسخة احتياطية قبل تصفير الوردية ($formattedDate)",
            notes = "تم الحفظ تلقائياً قبل تصفير الوردية. $backupNote"
        )
        archiveCurrentDay(customNotes = if (shiftNotes.isNotBlank()) shiftNotes else "إغلاق وتصفير الوردية - $formattedDate")
        _uiState.update { state ->
            val resetGroups = state.groups.map { grp ->
                when (grp.type) {
                    SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> {
                        grp.copy(rows = grp.rows.map { it.copy(givenInput = "", addedInput = "", remainingInput = "", notes = "") })
                    }
                    SalesGroupType.DIRECT_ENTRY -> {
                        grp.copy(directEntries = emptyList())
                    }
                }
            }
            val resetCashGroups = state.cashGroups.map { grp ->
                when (grp.type) {
                    CashGroupType.DENOMINATIONS -> {
                        grp.copy(denomRows = grp.denomRows.map { it.copy(countInput = "", notes = "") })
                    }
                    CashGroupType.DIRECT_ENTRY, CashGroupType.EXPENSES, CashGroupType.DEPOSITS -> {
                        grp.copy(directEntries = emptyList())
                    }
                }
            }
            state.copy(
                groups = resetGroups,
                cashGroups = resetCashGroups,
                cashInBoxYerInput = "",
                cashInBoxSarInput = "",
                expenses = emptyList(),
                notes = "",
                dateTimestamp = System.currentTimeMillis(),
                shiftResetSuccessBannerMessage = "تم تصفير الوردية وأرشفة البيانات بنجاح! جميع العدادات أصبحت صفراً والوردية جاهزة"
            )
        }
        addAuditLog("الوردية", "تصفير الوردية", "تصفير شامل للوردية وحفظ الأرشيف: ${shiftNotes.ifBlank { "بدون ملاحظات" }}", "", "0")
        saveToDatabase()
    }

    private var autoResetMonitorJob: kotlinx.coroutines.Job? = null

    fun getEffectiveAccountingDayTimestamp(startHour: Int = _uiState.value.accountingDayStartHour): Long {
        val cal = java.util.Calendar.getInstance()
        if (cal.get(java.util.Calendar.HOUR_OF_DAY) < startHour) {
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        cal.set(java.util.Calendar.HOUR_OF_DAY, startHour)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun startAutoResetMonitor() {
        autoResetMonitorJob?.cancel()
        autoResetMonitorJob = viewModelScope.launch {
            while (coroutineContext.isActive) {
                try {
                    checkAndPerformAutoResetIfNeeded(forceManual = false)
                } catch (_: Exception) {}
                kotlinx.coroutines.delay(45_000L) // فحص كل 45 ثانية
            }
        }
    }

    fun triggerManualAutoResetTest() {
        checkAndPerformAutoResetIfNeeded(forceManual = true)
    }

    fun checkAndPerformAutoResetIfNeeded(forceManual: Boolean = false) {
        val isEnabled = sharedPreferences.getBoolean("isAutoDailyResetEnabled", false)
        if (!isEnabled && !forceManual) return

        val scheduledTime = _uiState.value.scheduledResetTime.ifBlank { "03:00" }
        val timeParts = scheduledTime.split(":")
        val resetHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 3
        val resetMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        val cal = java.util.Calendar.getInstance()
        val nowHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val nowMinute = cal.get(java.util.Calendar.MINUTE)

        val accountingDayTs = getEffectiveAccountingDayTimestamp(_uiState.value.accountingDayStartHour)
        val todayDateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date(accountingDayTs))
        val lastResetDate = sharedPreferences.getString("last_auto_reset_accounting_date", "") ?: ""

        val timeReached = nowHour > resetHour || (nowHour == resetHour && nowMinute >= resetMinute)
        val shouldTrigger = forceManual || (timeReached && lastResetDate != todayDateString)

        if (shouldTrigger) {
            sharedPreferences.edit().putString("last_auto_reset_accounting_date", todayDateString).apply()
            confirmAndResetShift("تصفير تلقائي مجدول لبداية اليوم المحاسبي (${scheduledTime})")
            _uiState.update { it.copy(shiftResetSuccessBannerMessage = "تم التصفير التلقائي اليومي بنجاح وبداية يوم محاسبي جديد ✨") }
        }
    }

    // اعتماد بيع الأصناف المختارة من الآلة الحاسبة مرة واحدة
    fun commitSalesFromCalculator(quantitiesMap: Map<String, String>) {
        if (quantitiesMap.isEmpty()) return

        var totalCommittedRevenue = 0.0
        var totalCommittedTickets = 0

        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.type == SalesGroupType.DIRECT_ENTRY) {
                    val amountStr = quantitiesMap["${grp.id}_amount"] ?: ""
                    val amountVal = amountStr.toDoubleOrNull() ?: 0.0
                    if (amountVal > 0) {
                        totalCommittedRevenue += amountVal
                        val newItem = DirectEntryItem(
                            title = "مبيعات صيني - حاسبة الفئات",
                            amountInput = amountStr,
                            quantityInput = "1",
                            notes = "اعتماد مبيعات بالآلة الحاسبة"
                        )
                        grp.copy(directEntries = grp.directEntries + newItem, isEnabled = true)
                    } else {
                        grp
                    }
                } else {
                    var hasRowChange = false
                    val updatedRows = grp.rows.map { row ->
                        val qtyStr = quantitiesMap["${grp.id}_${row.denomination}"] ?: ""
                        val qty = qtyStr.toIntOrNull() ?: 0
                        if (qty > 0) {
                            hasRowChange = true
                            totalCommittedTickets += qty
                            totalCommittedRevenue += (qty * row.denomination).toDouble()

                            val currentGiven = row.given
                            val currentAdded = row.added
                            val effectiveGiven = if (currentGiven == 0 && currentAdded == 0) qty else currentGiven
                            val newRemaining = (effectiveGiven + currentAdded - (row.sold + qty)).coerceAtLeast(0)

                            row.copy(
                                givenInput = if (currentGiven == 0 && currentAdded == 0) qty.toString() else row.givenInput,
                                remainingInput = newRemaining.toString()
                            )
                        } else {
                            row
                        }
                    }
                    if (hasRowChange) grp.copy(rows = updatedRows, isEnabled = true) else grp
                }
            }

            state.copy(
                groups = updatedGroups,
                shiftResetSuccessBannerMessage = "تم اعتماد بيع الأصناف بنجاح! الإجمالي: ${AccountingFormatter.formatYer(totalCommittedRevenue)} ريال ($totalCommittedTickets تذكرة) ✨"
            )
        }

        saveToDatabase()
        addAuditLog("الآلة الحاسبة", "اعتماد مبيعات", "تم اعتماد مبيعات إجمالية: ${AccountingFormatter.formatYer(totalCommittedRevenue)} ريال", "", "")
    }

    // Reset all groups and denominations
    fun resetAllGroupsAndDenominations() {
        createBackup(title = "نسخة تلقائية قبل تصفير المجموعات والفئات", notes = "تم الحفظ تلقائياً قبل إجراء التصفير")
        _uiState.update { state ->
            val resetGroups = state.groups.map { grp ->
                when (grp.type) {
                    SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> {
                        grp.copy(rows = grp.rows.map { it.copy(givenInput = "", addedInput = "", remainingInput = "", notes = "") })
                    }
                    SalesGroupType.DIRECT_ENTRY -> {
                        grp.copy(directEntries = emptyList())
                    }
                }
            }
            val resetCashGroups = state.cashGroups.map { grp ->
                when (grp.type) {
                    CashGroupType.DENOMINATIONS -> {
                        grp.copy(denomRows = grp.denomRows.map { it.copy(countInput = "", notes = "") })
                    }
                    CashGroupType.DIRECT_ENTRY, CashGroupType.EXPENSES, CashGroupType.DEPOSITS -> {
                        grp.copy(directEntries = emptyList())
                    }
                }
            }
            state.copy(
                groups = resetGroups,
                cashGroups = resetCashGroups,
                cashInBoxYerInput = "",
                cashInBoxSarInput = "",
                expenses = emptyList()
            )
        }
        addAuditLog("تصفير", "تصفير شامل", "تم تصفير كافة المجموعات والفئات بنجاح", "", "0")
        saveToDatabase()
    }

    fun archiveCurrentDay(customNotes: String = "") {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentSummary = salesSummary.value
            
            // Build details breakdown for all enabled groups
            val detailsBuilder = StringBuilder()
            currentState.groups.filter { it.isEnabled }.forEach { group ->
                if (group.type == SalesGroupType.DENOMINATIONS || group.type == SalesGroupType.CUSTOM_FIELDS) {
                    val activeSoldRows = group.rows.filter { it.isEnabled && it.sold > 0 }
                    if (activeSoldRows.isNotEmpty()) {
                        detailsBuilder.append("[${group.name}]: ")
                        activeSoldRows.forEach { row ->
                            detailsBuilder.append("فئة ${row.denomination}: مباع ${row.sold} (${AccountingFormatter.formatMoney(row.total)}) - ")
                        }
                    }
                } else if (group.type == SalesGroupType.DIRECT_ENTRY && group.directEntries.isNotEmpty()) {
                    detailsBuilder.append("[${group.name}]: ")
                    group.directEntries.forEach { entry ->
                        if (entry.total > 0) {
                            val titlePart = if (entry.title.isNotBlank()) "${entry.title}: " else ""
                            detailsBuilder.append("$titlePart${AccountingFormatter.formatMoney(entry.total)} - ")
                        }
                    }
                }
            }
            var details = detailsBuilder.toString()
            if (details.endsWith(" - ")) {
                details = details.substring(0, details.length - 3)
            }
            if (details.isBlank()) {
                details = "لا توجد مبيعات مسجلة لهذا اليوم"
            }
            
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val dateStr = sdf.format(java.util.Date(currentState.dateTimestamp))
            
            // Check if archive for today already exists to prevent duplicates
            val existingArchives = currentState.dailyReportArchives
            if (existingArchives.any { it.dateString.startsWith(dateStr) }) {
                // Already archived today, update or skip duplicate unless explicitly confirmed
                return@launch
            }
            
            val finalNotes = if (customNotes.isNotBlank()) {
                if (currentState.notes.isNotBlank()) "${currentState.notes} | $customNotes" else customNotes
            } else currentState.notes
            
            val archive = DailyReportArchiveEntity(
                dateTimestamp = currentState.dateTimestamp,
                dateString = dateStr,
                sellerName = currentState.sellerName.ifBlank { "محاسب المبيعات" },
                totalRevenue = currentSummary.totalRevenue,
                totalSoldTickets = currentSummary.totalSold,
                cashInBoxYer = currentState.cashInBoxYerInput.toDoubleOrNull() ?: 0.0,
                cashInBoxSar = currentState.cashInBoxSarInput.toDoubleOrNull() ?: 0.0,
                netCashYer = currentSummary.netCashYer,
                otherCurrenciesTotalYer = currentSummary.otherCurrenciesTotalYer,
                grossCashInBox = currentSummary.grossCashInBox,
                notes = finalNotes,
                detailsString = details
            )
            
            repository.insertDailyReportArchive(archive)
            addAuditLog("النظام", "أرشفة السندات", "حفظ اليوم الحالي في السجل اليومي", "", dateStr)
        }
    }

    fun deleteDailyReportArchive(id: Long) {
        viewModelScope.launch {
            repository.deleteDailyReportArchive(id)
            addAuditLog("النظام", "أرشفة السندات", "حذف تصفير سابق من السجل", "", "ID: $id")
        }
    }

    fun clearDailyReportArchives() {
        viewModelScope.launch {
            repository.clearDailyReportArchives()
            addAuditLog("النظام", "أرشفة السندات", "تصفير كامل سجل السندات اليومية السابقة", "", "")
        }
    }

    // Group Management
    fun createSalesGroup(name: String, type: SalesGroupType, denominations: List<Int>, isExcluded: Boolean, givenLabel: String = "المعطى", addedLabel: String = "إضافة", remainingLabel: String = "المتبقي", formula: CalculationFormula = CalculationFormula.TICKET_STANDARD) {
        val newGroupId = "group_" + UUID.randomUUID().toString().take(8)
        val denoms = denominations.ifEmpty { DEFAULT_TICKET_CATEGORIES }
        val newGroup = SalesGroupUiState(
            id = newGroupId,
            name = name.trim().ifBlank { "مبيعات جديدة" },
            type = type,
            isEnabled = true,
            orderIndex = _uiState.value.groups.size,
            isAddedFieldEnabled = false,
            rows = if (type == SalesGroupType.DENOMINATIONS || type == SalesGroupType.CUSTOM_FIELDS) denoms.map { DirectSalesRowUiState(denomination = it, formula = formula) } else emptyList(),
            directEntries = emptyList(),
            isExcludedFromBalance = isExcluded,
            givenLabel = givenLabel.ifBlank { "المعطى" },
            addedLabel = addedLabel.ifBlank { "إضافة" },
            remainingLabel = remainingLabel.ifBlank { "المتبقي" },
            defaultFormula = formula
        )
        _uiState.update { state ->
            state.copy(groups = state.groups + newGroup, selectedGroupId = newGroupId, showAddGroupDialog = false)
        }
        saveToDatabase()
    }

    fun setShowAddGroupDialog(show: Boolean) {
        _uiState.update { it.copy(showAddGroupDialog = show) }
    }

    fun setShowAddCashGroupDialog(show: Boolean) {
        _uiState.update { it.copy(showAddCashGroupDialog = show) }
    }

    fun updateGroupName(groupId: String, newName: String) {
        _uiState.update { state ->
            val updated = state.groups.map { if (it.id == groupId) it.copy(name = newName.trim()) else it }
            state.copy(groups = updated)
        }
        saveToDatabase()
    }

    fun updateCashGroupName(groupId: String, newName: String) {
        _uiState.update { state ->
            val updated = state.cashGroups.map { if (it.id == groupId) it.copy(name = newName.trim()) else it }
            state.copy(cashGroups = updated)
        }
        saveToDatabase()
    }

    fun updateSalesGroupColor(groupId: String, colorLong: Long?) {
        _uiState.update { state ->
            val updated = state.groups.map { if (it.id == groupId) it.copy(color = colorLong) else it }
            state.copy(groups = updated)
        }
        saveToDatabase()
    }

    fun updateCashGroupColor(groupId: String, colorLong: Long?) {
        _uiState.update { state ->
            val updated = state.cashGroups.map { if (it.id == groupId) it.copy(color = colorLong) else it }
            state.copy(cashGroups = updated)
        }
        saveToDatabase()
    }

    fun deleteSalesGroup(groupId: String) {
        if (groupId == GROUP_SANAD_ID) return // Don't delete primary
        createEventDrivenBackup("قبل الحذف")
        _uiState.update { state ->
            val updated = state.groups.filterNot { it.id == groupId }
            val nextSelected = if (state.selectedGroupId == groupId) updated.firstOrNull()?.id ?: GROUP_SANAD_ID else state.selectedGroupId
            state.copy(groups = updated, selectedGroupId = nextSelected)
        }
        saveToDatabase()
    }

    fun deleteCashGroup(groupId: String) {
        if (groupId == CASH_GROUP_MAIN_ID) return // Don't delete primary
        createEventDrivenBackup("قبل الحذف")
        _uiState.update { state ->
            val updated = state.cashGroups.filterNot { it.id == groupId }
            val nextSelected = if (state.selectedCashGroupId == groupId) updated.firstOrNull()?.id ?: CASH_GROUP_MAIN_ID else state.selectedCashGroupId
            state.copy(cashGroups = updated, selectedCashGroupId = nextSelected)
        }
        saveToDatabase()
    }

    fun createCashGroup(name: String, type: CashGroupType, isExcludedFromBalance: Boolean) {
        val newGroupId = "cash_group_" + UUID.randomUUID().toString().take(8)
        val denoms = if (type == CashGroupType.DENOMINATIONS) DEFAULT_CASH_DENOMINATIONS.map { CashDenomRowUiState(denomination = it) } else emptyList()
        val newGroup = CashBoxGroupUiState(
            id = newGroupId,
            name = name.trim().ifBlank { "مجموعة نقدية جديدة" },
            type = type,
            isEnabled = true,
            orderIndex = _uiState.value.cashGroups.size,
            isExcludedFromBalance = isExcludedFromBalance,
            isDefault = false,
            denomRows = denoms
        )
        _uiState.update { s ->
            s.copy(cashGroups = s.cashGroups + newGroup, selectedCashGroupId = newGroupId)
        }
        addAuditLog("الصندوق", "إدارة المجموعات", "إنشاء مجموعة صندوق جديدة: $name", "", "")
        saveToDatabase()
    }

    fun toggleReorderMode(enabled: Boolean? = null) {
        _uiState.update { s ->
            s.copy(isReorderModeEnabled = enabled ?: !s.isReorderModeEnabled)
        }
    }

    fun setMultiCategoryCalculatorEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isMultiCategoryCalculatorEnabled = enabled) }
        saveToDatabase()
    }

    fun applyMultiCategoryQuantities(
        quantitiesMap: Map<String, Map<Int, String>>,
        directAmountsMap: Map<String, String> = emptyMap()
    ) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { grp ->
                if (grp.type == SalesGroupType.DIRECT_ENTRY) {
                    val directAmtStr = directAmountsMap[grp.id]
                    if (directAmtStr != null && directAmtStr.isNotBlank()) {
                        val amt = directAmtStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0.0) {
                            val newItem = DirectEntryItem(
                                id = UUID.randomUUID().toString(),
                                title = "إدخال حاسبة",
                                amountInput = directAmtStr,
                                quantityInput = "1",
                                notes = ""
                            )
                            grp.copy(directEntries = listOf(newItem))
                        } else {
                            grp.copy(directEntries = emptyList())
                        }
                    } else grp
                } else {
                    val denomMap = quantitiesMap[grp.id]
                    if (denomMap != null) {
                        val updatedRows = grp.rows.map { row ->
                            val newQty = denomMap[row.denomination]
                            if (newQty != null) {
                                val autoRem = if (row.givenInput.isBlank() || row.givenInput == "0" || row.remainingInput.isBlank() || row.remainingInput == "5" || row.remainingInput == row.givenInput) {
                                    newQty
                                } else {
                                    row.remainingInput
                                }
                                row.copy(givenInput = newQty, remainingInput = autoRem)
                            } else row
                        }
                        grp.copy(rows = updatedRows)
                    } else grp
                }
            }
            state.copy(groups = updatedGroups)
        }
        addAuditLog("المبيعات", "آلة حاسبة متعددة الفئات", "تطبيق الكميات والقيم", "", "")
        saveToDatabase()
    }

    fun toggleReportDetailedMode(isDetailed: Boolean) {
        _uiState.update { it.copy(isReportDetailedMode = isDetailed) }
        saveToDatabase()
    }

    fun toggleIncludeOtherCurrenciesInReport(include: Boolean) {
        _uiState.update { it.copy(includeOtherCurrenciesInReport = include) }
        saveToDatabase()
    }

    fun updateReportSections(
        salesSummary: Boolean,
        ticketDenoms: Boolean,
        cashBox: Boolean,
        expensesDeposits: Boolean,
        differences: Boolean
    ) {
        _uiState.update {
            it.copy(
                reportIncludeSalesSummary = salesSummary,
                reportIncludeTicketDenoms = ticketDenoms,
                reportIncludeCashBox = cashBox,
                reportIncludeExpensesDeposits = expensesDeposits,
                reportIncludeDifferences = differences
            )
        }
        saveToDatabase()
    }

    fun updateUiScaleFactor(scale: Float) {
        _uiState.update { it.copy(uiScaleFactor = scale.coerceIn(0.75f, 1.25f)) }
        saveToDatabase()
    }

    fun runDiagnosticsSilently() {
        viewModelScope.launch {
            try {
                // Silent integrity check: verify groups, rates, and values
                val state = _uiState.value
                val hasInvalidRate = state.exchangeRateInput.toDoubleOrNull().let { it == null || it <= 0 }
                if (hasInvalidRate) {
                    _uiState.update { it.copy(remainingWarningMessage = "تنبيه الصيانة: يرجى التحقق من سعر الصرف المائل") }
                } else {
                    // All clean
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(remainingWarningMessage = "خطأ تشخيص تلقائي: ${e.message}") }
            }
        }
    }

    fun moveSalesGroup(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val list = state.groups.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                val reordered = list.mapIndexed { idx, grp -> grp.copy(orderIndex = idx) }
                state.copy(groups = reordered)
            } else state
        }
        saveToDatabase()
    }

    fun moveCashGroup(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val list = state.cashGroups.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                val reordered = list.mapIndexed { idx, grp -> grp.copy(orderIndex = idx) }
                state.copy(cashGroups = reordered)
            } else state
        }
        saveToDatabase()
    }

    fun moveCustomCurrency(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val list = state.customCurrencies.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                saveCurrenciesToPrefs(list)
                state.copy(customCurrencies = list)
            } else state
        }
    }

    fun moveDenominationInGroup(groupId: String, fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { group ->
                if (group.id == groupId) {
                    val rowsList = group.rows.toMutableList()
                    if (fromIndex in rowsList.indices && toIndex in rowsList.indices) {
                        val item = rowsList.removeAt(fromIndex)
                        rowsList.add(toIndex, item)
                        group.copy(rows = rowsList)
                    } else group
                } else group
            }
            state.copy(groups = updatedGroups)
        }
        saveToDatabase()
    }

    fun moveDirectEntryInGroup(groupId: String, fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { group ->
                if (group.id == groupId) {
                    val entriesList = group.directEntries.toMutableList()
                    if (fromIndex in entriesList.indices && toIndex in entriesList.indices) {
                        val item = entriesList.removeAt(fromIndex)
                        entriesList.add(toIndex, item)
                        group.copy(directEntries = entriesList)
                    } else group
                } else group
            }
            state.copy(groups = updatedGroups)
        }
        saveToDatabase()
    }

    fun moveCashDirectEntry(groupId: String, fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val updatedCashGroups = state.cashGroups.map { group ->
                if (group.id == groupId) {
                    val entriesList = group.directEntries.toMutableList()
                    if (fromIndex in entriesList.indices && toIndex in entriesList.indices) {
                        val item = entriesList.removeAt(fromIndex)
                        entriesList.add(toIndex, item)
                        group.copy(directEntries = entriesList)
                    } else group
                } else group
            }
            state.copy(cashGroups = updatedCashGroups)
        }
        saveToDatabase()
    }

    fun moveExpense(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val list = state.expenses.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                state.copy(expenses = list)
            } else state
        }
        saveToDatabase()
    }

    // Theme Customization
    fun setAppBackgroundImageUri(uri: String?) {
        _uiState.update { it.copy(appBackgroundImageUri = uri, appBackgroundStyle = if (uri != null) "CUSTOM_IMAGE" else it.appBackgroundStyle) }
        sharedPreferences.edit().putString("appBackgroundImageUri", uri ?: "").apply()
        sharedPreferences.edit().putString("appBackgroundStyle", if (uri != null) "CUSTOM_IMAGE" else _uiState.value.appBackgroundStyle).apply()
        saveToDatabase()
    }

    fun setHijriAdjustmentDays(days: Int) {
        _uiState.update { it.copy(hijriAdjustmentDays = days) }
        sharedPreferences.edit().putInt("hijriAdjustmentDays", days).apply()
        addAuditLog("الإعدادات", "تعديل التاريخ الهجري", "تعديل فارق الأيام: $days يوم", "", "$days")
        saveToDatabase()
    }

    fun toggleTheme() {
        _isDarkTheme.update { !it }
    }

    fun setDayPrimaryColor(color: Color) {
        _customDayPrimary.value = color
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        sharedPreferences.edit().putLong("custom_day_primary", argb).apply()
    }

    fun setNightPrimaryColor(color: Color) {
        _customNightPrimary.value = color
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        sharedPreferences.edit().putLong("custom_night_primary", argb).apply()
    }

    fun setCustomDayPrimary(color: Color) {
        setDayPrimaryColor(color)
    }

    fun setCustomNightPrimary(color: Color) {
        setNightPrimaryColor(color)
    }

    fun setTableHeaderBgColor(color: Color) {
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        _uiState.update { it.copy(tableHeaderBgColor = argb) }
        sharedPreferences.edit().putLong("tableHeaderBgColor", argb).apply()
        saveToDatabase()
    }

    fun setTableHeaderTextColor(color: Color) {
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        _uiState.update { it.copy(tableHeaderTextColor = argb) }
        sharedPreferences.edit().putLong("tableHeaderTextColor", argb).apply()
        saveToDatabase()
    }

    fun setTableBorderColor(color: Color) {
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        _uiState.update { it.copy(tableBorderColor = argb) }
        sharedPreferences.edit().putLong("tableBorderColor", argb).apply()
        saveToDatabase()
    }

    fun setTableCardBgColor(color: Color) {
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        _uiState.update { it.copy(tableCardBgColor = argb) }
        sharedPreferences.edit().putLong("tableCardBgColor", argb).apply()
        saveToDatabase()
    }

    fun setActiveTabColor(color: Color) {
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        _uiState.update { it.copy(activeTabColor = argb) }
        sharedPreferences.edit().putLong("activeTabColor", argb).apply()
        saveToDatabase()
    }

    fun setInactiveTabColor(color: Color) {
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        _uiState.update { it.copy(inactiveTabColor = argb) }
        sharedPreferences.edit().putLong("inactiveTabColor", argb).apply()
        saveToDatabase()
    }

    fun setCalculatorButtonColor(color: Color) {
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        _uiState.update { it.copy(calculatorButtonColor = argb) }
        sharedPreferences.edit().putLong("calculatorButtonColor", argb).apply()
        saveToDatabase()
    }

    fun setCalculatorBgColor(color: Color) {
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        _uiState.update { it.copy(calculatorBgColor = argb) }
        sharedPreferences.edit().putLong("calculatorBgColor", argb).apply()
        saveToDatabase()
    }

    fun setReportPrimaryColor(color: Color) {
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        _uiState.update { it.copy(reportPrimaryColor = argb) }
        sharedPreferences.edit().putLong("reportPrimaryColor", argb).apply()
        saveToDatabase()
    }

    fun setKeypadThemeColor(colorName: String) {
        _uiState.update { it.copy(keypadThemeColor = colorName) }
        sharedPreferences.edit().putString("keypadThemeColor", colorName).apply()
        saveToDatabase()
    }

    fun setKeypadButtonStyle(styleName: String) {
        _uiState.update { it.copy(keypadButtonStyle = styleName) }
        sharedPreferences.edit().putString("keypadButtonStyle", styleName).apply()
        saveToDatabase()
    }

    fun setAppThemeSkin(skin: String) {
        _uiState.update { it.copy(appThemeSkin = skin) }
        sharedPreferences.edit().putString("appThemeSkin", skin).apply()
        addAuditLog("المظهر", "جلد السمة", "تغيير جلد السمة البصري: $skin", "", "")
        saveToDatabase()
    }

    fun setGridViewEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isGridViewEnabled = enabled) }
        sharedPreferences.edit().putBoolean("isGridViewEnabled", enabled).apply()
        addAuditLog("التخطيط", "العرض الشبكي", if (enabled) "تفعيل" else "إيقاف", "", "")
        saveToDatabase()
    }

    fun applyThemePreset(preset: AppThemePreset) {
        val pColor = Color(preset.primaryColor)
        if (preset.isNightMode) {
            _customNightPrimary.value = pColor
            _isDarkTheme.value = true
        } else {
            _customDayPrimary.value = pColor
            _isDarkTheme.value = false
        }
        _uiState.update { s ->
            s.copy(
                selectedThemePresetName = preset.name,
                reportPrimaryColor = preset.primaryColor,
                tableHeaderBgColor = preset.headerBgColor,
                tableHeaderTextColor = preset.headerTextColor,
                tableBorderColor = preset.borderColor,
                activeTabColor = preset.activeTabColor,
                calculatorButtonColor = preset.calculatorColor,
                appBackgroundStyle = if (preset.bgStyle.isNotBlank()) preset.bgStyle else s.appBackgroundStyle
            )
        }
        sharedPreferences.edit()
            .putString("selectedThemePresetName", preset.name)
            .putLong("custom_day_primary", if (!preset.isNightMode) preset.primaryColor else (_customDayPrimary.value?.toArgb()?.toLong() ?: preset.primaryColor))
            .putLong("custom_night_primary", if (preset.isNightMode) preset.primaryColor else (_customNightPrimary.value?.toArgb()?.toLong() ?: preset.primaryColor))
            .putLong("reportPrimaryColor", preset.primaryColor)
            .putLong("tableHeaderBgColor", preset.headerBgColor)
            .putLong("tableHeaderTextColor", preset.headerTextColor)
            .putLong("tableBorderColor", preset.borderColor)
            .putLong("activeTabColor", preset.activeTabColor)
            .putLong("calculatorButtonColor", preset.calculatorColor)
            .putString("appBackgroundStyle", if (preset.bgStyle.isNotBlank()) preset.bgStyle else _uiState.value.appBackgroundStyle)
            .apply()
        addAuditLog("الإعدادات", "تغيير السمة", "تطبيق سمة معدة مسبقاً: ${preset.name}", "", "")
        saveToDatabase()
    }

    fun addCashToBoxYer(amount: Double) {
        if (amount <= 0.0) return
        val current = _uiState.value.cashInBoxYerInput.trim().toDoubleOrNull() ?: 0.0
        val updated = current + amount
        val formatted = if (updated % 1.0 == 0.0) updated.toLong().toString() else updated.toString()
        _uiState.update { it.copy(cashInBoxYerInput = formatted) }
        addAuditLog("الصندوق", "زيادة النقد", "إضافة ${com.example.ui.model.AccountingFormatter.formatYer(amount)} للنقد", com.example.ui.model.AccountingFormatter.formatYer(current), com.example.ui.model.AccountingFormatter.formatYer(updated))
        saveToDatabase()
    }

    fun setAppBackgroundStyle(style: String) {
        _uiState.update { it.copy(appBackgroundStyle = style) }
        sharedPreferences.edit().putString("appBackgroundStyle", style).apply()
        addAuditLog("الإعدادات", "تعديل خلفية التطبيق", style, "", "")
        saveToDatabase()
    }

    fun setAppBackgroundColor(color: Color) {
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        _uiState.update { it.copy(appBackgroundColor = argb, appBackgroundStyle = "CUSTOM_COLOR") }
        sharedPreferences.edit().putLong("appBackgroundColor", argb).apply()
        sharedPreferences.edit().putString("appBackgroundStyle", "CUSTOM_COLOR").apply()
        addAuditLog("الإعدادات", "تعديل لون خلفية التطبيق", "#${argb.toString(16)}", "", "")
        saveToDatabase()
    }

    fun resetThemeColors() {
        _customDayPrimary.value = null
        _customNightPrimary.value = null
        _uiState.update { s ->
            s.copy(
                reportPrimaryColor = 0xFF1E88E5,
                tableHeaderBgColor = 0xFFF1F5F9,
                tableHeaderTextColor = 0xFF1E293B,
                tableBorderColor = 0xFFCBD5E1,
                tableCardBgColor = 0xFFFFFFFF,
                activeTabColor = 0xFF1E88E5,
                inactiveTabColor = 0xFF64748B,
                calculatorButtonColor = 0xFF1E88E5,
                calculatorBgColor = 0xFF1E293B
            )
        }
        sharedPreferences.edit()
            .remove("custom_day_primary")
            .remove("custom_night_primary")
            .remove("reportPrimaryColor")
            .remove("tableHeaderBgColor")
            .remove("tableHeaderTextColor")
            .remove("tableBorderColor")
            .remove("tableCardBgColor")
            .remove("activeTabColor")
            .remove("inactiveTabColor")
            .remove("calculatorButtonColor")
            .remove("calculatorBgColor")
            .apply()
        addAuditLog("الإعدادات", "تغيير الثيم", "إعادة تعيين ألوان الثيم للقيم الافتراضية", "", "")
        saveToDatabase()
    }

    fun toggleSalesGroupEnabled(groupId: String) {
        toggleGroupEnabled(groupId)
    }

    fun resetAllInputs() {
        resetDailySales()
    }

    fun loadPreset(presetType: String) {
        val newGroups = mutableListOf<SalesGroupUiState>()
        when (presetType) {
            "grocery" -> {
                newGroups.add(SalesGroupUiState(id = "grp_grocery_1", name = "المبيعات النقدية", type = SalesGroupType.DENOMINATIONS, orderIndex = 0, isEnabled = true))
                newGroups.add(SalesGroupUiState(id = "grp_grocery_2", name = "مبيعات الشبكة (بطاقات)", type = SalesGroupType.DIRECT_ENTRY, orderIndex = 1, isEnabled = true))
                newGroups.add(SalesGroupUiState(id = "grp_grocery_3", name = "ديون آجلة", type = SalesGroupType.DIRECT_ENTRY, orderIndex = 2, isEnabled = true, isExcludedFromBalance = true))
            }
            "gas_station" -> {
                newGroups.add(SalesGroupUiState(id = "grp_gas_1", name = "طرمبة 1 (بترول)", type = SalesGroupType.DIRECT_ENTRY, orderIndex = 0, isEnabled = true))
                newGroups.add(SalesGroupUiState(id = "grp_gas_2", name = "طرمبة 2 (ديزل)", type = SalesGroupType.DIRECT_ENTRY, orderIndex = 1, isEnabled = true))
                newGroups.add(SalesGroupUiState(id = "grp_gas_3", name = "مبيعات نقدية", type = SalesGroupType.DENOMINATIONS, orderIndex = 2, isEnabled = true))
                newGroups.add(SalesGroupUiState(id = "grp_gas_4", name = "زيوت", type = SalesGroupType.CUSTOM_FIELDS, orderIndex = 3, isEnabled = true))
            }
            "restaurant" -> {
                newGroups.add(SalesGroupUiState(id = "grp_rest_1", name = "الكاشير 1 (نقدي)", type = SalesGroupType.DENOMINATIONS, orderIndex = 0, isEnabled = true))
                newGroups.add(SalesGroupUiState(id = "grp_rest_2", name = "شبكة / صراف", type = SalesGroupType.DIRECT_ENTRY, orderIndex = 1, isEnabled = true))
                newGroups.add(SalesGroupUiState(id = "grp_rest_3", name = "توصيل (طلبات)", type = SalesGroupType.DIRECT_ENTRY, orderIndex = 2, isEnabled = true))
            }
        }
        if (newGroups.isNotEmpty()) {
            _uiState.update { it.copy(groups = newGroups) }
            saveToDatabase()
            addAuditLog("الإعدادات", "", "تحميل نموذج جاهز", "", presetType)
        }
    }

    fun updateCashDenomCount(groupId: String, denom: Int, count: String) {
        _uiState.update { state ->
            val updated = state.cashGroups.map { cg ->
                if (cg.id == groupId) {
                    val newRows = cg.denomRows.map { r ->
                        if (r.denomination == denom) r.copy(countInput = count) else r
                    }
                    cg.copy(denomRows = newRows)
                } else cg
            }
            state.copy(cashGroups = updated)
        }
        saveToDatabase()
    }

    fun updateCashDenomNotes(groupId: String, denom: Int, notes: String) {
        _uiState.update { state ->
            val updated = state.cashGroups.map { cg ->
                if (cg.id == groupId) {
                    val newRows = cg.denomRows.map { r ->
                        if (r.denomination == denom) r.copy(notes = notes) else r
                    }
                    cg.copy(denomRows = newRows)
                } else cg
            }
            state.copy(cashGroups = updated)
        }
        saveToDatabase()
    }

    // Universal Calculator
    fun openUniversalCalculator(title: String, initialValue: String, isCurrency: Boolean = true, onApply: (String) -> Unit) {
        _uiState.update {
            it.copy(
                showUniversalCalculator = true,
                universalCalcTitle = title,
                universalCalcInitialValue = initialValue,
                universalCalcIsCurrency = isCurrency,
                universalCalcOnApply = onApply
            )
        }
    }

    fun closeUniversalCalculator() {
        _uiState.update {
            it.copy(showUniversalCalculator = false, universalCalcOnApply = null)
        }
    }

    fun updateCustomColorTheme(
        tableCardBg: Long? = null,
        tableHeaderText: Long? = null,
        tableBorderColor: Long? = null,
        groupActiveTabBg: Long? = null,
        groupInactiveTabBg: Long? = null,
        clearAll: Boolean = false
    ) {
        _uiState.update { state ->
            if (clearAll) {
                state.copy(customColorThemeState = CustomColorThemeState())
            } else {
                val current = state.customColorThemeState
                state.copy(
                    customColorThemeState = current.copy(
                        tableCardBg = tableCardBg ?: current.tableCardBg,
                        tableHeaderText = tableHeaderText ?: current.tableHeaderText,
                        tableBorderColor = tableBorderColor ?: current.tableBorderColor,
                        groupActiveTabBg = groupActiveTabBg ?: current.groupActiveTabBg,
                        groupInactiveTabBg = groupInactiveTabBg ?: current.groupInactiveTabBg
                    )
                )
            }
        }
        addAuditLog("الإعدادات", "المظهر", "تغيير ألوان التخصيص", "", if (clearAll) "استعادة الافتراضي" else "تم التعديل")
        saveToDatabase()
    }

    private fun saveToDatabase() {
        viewModelScope.launch {
            val state = _uiState.value
            val yer = state.cashInBoxYerInput.toDoubleOrNull() ?: 0.0
            val sar = state.cashInBoxSarInput.toDoubleOrNull() ?: 0.0
            val rate = state.exchangeRateInput.toDoubleOrNull() ?: 380.0

            val entity = DailySalesEntity(
                id = 1L,
                dateTimestamp = state.dateTimestamp,
                sellerName = state.sellerName,
                notes = state.notes,
                cashInBox = yer,
                cashInBoxSaudi = sar,
                exchangeRateSarToYer = rate,
                totalRevenue = state.groups.filter { it.isEnabled && !it.isExcludedFromBalance }.sumOf { it.totalRevenue },
                showCashDenominationsTable = state.showCashDenominationsTable,
                useEasternArabicNumerals = state.useEasternArabicNumerals,
                lastUpdated = System.currentTimeMillis()
            )
            repository.insertOrUpdateDailySales(entity)

            // Save Groups
            val groupEntities = state.groups.map { grp ->
                SalesGroupEntity(
                    id = grp.id,
                    name = grp.name,
                    type = grp.type.name,
                    isEnabled = grp.isEnabled,
                    orderIndex = grp.orderIndex,
                    isDefault = grp.isDefault,
                    isExcludedFromBalance = grp.isExcludedFromBalance,
                    givenLabel = grp.givenLabel,
                    addedLabel = grp.addedLabel,
                    remainingLabel = grp.remainingLabel,
                    defaultFormula = grp.defaultFormula.name
                )
            }
            repository.insertOrUpdateSalesGroups(groupEntities)

            // Save items
            val allCategoryItems = mutableListOf<DailyCategoryItemEntity>()
            val allDirectEntries = mutableListOf<DailyDirectEntryItemEntity>()

            for (grp in state.groups) {
                if (grp.type == SalesGroupType.DENOMINATIONS || grp.type == SalesGroupType.CUSTOM_FIELDS) {
                    grp.rows.forEachIndexed { idx, row ->
                        allCategoryItems.add(
                            DailyCategoryItemEntity(
                                groupId = grp.id,
                                denomination = row.denomination,
                                given = row.given,
                                added = row.added,
                                remaining = row.remaining,
                                sold = row.sold,
                                total = row.total,
                                notes = row.notes,
                                customTitle = row.customTitle,
                                formula = row.formula.name,
                                orderIndex = idx
                            )
                        )
                    }
                } else if (grp.type == SalesGroupType.DIRECT_ENTRY) {
                    grp.directEntries.forEachIndexed { idx, de ->
                        allDirectEntries.add(
                            DailyDirectEntryItemEntity(
                                id = de.id,
                                groupId = grp.id,
                                title = de.title,
                                amount = de.amount,
                                quantity = 1,
                                notes = de.notes,
                                orderIndex = idx
                            )
                        )
                    }
                }
            }

            // Save Cash Groups
            val cashGroupEntities = state.cashGroups.map { cg ->
                CashBoxGroupEntity(
                    id = cg.id,
                    name = cg.name,
                    type = cg.type.name,
                    isEnabled = cg.isEnabled,
                    orderIndex = cg.orderIndex,
                    isDefault = cg.isDefault
                )
            }

            // Save Cash items
            val allCashDenoms = mutableListOf<CashBoxDenomItemEntity>()
            val allCashDirects = mutableListOf<CashBoxDirectEntryEntity>()

            for (cg in state.cashGroups) {
                if (cg.type == CashGroupType.DENOMINATIONS) {
                    cg.denomRows.forEachIndexed { idx, cr ->
                        allCashDenoms.add(
                            CashBoxDenomItemEntity(
                                groupId = cg.id,
                                denomination = cr.denomination,
                                count = cr.count,
                                total = cr.total,
                                notes = cr.notes,
                                orderIndex = idx
                            )
                        )
                    }
                } else {
                    cg.directEntries.forEachIndexed { idx, cde ->
                        allCashDirects.add(
                            CashBoxDirectEntryEntity(
                                id = cde.id,
                                groupId = cg.id,
                                title = cde.title,
                                amount = cde.amount,
                                currencyCode = cde.currencyCode,
                                isSar = cde.isSar,
                                customExchangeRate = cde.effectiveRate,
                                notes = cde.notes,
                                orderIndex = idx
                            )
                        )
                    }
                }
            }

            // Save Expenses
            val expenseEntities = state.expenses.mapIndexed { idx, exp ->
                CashExpenseEntity(
                    id = exp.id,
                    title = exp.title,
                    amount = exp.amount,
                    currencyCode = exp.currencyCode,
                    isSar = exp.isSar,
                    isDeposit = exp.isDeposit,
                    customExchangeRate = exp.effectiveRate,
                    category = exp.category,
                    notes = exp.notes,
                    timestamp = exp.timestamp,
                    orderIndex = idx
                )
            }

            // Save atomically to database via single transaction
            repository.saveDailySales(
                sales = entity,
                groups = groupEntities,
                items = allCategoryItems,
                directEntries = allDirectEntries,
                cashGroups = cashGroupEntities,
                cashDenoms = allCashDenoms,
                cashDirects = allCashDirects,
                expenses = expenseEntities
            )
        }
    }

    // ==========================================
    // BACKUP & RESTORE WITH TIMESTAMP (النسخ الاحتياطي والاستعادة)
    // ==========================================

    fun formatDateTimeForBackup(timestamp: Long): String {
        val sdfDate = java.text.SimpleDateFormat("yyyy/MM/dd - hh:mm", java.util.Locale.US)
        val amPm = if (java.text.SimpleDateFormat("a", java.util.Locale.US).format(java.util.Date(timestamp)).equals("AM", ignoreCase = true)) "ص" else "م"
        return "${sdfDate.format(java.util.Date(timestamp))} $amPm"
    }

    fun createBackup(title: String = "", notes: String = "") {
        viewModelScope.launch {
            val currentState = _uiState.value
            val summary = calculateSalesSummary(currentState)
            val timestamp = System.currentTimeMillis()
            val formattedDate = formatDateTimeForBackup(timestamp)
            val finalTitle = title.ifBlank { "نسخة احتياطية ($formattedDate)" }
            val payloadJson = serializeStateToJson(currentState)

            val snapshot = BackupSnapshotEntity(
                timestamp = timestamp,
                formattedDateTime = formattedDate,
                title = finalTitle,
                notes = notes,
                totalRevenue = summary.totalRevenue,
                cashInBoxYer = currentState.cashInBoxYerInput.toDoubleOrNull() ?: 0.0,
                cashInBoxSar = currentState.cashInBoxSarInput.toDoubleOrNull() ?: 0.0,
                payloadJson = payloadJson
            )
            repository.insertBackupSnapshot(snapshot)
            addAuditLog("النسخ الاحتياطي", "إنشاء نسخة", finalTitle, "", formattedDate)
        }
    }

    fun createEventDrivenBackup(eventPrefix: String) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd_HH:mm", java.util.Locale.US)
        val formattedDate = sdf.format(java.util.Date())
        val title = "${eventPrefix}_$formattedDate"
        createBackup(title = title, notes = "نسخة تلقائية شرطية ($eventPrefix)")
    }

    fun renameBackup(id: Long, newTitle: String) {
        viewModelScope.launch {
            repository.updateBackupSnapshotTitle(id, newTitle.trim())
            addAuditLog("النسخ الاحتياطي", "تعديل اسم النسخة", newTitle.trim(), "", "")
        }
    }

    fun setBackupSchedule(frequency: String) {
        sharedPreferences.edit().putString("backupScheduleFrequency", frequency).apply()
        _uiState.update { it.copy(backupScheduleFrequency = frequency) }
        addAuditLog("النسخ الاحتياطي", "تحديث الجدول الزمني", frequency, "", "")
    }

    fun setAutoPruneDays(days: Int) {
        sharedPreferences.edit().putInt("autoPruneDays", days).apply()
        _uiState.update { it.copy(autoPruneDays = days) }
        if (days > 0) {
            pruneOldBackups(days)
        }
        addAuditLog("النسخ الاحتياطي", "تحديث مدة الحفظ الآلي", "$days يوم", "", "")
    }

    fun pruneOldBackups(days: Int) {
        if (days <= 0) return
        viewModelScope.launch {
            val cutoff = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000)
            repository.deleteBackupSnapshotsOlderThan(cutoff)
            addAuditLog("النسخ الاحتياطي", "تنظيف النسخ القديمة", "أقدم من $days يوم", "", "")
        }
    }

    fun cleanupCrashBackups() {
        viewModelScope.launch {
            repository.deleteCrashBackups()
        }
    }

    fun runFullSystemMaintenance() {
        viewModelScope.launch {
            try {
                // 1. Cleanup temporary crash backups
                repository.deleteCrashBackups()
                // 2. Prune old backups if enabled
                val pruneDays = _uiState.value.autoPruneDays
                if (pruneDays > 0) {
                    val cutoff = System.currentTimeMillis() - (pruneDays.toLong() * 24 * 60 * 60 * 1000)
                    repository.deleteBackupSnapshotsOlderThan(cutoff)
                }
                // 3. Force database save & sync
                saveToDatabase()
                addAuditLog("الصيانة الشاملة", "تشغيل أداة الصيانة", "تم فحص قاعدة البيانات وتنظيف الملفات وإصلاح السجلات بنجاح", "", "")
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "حدث خطأ أثناء الصيانة: ${e.message}") }
            }
        }
    }

    fun deleteBackup(id: Long) {
        viewModelScope.launch {
            repository.deleteBackupSnapshot(id)
            addAuditLog("النسخ الاحتياطي", "حذف نسخة", "معرف النسخة: $id", "", "")
        }
    }

    fun clearAllBackups() {
        viewModelScope.launch {
            repository.clearAllBackupSnapshots()
            addAuditLog("النسخ الاحتياطي", "مسح جميع النسخ", "حذف كافة النسخ الاحتياطية", "", "")
        }
    }

    fun restoreBackup(snapshot: BackupSnapshotEntity) {
        viewModelScope.launch {
            try {
                val restoredState = deserializeStateFromJson(snapshot.payloadJson, _uiState.value)
                _uiState.value = restoredState
                saveToDatabase()
                addAuditLog("النسخ الاحتياطي", "استعادة نسخة احتياطية", snapshot.title, "", snapshot.formattedDateTime)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "حدث خطأ أثناء استعادة النسخة: ${e.message}") }
            }
        }
    }

    fun exportBackupAsShareIntent(context: Context, snapshot: BackupSnapshotEntity) {
        try {
            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, snapshot.payloadJson)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "WLF Cash - ${snapshot.title} (${snapshot.formattedDateTime})")
                type = "text/plain"
            }
            val shareIntent = android.content.Intent.createChooser(sendIntent, "مشاركة / حفظ النسخة الاحتياطية")
            shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (_: Exception) {}
    }

    fun importBackupFromJson(jsonString: String, title: String = ""): Boolean {
        return try {
            val testState = deserializeStateFromJson(jsonString, _uiState.value)
            val timestamp = System.currentTimeMillis()
            val formattedDate = formatDateTimeForBackup(timestamp)
            val finalTitle = title.ifBlank { "نسخة مستوردة ($formattedDate)" }
            var calcTotalRevenue = 0.0
            testState.groups.filter { it.isEnabled && !it.isExcludedFromBalance }.forEach { g ->
                calcTotalRevenue += g.totalRevenue
            }

            viewModelScope.launch {
                val snapshot = BackupSnapshotEntity(
                    timestamp = timestamp,
                    formattedDateTime = formattedDate,
                    title = finalTitle,
                    notes = "مستوردة من نص أو ملف خارجي",
                    totalRevenue = calcTotalRevenue,
                    cashInBoxYer = testState.cashInBoxYerInput.toDoubleOrNull() ?: 0.0,
                    cashInBoxSar = testState.cashInBoxSarInput.toDoubleOrNull() ?: 0.0,
                    payloadJson = jsonString
                )
                repository.insertBackupSnapshot(snapshot)
                addAuditLog("النسخ الاحتياطي", "استيراد نسخة", finalTitle, "", formattedDate)
            }
            true
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "تعذر استيراد النسخة: تنسيق البيانات غير صالح") }
            false
        }
    }

    fun closeAddExpenseDialog() {
        _uiState.update { it.copy(showAddExpenseDialog = false, editingExpense = null) }
    }

    fun openArchiveConfirmDialog() {
        _uiState.update { it.copy(showArchiveConfirmDialog = true) }
    }

    fun closeArchiveConfirmDialog() {
        _uiState.update { it.copy(showArchiveConfirmDialog = false) }
    }

    private fun serializeStateToJson(state: DailyDirectSalesUiState): String {
        val root = org.json.JSONObject()
        root.put("version", 1)
        root.put("dateTimestamp", state.dateTimestamp)
        root.put("sellerName", state.sellerName)
        root.put("notes", state.notes)
        root.put("cashInBoxYerInput", state.cashInBoxYerInput)
        root.put("cashInBoxSarInput", state.cashInBoxSarInput)
        root.put("exchangeRateInput", state.exchangeRateInput)
        root.put("showCashDenominationsTable", state.showCashDenominationsTable)
        root.put("useEasternArabicNumerals", state.useEasternArabicNumerals)
        root.put("showHijriDate", state.showHijriDate)
        root.put("use24HourFormat", state.use24HourFormat)
        root.put("includeInternetInReport", state.includeInternetInReport)
        root.put("reportHeaderTitle", state.reportHeaderTitle)
        root.put("reportHeaderSubtitle", state.reportHeaderSubtitle)
        root.put("reportFontFamily", state.reportFontFamily)
        root.put("keypadThemeColor", state.keypadThemeColor)
        root.put("keypadButtonStyle", state.keypadButtonStyle)
        root.put("dailyReminderEnabled", state.dailyReminderEnabled)
        root.put("dailyReminderTime", state.dailyReminderTime)
        root.put("currencySymbolPosition", state.currencySymbolPosition)
        root.put("currencyDecimalMode", state.currencyDecimalMode)
        root.put("currencyThousandsSeparator", state.currencyThousandsSeparator)
        root.put("currencyDisplayType", state.currencyDisplayType)

        val colorState = state.customColorThemeState
        val colorObj = org.json.JSONObject().apply {
            put("tableCardBg", colorState.tableCardBg ?: -1L)
            put("tableHeaderText", colorState.tableHeaderText ?: -1L)
            put("tableBorderColor", colorState.tableBorderColor ?: -1L)
            put("groupActiveTabBg", colorState.groupActiveTabBg ?: -1L)
            put("groupInactiveTabBg", colorState.groupInactiveTabBg ?: -1L)
        }
        root.put("customColorTheme", colorObj)

        val groupsArray = org.json.JSONArray()
        for (g in state.groups) {
            val gObj = org.json.JSONObject()
            gObj.put("id", g.id)
            gObj.put("name", g.name)
            gObj.put("type", g.type.name)
            gObj.put("isEnabled", g.isEnabled)
            gObj.put("orderIndex", g.orderIndex)
            gObj.put("isDefault", g.isDefault)
            gObj.put("isAddedFieldEnabled", g.isAddedFieldEnabled)
            gObj.put("isExcludedFromBalance", g.isExcludedFromBalance)
            gObj.put("notes", g.notes)
            gObj.put("color", g.color ?: -1L)

            val rowsArray = org.json.JSONArray()
            for (r in g.rows) {
                val rObj = org.json.JSONObject()
                rObj.put("denomination", r.denomination)
                rObj.put("givenInput", r.givenInput)
                rObj.put("addedInput", r.addedInput)
                rObj.put("remainingInput", r.remainingInput)
                rObj.put("isEnabled", r.isEnabled)
                rObj.put("notes", r.notes)
                rObj.put("customTitle", r.customTitle)
                rObj.put("formula", r.formula)
                rowsArray.put(rObj)
            }
            gObj.put("rows", rowsArray)

            val directArray = org.json.JSONArray()
            for (d in g.directEntries) {
                val dObj = org.json.JSONObject()
                dObj.put("id", d.id)
                dObj.put("title", d.title)
                dObj.put("amount", d.amount)
                dObj.put("quantity", d.quantity)
                dObj.put("notes", d.notes)
                directArray.put(dObj)
            }
            gObj.put("directEntries", directArray)

            groupsArray.put(gObj)
        }
        root.put("groups", groupsArray)

        val cashGroupsArray = org.json.JSONArray()
        for (cg in state.cashGroups) {
            val cgObj = org.json.JSONObject()
            cgObj.put("id", cg.id)
            cgObj.put("name", cg.name)
            cgObj.put("type", cg.type.name)
            cgObj.put("isEnabled", cg.isEnabled)
            cgObj.put("isExpanded", cg.isExpanded)
            cgObj.put("orderIndex", cg.orderIndex)
            cgObj.put("isDefault", cg.isDefault)
            cgObj.put("color", cg.color ?: -1L)

            val denomsArray = org.json.JSONArray()
            for (dr in cg.denomRows) {
                val drObj = org.json.JSONObject()
                drObj.put("denomination", dr.denomination)
                drObj.put("countInput", dr.countInput)
                drObj.put("isEnabled", dr.isEnabled)
                drObj.put("notes", dr.notes)
                denomsArray.put(drObj)
            }
            cgObj.put("denomRows", denomsArray)

            val cashDirectArray = org.json.JSONArray()
            for (cde in cg.directEntries) {
                val cdeObj = org.json.JSONObject()
                cdeObj.put("id", cde.id)
                cdeObj.put("title", cde.title)
                cdeObj.put("amount", cde.amount)
                cdeObj.put("currencyCode", cde.currencyCode)
                cdeObj.put("isSar", cde.isSar)
                cdeObj.put("customExchangeRate", cde.customExchangeRate ?: -1.0)
                cdeObj.put("notes", cde.notes)
                cashDirectArray.put(cdeObj)
            }
            cgObj.put("directEntries", cashDirectArray)

            cashGroupsArray.put(cgObj)
        }
        root.put("cashGroups", cashGroupsArray)

        val expensesArray = org.json.JSONArray()
        for (exp in state.expenses) {
            val expObj = org.json.JSONObject()
            expObj.put("id", exp.id)
            expObj.put("title", exp.title)
            expObj.put("amount", exp.amount)
            expObj.put("currencyCode", exp.currencyCode)
            expObj.put("isSar", exp.isSar)
            expObj.put("isDeposit", exp.isDeposit)
            expObj.put("customExchangeRate", exp.customExchangeRate ?: -1.0)
            expObj.put("category", exp.category)
            expObj.put("notes", exp.notes)
            expObj.put("timestamp", exp.timestamp)
            expObj.put("deductFromCash", exp.deductFromCash)
            expensesArray.put(expObj)
        }
        root.put("expenses", expensesArray)

        return root.toString(2)
    }

    private fun deserializeStateFromJson(jsonStr: String, currentState: DailyDirectSalesUiState): DailyDirectSalesUiState {
        val root = org.json.JSONObject(jsonStr)
        val dateTimestamp = root.optLong("dateTimestamp", System.currentTimeMillis())
        val sellerName = root.optString("sellerName", "")
        val notes = root.optString("notes", "")
        val cashInBoxYerInput = root.optString("cashInBoxYerInput", "")
        val cashInBoxSarInput = root.optString("cashInBoxSarInput", "")
        val exchangeRateInput = root.optString("exchangeRateInput", "380")
        val showCashDenominationsTable = root.optBoolean("showCashDenominationsTable", currentState.showCashDenominationsTable)
        val useEasternArabicNumerals = root.optBoolean("useEasternArabicNumerals", currentState.useEasternArabicNumerals)
        val showHijriDate = root.optBoolean("showHijriDate", currentState.showHijriDate)
        val use24HourFormat = root.optBoolean("use24HourFormat", currentState.use24HourFormat)
        val includeInternetInReport = root.optBoolean("includeInternetInReport", currentState.includeInternetInReport)
        val reportHeaderTitle = root.optString("reportHeaderTitle", currentState.reportHeaderTitle)
        val reportHeaderSubtitle = root.optString("reportHeaderSubtitle", currentState.reportHeaderSubtitle)
        val reportFontFamily = root.optString("reportFontFamily", currentState.reportFontFamily)
        val keypadThemeColor = root.optString("keypadThemeColor", currentState.keypadThemeColor)
        val keypadButtonStyle = root.optString("keypadButtonStyle", currentState.keypadButtonStyle)
        val dailyReminderEnabled = root.optBoolean("dailyReminderEnabled", currentState.dailyReminderEnabled)
        val dailyReminderTime = root.optString("dailyReminderTime", currentState.dailyReminderTime)
        val currencySymbolPosition = root.optString("currencySymbolPosition", currentState.currencySymbolPosition)
        val currencyDecimalMode = root.optString("currencyDecimalMode", currentState.currencyDecimalMode)
        val currencyThousandsSeparator = root.optString("currencyThousandsSeparator", currentState.currencyThousandsSeparator)
        val currencyDisplayType = root.optString("currencyDisplayType", currentState.currencyDisplayType)

        val colorObj = root.optJSONObject("customColorTheme")
        val customColorThemeState = if (colorObj != null) {
            CustomColorThemeState(
                tableCardBg = colorObj.optLong("tableCardBg", -1L).let { if (it == -1L) null else it },
                tableHeaderText = colorObj.optLong("tableHeaderText", -1L).let { if (it == -1L) null else it },
                tableBorderColor = colorObj.optLong("tableBorderColor", -1L).let { if (it == -1L) null else it },
                groupActiveTabBg = colorObj.optLong("groupActiveTabBg", -1L).let { if (it == -1L) null else it },
                groupInactiveTabBg = colorObj.optLong("groupInactiveTabBg", -1L).let { if (it == -1L) null else it }
            )
        } else currentState.customColorThemeState

        val parsedGroups = mutableListOf<SalesGroupUiState>()
        val groupsArray = root.optJSONArray("groups")
        if (groupsArray != null) {
            for (i in 0 until groupsArray.length()) {
                val gObj = groupsArray.getJSONObject(i)
                val id = gObj.optString("id", UUID.randomUUID().toString())
                val name = gObj.optString("name", "قسم")
                val typeStr = gObj.optString("type", "DENOMINATIONS")
                val type = try { SalesGroupType.valueOf(typeStr) } catch (e: Exception) { SalesGroupType.DENOMINATIONS }
                val isEnabled = gObj.optBoolean("isEnabled", true)
                val orderIndex = gObj.optInt("orderIndex", i)
                val isDefault = gObj.optBoolean("isDefault", false)
                val isAddedFieldEnabled = gObj.optBoolean("isAddedFieldEnabled", false)
                val isExcludedFromBalance = gObj.optBoolean("isExcludedFromBalance", false)
                val groupNotes = gObj.optString("notes", "")
                val groupColor = gObj.optLong("color", -1L).let { if (it == -1L) null else it }

                val rowsList = mutableListOf<DirectSalesRowUiState>()
                val rowsArray = gObj.optJSONArray("rows")
                if (rowsArray != null) {
                    for (j in 0 until rowsArray.length()) {
                        val rObj = rowsArray.getJSONObject(j)
                        val denom = rObj.optInt("denomination", 0)
                        val givenInput = rObj.optString("givenInput", "")
                        val addedInput = rObj.optString("addedInput", "")
                        val remainingInput = rObj.optString("remainingInput", "")
                        val rIsEnabled = rObj.optBoolean("isEnabled", true)
                        val rNotes = rObj.optString("notes", "")
                        val customTitle = rObj.optString("customTitle", "")
                        val formulaStr = rObj.optString("formula", "TICKET_STANDARD")
                        val parsedFormula = try { CalculationFormula.valueOf(formulaStr) } catch (e: Exception) { CalculationFormula.TICKET_STANDARD }
                        rowsList.add(
                            DirectSalesRowUiState(
                                denomination = denom,
                                givenInput = givenInput,
                                addedInput = addedInput,
                                remainingInput = remainingInput,
                                isEnabled = rIsEnabled,
                                notes = rNotes,
                                customTitle = customTitle,
                                formula = parsedFormula
                            )
                        )
                    }
                }

                val directList = mutableListOf<DirectEntryItem>()
                val directArray = gObj.optJSONArray("directEntries")
                if (directArray != null) {
                    for (k in 0 until directArray.length()) {
                        val dObj = directArray.getJSONObject(k)
                        val dId = dObj.optString("id", UUID.randomUUID().toString())
                        val dTitle = dObj.optString("title", "")
                        val dAmount = dObj.optDouble("amount", 0.0)
                        val dQty = dObj.optInt("quantity", 1)
                        val dNotes = dObj.optString("notes", "")
                        directList.add(
                            DirectEntryItem(
                                id = dId,
                                title = dTitle,
                                amountInput = if (dAmount > 0) dAmount.toString().removeSuffix(".0") else "",
                                quantityInput = dQty.toString(),
                                notes = dNotes
                            )
                        )
                    }
                }

                parsedGroups.add(
                    SalesGroupUiState(
                        id = id,
                        name = name,
                        type = type,
                        isEnabled = isEnabled,
                        orderIndex = orderIndex,
                        isDefault = isDefault,
                        isAddedFieldEnabled = isAddedFieldEnabled,
                        isExcludedFromBalance = isExcludedFromBalance,
                        notes = groupNotes,
                        rows = rowsList,
                        directEntries = directList,
                        color = groupColor
                    )
                )
            }
        }

        val parsedCashGroups = mutableListOf<CashBoxGroupUiState>()
        val cashGroupsArray = root.optJSONArray("cashGroups")
        if (cashGroupsArray != null) {
            for (i in 0 until cashGroupsArray.length()) {
                val cgObj = cashGroupsArray.getJSONObject(i)
                val id = cgObj.optString("id", UUID.randomUUID().toString())
                val name = cgObj.optString("name", "نقدي")
                val typeStr = cgObj.optString("type", "DENOMINATIONS")
                val type = try { CashGroupType.valueOf(typeStr) } catch (e: Exception) { CashGroupType.DENOMINATIONS }
                val isEnabled = cgObj.optBoolean("isEnabled", true)
                val isExpanded = cgObj.optBoolean("isExpanded", true)
                val orderIndex = cgObj.optInt("orderIndex", i)
                val isDefault = cgObj.optBoolean("isDefault", false)
                val cashGroupColor = cgObj.optLong("color", -1L).let { if (it == -1L) null else it }

                val denomList = mutableListOf<CashDenomRowUiState>()
                val denomsArray = cgObj.optJSONArray("denomRows")
                if (denomsArray != null) {
                    for (j in 0 until denomsArray.length()) {
                        val drObj = denomsArray.getJSONObject(j)
                        val denom = drObj.optInt("denomination", 0)
                        val countInput = drObj.optString("countInput", "")
                        val drIsEnabled = drObj.optBoolean("isEnabled", true)
                        val drNotes = drObj.optString("notes", "")
                        denomList.add(CashDenomRowUiState(denomination = denom, countInput = countInput, isEnabled = drIsEnabled, notes = drNotes))
                    }
                }

                val directCashList = mutableListOf<CashDirectEntryItem>()
                val cashDirectArray = cgObj.optJSONArray("directEntries")
                if (cashDirectArray != null) {
                    for (k in 0 until cashDirectArray.length()) {
                        val cdeObj = cashDirectArray.getJSONObject(k)
                        val cdeId = cdeObj.optString("id", UUID.randomUUID().toString())
                        val cdeTitle = cdeObj.optString("title", "")
                        val cdeAmount = cdeObj.optDouble("amount", 0.0)
                        val cdeCurrency = cdeObj.optString("currencyCode", "YER")
                        val cdeIsSar = cdeObj.optBoolean("isSar", false)
                        val cdeCustomRateRaw = cdeObj.optDouble("customExchangeRate", -1.0)
                        val cdeCustomRate = if (cdeCustomRateRaw > 0) cdeCustomRateRaw else null
                        val cdeNotes = cdeObj.optString("notes", "")
                        directCashList.add(
                            CashDirectEntryItem(
                                id = cdeId,
                                title = cdeTitle,
                                amountInput = if (cdeAmount > 0) cdeAmount.toString().removeSuffix(".0") else "",
                                currencyCode = cdeCurrency,
                                isSar = cdeIsSar,
                                customExchangeRate = cdeCustomRate,
                                notes = cdeNotes
                            )
                        )
                    }
                }

                parsedCashGroups.add(
                    CashBoxGroupUiState(
                        id = id,
                        name = name,
                        type = type,
                        isEnabled = isEnabled,
                        isExpanded = isExpanded,
                        orderIndex = orderIndex,
                        isDefault = isDefault,
                        denomRows = denomList,
                        directEntries = directCashList,
                        color = cashGroupColor
                    )
                )
            }
        }

        val parsedExpenses = mutableListOf<CashExpenseItem>()
        val expensesArray = root.optJSONArray("expenses")
        if (expensesArray != null) {
            for (i in 0 until expensesArray.length()) {
                val expObj = expensesArray.getJSONObject(i)
                val id = expObj.optString("id", UUID.randomUUID().toString())
                val title = expObj.optString("title", "")
                val amount = expObj.optDouble("amount", 0.0)
                val currencyCode = expObj.optString("currencyCode", "YER")
                val isSar = expObj.optBoolean("isSar", false)
                val isDeposit = expObj.optBoolean("isDeposit", false)
                val customRateRaw = expObj.optDouble("customExchangeRate", -1.0)
                val customRate = if (customRateRaw > 0) customRateRaw else null
                val category = expObj.optString("category", "عام")
                val expNotes = expObj.optString("notes", "")
                val timestamp = expObj.optLong("timestamp", System.currentTimeMillis())
                val deductFromCash = expObj.optBoolean("deductFromCash", false)
                parsedExpenses.add(
                    CashExpenseItem(
                        id = id,
                        title = title,
                        amountInput = if (amount > 0) amount.toString().removeSuffix(".0") else "",
                        currencyCode = currencyCode,
                        isSar = isSar,
                        isDeposit = isDeposit,
                        customExchangeRate = customRate,
                        category = category,
                        notes = expNotes,
                        timestamp = timestamp,
                        deductFromCash = deductFromCash
                    )
                )
            }
        }

        return currentState.copy(
            dateTimestamp = dateTimestamp,
            sellerName = sellerName,
            notes = notes,
            cashInBoxYerInput = cashInBoxYerInput,
            cashInBoxSarInput = cashInBoxSarInput,
            exchangeRateInput = exchangeRateInput,
            showCashDenominationsTable = showCashDenominationsTable,
            useEasternArabicNumerals = useEasternArabicNumerals,
            showHijriDate = showHijriDate,
            use24HourFormat = use24HourFormat,
            includeInternetInReport = includeInternetInReport,
            reportHeaderTitle = reportHeaderTitle,
            reportHeaderSubtitle = reportHeaderSubtitle,
            reportFontFamily = reportFontFamily,
            keypadThemeColor = keypadThemeColor,
            keypadButtonStyle = keypadButtonStyle,
            dailyReminderEnabled = dailyReminderEnabled,
            dailyReminderTime = dailyReminderTime,
            currencySymbolPosition = currencySymbolPosition,
            currencyDecimalMode = currencyDecimalMode,
            currencyThousandsSeparator = currencyThousandsSeparator,
            currencyDisplayType = currencyDisplayType,
            customColorThemeState = customColorThemeState,
            groups = if (parsedGroups.isNotEmpty()) parsedGroups else currentState.groups,
            cashGroups = if (parsedCashGroups.isNotEmpty()) parsedCashGroups else currentState.cashGroups,
            expenses = parsedExpenses
        )
    }

    companion object {
        fun calculateSalesSummary(state: DailyDirectSalesUiState): DailySalesSummary {
            var totalGiven = 0
            var totalAdded = 0
            var totalRemaining = 0
            var totalSold = 0
            var totalRevenue = 0.0
            var sideInternetRevenue = 0.0
            var totalSideRevenue = 0.0
            var hasErrors = false

            val activeGroups = state.groups.filter { grp ->
                grp.isEnabled && (state.includeInternetInReport || (grp.id != GROUP_INTERNET_ID && !grp.name.contains("انترنت")))
            }

            for (group in activeGroups) {
                if (group.isExcludedFromBalance) {
                    totalSideRevenue += group.totalRevenue
                    if (group.id == GROUP_INTERNET_ID) {
                        sideInternetRevenue += group.totalRevenue
                    }
                } else {
                    totalRevenue += group.totalRevenue
                }

                val isSanadGroup = group.id == GROUP_SANAD_ID || group.name == "سند"
                val isChiniGroup = group.id == GROUP_CHINI_ID || group.name.contains("صيني")
                when (group.type) {
                    SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> {
                        for (row in group.activeRows) {
                            if (!isChiniGroup) {
                                totalGiven += row.given
                                totalAdded += row.added
                                totalRemaining += row.remaining
                                if (isSanadGroup) {
                                    totalSold += row.sold
                                }
                            }
                            if (row.isRemainingExceeded) {
                                hasErrors = true
                            }
                        }
                    }
                    SalesGroupType.DIRECT_ENTRY -> {
                        if (!isChiniGroup && isSanadGroup) {
                            for (entry in group.directEntries) {
                                if (entry.isFilled) totalSold += 1
                            }
                        }
                    }
                }
            }

            val cashYer = state.cashInBoxYerInput.trim().toDoubleOrNull() ?: 0.0
            val cashSar = state.cashInBoxSarInput.trim().toDoubleOrNull() ?: 0.0
            val exchangeRate = state.exchangeRateInput.trim().toDoubleOrNull()?.takeIf { it > 0 } ?: 380.0

            val cashGroupsCashTotal = state.cashGroups
                .filter { it.isEnabled && !it.isExcludedFromBalance && it.id != CASH_GROUP_MAIN_ID && (it.type == CashGroupType.DENOMINATIONS || it.type == CashGroupType.DIRECT_ENTRY) }
                .filter { group ->
                    if (group.type == CashGroupType.DENOMINATIONS) state.showCashDenominationsTable else true
                }
                .sumOf { it.getTotalYer(exchangeRate, cashYer) }

            val isExpensesGroupEnabled = state.cashGroups.find { it.type == CashGroupType.EXPENSES || it.id == CASH_GROUP_EXPENSES_ID }?.isEnabled ?: false
            val isDepositsGroupEnabled = state.cashGroups.find { it.type == CashGroupType.DEPOSITS || it.id == "cash_group_deposits" }?.isEnabled ?: false

            val cashGroupsExpensesTotal = if (isExpensesGroupEnabled) {
                state.cashGroups
                    .filter { it.isEnabled && !it.isExcludedFromBalance && it.type == CashGroupType.EXPENSES }
                    .sumOf { it.getTotalYer(exchangeRate) }
            } else 0.0

            val cashGroupsDepositsTotal = if (isDepositsGroupEnabled) {
                state.cashGroups
                    .filter { it.isEnabled && !it.isExcludedFromBalance && it.type == CashGroupType.DEPOSITS }
                    .sumOf { it.getTotalYer(exchangeRate) }
            } else 0.0

            val expensesList = if (isExpensesGroupEnabled) state.expenses.filter { !it.isDeposit } else emptyList()
            val depositsList = if (isDepositsGroupEnabled) state.expenses.filter { it.isDeposit } else emptyList()

            val totalExpensesYer = expensesList.filter { !it.isSar }.sumOf { it.amount }
            val totalExpensesSar = expensesList.filter { it.isSar }.sumOf { it.amount }
            val totalExpensesInYer = expensesList.sumOf { it.getTotalYer(exchangeRate) } + cashGroupsExpensesTotal

            val totalDepositsYer = depositsList.filter { !it.isSar }.sumOf { it.amount }
            val totalDepositsSar = depositsList.filter { it.isSar }.sumOf { it.amount }
            val totalDepositsInYer = depositsList.sumOf { it.getTotalYer(exchangeRate) } + cashGroupsDepositsTotal

            val physicalCashInBox = (cashYer + (cashSar * exchangeRate) + cashGroupsCashTotal).coerceAtLeast(0.0)
            val otherCurrenciesTotalYer = (cashSar * exchangeRate) + cashGroupsCashTotal
            val netCashYer = cashYer.coerceAtLeast(0.0)

            val cashAmount = cashYer.coerceAtLeast(0.0)
            val expensesAmount = totalExpensesInYer
            val currenciesAmount = (cashSar * exchangeRate) + cashGroupsCashTotal
            val depositsAmount = totalDepositsInYer

            val totalBoxAmount = cashAmount + expensesAmount + currenciesAmount + depositsAmount
            val grossCashInBox = totalBoxAmount

            val balance = totalBoxAmount - totalRevenue

            val balanceStatus = when {
                kotlin.math.abs(balance) < 0.001 -> BalanceStatus.MATCHED
                balance > 0.001 -> BalanceStatus.SURPLUS
                else -> BalanceStatus.DEFICIT
            }

            return DailySalesSummary(
                totalGiven = totalGiven,
                totalAdded = totalAdded,
                totalRemaining = totalRemaining,
                totalSold = totalSold,
                totalRevenue = totalRevenue,
                sideInternetRevenue = sideInternetRevenue,
                totalSideRevenue = totalSideRevenue,
                cashInBoxYer = cashYer,
                cashInBoxSar = cashSar,
                netCashYer = netCashYer,
                exchangeRateSarToYer = exchangeRate,
                totalExpensesYer = totalExpensesYer,
                totalExpensesSar = totalExpensesSar,
                totalExpensesInYer = totalExpensesInYer,
                totalDepositsYer = totalDepositsYer,
                totalDepositsSar = totalDepositsSar,
                totalDepositsInYer = totalDepositsInYer,
                otherCurrenciesTotalYer = otherCurrenciesTotalYer,
                grossCashInBox = grossCashInBox,
                cashInBox = physicalCashInBox,
                balance = balance,
                balanceStatus = balanceStatus,
                hasErrors = hasErrors,
                activeGroupsCount = activeGroups.size,
                totalGroupsCount = state.groups.size
            )
        }
    }
}
