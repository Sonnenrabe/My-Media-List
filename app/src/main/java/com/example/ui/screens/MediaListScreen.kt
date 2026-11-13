package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AnimeItem
import com.example.data.AppColorSettingsManager
import com.example.data.BookItem
import com.example.data.Game
import com.example.data.MangaItem
import com.example.data.NaturalOrderComparator
import com.example.data.RatingMode
import com.example.data.SeriesItem
import com.example.data.formatDurationHMS
import com.example.localization.LocalAppStrings
import com.example.ui.components.BookEditDialog
import com.example.ui.components.EditNotesDialog
import com.example.ui.components.FastScrollbar
import com.example.ui.components.GameDetailDialog
import com.example.ui.components.MangaEditDialog
import com.example.ui.components.ManualAdjustTimeDialog
import com.example.ui.components.SeriesAnimeEditDialog
import com.example.ui.components.TopTenScreen
import com.example.ui.theme.GameDarkBackground
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
import com.example.viewmodel.AppDestination
import com.example.viewmodel.GameViewModel
import java.util.Locale

enum class SeriesSortMode { STANDARD, NAME, RATING, STATUS, PLAYTIME }
enum class AnimeSortMode { STANDARD, NAME, RATING, STATUS, PLAYTIME }
enum class MangaSortMode { STANDARD, TITEL, RATING, CHAPTER, PLAYTIME }
enum class BookSortMode { STANDARD, TITEL, AUTOR, RATING, PLAYTIME }

private data class PendingMediaPlay(
    val categoryKey: String,
    val itemId: Long,
    val title: String,
    val notes: String,
    val colorHex: Long,
    val totalPlaytimeSeconds: Long
)

@Composable
private fun rememberMediaPlayStarter(
    viewModel: GameViewModel,
    context: Context
): (PendingMediaPlay) -> Unit {
    var pending by remember { mutableStateOf<PendingMediaPlay?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        pending?.let { target ->
            viewModel.playTarget(
                categoryKey = target.categoryKey,
                itemId = target.itemId,
                title = target.title,
                notes = target.notes,
                colorHex = target.colorHex,
                totalPlaytimeSeconds = target.totalPlaytimeSeconds,
                context = context
            )
            pending = null
        }
    }

    return { target ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                pending = target
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.playTarget(
                    categoryKey = target.categoryKey,
                    itemId = target.itemId,
                    title = target.title,
                    notes = target.notes,
                    colorHex = target.colorHex,
                    totalPlaytimeSeconds = target.totalPlaytimeSeconds,
                    context = context
                )
            }
        } else {
            viewModel.playTarget(
                categoryKey = target.categoryKey,
                itemId = target.itemId,
                title = target.title,
                notes = target.notes,
                colorHex = target.colorHex,
                totalPlaytimeSeconds = target.totalPlaytimeSeconds,
                context = context
            )
        }
    }
}

@Composable
private fun MediaHeaderBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    allTabTitle: String,
    topTenTitle: String,
    accentColor: Color,
    headerSurfaceColor: Color,
    sortContent: @Composable () -> Unit
) {
    Surface(
        color = headerSurfaceColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = accentColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = accentColor,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { onSelectTab(0) },
                        text = {
                            Text(
                                text = allTabTitle,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (selectedTab == 0) accentColor else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { onSelectTab(1) },
                        text = {
                            Text(
                                text = topTenTitle,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = if (selectedTab == 1) accentColor else TextSecondary
                            )
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(1.dp)
                        .height(20.dp)
                        .background(accentColor.copy(alpha = 0.35f))
                )
            }

            if (selectedTab == 0) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = accentColor.copy(alpha = 0.15f)
                )
                sortContent()
            }
        }
    }
}

