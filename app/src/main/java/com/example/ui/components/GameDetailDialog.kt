package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.DailyPlaytimeRecord
import com.example.data.Game
import com.example.localization.LocalAppStrings
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale

@Composable
fun GameDetailDialog(
    game: Game,
    isRunning: Boolean,
    elapsedSessionSeconds: Long,
    dailyRecords: List<DailyPlaytimeRecord>,
    onTogglePlayPause: () -> Unit,
    onOpenAdjustTime: () -> Unit,
    onOpenEditNotes: () -> Unit,
    onOpenArchive: (() -> Unit)? = null,
    onOpenRestore: (() -> Unit)? = null,
    onOpenDelete: (() -> Unit)? = null,
    onOpenEditDetails: () -> Unit,
    onDismiss: () -> Unit,
    indicatorColor: Color? = null
) {
    val strings = LocalAppStrings.current
    val displayPlaytime = if (isRunning) {
        game.formattedPlaytime(elapsedSessionSeconds)
    } else {
        game.formattedPlaytime(0L)
    }

    val accentColor = indicatorColor ?: Color(game.colorHex)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.5.dp,
                    color = accentColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
                .testTag("game_detail_dialog_${game.id}"),
            shape = RoundedCornerShape(20.dp),
            color = GameDarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header: Title, status indicator & close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(44.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = game.title,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                lineHeight = 28.sp,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isRunning) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NeonGreen)
                                            .alpha(pulseAlpha)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.runningNow,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                } else if (game.isArchived) {
                                    Text(
                                        text = strings.archivedCompleted,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                } else {
                                    Text(
                                        text = strings.activeTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_game_detail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.closeDesc,
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Massive Digital Timer Display (HH:MM:SS)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GameDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isRunning) NeonGreen.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isRunning) strings.sessionInProgress else strings.totalPlaytime,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isRunning) NeonGreen else TextTertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = displayPlaytime,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                                color = if (isRunning) NeonGreen else NeonCyan,
                                modifier = Modifier.testTag("detail_playtime_display")
                            )
                        }

                        if (isRunning) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = strings.thisSession,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                    letterSpacing = 0.8.sp
                                )
                                val sessionH = elapsedSessionSeconds / 3600
                                val sessionM = (elapsedSessionSeconds % 3600) / 60
                                val sessionS = elapsedSessionSeconds % 60
                                val sessionFormatted = String.format(Locale.US, "+%02d:%02d:%02d", sessionH, sessionM, sessionS)
                                Text(
                                    text = sessionFormatted,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    color = NeonGreen
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.06f)
                            ) {
                                Text(
                                    text = game.readablePlaytime(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row: Play/Pause, Adjust Time, Edit / Safe Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTogglePlayPause,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) NeonAmber else accentColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) strings.pauseUpper else strings.playUpper,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRunning) strings.pauseUpper else strings.playUpper,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    OutlinedIconButton(
                        onClick = onOpenAdjustTime,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("detail_adjust_time_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = strings.adjustPlaytimeDesc,
                            tint = NeonCyan
                        )
                    }

                    OutlinedIconButton(
                        onClick = onOpenEditDetails,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("detail_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = strings.editGameDesc,
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Freeform Notes Section
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GameDarkSurfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenEditNotes() }
                        .testTag("detail_notes_section")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = if (game.notes.isNotBlank()) NeonCyan else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (game.notes.isNotBlank()) game.notes else strings.tapToAddNotes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (game.notes.isNotBlank()) TextPrimary else TextTertiary,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = strings.edit,
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DailyActivitySection(
                    gameId = game.id,
                    gameColorHex = game.colorHex,
                    lifetimeTotalSeconds = game.totalPlaytimeSeconds,
                    dailyRecords = dailyRecords
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onOpenEditDetails,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("detail_edit_name_rating_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.editTitleAndRating,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
