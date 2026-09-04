package com.example.ui.model

import androidx.compose.ui.graphics.Color

/**
 * Comprehensive custom color state for all UI elements:
 * Cells (الخانات), Rows (الصفوف), Columns & Headers (الأعمدة ورؤوسهم),
 * Tables & Headers (الجدول ورأسه), Groups (), Sections & Headers (الأقسام ورؤوسهم).
 */
data class CustomColorThemeState(
    // 1. الخانات وحقول الإدخال (Cells & Inputs)
    val cellGivenBg: Long? = null,
    val cellGivenText: Long? = null,
    val cellAddedBg: Long? = null,
    val cellAddedText: Long? = null,
    val cellRemainingBg: Long? = null,
    val cellRemainingText: Long? = null,
    val cellSoldBg: Long? = null,
    val cellSoldText: Long? = null,
    val cellTotalBg: Long? = null,
    val cellTotalText: Long? = null,

    // 2. الصفوف (Rows)
    val rowEvenBg: Long? = null,
    val rowOddBg: Long? = null,
    val rowBorderColor: Long? = null,
    val rowErrorBg: Long? = null,

    // 3. الأعمدة ورؤوس الأعمدة (Columns & Column Headers)
    val colDenomHeaderBg: Long? = null,
    val colDenomHeaderText: Long? = null,
    val colGivenHeaderBg: Long? = null,
    val colGivenHeaderText: Long? = null,
    val colAddedHeaderBg: Long? = null,
    val colAddedHeaderText: Long? = null,
    val colRemainingHeaderBg: Long? = null,
    val colRemainingHeaderText: Long? = null,
    val colSoldHeaderBg: Long? = null,
    val colSoldHeaderText: Long? = null,

    // 4. الجدول ورأسه (Table Container & Table Header)
    val tableHeaderBg: Long? = null,
    val tableHeaderText: Long? = null,
    val tableCardBg: Long? = null,
    val tableBorderColor: Long? = null,

    // 5. الوالتبويبات (Groups & Tabs)
    val groupActiveTabBg: Long? = null,
    val groupActiveTabText: Long? = null,
    val groupInactiveTabBg: Long? = null,
    val groupInactiveTabText: Long? = null,
    val groupSummaryCardBg: Long? = null,
    val groupSummaryCardText: Long? = null,

    // 6. الأقسام والشريط العلوي (Sections & Headers)
    val topAppBarBg: Long? = null,
    val topAppBarText: Long? = null,
    val sectionSalesHeaderBg: Long? = null,
    val sectionSalesHeaderText: Long? = null,
    val sectionCashHeaderBg: Long? = null,
    val sectionCashHeaderText: Long? = null,
    val sectionReportsHeaderBg: Long? = null,
    val sectionReportsHeaderText: Long? = null,
    val sectionManagementHeaderBg: Long? = null,
    val sectionManagementHeaderText: Long? = null,

    // 7. إطار الحدود الخارجية للتطبيق (App Outer Border)
    val appBorderColor: Long? = null,
    val appBorderWidthDp: Int = 3
) {
    fun getColorOrNull(value: Long?): Color? = value?.let { 
        if (it == 0L) null else Color(it.toInt())
    }
}

/**
 * Category enum for Color Customization Studio in Settings
 */
enum class ColorCustomizationCategory(val title: String, val description: String) {
    CELLS("الخانات وحقول الإدخال", "تخصيص ألوان خلفيات ونصوص حقول المعطى، الإضافي، المتبقي، والإجمالي"),
    ROWS("الصفوف", "تخصيص ألوان الصفوف الزوجية والفردية وحالات التنبيه والخطأ"),
    COLUMNS("الأعمدة ورؤوسها", "تخصيص ألوان خلفيات ونصوص رؤوس أعمدة الفئات، المعطى، المتبقي، والمباع"),
    TABLES("الجداول ورؤوسها", "تخصيص إطار وخلفية بطاقة الجدول وشريط رأس الجدول"),
    GROUPS("الوالتبويبات", "تخصيص شريط ، التبويب النشط، وبطاقة ملخص "),
    SECTIONS("الأقسام والشريط العلوي", "تخصيص الشريط العلوي ورؤوس أقسام المبيعات والصندوق والتقرير")
}

/**
 * Preset Color Themes that configure all sub-elements harmoniously
 */
data class ColorThemePreset(
    val name: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val isNightMode: Boolean = false,
    val isSeasonal: Boolean = false,
    val seasonBadge: String = "",
    val state: CustomColorThemeState
)

