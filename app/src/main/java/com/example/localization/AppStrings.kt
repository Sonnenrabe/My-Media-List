package com.example.localization

import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String
) {
    ENGLISH("en", "English", "English"),
    GERMAN("de", "Deutsch", "German"),
    SPANISH("es", "Español", "Spanish"),
    ITALIAN("it", "Italiano", "Italian"),
    JAPANESE("ja", "日本語", "Japanese"),
    CHINESE("zh", "中文", "Chinese");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

interface AppStrings {
    // App & Navigation
    val appSubtitle: String
    val navGames: String
    val navSeries: String
    val navAnime: String
    val navManga: String
    val navBooks: String
    val navPlayTime: String
    val navPlayTimeTitle: String
    val navBackup: String
    val navSettings: String
    val timerActiveDrawer: String
    val liveBadge: String
    val pinnedCount: (Int) -> String
    val openMenuDesc: String
    val closeDesc: String
    val searchDesc: String
    val clearDesc: String
    val toActiveTimerDesc: String
    val searchPlaceholder: String
    val dailyAutoBackupToast: (String) -> String

    // Common Actions & Labels
    val save: String
    val saveUpper: String
    val cancel: String
    val delete: String
    val deletePermanently: String
    val edit: String
    val add: String
    val create: String
    val apply: String
    val reset: String
    val choose: String
    val sortDesc: String
    val unrated: String
    val notesOptional: String
    val ratingLabel: String
    val ratingOptional: String
    val starRatingLabel: String
    val wordRatingLabel: String
    val noRatingSelected: String
    val tapToRate: String
    val clearRating: String
    val confirmDeleteTitle: (String) -> String
    val confirmDeleteMessage: (String) -> String
    val savedToast: String
    val updatedToast: (String) -> String
    val deletedToast: (String) -> String

    // Games Screen & Cards
    val tabAllItems: (String, Int) -> String
    val tabTopTen: String
    val sortStandard: String
    val sortStandardReversed: String
    val sortNameAZ: String
    val sortNameZA: String
    val sortRatingBest: String
    val sortRatingLowest: String
    val sortPlaytimeMost: String
    val sortPlaytimeLeast: String
    val sortStatus: String
    val sortStatusReversed: String
    val sortChapters: String
    val sortChaptersReversed: String
    val sortAuthorAZ: String
    val sortAuthorZA: String
    val emptySearchTitle: String
    val emptySearchSubtitle: String
    val clearSearch: String
    val emptyGamesTitle: String
    val emptyGamesSubtitle: String
    val addFirstGame: String
    val fabAddGame: String
    val fabAddEntry: String
    val emptyCustomCategoryTitle: (String) -> String
    val emptyCustomCategorySubtitle: String
    val emptyCustomCategoryFirst: (String) -> String
    val editEntryTitle: (String) -> String
    val addEntryTitle: (String) -> String
    val timerRunning: String
    val pinnedToPlayTimeDesc: String
    val pinToPlayTimeDesc: String
    val reorderPositionDesc: String

    // Add & Edit Game Dialogs
    val addGameTitle: String
    val gameNameRequired: String
    val titleEmptyError: String
    val startPlaytimeOptional: String
    val hours: String
    val minutes: String
    val seconds: String
    val pinToPlayTime: String
    val pinToPlayTimeSub: String
    val gameNotesPlaceholder: String
    val editGameTitle: String
    val editGameSubtitle: String
    val titleRequired: String
    val deleteGame: String
    val deleteGameQuestion: String

    // Game Detail & Time / Notes Dialogs
    val runningNow: String
    val archivedCompleted: String
    val activeTitle: String
    val sessionInProgress: String
    val totalPlaytime: String
    val thisSession: String
    val pauseUpper: String
    val playUpper: String
    val adjustPlaytimeDesc: String
    val editGameDesc: String
    val tapToAddNotes: String
    val editTitleAndRating: String
    val adjustPlaytimeTitle: String
    val currentPlaytime: (String) -> String
    val newPlaytime: String
    val noChange: String
    val quickAdjustments: String
    val exactDuration: String
    val confirmPlaytimeWarning: String
    val confirmChange: String
    val gameNotesTitle: String
    val playtimePrefix: (String) -> String
    val notesDialogPlaceholder: String
    val saveNotes: String
    val completedTapDetails: String

    // 30-Day Activity Chart
    val last30DaysPlaytime: String
    val activeDaysCount: (Int) -> String
    val zeroMinutesLogged: String
    val rolling30DayWindow: String
    val lifetimeOverall: String
    val thirtyDaysAgo: String
    val today: String
    val yesterday: String
    val daysAgo: (Long) -> String
    val noPlaytime: String
    val recentActiveSessions: String
    val showLess: String
    val viewAll: (Int) -> String
    val noSessionsLast30Days: String

    // Media Screens & Dialogs (Series, Anime, Manga, Books)
    val emptySeriesTitle: String
    val emptySeriesSubtitle: String
    val addFirstSeries: String
    val fabAddSeries: String
    val emptyAnimeTitle: String
    val emptyAnimeSubtitle: String
    val addFirstAnime: String
    val fabAddAnime: String
    val emptyMangaTitle: String
    val emptyMangaSubtitle: String
    val addFirstManga: String
    val fabAddManga: String
    val emptyBooksTitle: String
    val emptyBooksSubtitle: String
    val addFirstBook: String
    val fabAddBook: String
    val authorPrefix: (String) -> String
    val bookLabel: String
    val editPrefix: String
    val addPrefix: String
    val typeSeries: String
    val typeAnime: String
    val typeManga: String
    val typeBook: String
    val nameTitleRequired: String
    val seasonStatusLabel: String
    val customSeasonLabel: String
    val seasonDefault: String
    val seasonOption1: String
    val seasonOption2: String
    val seasonOption3: String
    val statusFinished: String
    val statusQuit: String
    val seriesNotesPlaceholder: String
    val mangaTitleRequired: String
    val chapterStatusLabel: String
    val customChapterLabel: String
    val chapterDefault: String
    val chapterOption1: String
    val chapterOption2: String
    val chapterOption3: String
    val mangaNotesPlaceholder: String
    val bookTitleRequired: String
    val authorLabel: String
    val bookNotesPlaceholder: String

    // Top 10
    val noTopTenCategories: String
    val noTopTenCategoriesDesc: String
    val createCategory: String
    val newTopTenCategory: String
    val newTopTenCategoryPrompt: String
    val newTopTenCategoryHint: String
    val deleteCategoryTitle: String
    val deleteCategoryConfirm: (String) -> String
    val setRankPrompt: (Int) -> String
    val rankSlotTitle: (Int, String) -> String
    val pickFromEntries: String
    val subtitleOptional: String
    val subtitlePlaceholder: String

    // PlayTime Screen
    val notificationsDisabledBanner: String
    val enableNotificationsToast: String
    val currentlyRunning: (String) -> String
    val trackUpper: String
    val unpinFromPlayTimeDesc: String
    val playTimeInfoBanner: String
    val noPinnedGamesTitle: String
    val noPinnedGamesSubtitle: String
    val goToGames: String

    // Backup Screen
    val excelCompatibleTitle: String
    val excelCompatibleDesc: String
    val dailyAutoBackupTitle: String
    val activeBadge: String
    val inactiveBadge: String
    val dailyAutoBackupDesc: String
    val dailyAutoBackupDisabledDesc: String
    val storageLocation: String
    val defaultStorageName: String
    val selectedFolderName: String
    val fileLabel: (String) -> String
    val lastBackupLabel: (String) -> String
    val chooseFolder: String
    val backupNow: String
    val resetDefaultFolder: String
    val folderUpdatedToast: String
    val defaultFolderRestoredToast: String
    val manualExportTitle: String
    val csvExportButton: String
    val shareButton: String
    val shareBackupTitle: String
    val copyCsvButton: String
    val csvSavedToast: String
    val saveErrorToast: (String) -> String
    val csvCopiedToast: String
    val importDataTitle: String
    val importDataDesc: String
    val openBackupFileButton: String
    val pastePlaceholder: String
    val pasteDesc: String
    val checkAndImportButton: String
    val invalidFormatToast: (String) -> String
    val readErrorToast: (String) -> String
    val selectFileOrPasteToast: String
    val importedCountToast: (Int) -> String
    val confirmImportTitle: String
    val backupOverview: String
    val entriesFound: (Int) -> String
    val importSummaryLine: (Int, Int, Int, Int, Int, Int) -> String
    val importModeLabel: String
    val mergeTitle: String
    val mergeDesc: String
    val replaceTitle: String
    val replaceDesc: String
    val replaceAndImport: String
    val mergeAndImport: String

    // Settings Screen
    val settingsTabCategories: String
    val settingsTabRatingWords: String
    val settingsTabStarScale: String
    val settingsTabLanguage: String
    val categoriesSectionTitle: String
    val categoriesSectionDesc: String
    val accentColorLabel: (String) -> String
    val ratingModeBadgeWords: String
    val ratingModeBadgeStars: String
    val editCategoryButton: String
    val resetAllSettingsButton: String
    val resetConfirmTitle: String
    val resetConfirmMessage: String
    val defaultsRestoredToast: String
    val editCategoryDialogTitle: (String) -> String
    val createCategoryDialogTitle: String
    val newCategoryButton: String
    val categoryNameLabel: String
    val categoryNameHint: String
    val categoryNameRequiredError: String
    val gameCategoryLabel: String
    val gameCategoryDesc: String
    val gameCategoryBadge: String
    val ratingPrincipleLabel: String
    val ratingPrincipleWords: String
    val ratingPrincipleWordsDesc: String
    val ratingPrincipleStars: String
    val ratingPrincipleStarsDesc: String
    val categoryUpdatedToast: (String) -> String
    val categoryCreatedToast: (String) -> String
    val categoryDeletedToast: (String) -> String
    val deleteCustomCategoryQuestion: String
    val deleteCustomCategoryConfirm: (String) -> String
    val quickColorSelect: String
    val customHexCode: String
    val ratingWordsSectionTitle: String
    val ratingWordsSectionDesc: String
    val newButton: String
    val moveUpDesc: String
    val moveDownDesc: String
    val editRatingWordTitle: String
    val newRatingWordTitle: String
    val ratingWordFieldLabel: String
    val chooseColorLabel: String
    val ratingWordSavedToast: (String) -> String
    val ratingWordRemovedToast: (String) -> String
    val starRangesSectionTitle: String
    val starRangesSectionDesc: String
    val editStarRangeTitle: String
    val fromScoreLabel: String
    val toScoreLabel: String
    val rangeLabelOptional: String
    val rangeSavedToast: String
    val rangeRemovedToast: String
    val languageSectionTitle: String
    val languageSectionDesc: String
    val languageChangedToast: (String) -> String

    // Notifications
    val notifChannelName: String
    val notifChannelDesc: String
    val notifPlayingTitle: (String) -> String
    val notifTimerRunning: String
    val notifPauseAction: String

    // Default Global Rating Words (ordered best -> worst)
    val defaultRatingWords: List<String>
    val defaultStarRangeLabels: List<String>
}

object AppStringsProvider {
    fun get(language: AppLanguage): AppStrings = when (language) {
        AppLanguage.ENGLISH -> EnglishStrings
        AppLanguage.GERMAN -> GermanStrings
        AppLanguage.SPANISH -> SpanishStrings
        AppLanguage.ITALIAN -> ItalianStrings
        AppLanguage.JAPANESE -> JapaneseStrings
        AppLanguage.CHINESE -> ChineseStrings
    }
}

val LocalAppStrings = compositionLocalOf<AppStrings> { EnglishStrings }
