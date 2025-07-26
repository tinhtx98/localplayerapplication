package com.tinhtx.localplayerapplication.presentation.shared.extension

import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

/**
 * String extension functions for common operations
 */

// Text processing extensions
fun String.capitalize(): String = 
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun String.capitalizeWords(): String = 
    split(" ").joinToString(" ") { it.capitalize() }

fun String.removeAccents(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(normalized).replaceAll("")
}

fun String.toCamelCase(): String {
    return split(" ", "_", "-").mapIndexed { index, word ->
        if (index == 0) word.lowercase() else word.capitalize()
    }.joinToString("")
}

fun String.toSnakeCase(): String {
    return replace(Regex("([a-z])([A-Z])"), "$1_$2")
        .replace(" ", "_")
        .replace("-", "_")
        .lowercase()
}

fun String.toKebabCase(): String {
    return replace(Regex("([a-z])([A-Z])"), "$1-$2")
        .replace(" ", "-")
        .replace("_", "-")
        .lowercase()
}

// Validation extensions
fun String.isValidEmail(): Boolean {
    val emailPattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )
    return emailPattern.matcher(this).matches()
}

fun String.isValidPhoneNumber(): Boolean {
    val phonePattern = Pattern.compile("^[+]?[0-9]{10,13}\$")
    return phonePattern.matcher(this.replace("\\s".toRegex(), "")).matches()
}

fun String.isValidUrl(): Boolean {
    val urlPattern = Pattern.compile(
        "^(https?|ftp)://[^\\s/\$.?#].[^\\s]*\$",
        Pattern.CASE_INSENSITIVE
    )
    return urlPattern.matcher(this).matches()
}

fun String.containsDigit(): Boolean = any { it.isDigit() }

fun String.containsLetter(): Boolean = any { it.isLetter() }

fun String.containsUpperCase(): Boolean = any { it.isUpperCase() }

fun String.containsLowerCase(): Boolean = any { it.isLowerCase() }

fun String.containsSpecialChar(): Boolean = any { !it.isLetterOrDigit() }

// Search and filtering extensions
fun String.containsIgnoreCase(other: String): Boolean = 
    this.lowercase().contains(other.lowercase())

fun String.startsWithIgnoreCase(prefix: String): Boolean = 
    this.lowercase().startsWith(prefix.lowercase())

fun String.endsWithIgnoreCase(suffix: String): Boolean = 
    this.lowercase().endsWith(suffix.lowercase())

fun String.equalsIgnoreCase(other: String): Boolean = 
    this.lowercase() == other.lowercase()

fun String.fuzzyMatch(query: String, threshold: Double = 0.6): Boolean {
    if (query.isBlank()) return true
    if (this.isBlank()) return false
    
    val similarity = calculateLevenshteinSimilarity(this.lowercase(), query.lowercase())
    return similarity >= threshold
}

private fun calculateLevenshteinSimilarity(s1: String, s2: String): Double {
    val maxLength = maxOf(s1.length, s2.length)
    if (maxLength == 0) return 1.0
    
    val distance = levenshteinDistance(s1, s2)
    return 1.0 - distance.toDouble() / maxLength
}

private fun levenshteinDistance(s1: String, s2: String): Int {
    val len1 = s1.length
    val len2 = s2.length
    
    val matrix = Array(len1 + 1) { IntArray(len2 + 1) }
    
    for (i in 0..len1) matrix[i][0] = i
    for (j in 0..len2) matrix[0][j] = j
    
    for (i in 1..len1) {
        for (j in 1..len2) {
            val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
            matrix[i][j] = minOf(
                matrix[i - 1][j] + 1,
                matrix[i][j - 1] + 1,
                matrix[i - 1][j - 1] + cost
            )
        }
    }
    
    return matrix[len1][len2]
}

// Text truncation extensions
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (length <= maxLength) this
    else take(maxLength - ellipsis.length) + ellipsis
}

fun String.truncateWords(maxWords: Int, ellipsis: String = "..."): String {
    val words = split(" ")
    return if (words.size <= maxWords) this
    else words.take(maxWords).joinToString(" ") + ellipsis
}

fun String.truncateAtWord(maxLength: Int, ellipsis: String = "..."): String {
    if (length <= maxLength) return this
    
    val truncated = take(maxLength - ellipsis.length)
    val lastSpace = truncated.lastIndexOf(' ')
    
    return if (lastSpace > 0) {
        truncated.take(lastSpace) + ellipsis
    } else {
        truncated + ellipsis
    }
}

