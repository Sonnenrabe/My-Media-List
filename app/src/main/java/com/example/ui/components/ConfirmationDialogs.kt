package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppColorSettingsManager
import com.example.data.AutoBackupStatus
import com.example.data.FullBackupSummary
import com.example.data.Game
import com.example.data.GameRatingColor
import com.example.data.RatingMode
import com.example.localization.LocalAppStrings
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.GameDarkSurfaceHighlight
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog to manually alter playtime with quick adjustment chips,
 * manual H/M/S inputs, live preview, and explicit confirmation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualAdjustTimeDialog(
    game: Game,
    onConfirm: (newTotalSeconds: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var targetSeconds by remember { mutableLongStateOf(game.totalPlaytimeSeconds) }
    var hoursInput by remember { mutableStateOf((targetSeconds / 3600).toString()) }
    var minutesInput by remember { mutableStateOf(((targetSeconds % 3600) / 60).toString()) }
    var secondsInput by remember { mutableStateOf((targetSeconds % 60).toString()) }

    fun updateFromInputs(hStr: String, mStr: String, sStr: String) {
        val h = hStr.toLongOrNull() ?: 0L
        val m = mStr.toLongOrNull() ?: 0L
        val s = sStr.toLongOrNull() ?: 0L
        targetSeconds = (h * 3600L + m * 60L + s).coerceAtLeast(0L)
    }

    fun applyDelta(secondsDelta: Long) {
        val newSec = (targetSeconds + secondsDelta).coerceAtLeast(0L)
        targetSeconds = newSec
        hoursInput = (newSec / 3600).toString()
        minutesInput = ((newSec % 3600) / 60).toString()
        secondsInput = (newSec % 60).toString()
    }

    val diffSeconds = targetSeconds - game.totalPlaytimeSeconds
    val diffFormatted = when {
        diffSeconds > 0 -> {
            val dh = diffSeconds / 3600
            val dm = (diffSeconds % 3600) / 60
            val ds = diffSeconds % 60
            "+ ${dh}h ${dm}m ${ds}s"
        }
        diffSeconds < 0 -> {
            val posDiff = -diffSeconds
            val dh = posDiff / 3600
            val dm = (posDiff % 3600) / 60
            val ds = posDiff % 60
            "- ${dh}h ${dm}m ${ds}s"
        }
        else -> strings.noChange
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.adjustPlaytimeTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(game.colorHex)
                )
                Text(
                    text = strings.currentPlaytime(game.formattedPlaytime()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GameDarkSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.newPlaytime,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            letterSpacing = 1.sp
                        )
                        val h = targetSeconds / 3600
                        val m = (targetSeconds % 3600) / 60
                        val s = targetSeconds % 60
                        val formattedNew = String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
                        Text(
                            text = formattedNew,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = NeonCyan
                        )
                        Text(
                            text = diffFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (diffSeconds >= 0) NeonGreen else NeonRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.quickAdjustments,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "+15m" to 15 * 60L,
                        "+30m" to 30 * 60L,
                        "+1h" to 3600L,
                        "+5h" to 5 * 3600L,
                        "-15m" to -15 * 60L,
                        "-30m" to -30 * 60L,
                        "-1h" to -3600L
                    ).forEach { (label, delta) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (delta > 0) Color(0xFF003844) else Color(0xFF38151E),
                            modifier = Modifier
                                .clickable { applyDelta(delta) }
                                .padding(1.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (delta > 0) NeonCyan else NeonRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.exactDuration,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = hoursInput,
                        onValueChange = {
                            hoursInput = it
                            updateFromInputs(it, minutesInput, secondsInput)
                        },
                        label = { Text(strings.hours, style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("hours_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = TextTertiary
                        )
                    )
                    OutlinedTextField(
                        value = minutesInput,
                        onValueChange = {
                            minutesInput = it
                            updateFromInputs(hoursInput, it, secondsInput)
                        },
                        label = { Text(strings.minutes, style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("mins_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = TextTertiary
                        )
                    )
                    OutlinedTextField(
                        value = secondsInput,
                        onValueChange = {
                            secondsInput = it
                            updateFromInputs(hoursInput, minutesInput, it)
                        },
                        label = { Text(strings.seconds, style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("secs_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = TextTertiary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = strings.confirmPlaytimeWarning,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(targetSeconds) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF002026)),
                modifier = Modifier.testTag("confirm_adjust_time_button")
            ) {
                Text(strings.confirmChange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )
}

/**
 * Dialog to view and edit freeform notes for any game.
 */
@Composable
fun EditNotesDialog(
    game: Game,
    onSaveNotes: (notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var notesText by remember { mutableStateOf(game.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.gameNotesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(game.colorHex)
                )
                Text(
                    text = strings.playtimePrefix(game.formattedPlaytime()),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text(strings.notesDialogPlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 220.dp)
                        .testTag("edit_notes_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = TextTertiary,
                        focusedContainerColor = GameDarkSurfaceVariant,
                        unfocusedContainerColor = GameDarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveNotes(notesText) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF002026)),
                modifier = Modifier.testTag("save_notes_button")
            ) {
                Text(strings.saveNotes, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )
}

/**
 * Confirmation dialog before moving a game to the Archived tab.
 */
@Composable
fun ArchiveConfirmationDialog(
    game: Game,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = strings.archivedCompleted,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GameDarkSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.totalPlaytime,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Text(
                            text = game.formattedPlaytime(),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = NeonCyan
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF002026)),
                modifier = Modifier.testTag("confirm_archive_button")
            ) {
                Text(strings.confirmChange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )
}

/**
 * Confirmation dialog before restoring an archived game back to the Active tab.
 */
@Composable
fun RestoreConfirmationDialog(
    game: Game,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = game.title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = strings.playtimePrefix(game.formattedPlaytime()),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color(0xFF00391A)),
                modifier = Modifier.testTag("confirm_restore_button")
            ) {
                Text(strings.apply, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )
}

/**
 * Confirmation dialog before permanently deleting a game.
 */
@Composable
fun DeleteConfirmationDialog(
    game: Game,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = NeonRed,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = strings.deleteGameQuestion,
                style = MaterialTheme.typography.titleLarge,
                color = NeonRed,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = strings.confirmDeleteMessage(game.title),
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text(strings.deletePermanently, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )
}

/**
 * Dialog to add a new game with title, initial playtime, global word or star rating, and notes.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGameDialog(
    ratingMode: RatingMode = RatingMode.WORDS,
    ratingWords: List<GameRatingColor> = AppColorSettingsManager.DEFAULT_GAME_RATING_COLORS,
    showPlaytimeUi: Boolean = true,
    dialogTitleOverride: String? = null,
    titleLabelOverride: String? = null,
    onAddGame: (
        title: String,
        hours: Long,
        minutes: Long,
        seconds: Long,
        rating: String,
        starRating: Double,
        colorHex: Long,
        isPinned: Boolean,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("0") }
    var minutes by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var starRating by remember { mutableDoubleStateOf(0.0) }
    var isPinned by remember { mutableStateOf(showPlaytimeUi) }
    var isTitleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dialogTitleOverride ?: strings.addGameTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = { Text(titleLabelOverride ?: strings.gameNameRequired) },
                    isError = isTitleError,
                    supportingText = if (isTitleError) {
                        { Text(strings.titleEmptyError) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_game_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = TextTertiary
                    )
                )

                if (showPlaytimeUi) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = strings.startPlaytimeOptional,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = hours,
                            onValueChange = { hours = it },
                            label = { Text(strings.hours, style = MaterialTheme.typography.labelSmall) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = minutes,
                            onValueChange = { minutes = it },
                            label = { Text(strings.minutes, style = MaterialTheme.typography.labelSmall) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (ratingMode == RatingMode.WORDS) {
                    Text(
                        text = strings.ratingOptional,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (item in ratingWords) {
                            val isSelected = rating.equals(item.ratingWord, ignoreCase = true)
                            val wordColor = Color(item.colorHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = { rating = if (isSelected) "" else item.ratingWord },
                                label = { Text(item.ratingWord, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = wordColor.copy(alpha = 0.22f),
                                    selectedLabelColor = wordColor
                                )
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.starRatingLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (starRating > 0.0) {
                                TextButton(onClick = { starRating = 0.0 }) {
                                    Text(strings.clearRating, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (starRating > 0.0) NeonAmber.copy(alpha = 0.15f) else GameDarkSurfaceVariant
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (starRating > 0.0) NeonAmber else TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (starRating > 0.0) String.format(Locale.US, "%.1f", starRating) else "-",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (starRating > 0.0) NeonAmber else TextTertiary
                                    )
                                }
                            }
                        }
                    }

                    if (starRating <= 0.0) {
                        OutlinedButton(
                            onClick = { starRating = 8.0 },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.tapToRate, color = NeonAmber, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Slider(
                            value = starRating.toFloat().coerceIn(1.0f, 10.0f),
                            onValueChange = {
                                starRating = Math.round(it * 10.0) / 10.0
                            },
                            valueRange = 1.0f..10.0f,
                            steps = 89,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonAmber,
                                activeTrackColor = NeonAmber,
                                inactiveTrackColor = GameDarkSurfaceVariant
                            )
                        )
                    }
                }

                if (showPlaytimeUi) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GameDarkSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPinned = !isPinned }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.pinToPlayTime, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                            Checkbox(
                                checked = isPinned,
                                onCheckedChange = { isPinned = it },
                                colors = CheckboxDefaults.colors(checkedColor = NeonCyan)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = strings.notesOptional,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text(strings.gameNotesPlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 70.dp, max = 120.dp)
                        .testTag("add_game_notes_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = TextTertiary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isTitleError = true
                    } else {
                        onAddGame(
                            title,
                            if (showPlaytimeUi) (hours.toLongOrNull() ?: 0L) else 0L,
                            if (showPlaytimeUi) (minutes.toLongOrNull() ?: 0L) else 0L,
                            0L,
                            rating,
                            starRating,
                            0xFF818CF8L,
                            if (showPlaytimeUi) isPinned else false,
                            notes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF002026)),
                modifier = Modifier.testTag("submit_add_game_button")
            ) {
                Text(strings.add, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )
}

/**
 * Legacy transfer dialog kept compatible and localized.
 */
@Composable
fun TransferDataDialog(
    exportJsonString: String,
    autoBackupStatus: AutoBackupStatus,
    onTriggerSaveFile: () -> Unit,
    onTriggerOpenFile: () -> Unit,
    onTriggerChooseFolder: () -> Unit,
    onTriggerResetFolder: () -> Unit,
    onTriggerRunDailyBackupNow: () -> Unit,
    onInspectImportText: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var pasteInputText by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Save, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = strings.navBackup, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = GameDarkSurfaceVariant,
                    contentColor = NeonCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonCyan,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("EXPORT", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("IMPORT", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GameDarkSurfaceHighlight.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Sync, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.dailyAutoBackupTitle, style = MaterialTheme.typography.labelSmall, color = NeonGreen, fontWeight = FontWeight.Bold)
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = NeonGreen.copy(alpha = 0.15f)) {
                                    Text(strings.activeBadge, style = MaterialTheme.typography.labelSmall, color = NeonGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(strings.dailyAutoBackupDesc, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = GameDarkSurfaceVariant, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(strings.storageLocation, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                                    Text(autoBackupStatus.folderDisplayName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(strings.fileLabel(autoBackupStatus.lastBackupFileName), style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontSize = 11.sp)
                                    if (autoBackupStatus.lastBackupTimestampMs > 0L) {
                                        val formatted = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(autoBackupStatus.lastBackupTimestampMs))
                                        Text(strings.lastBackupLabel(formatted), style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onTriggerChooseFolder, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003844), contentColor = NeonCyan), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).defaultMinSize(minHeight = 40.dp)) {
                                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.chooseFolder, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Button(onClick = onTriggerRunDailyBackupNow, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3B2B), contentColor = NeonGreen), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).defaultMinSize(minHeight = 40.dp)) {
                                    Text(strings.backupNow, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            if (autoBackupStatus.hasCustomFolder) {
                                TextButton(onClick = onTriggerResetFolder, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                    Text(strings.resetDefaultFolder, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onTriggerSaveFile, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF002026)), modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp)) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.csvExportButton, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, exportJsonString)
                                    type = "text/csv"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, strings.shareBackupTitle))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003844), contentColor = NeonCyan),
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.shareButton, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Backup", exportJsonString))
                            Toast.makeText(context, strings.csvCopiedToast, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 42.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.copyCsvButton, color = TextPrimary)
                    }
                } else {
                    Text(strings.importDataDesc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onTriggerOpenFile, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF002026)), modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 44.dp)) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.openBackupFileButton, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pasteInputText,
                        onValueChange = { pasteInputText = it },
                        placeholder = { Text(strings.pastePlaceholder, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = TextTertiary),
                        trailingIcon = {
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                if (text.isNotBlank()) pasteInputText = text
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = strings.pasteDesc, tint = NeonCyan)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (pasteInputText.isNotBlank()) {
                                onInspectImportText(pasteInputText)
                            } else {
                                Toast.makeText(context, strings.selectFileOrPasteToast, Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003844), contentColor = NeonCyan),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 44.dp)
                    ) {
                        Text(strings.checkAndImportButton, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.closeDesc, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )
}

/**
 * Confirmation dialog before importing backup data, giving the user the choice
 * to Merge with existing data or Replace everything.
 */
@Composable
fun ImportConfirmationDialog(
    summary: FullBackupSummary,
    onConfirm: (replaceExisting: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var replaceExisting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = strings.confirmImportTitle,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GameDarkSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.backupOverview,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.entriesFound(summary.totalItems),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.importSummaryLine(
                                summary.gameCount,
                                summary.seriesCount,
                                summary.animeCount,
                                summary.mangaCount,
                                summary.bookCount,
                                summary.topTenCount
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.importModeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Merge option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { replaceExisting = false }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !replaceExisting,
                        onClick = { replaceExisting = false },
                        colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(strings.mergeTitle, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text(strings.mergeDesc, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }

                // Replace option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { replaceExisting = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = replaceExisting,
                        onClick = { replaceExisting = true },
                        colors = RadioButtonDefaults.colors(selectedColor = NeonRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            strings.replaceTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (replaceExisting) NeonRed else TextPrimary
                        )
                        Text(strings.replaceDesc, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(replaceExisting) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (replaceExisting) NeonRed else NeonCyan,
                    contentColor = if (replaceExisting) Color.White else Color(0xFF002026)
                ),
                modifier = Modifier.testTag("confirm_import_button")
            ) {
                Text(
                    text = if (replaceExisting) strings.replaceAndImport else strings.mergeAndImport,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )
}
