package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppColorSettingsManager
import com.example.data.Game
import com.example.data.GameRatingColor
import com.example.data.RatingMode
import com.example.localization.LocalAppStrings
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale

/**
 * Dialog shown to edit a game's details.
 * Supports both Global Rating Words and 1.0 - 10.0 Star Principle depending on category settings.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditGameDialog(
    game: Game,
    ratingMode: RatingMode = RatingMode.WORDS,
    ratingWords: List<GameRatingColor> = AppColorSettingsManager.DEFAULT_GAME_RATING_COLORS,
    showPlaytimeUi: Boolean = true,
    showNotesField: Boolean = false,
    dialogTitleOverride: String? = null,
    onNotesSaved: ((String) -> Unit)? = null,
    onSave: (newTitle: String, newRating: String, newStarRating: Double, newColorHex: Long, isPinned: Boolean) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf(game.title) }
    var rating by remember { mutableStateOf(game.rating) }
    var starRating by remember { mutableDoubleStateOf(game.starRating) }
    var notes by remember { mutableStateOf(game.notes) }
    var isPinned by remember { mutableStateOf(game.isPinnedToPlaytime) }
    var isTitleError by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = NeonCyan.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = dialogTitleOverride ?: strings.editGameTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = strings.editGameSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
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
                    label = { Text(strings.titleRequired) },
                    isError = isTitleError,
                    trailingIcon = {
                        if (title.isNotEmpty()) {
                            IconButton(onClick = { title = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = strings.clearDesc, tint = TextTertiary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_game_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = TextTertiary,
                        focusedContainerColor = GameDarkSurfaceVariant,
                        unfocusedContainerColor = GameDarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (ratingMode == RatingMode.WORDS) {
                    Text(
                        text = strings.ratingLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                                label = {
                                    Text(
                                        text = item.ratingWord,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
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
                            ),
                            modifier = Modifier.testTag("game_star_rating_slider")
                        )
                    }
                }

                if (showNotesField) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = strings.notesOptional,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text(strings.gameNotesPlaceholder) },
                        singleLine = false,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_game_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = TextTertiary,
                            focusedContainerColor = GameDarkSurfaceVariant,
                            unfocusedContainerColor = GameDarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (showPlaytimeUi) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Pinned to PlayTime Toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GameDarkSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPinned = !isPinned }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    tint = if (isPinned) NeonCyan else TextTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = strings.pinToPlayTime,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = strings.pinToPlayTimeSub,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Switch(
                                checked = isPinned,
                                onCheckedChange = { isPinned = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonCyan,
                                    checkedTrackColor = Color(0xFF003844)
                                )
                            )
                        }
                    }
                }

                // Safe Deletion Button inside Edit Dialog
                if (onDelete != null) {
                    Spacer(modifier = Modifier.height(18.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.deleteGame, fontWeight = FontWeight.Bold, color = NeonRed)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isTitleError = true
                    } else {
                        if (showNotesField) {
                            onNotesSaved?.invoke(notes)
                        }
                        onSave(title.trim(), rating, starRating, game.colorHex, if (showPlaytimeUi) isPinned else false)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color(0xFF002026)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("save_game_details_button")
            ) {
                Text(
                    text = strings.saveUpper,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )

    // Confirmation dialog before deleting game
    if (showDeleteConfirmDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = NeonRed, modifier = Modifier.size(32.dp)) },
            title = { Text(strings.deleteGameQuestion, color = NeonRed) },
            text = {
                Text(
                    text = strings.confirmDeleteMessage(title),
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White)
                ) {
                    Text(strings.deletePermanently, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = GameDarkSurface
        )
    }
}
