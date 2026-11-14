package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import com.example.data.GameRatingColor
import com.example.data.RatingMode
import com.example.localization.LocalAppStrings
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RatingSelectorBlock(
    ratingMode: RatingMode,
    ratingWords: List<GameRatingColor>,
    selectedWord: String,
    onSelectWord: (String) -> Unit,
    starScore: Double,
    onChangeStarScore: (Double) -> Unit
) {
    val strings = LocalAppStrings.current
    if (ratingMode == RatingMode.WORDS) {
        Text(
            text = strings.wordRatingLabel,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (item in ratingWords) {
                val isSelected = selectedWord.equals(item.ratingWord, ignoreCase = true)
                val wordColor = Color(item.colorHex)
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectWord(if (isSelected) "" else item.ratingWord) },
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
                if (starScore > 0.0) {
                    TextButton(onClick = { onChangeStarScore(0.0) }) {
                        Text(strings.clearRating, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (starScore > 0.0) NeonAmber.copy(alpha = 0.15f) else GameDarkSurfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (starScore > 0.0) NeonAmber else TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (starScore > 0.0) String.format(Locale.US, "%.1f", starScore) else "-",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (starScore > 0.0) NeonAmber else TextTertiary
                        )
                    }
                }
            }
        }

        if (starScore <= 0.0) {
            OutlinedButton(
                onClick = { onChangeStarScore(8.0) },
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
                value = starScore.toFloat().coerceIn(1.0f, 10.0f),
                onValueChange = {
                    onChangeStarScore(Math.round(it * 10.0) / 10.0)
                },
                valueRange = 1.0f..10.0f,
                steps = 89,
                colors = SliderDefaults.colors(
                    thumbColor = NeonAmber,
                    activeTrackColor = NeonAmber,
                    inactiveTrackColor = GameDarkSurfaceVariant
                ),
                modifier = Modifier.testTag("rating_slider")
            )
        }
    }
}

/**
 * Dialog for adding/editing a TV Series or Anime with protected deletion via confirmation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeriesAnimeEditDialog(
    initialName: String = "",
    initialSeason: String? = null,
    initialRating: Double = 8.0,
    initialRatingWord: String = "",
    initialNotes: String = "",
    ratingMode: RatingMode = RatingMode.STARS,
    ratingWords: List<GameRatingColor> = AppColorSettingsManager.DEFAULT_GAME_RATING_COLORS,
    accentColor: Color? = null,
    isAnime: Boolean = false,
    isEdit: Boolean = false,
    onSave: (name: String, season: String, rating: Double, ratingWord: String, notes: String) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(initialName) }
    var season by remember { mutableStateOf(initialSeason ?: strings.seasonDefault) }
    var rating by remember { mutableDoubleStateOf(if (isEdit) initialRating else if (ratingMode == RatingMode.STARS) initialRating else 0.0) }
    var ratingWord by remember { mutableStateOf(initialRatingWord) }
    var notes by remember { mutableStateOf(initialNotes) }
    var isNameError by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val accent = accentColor ?: if (isAnime) NeonPurple else NeonCyan
    val titlePrefix = if (isEdit) strings.editPrefix else strings.addPrefix
    val typeTitle = if (isAnime) strings.typeAnime else strings.typeSeries
    val seasonChips = listOf(
        strings.seasonOption1,
        strings.seasonOption2,
        strings.seasonOption3,
        strings.statusFinished,
        strings.statusQuit
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "$titlePrefix: $typeTitle",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) isNameError = false
                    },
                    label = { Text(strings.nameTitleRequired) },
                    isError = isNameError,
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("series_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = TextTertiary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.seasonStatusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (chipLabel in seasonChips) {
                        FilterChip(
                            selected = season == chipLabel,
                            onClick = { season = chipLabel },
                            label = { Text(chipLabel, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.copy(alpha = 0.25f),
                                selectedLabelColor = accent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = season,
                    onValueChange = { season = it },
                    label = { Text(strings.customSeasonLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("series_season_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = TextTertiary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                RatingSelectorBlock(
                    ratingMode = ratingMode,
                    ratingWords = ratingWords,
                    selectedWord = ratingWord,
                    onSelectWord = { ratingWord = it },
                    starScore = rating,
                    onChangeStarScore = { rating = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExpandableNotesField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = strings.notesOptional,
                    placeholder = strings.seriesNotesPlaceholder,
                    accentColor = accent,
                    fieldModifier = Modifier.testTag("series_notes_input"),
                    dialogTitle = "${strings.notesOptional} • ${name.ifBlank { typeTitle }}"
                )

                if (isEdit && onDelete != null) {
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
                        Text("${strings.delete}: $typeTitle", fontWeight = FontWeight.Bold, color = NeonRed)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isNameError = true
                    } else {
                        onSave(name.trim(), season.trim(), rating, ratingWord.trim(), notes.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                modifier = Modifier.testTag("save_series_button")
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )

    if (showDeleteConfirmDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = NeonRed, modifier = Modifier.size(32.dp)) },
            title = { Text(strings.confirmDeleteTitle(typeTitle), color = NeonRed) },
            text = { Text(strings.confirmDeleteMessage(name), color = TextPrimary) },
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

/**
 * Dialog for adding/editing a Manga with protected deletion via confirmation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MangaEditDialog(
    initialTitle: String = "",
    initialChapter: String? = null,
    initialRating: Double = 8.0,
    initialRatingWord: String = "",
    initialNotes: String = "",
    ratingMode: RatingMode = RatingMode.STARS,
    ratingWords: List<GameRatingColor> = AppColorSettingsManager.DEFAULT_GAME_RATING_COLORS,
    accentColor: Color? = null,
    isEdit: Boolean = false,
    onSave: (title: String, chapter: String, rating: Double, ratingWord: String, notes: String) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf(initialTitle) }
    var chapter by remember { mutableStateOf(initialChapter ?: strings.chapterDefault) }
    var rating by remember { mutableDoubleStateOf(if (isEdit) initialRating else if (ratingMode == RatingMode.STARS) initialRating else 0.0) }
    var ratingWord by remember { mutableStateOf(initialRatingWord) }
    var notes by remember { mutableStateOf(initialNotes) }
    var isTitleError by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val accent = accentColor ?: NeonGreen
    val titlePrefix = if (isEdit) strings.editPrefix else strings.addPrefix
    val chapterChips = listOf(
        strings.chapterOption1,
        strings.chapterOption2,
        strings.chapterOption3,
        strings.statusFinished,
        strings.statusQuit
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "$titlePrefix: ${strings.typeManga}",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = { Text(strings.mangaTitleRequired) },
                    isError = isTitleError,
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manga_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = TextTertiary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.chapterStatusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (chipLabel in chapterChips) {
                        FilterChip(
                            selected = chapter == chipLabel,
                            onClick = { chapter = chipLabel },
                            label = { Text(chipLabel, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.copy(alpha = 0.25f),
                                selectedLabelColor = accent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text(strings.customChapterLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manga_chapter_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = TextTertiary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                RatingSelectorBlock(
                    ratingMode = ratingMode,
                    ratingWords = ratingWords,
                    selectedWord = ratingWord,
                    onSelectWord = { ratingWord = it },
                    starScore = rating,
                    onChangeStarScore = { rating = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExpandableNotesField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = strings.notesOptional,
                    placeholder = strings.mangaNotesPlaceholder,
                    accentColor = accent,
                    fieldModifier = Modifier.testTag("manga_notes_input"),
                    dialogTitle = "${strings.notesOptional} • ${title.ifBlank { strings.typeManga }}"
                )

                if (isEdit && onDelete != null) {
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
                        Text("${strings.delete}: ${strings.typeManga}", fontWeight = FontWeight.Bold, color = NeonRed)
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
                        onSave(title.trim(), chapter.trim(), rating, ratingWord.trim(), notes.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                modifier = Modifier.testTag("save_manga_button")
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )

    if (showDeleteConfirmDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = NeonRed, modifier = Modifier.size(32.dp)) },
            title = { Text(strings.confirmDeleteTitle(strings.typeManga), color = NeonRed) },
            text = { Text(strings.confirmDeleteMessage(title), color = TextPrimary) },
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

/**
 * Dialog for adding/editing a Book with protected deletion via confirmation.
 */
