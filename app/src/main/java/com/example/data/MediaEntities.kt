package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Series / TV Shows
 */
@Entity(tableName = "series")
data class SeriesItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val season: String = "Season 1",
    val rating: Double = 0.0, // 1.0 to 10.0 (0.0 means unrated)
    val ratingWord: String = "",
    val notes: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val totalPlaytimeSeconds: Long = 0L,
    val isPinnedToPlaytime: Boolean = false,
    val lastPlayedAt: Long? = null
) {
    fun formattedRating(): String = if (rating > 0.0) String.format("%.1f", rating) else "-"
    fun formattedPlaytime(extraSeconds: Long = 0L): String = formatDurationHMS(totalPlaytimeSeconds + extraSeconds)
}

/**
 * Anime
 */
@Entity(tableName = "anime")
data class AnimeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val season: String = "Season 1",
    val rating: Double = 0.0, // 1.0 to 10.0
    val ratingWord: String = "",
    val notes: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val totalPlaytimeSeconds: Long = 0L,
    val isPinnedToPlaytime: Boolean = false,
    val lastPlayedAt: Long? = null
) {
    fun formattedRating(): String = if (rating > 0.0) String.format("%.1f", rating) else "-"
    fun formattedPlaytime(extraSeconds: Long = 0L): String = formatDurationHMS(totalPlaytimeSeconds + extraSeconds)
}

/**
 * Manga
 */
@Entity(tableName = "manga")
data class MangaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val chapter: String = "Chapter 1",
    val rating: Double = 0.0, // 1.0 to 10.0
    val ratingWord: String = "",
    val notes: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val totalPlaytimeSeconds: Long = 0L,
    val isPinnedToPlaytime: Boolean = false,
    val lastPlayedAt: Long? = null
) {
    fun formattedRating(): String = if (rating > 0.0) String.format("%.1f", rating) else "-"
    fun formattedPlaytime(extraSeconds: Long = 0L): String = formatDurationHMS(totalPlaytimeSeconds + extraSeconds)
}

/**
 * Books
 */
@Entity(tableName = "books")
data class BookItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String = "",
    val rating: Double = 0.0, // 1.0 to 10.0
    val ratingWord: String = "",
    val notes: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val totalPlaytimeSeconds: Long = 0L,
    val isPinnedToPlaytime: Boolean = false,
    val lastPlayedAt: Long? = null
) {
    fun formattedRating(): String = if (rating > 0.0) String.format("%.1f", rating) else "-"
    fun formattedPlaytime(extraSeconds: Long = 0L): String = formatDurationHMS(totalPlaytimeSeconds + extraSeconds)
}

fun formatDurationHMS(totalSeconds: Long): String {
    val safe = totalSeconds.coerceAtLeast(0L)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * Top 10 Entry for any media type.
 * Supports multiple user-defined categories (e.g. for games: Singleplayer, Multiplayer, RPG).
 */
@Entity(tableName = "top_ten_entries")
data class TopTenEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaType: String, // "SPIELE", "SERIEN", "ANIME", "MANGA", "BUECHER"
    val categoryName: String, // e.g. "Singleplayer", "Multiplayer", "Top 10"
    val rank: Int, // 1 to 10
    val title: String,
    val subtitle: String = "",
    val referenceId: Long? = null
)
