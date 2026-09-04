package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.ui.model.AccountingFormatter
import com.example.ui.model.BalanceStatus
import com.example.ui.model.DailySalesSummary
import com.example.ui.model.DirectEntryItem
import com.example.ui.model.DirectSalesRowUiState
import com.example.ui.model.SalesGroupType
import com.example.ui.model.SalesGroupUiState
import com.example.ui.viewmodel.DailyDirectSalesUiState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

object ReportExporter {

    enum class PdfTheme {
        COLORED,
        BLACK_AND_WHITE
    }

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getShareUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun getFormattedFileName(prefix: String, ext: String): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
        return "${prefix}_${sdf.format(Date())}.$ext"
    }

    // ==========================================
    // Save to Downloads helper
    // ==========================================
    fun saveToDownloadsFolder(context: Context, sourceFile: File, fileName: String): Boolean {
        return try {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val destFile = File(downloadsDir, fileName)
            sourceFile.copyTo(destFile, overwrite = true)
            Toast.makeText(context, "تم حفظ التقرير بنجاح في مجلد التنزيلات (Downloads):\n$fileName", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            try {
                val altDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(altDir, fileName)
                sourceFile.copyTo(destFile, overwrite = true)
                Toast.makeText(context, "تم حفظ التقرير في مجلد التطبيق:\n$fileName", Toast.LENGTH_LONG).show()
                true
            } catch (ex: Exception) {
                Toast.makeText(context, "فشل حفظ الملف: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    // ==========================================
    // 1. Text Sharing & Text File Export
    // ==========================================

    fun generateTextReport(state: DailyDirectSalesUiState, summary: DailySalesSummary): String {
        val sb = StringBuilder()
        sb.append("📋 *${state.reportHeaderTitle}*\n")
        sb.append("📅 التاريخ: ${AccountingFormatter.formatDisplayDate(state.dateTimestamp, state.showHijriDate, state.useEasternArabicNumerals)}\n")
        if (state.sellerName.isNotBlank()) {
            sb.append("👤 البائع: ${state.sellerName}\n")
        }
        sb.append("───────────────────────────────\n")

        val activeGroups = state.groups.filter { grp ->
            grp.isEnabled && (state.includeInternetInReport || (grp.id != "group_internet" && !grp.name.contains("انترنت")))
        }

        for (group in activeGroups) {
            val groupNameClean = group.name.replace("مجموعة", "").replace("(نشط)", "").trim()
            sb.append("\n🔹 *$groupNameClean*\n")
            if (state.isReportDetailedMode) {
                if (group.type == SalesGroupType.DENOMINATIONS) {
                    sb.append("الفئة | المعطى | إضافة | متبقي | مباع | الإجمالي\n")
                    for (row in group.rows) {
                        if (row.given > 0 || row.added > 0 || row.remaining > 0 || row.sold > 0 || row.notes.isNotBlank()) {
                            val notePart = if (row.notes.isNotBlank()) " [${row.notes}]" else ""
                            sb.append("${row.denomination} | ${row.given} | ${row.added} | ${row.remaining} | ${row.sold} | ${AccountingFormatter.formatYer(row.total)}$notePart\n")
                        }
                    }
                } else {
                    val isChini = group.id == "group_chini" || group.name.contains("صيني")
                    for (entry in group.directEntries) {
                        if (entry.isFilled) {
                            val titleStr = if (entry.title.isBlank()) (if (isChini) "مبيعات صيني" else "بند") else entry.title
                            val qtyPart = if (isChini || entry.quantity <= 1) "" else " × ${entry.quantity}"
                            val notePart = if (entry.notes.isNotBlank()) " [${entry.notes}]" else ""
                            if (isChini) {
                                if (entry.title.isBlank()) {
                                    sb.append("- ${AccountingFormatter.formatYer(entry.amount)}$notePart\n")
                                } else {
                                    sb.append("- $titleStr: ${AccountingFormatter.formatYer(entry.amount)}$notePart\n")
                                }
                            } else {
                                sb.append("- $titleStr: ${AccountingFormatter.formatYer(entry.amount)}$qtyPart = ${AccountingFormatter.formatYer(entry.total)}$notePart\n")
                            }
                        }
                    }
                }
            } else {
                // Aggregated summary mode (افتراضي تجميعي)
                if (group.type == SalesGroupType.DENOMINATIONS) {
                    val activeRows = group.rows.filter { it.sold > 0 }
                    val summaryStr = if (activeRows.isEmpty()) "لا توجد حركات مبيعات" else activeRows.joinToString(", ") { "فئة ${it.denomination}: ${it.sold} تذكرة" }
                    sb.append("ملخص الفئات المباعة: $summaryStr\n")
                } else {
                    val filledEntries = group.directEntries.filter { it.isFilled }
                    val summaryStr = if (filledEntries.isEmpty()) "لا توجد بنود مضافة" else filledEntries.joinToString(", ") { "${it.title.ifBlank { "بند" }}: ${AccountingFormatter.formatYer(it.total)}" }
                    sb.append("ملخص البنود: $summaryStr\n")
                }
            }
            sb.append("💰 إجمالي $groupNameClean : ${AccountingFormatter.formatYer(group.totalRevenue)}\n")
        }

        if (state.includeOtherCurrenciesInReport && state.customCurrencies.size > 1) {
            sb.append("\n💵 *تفاصيل العملات الأخرى بالحسابات:*\n")
            state.customCurrencies.filter { !it.isMain }.forEach { curr ->
                sb.append("• ${curr.flag} ${curr.arabicName} (${curr.symbol}): سعر الصرف المعين ${curr.defaultRateYer} ر.ي.\n")
            }
        }

        sb.append("\n───────────────────────────────\n")
        sb.append("📊 *الملخص المالي العام:*\n")
        sb.append("📥 إجمالي التذاكر/العناصر المباعة: ${AccountingFormatter.formatNumber(summary.totalSold)}\n")
        sb.append("💰 *مجموع المبيعات الإجمالي:* ${AccountingFormatter.formatYer(summary.totalRevenue)}\n")

        if (summary.cashInBoxSar > 0) {
            sb.append("💵 النقد بالريال اليمني: ${AccountingFormatter.formatYer(summary.cashInBoxYer)}\n")
            sb.append("🇸🇦 النقد بالريال السعودي: ${AccountingFormatter.formatSar(summary.cashInBoxSar)} (صرف ${summary.exchangeRateSarToYer} = ${AccountingFormatter.formatYer(summary.cashInBoxSarInYer)})\n")
        }
        sb.append("📥 *إجمالي الصندوق الفعلي:* ${AccountingFormatter.formatYer(summary.cashInBox)}\n")

        val statusText = when (summary.balanceStatus) {
            BalanceStatus.MATCHED -> "متطابق تماماً (لا يوجد عجز أو فائض)"
            BalanceStatus.DEFICIT -> "عجز في الصندوق بمقدار ${AccountingFormatter.formatYer(summary.balance)}"
            BalanceStatus.SURPLUS -> "فائض في الصندوق بمقدار ${AccountingFormatter.formatYer(abs(summary.balance))}"
        }
        sb.append("⚖️ *الموازنة (المبيعات - الصندوق):* ${AccountingFormatter.formatYer(summary.balance)}\n")
        sb.append("🏷️ *حالة الموازنة:* $statusText\n")

        if (state.notes.isNotBlank()) {
            sb.append("\n📝 ملاحظات: ${state.notes}\n")
        }
        sb.append("\nتم استخراج التقرير عبر تطبيق محاسبة مبيعات التذاكر والصندوق | Design By : HAS (+967780776191)")
        return sb.toString()
    }

    fun shareAsText(context: Context, state: DailyDirectSalesUiState, summary: DailySalesSummary) {
        val text = generateTextReport(state, summary)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "تقرير مبيعات التذاكر - ${AccountingFormatter.formatDayDate(state.dateTimestamp)}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة تقرير المبيعات كنص"))
    }

    fun exportAsTextFile(
        context: Context,
        state: DailyDirectSalesUiState,
        summary: DailySalesSummary,
        saveDirectly: Boolean = false
    ) {
        try {
            val text = generateTextReport(state, summary)
            val file = File(getReportsDir(context), getFormattedFileName("SalesReport", "txt"))
            file.writeText(text, Charsets.UTF_8)

            if (saveDirectly) {
                saveToDownloadsFolder(context, file, file.name)
            } else {
                val uri = getShareUri(context, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "تقرير مبيعات التذاكر - ${AccountingFormatter.formatDisplayDate(state.dateTimestamp, state.showHijriDate, state.useEasternArabicNumerals)}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "مشاركة أو حفظ ملف النص (TXT)"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "فشل تصدير ملف النص: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // 2. CSV / Excel Export
    // ==========================================

    fun exportAsCsv(
        context: Context,
        state: DailyDirectSalesUiState,
        summary: DailySalesSummary,
        saveDirectly: Boolean = false
    ) {
        try {
            val file = File(getReportsDir(context), getFormattedFileName("SalesReport_CSV", "csv"))
            val fos = FileOutputStream(file)
            
            // Write UTF-8 BOM so Excel opens Arabic correctly
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            
            val sb = StringBuilder()
            sb.append("${state.reportHeaderTitle}\n")
            sb.append("التاريخ,${AccountingFormatter.formatDisplayDate(state.dateTimestamp, state.showHijriDate, state.useEasternArabicNumerals)}\n")
            if (state.sellerName.isNotBlank()) {
                sb.append("اسم البائع,${escapeCsv(state.sellerName)}\n")
            }
            sb.append("\n")

            val activeGroups = state.groups.filter { grp ->
                grp.isEnabled && (state.includeInternetInReport || (grp.id != "group_internet" && !grp.name.contains("انترنت")))
            }

            // Groups details
            for (group in activeGroups) {
                sb.append(",${escapeCsv(group.name)},النوع,${if (group.type == SalesGroupType.DENOMINATIONS) "فئات سند" else "مبيعات مباشرة"}\n")
                
                if (group.type == SalesGroupType.DENOMINATIONS) {
                    sb.append("الفئة (ر.ي),المعطى,الإضافي,المتبقي,المباع,إجمالي المبيعات (ر.ي),الملاحظات\n")
                    for (row in group.rows) {
                        sb.append("${row.denomination},${row.given},${row.added},${row.remaining},${row.sold},${row.total},${escapeCsv(row.notes)}\n")
                    }
                } else {
                    val isChini = group.id == "group_chini" || group.name.contains("صيني")
                    if (isChini) {
                        sb.append("البند,المبلغ (ر.ي),الملاحظات\n")
                        for (entry in group.directEntries) {
                            if (entry.isFilled) {
                                val titleStr = if (entry.title.isBlank()) "مبيعات صيني" else entry.title
                                sb.append("${escapeCsv(titleStr)},${entry.amount},${escapeCsv(entry.notes)}\n")
                            }
                        }
                    } else {
                        sb.append("البند,المبلغ الفردي (ر.ي),الكمية,الإجمالي (ر.ي),الملاحظات\n")
                        for (entry in group.directEntries) {
                            if (entry.isFilled) {
                                val titleStr = if (entry.title.isBlank()) "بند" else entry.title
                                sb.append("${escapeCsv(titleStr)},${entry.amount},${entry.quantity},${entry.total},${escapeCsv(entry.notes)}\n")
                            }
                        }
                    }
                }
                sb.append("إجمالي ,${group.totalRevenue}\n\n")
            }

            // Summary Section
            sb.append("الملخص المالي العام\n")
            sb.append("إجمالي التذاكر/العناصر المباعة,${summary.totalSold}\n")
            sb.append("مجموع المبيعات الإجمالي (ر.ي),${summary.totalRevenue}\n")
            sb.append("النقد بالريال اليمني,${summary.cashInBoxYer}\n")
            sb.append("النقد بالريال السعودي,${summary.cashInBoxSar}\n")
            sb.append("سعر صرف السعودي,${summary.exchangeRateSarToYer}\n")
            sb.append("معادل السعودي باليمني,${summary.cashInBoxSarInYer}\n")
            sb.append("إجمالي الصندوق الفعلي (ر.ي),${summary.cashInBox}\n")
            sb.append("الموازنة (المبيعات - الصندوق),${summary.balance}\n")
            
            val statusText = when (summary.balanceStatus) {
                BalanceStatus.MATCHED -> "متطابق تماماً"
                BalanceStatus.DEFICIT -> "عجز في الصندوق"
                BalanceStatus.SURPLUS -> "فائض في الصندوق"
            }
            sb.append("حالة الموازنة,$statusText\n")
            if (state.notes.isNotBlank()) {
                sb.append("ملاحظات عامة,${escapeCsv(state.notes)}\n")
            }

            fos.write(sb.toString().toByteArray(Charsets.UTF_8))
            fos.flush()
            fos.close()

            if (saveDirectly) {
                saveToDownloadsFolder(context, file, file.name)
            } else {
                val uri = getShareUri(context, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "كشف مبيعات CSV - ${AccountingFormatter.formatDisplayDate(state.dateTimestamp, state.showHijriDate, state.useEasternArabicNumerals)}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "مشاركة أو فتح ملف CSV / Excel"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "فشل تصدير ملف CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }

    // ==========================================
    // 3. PDF Document Export (Colored / B&W)
    // ==========================================

    fun exportAsPdf(
        context: Context,
        state: DailyDirectSalesUiState,
        summary: DailySalesSummary,
        theme: PdfTheme,
        saveDirectly: Boolean = false
    ) {
        try {
            val pageWidth = 595 // A4 standard width (points)
            val pageHeight = 842 // A4 standard height (points)
            val pdfDocument = PdfDocument()

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            renderReportToCanvas(
                canvas = canvas,
                width = pageWidth.toFloat(),
                height = pageHeight.toFloat(),
                state = state,
                summary = summary,
                theme = theme,
                scale = 1.0f
            )

            pdfDocument.finishPage(page)

            val filePrefix = if (theme == PdfTheme.COLORED) "SalesReport_Color" else "SalesReport_BW"
            val file = File(getReportsDir(context), getFormattedFileName(filePrefix, "pdf"))
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()
            pdfDocument.close()

            if (saveDirectly) {
                saveToDownloadsFolder(context, file, file.name)
            } else {
                val uri = getShareUri(context, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "تقرير مبيعات PDF - ${AccountingFormatter.formatDisplayDate(state.dateTimestamp, state.showHijriDate, state.useEasternArabicNumerals)}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "مشاركة أو طباعة تقرير PDF"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "فشل تصدير ملف PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // 4. Image Export (PNG / JPG)
    // ==========================================

    fun exportAsImage(
        context: Context,
        state: DailyDirectSalesUiState,
        summary: DailySalesSummary,
        isPng: Boolean = true,
        saveDirectly: Boolean = false
    ) {
        try {
            // High-resolution image bitmap (e.g. 1080x1560 px)
            val imgWidth = 1080
            val imgHeight = 1560
            val bitmap = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            renderReportToCanvas(
                canvas = canvas,
                width = imgWidth.toFloat(),
                height = imgHeight.toFloat(),
                state = state,
                summary = summary,
                theme = PdfTheme.COLORED,
                scale = imgWidth / 595f // scale up to high res
            )

            val ext = if (isPng) "png" else "jpg"
            val mime = if (isPng) "image/png" else "image/jpeg"
            val file = File(getReportsDir(context), getFormattedFileName("SalesReport_Image", ext))
            val fos = FileOutputStream(file)
            val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            bitmap.compress(compressFormat, 95, fos)
            fos.flush()
            fos.close()

            if (saveDirectly) {
                saveToDownloadsFolder(context, file, file.name)
            } else {
                val uri = getShareUri(context, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "صورة كشف المبيعات - ${AccountingFormatter.formatDisplayDate(state.dateTimestamp, state.showHijriDate, state.useEasternArabicNumerals)}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "مشاركة صورة كشف المبيعات"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "فشل تصدير الصورة: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // Unified Canvas Renderer for PDF and Image
    // ==========================================

    private fun renderReportToCanvas(
        canvas: Canvas,
        width: Float,
        height: Float,
        state: DailyDirectSalesUiState,
        summary: DailySalesSummary,
        theme: PdfTheme,
        scale: Float
    ) {
        // Theme Colors
        val isColor = theme == PdfTheme.COLORED

        val bgColor = if (isColor) Color.rgb(250, 252, 251) else Color.WHITE
        val customPrimary = if (state.reportPrimaryColor != 0L) state.reportPrimaryColor.toInt() else Color.rgb(0, 105, 92)
        val primaryColor = if (isColor) customPrimary else Color.BLACK
        val primaryLightColor = if (isColor) Color.rgb(224, 242, 241) else Color.rgb(240, 240, 240)
        val textPrimaryColor = Color.rgb(17, 24, 39)
        val textSecondaryColor = if (isColor) Color.rgb(75, 85, 99) else Color.rgb(100, 100, 100)
        val borderColor = if (isColor) Color.rgb(209, 213, 219) else Color.rgb(180, 180, 180)
        val cardBg = if (isColor) Color.rgb(255, 255, 255) else Color.WHITE
        val highlightBg = if (isColor) Color.rgb(243, 244, 246) else Color.rgb(245, 245, 245)

        // Status Colors
        val statusBg = if (!isColor) {
            Color.rgb(240, 240, 240)
        } else {
            when (summary.balanceStatus) {
                BalanceStatus.MATCHED -> Color.rgb(220, 252, 231) // Light Green
                BalanceStatus.DEFICIT -> Color.rgb(254, 226, 226) // Light Red
                BalanceStatus.SURPLUS -> Color.rgb(254, 243, 199) // Light Amber
            }
        }
        val statusTextColor = if (!isColor) {
            Color.BLACK
        } else {
            when (summary.balanceStatus) {
                BalanceStatus.MATCHED -> Color.rgb(22, 101, 52)
                BalanceStatus.DEFICIT -> Color.rgb(153, 27, 27)
                BalanceStatus.SURPLUS -> Color.rgb(146, 64, 14)
            }
        }

        // Paints
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = textPrimaryColor

        // Background
        paint.color = bgColor
        canvas.drawRect(0f, 0f, width, height, paint)

        val margin = 20f * scale
        val contentWidth = width - (2 * margin)
        var curY = 24f * scale

        // 1. Header Banner
        val headerHeight = 65f * scale
        paint.color = primaryColor
        val headerRect = RectF(margin, curY, width - margin, curY + headerHeight)
        canvas.drawRoundRect(headerRect, 10f * scale, 10f * scale, paint)

        // App Title
        textPaint.color = Color.WHITE
        textPaint.textSize = 18f * scale
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(state.reportHeaderTitle, width / 2, curY + 28f * scale, textPaint)

        // Subtitle (Date + Seller)
        textPaint.textSize = 11f * scale
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateText = "التاريخ: ${AccountingFormatter.formatDisplayDate(state.dateTimestamp, state.showHijriDate, state.useEasternArabicNumerals)}" +
                if (state.sellerName.isNotBlank()) "   |   البائع: ${state.sellerName}" else ""
        canvas.drawText(dateText, width / 2, curY + 48f * scale, textPaint)

        curY += headerHeight + (12f * scale)

        val activeGroups = state.groups.filter { grp ->
            grp.isEnabled && (state.includeInternetInReport || (grp.id != "group_internet" && !grp.name.contains("انترنت")))
        }

        // 2. Groups Table Section
        for (group in activeGroups) {
            // Group Header Bar
            paint.color = primaryLightColor
            val groupHeaderRect = RectF(margin, curY, width - margin, curY + 24f * scale)
            canvas.drawRoundRect(groupHeaderRect, 6f * scale, 6f * scale, paint)

            textPaint.color = primaryColor
            textPaint.textSize = 12f * scale
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textAlign = Paint.Align.RIGHT
            val groupTitle = "مجموعة: ${group.name} (${if (group.isEnabled) "نشط" else "معطل"})"
            canvas.drawText(groupTitle, width - margin - (10f * scale), curY + 16f * scale, textPaint)

            // Group total on left
            textPaint.textAlign = Paint.Align.LEFT
            val groupTotText = "الإجمالي: ${AccountingFormatter.formatYer(group.totalRevenue)}"
            canvas.drawText(groupTotText, margin + (10f * scale), curY + 16f * scale, textPaint)

            curY += 28f * scale

            if (group.type == SalesGroupType.DENOMINATIONS) {
                // Table Header
                paint.color = highlightBg
                canvas.drawRect(margin, curY, width - margin, curY + 18f * scale, paint)
                
                textPaint.color = textSecondaryColor
                textPaint.textSize = 9.5f * scale
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

                val cols = listOf(
                    Triple("الفئة", 0.88f, Paint.Align.CENTER),
                    Triple("المعطى", 0.73f, Paint.Align.CENTER),
                    Triple("الإضافي", 0.58f, Paint.Align.CENTER),
                    Triple("المتبقي", 0.43f, Paint.Align.CENTER),
                    Triple("المباع", 0.28f, Paint.Align.CENTER),
                    Triple("المجموع", 0.12f, Paint.Align.CENTER)
                )

                for (col in cols) {
                    textPaint.textAlign = col.third
                    canvas.drawText(col.first, margin + (contentWidth * col.second), curY + 13f * scale, textPaint)
                }

                curY += 19f * scale

                // Table Rows
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                for (row in group.rows) {
                    if (row.given > 0 || row.added > 0 || row.remaining > 0 || row.sold > 0) {
                        paint.color = borderColor
                        paint.strokeWidth = 0.5f * scale
                        canvas.drawLine(margin, curY + 16f * scale, width - margin, curY + 16f * scale, paint)

                        textPaint.color = textPrimaryColor
                        textPaint.textAlign = Paint.Align.CENTER

                        canvas.drawText(row.denomination.toString(), margin + (contentWidth * 0.88f), curY + 12f * scale, textPaint)
                        canvas.drawText(row.given.toString(), margin + (contentWidth * 0.73f), curY + 12f * scale, textPaint)
                        canvas.drawText(row.added.toString(), margin + (contentWidth * 0.58f), curY + 12f * scale, textPaint)
                        canvas.drawText(row.remaining.toString(), margin + (contentWidth * 0.43f), curY + 12f * scale, textPaint)
                        canvas.drawText(row.sold.toString(), margin + (contentWidth * 0.28f), curY + 12f * scale, textPaint)
                        
                        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText(AccountingFormatter.formatYer(row.total), margin + (contentWidth * 0.12f), curY + 12f * scale, textPaint)
                        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                        curY += 17f * scale
                    }
                }
            } else {
                // Direct Entry Rows
                textPaint.textSize = 9.5f * scale
                for (entry in group.directEntries) {
                    if (entry.isFilled) {
                        paint.color = borderColor
                        paint.strokeWidth = 0.5f * scale
                        canvas.drawLine(margin, curY + 16f * scale, width - margin, curY + 16f * scale, paint)

                        textPaint.textAlign = Paint.Align.RIGHT
                        textPaint.color = textPrimaryColor
                        val isChini = group.id == "group_chini" || group.name.contains("صيني")
                        val titleStr = if (entry.title.isBlank()) {
                            if (isChini) "مبيعات صيني" else "بند مبيعات"
                        } else entry.title

                        val qtyPart = if (isChini || entry.quantity <= 1) "" else " × ${entry.quantity}"
                        val notePart = if (entry.notes.isNotBlank()) " [${entry.notes}]" else ""

                        val entryTitle = if (isChini) {
                            if (entry.title.isNotBlank()) "• ${entry.title} (${AccountingFormatter.formatYer(entry.amount)})$notePart"
                            else "• ${AccountingFormatter.formatYer(entry.amount)}$notePart"
                        } else {
                            "• $titleStr (${AccountingFormatter.formatYer(entry.amount)}$qtyPart)$notePart"
                        }
                        canvas.drawText(entryTitle, width - margin - (10f * scale), curY + 12f * scale, textPaint)

                        textPaint.textAlign = Paint.Align.LEFT
                        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText(AccountingFormatter.formatYer(entry.total), margin + (10f * scale), curY + 12f * scale, textPaint)
                        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                        curY += 17f * scale
                    }
                }
            }
            curY += 8f * scale
        }

        // 3. Summary & Cash Box Cards
        curY += 4f * scale
        val cardHeight = 110f * scale
        paint.color = cardBg
        paint.style = Paint.Style.FILL
        val summaryRect = RectF(margin, curY, width - margin, curY + cardHeight)
        canvas.drawRoundRect(summaryRect, 10f * scale, 10f * scale, paint)

        // Border
        paint.style = Paint.Style.STROKE
        paint.color = borderColor
        paint.strokeWidth = 1f * scale
        canvas.drawRoundRect(summaryRect, 10f * scale, 10f * scale, paint)
        paint.style = Paint.Style.FILL

        // Summary Inner Layout
        var sY = curY + 18f * scale
        textPaint.color = primaryColor
        textPaint.textSize = 12f * scale
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("الملخص المالي والصندوق", width - margin - (14f * scale), sY, textPaint)

        sY += 18f * scale
        textPaint.color = textSecondaryColor
        textPaint.textSize = 10f * scale
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // Row 1: Total Sold & Total Sales
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("إجمالي العناصر المباعة: ${AccountingFormatter.formatNumber(summary.totalSold)} تذكرة", width - margin - (14f * scale), sY, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = textPrimaryColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("مجموع المبيعات: ${AccountingFormatter.formatYer(summary.totalRevenue)}", margin + (14f * scale), sY, textPaint)

        sY += 16f * scale
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = textSecondaryColor
        textPaint.textAlign = Paint.Align.RIGHT
        val yerText = "النقد اليمني: ${AccountingFormatter.formatYer(summary.cashInBoxYer)}"
        val sarText = if (summary.cashInBoxSar > 0) " | السعودي: ${AccountingFormatter.formatSar(summary.cashInBoxSar)}" else ""
        canvas.drawText(yerText + sarText, width - margin - (14f * scale), sY, textPaint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = textPrimaryColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("إجمالي الصندوق: ${AccountingFormatter.formatYer(summary.cashInBox)}", margin + (14f * scale), sY, textPaint)

        sY += 18f * scale
        // Status Badge at bottom of card
        val badgeRect = RectF(margin + (14f * scale), sY - (4f * scale), width - margin - (14f * scale), sY + 22f * scale)
        paint.color = statusBg
        canvas.drawRoundRect(badgeRect, 6f * scale, 6f * scale, paint)

        textPaint.color = statusTextColor
        textPaint.textSize = 11f * scale
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER

        val statusText = when (summary.balanceStatus) {
            BalanceStatus.MATCHED -> "✓ حالة الموازنة: متطابق تماماً (المبيعات = الصندوق)"
            BalanceStatus.DEFICIT -> "⚠ حالة الموازنة: عجز في الصندوق بمقدار ${AccountingFormatter.formatYer(summary.balance)}"
            BalanceStatus.SURPLUS -> "★ حالة الموازنة: فائض في الصندوق بمقدار ${AccountingFormatter.formatYer(abs(summary.balance))}"
        }
        canvas.drawText(statusText, width / 2, sY + 12f * scale, textPaint)

        curY += cardHeight + (10f * scale)

        // Notes if available
        if (state.notes.isNotBlank()) {
            textPaint.color = textSecondaryColor
            textPaint.textSize = 9.5f * scale
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("ملاحظات: ${state.notes}", width - margin - (6f * scale), curY + 10f * scale, textPaint)
            curY += 18f * scale
        }

        // Footer Watermark
        textPaint.color = textSecondaryColor
        textPaint.textSize = 8.5f * scale
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("تم إنشاء التقرير بواسطة تطبيق محاسبة مبيعات التذاكر والصندوق | Design By : HAS (+967780776191)", width / 2, height - (14f * scale), textPaint)
    }
}
