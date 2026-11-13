package com.example.data

import com.example.localization.AppLanguage
import org.json.JSONArray
import org.json.JSONObject

data class FullBackupData(
    val games: List<Game> = emptyList(),
    val series: List<SeriesItem> = emptyList(),
    val anime: List<AnimeItem> = emptyList(),
    val manga: List<MangaItem> = emptyList(),
    val books: List<BookItem> = emptyList(),
    val topTenEntries: List<TopTenEntry> = emptyList(),
    val categoryColors: Map<String, CategoryColorConfig> = emptyMap(),
    val gameRatingColors: List<GameRatingColor> = emptyList(),
    val mediaRatingRanges: List<MediaRatingRange> = emptyList(),
    val appLanguage: AppLanguage? = null,
    val autoBackupEnabled: Boolean? = null
)

data class FullBackupSummary(
    val gameCount: Int,
    val seriesCount: Int,
    val animeCount: Int,
    val mangaCount: Int,
    val bookCount: Int,
    val topTenCount: Int,
    val data: FullBackupData
) {
    val totalItems: Int get() = gameCount + seriesCount + animeCount + mangaCount + bookCount
}

object DataExportImportManager {

    /**
     * Generates an Excel-compatible CSV file with UTF-8 BOM (\uFEFF) and semicolon (;) delimiter.
     * When opened in Microsoft Excel or Google Sheets, it cleanly formats into columns and is directly readable.
     */
    fun exportToExcelCsv(data: FullBackupData): String {
        val sb = StringBuilder()
        // UTF-8 Byte Order Mark so Excel immediately recognises UTF-8 and German umlauts
        sb.append('\uFEFF')

        // 1. SPIELE
        sb.append("### SPIELE ###\n")
        sb.append("Titel;Spielzeit;Spielzeit_Sekunden;Bewertung;Notizen;Archiviert;Angeheftet_PlayTime;Sortierung;SterneBewertung;Kategorie\n")
        for (g in data.games) {
            val title = escapeCsv(g.title)
            val playtime = g.formattedPlaytime()
            val sec = g.totalPlaytimeSeconds
            val rating = escapeCsv(g.rating)
            val notes = escapeCsv(g.notes)
            val archived = if (g.isArchived) "JA" else "NEIN"
            val pinned = if (g.isPinnedToPlaytime) "JA" else "NEIN"
            val sort = g.sortOrder
            val starStr = if (g.starRating > 0.0) String.format(java.util.Locale.US, "%.1f", g.starRating) else ""
            val categoryKey = escapeCsv(g.categoryKey)
            sb.append("$title;$playtime;$sec;$rating;$notes;$archived;$pinned;$sort;$starStr;$categoryKey\n")
        }
        sb.append("\n")

        // 2. SERIEN
        sb.append("### SERIEN ###\n")
        sb.append("Name;Staffel;Bewertung;Notizen;Sortierung;Bewertungswort;Spielzeit_Sekunden;Angeheftet_PlayTime\n")
        for (s in data.series) {
            val name = escapeCsv(s.name)
            val season = escapeCsv(s.season)
            val rating = if (s.rating > 0.0) String.format(java.util.Locale.US, "%.1f", s.rating) else ""
            val notes = escapeCsv(s.notes)
            val sort = s.sortOrder
            val rWord = escapeCsv(s.ratingWord)
            val sec = s.totalPlaytimeSeconds
            val pinned = if (s.isPinnedToPlaytime) "JA" else "NEIN"
            sb.append("$name;$season;$rating;$notes;$sort;$rWord;$sec;$pinned\n")
        }
        sb.append("\n")

        // 3. ANIME
        sb.append("### ANIME ###\n")
        sb.append("Name;Staffel;Bewertung;Notizen;Sortierung;Bewertungswort;Spielzeit_Sekunden;Angeheftet_PlayTime\n")
        for (a in data.anime) {
            val name = escapeCsv(a.name)
            val season = escapeCsv(a.season)
            val rating = if (a.rating > 0.0) String.format(java.util.Locale.US, "%.1f", a.rating) else ""
            val notes = escapeCsv(a.notes)
            val sort = a.sortOrder
            val rWord = escapeCsv(a.ratingWord)
            val sec = a.totalPlaytimeSeconds
            val pinned = if (a.isPinnedToPlaytime) "JA" else "NEIN"
            sb.append("$name;$season;$rating;$notes;$sort;$rWord;$sec;$pinned\n")
        }
        sb.append("\n")

        // 4. MANGA
        sb.append("### MANGA ###\n")
        sb.append("Titel;Kapitel;Bewertung;Notizen;Sortierung;Bewertungswort;Spielzeit_Sekunden;Angeheftet_PlayTime\n")
        for (m in data.manga) {
            val title = escapeCsv(m.title)
            val chapter = escapeCsv(m.chapter)
            val rating = if (m.rating > 0.0) String.format(java.util.Locale.US, "%.1f", m.rating) else ""
            val notes = escapeCsv(m.notes)
            val sort = m.sortOrder
            val rWord = escapeCsv(m.ratingWord)
            val sec = m.totalPlaytimeSeconds
            val pinned = if (m.isPinnedToPlaytime) "JA" else "NEIN"
            sb.append("$title;$chapter;$rating;$notes;$sort;$rWord;$sec;$pinned\n")
        }
        sb.append("\n")

        // 5. BÜCHER
        sb.append("### BUECHER ###\n")
        sb.append("Titel;Autor;Bewertung;Notizen;Sortierung;Bewertungswort;Spielzeit_Sekunden;Angeheftet_PlayTime\n")
        for (b in data.books) {
            val title = escapeCsv(b.title)
            val author = escapeCsv(b.author)
            val rating = if (b.rating > 0.0) String.format(java.util.Locale.US, "%.1f", b.rating) else ""
            val notes = escapeCsv(b.notes)
            val sort = b.sortOrder
            val rWord = escapeCsv(b.ratingWord)
            val sec = b.totalPlaytimeSeconds
            val pinned = if (b.isPinnedToPlaytime) "JA" else "NEIN"
            sb.append("$title;$author;$rating;$notes;$sort;$rWord;$sec;$pinned\n")
        }
        sb.append("\n")

        // 6. TOP 10
        sb.append("### TOP 10 ###\n")
        sb.append("MedienTyp;Kategorie;Platz;Titel;Zusatz\n")
        for (t in data.topTenEntries) {
            val type = escapeCsv(t.mediaType)
            val cat = escapeCsv(t.categoryName)
            val rank = t.rank
            val title = escapeCsv(t.title)
            val sub = escapeCsv(t.subtitle)
            sb.append("$type;$cat;$rank;$title;$sub\n")
        }

        // 7. KATEGORIE-FARBEN & EINSTELLUNGEN
        if (data.categoryColors.isNotEmpty()) {
            sb.append("\n### EINSTELLUNGEN_KATEGORIEN ###\n")
            sb.append("Kategorie;AkzentFarbe;TextFarbe;Name;BewertungsModus;SpieleKategorie\n")
            for ((key, cfg) in data.categoryColors) {
                val isGameFlag = if (cfg.isGameCategory) "JA" else "NEIN"
                sb.append("${escapeCsv(key)};${formatHexColor(cfg.accentColorHex)};${formatHexColor(cfg.textColorHex)};${escapeCsv(cfg.customName)};${cfg.ratingMode.name};$isGameFlag\n")
            }
        }

        // 8. BEWERTUNGSFARBEN SPIELE
        if (data.gameRatingColors.isNotEmpty()) {
            sb.append("\n### EINSTELLUNGEN_BEWERTUNGEN_SPIELE ###\n")
            sb.append("Wort;Farbe\n")
            for (rc in data.gameRatingColors) {
                sb.append("${escapeCsv(rc.ratingWord)};${formatHexColor(rc.colorHex)}\n")
            }
        }

        // 9. BEWERTUNGSFARBEN MEDIEN (RANGES)
        if (data.mediaRatingRanges.isNotEmpty()) {
            sb.append("\n### EINSTELLUNGEN_BEWERTUNGEN_MEDIEN ###\n")
            sb.append("Min;Max;Farbe;Bezeichnung\n")
            for (rr in data.mediaRatingRanges) {
                val min = String.format(java.util.Locale.US, "%.1f", rr.minScore)
                val max = String.format(java.util.Locale.US, "%.1f", rr.maxScore)
                sb.append("$min;$max;${formatHexColor(rr.colorHex)};${escapeCsv(rr.label)}\n")
            }
        }

        // 10. APP-EINSTELLUNGEN (SPRACHE & AUTO-BACKUP)
        if (data.appLanguage != null || data.autoBackupEnabled != null) {
            sb.append("\n### EINSTELLUNGEN_APP ###\n")
            sb.append("Schluessel;Wert\n")
            data.appLanguage?.let { lang ->
                sb.append("Sprache;${lang.code}\n")
            }
            data.autoBackupEnabled?.let { enabled ->
                sb.append("AutoBackup;${if (enabled) "JA" else "NEIN"}\n")
            }
        }

        return sb.toString()
    }

