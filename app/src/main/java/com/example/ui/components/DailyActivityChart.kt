package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyDateHelper
import com.example.data.DailyPlaytimeRecord
import com.example.localization.AppStrings
import com.example.localization.LocalAppStrings
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.GameDarkSurfaceHighlight
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

private fun localizedEpochDayLabel(epochDay: Long, todayEpoch: Long, strings: AppStrings): String {
    val diff = todayEpoch - epochDay
    return when (diff) {
        0L -> strings.today
        1L -> strings.yesterday
        else -> strings.daysAgo(diff)
    }
}

/**
 * Visual 30-day rolling activity chart and daily log breakdown.
 * Designed to sit inside the detail dialog for each game.
 */
@Composable
fun DailyActivitySection(
    gameId: Long,
    gameColorHex: Long,
    lifetimeTotalSeconds: Long,
    dailyRecords: List<DailyPlaytimeRecord>,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val accentColor = Color(gameColorHex)
    val todayEpoch = remember { DailyDateHelper.getLocalEpochDay() }
    val windowStartEpoch = remember(todayEpoch) { todayEpoch - 29 }

    val recordMap = remember(dailyRecords) {
        dailyRecords.associateBy { it.dateEpochDay }
    }

    val windowDays = remember(todayEpoch, windowStartEpoch) {
        (0 until 30).map { offset -> windowStartEpoch + offset }
    }

    val total30DaySeconds = remember(dailyRecords) {
        dailyRecords.sumOf { it.playtimeSeconds }
    }
    val activeDaysCount = remember(dailyRecords) {
        dailyRecords.count { it.playtimeSeconds > 0L }
    }

    val maxDailySeconds = remember(dailyRecords) {
        dailyRecords.maxOfOrNull { it.playtimeSeconds }?.coerceAtLeast(1800L) ?: 3600L
    }

    var selectedDayIndex by remember { mutableIntStateOf(29) }
    var showAllActiveDays by remember { mutableStateOf(false) }

    val activeRecordsSorted = remember(dailyRecords) {
        dailyRecords.filter { it.playtimeSeconds > 0L }.sortedByDescending { it.dateEpochDay }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = GameDarkSurfaceVariant.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_activity_section_$gameId")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.last30DaysPlaytime,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiary,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = strings.activeDaysCount(activeDaysCount),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    val h30 = total30DaySeconds / 3600
                    val m30 = (total30DaySeconds % 3600) / 60
                    Text(
                        text = if (total30DaySeconds > 0) "${h30}h ${m30}m" else strings.zeroMinutesLogged,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = strings.rolling30DayWindow,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GameDarkBackground.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = strings.lifetimeOverall,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontSize = 9.sp
                        )
                        val totalH = lifetimeTotalSeconds / 3600
                        val totalM = (lifetimeTotalSeconds % 3600) / 60
                        Text(
                            text = "${totalH}h ${totalM}m",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NeonCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(GameDarkBackground.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    windowDays.forEachIndexed { index, epochDay ->
                        val record = recordMap[epochDay]
                        val seconds = record?.playtimeSeconds ?: 0L
                        val isSelected = (index == selectedDayIndex)
                        val hasPlaytime = seconds > 0L

                        val barHeight = if (hasPlaytime) {
                            val ratio = (seconds.toFloat() / maxDailySeconds.toFloat()).coerceIn(0.15f, 1f)
                            (ratio * 46f).dp
                        } else {
                            3.dp
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clickable { selectedDayIndex = index },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(if (isSelected) 6.dp else 4.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                    .background(
                                        when {
                                            isSelected -> Color.White
                                            hasPlaytime -> accentColor
                                            else -> TextTertiary.copy(alpha = 0.25f)
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) accentColor else Color.Transparent,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.thirtyDaysAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
                Text(
                    text = "15d",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
                Text(
                    text = strings.today,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val inspectedEpochDay = windowDays.getOrElse(selectedDayIndex) { todayEpoch }
            val inspectedRecord = recordMap[inspectedEpochDay]
            val inspectedPlaytimeSeconds = inspectedRecord?.playtimeSeconds ?: 0L
            val inspectedDateLabel = localizedEpochDayLabel(inspectedEpochDay, todayEpoch, strings)
            val inspectedIsoDate = DailyDateHelper.formatIsoDate(inspectedEpochDay)

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GameDarkSurfaceHighlight.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (inspectedPlaytimeSeconds > 0L) accentColor else TextTertiary)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "$inspectedDateLabel ($inspectedIsoDate):",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = if (inspectedPlaytimeSeconds > 0L) {
                            val h = inspectedPlaytimeSeconds / 3600
                            val m = (inspectedPlaytimeSeconds % 3600) / 60
                            val s = inspectedPlaytimeSeconds % 60
                            if (h > 0) "${h}h ${m}m" else "${m}m ${s}s"
                        } else {
                            strings.noPlaytime
                        },
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (inspectedPlaytimeSeconds > 0L) accentColor else TextTertiary
                    )
                }
            }

            if (activeRecordsSorted.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.recentActiveSessions,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    if (activeRecordsSorted.size > 3) {
                        TextButton(
                            onClick = { showAllActiveDays = !showAllActiveDays },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (showAllActiveDays) strings.showLess else strings.viewAll(activeRecordsSorted.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val displayedRecords = if (showAllActiveDays) activeRecordsSorted else activeRecordsSorted.take(3)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.animateContentSize()
                ) {
                    displayedRecords.forEach { record ->
                        val dayLabel = localizedEpochDayLabel(record.dateEpochDay, todayEpoch, strings)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GameDarkBackground.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$dayLabel (${record.dateString})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = record.formattedPlaytime(),
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = accentColor
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.noSessionsLast30Days,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}
