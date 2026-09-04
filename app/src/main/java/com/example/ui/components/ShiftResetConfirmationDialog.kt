package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.model.AccountingFormatter
import com.example.ui.model.BalanceStatus
import com.example.ui.model.DailySalesSummary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ShiftResetPhase {
    CONFIRMATION,
    ANIMATING,
    SUCCESS
}

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val angle: Double,
    val speed: Float,
    val isSquare: Boolean
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShiftResetConfirmationDialog(
    summary: DailySalesSummary,
    useEasternDigits: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmReset: (shiftNotes: String) -> Unit
) {
    var phase by remember { mutableStateOf(ShiftResetPhase.CONFIRMATION) }
    var shiftNoteInput by remember { mutableStateOf("") }
    var animationStep by remember { mutableIntStateOf(0) }
    var autoDismissCountdown by remember { mutableFloatStateOf(1f) }

    // Pulsing animation for confirmation icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Numbers animation countdown from original value to 0.0
    val targetSales = if (animationStep >= 3) 0f else summary.totalRevenue.toFloat()
    val animatedSales by animateFloatAsState(
        targetValue = targetSales,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "animatedSales"
    )

    val targetCash = if (animationStep >= 4) 0f else summary.cashInBox.toFloat()
    val animatedCash by animateFloatAsState(
        targetValue = targetCash,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "animatedCash"
    )

    // Trigger step sequence when in ANIMATING phase
    LaunchedEffect(phase) {
        if (phase == ShiftResetPhase.ANIMATING) {
            animationStep = 1 // Step 1: Backup
            delay(550)
            animationStep = 2 // Step 2: Archive
            delay(550)
            animationStep = 3 // Step 3: Sales reset
            delay(650)
            animationStep = 4 // Step 4: Cash reset
            delay(650)
            animationStep = 5 // Finished
            onConfirmReset(shiftNoteInput)
            delay(250)
            phase = ShiftResetPhase.SUCCESS
        } else if (phase == ShiftResetPhase.SUCCESS) {
            // Auto-dismiss countdown (5 seconds)
            val startTime = System.currentTimeMillis()
            val totalDuration = 5000L
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = 1f - (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)
                autoDismissCountdown = progress
                if (progress <= 0f) {
                    onDismiss()
                    break
                }
                delay(50)
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (phase == ShiftResetPhase.CONFIRMATION || phase == ShiftResetPhase.SUCCESS) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = phase != ShiftResetPhase.ANIMATING,
            dismissOnClickOutside = phase != ShiftResetPhase.ANIMATING,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("shift_reset_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            border = BorderStroke(
                1.5.dp,
                when (phase) {
                    ShiftResetPhase.CONFIRMATION -> MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    ShiftResetPhase.ANIMATING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ShiftResetPhase.SUCCESS -> Color(0xFF10B981).copy(alpha = 0.6f)
                }
            )
        ) {
            AnimatedContent(
                targetState = phase,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f) togetherWith
                            fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.05f)
                },
                label = "dialog_phase_transition"
            ) { currentPhase ->
                when (currentPhase) {
                    ShiftResetPhase.CONFIRMATION -> {
                        ConfirmationPhaseContent(
                            summary = summary,
                            useEasternDigits = useEasternDigits,
                            pulseScale = pulseScale,
                            glowAlpha = glowAlpha,
                            shiftNoteInput = shiftNoteInput,
                            onNoteChange = { shiftNoteInput = it },
                            onCancel = onDismiss,
                            onConfirm = { phase = ShiftResetPhase.ANIMATING }
                        )
                    }
                    ShiftResetPhase.ANIMATING -> {
                        AnimatingPhaseContent(
                            step = animationStep,
                            animatedSales = animatedSales.toDouble(),
                            animatedCash = animatedCash.toDouble(),
                            useEasternDigits = useEasternDigits
                        )
                    }
                    ShiftResetPhase.SUCCESS -> {
                        SuccessPhaseContent(
                            countdownProgress = autoDismissCountdown,
                            onClose = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationPhaseContent(
    summary: DailySalesSummary,
    useEasternDigits: Boolean,
    pulseScale: Float,
    glowAlpha: Float,
    shiftNoteInput: String,
    onNoteChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Animated Glowing Warning Badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            // Glow Halo
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f * glowAlpha)
                    )
            )
            // Center Circle
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(52.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        // Title and Subtitle
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "تصفير الوردية الحالية",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "مراجعة الحسابات وتأكيد بدء وردية جديدة",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        // Live Shift Metrics Summary Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "موجز حسابات الوردية الحالية:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.5.sp
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (summary.balanceStatus) {
                            BalanceStatus.MATCHED -> Color(0xFF10B981).copy(alpha = 0.18f)
                            BalanceStatus.SURPLUS -> Color(0xFF0284C7).copy(alpha = 0.18f)
                            BalanceStatus.DEFICIT -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
                        }
                    ) {
                        Text(
                            text = summary.balanceStatus.label,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = when (summary.balanceStatus) {
                                    BalanceStatus.MATCHED -> Color(0xFF047857)
                                    BalanceStatus.SURPLUS -> Color(0xFF0369A1)
                                    BalanceStatus.DEFICIT -> MaterialTheme.colorScheme.error
                                }
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Metric Rows
                MetricRow(
                    label = "إجمالي المبيعات:",
                    value = AccountingFormatter.formatYer(summary.totalRevenue, useEasternDigits = useEasternDigits),
                    icon = Icons.Default.PointOfSale,
                    color = MaterialTheme.colorScheme.primary
                )
                MetricRow(
                    label = "التذاكر المباعة:",
                    value = "${summary.totalSold} تذكرة",
                    icon = Icons.Default.ConfirmationNumber,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MetricRow(
                    label = "النقد بالصندوق:",
                    value = AccountingFormatter.formatYer(summary.cashInBox, useEasternDigits = useEasternDigits),
                    icon = Icons.Default.AccountBalanceWallet,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (summary.totalExpensesInYer > 0) {
                    MetricRow(
                        label = "المصروفات:",
                        value = AccountingFormatter.formatYer(summary.totalExpensesInYer, useEasternDigits = useEasternDigits),
                        icon = Icons.Default.TrendingDown,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Automatic Backup & Archive Guarantee
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "أمان البيانات: سيتم حفظ نسخة احتياطية وأرشفة تقرير الوردية تلقائياً قبل تصفير العدادات، ولن تضيع أي بيانات.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Optional Shift Closing Notes
        OutlinedTextField(
            value = shiftNoteInput,
            onValueChange = onNoteChange,
            label = { Text("ملاحظة إغلاق الوردية (اختياري)", fontSize = 11.sp) },
            placeholder = { Text("مثال: وردية المساء - كاشير أحمد", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_shift_reset_notes"),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            leadingIcon = {
                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        )

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(44.dp).testTag("btn_cancel_shift_reset")
            ) {
                Text("تراجع", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
            }

            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.weight(1.3f).height(44.dp).testTag("btn_confirm_shift_reset")
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تأكيد وتصفير الوردية", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color))
    }
}

@Composable
private fun AnimatingPhaseContent(
    step: Int,
    animatedSales: Double,
    animatedCash: Double,
    useEasternDigits: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "anim_trans")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rotating Sync/Refresh Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(34.dp)
                            .rotate(rotation)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "جاري تصفير الوردية...",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "يرجى الانتظار لحظات أثناء حفظ البيانات وإعادة ضبط العدادات",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                ),
                textAlign = TextAlign.Center
            )
        }

        // Live Animated Counters Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "عداد المبيعات",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.outline)
                    )
                    Text(
                        text = AccountingFormatter.formatYer(animatedSales, useEasternDigits = useEasternDigits),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = if (animatedSales == 0.0) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                        )
                    )
                }

                VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "نقد الصندوق",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.outline)
                    )
                    Text(
                        text = AccountingFormatter.formatYer(animatedCash, useEasternDigits = useEasternDigits),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = if (animatedCash == 0.0) Color(0xFF10B981) else MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }
        }

        // Step-by-Step Progress List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgressStepItem(
                stepIndex = 1,
                currentStep = step,
                title = "حفظ نسخة احتياطية مشفرة",
                icon = Icons.Default.CloudUpload
            )
            ProgressStepItem(
                stepIndex = 2,
                currentStep = step,
                title = "أرشفة بيانات الوردية في السجل اليومي",
                icon = Icons.Default.FolderZip
            )
            ProgressStepItem(
                stepIndex = 3,
                currentStep = step,
                title = "تصفير عدادات مبيعات التذاكر والفئات",
                icon = Icons.Default.ConfirmationNumber
            )
            ProgressStepItem(
                stepIndex = 4,
                currentStep = step,
                title = "تفريغ وتصفير الصندوق والمصروفات",
                icon = Icons.Default.AccountBalanceWallet
            )
        }

        // Overall progress bar
        val progressFraction = (step.toFloat() / 4f).coerceIn(0f, 1f)
        val animatedProgress by animateFloatAsState(
            targetValue = progressFraction,
            animationSpec = tween(400, easing = LinearEasing),
            label = "progress"
        )
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun ProgressStepItem(
    stepIndex: Int,
    currentStep: Int,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val isCompleted = currentStep > stepIndex
    val isInProgress = currentStep == stepIndex

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = when {
            isCompleted -> Color(0xFF10B981).copy(alpha = 0.12f)
            isInProgress -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = when {
                        isCompleted -> Color(0xFF059669)
                        isInProgress -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
                    }
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = if (isInProgress || isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isCompleted -> Color(0xFF065F46)
                            isInProgress -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                )
            }

            when {
                isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
                isInProgress -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessPhaseContent(
    countdownProgress: Float,
    onClose: () -> Unit
) {
    // Spring animation for big checkmark
    var springTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        springTrigger = true
    }

    val scale by animateFloatAsState(
        targetValue = if (springTrigger) 1f else 0.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "springScale"
    )

    // Particles for celebratory confetti
    val particles = remember {
        val colors = listOf(
            Color(0xFF10B981),
            Color(0xFF3B82F6),
            Color(0xFFF59E0B),
            Color(0xFF8B5CF6),
            Color(0xFFEC4899),
            Color(0xFF14B8A6)
        )
        List(40) {
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val speed = Random.nextFloat() * 70f + 25f
            val dist = Random.nextFloat() * 60f + 20f
            ConfettiParticle(
                x = (cos(angle) * dist).toFloat(),
                y = (sin(angle) * dist).toFloat(),
                color = colors[Random.nextInt(colors.size)],
                size = Random.nextFloat() * 6f + 3f,
                angle = angle,
                speed = speed,
                isSquare = Random.nextBoolean()
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Celebratory Checkmark with Confetti Canvas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(110.dp)
                .padding(top = 4.dp)
        ) {
            // Confetti canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                particles.forEach { p ->
                    val particlePos = Offset(center.x + p.x, center.y + p.y)
                    if (p.isSquare) {
                        drawRect(
                            color = p.color.copy(alpha = 0.85f),
                            topLeft = Offset(particlePos.x - p.size / 2, particlePos.y - p.size / 2),
                            size = androidx.compose.ui.geometry.Size(p.size, p.size)
                        )
                    } else {
                        drawCircle(
                            color = p.color.copy(alpha = 0.85f),
                            radius = p.size / 2,
                            center = particlePos
                        )
                    }
                }
            }

            // Central Bouncy Checkmark Circle
            Surface(
                shape = CircleShape,
                color = Color(0xFF10B981),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(68.dp)
                    .scale(scale)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // Title and Reassurance
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "تم تصفير الوردية بنجاح! ✨",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    color = Color(0xFF047857)
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "تم أرشفة بيانات الوردية السابقة وحفظ نسخة احتياطية آمنة. كافة العدادات والجداول والصناديق الآن عند 0.00 ر.ي.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )
        }

        // Zeroed Stats Pill Badges
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF10B981).copy(alpha = 0.12f),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatZeroBadge(label = "المبيعات", value = "0 ر.ي")
                VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFF10B981).copy(alpha = 0.4f))
                StatZeroBadge(label = "الصندوق", value = "0 ر.ي")
                VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFF10B981).copy(alpha = 0.4f))
                StatZeroBadge(label = "المصروفات", value = "0 ر.ي")
            }
        }

        // Action Button: Start New Shift
        Button(
            onClick = onClose,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("btn_start_new_shift")
        ) {
            Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("بدء الوردية الجديدة الآن", fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp)
        }

        // Auto-dismiss indicator
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LinearProgressIndicator(
                progress = { countdownProgress },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF10B981).copy(alpha = 0.7f),
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "سيتم الإغلاق تلقائياً وبدء العمل",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
private fun StatZeroBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color(0xFF065F46)))
        Text(text = value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF047857)))
    }
}
