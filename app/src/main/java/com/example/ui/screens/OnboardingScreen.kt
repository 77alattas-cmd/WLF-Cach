package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.TicketAccountingViewModel

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val badgeText: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: TicketAccountingViewModel,
    modifier: Modifier = Modifier
) {
    val pages = listOf(
        OnboardingPage(
            title = "واجهة مبيعات سند (التذاكر)",
            subtitle = "إدخال واحتساب المبيعات تلقائياً",
            description = "تُمثل شاشة المبيعات الأساسية في التطبيق. من هنا يمكنك:\n\n• إدخال عدد التذاكر المتاحة (المعطى) والإضافي لكل فئة (مثل 500، 1000، 2000).\n• إدخال عدد التذاكر المتبقية بعد انتهاء نوبة العمل.\n• يقوم النظام تلقائياً باحتساب عدد التذاكر المباعة وإجمالي المبالغ فوراً وبدون أي أخطاء.",
            icon = Icons.Default.ConfirmationNumber,
            badgeText = "الخطوة الأولى"
        ),
        OnboardingPage(
            title = "إدارة ومطابقة الصندوق الفعلي",
            subtitle = "حساب العجز والزيادة والعملات",
            description = "تضمن لك مطابقة دقيقة لأموال الصندوق بمرونة فائقة:\n\n• إدخال النقدية الموجودة بالصندوق بالريال اليمني والريال السعودي.\n• تحديد سعر صرف العملة السعودية مقابل اليمنية لتسوية المبالغ.\n• يقوم النظام بحساب الفارق فوراً وتوضيح حالة الصندوق (عجز أو زيادة) لتسهيل التسوية وتجنب التباين المالي.",
            icon = Icons.Default.AccountBalanceWallet,
            badgeText = "الخطوة الثانية"
        ),
        OnboardingPage(
            title = "حركة المصاريف والإيداعات",
            subtitle = "تتبع التدفق المالي الخارج والداخل",
            description = "لمتابعة حركة الأموال بكل تفصيل داخل الصندوق:\n\n• تسجيل أي مصروفات خارجة ببنود واضحة وعملات مخصصة.\n• تسجيل الإيداعات والمبالغ الداخلة للصندوق لتحديث الرصيد الفعلي.\n• ميزة تصفية ودمج البنود للتعامل الإجمالي السريع.",
            icon = Icons.Default.Payments,
            badgeText = "الخطوة الثالثة"
        ),
        OnboardingPage(
            title = "الآلة الحاسبة وترحيل المبيعات",
            subtitle = "تسهيل الحساب وتنزيل المتبقي",
            description = "أداة حاسبة مدمجة تم تصميمها خصيصاً للتسهيل على البائع:\n\n• إجراء العمليات الحسابية السريعة والضرب والجمع المباشر.\n• ترحيل نواتج الحساب بضغطة زر إلى خانة المتبقي في المبيعات، وإضافة المبالغ إلى نقدية الصندوق آلياً لتسريع الإنتاجية.",
            icon = Icons.Default.Calculate,
            badgeText = "الخطوة الرابعة"
        ),
        OnboardingPage(
            title = "كشوفات التقارير والأرشفة",
            subtitle = "تصدير البيانات ومراجعة السجل اليومي",
            description = "أدوات متكاملة لإعداد التقارير والمشاركة:\n\n• تصدير تقارير المبيعات اليومية والصندوق كصورة احترافية ومشاركتها عبر الواتساب.\n• استعراض 'سجل السندات السابقة' للاطلاع على تفاصيل وتصفيرات الأيام والورديات السابقة بكل مرونة مع إمكانية مسح السجلات.",
            icon = Icons.Default.Assessment,
            badgeText = "الخطوة الخامسة"
        )
    )

    var currentPage by remember { mutableIntStateOf(0) }
    val page = pages[currentPage]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Bar with Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "دليل الاستخدام التعليمي",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            
            TextButton(
                onClick = { viewModel.finishOnboarding() }
            ) {
                Text(
                    text = "تخطي الشرح ✖",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card displaying current page details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = page.badgeText,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                // Decorative Icon Circle
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Subtitle
                Text(
                    text = page.subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Block
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page Indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(width = if (index == currentPage) 24.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Controls: Back & Next Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Previous Button (shown if not on first page)
            if (currentPage > 0) {
                OutlinedButton(
                    onClick = { currentPage-- },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "السابق",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Next / Done Button
            Button(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        viewModel.finishOnboarding()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(if (currentPage > 0) 1.5f else 1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (currentPage < pages.size - 1) "التالي" else "فهمت، ابدأ استخدام التطبيق",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (currentPage < pages.size - 1) Icons.Default.ChevronLeft else Icons.Default.CheckCircle,
                    contentDescription = null
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}
