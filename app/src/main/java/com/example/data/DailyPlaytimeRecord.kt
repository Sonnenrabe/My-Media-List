package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Tracks daily playtime per game for a rolling 30-day window.
 * Entries older than 30 days expire automatically to prevent data buildup,
 * while the overall cumulative playtime on [Game] remains permanently preserved.
 */
@Entity(
    tableName = "daily_playtime",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["gameId", "dateEpochDay"], unique = true),
        Index(value = ["gameId"]),
        Index(value = ["dateEpochDay"])
    ]
)
data class DailyPlaytimeRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long,
    val dateEpochDay: Long, // Days since Unix epoch in user's local timezone
    val dateString: String,  // "YYYY-MM-DD"
    val playtimeSeconds: Long = 0L
) {
    fun formattedPlaytime(): String {
        val hours = playtimeSeconds / 3600
        val minutes = (playtimeSeconds % 3600) / 60
        val seconds = playtimeSeconds % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }
}

@Entity(
    tableName = "media_daily_playtime",
    indices = [
        Index(value = ["mediaType", "itemId", "dateEpochDay"], unique = true),
        Index(value = ["mediaType", "itemId"]),
        Index(value = ["dateEpochDay"])
    ]
)
data class MediaDailyPlaytimeRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaType: String,
    val itemId: Long,
    val dateEpochDay: Long,
    val dateString: String,
    val playtimeSeconds: Long = 0L
) {
    fun toDailyPlaytimeRecord(): DailyPlaytimeRecord = DailyPlaytimeRecord(
        id = id,
        gameId = itemId,
        dateEpochDay = dateEpochDay,
        dateString = dateString,
        playtimeSeconds = playtimeSeconds
    )
}

object DailyDateHelper {
    /**
     * Returns local calendar day index (days since Jan 1, 1970).
     */
    fun getLocalEpochDay(timestampMs: Long = System.currentTimeMillis()): Long {
        val tz = TimeZone.getDefault()
        val offset = tz.getOffset(timestampMs)
        return (timestampMs + offset) / 86400000L
    }

    /**
     * Formats local epoch day into ISO string "YYYY-MM-DD"
     */
    fun formatIsoDate(epochDay: Long): String {
        val utcMs = epochDay * 86400000L
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date(utcMs))
    }

    /**
     * Formats local epoch day for UI display, e.g. "Today", "Yesterday", or "Nov 5"
     */
    fun formatDisplayDate(epochDay: Long, todayEpochDay: Long = getLocalEpochDay()): String {
        return when (epochDay) {
            todayEpochDay -> "Today"
            todayEpochDay - 1 -> "Yesterday"
            else -> {
                val utcMs = epochDay * 86400000L
                val sdf = SimpleDateFormat("MMM d", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                sdf.format(Date(utcMs))
            }
        }
    }
}
