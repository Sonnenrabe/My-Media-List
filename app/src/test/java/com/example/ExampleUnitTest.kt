package com.example

import com.example.data.AnimeItem
import com.example.data.BookItem
import com.example.data.CategoryColorConfig
import com.example.data.DataExportImportManager
import com.example.data.FullBackupData
import com.example.data.Game
import com.example.data.GameRatingColor
import com.example.data.MangaItem
import com.example.data.MediaRatingRange
import com.example.data.NaturalOrderComparator
import com.example.data.RatingMode
import com.example.data.SeriesItem
import com.example.localization.AppLanguage
import com.example.localization.AppStringsProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testNaturalOrderSorting() {
        val list = listOf("Staffel 1", "Staffel 11", "Staffel 2", "Staffel 10")
        val sorted = list.sortedWith { a, b -> NaturalOrderComparator.compareStatus(a, b) }
        assertEquals(listOf("Staffel 1", "Staffel 2", "Staffel 10", "Staffel 11"), sorted)
    }

    @Test
    fun testHexColorFormattingAndParsing() {
        val colorHex = 0xFFFF5722
        val hexString = DataExportImportManager.formatHexColor(colorHex)
        assertEquals("#FFFF5722", hexString)
        val parsed = DataExportImportManager.parseHexColor(hexString, 0L)
        assertEquals(colorHex, parsed)
    }

    @Test
    fun testAllSixLanguagesHaveValidConciseStringsAndNoSchoolGrades() {
        assertEquals(6, AppLanguage.entries.size)
        for (lang in AppLanguage.entries) {
            val s = AppStringsProvider.get(lang)
            assertTrue("${lang.name} navGames blank", s.navGames.isNotBlank())
            assertTrue("${lang.name} csvExportButton blank", s.csvExportButton.isNotBlank())
            assertTrue("${lang.name} settingsTabCategories blank", s.settingsTabCategories.isNotBlank())
            assertEquals("${lang.name} defaultRatingWords size", 6, s.defaultRatingWords.size)
            assertEquals("${lang.name} defaultStarRangeLabels size", 5, s.defaultStarRangeLabels.size)
            assertFalse("${lang.name} should not contain school grade '1+'", s.defaultRatingWords.contains("1+"))
            assertFalse("${lang.name} should not contain school grade '2-'", s.defaultRatingWords.contains("2-"))
        }
        assertEquals("Games", AppStringsProvider.get(AppLanguage.ENGLISH).navGames)
        assertEquals("Spiele", AppStringsProvider.get(AppLanguage.GERMAN).navGames)
        assertEquals("Juegos", AppStringsProvider.get(AppLanguage.SPANISH).navGames)
        assertEquals("Giochi", AppStringsProvider.get(AppLanguage.ITALIAN).navGames)
        assertEquals("ゲーム", AppStringsProvider.get(AppLanguage.JAPANESE).navGames)
        assertEquals("游戏", AppStringsProvider.get(AppLanguage.CHINESE).navGames)
    }

    @Test
    fun testCsvBackupWithCustomCategoriesRatingModesAndWordOrStarRatingsRoundTrip() {
        val backupData = FullBackupData(
            games = listOf(
                Game(title = "Hades II", totalPlaytimeSeconds = 3600L, rating = "Masterpiece", starRating = 9.5),
                Game(title = "Catan", totalPlaytimeSeconds = 7200L, starRating = 9.0, isPinnedToPlaytime = true, categoryKey = "CUSTOM_BOARDGAMES")
            ),
            series = listOf(
                SeriesItem(name = "Andor", season = "Season 2", rating = 9.2, ratingWord = "Masterpiece", totalPlaytimeSeconds = 5400L, isPinnedToPlaytime = true)
            ),
            anime = listOf(
                AnimeItem(name = "Frieren", season = "Finished", rating = 9.6, ratingWord = "Very Good")
            ),
            manga = listOf(
                MangaItem(title = "Berserk", chapter = "Chapter 375", rating = 9.9, ratingWord = "Masterpiece")
            ),
            books = listOf(
                BookItem(title = "Dune", author = "Frank Herbert", rating = 9.4, ratingWord = "Masterpiece")
            ),
            categoryColors = mapOf(
                "SPIELE" to CategoryColorConfig("SPIELE", 0xFF818CF8, 0xFFF8FAFC, "PC Games", RatingMode.STARS, isGameCategory = true),
                "SERIEN" to CategoryColorConfig("SERIEN", 0xFFFF7043, 0xFFFFFFFF, "TV Shows", RatingMode.WORDS, isGameCategory = true),
                "CUSTOM_BOARDGAMES" to CategoryColorConfig("CUSTOM_BOARDGAMES", 0xFF14B8A6, 0xFFF8FAFC, "Board Games", RatingMode.STARS, isGameCategory = true)
            ),
            gameRatingColors = listOf(
                GameRatingColor("Masterpiece", 0xFFFFD700),
                GameRatingColor("Very Good", 0xFF00E676)
            ),
            mediaRatingRanges = listOf(
                MediaRatingRange(9.0, 10.0, 0xFF00E676, "Masterpiece"),
                MediaRatingRange(7.5, 8.9, 0xFF00E5FF, "Very Good")
            ),
            appLanguage = AppLanguage.JAPANESE,
            autoBackupEnabled = false
        )

        val csvString = DataExportImportManager.exportToExcelCsv(backupData)
        assertTrue(csvString.contains("### EINSTELLUNGEN_KATEGORIEN ###"))
        assertTrue(csvString.contains("### EINSTELLUNGEN_BEWERTUNGEN_SPIELE ###"))
        assertTrue(csvString.contains("### EINSTELLUNGEN_BEWERTUNGEN_MEDIEN ###"))
        assertTrue(csvString.contains("### EINSTELLUNGEN_APP ###"))

        val result = DataExportImportManager.parseBackup(csvString)
        assertTrue(result.isSuccess)
        val parsedData = result.getOrThrow().data
        assertEquals(AppLanguage.JAPANESE, parsedData.appLanguage)
        assertEquals(false, parsedData.autoBackupEnabled)
        assertNotNull(parsedData.categoryColors)
        assertEquals(0xFF818CF8, parsedData.categoryColors["SPIELE"]?.accentColorHex)
        assertEquals("PC Games", parsedData.categoryColors["SPIELE"]?.customName)
        assertEquals(RatingMode.STARS, parsedData.categoryColors["SPIELE"]?.ratingMode)
        assertEquals(true, parsedData.categoryColors["SPIELE"]?.isGameCategory)
        assertEquals("TV Shows", parsedData.categoryColors["SERIEN"]?.customName)
        assertEquals(RatingMode.WORDS, parsedData.categoryColors["SERIEN"]?.ratingMode)
        assertEquals(true, parsedData.categoryColors["SERIEN"]?.isGameCategory)
        assertEquals("Board Games", parsedData.categoryColors["CUSTOM_BOARDGAMES"]?.customName)
        assertEquals(true, parsedData.categoryColors["CUSTOM_BOARDGAMES"]?.isGameCategory)

        assertEquals(9.5, parsedData.games[0].starRating, 0.01)
        assertEquals("SPIELE", parsedData.games[0].categoryKey)
        assertEquals("Catan", parsedData.games[1].title)
        assertEquals("CUSTOM_BOARDGAMES", parsedData.games[1].categoryKey)
        assertEquals("Masterpiece", parsedData.series[0].ratingWord)
        assertEquals(5400L, parsedData.series[0].totalPlaytimeSeconds)
        assertTrue(parsedData.series[0].isPinnedToPlaytime)
        assertEquals("Very Good", parsedData.anime[0].ratingWord)
        assertEquals("Masterpiece", parsedData.manga[0].ratingWord)
        assertEquals("Masterpiece", parsedData.books[0].ratingWord)

        assertEquals(2, parsedData.gameRatingColors.size)
        assertEquals("Masterpiece", parsedData.gameRatingColors[0].ratingWord)
        assertEquals(2, parsedData.mediaRatingRanges.size)
        assertEquals(9.0, parsedData.mediaRatingRanges[0].minScore, 0.01)
    }

    @Test
    fun testLegacyGameAndMediaCsvImportCompatibility() {
        val legacyCsv = """
            ### SPIELE ###
            Titel;Spielzeit;Spielzeit_Sekunden;Bewertung;Notizen;Archiviert;Angeheftet_PlayTime;Sortierung
            Elden Ring;10:00:00;36000;Überragend;Großartig;NEIN;JA;0

            ### SERIEN ###
            Name;Staffel;Bewertung;Notizen;Sortierung
            Dark;Finished;9.5;Komplex;0

            ### EINSTELLUNGEN_KATEGORIEN ###
            Kategorie;AkzentFarbe;TextFarbe
            SPIELE;#FF818CF8;#FFF8FAFC
            SERIEN;#FFFB7185;#FFF8FAFC

            ### EINSTELLUNGEN_BEWERTUNGEN_SPIELE ###
            Wort;Farbe
            Überragend;#FF10B981
        """.trimIndent()

        val result = DataExportImportManager.parseBackup(legacyCsv)
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(1, summary.gameCount)
        assertEquals(1, summary.seriesCount)
        assertEquals("Elden Ring", summary.data.games[0].title)
        assertEquals("SPIELE", summary.data.games[0].categoryKey)
        assertEquals("Überragend", summary.data.games[0].rating)
        assertEquals(0.0, summary.data.games[0].starRating, 0.01)
        assertEquals("Dark", summary.data.series[0].name)
        assertEquals(9.5, summary.data.series[0].rating, 0.01)
        assertEquals("", summary.data.series[0].ratingWord)
        assertEquals(0L, summary.data.series[0].totalPlaytimeSeconds)
        assertFalse(summary.data.series[0].isPinnedToPlaytime)
        assertEquals(RatingMode.WORDS, summary.data.categoryColors["SPIELE"]?.ratingMode)
        assertEquals(true, summary.data.categoryColors["SPIELE"]?.isGameCategory)
        assertEquals(RatingMode.STARS, summary.data.categoryColors["SERIEN"]?.ratingMode)
        assertEquals(false, summary.data.categoryColors["SERIEN"]?.isGameCategory)
        assertEquals(null, summary.data.appLanguage)
        assertEquals(null, summary.data.autoBackupEnabled)
    }
}