object ColorPresetsRegistry {
    val presets = listOf(
        // ==================== ☀️ السمات النهارية ====================
        ColorThemePreset(
            name = "🏛️ سمة الجدار المحبب المعماري",
            description = "مظهر جداري إسمنتي محبب ناعم وعصري بطابع معماري مريح للبصر",
            primaryColor = Color(0xFF475569),
            secondaryColor = Color(0xFF334155),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "جدار محبب 🏛️",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF475569,
                topAppBarText = 0xFFF8FAFC,
                tableHeaderBg = 0xFFF1F5F9,
                tableHeaderText = 0xFF1E293B,
                tableBorderColor = 0xFF94A3B8,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFF475569,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF94A3B8
            )
        ),
        ColorThemePreset(
            name = "🪵 سمة الخشب الطبيعي الجميل",
            description = "خشب الجوز والأرو الفاخر بتدرجات البني الدافئ والنقوش الخشبية الراقية",
            primaryColor = Color(0xFF8D6E63),
            secondaryColor = Color(0xFF4E342E),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "خشب طبيعي 🪵",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF4E342E,
                topAppBarText = 0xFFFFECB3,
                tableHeaderBg = 0xFFEFEBE9,
                tableHeaderText = 0xFF3E2723,
                tableBorderColor = 0xFF8D6E63,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFF5D4037,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF8D6E63
            )
        ),
        ColorThemePreset(
            name = "⚙️ سمة الفولاذ المصقول المعدني",
            description = "سمة معدنية فاخرة بدرجات الفولاذ الفضي والستيل المضيء",
            primaryColor = Color(0xFF475569),
            secondaryColor = Color(0xFF334155),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "فولاذ معدني ⚙️",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF475569,
                topAppBarText = 0xFFFFFFFF,
                tableHeaderBg = 0xFFE2E8F0,
                tableHeaderText = 0xFF0F172A,
                tableBorderColor = 0xFF94A3B8,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFF475569,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF94A3B8
            )
        ),
        ColorThemePreset(
            name = "🪙 سمة الذهب المعدني البراق",
            description = "واجهة برّاقة بتدرجات الذهب الخالص والبرونز الفاخر",
            primaryColor = Color(0xFFB45309),
            secondaryColor = Color(0xFF92400E),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "ذهب معدني 🪙",
            state = CustomColorThemeState(
                topAppBarBg = 0xFFD4AF37,
                topAppBarText = 0xFFFFFFFF,
                tableHeaderBg = 0xFFFEF3C7,
                tableHeaderText = 0xFF78350F,
                tableBorderColor = 0xFFF59E0B,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFFD97706,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFF59E0B
            )
        ),
        ColorThemePreset(
            name = "⚙️ سمة الفضة المعدنية اللامعة",
            description = "طابع معدني ناصع وأنيق بتدرجات الفضة والكروم المصقول",
            primaryColor = Color(0xFF64748B),
            secondaryColor = Color(0xFF475569),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "فضة معدنية ⚙️",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF64748B,
                topAppBarText = 0xFFFFFFFF,
                tableHeaderBg = 0xFFF8FAFC,
                tableHeaderText = 0xFF1E293B,
                tableBorderColor = 0xFFCBD5E1,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFF64748B,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFCBD5E1
            )
        ),
        ColorThemePreset(
            name = "🧱 سمة البرونز والنحاس المعشق",
            description = "مظهر معدني نحاسي فاخر بالدرجات الأنيقة والمعدنية الدافئة",
            primaryColor = Color(0xFFC2410C),
            secondaryColor = Color(0xFF9A3412),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "نحاس برونزي 🧱",
            state = CustomColorThemeState(
                topAppBarBg = 0xFFC2410C,
                topAppBarText = 0xFFFFFFFF,
                tableHeaderBg = 0xFFFFEDD5,
                tableHeaderText = 0xFF7C2D12,
                tableBorderColor = 0xFFFB923C,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFFEA580C,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFFB923C
            )
        ),
        ColorThemePreset(
            name = "✨ سمة الكروم المعدني النهاري",
            description = "مظهر كرومي ناصع مضيء ومشرق يلائم بيئات العمل النهارية الحيوية",
            primaryColor = Color(0xFF0EA5E9),
            secondaryColor = Color(0xFF0284C7),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "كروم نهاري ✨",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF0284C7,
                topAppBarText = 0xFFFFFFFF,
                tableHeaderBg = 0xFFE0F2FE,
                tableHeaderText = 0xFF0369A1,
                tableBorderColor = 0xFF38BDF8,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFF0284C7,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF38BDF8
            )
        ),
        ColorThemePreset(
            name = "🛠️ سمة الألومنيوم المصقول النهاري",
            description = "مظهر ألومنيوم صناعي راقي ومتوازن مع تفاصيل رمادية فاتحة ونظيفة",
            primaryColor = Color(0xFF71717A),
            secondaryColor = Color(0xFF52525B),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "ألومنيوم 🛠️",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF52525B,
                topAppBarText = 0xFFFFFFFF,
                tableHeaderBg = 0xFFF4F4F5,
                tableHeaderText = 0xFF27272A,
                tableBorderColor = 0xFFA1A1AA,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFF71717A,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFA1A1AA
            )
        ),

        // ==================== 🌙 السمات الليلية ====================
        ColorThemePreset(
            name = "🏎️ سمة كاربون فايبر الرياضي",
            description = "مظهر ألياف الكربون الرياضية المنسوجة الداكنة بتباين أحمر وفحمي فائق الأناقة",
            primaryColor = Color(0xFFEF4444),
            secondaryColor = Color(0xFF1E293B),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "كاربون 🏎️",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF18181B,
                topAppBarText = 0xFFF4F4F5,
                tableHeaderBg = 0xFF27272A,
                tableHeaderText = 0xFFEF4444,
                tableBorderColor = 0xFFDC2626,
                tableCardBg = 0xFF18181B,
                groupActiveTabBg = 0xFFDC2626,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFEF4444
            )
        ),
        ColorThemePreset(
            name = "💺 سمة الجلد المخرم الفاخر",
            description = "مظهر الجلد الملكي الفاخر بتخريمات ناعمة وتطريز هافان وأسود أنيق",
            primaryColor = Color(0xFFD97706),
            secondaryColor = Color(0xFF78350F),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "جلد فاخر 💺",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF1C1917,
                topAppBarText = 0xFFFDE68A,
                tableHeaderBg = 0xFF292524,
                tableHeaderText = 0xFFF59E0B,
                tableBorderColor = 0xFFB45309,
                tableCardBg = 0xFF1C1917,
                groupActiveTabBg = 0xFFB45309,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFD97706
            )
        ),
        ColorThemePreset(
            name = "🛠️ سمة التيتانيوم الصلب الداكن",
            description = "سمة معدنية ليلية صلبة بخلفية التيتانيوم المسبوك الفاخر",
            primaryColor = Color(0xFF94A3B8),
            secondaryColor = Color(0xFF64748B),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "تيتانيوم 🛠️",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF0F172A,
                topAppBarText = 0xFFF8FAFC,
                tableHeaderBg = 0xFF1E293B,
                tableHeaderText = 0xFFF1F5F9,
                tableBorderColor = 0xFF334155,
                tableCardBg = 0xFF0F172A,
                groupActiveTabBg = 0xFF334155,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF475569
            )
        ),
        ColorThemePreset(
            name = "💠 سمة البلاتين الليلي اللامع",
            description = "مظهر بلاتيني معدني ليلي داكن بلمسات مضيئة وأنيقة",
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFF0369A1),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "بلاتين ليلي 💠",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF0B0F19,
                topAppBarText = 0xFFE0F2FE,
                tableHeaderBg = 0xFF1E293B,
                tableHeaderText = 0xFF38BDF8,
                tableBorderColor = 0xFF0284C7,
                tableCardBg = 0xFF0B0F19,
                groupActiveTabBg = 0xFF0284C7,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF38BDF8
            )
        ),
        ColorThemePreset(
            name = "🖤 سمة الحديد الأسود المطلي",
            description = "مظهر معدني صناعي أسود قاتم مع تطعيمات رمادية وفولاذية تريح العين ليلاً",
            primaryColor = Color(0xFFA1A1AA),
            secondaryColor = Color(0xFF27272A),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "حديد أسود 🖤",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF09090B,
                topAppBarText = 0xFFFAFAFA,
                tableHeaderBg = 0xFF18181B,
                tableHeaderText = 0xFFA1A1AA,
                tableBorderColor = 0xFF3F3F46,
                tableCardBg = 0xFF09090B,
                groupActiveTabBg = 0xFF27272A,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF52525B
            )
        )
    )
}
