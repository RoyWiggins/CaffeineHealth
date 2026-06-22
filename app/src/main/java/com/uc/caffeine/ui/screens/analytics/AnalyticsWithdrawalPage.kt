package com.uc.caffeine.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uc.caffeine.R
import com.uc.caffeine.ui.components.SettingsPageScaffold
import com.uc.caffeine.ui.components.rememberAppHaptics
import com.uc.caffeine.util.AnalyticsUiState
import com.uc.caffeine.util.DailyWithdrawalStat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun AnalyticsWithdrawalPage(
    uiState: AnalyticsUiState,
    onBack: () -> Unit,
) {
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    val haptics = rememberAppHaptics()

    SettingsPageScaffold(
        title = stringResource(R.string.analytics_withdrawal_impact),
        showBackButton = true,
        onBack = onBack,
    ) { bottomPadding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                WithdrawalSummaryCard(uiState = uiState)
            }

            item {
                WithdrawalCalendarCard(
                    displayMonth = displayMonth,
                    dailyStats = uiState.withdrawalDailyStats,
                    onPrevMonth = { haptics.toggle(); displayMonth = displayMonth.minusMonths(1) },
                    onNextMonth = { haptics.toggle(); displayMonth = displayMonth.plusMonths(1) },
                )
            }
        }
    }
}

@Composable
private fun WithdrawalSummaryCard(uiState: AnalyticsUiState) {
    val withdrawalDays = uiState.withdrawalDailyStats.values.count { it.belowThreshold }
    val headacheDays = uiState.withdrawalDailyStats.values.count { it.hadHeadache }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AnalyticsCardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = if (uiState.withdrawalThresholdEnabled) {
                    stringResource(
                        R.string.analytics_withdrawal_threshold_label,
                        uiState.withdrawalThresholdMg.roundToInt(),
                    )
                } else {
                    stringResource(R.string.analytics_withdrawal_disabled)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.analytics_withdrawal_summary, withdrawalDays, headacheDays),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WithdrawalCalendarCard(
    displayMonth: YearMonth,
    dailyStats: Map<LocalDate, DailyWithdrawalStat>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    val currentMonth = remember { YearMonth.now() }
    val minNavigableMonth = remember { currentMonth.minusMonths(24) }
    val monthYearFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }
    val weekFields = remember { WeekFields.of(Locale.getDefault()) }
    val firstDayOfWeek = remember { weekFields.firstDayOfWeek }
    val dayHeaders = remember {
        (0..6).map { offset ->
            firstDayOfWeek.plus(offset.toLong()).getDisplayName(TextStyle.NARROW, Locale.getDefault())
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AnalyticsCardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = onPrevMonth,
                    enabled = displayMonth > minNavigableMonth,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.analytics_bedtime_calendar_prev_month),
                    )
                }
                Text(
                    text = displayMonth.format(monthYearFormatter),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                FilledTonalIconButton(
                    onClick = onNextMonth,
                    enabled = displayMonth < currentMonth,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.analytics_bedtime_calendar_next_month),
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val daysInMonth = displayMonth.lengthOfMonth()
            val firstOfMonth = displayMonth.atDay(1)
            val startOffset = ((firstOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7)
            val rows = (startOffset + daysInMonth + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val dayOfMonth = row * 7 + col - startOffset + 1
                            val isValidDay = dayOfMonth in 1..daysInMonth
                            val date = if (isValidDay) displayMonth.atDay(dayOfMonth) else null
                            val isFuture = date != null && date.isAfter(today)
                            val stat = date?.let { dailyStats[it] }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .then(if (isFuture) Modifier.alpha(0.3f) else Modifier),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                if (isValidDay) {
                                    WithdrawalDayCell(dayNumber = dayOfMonth, stat = stat)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WithdrawalDayCell(
    dayNumber: Int,
    stat: DailyWithdrawalStat?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (stat != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val containerColor = if (stat.belowThreshold) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                }
                val iconColor = if (stat.belowThreshold) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                }
                Surface(
                    shape = CircleShape,
                    color = containerColor,
                    modifier = Modifier.size(20.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (stat.belowThreshold) Icons.Rounded.PriorityHigh else Icons.Rounded.Check,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
                if (stat.hadHeadache) {
                    Text(
                        text = "🤕",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Text(
                text = stringResource(R.string.analytics_value_mg, stat.minCaffeineMg.roundToInt()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
