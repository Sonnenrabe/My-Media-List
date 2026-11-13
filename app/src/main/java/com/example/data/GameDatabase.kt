package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Game::class,
        DailyPlaytimeRecord::class,
        SeriesItem::class,
        AnimeItem::class,
        MangaItem::class,
        BookItem::class,
        TopTenEntry::class,
        MediaDailyPlaytimeRecord::class
    ],
    version = 6,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_playtime` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `gameId` INTEGER NOT NULL,
                        `dateEpochDay` INTEGER NOT NULL,
                        `dateString` TEXT NOT NULL,
                        `playtimeSeconds` INTEGER NOT NULL,
                        FOREIGN KEY(`gameId`) REFERENCES `games`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_playtime_gameId_dateEpochDay` ON `daily_playtime` (`gameId`, `dateEpochDay`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_playtime_gameId` ON `daily_playtime` (`gameId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_playtime_dateEpochDay` ON `daily_playtime` (`dateEpochDay`)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN rating TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE games ADD COLUMN isPinnedToPlaytime INTEGER NOT NULL DEFAULT 1")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `series` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `season` TEXT NOT NULL,
                        `rating` REAL NOT NULL,
                        `notes` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `anime` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `season` TEXT NOT NULL,
                        `rating` REAL NOT NULL,
                        `notes` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `manga` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `chapter` TEXT NOT NULL,
                        `rating` REAL NOT NULL,
                        `notes` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `books` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `author` TEXT NOT NULL,
                        `rating` REAL NOT NULL,
                        `notes` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `top_ten_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `mediaType` TEXT NOT NULL,
                        `categoryName` TEXT NOT NULL,
                        `rank` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `subtitle` TEXT NOT NULL,
                        `referenceId` INTEGER
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN starRating REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE series ADD COLUMN ratingWord TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE anime ADD COLUMN ratingWord TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE manga ADD COLUMN ratingWord TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE books ADD COLUMN ratingWord TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN categoryKey TEXT NOT NULL DEFAULT 'SPIELE'")

                db.execSQL("ALTER TABLE series ADD COLUMN totalPlaytimeSeconds INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE series ADD COLUMN isPinnedToPlaytime INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE series ADD COLUMN lastPlayedAt INTEGER")

                db.execSQL("ALTER TABLE anime ADD COLUMN totalPlaytimeSeconds INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE anime ADD COLUMN isPinnedToPlaytime INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE anime ADD COLUMN lastPlayedAt INTEGER")

                db.execSQL("ALTER TABLE manga ADD COLUMN totalPlaytimeSeconds INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE manga ADD COLUMN isPinnedToPlaytime INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE manga ADD COLUMN lastPlayedAt INTEGER")

                db.execSQL("ALTER TABLE books ADD COLUMN totalPlaytimeSeconds INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE books ADD COLUMN isPinnedToPlaytime INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE books ADD COLUMN lastPlayedAt INTEGER")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `media_daily_playtime` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `mediaType` TEXT NOT NULL,
                        `itemId` INTEGER NOT NULL,
                        `dateEpochDay` INTEGER NOT NULL,
                        `dateString` TEXT NOT NULL,
                        `playtimeSeconds` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_media_daily_playtime_mediaType_itemId_dateEpochDay` ON `media_daily_playtime` (`mediaType`, `itemId`, `dateEpochDay`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_media_daily_playtime_mediaType_itemId` ON `media_daily_playtime` (`mediaType`, `itemId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_media_daily_playtime_dateEpochDay` ON `media_daily_playtime` (`dateEpochDay`)")
            }
        }

        fun getInstance(context: Context, scope: CoroutineScope): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "games_playtime.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: GameDatabase) {
                val gameDao = database.gameDao()
                val mediaDao = database.mediaDao()
                val now = System.currentTimeMillis()
                val todayEpoch = DailyDateHelper.getLocalEpochDay(now)

                // Seed Games
                val game1Id = gameDao.insertGame(
                    Game(
                        title = "Elden Ring",
                        totalPlaytimeSeconds = 84L * 3600L + 22L * 60L + 15L,
                        isArchived = false,
                        colorHex = 0xFFFFAB00,
                        createdAt = now - (30L * 86400000L),
                        lastPlayedAt = now - 3600000L,
                        notes = "Exploring Leyndell Capital",
                        sortOrder = 0,
                        rating = "Outstanding",
                        isPinnedToPlaytime = true
                    )
                )

                val game2Id = gameDao.insertGame(
                    Game(
                        title = "Metroid Prime Remastered",
                        totalPlaytimeSeconds = 12L * 3600L + 45L * 60L + 30L,
                        isArchived = false,
                        colorHex = 0xFF00E5FF,
                        createdAt = now - (14L * 86400000L),
                        lastPlayedAt = now - 86400000L,
                        notes = "Acquired Gravity Suit",
                        sortOrder = 1,
                        rating = "Very Good",
                        isPinnedToPlaytime = true
                    )
                )

                val game3Id = gameDao.insertGame(
                    Game(
                        title = "Hades II",
                        totalPlaytimeSeconds = 29L * 3600L + 10L * 60L + 4L,
                        isArchived = false,
                        colorHex = 0xFFFF4081,
                        createdAt = now - (7L * 86400000L),
                        lastPlayedAt = now - 1800000L,
                        notes = "Night of the Underworld runs",
                        sortOrder = 2,
                        rating = "Outstanding",
                        isPinnedToPlaytime = true
                    )
                )

                val game4Id = gameDao.insertGame(
                    Game(
                        title = "Rocket League",
                        totalPlaytimeSeconds = 145L * 3600L + 0L,
                        isArchived = false,
                        colorHex = 0xFF00E676,
                        createdAt = now - (100L * 86400000L),
                        lastPlayedAt = now - 7200000L,
                        notes = "Diamond rank 2v2",
                        sortOrder = 3,
                        rating = "Very Good",
                        isPinnedToPlaytime = false
                    )
                )

                // 30-day daily records
                val initialDailyRecords = mutableListOf<DailyPlaytimeRecord>()
                initialDailyRecords.add(DailyPlaytimeRecord(gameId = game1Id, dateEpochDay = todayEpoch, dateString = DailyDateHelper.formatIsoDate(todayEpoch), playtimeSeconds = 2L * 3600L + 15L * 60L))
                initialDailyRecords.add(DailyPlaytimeRecord(gameId = game1Id, dateEpochDay = todayEpoch - 1, dateString = DailyDateHelper.formatIsoDate(todayEpoch - 1), playtimeSeconds = 3L * 3600L + 40L * 60L))
                initialDailyRecords.add(DailyPlaytimeRecord(gameId = game1Id, dateEpochDay = todayEpoch - 3, dateString = DailyDateHelper.formatIsoDate(todayEpoch - 3), playtimeSeconds = 1L * 3600L + 10L * 60L))
                initialDailyRecords.add(DailyPlaytimeRecord(gameId = game3Id, dateEpochDay = todayEpoch, dateString = DailyDateHelper.formatIsoDate(todayEpoch), playtimeSeconds = 1L * 3600L + 30L * 60L))
                initialDailyRecords.add(DailyPlaytimeRecord(gameId = game2Id, dateEpochDay = todayEpoch - 1, dateString = DailyDateHelper.formatIsoDate(todayEpoch - 1), playtimeSeconds = 2L * 3600L + 20L * 60L))
                gameDao.insertAllDailyRecords(initialDailyRecords)

                // Seed Series
                mediaDao.insertAllSeries(
                    listOf(
                        SeriesItem(name = "Breaking Bad", season = "Finished", rating = 9.8, notes = "Absolute masterpiece"),
                        SeriesItem(name = "Severance", season = "Season 2", rating = 9.2, notes = "Incredible suspense"),
                        SeriesItem(name = "Stranger Things", season = "Season 4", rating = 8.5, notes = "Waiting for Season 5"),
                        SeriesItem(name = "The Last of Us", season = "Season 1", rating = 9.0, notes = "Great adaptation")
                    )
                )

                // Seed Anime
                mediaDao.insertAllAnime(
                    listOf(
                        AnimeItem(name = "Attack on Titan", season = "Finished", rating = 9.7, notes = "Epic story"),
                        AnimeItem(name = "Frieren: Beyond Journey's End", season = "Season 1", rating = 9.5, notes = "Beautifully animated"),
                        AnimeItem(name = "Jujutsu Kaisen", season = "Season 2", rating = 8.9, notes = "Shibuya Incident"),
                        AnimeItem(name = "Death Note", season = "Finished", rating = 9.1, notes = "All-time classic")
                    )
                )

                // Seed Manga
                mediaDao.insertAllManga(
                    listOf(
                        MangaItem(title = "One Piece", chapter = "Chapter 1115", rating = 9.6, notes = "Egghead Arc highlights"),
                        MangaItem(title = "Berserk", chapter = "Chapter 376", rating = 9.8, notes = "Masterpiece artwork"),
                        MangaItem(title = "Chainsaw Man", chapter = "Chapter 165", rating = 8.7, notes = "Part 2"),
                        MangaItem(title = "Monster", chapter = "Finished", rating = 9.3, notes = "Gripping thriller")
                    )
                )

                // Seed Books
                mediaDao.insertAllBooks(
                    listOf(
                        BookItem(title = "The Lord of the Rings", author = "J.R.R. Tolkien", rating = 10.0, notes = "Unmatched classic"),
                        BookItem(title = "Dune", author = "Frank Herbert", rating = 9.4, notes = "Sci-Fi milestone"),
                        BookItem(title = "The Name of the Wind", author = "Patrick Rothfuss", rating = 9.1, notes = "Kingkiller Chronicle"),
                        BookItem(title = "1984", author = "George Orwell", rating = 9.0, notes = "Dystopian classic")
                    )
                )

                // Seed Top 10 Entries
                val topTenList = listOf(
                    TopTenEntry(mediaType = "SPIELE", categoryName = "Singleplayer", rank = 1, title = "Elden Ring", subtitle = "Outstanding | 84h"),
                    TopTenEntry(mediaType = "SPIELE", categoryName = "Singleplayer", rank = 2, title = "Hades II", subtitle = "Outstanding | 29h"),
                    TopTenEntry(mediaType = "SPIELE", categoryName = "Singleplayer", rank = 3, title = "Metroid Prime Remastered", subtitle = "Very Good | 12h"),
                    TopTenEntry(mediaType = "SPIELE", categoryName = "Multiplayer", rank = 1, title = "Rocket League", subtitle = "Very Good | 145h"),
                    TopTenEntry(mediaType = "SERIEN", categoryName = "Top 10", rank = 1, title = "Breaking Bad", subtitle = "Rating: 9.8"),
                    TopTenEntry(mediaType = "SERIEN", categoryName = "Top 10", rank = 2, title = "Severance", subtitle = "Rating: 9.2"),
                    TopTenEntry(mediaType = "SERIEN", categoryName = "Top 10", rank = 3, title = "The Last of Us", subtitle = "Rating: 9.0"),
                    TopTenEntry(mediaType = "ANIME", categoryName = "Top 10", rank = 1, title = "Attack on Titan", subtitle = "Rating: 9.7"),
                    TopTenEntry(mediaType = "ANIME", categoryName = "Top 10", rank = 2, title = "Frieren", subtitle = "Rating: 9.5"),
                    TopTenEntry(mediaType = "MANGA", categoryName = "Top 10", rank = 1, title = "Berserk", subtitle = "Rating: 9.8"),
                    TopTenEntry(mediaType = "MANGA", categoryName = "Top 10", rank = 2, title = "One Piece", subtitle = "Rating: 9.6"),
                    TopTenEntry(mediaType = "BUECHER", categoryName = "Top 10", rank = 1, title = "The Lord of the Rings", subtitle = "J.R.R. Tolkien | 10.0"),
                    TopTenEntry(mediaType = "BUECHER", categoryName = "Top 10", rank = 2, title = "Dune", subtitle = "Frank Herbert | 9.4")
                )
                mediaDao.insertAllTopTen(topTenList)
            }
        }
    }
}