    fun formatHexColor(color: Long): String {
        val hex = (color and 0xFFFFFFFFL).toString(16).uppercase().padStart(8, '0')
        return "#$hex"
    }

    fun parseHexColor(hexStr: String, default: Long): Long {
        val clean = hexStr.trim().removePrefix("#")
        return try {
            if (clean.length == 6) {
                ("FF$clean").toLong(16)
            } else if (clean.length == 8) {
                clean.toLong(16)
            } else {
                clean.toLongOrNull() ?: default
            }
        } catch (_: Exception) {
            default
        }
    }

    private fun escapeCsv(value: String): String {
        var clean = value.replace("\r", " ").replace("\n", " ")
        if (clean.contains(";") || clean.contains("\"")) {
            clean = "\"" + clean.replace("\"", "\"\"") + "\""
        }
        return clean
    }

    private fun unescapeCsv(field: String): String {
        var s = field.trim()
        if (s.startsWith("\"") && s.endsWith("\"") && s.length >= 2) {
            s = s.substring(1, s.length - 1).replace("\"\"", "\"")
        }
        return s
    }

    /**
     * Parses an imported string which can be either:
     * 1. Our structured Excel CSV with section headers
     * 2. Legacy JSON backup format
     */
    fun parseBackup(content: String): Result<FullBackupSummary> {
        val trimmed = content.trim().removePrefix("\uFEFF").trim()
        return if (trimmed.startsWith("{")) {
            parseJsonBackup(trimmed)
        } else {
            parseCsvBackup(trimmed)
        }
    }

