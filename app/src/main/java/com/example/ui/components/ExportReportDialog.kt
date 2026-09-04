package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.DailySalesSummary
import com.example.ui.viewmodel.DailyDirectSalesUiState
import com.example.util.ReportExporter

@Composable
fun ExportReportDialog(
    state: DailyDirectSalesUiState,
    summary: DailySalesSummary,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var saveDirectlyToPhone by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تصدير ومشاركة التقرير",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            )
                            Text(
                                text = "اختر الصيغة المناسبة للحفظ أو الإرسال",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                // Direct Save Toggle Switch
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("نوع التقرير:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = !state.isReportDetailedMode,
                                    onClick = { /* Set aggregated mode in caller/parent */ },
                                    label = { Text("تجميعي (افتراضي)", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = state.isReportDetailedMode,
                                    onClick = { /* Set detailed mode in caller/parent */ },
                                    label = { Text("تفصيلي", fontSize = 10.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "حفظ مباشرة بالهاتف بدون مشاركة",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            androidx.compose.material3.Switch(
                                checked = saveDirectlyToPhone,
                                onCheckedChange = { saveDirectlyToPhone = it }
                            )
                        }
                    }
                }

                // Option 1: Colored PDF
                ExportOptionCard(
                    title = "مستند PDF ملون",
                    subtitle = "تصميم احترافي منسق بألوان جذابة جاهز للمشاركة",
                    icon = Icons.Default.Palette,
                    badgeText = "موصى به",
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    testTag = "export_option_pdf_color",
                    onClick = {
                        onDismiss()
                        ReportExporter.exportAsPdf(context, state, summary, ReportExporter.PdfTheme.COLORED, saveDirectly = saveDirectlyToPhone)
                    }
                )

                // Option 2: Black and White PDF
                ExportOptionCard(
                    title = "مستند PDF أبيض وأسود",
                    subtitle = "تنسيق عالي التباين مناسب للطباعة الورقية والاقتصادية",
                    icon = Icons.Default.Print,
                    iconBgColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    testTag = "export_option_pdf_bw",
                    onClick = {
                        onDismiss()
                        ReportExporter.exportAsPdf(context, state, summary, ReportExporter.PdfTheme.BLACK_AND_WHITE, saveDirectly = saveDirectlyToPhone)
                    }
                )

                // Option 3: Image (PNG / JPG)
                ExportOptionCard(
                    title = "صورة التقرير (PNG / JPG)",
                    subtitle = "بطاقة كشف مبيعات مصورة عالية الدقة للواتساب",
                    icon = Icons.Default.Image,
                    iconBgColor = Color(0xFFFFF3E0),
                    iconTint = Color(0xFFE65100),
                    testTag = "export_option_image",
                    onClick = {
                        onDismiss()
                        ReportExporter.exportAsImage(context, state, summary, isPng = true, saveDirectly = saveDirectlyToPhone)
                    }
                )

                // Option 4: CSV / Excel
                ExportOptionCard(
                    title = "جدول بيانات CSV (Excel)",
                    subtitle = "ملف كشف مجدول متوافق مع Excel و Google Sheets مع دعم العربية",
                    icon = Icons.Default.TableChart,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    testTag = "export_option_csv",
                    onClick = {
                        onDismiss()
                        ReportExporter.exportAsCsv(context, state, summary, saveDirectly = saveDirectlyToPhone)
                    }
                )

                // Option 5: Text File / Message (TXT)
                ExportOptionCard(
                    title = "ملف نصي / رسالة سريعة (TXT)",
                    subtitle = "نص منسق يمكن إرساله كرسالة فورية أو حفظه كملف نصي",
                    icon = Icons.Default.Description,
                    iconBgColor = Color(0xFFE3F2FD),
                    iconTint = Color(0xFF1565C0),
                    testTag = "export_option_txt",
                    onClick = {
                        onDismiss()
                        ReportExporter.exportAsTextFile(context, state, summary, saveDirectly = saveDirectlyToPhone)
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_dismiss_export_dialog")
            ) {
                Text("إلغاء", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("export_report_dialog")
    )
}

@Composable
private fun ExportOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    badgeText: String? = null,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconBgColor,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    ),
                    maxLines = 2
                )
            }

            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
