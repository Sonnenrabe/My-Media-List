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
interface GameDao {
    @Query("SELECT * FROM games WHERE isArchived = 0 AND categoryKey = 'SPIELE' ORDER BY sortOrder ASC, id DESC")
    fun getActiveGames(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE isArchived = 0 AND categoryKey = :categoryKey ORDER BY sortOrder ASC, id DESC")
    fun getGamesForCategory(categoryKey: String): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE isArchived = 0 ORDER BY sortOrder ASC, id DESC")
    fun getAllActiveGamesAllCategories(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE isArchived = 1 ORDER BY sortOrder ASC, id DESC")
    fun getArchivedGames(): Flow<List<Game>>

    @Query("SELECT * FROM games ORDER BY sortOrder ASC, id DESC")
    suspend fun getAllGamesDirect(): List<Game>

    @Query("SELECT * FROM games WHERE id = :id")
    fun getGameById(id: Long): Flow<Game?>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameByIdDirect(id: Long): Game?

    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGameCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Game>)

    @Update
    suspend fun updateGame(game: Game)

    @Update
    suspend fun updateAll(games: List<Game>)

    @Delete
    suspend fun deleteGame(game: Game)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGameById(id: Long)

    @Query("DELETE FROM games WHERE categoryKey = :categoryKey")
    suspend fun deleteGamesByCategory(categoryKey: String)

    @Query("DELETE FROM games")
    suspend fun clearAllGames()

    @Query("UPDATE games SET isArchived = :isArchived, lastPlayedAt = :timestamp WHERE id = :id")
    suspend fun setArchivedStatus(id: Long, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE games SET totalPlaytimeSeconds = :playtimeSeconds, lastPlayedAt = :timestamp WHERE id = :id")
    suspend fun updatePlaytime(id: Long, playtimeSeconds: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE games SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("UPDATE games SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("SELECT * FROM games WHERE isArchived = 0 AND isPinnedToPlaytime = 1 ORDER BY sortOrder ASC, id DESC")
    fun getPinnedGames(): Flow<List<Game>>

    @Query("UPDATE games SET isPinnedToPlaytime = :isPinned WHERE id = :id")
    suspend fun updatePinnedStatus(id: Long, isPinned: Boolean)

    @Query("UPDATE games SET rating = :rating WHERE id = :id")
    suspend fun updateRating(id: Long, rating: String)

    // ==========================================
    // 30-Day Rolling Daily Playtime Tracking
    // ==========================================

    @Query("SELECT * FROM daily_playtime WHERE gameId = :gameId AND dateEpochDay >= :minEpochDay ORDER BY dateEpochDay ASC")
    fun getDailyPlaytimeForGame(gameId: Long, minEpochDay: Long): Flow<List<DailyPlaytimeRecord>>

    @Query("SELECT * FROM daily_playtime WHERE dateEpochDay >= :minEpochDay ORDER BY dateEpochDay ASC")
    fun getAllDailyPlaytime(minEpochDay: Long): Flow<List<DailyPlaytimeRecord>>

    @Query("SELECT * FROM daily_playtime WHERE dateEpochDay >= :minEpochDay ORDER BY dateEpochDay ASC")
    suspend fun getAllDailyPlaytimeDirect(minEpochDay: Long): List<DailyPlaytimeRecord>

    @Query("SELECT * FROM daily_playtime WHERE gameId = :gameId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getDailyRecordDirect(gameId: Long, dateEpochDay: Long): DailyPlaytimeRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyRecord(record: DailyPlaytimeRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDailyRecords(records: List<DailyPlaytimeRecord>)

    @Query("DELETE FROM daily_playtime WHERE dateEpochDay < :cutoffEpochDay")
    suspend fun deleteOldDailyRecords(cutoffEpochDay: Long): Int

    @Query("DELETE FROM daily_playtime WHERE gameId = :gameId")
    suspend fun deleteDailyRecordsForGame(gameId: Long)

    @Transaction
    suspend fun addPlaytimeForDay(
        gameId: Long,
        dateEpochDay: Long,
        dateString: String,
        addedSeconds: Long
    ) {
        if (addedSeconds <= 0L) return
        val existing = getDailyRecordDirect(gameId, dateEpochDay)
        if (existing != null) {
            val updated = existing.copy(playtimeSeconds = existing.playtimeSeconds + addedSeconds)
            insertDailyRecord(updated)
        } else {
            insertDailyRecord(
                DailyPlaytimeRecord(
                    gameId = gameId,
                    dateEpochDay = dateEpochDay,
                    dateString = dateString,
                    playtimeSeconds = addedSeconds
                )
            )
        }
    }
}
