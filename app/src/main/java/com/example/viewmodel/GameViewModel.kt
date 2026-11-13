package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AnimeItem
import com.example.data.BookItem
import com.example.data.DailyPlaytimeRecord
import com.example.data.DataExportImportManager
import com.example.data.FullBackupData
import com.example.data.FullBackupSummary
import com.example.data.Game
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.data.MangaItem
import com.example.data.SeriesItem
import com.example.data.TopTenEntry
import com.example.notification.GameTimerSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import androidx.compose.ui.graphics.Color
import com.example.data.AppColorSettingsManager
import com.example.data.AutoBackupManager
import com.example.data.CategoryColorConfig
import com.example.data.GameRatingColor
import com.example.data.MediaDailyPlaytimeRecord
import com.example.data.MediaRatingRange
import com.example.data.RatingMode
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.AppStringsProvider
import com.example.notification.TimerTarget
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination(val title: String) {
    GAMES("Games"),
    SERIES("Series"),
    ANIME("Anime"),
    MANGA("Manga"),
    BOOKS("Books"),
    CUSTOM_CATEGORY("Category"),
    PLAYTIME("PlayTime"),
    BACKUP("Backup & Export"),
    SETTINGS("Settings")
}

data class PlayableEntry(
    val categoryKey: String,
    val id: Long,
    val title: String,
    val notes: String,
    val ratingWord: String,
    val starRating: Double,
    val totalPlaytimeSeconds: Long,
    val colorHex: Long,
    val isPinnedToPlaytime: Boolean,
    val ratingMode: RatingMode,
    val categoryDisplayName: String,
    val categoryAccentHex: Long,
    val subtitleLine: String = "",
    val asGameSnapshot: Game
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository
    val colorSettings = AppColorSettingsManager(application)

    val appLanguage: StateFlow<AppLanguage> = colorSettings.appLanguage
    val categoryColors: StateFlow<Map<String, CategoryColorConfig>> = colorSettings.categoryColors
    val orderedCategoryKeys: StateFlow<List<String>> = colorSettings.orderedCategoryKeys
    val gameRatingColors: StateFlow<List<GameRatingColor>> = colorSettings.gameRatingColors
    val mediaRatingRanges: StateFlow<List<MediaRatingRange>> = colorSettings.mediaRatingRanges

    private val _autoBackupEnabled = MutableStateFlow(AutoBackupManager.isAutoBackupEnabled(application))
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled.asStateFlow()

    fun setAutoBackupEnabled(enabled: Boolean) {
        AutoBackupManager.setAutoBackupEnabled(getApplication(), enabled)
        _autoBackupEnabled.value = enabled
    }

    fun setAppLanguage(language: AppLanguage) {
        colorSettings.setAppLanguage(language)
    }

    fun isBuiltInCategory(key: String): Boolean = colorSettings.isBuiltInCategory(key)

    fun isGameCategory(key: String): Boolean = colorSettings.isGameCategory(key)

    fun getCategoryRatingMode(key: String): RatingMode =
        colorSettings.getCategoryRatingMode(key)

    fun getCategoryDisplayName(key: String, strings: AppStrings): String =
        colorSettings.getCategoryDisplayName(key, strings)

    fun getRatingWordRank(rating: String): Int =
        colorSettings.getRatingWordRank(rating)

    fun resolveDisplayRatingWord(rawWord: String): String =
        colorSettings.resolveDisplayRatingWord(rawWord)

    fun getColorForGameRating(rating: String, fallback: Color): Color =
        colorSettings.getColorForGameRating(rating, fallback)

    fun getColorForMediaScore(score: Double, fallback: Color): Color =
        colorSettings.getColorForMediaScore(score, fallback)

    fun getIndicatorColorForRating(
        ratingMode: RatingMode,
        wordRating: String,
        starRating: Double,
        fallback: Color
    ): Color {
        return when (ratingMode) {
            RatingMode.WORDS -> colorSettings.getColorForGameRating(wordRating, fallback)
            RatingMode.STARS -> colorSettings.getColorForMediaScore(starRating, fallback)
        }
    }

    fun updateCategoryColor(key: String, accentHex: Long, textHex: Long = 0xFFF8FAFC) {
        colorSettings.setCategoryColor(key, accentHex, textHex)
    }

    fun updateCategoryConfig(
        key: String,
        accentHex: Long,
        customName: String,
        ratingMode: RatingMode,
        isGameCategory: Boolean? = null
    ) {
        colorSettings.setCategoryConfig(
            key = key,
            accentHex = accentHex,
            customName = customName,
            ratingMode = ratingMode,
            isGameCategory = isGameCategory
        )
    }

    fun createCustomCategory(
        name: String,
        accentHex: Long,
        ratingMode: RatingMode,
        isGameCategory: Boolean
    ): String {
        return colorSettings.createCustomCategory(name, accentHex, ratingMode, isGameCategory)
    }

    fun deleteCustomCategory(categoryKey: String) {
        if (colorSettings.isBuiltInCategory(categoryKey)) return
        if (runningTarget.value?.categoryKey == categoryKey) {
            pauseGame()
        }
        colorSettings.deleteCustomCategory(categoryKey)
        if (_currentDestination.value == AppDestination.CUSTOM_CATEGORY && _selectedCustomCategoryKey.value == categoryKey) {
            _selectedCustomCategoryKey.value = null
            _currentDestination.value = AppDestination.GAMES
        }
        viewModelScope.launch {
            repository.deleteGamesByCategory(categoryKey)
            repository.deleteTopTenByMediaType(categoryKey)
        }
    }

    fun updateGameRatingColors(
        list: List<GameRatingColor>,
        renamedWord: Pair<String, String>? = null
    ) {
        colorSettings.setGameRatingColors(list)
        if (renamedWord != null) {
            val (oldWord, newWord) = renamedWord
            if (oldWord.isNotBlank() && newWord.isNotBlank() && oldWord != newWord) {
                viewModelScope.launch {
                    val matchingGames = repository.getAllGamesDirect()
                        .filter { it.rating.equals(oldWord, ignoreCase = true) }
                        .map { it.copy(rating = newWord) }
                    if (matchingGames.isNotEmpty()) {
                        repository.updateAll(matchingGames)
                    }
                    repository.getAllSeriesDirect()
                        .filter { it.ratingWord.equals(oldWord, ignoreCase = true) }
                        .forEach { repository.updateSeries(it.copy(ratingWord = newWord)) }
                    repository.getAllAnimeDirect()
                        .filter { it.ratingWord.equals(oldWord, ignoreCase = true) }
                        .forEach { repository.updateAnime(it.copy(ratingWord = newWord)) }
                    repository.getAllMangaDirect()
                        .filter { it.ratingWord.equals(oldWord, ignoreCase = true) }
                        .forEach { repository.updateManga(it.copy(ratingWord = newWord)) }
                    repository.getAllBooksDirect()
                        .filter { it.ratingWord.equals(oldWord, ignoreCase = true) }
                        .forEach { repository.updateBook(it.copy(ratingWord = newWord)) }
                }
            }
        }
    }

    fun updateMediaRatingRanges(list: List<MediaRatingRange>) {
        colorSettings.setMediaRatingRanges(list)
    }

    fun resetColorSettingsToDefaults() {
        colorSettings.resetToDefaults()
    }

    fun getCategoryAccent(destination: AppDestination, categoryKeyOverride: String? = null): Color {
        val key = categoryKeyOverride ?: when (destination) {
            AppDestination.GAMES -> AppColorSettingsManager.KEY_SPIELE
            AppDestination.SERIES -> AppColorSettingsManager.KEY_SERIEN
            AppDestination.ANIME -> AppColorSettingsManager.KEY_ANIME
            AppDestination.MANGA -> AppColorSettingsManager.KEY_MANGA
            AppDestination.BOOKS -> AppColorSettingsManager.KEY_BUECHER
            AppDestination.CUSTOM_CATEGORY -> _selectedCustomCategoryKey.value ?: AppColorSettingsManager.KEY_SPIELE
            AppDestination.PLAYTIME -> return Color(0xFF38BDF8)
            AppDestination.BACKUP -> return Color(0xFF818CF8)
            AppDestination.SETTINGS -> return Color(0xFF818CF8)
        }
        val hex = categoryColors.value[key]?.accentColorHex ?: 0xFF818CF8
        return Color(hex)
    }

    fun getCategoryText(destination: AppDestination, categoryKeyOverride: String? = null): Color {
        val key = categoryKeyOverride ?: when (destination) {
            AppDestination.GAMES -> AppColorSettingsManager.KEY_SPIELE
            AppDestination.SERIES -> AppColorSettingsManager.KEY_SERIEN
            AppDestination.ANIME -> AppColorSettingsManager.KEY_ANIME
            AppDestination.MANGA -> AppColorSettingsManager.KEY_MANGA
            AppDestination.BOOKS -> AppColorSettingsManager.KEY_BUECHER
            AppDestination.CUSTOM_CATEGORY -> _selectedCustomCategoryKey.value ?: AppColorSettingsManager.KEY_SPIELE
            else -> return Color(0xFFF8FAFC)
        }
        val hex = categoryColors.value[key]?.textColorHex ?: 0xFFF8FAFC
        return Color(hex)
    }

    fun getCategoryHeaderSurface(destination: AppDestination, categoryKeyOverride: String? = null): Color {
        val key = categoryKeyOverride ?: when (destination) {
            AppDestination.GAMES -> AppColorSettingsManager.KEY_SPIELE
            AppDestination.SERIES -> AppColorSettingsManager.KEY_SERIEN
            AppDestination.ANIME -> AppColorSettingsManager.KEY_ANIME
            AppDestination.MANGA -> AppColorSettingsManager.KEY_MANGA
            AppDestination.BOOKS -> AppColorSettingsManager.KEY_BUECHER
            AppDestination.CUSTOM_CATEGORY -> _selectedCustomCategoryKey.value ?: AppColorSettingsManager.KEY_SPIELE
            AppDestination.PLAYTIME -> return Color(0xFF131B2A)
            AppDestination.BACKUP -> return Color(0xFF17192A)
            AppDestination.SETTINGS -> return Color(0xFF17192A)
        }
        val hex = categoryColors.value[key]?.accentColorHex ?: 0xFF818CF8
        return AppColorSettingsManager.getHeaderSurfaceColor(hex)
    }

    fun getCategorySortBarBg(destination: AppDestination, categoryKeyOverride: String? = null): Color {
        val key = categoryKeyOverride ?: when (destination) {
            AppDestination.GAMES -> AppColorSettingsManager.KEY_SPIELE
            AppDestination.SERIES -> AppColorSettingsManager.KEY_SERIEN
            AppDestination.ANIME -> AppColorSettingsManager.KEY_ANIME
            AppDestination.MANGA -> AppColorSettingsManager.KEY_MANGA
            AppDestination.BOOKS -> AppColorSettingsManager.KEY_BUECHER
            AppDestination.CUSTOM_CATEGORY -> _selectedCustomCategoryKey.value ?: AppColorSettingsManager.KEY_SPIELE
            else -> return Color(0xFF101420)
        }
        val hex = categoryColors.value[key]?.accentColorHex ?: 0xFF818CF8
        return AppColorSettingsManager.getSortingBarBgColor(hex)
    }

    init {
        val database = GameDatabase.getInstance(application, viewModelScope)
        repository = GameRepository(database.gameDao(), database.mediaDao())
        viewModelScope.launch {
            repository.pruneExpiredDailyPlaytime()
        }
    }

    // Current Navigation Screen (from Drawer)
    private val _currentDestination = MutableStateFlow(AppDestination.GAMES)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _selectedCustomCategoryKey = MutableStateFlow<String?>(null)
    val selectedCustomCategoryKey: StateFlow<String?> = _selectedCustomCategoryKey.asStateFlow()

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    fun navigateToCustomCategory(categoryKey: String) {
        _selectedCustomCategoryKey.value = categoryKey
        _currentDestination.value = AppDestination.CUSTOM_CATEGORY
    }

    // Tab Selection within Screens:
    // For Games: 0 = Alle Spiele (Tabelle/Liste), 1 = Top 10, 2 = Archiv
    private val _gameSubTab = MutableStateFlow(0)
    val gameSubTab: StateFlow<Int> = _gameSubTab.asStateFlow()
    fun setGameSubTab(index: Int) { _gameSubTab.value = index }

    // For other media: 0 = Liste, 1 = Top 10
    private val _mediaSubTab = MutableStateFlow(0)
    val mediaSubTab: StateFlow<Int> = _mediaSubTab.asStateFlow()
    fun setMediaSubTab(index: Int) { _mediaSubTab.value = index }

    // Selected Top 10 Category for Games (e.g. "Singleplayer", "Multiplayer", etc.)
    private val _selectedGameTopTenCategory = MutableStateFlow("Singleplayer")
    val selectedGameTopTenCategory: StateFlow<String> = _selectedGameTopTenCategory.asStateFlow()
    fun setSelectedGameTopTenCategory(cat: String) { _selectedGameTopTenCategory.value = cat }

    // Running Timer State synced with GameTimerSessionManager & Ongoing Push Notification
    val runningTarget: StateFlow<TimerTarget?> = GameTimerSessionManager.runningTarget
    val runningGameId: StateFlow<Long?> = GameTimerSessionManager.runningGameId
    val elapsedSessionSeconds: StateFlow<Long> = GameTimerSessionManager.elapsedSessionSeconds

    // Search query for lists
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    // Raw streams from DB
    val allActiveGames: StateFlow<List<Game>> = repository.activeGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActiveGamesAllCategories: StateFlow<List<Game>> = repository.allActiveGamesAllCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCountsByCategory: StateFlow<Map<String, Int>> = allActiveGamesAllCategories
        .map { games -> games.groupingBy { it.categoryKey }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val allArchivedGames: StateFlow<List<Game>> = repository.archivedGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedGames: StateFlow<List<Game>> = repository.pinnedGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered lists for Games
    val filteredActiveGames: StateFlow<List<Game>> = combine(allActiveGames, searchQuery, appLanguage) { games, query, _ ->
        if (query.isBlank()) games else games.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true) ||
                it.rating.contains(query, ignoreCase = true) ||
                colorSettings.resolveDisplayRatingWord(it.rating).contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredArchivedGames: StateFlow<List<Game>> = combine(allArchivedGames, searchQuery) { games, query ->
        if (query.isBlank()) games else games.filter {
            it.title.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 30-Day Rolling Daily Playtime Streams
    val allDailyPlaytime: StateFlow<List<DailyPlaytimeRecord>> = repository.getAll30DayDailyPlaytime()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyPlaytimeByGame: StateFlow<Map<Long, List<DailyPlaytimeRecord>>> = allDailyPlaytime
        .map { records -> records.groupBy { it.gameId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val allMediaDailyPlaytime: StateFlow<List<MediaDailyPlaytimeRecord>> = repository.getAll30DayMediaDailyPlaytime()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyPlaytimeByMedia: StateFlow<Map<Pair<String, Long>, List<DailyPlaytimeRecord>>> = allMediaDailyPlaytime
        .map { records ->
            records.groupBy(
                keySelector = { it.mediaType to it.itemId },
                valueTransform = { it.toDailyPlaytimeRecord() }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun getGamesForCategory(categoryKey: String): Flow<List<Game>> = repository.getGamesForCategory(categoryKey)

    fun getFilteredGamesForCategory(categoryKey: String): Flow<List<Game>> {
        return combine(repository.getGamesForCategory(categoryKey), searchQuery, appLanguage) { games, query, _ ->
            if (query.isBlank()) games else games.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.notes.contains(query, ignoreCase = true) ||
                    it.rating.contains(query, ignoreCase = true) ||
                    colorSettings.resolveDisplayRatingWord(it.rating).contains(query, ignoreCase = true)
            }
        }
    }

    // ==========================================
    // Media Streams (Series, Anime, Manga, Books)
    // ==========================================
    val allSeries: StateFlow<List<SeriesItem>> = repository.allSeries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val filteredSeries: StateFlow<List<SeriesItem>> = combine(allSeries, searchQuery, appLanguage) { items, query, _ ->
        if (query.isBlank()) items else items.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true) ||
                it.ratingWord.contains(query, ignoreCase = true) ||
                colorSettings.resolveDisplayRatingWord(it.ratingWord).contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnime: StateFlow<List<AnimeItem>> = repository.allAnime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val filteredAnime: StateFlow<List<AnimeItem>> = combine(allAnime, searchQuery, appLanguage) { items, query, _ ->
        if (query.isBlank()) items else items.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true) ||
                it.ratingWord.contains(query, ignoreCase = true) ||
                colorSettings.resolveDisplayRatingWord(it.ratingWord).contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allManga: StateFlow<List<MangaItem>> = repository.allManga
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val filteredManga: StateFlow<List<MangaItem>> = combine(allManga, searchQuery, appLanguage) { items, query, _ ->
        if (query.isBlank()) items else items.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true) ||
                it.ratingWord.contains(query, ignoreCase = true) ||
                colorSettings.resolveDisplayRatingWord(it.ratingWord).contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBooks: StateFlow<List<BookItem>> = repository.allBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val filteredBooks: StateFlow<List<BookItem>> = combine(allBooks, searchQuery, appLanguage) { items, query, _ ->
        if (query.isBlank()) items else items.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true) ||
                it.ratingWord.contains(query, ignoreCase = true) ||
                colorSettings.resolveDisplayRatingWord(it.ratingWord).contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // Top 10 Streams
    // ==========================================
    fun getTopTenEntries(mediaType: String, categoryName: String): Flow<List<TopTenEntry>> {
        return repository.getTopTenEntries(mediaType, categoryName)
    }

    fun getTopTenCategories(mediaType: String): Flow<List<String>> {
        return repository.getTopTenCategories(mediaType)
    }

    fun saveTopTenEntry(mediaType: String, categoryName: String, rank: Int, title: String, subtitle: String = "") {
        viewModelScope.launch {
            repository.saveTopTenEntry(
                TopTenEntry(
                    mediaType = mediaType,
                    categoryName = categoryName,
                    rank = rank,
                    title = title.trim(),
                    subtitle = subtitle.trim()
                )
            )
        }
    }

    fun deleteTopTenEntry(id: Long) {
        viewModelScope.launch { repository.deleteTopTenEntry(id) }
    }

    fun deleteTopTenCategory(mediaType: String, categoryName: String) {
        viewModelScope.launch {
            repository.clearTopTenCategory(mediaType, categoryName)
        }
    }

    fun createTopTenCategory(mediaType: String, categoryName: String) {
        viewModelScope.launch {
            repository.saveTopTenEntry(
                TopTenEntry(
                    mediaType = mediaType,
                    categoryName = categoryName.trim(),
                    rank = 1,
                    title = "",
                    subtitle = ""
                )
            )
        }
    }

    // ==========================================
    // Game & Unified PlayTime Operations
    // ==========================================
    fun playGame(game: Game, context: android.content.Context = getApplication()) {
        GameTimerSessionManager.startGame(context, game, repository)
    }

    fun playTarget(
        categoryKey: String,
        itemId: Long,
        title: String,
        notes: String = "",
        colorHex: Long = 0xFF818CF8L,
        totalPlaytimeSeconds: Long = 0L,
        context: android.content.Context = getApplication()
    ) {
        GameTimerSessionManager.startItem(
            context = context,
            categoryKey = categoryKey,
            itemId = itemId,
            title = title,
            notes = notes,
            colorHex = colorHex,
            totalPlaytimeSeconds = totalPlaytimeSeconds,
            repository = repository
        )
    }

    fun pauseGame(context: android.content.Context = getApplication()) {
        GameTimerSessionManager.pauseGame(context, repository)
    }

    fun togglePlayPause(game: Game) {
        if (GameTimerSessionManager.isRunningTarget(game.categoryKey, game.id)) {
            pauseGame()
        } else {
            playGame(game)
        }
    }

    fun togglePlayPauseTarget(
        categoryKey: String,
        itemId: Long,
        title: String,
        notes: String = "",
        colorHex: Long = 0xFF818CF8L,
        totalPlaytimeSeconds: Long = 0L
    ) {
        if (GameTimerSessionManager.isRunningTarget(categoryKey, itemId)) {
            pauseGame()
        } else {
            playTarget(categoryKey, itemId, title, notes, colorHex, totalPlaytimeSeconds)
        }
    }

    fun toggleGamePinned(gameId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            repository.updatePinnedStatus(gameId, isPinned)
        }
    }

    fun toggleTargetPinned(categoryKey: String, itemId: Long, isPinned: Boolean) {
        viewModelScope.launch {
            repository.updateTargetPinned(categoryKey, itemId, isPinned)
        }
    }

    fun updateGameRating(gameId: Long, rating: String) {
        viewModelScope.launch {
            repository.updateGameRating(gameId, rating)
        }
    }

    fun reorderGames(reorderedList: List<Game>) {
        viewModelScope.launch {
            val updatedGames = reorderedList.mapIndexed { index, game ->
                game.copy(sortOrder = index)
            }
            repository.updateAll(updatedGames)
        }
    }

    fun adjustPlaytime(gameId: Long, newTotalSeconds: Long) {
        adjustTargetPlaytime(AppColorSettingsManager.KEY_SPIELE, gameId, newTotalSeconds)
    }

    fun adjustTargetPlaytime(categoryKey: String, itemId: Long, newTotalSeconds: Long) {
        viewModelScope.launch {
            if (GameTimerSessionManager.isRunningTarget(categoryKey, itemId)) {
                pauseGame()
            }
            repository.updateTargetPlaytime(categoryKey, itemId, newTotalSeconds.coerceAtLeast(0L))
        }
    }

    fun updateGameNotes(gameId: Long, notes: String) {
        updateTargetNotes(AppColorSettingsManager.KEY_SPIELE, gameId, notes)
    }

    fun updateTargetNotes(categoryKey: String, itemId: Long, notes: String) {
        viewModelScope.launch {
            repository.updateTargetNotes(categoryKey, itemId, notes.trim())
        }
    }

    fun deleteTargetItem(categoryKey: String, itemId: Long) {
        viewModelScope.launch {
            if (GameTimerSessionManager.isRunningTarget(categoryKey, itemId)) {
                pauseGame()
            }
            repository.deleteTargetItem(categoryKey, itemId)
        }
    }

    fun updateGameDetails(
        gameId: Long,
        newTitle: String,
        newRating: String,
        newColorHex: Long,
        isPinned: Boolean,
        context: android.content.Context = getApplication(),
        newStarRating: Double? = null
    ) {
        viewModelScope.launch {
            val game = repository.getGameByIdDirect(gameId) ?: return@launch
            val updated = game.copy(
                title = newTitle.trim(),
                rating = newRating,
                starRating = newStarRating ?: game.starRating,
                colorHex = newColorHex,
                isPinnedToPlaytime = isPinned
            )
            repository.updateGame(updated)
            if (GameTimerSessionManager.isRunningTarget(updated.categoryKey, gameId)) {
                GameTimerSessionManager.updateRunningGame(context, updated)
            }
        }
    }

    fun archiveGame(gameId: Long) {
        viewModelScope.launch {
            val game = repository.getGameByIdDirect(gameId)
            if (game != null && GameTimerSessionManager.isRunningTarget(game.categoryKey, gameId)) pauseGame()
            repository.setArchivedStatus(gameId, true)
        }
    }

    fun restoreGame(gameId: Long) {
        viewModelScope.launch {
            repository.setArchivedStatus(gameId, false)
        }
    }

    fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            val game = repository.getGameByIdDirect(gameId)
            if (game != null && GameTimerSessionManager.isRunningTarget(game.categoryKey, gameId)) pauseGame()
            repository.deleteGameById(gameId)
        }
    }

    fun addNewGame(
        title: String,
        initialHours: Long,
        initialMinutes: Long,
        initialSeconds: Long,
        rating: String = "",
        colorHex: Long = 0xFF00E5FF,
        isPinned: Boolean = true,
        notes: String = "",
        starRating: Double = 0.0,
        categoryKey: String = AppColorSettingsManager.KEY_SPIELE
    ) {
        viewModelScope.launch {
            val totalSeconds = (initialHours * 3600L) + (initialMinutes * 60L) + initialSeconds
            val existingMax = repository.getAllGamesDirect()
                .filter { it.categoryKey == categoryKey }
                .maxOfOrNull { it.sortOrder } ?: -1
            val newGame = Game(
                title = title.trim(),
                platform = "",
                genre = "",
                totalPlaytimeSeconds = totalSeconds.coerceAtLeast(0L),
                isArchived = false,
                colorHex = colorHex,
                createdAt = System.currentTimeMillis(),
                lastPlayedAt = null,
                notes = notes.trim(),
                sortOrder = existingMax + 1,
                rating = rating,
                starRating = starRating,
                isPinnedToPlaytime = isPinned,
                categoryKey = categoryKey
            )
            repository.insertGame(newGame)
        }
    }

    // Unified Playable Entries across all Game-enabled Categories for PlayTimeScreen
    val pinnedPlayableEntries: StateFlow<List<PlayableEntry>> = combine(
        pinnedGames,
        allSeries,
        allAnime,
        allManga,
        allBooks,
        categoryColors,
        appLanguage
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val pGames = args[0] as List<Game>
        @Suppress("UNCHECKED_CAST")
        val seriesList = args[1] as List<SeriesItem>
        @Suppress("UNCHECKED_CAST")
        val animeList = args[2] as List<AnimeItem>
        @Suppress("UNCHECKED_CAST")
        val mangaList = args[3] as List<MangaItem>
        @Suppress("UNCHECKED_CAST")
        val bookList = args[4] as List<BookItem>
        @Suppress("UNCHECKED_CAST")
        val catMap = args[5] as Map<String, CategoryColorConfig>
        val lang = args[6] as AppLanguage
        val strings = AppStringsProvider.get(lang)
        val entries = mutableListOf<PlayableEntry>()

        for (g in pGames) {
            val cfg = catMap[g.categoryKey]
            val isGameCat = cfg?.isGameCategory ?: (g.categoryKey == AppColorSettingsManager.KEY_SPIELE)
            if (isGameCat) {
                val accentHex = cfg?.accentColorHex ?: 0xFF818CF8L
                val mode = cfg?.ratingMode ?: RatingMode.WORDS
                val displayCat = colorSettings.getCategoryDisplayName(g.categoryKey, strings)
                entries.add(
                    PlayableEntry(
                        categoryKey = g.categoryKey,
                        id = g.id,
                        title = g.title,
                        notes = g.notes,
                        ratingWord = g.rating,
                        starRating = g.starRating,
                        totalPlaytimeSeconds = g.totalPlaytimeSeconds,
                        colorHex = g.colorHex,
                        isPinnedToPlaytime = true,
                        ratingMode = mode,
                        categoryDisplayName = displayCat,
                        categoryAccentHex = accentHex,
                        asGameSnapshot = g
                    )
                )
            }
        }

        val sCfg = catMap[AppColorSettingsManager.KEY_SERIEN]
        if (sCfg?.isGameCategory == true) {
            val displayCat = colorSettings.getCategoryDisplayName(AppColorSettingsManager.KEY_SERIEN, strings)
            for (s in seriesList) {
                if (s.isPinnedToPlaytime) {
                    entries.add(
                        PlayableEntry(
                            categoryKey = AppColorSettingsManager.KEY_SERIEN,
                            id = s.id,
                            title = s.name,
                            notes = s.notes,
                            ratingWord = s.ratingWord,
                            starRating = s.rating,
                            totalPlaytimeSeconds = s.totalPlaytimeSeconds,
                            colorHex = sCfg.accentColorHex,
                            isPinnedToPlaytime = true,
                            ratingMode = sCfg.ratingMode,
                            categoryDisplayName = displayCat,
                            categoryAccentHex = sCfg.accentColorHex,
                            subtitleLine = s.season,
                            asGameSnapshot = Game(
                                id = s.id,
                                title = s.name,
                                totalPlaytimeSeconds = s.totalPlaytimeSeconds,
                                colorHex = sCfg.accentColorHex,
                                notes = s.notes,
                                rating = s.ratingWord,
                                starRating = s.rating,
                                isPinnedToPlaytime = true,
                                categoryKey = AppColorSettingsManager.KEY_SERIEN
                            )
                        )
                    )
                }
            }
        }

        val aCfg = catMap[AppColorSettingsManager.KEY_ANIME]
        if (aCfg?.isGameCategory == true) {
            val displayCat = colorSettings.getCategoryDisplayName(AppColorSettingsManager.KEY_ANIME, strings)
            for (a in animeList) {
                if (a.isPinnedToPlaytime) {
                    entries.add(
                        PlayableEntry(
                            categoryKey = AppColorSettingsManager.KEY_ANIME,
                            id = a.id,
                            title = a.name,
                            notes = a.notes,
                            ratingWord = a.ratingWord,
                            starRating = a.rating,
                            totalPlaytimeSeconds = a.totalPlaytimeSeconds,
                            colorHex = aCfg.accentColorHex,
                            isPinnedToPlaytime = true,
                            ratingMode = aCfg.ratingMode,
                            categoryDisplayName = displayCat,
                            categoryAccentHex = aCfg.accentColorHex,
                            subtitleLine = a.season,
                            asGameSnapshot = Game(
                                id = a.id,
                                title = a.name,
                                totalPlaytimeSeconds = a.totalPlaytimeSeconds,
                                colorHex = aCfg.accentColorHex,
                                notes = a.notes,
                                rating = a.ratingWord,
                                starRating = a.rating,
                                isPinnedToPlaytime = true,
                                categoryKey = AppColorSettingsManager.KEY_ANIME
                            )
                        )
                    )
                }
            }
        }

        val mCfg = catMap[AppColorSettingsManager.KEY_MANGA]
        if (mCfg?.isGameCategory == true) {
            val displayCat = colorSettings.getCategoryDisplayName(AppColorSettingsManager.KEY_MANGA, strings)
            for (m in mangaList) {
                if (m.isPinnedToPlaytime) {
                    entries.add(
                        PlayableEntry(
                            categoryKey = AppColorSettingsManager.KEY_MANGA,
                            id = m.id,
                            title = m.title,
                            notes = m.notes,
                            ratingWord = m.ratingWord,
                            starRating = m.rating,
                            totalPlaytimeSeconds = m.totalPlaytimeSeconds,
                            colorHex = mCfg.accentColorHex,
                            isPinnedToPlaytime = true,
                            ratingMode = mCfg.ratingMode,
                            categoryDisplayName = displayCat,
                            categoryAccentHex = mCfg.accentColorHex,
                            subtitleLine = m.chapter,
                            asGameSnapshot = Game(
                                id = m.id,
                                title = m.title,
                                totalPlaytimeSeconds = m.totalPlaytimeSeconds,
                                colorHex = mCfg.accentColorHex,
                                notes = m.notes,
                                rating = m.ratingWord,
                                starRating = m.rating,
                                isPinnedToPlaytime = true,
                                categoryKey = AppColorSettingsManager.KEY_MANGA
                            )
                        )
                    )
                }
            }
        }

        val bCfg = catMap[AppColorSettingsManager.KEY_BUECHER]
        if (bCfg?.isGameCategory == true) {
            val displayCat = colorSettings.getCategoryDisplayName(AppColorSettingsManager.KEY_BUECHER, strings)
            for (b in bookList) {
                if (b.isPinnedToPlaytime) {
                    entries.add(
                        PlayableEntry(
                            categoryKey = AppColorSettingsManager.KEY_BUECHER,
                            id = b.id,
                            title = b.title,
                            notes = b.notes,
                            ratingWord = b.ratingWord,
                            starRating = b.rating,
                            totalPlaytimeSeconds = b.totalPlaytimeSeconds,
                            colorHex = bCfg.accentColorHex,
                            isPinnedToPlaytime = true,
                            ratingMode = bCfg.ratingMode,
                            categoryDisplayName = displayCat,
                            categoryAccentHex = bCfg.accentColorHex,
                            subtitleLine = b.author,
                            asGameSnapshot = Game(
                                id = b.id,
                                title = b.title,
                                totalPlaytimeSeconds = b.totalPlaytimeSeconds,
                                colorHex = bCfg.accentColorHex,
                                notes = b.notes,
                                rating = b.ratingWord,
                                starRating = b.rating,
                                isPinnedToPlaytime = true,
                                categoryKey = AppColorSettingsManager.KEY_BUECHER
                            )
                        )
                    )
                }
            }
        }

        entries
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val runningPlayableEntry: StateFlow<PlayableEntry?> = combine(
        runningTarget,
        pinnedPlayableEntries,
        allActiveGamesAllCategories,
        allSeries,
        allAnime,
        allManga,
        allBooks,
        categoryColors,
        appLanguage
    ) { args ->
        val target = args[0] as TimerTarget? ?: return@combine null
        @Suppress("UNCHECKED_CAST")
        val pinned = args[1] as List<PlayableEntry>
        pinned.firstOrNull { it.categoryKey == target.categoryKey && it.id == target.itemId }
            ?: run {
                @Suppress("UNCHECKED_CAST")
                val catMap = args[7] as Map<String, CategoryColorConfig>
                val lang = args[8] as AppLanguage
                val strings = AppStringsProvider.get(lang)
                val cfg = catMap[target.categoryKey]
                val displayCat = colorSettings.getCategoryDisplayName(target.categoryKey, strings)
                val accentHex = cfg?.accentColorHex ?: 0xFF818CF8L
                val mode = cfg?.ratingMode ?: RatingMode.STARS
                val runningSnap = GameTimerSessionManager.runningGame.value
                when (target.categoryKey) {
                    AppColorSettingsManager.KEY_SERIEN -> {
                        @Suppress("UNCHECKED_CAST")
                        val item = (args[3] as List<SeriesItem>).firstOrNull { it.id == target.itemId } ?: return@run null
                        PlayableEntry(target.categoryKey, item.id, item.name, item.notes, item.ratingWord, item.rating, item.totalPlaytimeSeconds, accentHex, item.isPinnedToPlaytime, mode, displayCat, accentHex, item.season,
                            Game(id = item.id, title = item.name, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, notes = item.notes, rating = item.ratingWord, starRating = item.rating, isPinnedToPlaytime = item.isPinnedToPlaytime, categoryKey = target.categoryKey))
                    }
                    AppColorSettingsManager.KEY_ANIME -> {
                        @Suppress("UNCHECKED_CAST")
                        val item = (args[4] as List<AnimeItem>).firstOrNull { it.id == target.itemId } ?: return@run null
                        PlayableEntry(target.categoryKey, item.id, item.name, item.notes, item.ratingWord, item.rating, item.totalPlaytimeSeconds, accentHex, item.isPinnedToPlaytime, mode, displayCat, accentHex, item.season,
                            Game(id = item.id, title = item.name, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, notes = item.notes, rating = item.ratingWord, starRating = item.rating, isPinnedToPlaytime = item.isPinnedToPlaytime, categoryKey = target.categoryKey))
                    }
                    AppColorSettingsManager.KEY_MANGA -> {
                        @Suppress("UNCHECKED_CAST")
                        val item = (args[5] as List<MangaItem>).firstOrNull { it.id == target.itemId } ?: return@run null
                        PlayableEntry(target.categoryKey, item.id, item.title, item.notes, item.ratingWord, item.rating, item.totalPlaytimeSeconds, accentHex, item.isPinnedToPlaytime, mode, displayCat, accentHex, item.chapter,
                            Game(id = item.id, title = item.title, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, notes = item.notes, rating = item.ratingWord, starRating = item.rating, isPinnedToPlaytime = item.isPinnedToPlaytime, categoryKey = target.categoryKey))
                    }
                    AppColorSettingsManager.KEY_BUECHER -> {
                        @Suppress("UNCHECKED_CAST")
                        val item = (args[6] as List<BookItem>).firstOrNull { it.id == target.itemId } ?: return@run null
                        PlayableEntry(target.categoryKey, item.id, item.title, item.notes, item.ratingWord, item.rating, item.totalPlaytimeSeconds, accentHex, item.isPinnedToPlaytime, mode, displayCat, accentHex, item.author,
                            Game(id = item.id, title = item.title, totalPlaytimeSeconds = item.totalPlaytimeSeconds, colorHex = accentHex, notes = item.notes, rating = item.ratingWord, starRating = item.rating, isPinnedToPlaytime = item.isPinnedToPlaytime, categoryKey = target.categoryKey))
                    }
                    else -> {
                        val g = (args[2] as List<Game>).firstOrNull { it.id == target.itemId } ?: runningSnap ?: return@run null
                        PlayableEntry(target.categoryKey, g.id, g.title, g.notes, g.rating, g.starRating, g.totalPlaytimeSeconds, g.colorHex, g.isPinnedToPlaytime, mode, displayCat, accentHex, "", g)
                    }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ==========================================
    // Series Operations
    // ==========================================
    fun addSeries(name: String, season: String, rating: Double, notes: String, ratingWord: String = "") {
        viewModelScope.launch {
            repository.insertSeries(
                SeriesItem(
                    name = name.trim(),
                    season = season.trim(),
                    rating = rating,
                    ratingWord = ratingWord,
                    notes = notes.trim(),
                    sortOrder = allSeries.value.size
                )
            )
        }
    }

    fun updateSeries(item: SeriesItem) {
        viewModelScope.launch { repository.updateSeries(item) }
    }

    fun deleteSeries(id: Long) {
        viewModelScope.launch { repository.deleteSeries(id) }
    }

    // ==========================================
    // Anime Operations
    // ==========================================
    fun addAnime(name: String, season: String, rating: Double, notes: String, ratingWord: String = "") {
        viewModelScope.launch {
            repository.insertAnime(
                AnimeItem(
                    name = name.trim(),
                    season = season.trim(),
                    rating = rating,
                    ratingWord = ratingWord,
                    notes = notes.trim(),
                    sortOrder = allAnime.value.size
                )
            )
        }
    }

    fun updateAnime(item: AnimeItem) {
        viewModelScope.launch { repository.updateAnime(item) }
    }

    fun deleteAnime(id: Long) {
        viewModelScope.launch { repository.deleteAnime(id) }
    }

    // ==========================================
    // Manga Operations
    // ==========================================
    fun addManga(title: String, chapter: String, rating: Double, notes: String, ratingWord: String = "") {
        viewModelScope.launch {
            repository.insertManga(
                MangaItem(
                    title = title.trim(),
                    chapter = chapter.trim(),
                    rating = rating,
                    ratingWord = ratingWord,
                    notes = notes.trim(),
                    sortOrder = allManga.value.size
                )
            )
        }
    }

    fun updateManga(item: MangaItem) {
        viewModelScope.launch { repository.updateManga(item) }
    }

    fun deleteManga(id: Long) {
        viewModelScope.launch { repository.deleteManga(id) }
    }

    // ==========================================
    // Books Operations
    // ==========================================
    fun addBook(title: String, author: String, rating: Double, notes: String, ratingWord: String = "") {
        viewModelScope.launch {
            repository.insertBook(
                BookItem(
                    title = title.trim(),
                    author = author.trim(),
                    rating = rating,
                    ratingWord = ratingWord,
                    notes = notes.trim(),
                    sortOrder = allBooks.value.size
                )
            )
        }
    }

    fun updateBook(item: BookItem) {
        viewModelScope.launch { repository.updateBook(item) }
    }

    fun deleteBook(id: Long) {
        viewModelScope.launch { repository.deleteBook(id) }
    }

    // ==========================================
    // Backup & Export / Import
    // ==========================================
    suspend fun getFullBackupData(): FullBackupData {
        val games = repository.getAllGamesDirect()
        val series = repository.getAllSeriesDirect()
        val anime = repository.getAllAnimeDirect()
        val manga = repository.getAllMangaDirect()
        val books = repository.getAllBooksDirect()
        val topTen = repository.getAllTopTenDirect()

        return FullBackupData(
            games = games,
            series = series,
            anime = anime,
            manga = manga,
            books = books,
            topTenEntries = topTen,
            categoryColors = colorSettings.categoryColors.value,
            gameRatingColors = colorSettings.gameRatingColors.value,
            mediaRatingRanges = colorSettings.mediaRatingRanges.value,
            appLanguage = colorSettings.appLanguage.value,
            autoBackupEnabled = AutoBackupManager.isAutoBackupEnabled(getApplication())
        )
    }

    suspend fun exportDataAsExcelCsv(): String {
        if (runningTarget.value != null) {
            pauseGame()
        }
        val data = getFullBackupData()
        return DataExportImportManager.exportToExcelCsv(data)
    }

    fun parseBackupSummary(content: String): Result<FullBackupSummary> {
        return DataExportImportManager.parseBackup(content)
    }

    fun importFullBackup(data: FullBackupData, replaceExisting: Boolean, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            if (runningTarget.value != null) {
                pauseGame()
            }
            if (data.games.isNotEmpty()) {
                repository.importGames(data.games, replaceExisting)
            }
            if (data.series.isNotEmpty()) {
                repository.importSeries(data.series, replaceExisting)
            }
            if (data.anime.isNotEmpty()) {
                repository.importAnime(data.anime, replaceExisting)
            }
            if (data.manga.isNotEmpty()) {
                repository.importManga(data.manga, replaceExisting)
            }
            if (data.books.isNotEmpty()) {
                repository.importBooks(data.books, replaceExisting)
            }
            if (data.topTenEntries.isNotEmpty()) {
                repository.importTopTen(data.topTenEntries, replaceExisting)
            }
            // Restore all settings if present in backup
            if (data.appLanguage != null) {
                colorSettings.setAppLanguage(data.appLanguage)
            }
            if (data.autoBackupEnabled != null) {
                setAutoBackupEnabled(data.autoBackupEnabled)
            }
            if (data.categoryColors.isNotEmpty()) {
                colorSettings.setAllCategoryColors(data.categoryColors)
            }
            if (data.gameRatingColors.isNotEmpty()) {
                colorSettings.setGameRatingColors(data.gameRatingColors, allowTranslateIfDefault = true)
            }
            if (data.mediaRatingRanges.isNotEmpty()) {
                colorSettings.setMediaRatingRanges(data.mediaRatingRanges)
            }
            val count = data.games.size + data.series.size + data.anime.size + data.manga.size + data.books.size
            onComplete(count)
        }
    }
}
