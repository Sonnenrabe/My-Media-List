package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class GameRepository(
    private val gameDao: GameDao,
    private val mediaDao: MediaDao? = null
) {
    val activeGames: Flow<List<Game>> = gameDao.getActiveGames()
    val allActiveGamesAllCategories: Flow<List<Game>> = gameDao.getAllActiveGamesAllCategories()
    val archivedGames: Flow<List<Game>> = gameDao.getArchivedGames()
    val pinnedGames: Flow<List<Game>> = gameDao.getPinnedGames()

    fun getGamesForCategory(categoryKey: String): Flow<List<Game>> = gameDao.getGamesForCategory(categoryKey)

    fun getGameById(id: Long): Flow<Game?> = gameDao.getGameById(id)

    suspend fun getGameByIdDirect(id: Long): Game? = gameDao.getGameByIdDirect(id)

    suspend fun getAllGamesDirect(): List<Game> = gameDao.getAllGamesDirect()

    suspend fun insertGame(game: Game): Long = gameDao.insertGame(game)

    suspend fun insertAll(games: List<Game>) = gameDao.insertAll(games)

    suspend fun updateGame(game: Game) = gameDao.updateGame(game)

    suspend fun updateAll(games: List<Game>) = gameDao.updateAll(games)

    suspend fun deleteGame(game: Game) = gameDao.deleteGame(game)

    suspend fun deleteGameById(id: Long) = gameDao.deleteGameById(id)

    suspend fun deleteGamesByCategory(categoryKey: String) = gameDao.deleteGamesByCategory(categoryKey)

    suspend fun clearAllGames() = gameDao.clearAllGames()

    suspend fun setArchivedStatus(id: Long, isArchived: Boolean) {
        gameDao.setArchivedStatus(id, isArchived, System.currentTimeMillis())
    }

    suspend fun updatePlaytime(id: Long, playtimeSeconds: Long) {
        gameDao.updatePlaytime(id, playtimeSeconds, System.currentTimeMillis())
    }

    suspend fun updateNotes(id: Long, notes: String) {
        gameDao.updateNotes(id, notes)
    }

    suspend fun updateSortOrder(id: Long, sortOrder: Int) {
        gameDao.updateSortOrder(id, sortOrder)
    }

    suspend fun updatePinnedStatus(id: Long, isPinned: Boolean) {
        gameDao.updatePinnedStatus(id, isPinned)
    }

    suspend fun updateGameRating(id: Long, rating: String) {
        gameDao.updateRating(id, rating)
    }

    // ==========================================
    // 30-Day Rolling Daily Playtime Tracking
    // ==========================================

    fun get30DayDailyPlaytimeForGame(gameId: Long): Flow<List<DailyPlaytimeRecord>> {
        val minEpochDay = DailyDateHelper.getLocalEpochDay() - 29
        return gameDao.getDailyPlaytimeForGame(gameId, minEpochDay)
    }

    fun getAll30DayDailyPlaytime(): Flow<List<DailyPlaytimeRecord>> {
        val minEpochDay = DailyDateHelper.getLocalEpochDay() - 29
        return gameDao.getAllDailyPlaytime(minEpochDay)
    }

    suspend fun getAll30DayDailyPlaytimeDirect(): List<DailyPlaytimeRecord> {
        val minEpochDay = DailyDateHelper.getLocalEpochDay() - 29
        return gameDao.getAllDailyPlaytimeDirect(minEpochDay)
    }

    /**
     * Records elapsed session seconds to today's daily log and prunes records older than 30 days.
     * Note: Total cumulative playtime on the Game entity is updated separately and never lost.
     */
    suspend fun recordSessionPlaytime(gameId: Long, sessionSeconds: Long) {
        if (sessionSeconds <= 0L) return
        val todayEpoch = DailyDateHelper.getLocalEpochDay()
        val dateString = DailyDateHelper.formatIsoDate(todayEpoch)
        gameDao.addPlaytimeForDay(
            gameId = gameId,
            dateEpochDay = todayEpoch,
            dateString = dateString,
            addedSeconds = sessionSeconds
        )
        // Clean up records older than 30 days to keep data compact
        pruneExpiredDailyPlaytime()
    }

    /**
     * Prunes any daily playtime records older than 30 days from the database.
     */
    suspend fun pruneExpiredDailyPlaytime(): Int {
        val cutoffEpochDay = DailyDateHelper.getLocalEpochDay() - 29
        return gameDao.deleteOldDailyRecords(cutoffEpochDay)
    }

    suspend fun insertDailyRecords(records: List<DailyPlaytimeRecord>) {
        gameDao.insertAllDailyRecords(records)
    }

    suspend fun importGames(games: List<Game>, replaceExisting: Boolean) {
        if (replaceExisting) {
            gameDao.clearAllGames()
        }
        gameDao.insertAll(games)
    }

    // ==========================================
    // SERIEN
    // ==========================================
    val allSeries: Flow<List<SeriesItem>> = mediaDao?.getAllSeries() ?: emptyFlow()
    suspend fun getAllSeriesDirect(): List<SeriesItem> = mediaDao?.getAllSeriesDirect() ?: emptyList()
    suspend fun insertSeries(item: SeriesItem): Long = mediaDao?.insertSeries(item) ?: 0L
    suspend fun updateSeries(item: SeriesItem) { mediaDao?.updateSeries(item) }
    suspend fun deleteSeries(id: Long) { mediaDao?.deleteSeriesById(id) }
    suspend fun importSeries(items: List<SeriesItem>, replaceExisting: Boolean) {
        if (replaceExisting) mediaDao?.clearAllSeries()
        mediaDao?.insertAllSeries(items)
    }

    // ==========================================
    // ANIME
    // ==========================================
    val allAnime: Flow<List<AnimeItem>> = mediaDao?.getAllAnime() ?: emptyFlow()
    suspend fun getAllAnimeDirect(): List<AnimeItem> = mediaDao?.getAllAnimeDirect() ?: emptyList()
    suspend fun insertAnime(item: AnimeItem): Long = mediaDao?.insertAnime(item) ?: 0L
    suspend fun updateAnime(item: AnimeItem) { mediaDao?.updateAnime(item) }
    suspend fun deleteAnime(id: Long) { mediaDao?.deleteAnimeById(id) }
    suspend fun importAnime(items: List<AnimeItem>, replaceExisting: Boolean) {
        if (replaceExisting) mediaDao?.clearAllAnime()
        mediaDao?.insertAllAnime(items)
    }

    // ==========================================
    // MANGA
    // ==========================================
    val allManga: Flow<List<MangaItem>> = mediaDao?.getAllManga() ?: emptyFlow()
    suspend fun getAllMangaDirect(): List<MangaItem> = mediaDao?.getAllMangaDirect() ?: emptyList()
    suspend fun insertManga(item: MangaItem): Long = mediaDao?.insertManga(item) ?: 0L
    suspend fun updateManga(item: MangaItem) { mediaDao?.updateManga(item) }
    suspend fun deleteManga(id: Long) { mediaDao?.deleteMangaById(id) }
    suspend fun importManga(items: List<MangaItem>, replaceExisting: Boolean) {
        if (replaceExisting) mediaDao?.clearAllManga()
        mediaDao?.insertAllManga(items)
    }

    // ==========================================
    // BÜCHER
    // ==========================================
    val allBooks: Flow<List<BookItem>> = mediaDao?.getAllBooks() ?: emptyFlow()
    suspend fun getAllBooksDirect(): List<BookItem> = mediaDao?.getAllBooksDirect() ?: emptyList()
    suspend fun insertBook(item: BookItem): Long = mediaDao?.insertBook(item) ?: 0L
    suspend fun updateBook(item: BookItem) { mediaDao?.updateBook(item) }
    suspend fun deleteBook(id: Long) { mediaDao?.deleteBookById(id) }
    suspend fun importBooks(items: List<BookItem>, replaceExisting: Boolean) {
        if (replaceExisting) mediaDao?.clearAllBooks()
        mediaDao?.insertAllBooks(items)
    }

    // ==========================================
    // TOP 10
    // ==========================================
    fun getTopTenEntries(mediaType: String, categoryName: String): Flow<List<TopTenEntry>> {
        return mediaDao?.getTopTenEntries(mediaType, categoryName) ?: emptyFlow()
    }

    fun getTopTenCategories(mediaType: String): Flow<List<String>> {
        return mediaDao?.getCategoriesForMediaType(mediaType) ?: emptyFlow()
    }

    suspend fun getAllTopTenDirect(): List<TopTenEntry> = mediaDao?.getAllTopTenEntriesDirect() ?: emptyList()

    suspend fun saveTopTenEntry(entry: TopTenEntry): Long = mediaDao?.insertTopTenEntry(entry) ?: 0L

    suspend fun deleteTopTenEntry(id: Long) { mediaDao?.deleteTopTenEntryById(id) }

    suspend fun clearTopTenCategory(mediaType: String, categoryName: String) {
        mediaDao?.clearTopTenForCategory(mediaType, categoryName)
    }

    suspend fun deleteTopTenByMediaType(mediaType: String) {
        mediaDao?.deleteTopTenByMediaType(mediaType)
    }

    suspend fun importTopTen(entries: List<TopTenEntry>, replaceExisting: Boolean) {
        if (replaceExisting) mediaDao?.clearAllTopTen()
        mediaDao?.insertAllTopTen(entries)
    }

    // ==========================================
    // Unified Category Playtime & Target Helpers
    // ==========================================

    fun getAll30DayMediaDailyPlaytime(): Flow<List<MediaDailyPlaytimeRecord>> {
        val minEpochDay = DailyDateHelper.getLocalEpochDay() - 29
        return mediaDao?.getAllMediaDailyPlaytime(minEpochDay) ?: emptyFlow()
    }

    suspend fun recordMediaSessionPlaytime(mediaType: String, itemId: Long, sessionSeconds: Long) {
        if (sessionSeconds <= 0L || mediaDao == null) return
        val todayEpoch = DailyDateHelper.getLocalEpochDay()
        val dateString = DailyDateHelper.formatIsoDate(todayEpoch)
        mediaDao.addMediaPlaytimeForDay(
            mediaType = mediaType,
            itemId = itemId,
            dateEpochDay = todayEpoch,
            dateString = dateString,
            addedSeconds = sessionSeconds
        )
        val cutoffEpochDay = DailyDateHelper.getLocalEpochDay() - 29
        mediaDao.deleteOldMediaDailyRecords(cutoffEpochDay)
    }

    suspend fun addSessionPlaytimeToTarget(categoryKey: String, itemId: Long, sessionSeconds: Long) {
        if (sessionSeconds <= 0L) return
        val now = System.currentTimeMillis()
        when (categoryKey) {
            AppColorSettingsManager.KEY_SERIEN -> {
                val item = mediaDao?.getSeriesByIdDirect(itemId) ?: return
                mediaDao.updateSeriesPlaytime(itemId, item.totalPlaytimeSeconds + sessionSeconds, now)
                recordMediaSessionPlaytime(categoryKey, itemId, sessionSeconds)
            }
            AppColorSettingsManager.KEY_ANIME -> {
                val item = mediaDao?.getAnimeByIdDirect(itemId) ?: return
                mediaDao.updateAnimePlaytime(itemId, item.totalPlaytimeSeconds + sessionSeconds, now)
                recordMediaSessionPlaytime(categoryKey, itemId, sessionSeconds)
            }
            AppColorSettingsManager.KEY_MANGA -> {
                val item = mediaDao?.getMangaByIdDirect(itemId) ?: return
                mediaDao.updateMangaPlaytime(itemId, item.totalPlaytimeSeconds + sessionSeconds, now)
                recordMediaSessionPlaytime(categoryKey, itemId, sessionSeconds)
            }
            AppColorSettingsManager.KEY_BUECHER -> {
                val item = mediaDao?.getBookByIdDirect(itemId) ?: return
                mediaDao.updateBookPlaytime(itemId, item.totalPlaytimeSeconds + sessionSeconds, now)
                recordMediaSessionPlaytime(categoryKey, itemId, sessionSeconds)
            }
            else -> {
                val game = gameDao.getGameByIdDirect(itemId) ?: return
                gameDao.updatePlaytime(itemId, game.totalPlaytimeSeconds + sessionSeconds, now)
                recordSessionPlaytime(itemId, sessionSeconds)
            }
        }
    }

    suspend fun updateTargetPlaytime(categoryKey: String, itemId: Long, newTotalSeconds: Long) {
        val safe = newTotalSeconds.coerceAtLeast(0L)
        val now = System.currentTimeMillis()
        when (categoryKey) {
            AppColorSettingsManager.KEY_SERIEN -> mediaDao?.updateSeriesPlaytime(itemId, safe, now)
            AppColorSettingsManager.KEY_ANIME -> mediaDao?.updateAnimePlaytime(itemId, safe, now)
            AppColorSettingsManager.KEY_MANGA -> mediaDao?.updateMangaPlaytime(itemId, safe, now)
            AppColorSettingsManager.KEY_BUECHER -> mediaDao?.updateBookPlaytime(itemId, safe, now)
            else -> gameDao.updatePlaytime(itemId, safe, now)
        }
    }

    suspend fun updateTargetPinned(categoryKey: String, itemId: Long, isPinned: Boolean) {
        when (categoryKey) {
            AppColorSettingsManager.KEY_SERIEN -> mediaDao?.updateSeriesPinned(itemId, isPinned)
            AppColorSettingsManager.KEY_ANIME -> mediaDao?.updateAnimePinned(itemId, isPinned)
            AppColorSettingsManager.KEY_MANGA -> mediaDao?.updateMangaPinned(itemId, isPinned)
            AppColorSettingsManager.KEY_BUECHER -> mediaDao?.updateBookPinned(itemId, isPinned)
            else -> gameDao.updatePinnedStatus(itemId, isPinned)
        }
    }

    suspend fun updateTargetNotes(categoryKey: String, itemId: Long, notes: String) {
        when (categoryKey) {
            AppColorSettingsManager.KEY_SERIEN -> mediaDao?.updateSeriesNotes(itemId, notes)
            AppColorSettingsManager.KEY_ANIME -> mediaDao?.updateAnimeNotes(itemId, notes)
            AppColorSettingsManager.KEY_MANGA -> mediaDao?.updateMangaNotes(itemId, notes)
            AppColorSettingsManager.KEY_BUECHER -> mediaDao?.updateBookNotes(itemId, notes)
            else -> gameDao.updateNotes(itemId, notes)
        }
    }

    suspend fun deleteTargetItem(categoryKey: String, itemId: Long) {
        when (categoryKey) {
            AppColorSettingsManager.KEY_SERIEN -> {
                mediaDao?.deleteSeriesById(itemId)
                mediaDao?.deleteMediaDailyRecordsForItem(categoryKey, itemId)
            }
            AppColorSettingsManager.KEY_ANIME -> {
                mediaDao?.deleteAnimeById(itemId)
                mediaDao?.deleteMediaDailyRecordsForItem(categoryKey, itemId)
            }
            AppColorSettingsManager.KEY_MANGA -> {
                mediaDao?.deleteMangaById(itemId)
                mediaDao?.deleteMediaDailyRecordsForItem(categoryKey, itemId)
            }
            AppColorSettingsManager.KEY_BUECHER -> {
                mediaDao?.deleteBookById(itemId)
                mediaDao?.deleteMediaDailyRecordsForItem(categoryKey, itemId)
            }
            else -> gameDao.deleteGameById(itemId)
        }
    }
}
