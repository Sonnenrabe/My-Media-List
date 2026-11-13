package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AnimeItem
import com.example.data.BookItem
import com.example.data.DataExportImportManager
import com.example.data.FullBackupData
import com.example.data.Game
import com.example.data.MangaItem
import com.example.data.SeriesItem
import com.example.data.TopTenEntry
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My Media List", appName)
    }

    @Test
    fun `test game playtime formatting and rating`() {
        val game = Game(
            title = "Test Game",
            totalPlaytimeSeconds = 3665L,
            notes = "Completed chapter 1",
            sortOrder = 0,
            rating = "Überragend",
            isPinnedToPlaytime = true
        )
        assertEquals("01:01:05", game.formattedPlaytime())
        assertEquals("01:01:15", game.formattedPlaytime(10L))
        assertEquals("Completed chapter 1", game.notes)
        assertEquals("Überragend", game.rating)
        assertTrue(game.isPinnedToPlaytime)
        assertEquals(0, game.sortOrder)
    }

    @Test
    fun `test excel csv export and import roundtrip`() {
        val sampleData = FullBackupData(
            games = listOf(
                Game(title = "Elden Ring", totalPlaytimeSeconds = 36000L, rating = "Überragend", isPinnedToPlaytime = true)
            ),
            series = listOf(
                SeriesItem(name = "Breaking Bad", season = "Finished", rating = 9.8, notes = "Legendär")
            ),
            anime = listOf(
                AnimeItem(name = "Attack on Titan", season = "Finished", rating = 9.7)
            ),
            manga = listOf(
                MangaItem(title = "One Piece", chapter = "Kapitel 1115", rating = 9.6)
            ),
            books = listOf(
                BookItem(title = "Der Herr der Ringe", author = "J.R.R. Tolkien", rating = 10.0)
            ),
            topTenEntries = listOf(
                TopTenEntry(mediaType = "SPIELE", categoryName = "Singleplayer", rank = 1, title = "Elden Ring")
            )
        )

        val csv = DataExportImportManager.exportToExcelCsv(sampleData)
        assertTrue(csv.contains("### SPIELE ###"))
        assertTrue(csv.contains("### SERIEN ###"))
        assertTrue(csv.contains("### ANIME ###"))
        assertTrue(csv.contains("### MANGA ###"))
        assertTrue(csv.contains("### BUECHER ###"))
        assertTrue(csv.contains("### TOP 10 ###"))
        assertTrue(csv.contains("Elden Ring"))
        assertTrue(csv.contains("Breaking Bad"))

        val parsedResult = DataExportImportManager.parseBackup(csv)
        assertTrue(parsedResult.isSuccess)
        val summary = parsedResult.getOrThrow()
        assertEquals(1, summary.gameCount)
        assertEquals(1, summary.seriesCount)
        assertEquals(1, summary.animeCount)
        assertEquals(1, summary.mangaCount)
        assertEquals(1, summary.bookCount)
        assertEquals(1, summary.topTenCount)
        assertEquals("Breaking Bad", summary.data.series[0].name)
        assertEquals("Finished", summary.data.series[0].season)
        assertEquals(9.8, summary.data.series[0].rating, 0.01)
        assertEquals("J.R.R. Tolkien", summary.data.books[0].author)
    }

    @Test
    fun `test legacy backup json parsing`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = GameViewModel(context)

        val sampleJson = """
            {
              "version": 2,
              "app": "PlayTime",
              "exportedAt": 1730000000000,
              "games": [
                {
                  "title": "Celeste",
                  "totalPlaytimeSeconds": 7200,
                  "isArchived": false,
                  "colorHex": -16717825,
                  "createdAt": 1730000000000,
                  "notes": "Finished Chapter 7 Summit",
                  "sortOrder": 0
                },
                {
                  "title": "Portal",
                  "totalPlaytimeSeconds": 10800,
                  "isArchived": true,
                  "colorHex": -8127393,
                  "createdAt": 1730000000000,
                  "notes": "100% completed",
                  "sortOrder": 1
                }
              ]
            }
        """.trimIndent()

        val result = viewModel.parseBackupSummary(sampleJson)
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(2, summary.gameCount)
        assertEquals("Celeste", summary.data.games[0].title)
        assertEquals("Finished Chapter 7 Summit", summary.data.games[0].notes)
    }

    @Test
    fun `test game copy with updated title and color`() {
        val game = Game(
            id = 42L,
            title = "Old Title",
            colorHex = 0xFF00E5FF,
            totalPlaytimeSeconds = 5000L
        )
        val updated = game.copy(
            title = "New Custom Title",
            colorHex = 0xFFFF4081
        )
        assertEquals("New Custom Title", updated.title)
        assertEquals(0xFFFF4081, updated.colorHex)
        assertEquals(5000L, updated.totalPlaytimeSeconds)
    }

    @Test
    fun `test daily date helper formatting`() {
        val testEpochDay = 19700L
        val iso = com.example.data.DailyDateHelper.formatIsoDate(testEpochDay)
        assertTrue(iso.startsWith("2023-") || iso.startsWith("2024-"))

        val todayEpoch = com.example.data.DailyDateHelper.getLocalEpochDay()
        assertEquals("Today", com.example.data.DailyDateHelper.formatDisplayDate(todayEpoch, todayEpoch))
        assertEquals("Yesterday", com.example.data.DailyDateHelper.formatDisplayDate(todayEpoch - 1, todayEpoch))
    }

    @Test
    fun `test pruning expired daily logs preserves overall lifetime playtime`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val db = com.example.data.GameDatabase.getInstance(context, this)
        val repository = com.example.data.GameRepository(db.gameDao(), db.mediaDao())

        val testGameId = repository.insertGame(
            Game(
                title = "Total Time Test Game",
                totalPlaytimeSeconds = 72000L
            )
        )

        val today = com.example.data.DailyDateHelper.getLocalEpochDay()

        repository.insertDailyRecords(
            listOf(
                com.example.data.DailyPlaytimeRecord(
                    gameId = testGameId,
                    dateEpochDay = today - 5,
                    dateString = com.example.data.DailyDateHelper.formatIsoDate(today - 5),
                    playtimeSeconds = 3600L
                ),
                com.example.data.DailyPlaytimeRecord(
                    gameId = testGameId,
                    dateEpochDay = today - 45,
                    dateString = com.example.data.DailyDateHelper.formatIsoDate(today - 45),
                    playtimeSeconds = 5400L
                )
            )
        )

        val deletedCount = repository.pruneExpiredDailyPlaytime()
        assertTrue("Pruned expired records", deletedCount >= 1)

        val fetchedGame = repository.getGameByIdDirect(testGameId)
        org.junit.Assert.assertNotNull(fetchedGame)
        assertEquals(72000L, fetchedGame!!.totalPlaytimeSeconds)
    }

    @Test
    fun `test auto backup creates single file and overwrites old one`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val initialContent = "### SPIELE ###\nTest1"
        val updatedContent = "### SPIELE ###\nTest2"

        val result1 = com.example.data.AutoBackupManager.runDailyBackup(context, initialContent, force = true)
        assertTrue(result1.isSuccess)

        val backupDir = java.io.File(context.getExternalFilesDir(null), "backups")
        val backupFile = java.io.File(backupDir, com.example.data.AutoBackupManager.BACKUP_FILE_NAME)
        assertTrue(backupFile.exists())
        assertEquals(initialContent, backupFile.readText())

        val result2 = com.example.data.AutoBackupManager.runDailyBackup(context, updatedContent, force = true)
        assertTrue(result2.isSuccess)

        val files = backupDir.listFiles { _, name -> name.endsWith(".csv") }
        org.junit.Assert.assertNotNull(files)
        assertEquals(1, files!!.size)
        assertEquals(updatedContent, backupFile.readText())
    }

    @Test
    fun `test natural order status comparator for series and chapters`() {
        val unsortedSeasons = listOf("Staffel 1", "Staffel 11", "Staffel 2", "Finished", "Staffel 10", "Quit", "Staffel 3")
        val sortedSeasons = unsortedSeasons.sortedWith { a, b ->
            com.example.data.NaturalOrderComparator.compareStatus(a, b)
        }

        val expected = listOf("Staffel 1", "Staffel 2", "Staffel 3", "Staffel 10", "Staffel 11", "Finished", "Quit")
        assertEquals(expected, sortedSeasons)

        // Reverse check
        val reversed = sortedSeasons.reversed()
        assertEquals(listOf("Quit", "Finished", "Staffel 11", "Staffel 10", "Staffel 3", "Staffel 2", "Staffel 1"), reversed)

        // Manga chapters
        val unsortedChapters = listOf("Kapitel 1", "Kapitel 12", "Kapitel 2", "Kapitel 100", "Kapitel 3")
        val sortedChapters = unsortedChapters.sortedWith { a, b ->
            com.example.data.NaturalOrderComparator.compareStatus(a, b)
        }
        assertEquals(listOf("Kapitel 1", "Kapitel 2", "Kapitel 3", "Kapitel 12", "Kapitel 100"), sortedChapters)

        // Alphanumeric game titles
        val games = listOf("Final Fantasy 10", "Final Fantasy 7", "Final Fantasy 8", "Final Fantasy 2")
        val sortedGames = games.sortedWith { a, b ->
            com.example.data.NaturalOrderComparator.naturalCompare(a, b)
        }
        assertEquals(listOf("Final Fantasy 2", "Final Fantasy 7", "Final Fantasy 8", "Final Fantasy 10"), sortedGames)
    }

    @Test
    fun `test top ten category deletion`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val db = com.example.data.GameDatabase.getInstance(context, this)
        val repository = com.example.data.GameRepository(db.gameDao(), db.mediaDao())

        // Add a category and entry directly via repository
        repository.saveTopTenEntry(
            TopTenEntry(
                mediaType = "SPIELE",
                categoryName = "Co-op",
                rank = 1,
                title = "It Takes Two"
            )
        )

        val entries = repository.getTopTenEntries("SPIELE", "Co-op")
        val initialList = entries.first()
        assertTrue(initialList.any { it.title == "It Takes Two" })

        // Delete the category
        repository.clearTopTenCategory("SPIELE", "Co-op")
        val afterDelete = entries.first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `test settings manager defaults to english and syncs global rating words`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val manager = com.example.data.AppColorSettingsManager(context)
        manager.resetToDefaults()

        assertEquals(com.example.localization.AppLanguage.ENGLISH, manager.appLanguage.value)
        assertEquals(
            listOf("Outstanding", "Very Good", "Good", "Decent", "Playable", "Unplayable"),
            manager.gameRatingColors.value.map { it.ratingWord }
        )
        assertEquals(0, manager.getRatingWordRank("Outstanding"))
        assertEquals(1, manager.getRatingWordRank("Very Good"))
        assertEquals(3, manager.getRatingWordRank("Decent"))
        assertEquals(3, manager.getRatingWordRank("Satisfactory"))
        assertEquals("Decent", manager.resolveDisplayRatingWord("Satisfactory"))
        assertEquals(5, manager.getRatingWordRank("Unplayable"))
        assertEquals(Int.MAX_VALUE, manager.getRatingWordRank(""))

        // Cross-language default resolution for legacy/other-language saved ratings
        assertEquals("Outstanding", manager.resolveDisplayRatingWord("Überragend"))
        assertEquals(0, manager.getRatingWordRank("Überragend"))

        // Update category name, rating mode, and game-category tracking toggle
        manager.setCategoryConfig(
            key = com.example.data.AppColorSettingsManager.KEY_SERIEN,
            accentHex = 0xFF38BDF8L,
            customName = "Shows",
            ratingMode = com.example.data.RatingMode.WORDS,
            isGameCategory = true
        )
        assertEquals(com.example.data.RatingMode.WORDS, manager.getCategoryRatingMode(com.example.data.AppColorSettingsManager.KEY_SERIEN))
        assertTrue(manager.isGameCategory(com.example.data.AppColorSettingsManager.KEY_SERIEN))
        val enStrings = com.example.localization.AppStringsProvider.get(com.example.localization.AppLanguage.ENGLISH)
        assertEquals("Shows", manager.getCategoryDisplayName(com.example.data.AppColorSettingsManager.KEY_SERIEN, enStrings))

        // Create and delete custom category
        val customKey = manager.createCustomCategory(
            name = "Board Games",
            accentHex = 0xFF14B8A6L,
            ratingMode = com.example.data.RatingMode.STARS,
            isGameCategory = true
        )
        assertTrue(manager.orderedCategoryKeys.value.contains(customKey))
        assertTrue(manager.isGameCategory(customKey))
        assertEquals("Board Games", manager.getCategoryDisplayName(customKey, enStrings))
        manager.deleteCustomCategory(customKey)
        assertTrue(!manager.orderedCategoryKeys.value.contains(customKey))

        // Customize ONLY the color of slot 0 -> still translates cleanly to German while keeping custom color
        val updatedColors = manager.gameRatingColors.value.mapIndexed { idx, item ->
            if (idx == 0) item.copy(colorHex = 0xFFFFD700L) else item
        }
        manager.setGameRatingColors(updatedColors)
        manager.setAppLanguage(com.example.localization.AppLanguage.GERMAN)
        assertEquals(com.example.localization.AppLanguage.GERMAN, manager.appLanguage.value)
        assertEquals("Überragend", manager.gameRatingColors.value.first().ratingWord)
        assertEquals(0xFFFFD700L, manager.gameRatingColors.value.first().colorHex)
        assertEquals(
            androidx.compose.ui.graphics.Color(0xFFFFD700L),
            manager.getColorForGameRating("Outstanding", androidx.compose.ui.graphics.Color.Gray)
        )

        // Customize global rating word & color -> synced directly to color lookup
        manager.setGameRatingColors(listOf(com.example.data.GameRatingColor("Legend", 0xFFFFD700L)))
        assertEquals(
            androidx.compose.ui.graphics.Color(0xFFFFD700L),
            manager.getColorForGameRating("Legend", androidx.compose.ui.graphics.Color.Gray)
        )
        // Switching back to English preserves custom user rating words
        manager.setAppLanguage(com.example.localization.AppLanguage.ENGLISH)
        assertEquals("Legend", manager.gameRatingColors.value.first().ratingWord)
    }

    @Test
    fun `test auto backup can be disabled and skips automatic daily backup`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val backupDir = java.io.File(context.getExternalFilesDir(null), "backups")
        val backupFile = java.io.File(backupDir, com.example.data.AutoBackupManager.BACKUP_FILE_NAME)
        if (backupFile.exists()) backupFile.delete()

        com.example.data.AutoBackupManager.setAutoBackupEnabled(context, false)
        assertTrue(!com.example.data.AutoBackupManager.isAutoBackupEnabled(context))
        assertTrue(!com.example.data.AutoBackupManager.shouldRunDailyBackup(context))

        val autoResult = com.example.data.AutoBackupManager.runDailyBackup(context, "### SPIELE ###\nSkipped", force = false)
        assertTrue(autoResult.isFailure)
        assertTrue(!backupFile.exists())

        // Restore default
        com.example.data.AutoBackupManager.setAutoBackupEnabled(context, true)
        assertTrue(com.example.data.AutoBackupManager.isAutoBackupEnabled(context))
    }
}
