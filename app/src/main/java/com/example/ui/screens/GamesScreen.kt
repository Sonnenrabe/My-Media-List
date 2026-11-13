package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppColorSettingsManager
import com.example.data.Game
import com.example.data.NaturalOrderComparator
import com.example.data.RatingMode
import com.example.localization.LocalAppStrings
import com.example.ui.components.ActiveGameCard
import com.example.ui.components.AddGameDialog
import com.example.ui.components.EditGameDialog
import com.example.ui.components.EditNotesDialog
import com.example.ui.components.FastScrollbar
import com.example.ui.components.GameDetailDialog
import com.example.ui.components.ManualAdjustTimeDialog
import com.example.ui.components.TopTenScreen
import com.example.ui.components.rememberDragDropState
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.GameDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppDestination
import com.example.viewmodel.GameViewModel
import java.util.Locale

enum class GameSortMode {
    STANDARD,
    NAME,
    RATING,
    PLAYTIME
}

@Composable
fun GamesScreen(
    viewModel: GameViewModel,
    categoryKey: String = AppColorSettingsManager.KEY_SPIELE,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isBuiltInGames = (categoryKey == AppColorSettingsManager.KEY_SPIELE)
    val gameSubTab by viewModel.gameSubTab.collectAsStateWithLifecycle()
    val runningTarget by viewModel.runningTarget.collectAsStateWithLifecycle()
    val elapsedSessionSeconds by viewModel.elapsedSessionSeconds.collectAsStateWithLifecycle()

    val filteredGamesFlow = remember(categoryKey) { viewModel.getFilteredGamesForCategory(categoryKey) }
    val allGamesFlow = remember(categoryKey) { viewModel.getGamesForCategory(categoryKey) }
    val activeGames by filteredGamesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val allActiveGames by allGamesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val dailyPlaytimeByGame by viewModel.dailyPlaytimeByGame.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val categoryColors by viewModel.categoryColors.collectAsStateWithLifecycle()
    val gameRatingColors by viewModel.gameRatingColors.collectAsStateWithLifecycle()

    val gamesConfig = categoryColors[categoryKey]
    val gamesDisplayName = if (isBuiltInGames) {
        gamesConfig?.customName?.trim()?.takeIf { it.isNotEmpty() } ?: strings.navGames
    } else {
        viewModel.getCategoryDisplayName(categoryKey, strings)
    }
    val ratingMode = gamesConfig?.ratingMode ?: RatingMode.WORDS
    val isGameCategory = gamesConfig?.isGameCategory ?: isBuiltInGames

    var sortMode by remember(categoryKey) { mutableStateOf(GameSortMode.STANDARD) }
    var sortReversed by remember(categoryKey) { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(isGameCategory) {
        if (!isGameCategory && sortMode == GameSortMode.PLAYTIME) {
            sortMode = GameSortMode.STANDARD
            sortReversed = false
        }
    }

    var gameToAdjust by remember { mutableStateOf<Game?>(null) }
    var gameToEditNotes by remember { mutableStateOf<Game?>(null) }
    var gameToEditDetails by remember { mutableStateOf<Game?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGameForDetails by remember { mutableStateOf<Game?>(null) }
    var pendingGameToPlay by remember { mutableStateOf<Game?>(null) }

    // Sorted active games according to selected sort mode, category rating mode, and direction
    val sortedActiveGames = remember(activeGames, sortMode, sortReversed, ratingMode, gameRatingColors) {
        val baseSorted = when (sortMode) {
            GameSortMode.STANDARD -> activeGames.sortedBy { it.sortOrder }
            GameSortMode.NAME -> activeGames.sortedWith { a, b -> NaturalOrderComparator.naturalCompare(a.title, b.title) }
            GameSortMode.RATING -> if (ratingMode == RatingMode.WORDS) {
                activeGames.sortedBy { viewModel.getRatingWordRank(it.rating) }
            } else {
                activeGames.sortedByDescending { it.starRating }
            }
            GameSortMode.PLAYTIME -> activeGames.sortedByDescending { it.totalPlaytimeSeconds }
        }
        if (sortReversed) baseSorted.reversed() else baseSorted
    }

    val activeLazyListState = rememberLazyListState()
    val activeDragDropState = rememberDragDropState(
        lazyListState = activeLazyListState,
        items = sortedActiveGames,
        onReorder = { viewModel.reorderGames(it) }
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        pendingGameToPlay?.let {
            viewModel.playGame(it, context)
            pendingGameToPlay = null
        }
    }

    fun startPlayingWithNotificationCheck(game: Game) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                pendingGameToPlay = game
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        viewModel.playGame(game, context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameDarkBackground)
    ) {
        val categoryAccent = Color(gamesConfig?.accentColorHex ?: 0xFF818CF8L)
        val destinationForColors = if (isBuiltInGames) AppDestination.GAMES else AppDestination.CUSTOM_CATEGORY
        val categoryHeaderSurface = viewModel.getCategoryHeaderSurface(destinationForColors, categoryKeyOverride = categoryKey)
        val categorySortBarBg = viewModel.getCategorySortBarBg(destinationForColors, categoryKeyOverride = categoryKey)
        val availableSortModes = remember(isGameCategory) {
            if (isGameCategory) GameSortMode.entries else GameSortMode.entries.filter { it != GameSortMode.PLAYTIME }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Clean Category Header Bar (no outer border frame, small vertical divider between tabs)
            Surface(
                color = categoryHeaderSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TabRow(
                            selectedTabIndex = gameSubTab.coerceIn(0, 1),
                            containerColor = Color.Transparent,
                            contentColor = categoryAccent,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[gameSubTab.coerceIn(0, 1)]),
                                    color = categoryAccent,
                                    height = 3.dp
                                )
                            },
                            divider = {}
                        ) {
                            Tab(
                                selected = gameSubTab == 0,
                                onClick = { viewModel.setGameSubTab(0) },
                                text = {
                                    Text(
                                        text = strings.tabAllItems(gamesDisplayName, allActiveGames.size),
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = if (gameSubTab == 0) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = if (gameSubTab == 0) categoryAccent else TextSecondary
                                    )
                                }
                            )
                            Tab(
                                selected = gameSubTab == 1,
                                onClick = { viewModel.setGameSubTab(1) },
                                text = {
                                    Text(
                                        text = strings.tabTopTen,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = if (gameSubTab == 1) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = if (gameSubTab == 1) categoryAccent else TextSecondary
                                    )
                                }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(1.dp)
                                .height(20.dp)
                                .background(categoryAccent.copy(alpha = 0.35f))
                        )
                    }

                    if (gameSubTab == 0) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = categoryAccent.copy(alpha = 0.15f)
                        )
                        // Sorting Selector Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(categorySortBarBg)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = strings.sortDesc,
                                tint = categoryAccent,
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
                                        GameSortMode.STANDARD -> if (isSelected && sortReversed) strings.sortStandardReversed else strings.sortStandard
                                        GameSortMode.NAME -> if (isSelected && sortReversed) strings.sortNameZA else strings.sortNameAZ
                                        GameSortMode.RATING -> if (isSelected && sortReversed) strings.sortRatingLowest else strings.sortRatingBest
                                        GameSortMode.PLAYTIME -> if (isSelected && sortReversed) strings.sortPlaytimeLeast else strings.sortPlaytimeMost
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (sortMode == mode) {
                                                sortReversed = !sortReversed
                                            } else {
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
                                            selectedContainerColor = categoryAccent.copy(alpha = 0.25f),
                                            selectedLabelColor = categoryAccent,
                                            containerColor = Color(0xFF191C2E)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (gameSubTab == 0) {
                if (sortedActiveGames.isEmpty()) {
                    EmptyStateView(
                        title = when {
                            searchQuery.isNotEmpty() -> strings.emptySearchTitle
                            isBuiltInGames -> strings.emptyGamesTitle
                            else -> strings.emptyCustomCategoryTitle(gamesDisplayName)
                        },
                        subtitle = when {
                            searchQuery.isNotEmpty() -> strings.emptySearchSubtitle
                            isBuiltInGames -> strings.emptyGamesSubtitle
                            else -> strings.emptyCustomCategorySubtitle
                        },
                        buttonText = when {
                            searchQuery.isNotEmpty() -> strings.clearSearch
                            isBuiltInGames -> strings.addFirstGame
                            else -> strings.emptyCustomCategoryFirst(gamesDisplayName)
                        },
                        onButtonClick = {
                            if (searchQuery.isNotEmpty()) viewModel.setSearchQuery("") else showAddDialog = true
                        }
                    )
                } else {
                    val displayedActive = if (activeDragDropState.isDragging) activeDragDropState.displayedGames else sortedActiveGames
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = activeLazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 24.dp, top = 6.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayedActive, key = { it.id }, contentType = { "game_card" }) { game ->
                                val isRunning = isGameCategory &&
                                    runningTarget?.categoryKey == categoryKey &&
                                    runningTarget?.itemId == game.id
                                val isBeingDragged = (game.id == activeDragDropState.draggedGameId)

                                val cardModifier = if (isBeingDragged) {
                                    Modifier
                                        .zIndex(10f)
                                        .graphicsLayer {
                                            translationY = activeDragDropState.dragOffsetY
                                            scaleX = 1.02f
                                            scaleY = 1.02f
                                            shadowElevation = 16.dp.toPx()
                                        }
                                } else if (activeDragDropState.isDragging) {
                                    Modifier.animateItem()
                                } else {
                                    Modifier
                                }

                                val dragHandleModifier = if (sortMode == GameSortMode.STANDARD && !sortReversed) {
                                    Modifier.pointerInput(game.id) {
                                        detectVerticalDragGestures(
                                            onDragStart = { activeDragDropState.onDragStart(game) },
                                            onDragEnd = { activeDragDropState.onDragEnd() },
                                            onDragCancel = { activeDragDropState.onDragCancel() },
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                activeDragDropState.onDrag(dragAmount)
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }

                                ActiveGameCard(
                                    game = game,
                                    isRunning = isRunning,
                                    elapsedSessionSeconds = if (isRunning) elapsedSessionSeconds else 0L,
                                    isBeingDragged = isBeingDragged,
                                    modifier = cardModifier,
                                    ratingMode = ratingMode,
                                    displayRatingWord = viewModel.resolveDisplayRatingWord(game.rating),
                                    indicatorColor = viewModel.getIndicatorColorForRating(
                                        ratingMode = ratingMode,
                                        wordRating = game.rating,
                                        starRating = game.starRating,
                                        fallback = Color(0xFF475569)
                                    ),
                                    showPlaytimeUi = isGameCategory,
                                    dragHandleModifier = dragHandleModifier,
                                    onTogglePin = if (isGameCategory) {
                                        { viewModel.toggleGamePinned(game.id, !game.isPinnedToPlaytime) }
                                    } else null,
                                    onClick = {
                                        if (isGameCategory) selectedGameForDetails = game
                                        else gameToEditDetails = game
                                    },
                                    onLongClick = { gameToEditDetails = game }
                                )
                            }
                        }

                        FastScrollbar(
                            lazyListState = activeLazyListState,
                            totalItemsCount = displayedActive.size,
                            accentColor = categoryAccent,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(bottom = 80.dp),
                            labelProvider = { index ->
                                val g = displayedActive.getOrNull(index)
                                when (sortMode) {
                                    GameSortMode.NAME -> g?.title?.take(1)?.uppercase() ?: ""
                                    GameSortMode.RATING -> if (ratingMode == RatingMode.WORDS) {
                                        g?.let { viewModel.resolveDisplayRatingWord(it.rating).ifBlank { strings.unrated } } ?: ""
                                    } else {
                                        if (g != null && g.starRating > 0.0) String.format(Locale.US, "%.1f", g.starRating) else "-"
                                    }
                                    GameSortMode.PLAYTIME -> g?.readablePlaytime() ?: ""
                                    else -> "#${index + 1}"
                                }
                            }
                        )
                    }
                }
            } else {
                // TOP 10 TAB with multi-category support
                TopTenScreen(
                    mediaType = categoryKey,
                    viewModel = viewModel,
                    availableTitles = allActiveGames.map { it.title },
                    allowCustomCategories = true
                )
            }
        }

        // FAB to add item
        if (gameSubTab == 0) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = categoryAccent,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        text = if (isBuiltInGames) strings.fabAddGame else strings.fabAddEntry,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_game")
            )
        }
    }

    // Scrollable Game Details Window
    selectedGameForDetails?.let { detailGame ->
        val currentLiveGame = activeGames.find { it.id == detailGame.id } ?: detailGame
        val isRunning = runningTarget?.categoryKey == categoryKey && runningTarget?.itemId == currentLiveGame.id

        GameDetailDialog(
            game = currentLiveGame,
            isRunning = isRunning,
            elapsedSessionSeconds = if (isRunning) elapsedSessionSeconds else 0L,
            dailyRecords = dailyPlaytimeByGame[currentLiveGame.id] ?: emptyList(),
            onTogglePlayPause = {
                if (isRunning) {
                    viewModel.pauseGame(context)
                } else {
                    startPlayingWithNotificationCheck(currentLiveGame)
                }
            },
            onOpenAdjustTime = { gameToAdjust = currentLiveGame },
            onOpenEditNotes = { gameToEditNotes = currentLiveGame },
            onOpenEditDetails = { gameToEditDetails = currentLiveGame },
            onDismiss = { selectedGameForDetails = null },
            indicatorColor = viewModel.getIndicatorColorForRating(
                ratingMode = ratingMode,
                wordRating = currentLiveGame.rating,
                starRating = currentLiveGame.starRating,
                fallback = Color(currentLiveGame.colorHex)
            )
        )
    }

    // Edit Item Dialog (Houses safe deletion with confirmation)
    gameToEditDetails?.let { game ->
        EditGameDialog(
            game = game.copy(rating = viewModel.resolveDisplayRatingWord(game.rating)),
            ratingMode = ratingMode,
            ratingWords = gameRatingColors,
            showPlaytimeUi = isGameCategory,
            showNotesField = !isGameCategory,
            dialogTitleOverride = if (isBuiltInGames) null else strings.editEntryTitle(gamesDisplayName),
            onNotesSaved = { newNotes ->
                viewModel.updateGameNotes(game.id, newNotes)
            },
            onSave = { newTitle, newRating, newStarRating, newColorHex, isPinned ->
                viewModel.updateGameDetails(
                    gameId = game.id,
                    newTitle = newTitle,
                    newRating = newRating,
                    newColorHex = newColorHex,
                    isPinned = isPinned,
                    context = context,
                    newStarRating = newStarRating
                )
                gameToEditDetails = null
                Toast.makeText(context, strings.updatedToast(newTitle), Toast.LENGTH_SHORT).show()
            },
            onDelete = {
                viewModel.deleteGame(game.id)
                gameToEditDetails = null
                selectedGameForDetails = null
                Toast.makeText(context, strings.deletedToast(game.title), Toast.LENGTH_SHORT).show()
            },
            onDismiss = { gameToEditDetails = null }
        )
    }

    // Manual Adjust Playtime Dialog
    gameToAdjust?.let { game ->
        ManualAdjustTimeDialog(
            game = game,
            onConfirm = { newSeconds ->
                viewModel.adjustPlaytime(game.id, newSeconds)
                gameToAdjust = null
            },
            onDismiss = { gameToAdjust = null }
        )
    }

    // Edit Notes Dialog
    gameToEditNotes?.let { game ->
        EditNotesDialog(
            game = game,
            onSaveNotes = { newNotes ->
                viewModel.updateGameNotes(game.id, newNotes)
                gameToEditNotes = null
                Toast.makeText(context, strings.savedToast, Toast.LENGTH_SHORT).show()
            },
            onDismiss = { gameToEditNotes = null }
        )
    }

    // Add Item Dialog
    if (showAddDialog) {
        AddGameDialog(
            ratingMode = ratingMode,
            ratingWords = gameRatingColors,
            showPlaytimeUi = isGameCategory,
            dialogTitleOverride = if (isBuiltInGames) null else strings.addEntryTitle(gamesDisplayName),
            titleLabelOverride = if (isBuiltInGames) null else strings.nameTitleRequired,
            onAddGame = { title, h, m, s, rating, starRating, color, isPinned, notes ->
                viewModel.addNewGame(
                    title = title,
                    initialHours = h,
                    initialMinutes = m,
                    initialSeconds = s,
                    rating = rating,
                    colorHex = color,
                    isPinned = isPinned,
                    notes = notes,
                    starRating = starRating,
                    categoryKey = categoryKey
                )
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun EmptyStateView(
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
                    imageVector = Icons.Default.Gamepad,
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
