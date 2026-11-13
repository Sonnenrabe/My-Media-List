package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppColorSettingsManager
import com.example.data.CategoryColorConfig
import com.example.data.DataExportImportManager
import com.example.data.GameRatingColor
import com.example.data.MediaRatingRange
import com.example.data.RatingMode
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.GameViewModel

private val PRESET_COLORS = listOf(
    0xFF818CF8, // Violet
    0xFFFB7185, // Coral Rose
    0xFFA855F7, // Orchid Purple
    0xFF10B981, // Emerald Green
    0xFFF59E0B, // Amber Gold
    0xFF38BDF8, // Sky Blue
    0xFFF43F5E, // Crimson Pink
    0xFF14B8A6, // Teal
    0xFF6366F1, // Indigo
    0xFFEC4899, // Fuchsia
    0xFFEAB308, // Bright Yellow
    0xFFEF4444  // Vivid Red
)

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val categoryColors by viewModel.categoryColors.collectAsStateWithLifecycle()
    val orderedCategoryKeys by viewModel.orderedCategoryKeys.collectAsStateWithLifecycle()
    val gameRatingColors by viewModel.gameRatingColors.collectAsStateWithLifecycle()
    val mediaRatingRanges by viewModel.mediaRatingRanges.collectAsStateWithLifecycle()

    var editingCategory by remember { mutableStateOf<CategoryColorConfig?>(null) }
    var isCreatingCategory by remember { mutableStateOf(false) }
    var editingGameRatingIndex by remember { mutableStateOf<Int?>(null) }
    var isAddingGameRating by remember { mutableStateOf(false) }

    var editingMediaRange by remember { mutableStateOf<Pair<Int, MediaRatingRange>?>(null) }
    var isAddingMediaRange by remember { mutableStateOf(false) }

    var pendingDeleteRatingWord by remember { mutableStateOf<Pair<Int, GameRatingColor>?>(null) }
    var pendingDeleteMediaRange by remember { mutableStateOf<Pair<Int, MediaRatingRange>?>(null) }

    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val tabTitles = listOf(
        strings.settingsTabCategories,
        strings.settingsTabRatingWords,
        strings.settingsTabStarScale,
        strings.settingsTabLanguage
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameDarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Surface
            Surface(
                color = Color(0xFF161A28),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryViolet.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 12.dp,
                        containerColor = Color.Transparent,
                        contentColor = PrimaryViolet,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = PrimaryViolet,
                                height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = if (selectedTab == index) PrimaryViolet else TextSecondary
                                    )
                                }
                            )
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = PrimaryViolet.copy(alpha = 0.15f))
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // CATEGORIES: Name, Rating Mode (Words vs Stars), Accent Color
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.categoriesSectionTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextTertiary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = strings.categoriesSectionDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { isCreatingCategory = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("create_custom_category_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.newCategoryButton, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        itemsIndexed(orderedCategoryKeys) { _, key ->
                            val config = categoryColors[key]
                                ?: AppColorSettingsManager.DEFAULT_CATEGORY_COLORS[key]
                                ?: CategoryColorConfig(categoryKey = key, accentColorHex = 0xFF818CF8, textColorHex = 0xFFF8FAFC)
                            val accentColor = Color(config.accentColorHex)
                            val displayName = viewModel.getCategoryDisplayName(key, strings)
                            val modeLabel = if (config.ratingMode == RatingMode.WORDS) {
                                strings.ratingModeBadgeWords
                            } else {
                                strings.ratingModeBadgeStars
                            }

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editingCategory = config }
                                    .testTag("category_card_$key")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(accentColor)
                                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = displayName,
                                                fontFamily = RajdhaniFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = accentColor.copy(alpha = 0.14f)
                                                ) {
                                                    Text(
                                                        text = modeLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = accentColor,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                if (config.isGameCategory) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = NeonCyan.copy(alpha = 0.14f)
                                                    ) {
                                                        Text(
                                                            text = strings.gameCategoryBadge,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = NeonCyan,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = strings.accentColorLabel(DataExportImportManager.formatHexColor(config.accentColorHex)),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextTertiary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = accentColor.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = strings.editCategoryButton,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { showResetConfirmDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.resetAllSettingsButton, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                1 -> {
                    // GLOBAL RATING WORDS & COLORS (ordered best -> worst)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.ratingWordsSectionTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextTertiary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = strings.ratingWordsSectionDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { isAddingGameRating = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.newButton, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        itemsIndexed(gameRatingColors) { index, item ->
                            val color = Color(item.colorHex)
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
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
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TextTertiary,
                                            modifier = Modifier.width(26.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(6.dp)
                                                .height(32.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.ratingWord,
                                                fontFamily = RajdhaniFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = DataExportImportManager.formatHexColor(item.colorHex),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextTertiary
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (index > 0) {
                                                    val mutable = gameRatingColors.toMutableList()
                                                    val swapped = mutable.removeAt(index)
                                                    mutable.add(index - 1, swapped)
                                                    viewModel.updateGameRatingColors(mutable)
                                                }
                                            },
                                            enabled = index > 0
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = strings.moveUpDesc,
                                                tint = if (index > 0) TextSecondary else TextTertiary.copy(alpha = 0.25f)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (index < gameRatingColors.lastIndex) {
                                                    val mutable = gameRatingColors.toMutableList()
                                                    val swapped = mutable.removeAt(index)
                                                    mutable.add(index + 1, swapped)
                                                    viewModel.updateGameRatingColors(mutable)
                                                }
                                            },
                                            enabled = index < gameRatingColors.lastIndex
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = strings.moveDownDesc,
                                                tint = if (index < gameRatingColors.lastIndex) TextSecondary else TextTertiary.copy(alpha = 0.25f)
                                            )
                                        }
                                        IconButton(onClick = { editingGameRatingIndex = index }) {
                                            Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = {
                                            pendingDeleteRatingWord = index to item
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = NeonRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // STAR RATING COLOR RANGES (1.0 - 10.0)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.starRangesSectionTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextTertiary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = strings.starRangesSectionDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { isAddingMediaRange = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.newButton, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        itemsIndexed(mediaRatingRanges) { index, item ->
                            val color = Color(item.colorHex)
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(6.dp)
                                                .height(34.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = color.copy(alpha = 0.16f),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
                                                ) {
                                                    Text(
                                                        text = "${String.format(java.util.Locale.US, "%.1f", item.minScore)} - ${String.format(java.util.Locale.US, "%.1f", item.maxScore)}",
                                                        fontFamily = RajdhaniFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = color,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                                if (item.label.isNotBlank()) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = item.label,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = TextPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            Text(
                                                text = DataExportImportManager.formatHexColor(item.colorHex),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextTertiary
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { editingMediaRange = index to item }) {
                                            Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = {
                                            pendingDeleteMediaRange = index to item
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = NeonRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // LANGUAGE SELECTOR
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = PrimaryViolet, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.languageSectionTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextTertiary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.languageSectionDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        itemsIndexed(AppLanguage.entries) { _, lang ->
                            val isSelected = (lang == appLanguage)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) PrimaryViolet.copy(alpha = 0.14f) else GameDarkSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PrimaryViolet else Color.White.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setAppLanguage(lang)
                                        val nextStrings = com.example.localization.AppStringsProvider.get(lang)
                                        Toast.makeText(context, nextStrings.languageChangedToast(lang.nativeName), Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("language_option_${lang.code}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = lang.nativeName,
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = if (isSelected) PrimaryViolet else TextPrimary
                                        )
                                        Text(
                                            text = "${lang.englishName} (${lang.code.uppercase()})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = PrimaryViolet.copy(alpha = 0.25f),
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = PrimaryViolet,
                                                    modifier = Modifier.size(18.dp)
                                                )
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
    }

    // DIALOG: EDIT CATEGORY (Name, Rating Principle, Game Category Mode, Accent Color, Delete for Custom)
    editingCategory?.let { cat ->
        val defaultName = viewModel.getCategoryDisplayName(cat.categoryKey, strings)
        val isBuiltIn = viewModel.isBuiltInCategory(cat.categoryKey)
        CategoryEditDialog(
            categoryKey = cat.categoryKey,
            defaultLocalizedName = when (cat.categoryKey) {
                AppColorSettingsManager.KEY_SPIELE -> strings.navGames
                AppColorSettingsManager.KEY_SERIEN -> strings.navSeries
                AppColorSettingsManager.KEY_ANIME -> strings.navAnime
                AppColorSettingsManager.KEY_MANGA -> strings.navManga
                AppColorSettingsManager.KEY_BUECHER -> strings.navBooks
                else -> cat.customName.ifBlank { cat.categoryKey }
            },
            initialCustomName = cat.customName,
            initialRatingMode = cat.ratingMode,
            initialColorHex = cat.accentColorHex,
            initialIsGameCategory = cat.isGameCategory,
            isCreate = false,
            isBuiltIn = isBuiltIn,
            onSave = { customName, ratingMode, newHex, isGameCat ->
                val resolvedName = if (!isBuiltIn && customName.isBlank()) cat.customName else customName
                viewModel.updateCategoryConfig(
                    key = cat.categoryKey,
                    accentHex = newHex,
                    customName = resolvedName,
                    ratingMode = ratingMode,
                    isGameCategory = isGameCat
                )
                editingCategory = null
                val finalName = resolvedName.ifBlank { defaultName }
                Toast.makeText(context, strings.categoryUpdatedToast(finalName), Toast.LENGTH_SHORT).show()
            },
            onDelete = if (!isBuiltIn) {
                {
                    viewModel.deleteCustomCategory(cat.categoryKey)
                    editingCategory = null
                    Toast.makeText(context, strings.categoryDeletedToast(defaultName), Toast.LENGTH_SHORT).show()
                }
            } else null,
            onDismiss = { editingCategory = null }
        )
    }

    // DIALOG: CREATE NEW CUSTOM CATEGORY
    if (isCreatingCategory) {
        CategoryEditDialog(
            categoryKey = "NEW",
            defaultLocalizedName = "",
            initialCustomName = "",
            initialRatingMode = RatingMode.STARS,
            initialColorHex = 0xFF38BDF8L,
            initialIsGameCategory = false,
            isCreate = true,
            isBuiltIn = false,
            onSave = { customName, ratingMode, newHex, isGameCat ->
                viewModel.createCustomCategory(
                    name = customName,
                    accentHex = newHex,
                    ratingMode = ratingMode,
                    isGameCategory = isGameCat
                )
                isCreatingCategory = false
                Toast.makeText(context, strings.categoryCreatedToast(customName), Toast.LENGTH_SHORT).show()
            },
            onDismiss = { isCreatingCategory = false }
        )
    }

    // DIALOG: EDIT / ADD GLOBAL RATING WORD
    val editIndex = editingGameRatingIndex
    if (isAddingGameRating || editIndex != null) {
        val existing = editIndex?.let { gameRatingColors.getOrNull(it) }
        GameRatingColorDialog(
            initialWord = existing?.ratingWord ?: "",
            initialColorHex = existing?.colorHex ?: 0xFF10B981,
            isEdit = (existing != null),
            onSave = { word, hex ->
                val mutable = gameRatingColors.toMutableList()
                if (editIndex != null && editIndex in mutable.indices) {
                    mutable[editIndex] = GameRatingColor(word, hex)
                } else {
                    val existingDup = mutable.indexOfFirst { it.ratingWord.equals(word, ignoreCase = true) }
                    if (existingDup >= 0) {
                        mutable[existingDup] = GameRatingColor(word, hex)
                    } else {
                        mutable.add(GameRatingColor(word, hex))
                    }
                }
                val renamedPair = existing?.ratingWord?.let { oldWord ->
                    if (!oldWord.equals(word, ignoreCase = false)) oldWord to word else null
                }
                viewModel.updateGameRatingColors(mutable, renamedWord = renamedPair)
                isAddingGameRating = false
                editingGameRatingIndex = null
                Toast.makeText(context, strings.ratingWordSavedToast(word), Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                isAddingGameRating = false
                editingGameRatingIndex = null
            }
        )
    }

    // DIALOG: EDIT / ADD STAR RATING RANGE
    if (isAddingMediaRange || editingMediaRange != null) {
        val existingPair = editingMediaRange
        val existing = existingPair?.second
        MediaRangeColorDialog(
            initialMin = existing?.minScore ?: 8.0,
            initialMax = existing?.maxScore ?: 8.9,
            initialLabel = existing?.label ?: "",
            initialColorHex = existing?.colorHex ?: 0xFF38BDF8,
            onSave = { min, max, label, hex ->
                val list = mediaRatingRanges.toMutableList()
                if (existingPair != null) {
                    list[existingPair.first] = MediaRatingRange(min, max, hex, label)
                } else {
                    list.add(MediaRatingRange(min, max, hex, label))
                }
                viewModel.updateMediaRatingRanges(list.sortedByDescending { it.minScore })
                isAddingMediaRange = false
                editingMediaRange = null
                Toast.makeText(context, strings.rangeSavedToast, Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                isAddingMediaRange = false
                editingMediaRange = null
            }
        )
    }

    // DIALOG: CONFIRM DELETE GLOBAL RATING WORD
    pendingDeleteRatingWord?.let { (deleteIdx, item) ->
        AlertDialog(
            onDismissRequest = { pendingDeleteRatingWord = null },
            title = {
                Text(
                    text = strings.confirmDeleteTitle(item.ratingWord),
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = NeonRed
                )
            },
            text = {
                Text(
                    text = strings.confirmDeleteMessage(item.ratingWord),
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = gameRatingColors.toMutableList()
                        if (deleteIdx in updated.indices) {
                            updated.removeAt(deleteIdx)
                            viewModel.updateGameRatingColors(updated)
                            Toast.makeText(context, strings.ratingWordRemovedToast(item.ratingWord), Toast.LENGTH_SHORT).show()
                        }
                        pendingDeleteRatingWord = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(strings.deletePermanently, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pendingDeleteRatingWord = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = GameDarkSurface
        )
    }

    // DIALOG: CONFIRM DELETE STAR RATING RANGE
    pendingDeleteMediaRange?.let { (deleteIdx, item) ->
        val rangeTitle = "${String.format(java.util.Locale.US, "%.1f", item.minScore)} - ${String.format(java.util.Locale.US, "%.1f", item.maxScore)}" +
            if (item.label.isNotBlank()) " (${item.label})" else ""
        AlertDialog(
            onDismissRequest = { pendingDeleteMediaRange = null },
            title = {
                Text(
                    text = strings.confirmDeleteTitle(rangeTitle),
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = NeonRed
                )
            },
            text = {
                Text(
                    text = strings.confirmDeleteMessage(rangeTitle),
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = mediaRatingRanges.toMutableList()
                        if (deleteIdx in updated.indices) {
                            updated.removeAt(deleteIdx)
                            viewModel.updateMediaRatingRanges(updated)
                            Toast.makeText(context, strings.rangeRemovedToast, Toast.LENGTH_SHORT).show()
                        }
                        pendingDeleteMediaRange = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(strings.deletePermanently, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pendingDeleteMediaRange = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = GameDarkSurface
        )
    }

    // DIALOG: RESET CONFIRM
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text(strings.resetConfirmTitle, color = TextPrimary) },
            text = { Text(strings.resetConfirmMessage, color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetColorSettingsToDefaults()
                        showResetConfirmDialog = false
                        Toast.makeText(context, strings.defaultsRestoredToast, Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White)
                ) {
                    Text(strings.reset, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = GameDarkSurface
        )
    }
}

@Composable
fun CategoryEditDialog(
    categoryKey: String,
    defaultLocalizedName: String,
    initialCustomName: String,
    initialRatingMode: RatingMode,
    initialColorHex: Long,
    initialIsGameCategory: Boolean = false,
    isCreate: Boolean = false,
    isBuiltIn: Boolean = true,
    onSave: (customName: String, ratingMode: RatingMode, colorHex: Long, isGameCategory: Boolean) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var customName by remember { mutableStateOf(initialCustomName) }
    var ratingMode by remember { mutableStateOf(initialRatingMode) }
    var isGameCategory by remember { mutableStateOf(initialIsGameCategory) }
    var selectedHex by remember { mutableLongStateOf(initialColorHex) }
    var hexInputText by remember { mutableStateOf(DataExportImportManager.formatHexColor(initialColorHex)) }
    var isNameError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dialogTitleText = if (isCreate) {
        strings.createCategoryDialogTitle
    } else {
        strings.editCategoryDialogTitle(initialCustomName.ifBlank { defaultLocalizedName })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = Color(selectedHex), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dialogTitleText,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = customName,
                    onValueChange = {
                        customName = it
                        if (it.isNotBlank()) isNameError = false
                    },
                    label = { Text(strings.categoryNameLabel) },
                    placeholder = { if (defaultLocalizedName.isNotBlank()) Text(defaultLocalizedName) },
                    isError = isNameError,
                    supportingText = if (isNameError) {
                        { Text(strings.categoryNameRequiredError) }
                    } else null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(selectedHex),
                        unfocusedBorderColor = TextTertiary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_category_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.ratingPrincipleLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Rating Principle Option 1: Words
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (ratingMode == RatingMode.WORDS) Color(selectedHex).copy(alpha = 0.14f) else GameDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (ratingMode == RatingMode.WORDS) Color(selectedHex) else Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { ratingMode = RatingMode.WORDS }
                        .testTag("rating_mode_words_$categoryKey")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = ratingMode == RatingMode.WORDS,
                            onClick = { ratingMode = RatingMode.WORDS },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(selectedHex))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.ratingPrincipleWords, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text(strings.ratingPrincipleWordsDesc, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rating Principle Option 2: Stars (1.0 - 10.0)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (ratingMode == RatingMode.STARS) Color(selectedHex).copy(alpha = 0.14f) else GameDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (ratingMode == RatingMode.STARS) Color(selectedHex) else Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { ratingMode = RatingMode.STARS }
                        .testTag("rating_mode_stars_$categoryKey")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = ratingMode == RatingMode.STARS,
                            onClick = { ratingMode = RatingMode.STARS },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(selectedHex))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.ratingPrincipleStars, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text(strings.ratingPrincipleStarsDesc, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Game Category Toggle (enables/disables time tracking & pinning to PlayTime)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isGameCategory) NeonCyan.copy(alpha = 0.12f) else GameDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isGameCategory) NeonCyan.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isGameCategory = !isGameCategory }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.gameCategoryLabel,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.gameCategoryDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isGameCategory,
                            onCheckedChange = { isGameCategory = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = Color(0xFF003844)
                            ),
                            modifier = Modifier.testTag("category_is_game_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Color Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(selectedHex),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = DataExportImportManager.formatHexColor(selectedHex),
                            fontWeight = FontWeight.Bold,
                            color = if (Color(selectedHex).red * 0.299 + Color(selectedHex).green * 0.587 + Color(selectedHex).blue * 0.114 > 0.5) Color.Black else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(strings.quickColorSelect, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                val rows = PRESET_COLORS.chunked(6)
                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in row) {
                            val isChosen = (col == selectedHex)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(col))
                                    .border(
                                        width = if (isChosen) 3.dp else 1.dp,
                                        color = if (isChosen) Color.White else Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedHex = col
                                        hexInputText = DataExportImportManager.formatHexColor(col)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChosen) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(strings.customHexCode, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = hexInputText,
                    onValueChange = { input ->
                        hexInputText = input
                        selectedHex = DataExportImportManager.parseHexColor(input, selectedHex)
                    },
                    placeholder = { Text("#RRGGBB") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(selectedHex),
                        unfocusedBorderColor = TextTertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isCreate && !isBuiltIn && onDelete != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("delete_custom_category_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.delete, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = customName.trim()
                    if ((isCreate || !isBuiltIn) && trimmed.isBlank()) {
                        isNameError = true
                    } else {
                        onSave(trimmed, ratingMode, selectedHex, isGameCategory)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(selectedHex)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_category_config_button")
            ) {
                Text(strings.apply, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )

    if (showDeleteConfirm && onDelete != null) {
        val displayTitle = customName.ifBlank { defaultLocalizedName }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(strings.deleteCustomCategoryQuestion, color = NeonRed) },
            text = { Text(strings.deleteCustomCategoryConfirm(displayTitle), color = TextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White)
                ) {
                    Text(strings.deletePermanently, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            },
            containerColor = GameDarkSurface
        )
    }
}

@Composable
fun GameRatingColorDialog(
    initialWord: String,
    initialColorHex: Long,
    isEdit: Boolean,
    onSave: (word: String, hex: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var word by remember { mutableStateOf(initialWord) }
    var selectedHex by remember { mutableLongStateOf(initialColorHex) }
    var hexInputText by remember { mutableStateOf(DataExportImportManager.formatHexColor(initialColorHex)) }
    var isWordError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) strings.editRatingWordTitle else strings.newRatingWordTitle,
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
                OutlinedTextField(
                    value = word,
                    onValueChange = {
                        word = it
                        if (it.isNotBlank()) isWordError = false
                    },
                    label = { Text(strings.ratingWordFieldLabel) },
                    isError = isWordError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Live Color Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(selectedHex),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = DataExportImportManager.formatHexColor(selectedHex),
                            fontWeight = FontWeight.Bold,
                            color = if (Color(selectedHex).red * 0.299 + Color(selectedHex).green * 0.587 + Color(selectedHex).blue * 0.114 > 0.5) Color.Black else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(strings.chooseColorLabel, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                val rows = PRESET_COLORS.chunked(6)
                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in row) {
                            val isChosen = (col == selectedHex)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(col))
                                    .border(
                                        width = if (isChosen) 3.dp else 1.dp,
                                        color = if (isChosen) Color.White else Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedHex = col
                                        hexInputText = DataExportImportManager.formatHexColor(col)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChosen) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(strings.customHexCode, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = hexInputText,
                    onValueChange = { input ->
                        hexInputText = input
                        selectedHex = DataExportImportManager.parseHexColor(input, selectedHex)
                    },
                    placeholder = { Text("#RRGGBB") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(selectedHex),
                        unfocusedBorderColor = TextTertiary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rating_word_hex_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (word.isBlank()) {
                        isWordError = true
                    } else {
                        onSave(word.trim(), selectedHex)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(selectedHex)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )
}

@Composable
fun MediaRangeColorDialog(
    initialMin: Double,
    initialMax: Double,
    initialLabel: String,
    initialColorHex: Long,
    onSave: (min: Double, max: Double, label: String, hex: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var minText by remember { mutableStateOf(String.format(java.util.Locale.US, "%.1f", initialMin)) }
    var maxText by remember { mutableStateOf(String.format(java.util.Locale.US, "%.1f", initialMax)) }
    var label by remember { mutableStateOf(initialLabel) }
    var selectedHex by remember { mutableLongStateOf(initialColorHex) }
    var hexInputText by remember { mutableStateOf(DataExportImportManager.formatHexColor(initialColorHex)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.editStarRangeTitle,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { minText = it },
                        label = { Text(strings.fromScoreLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxText,
                        onValueChange = { maxText = it },
                        label = { Text(strings.toScoreLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(strings.rangeLabelOptional) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Live Color Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(selectedHex),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = DataExportImportManager.formatHexColor(selectedHex),
                            fontWeight = FontWeight.Bold,
                            color = if (Color(selectedHex).red * 0.299 + Color(selectedHex).green * 0.587 + Color(selectedHex).blue * 0.114 > 0.5) Color.Black else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(strings.chooseColorLabel, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                val rows = PRESET_COLORS.chunked(6)
                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in row) {
                            val isChosen = (col == selectedHex)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(col))
                                    .border(
                                        width = if (isChosen) 3.dp else 1.dp,
                                        color = if (isChosen) Color.White else Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedHex = col
                                        hexInputText = DataExportImportManager.formatHexColor(col)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChosen) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(strings.customHexCode, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = hexInputText,
                    onValueChange = { input ->
                        hexInputText = input
                        selectedHex = DataExportImportManager.parseHexColor(input, selectedHex)
                    },
                    placeholder = { Text("#RRGGBB") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(selectedHex),
                        unfocusedBorderColor = TextTertiary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("star_range_hex_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val min = minText.toDoubleOrNull() ?: 1.0
                    val max = maxText.toDoubleOrNull() ?: 10.0
                    onSave(min, max, label.trim(), selectedHex)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(selectedHex)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text(strings.cancel, color = TextSecondary)
            }
        },
        containerColor = GameDarkSurface
    )
}
