package com.prodash.reminders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class UnifiedItemType(val label: String, val hue: Float, val icon: ImageVector) {
    NOTE("Note", 85f, Icons.Outlined.EditNote),
    REMINDER("Reminder", 25f, Icons.Outlined.Notifications),
    CALENDAR("Event", 250f, Icons.Outlined.CalendarMonth),
    HABIT("Habit", 150f, Icons.Outlined.Flag),
    WALLET("Wallet", 300f, Icons.Outlined.AttachMoney),
}

data class UnifiedTypeColors(
    val bg: Color,
    val border: Color,
    val accent: Color,
)

fun unifiedTypeColors(type: UnifiedItemType, dark: Boolean): UnifiedTypeColors {
    val h = type.hue
    return if (dark) {
        UnifiedTypeColors(
            bg = Color.hsl(h, 0.12f, 0.22f),
            border = Color.hsl(h, 0.18f, 0.42f),
            accent = Color.hsl(h, 0.14f, 0.78f),
        )
    } else {
        UnifiedTypeColors(
            bg = Color.hsl(h, 0.10f, 0.94f),
            border = Color.hsl(h, 0.16f, 0.72f),
            accent = Color.hsl(h, 0.18f, 0.38f),
        )
    }
}

@Composable
fun UnifiedTintCard(
    type: UnifiedItemType,
    title: String,
    meta: String? = null,
    body: String? = null,
    amount: String? = null,
    amountColor: Color? = null,
    linkedLabel: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleDecoration: TextDecoration = TextDecoration.None,
) {
    val palette = LocalBoopPalette.current
    val dark = palette.background.red + palette.background.green + palette.background.blue < 0.35f
    val colors = unifiedTypeColors(type, dark)
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.bg,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = if (dark) Color.Black.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.5f),
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(type.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(13.dp))
                    Text(
                        type.label.uppercase(),
                        color = colors.accent,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            letterSpacing = 0.6.sp,
                        ),
                    )
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                ),
                color = palette.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = titleDecoration,
            )
            if (!meta.isNullOrBlank()) {
                Text(meta, color = palette.muted, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp))
            }
            if (!body.isNullOrBlank()) {
                Text(
                    body,
                    color = palette.muted,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 15.sp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!amount.isNullOrBlank()) {
                Text(
                    amount,
                    color = amountColor ?: colors.accent,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                )
            }
            if (!linkedLabel.isNullOrBlank()) {
                Text(
                    linkedLabel,
                    color = colors.accent,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun UnifiedWalletHero(
    label: String,
    balance: String,
    incomeLabel: String? = null,
    spentLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    val dark = palette.background.red + palette.background.green + palette.background.blue < 0.35f
    val colors = unifiedTypeColors(UnifiedItemType.WALLET, dark)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.bg,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                label.uppercase(),
                color = palette.muted,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    letterSpacing = 1.sp,
                ),
            )
            Text(
                balance,
                color = palette.onBackground,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            if (!incomeLabel.isNullOrBlank() || !spentLabel.isNullOrBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!incomeLabel.isNullOrBlank()) {
                        Text(incomeLabel, color = palette.muted, style = MaterialTheme.typography.bodySmall)
                    }
                    if (!spentLabel.isNullOrBlank()) {
                        Text(spentLabel, color = palette.muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun UnifiedWeekStrip(
    selectedMillis: Long,
    onSelectDay: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    val dark = palette.background.red + palette.background.green + palette.background.blue < 0.35f
    val colors = unifiedTypeColors(UnifiedItemType.CALENDAR, dark)
    val todayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(System.currentTimeMillis())
    val selectedKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(selectedMillis)
    val weekDays = remember(selectedMillis) {
        val anchor = Calendar.getInstance().apply {
            timeInMillis = selectedMillis
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val mondayOffset = (anchor.get(Calendar.DAY_OF_WEEK) + 5) % 7
        anchor.add(Calendar.DAY_OF_MONTH, -mondayOffset)
        List(7) { offset ->
            (anchor.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, offset) }
        }
    }
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        weekDays.forEach { dayCal ->
            val dayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(dayCal.time)
            val isSelected = dayKey == selectedKey
            val isToday = dayKey == todayKey
            val cellBg = when {
                isSelected -> colors.accent.copy(alpha = if (dark) 0.28f else 0.22f)
                isToday -> colors.bg
                else -> palette.surfaceVariant
            }
            val borderColor = when {
                isSelected -> colors.border
                isToday -> colors.accent.copy(alpha = 0.55f)
                else -> palette.muted.copy(alpha = 0.12f)
            }
            Surface(
                onClick = { onSelectDay(dayCal.timeInMillis) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = cellBg,
                border = BorderStroke(1.dp, borderColor),
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        SimpleDateFormat("EEE", Locale.US).format(dayCal.time).uppercase(Locale.US),
                        color = palette.muted,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                        color = if (isSelected) colors.accent else palette.onBackground,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun UnifiedHabitDots(
    dots: List<Boolean>,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    val dark = palette.background.red + palette.background.green + palette.background.blue < 0.35f
    val colors = unifiedTypeColors(UnifiedItemType.HABIT, dark)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dots.forEach { done ->
            Box(
                Modifier
                    .size(12.dp)
                    .background(
                        if (done) colors.accent else palette.surfaceVariant,
                        CircleShape,
                    )
                    .border(
                        1.dp,
                        if (done) colors.border else palette.muted.copy(alpha = 0.2f),
                        CircleShape,
                    ),
            )
        }
    }
}