@Composable
fun BookEditDialog(
    initialTitle: String = "",
    initialAuthor: String = "",
    initialRating: Double = 8.0,
    initialRatingWord: String = "",
    initialNotes: String = "",
    ratingMode: RatingMode = RatingMode.STARS,
    ratingWords: List<GameRatingColor> = AppColorSettingsManager.DEFAULT_GAME_RATING_COLORS,
    accentColor: Color? = null,
    isEdit: Boolean = false,
    onSave: (title: String, author: String, rating: Double, ratingWord: String, notes: String) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf(initialTitle) }
    var author by remember { mutableStateOf(initialAuthor) }
    var rating by remember { mutableDoubleStateOf(if (isEdit) initialRating else if (ratingMode == RatingMode.STARS) initialRating else 0.0) }
    var ratingWord by remember { mutableStateOf(initialRatingWord) }
    var notes by remember { mutableStateOf(initialNotes) }
    var isTitleError by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val accent = accentColor ?: Color(0xFFFF9100)
    val titlePrefix = if (isEdit) strings.editPrefix else strings.addPrefix

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "$titlePrefix: ${strings.typeBook}",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = { Text(strings.bookTitleRequired) },
                    isError = isTitleError,
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = TextTertiary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text(strings.authorLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_author_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = TextTertiary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                RatingSelectorBlock(
                    ratingMode = ratingMode,
                    ratingWords = ratingWords,
                    selectedWord = ratingWord,
                    onSelectWord = { ratingWord = it },
                    starScore = rating,
                    onChangeStarScore = { rating = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExpandableNotesField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = strings.notesOptional,
                    placeholder = strings.bookNotesPlaceholder,
                    accentColor = accent,
                    fieldModifier = Modifier.testTag("book_notes_input"),
                    dialogTitle = "${strings.notesOptional} • ${title.ifBlank { strings.typeBook }}"
                )

                if (isEdit && onDelete != null) {
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
                        Text("${strings.delete}: ${strings.typeBook}", fontWeight = FontWeight.Bold, color = NeonRed)
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
                        onSave(title.trim(), author.trim(), rating, ratingWord.trim(), notes.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                modifier = Modifier.testTag("save_book_button")
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )

    if (showDeleteConfirmDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = NeonRed, modifier = Modifier.size(32.dp)) },
            title = { Text(strings.confirmDeleteTitle(strings.typeBook), color = NeonRed) },
            text = { Text(strings.confirmDeleteMessage(title), color = TextPrimary) },
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
