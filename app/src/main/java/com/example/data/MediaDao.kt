package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    // ==========================================
    // SERIEN (Series)
    // ==========================================
    @Query("SELECT * FROM series ORDER BY sortOrder ASC, id DESC")
    fun getAllSeries(): Flow<List<SeriesItem>>

    @Query("SELECT * FROM series ORDER BY sortOrder ASC, id DESC")
    suspend fun getAllSeriesDirect(): List<SeriesItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(item: SeriesItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSeries(items: List<SeriesItem>)

    @Update
    suspend fun updateSeries(item: SeriesItem)

    @Query("SELECT * FROM series WHERE id = :id")
    suspend fun getSeriesByIdDirect(id: Long): SeriesItem?

    @Query("UPDATE series SET totalPlaytimeSeconds = :playtimeSeconds, lastPlayedAt = :timestamp WHERE id = :id")
    suspend fun updateSeriesPlaytime(id: Long, playtimeSeconds: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE series SET isPinnedToPlaytime = :isPinned WHERE id = :id")
    suspend fun updateSeriesPinned(id: Long, isPinned: Boolean)

    @Query("UPDATE series SET notes = :notes WHERE id = :id")
    suspend fun updateSeriesNotes(id: Long, notes: String)

    @Query("DELETE FROM series WHERE id = :id")
    suspend fun deleteSeriesById(id: Long)

    @Query("DELETE FROM series")
    suspend fun clearAllSeries()

    // ==========================================
    // ANIME
    // ==========================================
    @Query("SELECT * FROM anime ORDER BY sortOrder ASC, id DESC")
    fun getAllAnime(): Flow<List<AnimeItem>>

    @Query("SELECT * FROM anime ORDER BY sortOrder ASC, id DESC")
    suspend fun getAllAnimeDirect(): List<AnimeItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(item: AnimeItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAnime(items: List<AnimeItem>)

    @Update
    suspend fun updateAnime(item: AnimeItem)

    @Query("SELECT * FROM anime WHERE id = :id")
    suspend fun getAnimeByIdDirect(id: Long): AnimeItem?

    @Query("UPDATE anime SET totalPlaytimeSeconds = :playtimeSeconds, lastPlayedAt = :timestamp WHERE id = :id")
    suspend fun updateAnimePlaytime(id: Long, playtimeSeconds: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE anime SET isPinnedToPlaytime = :isPinned WHERE id = :id")
    suspend fun updateAnimePinned(id: Long, isPinned: Boolean)

    @Query("UPDATE anime SET notes = :notes WHERE id = :id")
    suspend fun updateAnimeNotes(id: Long, notes: String)

    @Query("DELETE FROM anime WHERE id = :id")
    suspend fun deleteAnimeById(id: Long)

    @Query("DELETE FROM anime")
    suspend fun clearAllAnime()

    // ==========================================
    // MANGA
    // ==========================================
    @Query("SELECT * FROM manga ORDER BY sortOrder ASC, id DESC")
    fun getAllManga(): Flow<List<MangaItem>>

    @Query("SELECT * FROM manga ORDER BY sortOrder ASC, id DESC")
    suspend fun getAllMangaDirect(): List<MangaItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(item: MangaItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllManga(items: List<MangaItem>)

    @Update
    suspend fun updateManga(item: MangaItem)

    @Query("SELECT * FROM manga WHERE id = :id")
    suspend fun getMangaByIdDirect(id: Long): MangaItem?

    @Query("UPDATE manga SET totalPlaytimeSeconds = :playtimeSeconds, lastPlayedAt = :timestamp WHERE id = :id")
    suspend fun updateMangaPlaytime(id: Long, playtimeSeconds: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE manga SET isPinnedToPlaytime = :isPinned WHERE id = :id")
    suspend fun updateMangaPinned(id: Long, isPinned: Boolean)

    @Query("UPDATE manga SET notes = :notes WHERE id = :id")
    suspend fun updateMangaNotes(id: Long, notes: String)

    @Query("DELETE FROM manga WHERE id = :id")
    suspend fun deleteMangaById(id: Long)

    @Query("DELETE FROM manga")
    suspend fun clearAllManga()

    // ==========================================
    // BÜCHER (Books)
    // ==========================================
    @Query("SELECT * FROM books ORDER BY sortOrder ASC, id DESC")
    fun getAllBooks(): Flow<List<BookItem>>

    @Query("SELECT * FROM books ORDER BY sortOrder ASC, id DESC")
    suspend fun getAllBooksDirect(): List<BookItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(item: BookItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBooks(items: List<BookItem>)

    @Update
    suspend fun updateBook(item: BookItem)

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookByIdDirect(id: Long): BookItem?

    @Query("UPDATE books SET totalPlaytimeSeconds = :playtimeSeconds, lastPlayedAt = :timestamp WHERE id = :id")
    suspend fun updateBookPlaytime(id: Long, playtimeSeconds: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE books SET isPinnedToPlaytime = :isPinned WHERE id = :id")
    suspend fun updateBookPinned(id: Long, isPinned: Boolean)

    @Query("UPDATE books SET notes = :notes WHERE id = :id")
    suspend fun updateBookNotes(id: Long, notes: String)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: Long)

    @Query("DELETE FROM books")
    suspend fun clearAllBooks()

    // ==========================================
    // TOP 10 ENTRIES
    // ==========================================
    @Query("SELECT * FROM top_ten_entries WHERE mediaType = :mediaType AND categoryName = :categoryName ORDER BY rank ASC, id ASC")
    fun getTopTenEntries(mediaType: String, categoryName: String): Flow<List<TopTenEntry>>

    @Query("SELECT DISTINCT categoryName FROM top_ten_entries WHERE mediaType = :mediaType ORDER BY categoryName ASC")
    fun getCategoriesForMediaType(mediaType: String): Flow<List<String>>

    @Query("SELECT * FROM top_ten_entries WHERE mediaType = :mediaType AND categoryName = :categoryName ORDER BY rank ASC, id ASC")
    suspend fun getTopTenEntriesForCategoryDirect(mediaType: String, categoryName: String): List<TopTenEntry>

    @Query("SELECT * FROM top_ten_entries WHERE id = :id LIMIT 1")
    suspend fun getTopTenEntryByIdDirect(id: Long): TopTenEntry?

    @Query("SELECT * FROM top_ten_entries ORDER BY mediaType ASC, categoryName ASC, rank ASC")
    suspend fun getAllTopTenEntriesDirect(): List<TopTenEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopTenEntry(entry: TopTenEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTopTen(entries: List<TopTenEntry>)

    @Update
    suspend fun updateTopTenEntry(entry: TopTenEntry)

    @Query("DELETE FROM top_ten_entries WHERE id = :id")
    suspend fun deleteTopTenEntryById(id: Long)

    @Query("DELETE FROM top_ten_entries WHERE mediaType = :mediaType AND categoryName = :categoryName")
    suspend fun clearTopTenForCategory(mediaType: String, categoryName: String)

    @Query("DELETE FROM top_ten_entries WHERE mediaType = :mediaType")
    suspend fun deleteTopTenByMediaType(mediaType: String)

    @Query("DELETE FROM top_ten_entries")
    suspend fun clearAllTopTen()

    @Transaction
    suspend fun saveTopTenEntryAtRank(
        mediaType: String,
        categoryName: String,
        rank: Int,
        title: String,
        subtitle: String,
        shiftExistingDown: Boolean
    ) {
        val cleanCategory = categoryName.trim()
        val cleanTitle = title.trim()
        val cleanSubtitle = subtitle.trim()
        if (cleanCategory.isBlank() || cleanTitle.isBlank()) return
        val clampedRank = rank.coerceIn(1, 10)

        val existingRows = getTopTenEntriesForCategoryDirect(mediaType, cleanCategory)
        val byRank = linkedMapOf<Int, TopTenEntry>()
        for (row in existingRows) {
            if (row.rank in 1..10 && row.title.isNotBlank() && !byRank.containsKey(row.rank)) {
                byRank[row.rank] = row
            }
        }

        val newEntry = TopTenEntry(
            id = 0,
            mediaType = mediaType,
            categoryName = cleanCategory,
            rank = clampedRank,
            title = cleanTitle,
            subtitle = cleanSubtitle
        )

        if (shiftExistingDown) {
            var currentRank = clampedRank
            var toPlace: TopTenEntry? = newEntry
            while (toPlace != null && currentRank <= 10) {
                val displaced = byRank[currentRank]
                byRank[currentRank] = toPlace.copy(id = 0, rank = currentRank)
                toPlace = displaced
                currentRank++
            }
        } else {
            byRank[clampedRank] = newEntry
        }

        clearTopTenForCategory(mediaType, cleanCategory)
        val normalized = byRank.entries
            .sortedBy { it.key }
            .map { (r, item) -> item.copy(id = 0, rank = r) }
        insertAllTopTen(normalized)
    }

    @Transaction
    suspend fun deleteTopTenEntryPreservingCategory(entryId: Long) {
        val target = getTopTenEntryByIdDirect(entryId) ?: return
        val existingRows = getTopTenEntriesForCategoryDirect(target.mediaType, target.categoryName)
        val remainingByRank = linkedMapOf<Int, TopTenEntry>()
        for (row in existingRows) {
            if (row.rank != target.rank && row.rank in 1..10 && row.title.isNotBlank() && !remainingByRank.containsKey(row.rank)) {
                remainingByRank[row.rank] = row
            }
        }
        clearTopTenForCategory(target.mediaType, target.categoryName)
        if (remainingByRank.isEmpty()) {
            insertTopTenEntry(
                TopTenEntry(
                    mediaType = target.mediaType,
                    categoryName = target.categoryName,
                    rank = 1,
                    title = "",
                    subtitle = ""
                )
            )
        } else {
            insertAllTopTen(
                remainingByRank.entries
                    .sortedBy { it.key }
                    .map { (r, item) -> item.copy(id = 0, rank = r) }
            )
        }
    }

    // ==========================================
    // 30-Day Rolling Daily Playtime (Media)
    // ==========================================
    @Query("SELECT * FROM media_daily_playtime WHERE dateEpochDay >= :minEpochDay ORDER BY dateEpochDay ASC")
    fun getAllMediaDailyPlaytime(minEpochDay: Long): Flow<List<MediaDailyPlaytimeRecord>>

    @Query("SELECT * FROM media_daily_playtime WHERE mediaType = :mediaType AND itemId = :itemId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getMediaDailyRecordDirect(mediaType: String, itemId: Long, dateEpochDay: Long): MediaDailyPlaytimeRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaDailyRecord(record: MediaDailyPlaytimeRecord): Long

    @Query("DELETE FROM media_daily_playtime WHERE dateEpochDay < :cutoffEpochDay")
    suspend fun deleteOldMediaDailyRecords(cutoffEpochDay: Long): Int

    @Query("DELETE FROM media_daily_playtime WHERE mediaType = :mediaType AND itemId = :itemId")
    suspend fun deleteMediaDailyRecordsForItem(mediaType: String, itemId: Long)

    @Transaction
    suspend fun addMediaPlaytimeForDay(
        mediaType: String,
        itemId: Long,
        dateEpochDay: Long,
        dateString: String,
        addedSeconds: Long
    ) {
        if (addedSeconds <= 0L) return
        val existing = getMediaDailyRecordDirect(mediaType, itemId, dateEpochDay)
        if (existing != null) {
            insertMediaDailyRecord(existing.copy(playtimeSeconds = existing.playtimeSeconds + addedSeconds))
        } else {
            insertMediaDailyRecord(
                MediaDailyPlaytimeRecord(
                    mediaType = mediaType,
                    itemId = itemId,
                    dateEpochDay = dateEpochDay,
                    dateString = dateString,
                    playtimeSeconds = addedSeconds
                )
            )
        }
    }
}
