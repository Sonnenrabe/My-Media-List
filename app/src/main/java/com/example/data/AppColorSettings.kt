package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.AppStringsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class RatingMode {
    WORDS, // Word-based rating using global rating words (e.g. Masterpiece, Very Good...)
    STARS; // Numeric 1.0 - 10.0 star principle

    companion object {
        fun fromString(value: String?, default: RatingMode): RatingMode {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: default
        }
    }
}

data class CategoryColorConfig(
    val categoryKey: String,
    val accentColorHex: Long,
    val textColorHex: Long = 0xFFF8FAFC,
    val customName: String = "",
    val ratingMode: RatingMode = if (categoryKey == AppColorSettingsManager.KEY_SPIELE) RatingMode.WORDS else RatingMode.STARS,
    val isGameCategory: Boolean = (categoryKey == AppColorSettingsManager.KEY_SPIELE)
)

data class GameRatingColor(
    val ratingWord: String,
    val colorHex: Long
)

data class MediaRatingRange(
    val minScore: Double,
    val maxScore: Double,
    val colorHex: Long,
    val label: String = ""
)

class AppColorSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        "app_color_settings_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        const val KEY_SPIELE = "SPIELE"
        const val KEY_SERIEN = "SERIEN"
        const val KEY_ANIME = "ANIME"
        const val KEY_MANGA = "MANGA"
        const val KEY_BUECHER = "BUECHER"

        val BUILT_IN_CATEGORY_KEYS = listOf(KEY_SPIELE, KEY_SERIEN, KEY_ANIME, KEY_MANGA, KEY_BUECHER)
        val ALL_CATEGORY_KEYS = BUILT_IN_CATEGORY_KEYS

        private const val KEY_LANGUAGE = "app_language_code"
        private const val KEY_CUSTOM_RATING_WORDS = "has_custom_rating_words"
        private const val KEY_CUSTOM_MEDIA_RANGES = "has_custom_media_ranges"
        private const val KEY_CATEGORY_ORDER = "category_order_json"

        val DEFAULT_RATING_COLORS_HEX = listOf(
            0xFF10B981L,
            0xFF38BDF8L,
            0xFF818CF8L,
            0xFFF59E0BL,
            0xFFFB7185L,
            0xFFEF4444L
        )

        val DEFAULT_MEDIA_RANGE_BOUNDS = listOf(
            9.0 to 10.0 to 0xFF10B981L,
            8.0 to 8.9 to 0xFF38BDF8L,
            7.0 to 7.9 to 0xFF818CF8L,
            5.0 to 6.9 to 0xFFF59E0BL,
            1.0 to 4.9 to 0xFFEF4444L
        )

        val DEFAULT_CATEGORY_COLORS = mapOf(
            KEY_SPIELE to CategoryColorConfig(KEY_SPIELE, 0xFF818CF8, 0xFFF8FAFC, "", RatingMode.WORDS, isGameCategory = true),
            KEY_SERIEN to CategoryColorConfig(KEY_SERIEN, 0xFFFB7185, 0xFFF8FAFC, "", RatingMode.STARS, isGameCategory = false),
            KEY_ANIME to CategoryColorConfig(KEY_ANIME, 0xFFA855F7, 0xFFF8FAFC, "", RatingMode.STARS, isGameCategory = false),
            KEY_MANGA to CategoryColorConfig(KEY_MANGA, 0xFF10B981, 0xFFF8FAFC, "", RatingMode.STARS, isGameCategory = false),
            KEY_BUECHER to CategoryColorConfig(KEY_BUECHER, 0xFFF59E0B, 0xFFF8FAFC, "", RatingMode.STARS, isGameCategory = false)
        )

        fun defaultGameRatingColors(language: AppLanguage = AppLanguage.ENGLISH): List<GameRatingColor> {
            val words = AppStringsProvider.get(language).defaultRatingWords
            return words.mapIndexed { index, word ->
                GameRatingColor(word, DEFAULT_RATING_COLORS_HEX.getOrElse(index) { 0xFF818CF8L })
            }
        }

        fun defaultMediaRatingRanges(language: AppLanguage = AppLanguage.ENGLISH): List<MediaRatingRange> {
            val labels = AppStringsProvider.get(language).defaultStarRangeLabels
            return DEFAULT_MEDIA_RANGE_BOUNDS.mapIndexed { index, (bounds, hex) ->
                MediaRatingRange(
                    minScore = bounds.first,
                    maxScore = bounds.second,
                    colorHex = hex,
                    label = labels.getOrElse(index) { "" }
                )
            }
        }

        val DEFAULT_GAME_RATING_COLORS: List<GameRatingColor> = defaultGameRatingColors(AppLanguage.ENGLISH)
        val DEFAULT_MEDIA_RATING_RANGES: List<MediaRatingRange> = defaultMediaRatingRanges(AppLanguage.ENGLISH)

        fun findDefaultRatingWordSlot(word: String): Int? {
            val clean = word.trim()
            if (clean.isEmpty()) return null
            for (lang in AppLanguage.entries) {
                val idx = AppStringsProvider.get(lang).defaultRatingWords.indexOfFirst {
                    it.equals(clean, ignoreCase = true)
                }
                if (idx >= 0) return idx
            }
            if (clean.equals("Satisfactory", ignoreCase = true)) return 3
            return null
        }

        fun isUnmodifiedDefaultWordsList(list: List<GameRatingColor>): Boolean {
            if (list.size != DEFAULT_RATING_COLORS_HEX.size) return false
            return list.indices.all { idx ->
                findDefaultRatingWordSlot(list[idx].ratingWord) == idx
            }
        }

        fun getSavedLanguage(context: Context): AppLanguage {
            val p = context.applicationContext.getSharedPreferences("app_color_settings_prefs", Context.MODE_PRIVATE)
            return AppLanguage.fromCode(p.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code))
        }

        fun getHeaderSurfaceColor(accentHex: Long): Color {
            val base = Color(accentHex)
            return Color(
                red = (0.08f * 0.86f + base.red * 0.14f).coerceIn(0f, 1f),
                green = (0.10f * 0.86f + base.green * 0.14f).coerceIn(0f, 1f),
                blue = (0.16f * 0.86f + base.blue * 0.14f).coerceIn(0f, 1f),
                alpha = 1.0f
            )
        }

        fun getSortingBarBgColor(accentHex: Long): Color {
            val base = Color(accentHex)
            return Color(
                red = (0.06f * 0.90f + base.red * 0.10f).coerceIn(0f, 1f),
                green = (0.07f * 0.90f + base.green * 0.10f).coerceIn(0f, 1f),
                blue = (0.12f * 0.90f + base.blue * 0.10f).coerceIn(0f, 1f),
                alpha = 1.0f
            )
        }
    }

    private val _appLanguage = MutableStateFlow(loadLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _categoryColors = MutableStateFlow(loadCategoryColors())
    val categoryColors: StateFlow<Map<String, CategoryColorConfig>> = _categoryColors.asStateFlow()

    private val _orderedCategoryKeys = MutableStateFlow(loadOrderedCategoryKeys(_categoryColors.value))
    val orderedCategoryKeys: StateFlow<List<String>> = _orderedCategoryKeys.asStateFlow()

    private val _gameRatingColors = MutableStateFlow(loadGameRatingColors())
    val gameRatingColors: StateFlow<List<GameRatingColor>> = _gameRatingColors.asStateFlow()

    private val _mediaRatingRanges = MutableStateFlow(loadMediaRatingRanges())
    val mediaRatingRanges: StateFlow<List<MediaRatingRange>> = _mediaRatingRanges.asStateFlow()

    private fun loadLanguage(): AppLanguage {
        return AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code))
    }

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()

        val newDefaults = AppStringsProvider.get(language).defaultRatingWords
        val hasCustomWords = prefs.getBoolean(KEY_CUSTOM_RATING_WORDS, false)
        if (!hasCustomWords && prefs.getString("game_rating_colors_json", null) == null) {
            _gameRatingColors.value = defaultGameRatingColors(language)
        } else {
            val translated = _gameRatingColors.value.map { item ->
                val slot = findDefaultRatingWordSlot(item.ratingWord)
                if (slot != null && slot in newDefaults.indices) {
                    item.copy(ratingWord = newDefaults[slot])
                } else {
                    item
                }
            }
            _gameRatingColors.value = translated
            persistGameRatingColorsJson(translated, markCustom = hasCustomWords)
        }

        if (!prefs.getBoolean(KEY_CUSTOM_MEDIA_RANGES, false)) {
            _mediaRatingRanges.value = defaultMediaRatingRanges(language)
        }
    }

    private fun loadCategoryColors(): Map<String, CategoryColorConfig> {
        val jsonStr = prefs.getString("category_colors_json", null) ?: return DEFAULT_CATEGORY_COLORS
        return try {
            val json = JSONObject(jsonStr)
            val result = linkedMapOf<String, CategoryColorConfig>()
            for (key in DEFAULT_CATEGORY_COLORS.keys) {
                val def = DEFAULT_CATEGORY_COLORS[key]!!
                if (json.has(key)) {
                    val obj = json.getJSONObject(key)
                    result[key] = CategoryColorConfig(
                        categoryKey = key,
                        accentColorHex = obj.optLong("accent", def.accentColorHex),
                        textColorHex = obj.optLong("text", def.textColorHex),
                        customName = obj.optString("name", ""),
                        ratingMode = RatingMode.fromString(obj.optString("ratingMode", def.ratingMode.name), def.ratingMode),
                        isGameCategory = obj.optBoolean("isGame", def.isGameCategory)
                    )
                } else {
                    result[key] = def
                }
            }
            val keysIter = json.keys()
            while (keysIter.hasNext()) {
                val key = keysIter.next()
                if (!result.containsKey(key)) {
                    val obj = json.getJSONObject(key)
                    result[key] = CategoryColorConfig(
                        categoryKey = key,
                        accentColorHex = obj.optLong("accent", 0xFF38BDF8L),
                        textColorHex = obj.optLong("text", 0xFFF8FAFC),
                        customName = obj.optString("name", key),
                        ratingMode = RatingMode.fromString(obj.optString("ratingMode", RatingMode.WORDS.name), RatingMode.WORDS),
                        isGameCategory = obj.optBoolean("isGame", false)
                    )
                }
            }
            result
        } catch (_: Exception) {
            DEFAULT_CATEGORY_COLORS
        }
    }

    private fun loadOrderedCategoryKeys(categories: Map<String, CategoryColorConfig>): List<String> {
        val jsonStr = prefs.getString(KEY_CATEGORY_ORDER, null)
        val result = mutableListOf<String>()
        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val k = array.optString(i)
                    if (k.isNotBlank() && categories.containsKey(k) && !result.contains(k)) {
                        result.add(k)
                    }
                }
            } catch (_: Exception) {}
        }
        for (k in BUILT_IN_CATEGORY_KEYS) {
            if (categories.containsKey(k) && !result.contains(k)) result.add(k)
        }
        for (k in categories.keys) {
            if (!result.contains(k)) result.add(k)
        }
        return result
    }

    private fun saveOrderedCategoryKeys(keys: List<String>) {
        val array = JSONArray()
        keys.forEach { array.put(it) }
        prefs.edit().putString(KEY_CATEGORY_ORDER, array.toString()).apply()
    }

    private fun loadGameRatingColors(): List<GameRatingColor> {
        val jsonStr = prefs.getString("game_rating_colors_json", null)
            ?: return defaultGameRatingColors(_appLanguage.value)
        return try {
            val array = JSONArray(jsonStr)
            val result = mutableListOf<GameRatingColor>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    GameRatingColor(
                        ratingWord = obj.getString("word"),
                        colorHex = obj.getLong("color")
                    )
                )
            }
            if (result.isEmpty()) defaultGameRatingColors(_appLanguage.value) else result
        } catch (_: Exception) {
            defaultGameRatingColors(_appLanguage.value)
        }
    }

    private fun loadMediaRatingRanges(): List<MediaRatingRange> {
        val jsonStr = prefs.getString("media_rating_ranges_json", null)
            ?: return defaultMediaRatingRanges(_appLanguage.value)
        return try {
            val array = JSONArray(jsonStr)
            val result = mutableListOf<MediaRatingRange>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    MediaRatingRange(
                        minScore = obj.getDouble("min"),
                        maxScore = obj.getDouble("max"),
                        colorHex = obj.getLong("color"),
                        label = obj.optString("label", "")
                    )
                )
            }
            if (result.isEmpty()) defaultMediaRatingRanges(_appLanguage.value) else result
        } catch (_: Exception) {
            defaultMediaRatingRanges(_appLanguage.value)
        }
    }

    fun setCategoryColor(key: String, accentHex: Long, textHex: Long = 0xFFF8FAFC) {
        val current = _categoryColors.value.toMutableMap()
        val existing = current[key] ?: DEFAULT_CATEGORY_COLORS[key] ?: CategoryColorConfig(key, accentHex, textHex)
        current[key] = existing.copy(accentColorHex = accentHex, textColorHex = textHex)
        _categoryColors.value = current
        saveCategoryColors(current)
    }

    fun setCategoryConfig(
        key: String,
        accentHex: Long,
        customName: String,
        ratingMode: RatingMode,
        textHex: Long = 0xFFF8FAFC,
        isGameCategory: Boolean? = null
    ) {
        val current = _categoryColors.value.toMutableMap()
        val existing = current[key] ?: DEFAULT_CATEGORY_COLORS[key]
        val resolvedIsGame = isGameCategory ?: existing?.isGameCategory ?: (key == KEY_SPIELE)
        current[key] = CategoryColorConfig(
            categoryKey = key,
            accentColorHex = accentHex,
            textColorHex = textHex,
            customName = customName.trim(),
            ratingMode = ratingMode,
            isGameCategory = resolvedIsGame
        )
        _categoryColors.value = current
        if (!_orderedCategoryKeys.value.contains(key)) {
            val newOrder = _orderedCategoryKeys.value + key
            _orderedCategoryKeys.value = newOrder
            saveOrderedCategoryKeys(newOrder)
        }
        saveCategoryColors(current)
    }

    fun createCustomCategory(
        name: String,
        accentHex: Long,
        ratingMode: RatingMode,
        isGameCategory: Boolean
    ): String {
        val current = _categoryColors.value.toMutableMap()
        var candidate = "CAT_${System.currentTimeMillis()}"
        var suffix = 1
        while (current.containsKey(candidate)) {
            candidate = "CAT_${System.currentTimeMillis()}_$suffix"
            suffix++
        }
        current[candidate] = CategoryColorConfig(
            categoryKey = candidate,
            accentColorHex = accentHex,
            textColorHex = 0xFFF8FAFC,
            customName = name.trim(),
            ratingMode = ratingMode,
            isGameCategory = isGameCategory
        )
        _categoryColors.value = current
        val newOrder = _orderedCategoryKeys.value + candidate
        _orderedCategoryKeys.value = newOrder
        saveCategoryColors(current)
        saveOrderedCategoryKeys(newOrder)
        return candidate
    }

    fun deleteCustomCategory(key: String) {
        if (isBuiltInCategory(key)) return
        val current = _categoryColors.value.toMutableMap()
        current.remove(key)
        _categoryColors.value = current
        val newOrder = _orderedCategoryKeys.value.filter { it != key }
        _orderedCategoryKeys.value = newOrder
        saveCategoryColors(current)
        saveOrderedCategoryKeys(newOrder)
    }

    fun setAllCategoryColors(map: Map<String, CategoryColorConfig>) {
        val merged = linkedMapOf<String, CategoryColorConfig>()
        merged.putAll(DEFAULT_CATEGORY_COLORS)
        merged.putAll(map)
        _categoryColors.value = merged
        val customKeys = map.keys.filter { it !in BUILT_IN_CATEGORY_KEYS }
        val newOrder = BUILT_IN_CATEGORY_KEYS + customKeys
        _orderedCategoryKeys.value = newOrder
        saveCategoryColors(merged)
        saveOrderedCategoryKeys(newOrder)
    }

    private fun saveCategoryColors(map: Map<String, CategoryColorConfig>) {
        val json = JSONObject()
        for ((k, v) in map) {
            val obj = JSONObject()
            obj.put("accent", v.accentColorHex)
            obj.put("text", v.textColorHex)
            obj.put("name", v.customName)
            obj.put("ratingMode", v.ratingMode.name)
            obj.put("isGame", v.isGameCategory)
            json.put(k, obj)
        }
        prefs.edit().putString("category_colors_json", json.toString()).apply()
    }

    fun setGameRatingColors(list: List<GameRatingColor>, allowTranslateIfDefault: Boolean = true) {
        val normalized = if (allowTranslateIfDefault && isUnmodifiedDefaultWordsList(list)) {
            val currentDefaults = AppStringsProvider.get(_appLanguage.value).defaultRatingWords
            list.mapIndexed { idx, item ->
                item.copy(ratingWord = currentDefaults.getOrElse(idx) { item.ratingWord })
            }
        } else {
            list
        }
        val isCustom = !(allowTranslateIfDefault && isUnmodifiedDefaultWordsList(normalized))
        _gameRatingColors.value = normalized
        persistGameRatingColorsJson(normalized, markCustom = isCustom)
    }

    private fun persistGameRatingColorsJson(list: List<GameRatingColor>, markCustom: Boolean) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("word", item.ratingWord)
            obj.put("color", item.colorHex)
            array.put(obj)
        }
        prefs.edit()
            .putString("game_rating_colors_json", array.toString())
            .putBoolean(KEY_CUSTOM_RATING_WORDS, markCustom)
            .apply()
    }

    fun setMediaRatingRanges(list: List<MediaRatingRange>) {
        _mediaRatingRanges.value = list
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("min", item.minScore)
            obj.put("max", item.maxScore)
            obj.put("color", item.colorHex)
            obj.put("label", item.label)
            array.put(obj)
        }
        prefs.edit()
            .putString("media_rating_ranges_json", array.toString())
            .putBoolean(KEY_CUSTOM_MEDIA_RANGES, true)
            .apply()
    }

    fun resetToDefaults() {
        val lang = _appLanguage.value
        _categoryColors.value = DEFAULT_CATEGORY_COLORS
        _orderedCategoryKeys.value = BUILT_IN_CATEGORY_KEYS
        _gameRatingColors.value = defaultGameRatingColors(lang)
        _mediaRatingRanges.value = defaultMediaRatingRanges(lang)
        prefs.edit()
            .remove("category_colors_json")
            .remove(KEY_CATEGORY_ORDER)
            .remove("game_rating_colors_json")
            .remove("media_rating_ranges_json")
            .putBoolean(KEY_CUSTOM_RATING_WORDS, false)
            .putBoolean(KEY_CUSTOM_MEDIA_RANGES, false)
            .apply()
    }

    fun isBuiltInCategory(key: String): Boolean = key in BUILT_IN_CATEGORY_KEYS

    fun isGameCategory(key: String): Boolean {
        return _categoryColors.value[key]?.isGameCategory
            ?: DEFAULT_CATEGORY_COLORS[key]?.isGameCategory
            ?: (key == KEY_SPIELE)
    }

    fun getCategoryRatingMode(key: String): RatingMode {
        return _categoryColors.value[key]?.ratingMode
            ?: DEFAULT_CATEGORY_COLORS[key]?.ratingMode
            ?: RatingMode.STARS
    }

    fun getCategoryDisplayName(key: String, strings: AppStrings): String {
        val custom = _categoryColors.value[key]?.customName?.trim().orEmpty()
        if (custom.isNotEmpty()) return custom
        return when (key) {
            KEY_SPIELE -> strings.navGames
            KEY_SERIEN -> strings.navSeries
            KEY_ANIME -> strings.navAnime
            KEY_MANGA -> strings.navManga
            KEY_BUECHER -> strings.navBooks
            else -> key
        }
    }

    fun resolveDisplayRatingWord(rawWord: String): String {
        val clean = rawWord.trim()
        if (clean.isEmpty()) return ""
        val exact = _gameRatingColors.value.firstOrNull {
            it.ratingWord.equals(clean, ignoreCase = true)
        }
        if (exact != null) return exact.ratingWord

        val slot = findDefaultRatingWordSlot(clean)
        if (slot != null) {
            val currentDefault = AppStringsProvider.get(_appLanguage.value).defaultRatingWords.getOrNull(slot)
            if (currentDefault != null) {
                val activeMatch = _gameRatingColors.value.firstOrNull {
                    it.ratingWord.equals(currentDefault, ignoreCase = true)
                }
                if (activeMatch != null) return activeMatch.ratingWord
            }
        }
        return clean
    }

    fun getColorForGameRating(rating: String, fallback: Color): Color {
        val clean = resolveDisplayRatingWord(rating)
        if (clean.isEmpty()) return fallback
        val match = _gameRatingColors.value.firstOrNull {
            it.ratingWord.equals(clean, ignoreCase = true)
        }
        return if (match != null) Color(match.colorHex) else fallback
    }

    fun getRatingWordRank(rating: String): Int {
        val clean = resolveDisplayRatingWord(rating)
        if (clean.isEmpty()) return Int.MAX_VALUE
        val index = _gameRatingColors.value.indexOfFirst {
            it.ratingWord.equals(clean, ignoreCase = true)
        }
        return if (index >= 0) index else Int.MAX_VALUE - 1
    }

    fun getColorForMediaScore(score: Double, fallback: Color): Color {
        if (score <= 0.0) return fallback
        val rounded = Math.round(score * 10.0) / 10.0
        val match = _mediaRatingRanges.value.firstOrNull {
            rounded >= (it.minScore - 0.05) && rounded <= (it.maxScore + 0.05)
        }
        return if (match != null) Color(match.colorHex) else fallback
    }
}
