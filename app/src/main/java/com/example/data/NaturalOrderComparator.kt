package com.example.data

import java.math.BigInteger

/**
 * Natural / Alphanumeric Order Comparator.
 * Ensures that numeric strings like "Staffel 1", "Staffel 2", "Staffel 10", "Staffel 11"
 * are ordered numerically (1, 2, ... 10, 11) rather than lexicographically (1, 11, 2).
 */
object NaturalOrderComparator : Comparator<String> {

    override fun compare(s1: String?, s2: String?): Int {
        if (s1 == null && s2 == null) return 0
        if (s1 == null) return -1
        if (s2 == null) return 1
        return naturalCompare(s1, s2)
    }

    /**
     * Compares two strings using natural/alphanumeric order.
     */
    fun naturalCompare(str1: String, str2: String): Int {
        val s1 = str1.trim()
        val s2 = str2.trim()

        var i1 = 0
        var i2 = 0
        val len1 = s1.length
        val len2 = s2.length

        while (i1 < len1 && i2 < len2) {
            val c1 = s1[i1]
            val c2 = s2[i2]

            if (c1.isDigit() && c2.isDigit()) {
                // Extract full numbers
                var j1 = i1
                while (j1 < len1 && s1[j1].isDigit()) j1++
                var j2 = i2
                while (j2 < len2 && s2[j2].isDigit()) j2++

                val numStr1 = s1.substring(i1, j1)
                val numStr2 = s2.substring(i2, j2)

                val numCompare = compareNumbers(numStr1, numStr2)
                if (numCompare != 0) return numCompare

                i1 = j1
                i2 = j2
            } else {
                // Compare characters case-insensitively
                val lower1 = c1.lowercaseChar()
                val lower2 = c2.lowercaseChar()
                if (lower1 != lower2) {
                    return lower1.compareTo(lower2)
                }
                i1++
                i2++
            }
        }

        return len1.compareTo(len2)
    }

    private fun compareNumbers(num1: String, num2: String): Int {
        // Strip leading zeros for comparison
        val stripped1 = num1.trimStart('0').ifEmpty { "0" }
        val stripped2 = num2.trimStart('0').ifEmpty { "0" }

        if (stripped1.length != stripped2.length) {
            return stripped1.length.compareTo(stripped2.length)
        }

        try {
            val val1 = stripped1.toLong()
            val val2 = stripped2.toLong()
            return val1.compareTo(val2)
        } catch (_: NumberFormatException) {
            val bi1 = BigInteger(stripped1)
            val bi2 = BigInteger(stripped2)
            return bi1.compareTo(bi2)
        }
    }

    /**
     * Specialized comparator for media seasons / status strings (Series, Anime, Manga).
     * Groups entries logically:
     * 1. Active / numbered entries (Staffel 1, Staffel 2, Folge 15, Kapitel 3, etc.) ordered naturally
     * 2. "Finished" / "Abgeschlossen"
     * 3. "Quit" / "Abgebrochen"
     * 4. Other custom notes
     */
    fun compareStatus(status1: String, status2: String): Int {
        val tier1 = getStatusTier(status1)
        val tier2 = getStatusTier(status2)

        return if (tier1 != tier2) {
            tier1.compareTo(tier2)
        } else {
            naturalCompare(status1, status2)
        }
    }

    private fun getStatusTier(status: String): Int {
        val trimmed = status.trim().lowercase()
        return when {
            trimmed.contains("finished") || trimmed.contains("abgeschlossen") -> 2
            trimmed.contains("quit") || trimmed.contains("abgebrochen") -> 3
            trimmed.any { it.isDigit() } -> 1 // Active numbered seasons, episodes, chapters
            else -> 4
        }
    }
}