    private fun parseCsvBackup(content: String): Result<FullBackupSummary> {
        return try {
            val games = mutableListOf<Game>()
            val series = mutableListOf<SeriesItem>()
            val anime = mutableListOf<AnimeItem>()
            val manga = mutableListOf<MangaItem>()
            val books = mutableListOf<BookItem>()
            val topTen = mutableListOf<TopTenEntry>()
            val categoryColors = linkedMapOf<String, CategoryColorConfig>()
            val gameRatingColors = mutableListOf<GameRatingColor>()
            val mediaRatingRanges = mutableListOf<MediaRatingRange>()
            var appLanguage: AppLanguage? = null
            var autoBackupEnabled: Boolean? = null

            var currentSection = ""
            val lines = content.lines()

            for (rawLine in lines) {
                val line = rawLine.trim()
                if (line.isEmpty()) continue

                if (line.startsWith("###") && line.endsWith("###")) {
                    currentSection = line.replace("#", "").trim().uppercase()
                    continue
                }

                // Split line by ';' respecting quotes
                val tokens = parseCsvLine(line)
                if (tokens.isEmpty()) continue

                // Skip header rows
                val first = tokens[0].trim().uppercase()
                if (first == "TITEL" || first == "NAME" || first == "MEDIENTYP" || first == "KATEGORIE" || first == "WORT" || first == "MIN" || first == "SCHLUESSEL") continue

                when (currentSection) {
                    "SPIELE" -> {
                        // Titel;Spielzeit;Spielzeit_Sekunden;Bewertung;Notizen;Archiviert;Angeheftet_PlayTime;Sortierung;SterneBewertung;Kategorie
                        val title = tokens.getOrNull(0) ?: "Untitled Game"
                        val sec = tokens.getOrNull(2)?.toLongOrNull() ?: 0L
                        val rating = tokens.getOrNull(3) ?: ""
                        val notes = tokens.getOrNull(4) ?: ""
                        val archived = tokens.getOrNull(5)?.equals("JA", ignoreCase = true) == true
                        val pinned = tokens.getOrNull(6)?.equals("NEIN", ignoreCase = true) != true
                        val sort = tokens.getOrNull(7)?.toIntOrNull() ?: games.size
                        val starRating = tokens.getOrNull(8)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                        val categoryKey = tokens.getOrNull(9)?.takeIf { it.isNotBlank() } ?: AppColorSettingsManager.KEY_SPIELE
                        games.add(
                            Game(
                                title = title,
                                totalPlaytimeSeconds = sec,
                                rating = rating,
                                starRating = starRating,
                                notes = notes,
                                isArchived = archived,
                                isPinnedToPlaytime = pinned,
                                sortOrder = sort,
                                categoryKey = categoryKey
                            )
                        )
                    }
                    "SERIEN" -> {
                        // Name;Staffel;Bewertung;Notizen;Sortierung;Bewertungswort;Spielzeit_Sekunden;Angeheftet_PlayTime
                        val name = tokens.getOrNull(0) ?: "Untitled Series"
                        val season = tokens.getOrNull(1) ?: "Season 1"
                        val rating = tokens.getOrNull(2)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                        val notes = tokens.getOrNull(3) ?: ""
                        val sort = tokens.getOrNull(4)?.toIntOrNull() ?: series.size
                        val ratingWord = tokens.getOrNull(5) ?: ""
                        val playtimeSec = tokens.getOrNull(6)?.toLongOrNull() ?: 0L
                        val pinned = tokens.getOrNull(7)?.equals("JA", ignoreCase = true) == true
                        series.add(
                            SeriesItem(
                                name = name,
                                season = season,
                                rating = rating,
                                ratingWord = ratingWord,
                                notes = notes,
                                sortOrder = sort,
                                totalPlaytimeSeconds = playtimeSec,
                                isPinnedToPlaytime = pinned
                            )
                        )
                    }
                    "ANIME" -> {
                        // Name;Staffel;Bewertung;Notizen;Sortierung;Bewertungswort;Spielzeit_Sekunden;Angeheftet_PlayTime
                        val name = tokens.getOrNull(0) ?: "Untitled Anime"
                        val season = tokens.getOrNull(1) ?: "Season 1"
                        val rating = tokens.getOrNull(2)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                        val notes = tokens.getOrNull(3) ?: ""
                        val sort = tokens.getOrNull(4)?.toIntOrNull() ?: anime.size
                        val ratingWord = tokens.getOrNull(5) ?: ""
                        val playtimeSec = tokens.getOrNull(6)?.toLongOrNull() ?: 0L
                        val pinned = tokens.getOrNull(7)?.equals("JA", ignoreCase = true) == true
                        anime.add(
                            AnimeItem(
                                name = name,
                                season = season,
                                rating = rating,
                                ratingWord = ratingWord,
                                notes = notes,
                                sortOrder = sort,
                                totalPlaytimeSeconds = playtimeSec,
                                isPinnedToPlaytime = pinned
                            )
                        )
                    }
                    "MANGA" -> {
                        // Titel;Kapitel;Bewertung;Notizen;Sortierung;Bewertungswort;Spielzeit_Sekunden;Angeheftet_PlayTime
                        val title = tokens.getOrNull(0) ?: "Untitled Manga"
                        val chapter = tokens.getOrNull(1) ?: "Chapter 1"
                        val rating = tokens.getOrNull(2)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                        val notes = tokens.getOrNull(3) ?: ""
                        val sort = tokens.getOrNull(4)?.toIntOrNull() ?: manga.size
                        val ratingWord = tokens.getOrNull(5) ?: ""
                        val playtimeSec = tokens.getOrNull(6)?.toLongOrNull() ?: 0L
                        val pinned = tokens.getOrNull(7)?.equals("JA", ignoreCase = true) == true
                        manga.add(
                            MangaItem(
                                title = title,
                                chapter = chapter,
                                rating = rating,
                                ratingWord = ratingWord,
                                notes = notes,
                                sortOrder = sort,
                                totalPlaytimeSeconds = playtimeSec,
                                isPinnedToPlaytime = pinned
                            )
                        )
                    }
                    "BUECHER" -> {
                        // Titel;Autor;Bewertung;Notizen;Sortierung;Bewertungswort;Spielzeit_Sekunden;Angeheftet_PlayTime
                        val title = tokens.getOrNull(0) ?: "Untitled Book"
                        val author = tokens.getOrNull(1) ?: ""
                        val rating = tokens.getOrNull(2)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                        val notes = tokens.getOrNull(3) ?: ""
                        val sort = tokens.getOrNull(4)?.toIntOrNull() ?: books.size
                        val ratingWord = tokens.getOrNull(5) ?: ""
                        val playtimeSec = tokens.getOrNull(6)?.toLongOrNull() ?: 0L
                        val pinned = tokens.getOrNull(7)?.equals("JA", ignoreCase = true) == true
                        books.add(
                            BookItem(
                                title = title,
                                author = author,
                                rating = rating,
                                ratingWord = ratingWord,
                                notes = notes,
                                sortOrder = sort,
                                totalPlaytimeSeconds = playtimeSec,
                                isPinnedToPlaytime = pinned
                            )
                        )
                    }
                    "TOP 10" -> {
                        // MedienTyp;Kategorie;Platz;Titel;Zusatz
                        val mediaType = tokens.getOrNull(0) ?: "SPIELE"
                        val category = tokens.getOrNull(1) ?: "Top 10"
                        val rank = tokens.getOrNull(2)?.toIntOrNull() ?: (topTen.size + 1)
                        val title = tokens.getOrNull(3) ?: ""
                        val sub = tokens.getOrNull(4) ?: ""
                        if (title.isNotBlank()) {
                            topTen.add(
                                TopTenEntry(
                                    mediaType = mediaType,
                                    categoryName = category,
                                    rank = rank,
                                    title = title,
                                    subtitle = sub
                                )
                            )
                        }
                    }
                    "EINSTELLUNGEN_KATEGORIEN" -> {
                        val key = tokens.getOrNull(0) ?: ""
                        val accent = parseHexColor(tokens.getOrNull(1) ?: "", 0xFF818CF8)
                        val text = parseHexColor(tokens.getOrNull(2) ?: "", 0xFFF8FAFC)
                        val customName = tokens.getOrNull(3) ?: ""
                        val defaultMode = AppColorSettingsManager.DEFAULT_CATEGORY_COLORS[key]?.ratingMode ?: RatingMode.STARS
                        val ratingMode = RatingMode.fromString(tokens.getOrNull(4), defaultMode)
                        val defaultIsGame = key == AppColorSettingsManager.KEY_SPIELE
                        val isGameCategory = tokens.getOrNull(5)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { it.equals("JA", ignoreCase = true) }
                            ?: defaultIsGame
                        if (key.isNotBlank()) {
                            categoryColors[key] = CategoryColorConfig(
                                categoryKey = key,
                                accentColorHex = accent,
                                textColorHex = text,
                                customName = customName,
                                ratingMode = ratingMode,
                                isGameCategory = isGameCategory
                            )
                        }
                    }
                    "EINSTELLUNGEN_BEWERTUNGEN_SPIELE" -> {
                        val word = tokens.getOrNull(0) ?: ""
                        val col = parseHexColor(tokens.getOrNull(1) ?: "", 0xFF10B981)
                        if (word.isNotBlank()) {
                            gameRatingColors.add(GameRatingColor(word, col))
                        }
                    }
                    "EINSTELLUNGEN_BEWERTUNGEN_MEDIEN" -> {
                        val min = tokens.getOrNull(0)?.replace(",", ".")?.toDoubleOrNull() ?: 1.0
                        val max = tokens.getOrNull(1)?.replace(",", ".")?.toDoubleOrNull() ?: 10.0
                        val col = parseHexColor(tokens.getOrNull(2) ?: "", 0xFF10B981)
                        val label = tokens.getOrNull(3) ?: ""
                        mediaRatingRanges.add(MediaRatingRange(min, max, col, label))
                    }
                    "EINSTELLUNGEN_APP" -> {
                        val key = tokens.getOrNull(0)?.trim() ?: ""
                        val value = tokens.getOrNull(1)?.trim() ?: ""
                        if (key.equals("Sprache", ignoreCase = true) && value.isNotEmpty()) {
                            appLanguage = AppLanguage.fromCode(value)
                        } else if (key.equals("AutoBackup", ignoreCase = true) && value.isNotEmpty()) {
                            autoBackupEnabled = value.equals("JA", ignoreCase = true)
                        }
                    }
                }
            }

            val data = FullBackupData(
                games = games,
                series = series,
                anime = anime,
                manga = manga,
                books = books,
                topTenEntries = topTen,
                categoryColors = categoryColors,
                gameRatingColors = gameRatingColors,
                mediaRatingRanges = mediaRatingRanges,
                appLanguage = appLanguage,
                autoBackupEnabled = autoBackupEnabled
            )

            Result.success(
                FullBackupSummary(
                    gameCount = games.size,
                    seriesCount = series.size,
                    animeCount = anime.size,
                    mangaCount = manga.size,
                    bookCount = books.size,
                    topTenCount = topTen.size,
                    data = data
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    current.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ';' && !inQuotes) {
                result.add(current.toString().trim())
                current.setLength(0)
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    private fun parseJsonBackup(json: String): Result<FullBackupSummary> {
        return try {
            val root = JSONObject(json)
            val games = mutableListOf<Game>()
            if (root.has("games")) {
                val array = root.getJSONArray("games")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    games.add(
                        Game(
                            title = obj.optString("title", "Game"),
                            totalPlaytimeSeconds = obj.optLong("totalPlaytimeSeconds", 0L),
                            isArchived = obj.optBoolean("isArchived", false),
                            colorHex = obj.optLong("colorHex", 0xFF00E5FF),
                            notes = obj.optString("notes", ""),
                            sortOrder = obj.optInt("sortOrder", i),
                            rating = obj.optString("rating", ""),
                            starRating = obj.optDouble("starRating", 0.0),
                            isPinnedToPlaytime = obj.optBoolean("isPinnedToPlaytime", true)
                        )
                    )
                }
            }

            val data = FullBackupData(games = games)
            Result.success(
                FullBackupSummary(
                    gameCount = games.size,
                    seriesCount = 0,
                    animeCount = 0,
                    mangaCount = 0,
                    bookCount = 0,
                    topTenCount = 0,
                    data = data
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
