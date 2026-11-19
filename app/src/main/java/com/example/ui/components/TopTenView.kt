package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TopTenEntry
import com.example.localization.LocalAppStrings
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopTenScreen(
    mediaType: String, // "SPIELE", "SERIEN", "ANIME", "MANGA", "BUECHER", or custom category key
    viewModel: GameViewModel,
    availableTitles: List<String>,
    allowCustomCategories: Boolean = true,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val categoryColors by viewModel.categoryColors.collectAsStateWithLifecycle()
    val categories by viewModel.getTopTenCategories(mediaType).collectAsStateWithLifecycle(emptyList())

    val allCats = remember(categories) { categories.filter { it.isNotBlank() } }
    var selectedCategory by remember {
        mutableStateOf(if (mediaType == "SPIELE") "Singleplayer" else "Top 10")
    }

    LaunchedEffect(allCats) {
        if (allCats.isNotEmpty() && !allCats.contains(selectedCategory)) {
            selectedCategory = allCats.first()
        }
    }

    val currentTopTenEntries by viewModel.getTopTenEntries(mediaType, selectedCategory)
        .collectAsStateWithLifecycle(emptyList())

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var slotToAssign by remember { mutableStateOf<Int?>(null) }
    var entryToEdit by remember { mutableStateOf<TopTenEntry?>(null) }

    val mediaAccentColor = Color(
        categoryColors[mediaType]?.accentColorHex ?: when (mediaType) {
            "SPIELE" -> 0xFF818CF8L
            "SERIEN" -> 0xFFFB7185L
            "ANIME" -> 0xFFA855F7L
            "MANGA" -> 0xFF10B981L
            "BUECHER" -> 0xFFF59E0BL
            else -> 0xFF818CF8L
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (allCats.isEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
                border = BorderStroke(1.dp, mediaAccentColor.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = mediaAccentColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = strings.noTopTenCategories,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = strings.noTopTenCategoriesDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { showAddCategoryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = mediaAccentColor, contentColor = Color(0xFF0D1226)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("topten_create_first_category_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.createCategory, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
                item(key = "top_ten_categories_header") {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        allCats.forEach { cat ->
                            val isSelected = (cat == selectedCategory)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .testTag("topten_category_chip_$cat"),
                                leadingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                label = {
                                    Text(
                                        text = cat,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = mediaAccentColor.copy(alpha = 0.22f),
                                    selectedLabelColor = mediaAccentColor,
                                    selectedLeadingIconColor = mediaAccentColor
                                )
                            )
                        }

                        if (allowCustomCategories) {
                            IconButton(
                                onClick = { showAddCategoryDialog = true },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .size(48.dp)
                                    .testTag("topten_add_category_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = mediaAccentColor.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, mediaAccentColor.copy(alpha = 0.45f)),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = strings.newTopTenCategory,
                                            tint = mediaAccentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            if (selectedCategory.isNotBlank()) {
                                IconButton(
                                    onClick = { categoryToDelete = selectedCategory },
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .size(48.dp)
                                        .testTag("topten_delete_category_button")
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = NeonRed.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.35f)),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = strings.deleteCategoryTitle,
                                                tint = NeonRed.copy(alpha = 0.85f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                items(10, key = { index -> "topten_rank_${index + 1}" }) { index ->
                    val rank = index + 1
                    val entry = currentTopTenEntries.firstOrNull { it.rank == rank && it.title.isNotBlank() }

                    val rankColor = when (rank) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> mediaAccentColor
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("topten_rank_card_$rank")
                            .clickable {
                                if (entry != null) {
                                    entryToEdit = entry
                                } else {
                                    slotToAssign = rank
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (entry != null) GameDarkSurface else GameDarkSurface.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(
                            width = if (rank <= 3 && entry != null) 1.5.dp else 1.dp,
                            color = if (entry != null) rankColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.06f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = rankColor.copy(alpha = if (entry != null) 0.2f else 0.08f),
                                border = BorderStroke(1.dp, rankColor.copy(alpha = 0.6f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#$rank",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = rankColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            if (entry != null) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 18.sp,
                                            hyphens = Hyphens.None,
                                            lineBreak = LineBreak.Heading
                                        ),
                                        color = TextPrimary,
                                        softWrap = true,
                                        maxLines = Int.MAX_VALUE
                                    )
                                    if (entry.subtitle.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = entry.subtitle,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                lineHeight = 16.sp,
                                                hyphens = Hyphens.None,
                                                lineBreak = LineBreak.Simple
                                            ),
                                            color = TextSecondary,
                                            softWrap = true,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = { entryToEdit = entry },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("topten_edit_rank_$rank")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = TextTertiary, modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.deleteTopTenEntry(entry.id) },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("topten_delete_rank_$rank")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = NeonRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                }
                            } else {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.setRankPrompt(rank),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextTertiary
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonCyan.copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(strings.choose, style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text(strings.newTopTenCategory, color = TextPrimary) },
            text = {
                Column {
                    Text(
                        text = strings.newTopTenCategoryPrompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        placeholder = { Text(strings.newTopTenCategoryHint) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topten_new_category_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newCatName.trim()
                        if (trimmed.isNotBlank()) {
                            viewModel.createTopTenCategory(mediaType, trimmed)
                            selectedCategory = trimmed
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF002026)),
                    modifier = Modifier.testTag("topten_create_category_confirm")
                ) {
                    Text(strings.create, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddCategoryDialog = false }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = GameDarkSurface
        )
    }

    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = {
                Text(
                    text = strings.deleteCategoryTitle,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = strings.deleteCategoryConfirm(cat),
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTopTenCategory(mediaType, cat)
                        val remaining = allCats.filter { it != cat }
                        selectedCategory = remaining.firstOrNull() ?: ""
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White),
                    modifier = Modifier.testTag("topten_delete_category_confirm")
                ) {
                    Text(strings.delete, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { categoryToDelete = null }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = GameDarkSurface
        )
    }

    val activeRank = slotToAssign ?: entryToEdit?.rank
    if (activeRank != null) {
        val existingAtRank = entryToEdit ?: currentTopTenEntries.firstOrNull { it.rank == activeRank && it.title.isNotBlank() }
        val canInsertAndShift = existingAtRank != null && activeRank < 10
        val occupiedRanks = remember(currentTopTenEntries) {
            currentTopTenEntries
                .filter { it.title.isNotBlank() && it.rank in 1..10 }
                .map { it.rank }
                .toSet()
        }
        val willDropRankTen = canInsertAndShift && (activeRank..10).all { it in occupiedRanks }

        var inputTitle by remember(activeRank, existingAtRank?.id, selectedCategory) {
            mutableStateOf(existingAtRank?.title ?: "")
        }
        var inputSubtitle by remember(activeRank, existingAtRank?.id, selectedCategory) {
            mutableStateOf(existingAtRank?.subtitle ?: "")
        }
        var shiftExistingDown by remember(activeRank, existingAtRank?.id, selectedCategory) {
            mutableStateOf(false)
        }

        AlertDialog(
            onDismissRequest = {
                slotToAssign = null
                entryToEdit = null
            },
            title = {
                Text(
                    text = strings.rankSlotTitle(activeRank, selectedCategory),
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (canInsertAndShift && existingAtRank != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (!shiftExistingDown) mediaAccentColor.copy(alpha = 0.22f) else GameDarkSurfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (!shiftExistingDown) mediaAccentColor else Color.White.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .testTag("topten_mode_edit")
                                    .clickable {
                                        shiftExistingDown = false
                                        if (inputTitle.isBlank() && inputSubtitle.isBlank()) {
                                            inputTitle = existingAtRank.title
                                            inputSubtitle = existingAtRank.subtitle
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = if (!shiftExistingDown) mediaAccentColor else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.topTenModeEdit,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (!shiftExistingDown) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!shiftExistingDown) mediaAccentColor else TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (shiftExistingDown) NeonCyan.copy(alpha = 0.2f) else GameDarkSurfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (shiftExistingDown) NeonCyan else Color.White.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .testTag("topten_mode_insert_shift")
                                    .clickable {
                                        shiftExistingDown = true
                                        if (inputTitle == existingAtRank.title && inputSubtitle == existingAtRank.subtitle) {
                                            inputTitle = ""
                                            inputSubtitle = ""
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (shiftExistingDown) NeonCyan else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.topTenModeInsertShift,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (shiftExistingDown) FontWeight.Bold else FontWeight.Medium,
                                        color = if (shiftExistingDown) NeonCyan else TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (shiftExistingDown) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeonCyan.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = strings.topTenShiftHint(activeRank, activeRank + 1),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeonCyan
                                    )
                                    if (willDropRankTen) {
                                        Text(
                                            text = strings.topTenShiftDropWarning,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NeonRed.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = strings.pickFromEntries,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (availableTitles.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(availableTitles) { title ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GameDarkSurfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { inputTitle = title }
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (inputTitle == title) NeonCyan else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text(strings.titleRequired) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topten_title_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputSubtitle,
                        onValueChange = { inputSubtitle = it },
                        label = { Text(strings.subtitleOptional) },
                        placeholder = { Text(strings.subtitlePlaceholder) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topten_subtitle_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputTitle.isNotBlank()) {
                            viewModel.saveTopTenEntry(
                                mediaType = mediaType,
                                categoryName = selectedCategory,
                                rank = activeRank,
                                title = inputTitle.trim(),
                                subtitle = inputSubtitle.trim(),
                                shiftExistingDown = shiftExistingDown
                            )
                            slotToAssign = null
                            entryToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF002026)),
                    modifier = Modifier.testTag("topten_save_button")
                ) {
                    Text(strings.save, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    slotToAssign = null
                    entryToEdit = null
                }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = GameDarkSurface
        )
    }
}