// Music-specific string processing
fun String.normalizeArtistName(): String {
    return this.trim()
        .replace(Regex("^(The|A|An)\\s+", RegexOption.IGNORE_CASE), "")
        .removeAccents()
        .trim()
}

fun String.normalizeAlbumTitle(): String {
    return this.trim()
        .replace(Regex("\\s*\\[.*?\\]\\s*"), "") // Remove [Remastered], [Deluxe], etc.
        .replace(Regex("\\s*\\(.*?\\)\\s*"), "") // Remove (2020 Remaster), etc.
        .removeAccents()
        .trim()
}

fun String.extractYear(): Int? {
    val yearPattern = Pattern.compile("\\b(19|20)\\d{2}\\b")
    val matcher = yearPattern.matcher(this)
    return if (matcher.find()) matcher.group().toIntOrNull() else null
}

fun String.extractTrackNumber(): Int? {
    val trackPattern = Pattern.compile("^(\\d+)")
    val matcher = trackPattern.matcher(this.trim())
    return if (matcher.find()) matcher.group().toIntOrNull() else null
}

// File and path extensions
fun String.getFileExtension(): String = 
    substringAfterLast('.', "")

fun String.getFileName(): String = 
    substringAfterLast('/')

fun String.getFileNameWithoutExtension(): String = 
    getFileName().substringBeforeLast('.')

fun String.getParentPath(): String = 
    substringBeforeLast('/')

// HTML and encoding extensions
fun String.stripHtml(): String = 
    replace(Regex("<[^>]*>"), "")

fun String.unescapeHtml(): String = 
    replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&nbsp;", " ")

// Number parsing extensions
fun String.toIntOrDefault(default: Int = 0): Int = toIntOrNull() ?: default

fun String.toLongOrDefault(default: Long = 0L): Long = toLongOrNull() ?: default

fun String.toFloatOrDefault(default: Float = 0f): Float = toFloatOrNull() ?: default

fun String.toDoubleOrDefault(default: Double = 0.0): Double = toDoubleOrNull() ?: default

// Collection-like operations on strings
fun String.words(): List<String> = split(Regex("\\s+")).filter { it.isNotBlank() }

fun String.lines(): List<String> = split('\n')

fun String.nonEmptyLines(): List<String> = lines().filter { it.isNotBlank() }

// String highlighting for search
fun String.highlightSubstring(
    substring: String,
    highlightStart: String = "<mark>",
    highlightEnd: String = "</mark>"
): String {
    if (substring.isBlank()) return this
    
    val pattern = Pattern.compile(Pattern.quote(substring), Pattern.CASE_INSENSITIVE)
    return pattern.matcher(this).replaceAll("$highlightStart$0$highlightEnd")
}

// Safe string operations
fun String?.orEmpty(): String = this ?: ""

fun String?.isNullOrEmpty(): Boolean = this == null || this.isEmpty()

fun String?.isNullOrBlank(): Boolean = this == null || this.isBlank()

fun String?.ifEmpty(default: String): String = if (isNullOrEmpty()) default else this!!

fun String?.ifBlank(default: String): String = if (isNullOrBlank()) default else this!!

// Pluralization helpers
fun String.pluralize(count: Int, plural: String = "${this}s"): String = 
    if (count == 1) this else plural

fun Int.pluralize(singular: String, plural: String = "${singular}s"): String = 
    if (this == 1) "$this $singular" else "$this $plural"

// Text masking
fun String.maskEmail(): String {
    val atIndex = indexOf('@')
    return if (atIndex > 2) {
        take(2) + "*".repeat(atIndex - 2) + drop(atIndex)
    } else this
}

fun String.maskPhone(): String {
    return if (length >= 4) {
        "*".repeat(length - 4) + takeLast(4)
    } else this
}

// Random string generation
fun generateRandomString(length: Int, chars: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"): String {
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}

// Text similarity
fun String.similarity(other: String): Double {
    return calculateLevenshteinSimilarity(this.lowercase(), other.lowercase())
}

// Color from string (for consistent UI colors)
fun String.toColor(): androidx.compose.ui.graphics.Color {
    val hash = hashCode()
    val red = (hash and 0xFF0000) shr 16
    val green = (hash and 0x00FF00) shr 8
    val blue = hash and 0x0000FF
    
    return androidx.compose.ui.graphics.Color(
        red = red / 255f,
        green = green / 255f,
        blue = blue / 255f,
        alpha = 1f
    )
}

// Initials extraction
fun String.getInitials(maxLength: Int = 2): String {
    return words()
        .take(maxLength)
        .map { it.first().uppercaseChar() }
        .joinToString("")
}
