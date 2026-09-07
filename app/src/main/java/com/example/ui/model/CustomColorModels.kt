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
    val appBorderWidthDp: Int = 3,

    // 8. درجات الشفافية للعناصر (Transparency / Opacity Sliders)
    val tableCardAlpha: Float = 1.0f,
    val tableHeaderAlpha: Float = 1.0f,
    val tableCellAlpha: Float = 1.0f,
    val groupCardAlpha: Float = 1.0f
) {
    fun getColorOrNull(value: Long?): Color? = value?.let { 
        if (it == 0L) null else Color(it.toInt())
    }

    fun getEffectiveTableCardBg(defaultColor: Color): Color {
        val base = getColorOrNull(tableCardBg) ?: defaultColor
        return base.copy(alpha = (base.alpha * tableCardAlpha).coerceIn(0f, 1f))
    }

    fun getEffectiveTableHeaderBg(defaultColor: Color): Color {
        val base = getColorOrNull(tableHeaderBg) ?: defaultColor
        return base.copy(alpha = (base.alpha * tableHeaderAlpha).coerceIn(0f, 1f))
    }

    fun getEffectiveTableCellBg(defaultColor: Color): Color {
        val base = getColorOrNull(cellGivenBg) ?: defaultColor
        return base.copy(alpha = (base.alpha * tableCellAlpha).coerceIn(0f, 1f))
    }

    fun getEffectiveGroupCardBg(defaultColor: Color): Color {
        val base = getColorOrNull(groupSummaryCardBg) ?: defaultColor
        return base.copy(alpha = (base.alpha * groupCardAlpha).coerceIn(0f, 1f))
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
            name = "⚙️ سمة الفولاذ المصقول المعدني (الافتراضي)",
            description = "السمة النهارية الافتراضية: مظهر معدني فاخر بدرجات الفولاذ الفضي والستيل المضيء عالي الوضوح",
            primaryColor = Color(0xFF475569),
            secondaryColor = Color(0xFF334155),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "الافتراضي ⚙️",
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
            name = "🐅 سمة نادي الاتحاد السعودي (العميد والنمور)",
            description = "مظهر رياضي ملكي ناصع بألوان نادي الاتحاد السعودي الأصيلة: الأصفر الذهبي والأسود الفخم بتطريز كربوني",
            primaryColor = Color(0xFFEAB308),
            secondaryColor = Color(0xFF18181B),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "نادي الاتحاد 🐅",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF18181B,
                topAppBarText = 0xFFFEF08A,
                tableHeaderBg = 0xFFFEF9C3,
                tableHeaderText = 0xFF18181B,
                tableBorderColor = 0xFFEAB308,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFFEAB308,
                groupActiveTabText = 0xFF18181B,
                appBorderColor = 0xFFEAB308
            )
        ),
        ColorThemePreset(
            name = "🏛️ سمة حجر الجرانيت الصخري",
            description = "مظهر صخري جرانيتي طبيعي متين بدرجات الرمادي والصلابة الحجرية الأنيقة",
            primaryColor = Color(0xFF64748B),
            secondaryColor = Color(0xFF475569),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "جرانيت صخري 🏛️",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF475569,
                topAppBarText = 0xFFF8FAFC,
                tableHeaderBg = 0xFFE2E8F0,
                tableHeaderText = 0xFF1E293B,
                tableBorderColor = 0xFF94A3B8,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFF475569,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF94A3B8
            )
        ),
        ColorThemePreset(
            name = "👑 سمة الرخام الأبيض الملكي",
            description = "مظهر رخامي ناصع ونقي بتعريقات كلاسيكية فخمة مريحة جداً للبصر والعمل المكتبي",
            primaryColor = Color(0xFF0284C7),
            secondaryColor = Color(0xFF0F172A),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "رخام ملكي 👑",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF1E293B,
                topAppBarText = 0xFFFFFFFF,
                tableHeaderBg = 0xFFF1F5F9,
                tableHeaderText = 0xFF0F172A,
                tableBorderColor = 0xFFCBD5E1,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFF0284C7,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF94A3B8
            )
        ),
        ColorThemePreset(
            name = "🏛️ سمة الحجر والصخور الطبيعية",
            description = "مظهر صخري حجري طبيعي متين وأنيق بدرجات الرمادي الترابي المريح",
            primaryColor = Color(0xFF64748B),
            secondaryColor = Color(0xFF475569),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "حجر وصخور 🏛️",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF475569,
                topAppBarText = 0xFFF8FAFC,
                tableHeaderBg = 0xFFE2E8F0,
                tableHeaderText = 0xFF1E293B,
                tableBorderColor = 0xFF94A3B8,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFF475569,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF94A3B8
            )
        ),
        ColorThemePreset(
            name = "🧊 سمة البلورات الصخرية والجليد",
            description = "مظهر بلوري ناصع مستوحى من الصخور الجليدية النقية والشفافة",
            primaryColor = Color(0xFF0284C7),
            secondaryColor = Color(0xFF0369A1),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "جليد وبلورات 🧊",
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
            name = "🧵 سمة الجلد الطبيعي المخرم",
            description = "مظهر الجلد الملكي الفاخر بتخريمات ناعمة وتطريز هافان وأسود أنيق",
            primaryColor = Color(0xFF9A3412),
            secondaryColor = Color(0xFF7C2D12),
            isNightMode = false,
            isSeasonal = false,
            seasonBadge = "جلد مخرم 🧵",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF7C2D12,
                topAppBarText = 0xFFFEF3C7,
                tableHeaderBg = 0xFFFFEDD5,
                tableHeaderText = 0xFF7C2D12,
                tableBorderColor = 0xFFC2410C,
                tableCardBg = 0xFFFFFFFF,
                groupActiveTabBg = 0xFF9A3412,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFC2410C
            )
        ),
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
            name = "🛠️ سمة التيتانيوم الصلب الداكن (الافتراضي)",
            description = "السمة الليلية الافتراضية: سمة معدنية صلبة فاخرة بخلفية التيتانيوم المسبوك الداكن مريحة جداً للعين",
            primaryColor = Color(0xFF94A3B8),
            secondaryColor = Color(0xFF64748B),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "الافتراضي 🛠️",
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
            name = "🐅 سمة نادي الاتحاد السعودي (العميد والنمور)",
            description = "مظهر ليلي كربوني فخم بألوان نادي الاتحاد السعودي: أسود فاحم مع تطعيمات الأصفر والذهبي الملكي",
            primaryColor = Color(0xFFFACC15),
            secondaryColor = Color(0xFFEAB308),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "نادي الاتحاد 🐅",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF09090B,
                topAppBarText = 0xFFFEF08A,
                tableHeaderBg = 0xFF18181B,
                tableHeaderText = 0xFFFACC15,
                tableBorderColor = 0xFFEAB308,
                tableCardBg = 0xFF09090B,
                groupActiveTabBg = 0xFFCA8A04,
                groupActiveTabText = 0xFF18181B,
                appBorderColor = 0xFFEAB308
            )
        ),
        ColorThemePreset(
            name = "⚡ سمة البلازما المشعة الزرقاء",
            description = "مظهر كوانتومي ليلي فائق الجرأة مع خطوط بلازما ساطعة بدرجات الأزرق النيوني المشع",
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFF0284C7),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "بلازما مشعة ⚡",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF08273A,
                topAppBarText = 0xFFE0F2FE,
                tableHeaderBg = 0xFF031E2F,
                tableHeaderText = 0xFF38BDF8,
                tableBorderColor = 0xFF0284C7,
                tableCardBg = 0xFF02131E,
                groupActiveTabBg = 0xFF0284C7,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF38BDF8
            )
        ),
        ColorThemePreset(
            name = "🪵 سمة خشب الجوز الإيطالي الفاخر",
            description = "مظهر ليلي كلاسيكي دافئ مستوحى من أخشاب الجوز الفاخرة بدرجات الشوكولاتة والقهوة الملكية",
            primaryColor = Color(0xFFD97706),
            secondaryColor = Color(0xFFB45309),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "خشب جوز 🪵",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF271A0C,
                topAppBarText = 0xFFFEF3C7,
                tableHeaderBg = 0xFF1C1207,
                tableHeaderText = 0xFFFBBF24,
                tableBorderColor = 0xFF92400E,
                tableCardBg = 0xFF150D05,
                groupActiveTabBg = 0xFFB45309,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFD97706
            )
        ),
        ColorThemePreset(
            name = "☢️ سمة المواد المشعة والنيون",
            description = "مظهر ليلي داكن مع إضاءات نيون مشعة باللون الأخضر والفوسفوري الفسفوري الفاخر",
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF059669),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "مواد مشعة ☢️",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF064E3B,
                topAppBarText = 0xFFD1FAE5,
                tableHeaderBg = 0xFF022C22,
                tableHeaderText = 0xFF34D399,
                tableBorderColor = 0xFF10B981,
                tableCardBg = 0xFF031913,
                groupActiveTabBg = 0xFF059669,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF10B981
            )
        ),
        ColorThemePreset(
            name = "🌋 سمة الصهارة البركانية الداكنة",
            description = "مظهر صخري بركاني داكن مع توهجات الحمم الحمراء والبرتقالية المضيئة",
            primaryColor = Color(0xFFF97316),
            secondaryColor = Color(0xFFEA580C),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "صهارة بركانية 🌋",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF431407,
                topAppBarText = 0xFFFFEDD5,
                tableHeaderBg = 0xFF2C0B04,
                tableHeaderText = 0xFFFB923C,
                tableBorderColor = 0xFFF97316,
                tableCardBg = 0xFF1A0702,
                groupActiveTabBg = 0xFFEA580C,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFF97316
            )
        ),
        ColorThemePreset(
            name = "🏎️ سمة كاربون فايبر الرياضي",
            description = "مظهر ألياف الكربون الرياضية المنسوجة الداكنة بتباين أحمر والفحمي فائق الأناقة",
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
        ),
        ColorThemePreset(
            name = "👑 سمة الذهب والسبائك الملكية الليلية",
            description = "مظهر ليلي أسود كربوني عميق بتطعيمات الذهب الخالص والبرونز الملكي البراق",
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFD97706),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "ذهب ملكي 👑",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF140F04,
                topAppBarText = 0xFFFEF3C7,
                tableHeaderBg = 0xFF231B08,
                tableHeaderText = 0xFFFBBF24,
                tableBorderColor = 0xFFD97706,
                tableCardBg = 0xFF0D0A03,
                groupActiveTabBg = 0xFFB45309,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFF59E0B
            )
        ),
        ColorThemePreset(
            name = "🌌 سمة الفضاء السديمي والكون",
            description = "مظهر كوني داكن بدرجات البنفسجي السديمي والنيلي المتوهج بنجوم الفضاء",
            primaryColor = Color(0xFF8B5CF6),
            secondaryColor = Color(0xFF6D28D9),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "فضاء كوني 🌌",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF0F0B1E,
                topAppBarText = 0xFFEDE9FE,
                tableHeaderBg = 0xFF1A1235,
                tableHeaderText = 0xFFA78BFA,
                tableBorderColor = 0xFF7C3AED,
                tableCardBg = 0xFF0A0714,
                groupActiveTabBg = 0xFF6D28D9,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF8B5CF6
            )
        ),
        ColorThemePreset(
            name = "🌲 سمة الزمرد وغابات الليل",
            description = "مظهر طبيعي داكن مستوحى من أشجار الغابات ليلاً بتوهج الزمرد المريح للعين",
            primaryColor = Color(0xFF059669),
            secondaryColor = Color(0xFF047857),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "زمرد ليلي 🌲",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF04140D,
                topAppBarText = 0xFFD1FAE5,
                tableHeaderBg = 0xFF09291B,
                tableHeaderText = 0xFF10B981,
                tableBorderColor = 0xFF059669,
                tableCardBg = 0xFF020B07,
                groupActiveTabBg = 0xFF047857,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF10B981
            )
        ),
        ColorThemePreset(
            name = "🌊 سمة أعماق الهاوية البحرية",
            description = "مظهر ليلي فائق العمق مستوحى من خنادق المحيط بدرجات التيركواز والأزرق الغامق",
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF0891B2),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "أعماق البحر 🌊",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF031520,
                topAppBarText = 0xFFCFFAFE,
                tableHeaderBg = 0xFF08273A,
                tableHeaderText = 0xFF22D3EE,
                tableBorderColor = 0xFF0891B2,
                tableCardBg = 0xFF020E17,
                groupActiveTabBg = 0xFF0891B2,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF06B6D4
            )
        ),
        ColorThemePreset(
            name = "⚡ سمة الصاعقة الزرقاء الكهربائية",
            description = "مظهر كحلي داكن وفخم بإشعاع أزرق كهربائي فوسفوري عالي التباين",
            primaryColor = Color(0xFF3B82F6),
            secondaryColor = Color(0xFF2563EB),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "صاعقة زرقاء ⚡",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF091224,
                topAppBarText = 0xFFDBEAFE,
                tableHeaderBg = 0xFF122448,
                tableHeaderText = 0xFF60A5FA,
                tableBorderColor = 0xFF2563EB,
                tableCardBg = 0xFF050B17,
                groupActiveTabBg = 0xFF1D4ED8,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFF3B82F6
            )
        ),
        ColorThemePreset(
            name = "🌹 سمة الياقوت والمخمل الأسود",
            description = "مظهر مخملي أسود ملكي بلمسات الياقوت الأحمر القرمزي الداكن والفاخر",
            primaryColor = Color(0xFFE11D48),
            secondaryColor = Color(0xFFBE123C),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "ياقوت ملكي 🌹",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF1C060B,
                topAppBarText = 0xFFFFE4E6,
                tableHeaderBg = 0xFF300B13,
                tableHeaderText = 0xFFFB7185,
                tableBorderColor = 0xFFBE123C,
                tableCardBg = 0xFF120307,
                groupActiveTabBg = 0xFF9F1239,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFE11D48
            )
        ),
        ColorThemePreset(
            name = "☕ سمة القهوة الداكنة والموكا",
            description = "مظهر بني إسبريسو داكن ودافئ مع درجات الكراميل الذهبي المريح في العمل الليلي",
            primaryColor = Color(0xFFB45309),
            secondaryColor = Color(0xFF78350F),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "موكا داكنة ☕",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF160E08,
                topAppBarText = 0xFFFEF3C7,
                tableHeaderBg = 0xFF26180E,
                tableHeaderText = 0xFFF59E0B,
                tableBorderColor = 0xFF92400E,
                tableCardBg = 0xFF0D0805,
                groupActiveTabBg = 0xFF78350F,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFB45309
            )
        ),
        ColorThemePreset(
            name = "🔮 سمة السايبر بانك والنيون البنفسجي",
            description = "مظهر تقني داكن مستقبلي بتوهجات النيون البنفسجي والفوشيا المتألق",
            primaryColor = Color(0xFFD946EF),
            secondaryColor = Color(0xFFC026D3),
            isNightMode = true,
            isSeasonal = false,
            seasonBadge = "سايبر بانك 🔮",
            state = CustomColorThemeState(
                topAppBarBg = 0xFF18051E,
                topAppBarText = 0xFFFAE8FF,
                tableHeaderBg = 0xFF2C0938,
                tableHeaderText = 0xFFE879F9,
                tableBorderColor = 0xFFC026D3,
                tableCardBg = 0xFF0F0214,
                groupActiveTabBg = 0xFFA21CAF,
                groupActiveTabText = 0xFFFFFFFF,
                appBorderColor = 0xFFD946EF
            )
        )
    )
}
