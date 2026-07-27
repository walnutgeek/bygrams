package com.walnutgeek.bygrams.domain

import com.walnutgeek.bygrams.parser.IngredientParser
import kotlin.math.roundToInt

/**
 * Parses the ingredient line, looks up conversion, and returns
 * "{original} -> {grams}g" if conversion found, or the original line if not.
 */
fun formatIngredientWithGrams(line: String, table: ConversionTable): String {
    val parsed = IngredientParser.parse(line)
    val grams = table.convertToGrams(parsed) ?: return line
    return "$line \u2192 ${grams.roundToInt()}g"
}