@Composable
fun SeriesScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val categoryKey = AppColorSettingsManager.KEY_SERIEN
    var selectedTab by remember { mutableIntStateOf(0) }
    val seriesList by viewModel.filteredSeries.collectAsStateWithLifecycle()
    val allSeries by viewModel.allSeries.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryColors by viewModel.categoryColors.collectAsStateWithLifecycle()
    val gameRatingColors by viewModel.gameRatingColors.collectAsStateWithLifecycle()
    val runningTarget by viewModel.runningTarget.collectAsStateWithLifecycle()
    val elapsedSessionSeconds by viewModel.elapsedSessionSeconds.collectAsStateWithLifecycle()
    val dailyPlaytimeByMedia by viewModel.dailyPlaytimeByMedia.collectAsStateWithLifecycle()

    val config = categoryColors[categoryKey]
    val displayName = config?.customName?.trim()?.takeIf { it.isNotEmpty() } ?: strings.navSeries
    val ratingMode = config?.ratingMode ?: RatingMode.STARS
    val isGameCategory = config?.isGameCategory == true
    val accentHex = config?.accentColorHex ?: 0xFFFB7185L
    val accentColor = Color(accentHex)
    val headerSurfaceColor = viewModel.getCategoryHeaderSurface(AppDestination.SERIES)
    val sortBarBgColor = viewModel.getCategorySortBarBg(AppDestination.SERIES)

    var sortMode by remember { mutableStateOf(SeriesSortMode.STANDARD) }
    var sortReversed by remember { mutableStateOf(false) }

    LaunchedEffect(isGameCategory) {
        if (!isGameCategory && sortMode == SeriesSortMode.PLAYTIME) {
            sortMode = SeriesSortMode.STANDARD
            sortReversed = false
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<SeriesItem?>(null) }
    var detailItemId by remember { mutableStateOf<Long?>(null) }
    var adjustTimeItemId by remember { mutableStateOf<Long?>(null) }
    var editNotesItemId by remember { mutableStateOf<Long?>(null) }
    val startMediaPlay = rememberMediaPlayStarter(viewModel, context)

    val availableSortModes = remember(isGameCategory) {
        if (isGameCategory) SeriesSortMode.entries else SeriesSortMode.entries.filter { it != SeriesSortMode.PLAYTIME }
    }

    val sortedSeriesList = remember(seriesList, sortMode, sortReversed, ratingMode, gameRatingColors) {
        val baseSorted = when (sortMode) {
            SeriesSortMode.STANDARD -> seriesList.sortedBy { it.sortOrder }
            SeriesSortMode.NAME -> seriesList.sortedWith { a, b -> NaturalOrderComparator.naturalCompare(a.name, b.name) }
            SeriesSortMode.RATING -> if (ratingMode == RatingMode.WORDS) {
                seriesList.sortedBy { viewModel.getRatingWordRank(it.ratingWord) }
            } else {
                seriesList.sortedByDescending { it.rating }
            }
            SeriesSortMode.STATUS -> seriesList.sortedWith { a, b -> NaturalOrderComparator.compareStatus(a.season, b.season) }
            SeriesSortMode.PLAYTIME -> seriesList.sortedByDescending { it.totalPlaytimeSeconds }
        }
        if (sortReversed) baseSorted.reversed() else baseSorted
    }

    val seriesLazyListState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize().background(GameDarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            MediaHeaderBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                allTabTitle = strings.tabAllItems(displayName, allSeries.size),
                topTenTitle = strings.tabTopTen,
                accentColor = accentColor,
                headerSurfaceColor = headerSurfaceColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(sortBarBgColor)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = strings.sortDesc,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableSortModes) { mode ->
                            val isSelected = (sortMode == mode)
                            val chipLabel = when (mode) {
                                SeriesSortMode.STANDARD -> if (isSelected && sortReversed) strings.sortStandardReversed else strings.sortStandard
                                SeriesSortMode.NAME -> if (isSelected && sortReversed) strings.sortNameZA else strings.sortNameAZ
                                SeriesSortMode.RATING -> if (isSelected && sortReversed) strings.sortRatingLowest else strings.sortRatingBest
                                SeriesSortMode.STATUS -> if (isSelected && sortReversed) strings.sortStatusReversed else strings.sortStatus
                                SeriesSortMode.PLAYTIME -> if (isSelected && sortReversed) strings.sortPlaytimeLeast else strings.sortPlaytimeMost
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (sortMode == mode) sortReversed = !sortReversed
                                    else {
                                        sortMode = mode
                                        sortReversed = false
                                    }
                                },
                                label = { Text(chipLabel, fontSize = 12.sp) },
                                trailingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = if (sortReversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = strings.sortDesc,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor.copy(alpha = 0.25f),
                                    selectedLabelColor = accentColor,
                                    containerColor = Color(0xFF281820)
                                )
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                if (sortedSeriesList.isEmpty()) {
                    EmptyMediaView(
                        title = if (searchQuery.isNotEmpty()) strings.emptySearchTitle else strings.emptySeriesTitle,
                        subtitle = if (searchQuery.isNotEmpty()) strings.emptySearchSubtitle else strings.emptySeriesSubtitle,
                        buttonText = if (searchQuery.isNotEmpty()) strings.clearSearch else strings.addFirstSeries,
                        onButtonClick = {
                            if (searchQuery.isNotEmpty()) viewModel.setSearchQuery("") else showAddDialog = true
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = seriesLazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 24.dp, top = 6.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(sortedSeriesList, key = { it.id }, contentType = { "series" }) { item ->
                                val isRunning = isGameCategory &&
                                    runningTarget?.categoryKey == categoryKey &&
                                    runningTarget?.itemId == item.id
                                MediaRowCard(
                                    title = item.name,
                                    statusLabel = item.season,
                                    ratingMode = ratingMode,
                                    rating = item.rating,
                                    ratingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
                                    subtitle = item.notes,
                                    accentColor = accentColor,
                                    indicatorColor = viewModel.getIndicatorColorForRating(ratingMode, item.ratingWord, item.rating, accentColor),
                                    isGameCategory = isGameCategory,
                                    totalPlaytimeSeconds = item.totalPlaytimeSeconds,
                                    isPinnedToPlaytime = item.isPinnedToPlaytime,
                                    isTimerRunning = isRunning,
                                    elapsedSessionSeconds = if (isRunning) elapsedSessionSeconds else 0L,
                                    onTogglePin = if (isGameCategory) {
                                        { viewModel.toggleTargetPinned(categoryKey, item.id, !item.isPinnedToPlaytime) }
                                    } else null,
                                    onCardClick = {
                                        if (isGameCategory) detailItemId = item.id else itemToEdit = item
                                    },
                                    onEdit = { itemToEdit = item }
                                )
                            }
                        }

                        FastScrollbar(
                            lazyListState = seriesLazyListState,
                            totalItemsCount = sortedSeriesList.size,
                            accentColor = accentColor,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(bottom = 80.dp),
                            labelProvider = { index ->
                                val s = sortedSeriesList.getOrNull(index)
                                when (sortMode) {
                                    SeriesSortMode.NAME -> s?.name?.take(1)?.uppercase() ?: ""
                                    SeriesSortMode.STATUS -> s?.season ?: ""
                                    SeriesSortMode.RATING -> if (ratingMode == RatingMode.WORDS) {
                                        s?.let { viewModel.resolveDisplayRatingWord(it.ratingWord).ifBlank { strings.unrated } } ?: ""
                                    } else {
                                        if (s != null && s.rating > 0) String.format(Locale.US, "%.1f", s.rating) else "-"
                                    }
                                    SeriesSortMode.PLAYTIME -> formatDurationHMS(s?.totalPlaytimeSeconds ?: 0L)
                                    else -> "#${index + 1}"
                                }
                            }
                        )
                    }
                }
            } else {
                TopTenScreen(
                    mediaType = categoryKey,
                    viewModel = viewModel,
                    availableTitles = allSeries.map { it.name }
                )
            }
        }

        if (selectedTab == 0) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = accentColor,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(strings.fabAddSeries, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_series")
            )
        }
    }

    if (showAddDialog) {
        SeriesAnimeEditDialog(
            ratingMode = ratingMode,
            ratingWords = gameRatingColors,
            accentColor = accentColor,
            isAnime = false,
            isEdit = false,
            onSave = { name, season, rating, ratingWord, notes ->
                viewModel.addSeries(name, season, rating, notes, ratingWord)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    itemToEdit?.let { item ->
        SeriesAnimeEditDialog(
            initialName = item.name,
            initialSeason = item.season,
            initialRating = item.rating,
            initialRatingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
            initialNotes = item.notes,
            ratingMode = ratingMode,
            ratingWords = gameRatingColors,
            accentColor = accentColor,
            isAnime = false,
            isEdit = true,
            onSave = { name, season, rating, ratingWord, notes ->
                viewModel.updateSeries(
                    item.copy(name = name, season = season, rating = rating, ratingWord = ratingWord, notes = notes)
                )
                itemToEdit = null
            },
            onDelete = {
                viewModel.deleteSeries(item.id)
                if (detailItemId == item.id) detailItemId = null
                itemToEdit = null
            },
            onDismiss = { itemToEdit = null }
        )
    }

    detailItemId?.let { id ->
        val item = allSeries.firstOrNull { it.id == id }
        if (item == null) {
            detailItemId = null
        } else {
            val isRunning = runningTarget?.categoryKey == categoryKey && runningTarget?.itemId == item.id
            val snap = Game(
                id = item.id,
                title = item.name,
                totalPlaytimeSeconds = item.totalPlaytimeSeconds,
                colorHex = accentHex,
                notes = item.notes,
                rating = item.ratingWord,
                starRating = item.rating,
                isPinnedToPlaytime = item.isPinnedToPlaytime,
                categoryKey = categoryKey
            )
            GameDetailDialog(
                game = snap,
                isRunning = isRunning,
                elapsedSessionSeconds = if (isRunning) elapsedSessionSeconds else 0L,
                dailyRecords = dailyPlaytimeByMedia[categoryKey to item.id] ?: emptyList(),
                onTogglePlayPause = {
                    if (isRunning) {
                        viewModel.pauseGame(context)
                    } else {
                        startMediaPlay(PendingMediaPlay(categoryKey, item.id, item.name, item.notes, accentHex, item.totalPlaytimeSeconds))
                    }
                },
                onOpenAdjustTime = { adjustTimeItemId = item.id },
                onOpenEditNotes = { editNotesItemId = item.id },
                onOpenEditDetails = { itemToEdit = item },
                onDismiss = { detailItemId = null },
                indicatorColor = viewModel.getIndicatorColorForRating(ratingMode, item.ratingWord, item.rating, accentColor)
            )
        }
    }

    adjustTimeItemId?.let { id ->
        allSeries.firstOrNull { it.id == id }?.let { item ->
            ManualAdjustTimeDialog(
                game = Game(id = item.id, title = item.name, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, categoryKey = categoryKey),
                onConfirm = { newSecs ->
                    viewModel.adjustTargetPlaytime(categoryKey, item.id, newSecs)
                    adjustTimeItemId = null
                },
                onDismiss = { adjustTimeItemId = null }
            )
        }
    }

    editNotesItemId?.let { id ->
        allSeries.firstOrNull { it.id == id }?.let { item ->
            EditNotesDialog(
                game = Game(id = item.id, title = item.name, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, notes = item.notes, categoryKey = categoryKey),
                onSaveNotes = { newNotes ->
                    viewModel.updateTargetNotes(categoryKey, item.id, newNotes)
                    editNotesItemId = null
                    Toast.makeText(context, strings.savedToast, Toast.LENGTH_SHORT).show()
                },
                onDismiss = { editNotesItemId = null }
            )
        }
    }
}

@Composable
fun AnimeScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val categoryKey = AppColorSettingsManager.KEY_ANIME
    var selectedTab by remember { mutableIntStateOf(0) }
    val animeList by viewModel.filteredAnime.collectAsStateWithLifecycle()
    val allAnime by viewModel.allAnime.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryColors by viewModel.categoryColors.collectAsStateWithLifecycle()
    val gameRatingColors by viewModel.gameRatingColors.collectAsStateWithLifecycle()
    val runningTarget by viewModel.runningTarget.collectAsStateWithLifecycle()
    val elapsedSessionSeconds by viewModel.elapsedSessionSeconds.collectAsStateWithLifecycle()
    val dailyPlaytimeByMedia by viewModel.dailyPlaytimeByMedia.collectAsStateWithLifecycle()

    val config = categoryColors[categoryKey]
    val displayName = config?.customName?.trim()?.takeIf { it.isNotEmpty() } ?: strings.navAnime
    val ratingMode = config?.ratingMode ?: RatingMode.STARS
    val isGameCategory = config?.isGameCategory == true
    val accentHex = config?.accentColorHex ?: 0xFFA855F7L
    val accentColor = Color(accentHex)
    val headerSurfaceColor = viewModel.getCategoryHeaderSurface(AppDestination.ANIME)
    val sortBarBgColor = viewModel.getCategorySortBarBg(AppDestination.ANIME)

    var sortMode by remember { mutableStateOf(AnimeSortMode.STANDARD) }
    var sortReversed by remember { mutableStateOf(false) }

    LaunchedEffect(isGameCategory) {
        if (!isGameCategory && sortMode == AnimeSortMode.PLAYTIME) {
            sortMode = AnimeSortMode.STANDARD
            sortReversed = false
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<AnimeItem?>(null) }
    var detailItemId by remember { mutableStateOf<Long?>(null) }
    var adjustTimeItemId by remember { mutableStateOf<Long?>(null) }
    var editNotesItemId by remember { mutableStateOf<Long?>(null) }
    val startMediaPlay = rememberMediaPlayStarter(viewModel, context)

    val availableSortModes = remember(isGameCategory) {
        if (isGameCategory) AnimeSortMode.entries else AnimeSortMode.entries.filter { it != AnimeSortMode.PLAYTIME }
    }

    val sortedAnimeList = remember(animeList, sortMode, sortReversed, ratingMode, gameRatingColors) {
        val baseSorted = when (sortMode) {
            AnimeSortMode.STANDARD -> animeList.sortedBy { it.sortOrder }
            AnimeSortMode.NAME -> animeList.sortedWith { a, b -> NaturalOrderComparator.naturalCompare(a.name, b.name) }
            AnimeSortMode.RATING -> if (ratingMode == RatingMode.WORDS) {
                animeList.sortedBy { viewModel.getRatingWordRank(it.ratingWord) }
            } else {
                animeList.sortedByDescending { it.rating }
            }
            AnimeSortMode.STATUS -> animeList.sortedWith { a, b -> NaturalOrderComparator.compareStatus(a.season, b.season) }
            AnimeSortMode.PLAYTIME -> animeList.sortedByDescending { it.totalPlaytimeSeconds }
        }
        if (sortReversed) baseSorted.reversed() else baseSorted
    }

    val animeLazyListState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize().background(GameDarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            MediaHeaderBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                allTabTitle = strings.tabAllItems(displayName, allAnime.size),
                topTenTitle = strings.tabTopTen,
                accentColor = accentColor,
                headerSurfaceColor = headerSurfaceColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(sortBarBgColor)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = strings.sortDesc,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableSortModes) { mode ->
                            val isSelected = (sortMode == mode)
                            val chipLabel = when (mode) {
                                AnimeSortMode.STANDARD -> if (isSelected && sortReversed) strings.sortStandardReversed else strings.sortStandard
                                AnimeSortMode.NAME -> if (isSelected && sortReversed) strings.sortNameZA else strings.sortNameAZ
                                AnimeSortMode.RATING -> if (isSelected && sortReversed) strings.sortRatingLowest else strings.sortRatingBest
                                AnimeSortMode.STATUS -> if (isSelected && sortReversed) strings.sortStatusReversed else strings.sortStatus
                                AnimeSortMode.PLAYTIME -> if (isSelected && sortReversed) strings.sortPlaytimeLeast else strings.sortPlaytimeMost
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (sortMode == mode) sortReversed = !sortReversed
                                    else {
                                        sortMode = mode
                                        sortReversed = false
                                    }
                                },
                                label = { Text(chipLabel, fontSize = 12.sp) },
                                trailingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = if (sortReversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = strings.sortDesc,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor.copy(alpha = 0.25f),
                                    selectedLabelColor = accentColor,
                                    containerColor = Color(0xFF241634)
                                )
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                if (sortedAnimeList.isEmpty()) {
                    EmptyMediaView(
                        title = if (searchQuery.isNotEmpty()) strings.emptySearchTitle else strings.emptyAnimeTitle,
                        subtitle = if (searchQuery.isNotEmpty()) strings.emptySearchSubtitle else strings.emptyAnimeSubtitle,
                        buttonText = if (searchQuery.isNotEmpty()) strings.clearSearch else strings.addFirstAnime,
                        onButtonClick = {
                            if (searchQuery.isNotEmpty()) viewModel.setSearchQuery("") else showAddDialog = true
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = animeLazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 24.dp, top = 6.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(sortedAnimeList, key = { it.id }, contentType = { "anime" }) { item ->
                                val isRunning = isGameCategory &&
                                    runningTarget?.categoryKey == categoryKey &&
                                    runningTarget?.itemId == item.id
                                MediaRowCard(
                                    title = item.name,
                                    statusLabel = item.season,
                                    ratingMode = ratingMode,
                                    rating = item.rating,
                                    ratingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
                                    subtitle = item.notes,
                                    accentColor = accentColor,
                                    indicatorColor = viewModel.getIndicatorColorForRating(ratingMode, item.ratingWord, item.rating, accentColor),
                                    isGameCategory = isGameCategory,
                                    totalPlaytimeSeconds = item.totalPlaytimeSeconds,
                                    isPinnedToPlaytime = item.isPinnedToPlaytime,
                                    isTimerRunning = isRunning,
                                    elapsedSessionSeconds = if (isRunning) elapsedSessionSeconds else 0L,
                                    onTogglePin = if (isGameCategory) {
                                        { viewModel.toggleTargetPinned(categoryKey, item.id, !item.isPinnedToPlaytime) }
                                    } else null,
                                    onCardClick = {
                                        if (isGameCategory) detailItemId = item.id else itemToEdit = item
                                    },
                                    onEdit = { itemToEdit = item }
                                )
                            }
                        }

                        FastScrollbar(
                            lazyListState = animeLazyListState,
                            totalItemsCount = sortedAnimeList.size,
                            accentColor = accentColor,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(bottom = 80.dp),
                            labelProvider = { index ->
                                val a = sortedAnimeList.getOrNull(index)
                                when (sortMode) {
                                    AnimeSortMode.NAME -> a?.name?.take(1)?.uppercase() ?: ""
                                    AnimeSortMode.STATUS -> a?.season ?: ""
                                    AnimeSortMode.RATING -> if (ratingMode == RatingMode.WORDS) {
                                        a?.let { viewModel.resolveDisplayRatingWord(it.ratingWord).ifBlank { strings.unrated } } ?: ""
                                    } else {
                                        if (a != null && a.rating > 0) String.format(Locale.US, "%.1f", a.rating) else "-"
                                    }
                                    AnimeSortMode.PLAYTIME -> formatDurationHMS(a?.totalPlaytimeSeconds ?: 0L)
                                    else -> "#${index + 1}"
                                }
                            }
                        )
                    }
                }
            } else {
                TopTenScreen(
                    mediaType = categoryKey,
                    viewModel = viewModel,
                    availableTitles = allAnime.map { it.name }
                )
            }
        }

        if (selectedTab == 0) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = accentColor,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(strings.fabAddAnime, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_anime")
            )
        }
    }

    if (showAddDialog) {
        SeriesAnimeEditDialog(
            ratingMode = ratingMode,
            ratingWords = gameRatingColors,
            accentColor = accentColor,
            isAnime = true,
            isEdit = false,
            onSave = { name, season, rating, ratingWord, notes ->
                viewModel.addAnime(name, season, rating, notes, ratingWord)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    itemToEdit?.let { item ->
        SeriesAnimeEditDialog(
            initialName = item.name,
            initialSeason = item.season,
            initialRating = item.rating,
            initialRatingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
            initialNotes = item.notes,
            ratingMode = ratingMode,
            ratingWords = gameRatingColors,
            accentColor = accentColor,
            isAnime = true,
            isEdit = true,
            onSave = { name, season, rating, ratingWord, notes ->
                viewModel.updateAnime(
                    item.copy(name = name, season = season, rating = rating, ratingWord = ratingWord, notes = notes)
                )
                itemToEdit = null
            },
            onDelete = {
                viewModel.deleteAnime(item.id)
                if (detailItemId == item.id) detailItemId = null
                itemToEdit = null
            },
            onDismiss = { itemToEdit = null }
        )
    }

    detailItemId?.let { id ->
        val item = allAnime.firstOrNull { it.id == id }
        if (item == null) {
            detailItemId = null
        } else {
            val isRunning = runningTarget?.categoryKey == categoryKey && runningTarget?.itemId == item.id
            val snap = Game(
                id = item.id,
                title = item.name,
                totalPlaytimeSeconds = item.totalPlaytimeSeconds,
                colorHex = accentHex,
                notes = item.notes,
                rating = item.ratingWord,
                starRating = item.rating,
                isPinnedToPlaytime = item.isPinnedToPlaytime,
                categoryKey = categoryKey
            )
            GameDetailDialog(
                game = snap,
                isRunning = isRunning,
                elapsedSessionSeconds = if (isRunning) elapsedSessionSeconds else 0L,
                dailyRecords = dailyPlaytimeByMedia[categoryKey to item.id] ?: emptyList(),
                onTogglePlayPause = {
                    if (isRunning) {
                        viewModel.pauseGame(context)
                    } else {
                        startMediaPlay(PendingMediaPlay(categoryKey, item.id, item.name, item.notes, accentHex, item.totalPlaytimeSeconds))
                    }
                },
                onOpenAdjustTime = { adjustTimeItemId = item.id },
                onOpenEditNotes = { editNotesItemId = item.id },
                onOpenEditDetails = { itemToEdit = item },
                onDismiss = { detailItemId = null },
                indicatorColor = viewModel.getIndicatorColorForRating(ratingMode, item.ratingWord, item.rating, accentColor)
            )
        }
    }

    adjustTimeItemId?.let { id ->
        allAnime.firstOrNull { it.id == id }?.let { item ->
            ManualAdjustTimeDialog(
                game = Game(id = item.id, title = item.name, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, categoryKey = categoryKey),
                onConfirm = { newSecs ->
                    viewModel.adjustTargetPlaytime(categoryKey, item.id, newSecs)
                    adjustTimeItemId = null
                },
                onDismiss = { adjustTimeItemId = null }
            )
        }
    }

    editNotesItemId?.let { id ->
        allAnime.firstOrNull { it.id == id }?.let { item ->
            EditNotesDialog(
                game = Game(id = item.id, title = item.name, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, notes = item.notes, categoryKey = categoryKey),
                onSaveNotes = { newNotes ->
                    viewModel.updateTargetNotes(categoryKey, item.id, newNotes)
                    editNotesItemId = null
                    Toast.makeText(context, strings.savedToast, Toast.LENGTH_SHORT).show()
                },
                onDismiss = { editNotesItemId = null }
            )
        }
    }
}

@Composable
fun MangaScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val categoryKey = AppColorSettingsManager.KEY_MANGA
    var selectedTab by remember { mutableIntStateOf(0) }
    val mangaList by viewModel.filteredManga.collectAsStateWithLifecycle()
    val allManga by viewModel.allManga.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryColors by viewModel.categoryColors.collectAsStateWithLifecycle()
    val gameRatingColors by viewModel.gameRatingColors.collectAsStateWithLifecycle()
    val runningTarget by viewModel.runningTarget.collectAsStateWithLifecycle()
    val elapsedSessionSeconds by viewModel.elapsedSessionSeconds.collectAsStateWithLifecycle()
    val dailyPlaytimeByMedia by viewModel.dailyPlaytimeByMedia.collectAsStateWithLifecycle()

    val config = categoryColors[categoryKey]
    val displayName = config?.customName?.trim()?.takeIf { it.isNotEmpty() } ?: strings.navManga
    val ratingMode = config?.ratingMode ?: RatingMode.STARS
    val isGameCategory = config?.isGameCategory == true
    val accentHex = config?.accentColorHex ?: 0xFF10B981L
    val accentColor = Color(accentHex)
    val headerSurfaceColor = viewModel.getCategoryHeaderSurface(AppDestination.MANGA)
    val sortBarBgColor = viewModel.getCategorySortBarBg(AppDestination.MANGA)

    var sortMode by remember { mutableStateOf(MangaSortMode.STANDARD) }
    var sortReversed by remember { mutableStateOf(false) }

    LaunchedEffect(isGameCategory) {
        if (!isGameCategory && sortMode == MangaSortMode.PLAYTIME) {
            sortMode = MangaSortMode.STANDARD
            sortReversed = false
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MangaItem?>(null) }
    var detailItemId by remember { mutableStateOf<Long?>(null) }
    var adjustTimeItemId by remember { mutableStateOf<Long?>(null) }
    var editNotesItemId by remember { mutableStateOf<Long?>(null) }
    val startMediaPlay = rememberMediaPlayStarter(viewModel, context)

    val availableSortModes = remember(isGameCategory) {
        if (isGameCategory) MangaSortMode.entries else MangaSortMode.entries.filter { it != MangaSortMode.PLAYTIME }
    }

    val sortedMangaList = remember(mangaList, sortMode, sortReversed, ratingMode, gameRatingColors) {
        val baseSorted = when (sortMode) {
            MangaSortMode.STANDARD -> mangaList.sortedBy { it.sortOrder }
            MangaSortMode.TITEL -> mangaList.sortedWith { a, b -> NaturalOrderComparator.naturalCompare(a.title, b.title) }
            MangaSortMode.RATING -> if (ratingMode == RatingMode.WORDS) {
                mangaList.sortedBy { viewModel.getRatingWordRank(it.ratingWord) }
            } else {
                mangaList.sortedByDescending { it.rating }
            }
            MangaSortMode.CHAPTER -> mangaList.sortedWith { a, b -> NaturalOrderComparator.compareStatus(a.chapter, b.chapter) }
            MangaSortMode.PLAYTIME -> mangaList.sortedByDescending { it.totalPlaytimeSeconds }
        }
        if (sortReversed) baseSorted.reversed() else baseSorted
    }

    val mangaLazyListState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize().background(GameDarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            MediaHeaderBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                allTabTitle = strings.tabAllItems(displayName, allManga.size),
                topTenTitle = strings.tabTopTen,
                accentColor = accentColor,
                headerSurfaceColor = headerSurfaceColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(sortBarBgColor)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = strings.sortDesc,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableSortModes) { mode ->
                            val isSelected = (sortMode == mode)
                            val chipLabel = when (mode) {
                                MangaSortMode.STANDARD -> if (isSelected && sortReversed) strings.sortStandardReversed else strings.sortStandard
                                MangaSortMode.TITEL -> if (isSelected && sortReversed) strings.sortNameZA else strings.sortNameAZ
                                MangaSortMode.RATING -> if (isSelected && sortReversed) strings.sortRatingLowest else strings.sortRatingBest
                                MangaSortMode.CHAPTER -> if (isSelected && sortReversed) strings.sortChaptersReversed else strings.sortChapters
                                MangaSortMode.PLAYTIME -> if (isSelected && sortReversed) strings.sortPlaytimeLeast else strings.sortPlaytimeMost
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (sortMode == mode) sortReversed = !sortReversed
                                    else {
                                        sortMode = mode
                                        sortReversed = false
                                    }
                                },
                                label = { Text(chipLabel, fontSize = 12.sp) },
                                trailingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = if (sortReversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = strings.sortDesc,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor.copy(alpha = 0.25f),
                                    selectedLabelColor = accentColor,
                                    containerColor = Color(0xFF152A21)
                                )
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                if (sortedMangaList.isEmpty()) {
                    EmptyMediaView(
                        title = if (searchQuery.isNotEmpty()) strings.emptySearchTitle else strings.emptyMangaTitle,
                        subtitle = if (searchQuery.isNotEmpty()) strings.emptySearchSubtitle else strings.emptyMangaSubtitle,
                        buttonText = if (searchQuery.isNotEmpty()) strings.clearSearch else strings.addFirstManga,
                        onButtonClick = {
                            if (searchQuery.isNotEmpty()) viewModel.setSearchQuery("") else showAddDialog = true
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = mangaLazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 24.dp, top = 6.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(sortedMangaList, key = { it.id }, contentType = { "manga" }) { item ->
                                val isRunning = isGameCategory &&
                                    runningTarget?.categoryKey == categoryKey &&
                                    runningTarget?.itemId == item.id
                                MediaRowCard(
                                    title = item.title,
                                    statusLabel = item.chapter,
                                    ratingMode = ratingMode,
                                    rating = item.rating,
                                    ratingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
                                    subtitle = item.notes,
                                    accentColor = accentColor,
                                    indicatorColor = viewModel.getIndicatorColorForRating(ratingMode, item.ratingWord, item.rating, accentColor),
                                    isGameCategory = isGameCategory,
                                    totalPlaytimeSeconds = item.totalPlaytimeSeconds,
                                    isPinnedToPlaytime = item.isPinnedToPlaytime,
                                    isTimerRunning = isRunning,
                                    elapsedSessionSeconds = if (isRunning) elapsedSessionSeconds else 0L,
                                    onTogglePin = if (isGameCategory) {
                                        { viewModel.toggleTargetPinned(categoryKey, item.id, !item.isPinnedToPlaytime) }
                                    } else null,
                                    onCardClick = {
                                        if (isGameCategory) detailItemId = item.id else itemToEdit = item
                                    },
                                    onEdit = { itemToEdit = item }
                                )
                            }
                        }

                        FastScrollbar(
                            lazyListState = mangaLazyListState,
                            totalItemsCount = sortedMangaList.size,
                            accentColor = accentColor,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(bottom = 80.dp),
                            labelProvider = { index ->
                                val m = sortedMangaList.getOrNull(index)
                                when (sortMode) {
                                    MangaSortMode.TITEL -> m?.title?.take(1)?.uppercase() ?: ""
                                    MangaSortMode.CHAPTER -> m?.chapter ?: ""
                                    MangaSortMode.RATING -> if (ratingMode == RatingMode.WORDS) {
                                        m?.let { viewModel.resolveDisplayRatingWord(it.ratingWord).ifBlank { strings.unrated } } ?: ""
                                    } else {
                                        if (m != null && m.rating > 0) String.format(Locale.US, "%.1f", m.rating) else "-"
                                    }
                                    MangaSortMode.PLAYTIME -> formatDurationHMS(m?.totalPlaytimeSeconds ?: 0L)
                                    else -> "#${index + 1}"
                                }
                            }
                        )
                    }
                }
            } else {
                TopTenScreen(
                    mediaType = categoryKey,
                    viewModel = viewModel,
                    availableTitles = allManga.map { it.title }
                )
            }
        }

        if (selectedTab == 0) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = accentColor,
                contentColor = Color(0xFF003314),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(strings.fabAddManga, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_manga")
            )
        }
    }

    if (showAddDialog) {
        MangaEditDialog(
            ratingMode = ratingMode,
            ratingWords = gameRatingColors,
            accentColor = accentColor,
            isEdit = false,
            onSave = { title, chapter, rating, ratingWord, notes ->
                viewModel.addManga(title, chapter, rating, notes, ratingWord)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    itemToEdit?.let { item ->
        MangaEditDialog(
            initialTitle = item.title,
            initialChapter = item.chapter,
            initialRating = item.rating,
            initialRatingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
            initialNotes = item.notes,
            ratingMode = ratingMode,
            ratingWords = gameRatingColors,
            accentColor = accentColor,
            isEdit = true,
            onSave = { title, chapter, rating, ratingWord, notes ->
                viewModel.updateManga(
                    item.copy(title = title, chapter = chapter, rating = rating, ratingWord = ratingWord, notes = notes)
                )
                itemToEdit = null
            },
            onDelete = {
                viewModel.deleteManga(item.id)
                if (detailItemId == item.id) detailItemId = null
                itemToEdit = null
            },
            onDismiss = { itemToEdit = null }
        )
    }

    detailItemId?.let { id ->
        val item = allManga.firstOrNull { it.id == id }
        if (item == null) {
            detailItemId = null
        } else {
            val isRunning = runningTarget?.categoryKey == categoryKey && runningTarget?.itemId == item.id
            val snap = Game(
                id = item.id,
                title = item.title,
                totalPlaytimeSeconds = item.totalPlaytimeSeconds,
                colorHex = accentHex,
                notes = item.notes,
                rating = item.ratingWord,
                starRating = item.rating,
                isPinnedToPlaytime = item.isPinnedToPlaytime,
                categoryKey = categoryKey
            )
            GameDetailDialog(
                game = snap,
                isRunning = isRunning,
                elapsedSessionSeconds = if (isRunning) elapsedSessionSeconds else 0L,
                dailyRecords = dailyPlaytimeByMedia[categoryKey to item.id] ?: emptyList(),
                onTogglePlayPause = {
                    if (isRunning) {
                        viewModel.pauseGame(context)
                    } else {
                        startMediaPlay(PendingMediaPlay(categoryKey, item.id, item.title, item.notes, accentHex, item.totalPlaytimeSeconds))
                    }
                },
                onOpenAdjustTime = { adjustTimeItemId = item.id },
                onOpenEditNotes = { editNotesItemId = item.id },
                onOpenEditDetails = { itemToEdit = item },
                onDismiss = { detailItemId = null },
                indicatorColor = viewModel.getIndicatorColorForRating(ratingMode, item.ratingWord, item.rating, accentColor)
            )
        }
    }

    adjustTimeItemId?.let { id ->
        allManga.firstOrNull { it.id == id }?.let { item ->
            ManualAdjustTimeDialog(
                game = Game(id = item.id, title = item.title, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, categoryKey = categoryKey),
                onConfirm = { newSecs ->
                    viewModel.adjustTargetPlaytime(categoryKey, item.id, newSecs)
                    adjustTimeItemId = null
                },
                onDismiss = { adjustTimeItemId = null }
            )
        }
    }

    editNotesItemId?.let { id ->
        allManga.firstOrNull { it.id == id }?.let { item ->
            EditNotesDialog(
                game = Game(id = item.id, title = item.title, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, notes = item.notes, categoryKey = categoryKey),
                onSaveNotes = { newNotes ->
                    viewModel.updateTargetNotes(categoryKey, item.id, newNotes)
                    editNotesItemId = null
                    Toast.makeText(context, strings.savedToast, Toast.LENGTH_SHORT).show()
                },
                onDismiss = { editNotesItemId = null }
            )
        }
    }
}

@Composable
fun BooksScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val categoryKey = AppColorSettingsManager.KEY_BUECHER
    var selectedTab by remember { mutableIntStateOf(0) }
    val booksList by viewModel.filteredBooks.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryColors by viewModel.categoryColors.collectAsStateWithLifecycle()
    val gameRatingColors by viewModel.gameRatingColors.collectAsStateWithLifecycle()
    val runningTarget by viewModel.runningTarget.collectAsStateWithLifecycle()
    val elapsedSessionSeconds by viewModel.elapsedSessionSeconds.collectAsStateWithLifecycle()
    val dailyPlaytimeByMedia by viewModel.dailyPlaytimeByMedia.collectAsStateWithLifecycle()

    val config = categoryColors[categoryKey]
    val displayName = config?.customName?.trim()?.takeIf { it.isNotEmpty() } ?: strings.navBooks
    val ratingMode = config?.ratingMode ?: RatingMode.STARS
    val isGameCategory = config?.isGameCategory == true
    val accentHex = config?.accentColorHex ?: 0xFFF59E0BL
    val bookAccent = Color(accentHex)
    val headerSurfaceColor = viewModel.getCategoryHeaderSurface(AppDestination.BOOKS)
    val sortBarBgColor = viewModel.getCategorySortBarBg(AppDestination.BOOKS)

    var sortMode by remember { mutableStateOf(BookSortMode.STANDARD) }
    var sortReversed by remember { mutableStateOf(false) }

    LaunchedEffect(isGameCategory) {
        if (!isGameCategory && sortMode == BookSortMode.PLAYTIME) {
            sortMode = BookSortMode.STANDARD
            sortReversed = false
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<BookItem?>(null) }
    var detailItemId by remember { mutableStateOf<Long?>(null) }
    var adjustTimeItemId by remember { mutableStateOf<Long?>(null) }
    var editNotesItemId by remember { mutableStateOf<Long?>(null) }
    val startMediaPlay = rememberMediaPlayStarter(viewModel, context)

    val availableSortModes = remember(isGameCategory) {
        if (isGameCategory) BookSortMode.entries else BookSortMode.entries.filter { it != BookSortMode.PLAYTIME }
    }

    val sortedBooksList = remember(booksList, sortMode, sortReversed, ratingMode, gameRatingColors) {
        val baseSorted = when (sortMode) {
            BookSortMode.STANDARD -> booksList.sortedBy { it.sortOrder }
            BookSortMode.TITEL -> booksList.sortedWith { a, b -> NaturalOrderComparator.naturalCompare(a.title, b.title) }
            BookSortMode.AUTOR -> booksList.sortedWith { a, b -> NaturalOrderComparator.naturalCompare(a.author, b.author) }
            BookSortMode.RATING -> if (ratingMode == RatingMode.WORDS) {
                booksList.sortedBy { viewModel.getRatingWordRank(it.ratingWord) }
            } else {
                booksList.sortedByDescending { it.rating }
            }
            BookSortMode.PLAYTIME -> booksList.sortedByDescending { it.totalPlaytimeSeconds }
        }
        if (sortReversed) baseSorted.reversed() else baseSorted
    }

    val booksLazyListState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize().background(GameDarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            MediaHeaderBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                allTabTitle = strings.tabAllItems(displayName, allBooks.size),
                topTenTitle = strings.tabTopTen,
                accentColor = bookAccent,
                headerSurfaceColor = headerSurfaceColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(sortBarBgColor)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = strings.sortDesc,
                        tint = bookAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableSortModes) { mode ->
                            val isSelected = (sortMode == mode)
                            val chipLabel = when (mode) {
                                BookSortMode.STANDARD -> if (isSelected && sortReversed) strings.sortStandardReversed else strings.sortStandard
                                BookSortMode.TITEL -> if (isSelected && sortReversed) strings.sortNameZA else strings.sortNameAZ
                                BookSortMode.AUTOR -> if (isSelected && sortReversed) strings.sortAuthorZA else strings.sortAuthorAZ
                                BookSortMode.RATING -> if (isSelected && sortReversed) strings.sortRatingLowest else strings.sortRatingBest
                                BookSortMode.PLAYTIME -> if (isSelected && sortReversed) strings.sortPlaytimeLeast else strings.sortPlaytimeMost
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (sortMode == mode) sortReversed = !sortReversed
                                    else {
                                        sortMode = mode
                                        sortReversed = false
                                    }
                                },
                                label = { Text(chipLabel, fontSize = 12.sp) },
                                trailingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = if (sortReversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = strings.sortDesc,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = bookAccent.copy(alpha = 0.25f),
                                    selectedLabelColor = bookAccent,
                                    containerColor = Color(0xFF2C2013)
                                )
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                if (sortedBooksList.isEmpty()) {
                    EmptyMediaView(
                        title = if (searchQuery.isNotEmpty()) strings.emptySearchTitle else strings.emptyBooksTitle,
                        subtitle = if (searchQuery.isNotEmpty()) strings.emptySearchSubtitle else strings.emptyBooksSubtitle,
                        buttonText = if (searchQuery.isNotEmpty()) strings.clearSearch else strings.addFirstBook,
                        onButtonClick = {
                            if (searchQuery.isNotEmpty()) viewModel.setSearchQuery("") else showAddDialog = true
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = booksLazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 24.dp, top = 6.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(sortedBooksList, key = { it.id }, contentType = { "books" }) { item ->
                                val isRunning = isGameCategory &&
                                    runningTarget?.categoryKey == categoryKey &&
                                    runningTarget?.itemId == item.id
                                MediaRowCard(
                                    title = item.title,
                                    statusLabel = if (item.author.isNotBlank()) strings.authorPrefix(item.author) else strings.bookLabel,
                                    ratingMode = ratingMode,
                                    rating = item.rating,
                                    ratingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
                                    subtitle = item.notes,
                                    accentColor = bookAccent,
                                    indicatorColor = viewModel.getIndicatorColorForRating(ratingMode, item.ratingWord, item.rating, bookAccent),
                                    isGameCategory = isGameCategory,
                                    totalPlaytimeSeconds = item.totalPlaytimeSeconds,
                                    isPinnedToPlaytime = item.isPinnedToPlaytime,
                                    isTimerRunning = isRunning,
                                    elapsedSessionSeconds = if (isRunning) elapsedSessionSeconds else 0L,
                                    onTogglePin = if (isGameCategory) {
                                        { viewModel.toggleTargetPinned(categoryKey, item.id, !item.isPinnedToPlaytime) }
                                    } else null,
                                    onCardClick = {
                                        if (isGameCategory) detailItemId = item.id else itemToEdit = item
                                    },
                                    onEdit = { itemToEdit = item }
                                )
                            }
                        }

                        FastScrollbar(
                            lazyListState = booksLazyListState,
                            totalItemsCount = sortedBooksList.size,
                            accentColor = bookAccent,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(bottom = 80.dp),
                            labelProvider = { index ->
                                val b = sortedBooksList.getOrNull(index)
                                when (sortMode) {
                                    BookSortMode.TITEL -> b?.title?.take(1)?.uppercase() ?: ""
                                    BookSortMode.AUTOR -> b?.author?.take(1)?.uppercase() ?: ""
                                    BookSortMode.RATING -> if (ratingMode == RatingMode.WORDS) {
                                        b?.let { viewModel.resolveDisplayRatingWord(it.ratingWord).ifBlank { strings.unrated } } ?: ""
                                    } else {
                                        if (b != null && b.rating > 0) String.format(Locale.US, "%.1f", b.rating) else "-"
                                    }
                                    BookSortMode.PLAYTIME -> formatDurationHMS(b?.totalPlaytimeSeconds ?: 0L)
                                    else -> "#${index + 1}"
                                }
                            }
                        )
                    }
                }
            } else {
                TopTenScreen(
                    mediaType = categoryKey,
                    viewModel = viewModel,
                    availableTitles = allBooks.map { it.title }
                )
            }
        }

        if (selectedTab == 0) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = bookAccent,
                contentColor = Color(0xFF261000),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(strings.fabAddBook, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_book")
            )
        }
    }

    if (showAddDialog) {
        BookEditDialog(
            ratingMode = ratingMode,
            ratingWords = gameRatingColors,
            accentColor = bookAccent,
            isEdit = false,
            onSave = { title, author, rating, ratingWord, notes ->
                viewModel.addBook(title, author, rating, notes, ratingWord)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    itemToEdit?.let { item ->
        BookEditDialog(
            initialTitle = item.title,
            initialAuthor = item.author,
            initialRating = item.rating,
            initialRatingWord = viewModel.resolveDisplayRatingWord(item.ratingWord),
            initialNotes = item.notes,
            ratingMode = ratingMode,
            ratingWords = gameRatingColors,
            accentColor = bookAccent,
            isEdit = true,
            onSave = { title, author, rating, ratingWord, notes ->
                viewModel.updateBook(
                    item.copy(title = title, author = author, rating = rating, ratingWord = ratingWord, notes = notes)
                )
                itemToEdit = null
            },
            onDelete = {
                viewModel.deleteBook(item.id)
                if (detailItemId == item.id) detailItemId = null
                itemToEdit = null
            },
            onDismiss = { itemToEdit = null }
        )
    }

    detailItemId?.let { id ->
        val item = allBooks.firstOrNull { it.id == id }
        if (item == null) {
            detailItemId = null
        } else {
            val isRunning = runningTarget?.categoryKey == categoryKey && runningTarget?.itemId == item.id
            val snap = Game(
                id = item.id,
                title = item.title,
                totalPlaytimeSeconds = item.totalPlaytimeSeconds,
                colorHex = accentHex,
                notes = item.notes,
                rating = item.ratingWord,
                starRating = item.rating,
                isPinnedToPlaytime = item.isPinnedToPlaytime,
                categoryKey = categoryKey
            )
            GameDetailDialog(
                game = snap,
                isRunning = isRunning,
                elapsedSessionSeconds = if (isRunning) elapsedSessionSeconds else 0L,
                dailyRecords = dailyPlaytimeByMedia[categoryKey to item.id] ?: emptyList(),
                onTogglePlayPause = {
                    if (isRunning) {
                        viewModel.pauseGame(context)
                    } else {
                        startMediaPlay(PendingMediaPlay(categoryKey, item.id, item.title, item.notes, accentHex, item.totalPlaytimeSeconds))
                    }
                },
                onOpenAdjustTime = { adjustTimeItemId = item.id },
                onOpenEditNotes = { editNotesItemId = item.id },
                onOpenEditDetails = { itemToEdit = item },
                onDismiss = { detailItemId = null },
                indicatorColor = viewModel.getIndicatorColorForRating(ratingMode, item.ratingWord, item.rating, bookAccent)
            )
        }
    }

    adjustTimeItemId?.let { id ->
        allBooks.firstOrNull { it.id == id }?.let { item ->
            ManualAdjustTimeDialog(
                game = Game(id = item.id, title = item.title, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, categoryKey = categoryKey),
                onConfirm = { newSecs ->
                    viewModel.adjustTargetPlaytime(categoryKey, item.id, newSecs)
                    adjustTimeItemId = null
                },
                onDismiss = { adjustTimeItemId = null }
            )
        }
    }

    editNotesItemId?.let { id ->
        allBooks.firstOrNull { it.id == id }?.let { item ->
            EditNotesDialog(
                game = Game(id = item.id, title = item.title, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, notes = item.notes, categoryKey = categoryKey),
                onSaveNotes = { newNotes ->
                    viewModel.updateTargetNotes(categoryKey, item.id, newNotes)
                    editNotesItemId = null
                    Toast.makeText(context, strings.savedToast, Toast.LENGTH_SHORT).show()
                },
                onDismiss = { editNotesItemId = null }
            )
        }
    }
}

/**
 * Clean Material 3 Card row for media items supporting both Star Principle, Global Rating Words,
 * and optional PlayTime tracking / pinning when configured as a Game Category.
 */
@Composable
fun MediaRowCard(
    title: String,
    statusLabel: String,
    rating: Double,
    subtitle: String,
    accentColor: Color,
    indicatorColor: Color? = null,
    ratingMode: RatingMode = RatingMode.STARS,
    ratingWord: String = "",
    isGameCategory: Boolean = false,
    totalPlaytimeSeconds: Long = 0L,
    isPinnedToPlaytime: Boolean = false,
    isTimerRunning: Boolean = false,
    elapsedSessionSeconds: Long = 0L,
    onTogglePin: (() -> Unit)? = null,
    onCardClick: (() -> Unit)? = null,
    onEdit: () -> Unit
) {
    val strings = LocalAppStrings.current
    val baseBorderColor = indicatorColor ?: accentColor
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTimerRunning) GameDarkSurfaceHighlight else GameDarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isTimerRunning) 2.dp else 1.dp,
            color = if (isTimerRunning) NeonGreen else baseBorderColor.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { (onCardClick ?: onEdit).invoke() }
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
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isTimerRunning) NeonGreen else baseBorderColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        ),
                        color = TextPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isFinished = statusLabel.contains("Finished", ignoreCase = true) ||
                            statusLabel.contains(strings.statusFinished, ignoreCase = true)
                        val isQuit = statusLabel.contains("Quit", ignoreCase = true) ||
                            statusLabel.contains("Dropped", ignoreCase = true) ||
                            statusLabel.contains(strings.statusQuit, ignoreCase = true)
                        val statusBg = when {
                            isFinished -> NeonGreen.copy(alpha = 0.15f)
                            isQuit -> NeonRed.copy(alpha = 0.15f)
                            else -> GameDarkSurfaceVariant
                        }
                        val statusTextColor = when {
                            isFinished -> NeonGreen
                            isQuit -> NeonRed
                            else -> TextSecondary
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusBg
                        ) {
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusTextColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (isGameCategory) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isTimerRunning) NeonGreen.copy(alpha = 0.15f) else GameDarkSurfaceVariant
                            ) {
                                Text(
                                    text = formatDurationHMS(totalPlaytimeSeconds + if (isTimerRunning) elapsedSessionSeconds else 0L),
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isTimerRunning) NeonGreen else NeonCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }

                        if (subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            val badgeColor = indicatorColor ?: NeonAmber
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                ) {
                    if (ratingMode == RatingMode.WORDS) {
                        Text(
                            text = ratingWord.ifBlank { "-" },
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = badgeColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (rating > 0.0) String.format(Locale.US, "%.1f", rating) else "-",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = badgeColor
                            )
                        }
                    }
                }

                if (isGameCategory && onTogglePin != null) {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (isPinnedToPlaytime) strings.unpinFromPlayTimeDesc else strings.pinToPlayTimeDesc,
                            tint = if (isPinnedToPlaytime) NeonCyan else TextTertiary.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(2.dp))
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = TextTertiary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyMediaView(
    title: String,
    subtitle: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = GameDarkSurfaceVariant,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF003844),
                contentColor = NeonCyan
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(buttonText, fontWeight = FontWeight.Bold)
        }
    }
}
