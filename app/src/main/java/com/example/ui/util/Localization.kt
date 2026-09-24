package com.example.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppLanguage = staticCompositionLocalOf { "ar" }

object AppStrings {
    fun get(key: String, lang: String = "ar"): String {
        return when (key) {
            // Navigation
            "nav_home" -> "الرئيسة"
            "nav_direct_sales" -> "مبيعات"
            "nav_cash_box" -> "الصندوق"
            "nav_reports" -> "البيان المالي"
            "nav_management" -> "التحكم"
            "nav_modes" -> "التحكم"
            "nav_settings" -> "الإعدادات"
            "nav_calculator" -> "الحاسبة"
            "organize_section" -> "تنظيم"
            "reset_all_groups" -> "تصفير المجموعات والفئات"

            // Titles
            "app_title" -> "WLF Cash"
            "financial_statement" -> "البيان المالي"
            "reports_title" -> "البيان المالي"
            "reports_subtitle" -> "كشف ومطابقة المبيعات اليومية والصندوق"

            // TopBar & Actions
            "dark_mode" -> "الوضع الليلي"
            "light_mode" -> "الوضع النهاري"
            "lock_given_extra" -> "منع تعديل المعطى والإضافي"
            "read_only_mode" -> "وضع القراءة"
            "close_day" -> "إغلاق اليوم"
            "day_closed" -> "اليوم مغلق"
            "unlock_day" -> "فك إغلاق اليوم"

            // Common Terms
            "total_sales" -> "إجمالي المبيعات"
            "total_cash" -> "إجمالي الصندوق"
            "total_expenses" -> "إجمالي المصروفات"
            "total_deposits" -> "إجمالي الإيداعات"
            "net_difference" -> "صافي الفارق"
            "difference" -> "الفارق"
            "given" -> "المعطى"
            "added" -> "الإضافي"
            "remaining" -> "المتبقي"
            "sold" -> "المباع"
            "price" -> "السعر"
            "total" -> "الإجمالي"
            "denom" -> "الفئة"
            "notes" -> "ملاحظات"
            "yer" -> "ر.ي."
            "sar" -> "ر.س."
            "usd" -> "USD"
            "save" -> "حفظ"
            "apply" -> "تطبيق"
            "clear" -> "مسح"
            "delete" -> "حذف"
            "edit" -> "تعديل"
            "add" -> "إضافة"
            "close" -> "إغلاق"
            "confirm" -> "تأكيد"
            "export" -> "تصدير"
            "share" -> "مشاركة"
            "print" -> "طباعة"
            "pdf" -> "تقرير PDF"
            "excel" -> "ملف Excel"
            "zero_all_remaining" -> "تصفير الكل"
            "active_now" -> "نشط حالياً"
            "currency" -> "العملة"
            "exchange_rate" -> "سعر الصرف"
            "search" -> "بحث"
            "filter" -> "تصفية"
            "all" -> "الكل"

            // Reports & Summaries
            "financial_summary" -> "الملخص المالي العام"
            "sales_breakdown" -> "تفاصيل المبيعات"
            "cash_breakdown" -> "تفاصيل الصندوق"
            "matching_status" -> "حالة المطابقة"
            "surplus" -> "فائض (+)"
            "deficit" -> "عجز (-)"
            "balanced" -> "مطابق تماماً"

            // Settings options
            "full_screen_mode" -> "وضع ملء الشاشة"
            "full_screen_desc" -> "إخفاء شريط الحالة وأشرطة النظام لتوفير مساحة عرض قصوى"
            "auto_scroll_mode" -> "تمرير الشاشة التلقائي عند تحديد الحقول"
            "auto_scroll_desc" -> "تحريك الشاشة تلقائياً لإظهار الحقل المحدد فوق لوحة الأرقام عند الإدخال في التبويبات والمطويات"
            "compact_mode" -> "الوضع المضغوط (عرض عالي الكثافة)"
            "compact_mode_desc" -> "تقليل الهوامش والمساحات لعرض أكبر قدر من البيانات والبنود دفعة واحدة"
            "scroll_to_top" -> "زر التمرير للأعلى"
            "scroll_to_top_desc" -> "إظهار زر طائر سريع للتمرير لأعلى الصفحة عند التصفح لأسفل"
            "reset_all" -> "تصفير مدخلات اليوم بالكامل"
            "reset_all_confirm_title" -> "تنبيه تأكيد تصفير كافة البيانات"
            "reset_all_confirm_msg" -> "تحذير: هل أنت متأكد من تصفير كافة مبيعات اليوم، حركات الصندوق، والمصروفات؟ هذا الإجراء سيمسح مدخلات الوردية الحالية ولا يمكن التراجع عنه."
            "confirm_yes" -> "نعم، تصفير الآن"
            "cancel" -> "إلغاء"
            "merge_items" -> "دمج البنود"
            "merge_expenses_deposits" -> "دمج المصروفات والإيداعات"
            "accordion_mode" -> "وضع المطوية"
            "tabs_mode" -> "وضع التبويبات"

            else -> key
        }
    }
}

@Composable
@ReadOnlyComposable
fun stringResourceByKey(key: String): String {
    val lang = LocalAppLanguage.current
    return AppStrings.get(key, lang)
}
