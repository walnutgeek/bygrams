package com.walnutgeek.bygrams.parser

object IngredientParser {

    // Units that, when following a number, indicate a measured ingredient
    private val KNOWN_UNITS = setOf(
        "g", "kg", "mg",
        "cup", "cups",
        "tbsp", "tsp",
        "tablespoon", "tablespoons", "teaspoon", "teaspoons",
        "oz", "ounce", "ounces",
        "lb", "lbs", "pound", "pounds",
        "ml", "l", "liter", "liters",
        "clove", "cloves",
        "can", "cans",
        "pinch", "dash",
        "slice", "slices",
        "piece", "pieces",
        "bunch", "bunches",
        "head", "heads",
        "stalk", "stalks",
        "sprig", "sprigs"
    )

    private val UNIT_NORMALIZATION = mapOf(
        "tablespoon" to "tbsp",
        "tablespoons" to "tbsp",
        "Tbsp" to "tbsp",
        "teaspoon" to "tsp",
        "teaspoons" to "tsp",
        "ounce" to "oz",
        "ounces" to "oz",
        "pound" to "lbs",
        "pounds" to "lbs",
        "lb" to "lbs"
    )

    // Regex for fractions like 1/2, 3/4
    private val FRACTION_RE = Regex("""(\d+)/(\d+)""")

    fun parse(line: String): ParsedIngredient {
        // Clean up the line
        var cleaned = line.trim()

        // Strip "(optional)" from end
        cleaned = cleaned.replace(Regex("""\s*\(optional\)\s*$""", RegexOption.IGNORE_CASE), "").trim()

        // Quick check: if line doesn't start with a digit or fraction, it's unparseable
        if (!cleaned.first().isDigit()) {
            return unparseable(cleaned)
        }

        // Check for percentage pattern like "2% of salt" — unparseable
        if (cleaned.matches(Regex("""^\d+%.*"""))) {
            return unparseable(cleaned)
        }

        // Normalize whitespace
        cleaned = cleaned.replace(Regex("""\s+"""), " ")

        // Try to parse the amount from the beginning
        var remaining = cleaned
        var amount: Double

        // Try: number directly followed by unit (no space), e.g. "250g"
        val attachedUnitMatch = Regex("""^(\d+(?:\.\d+)?)([a-zA-Z]+)\s*(.*)$""").matchEntire(remaining)
        if (attachedUnitMatch != null) {
            val numStr = attachedUnitMatch.groupValues[1]
            val unitRaw = attachedUnitMatch.groupValues[2]
            val rest = attachedUnitMatch.groupValues[3]

            if (unitRaw.lowercase() in KNOWN_UNITS || unitRaw in UNIT_NORMALIZATION) {
                amount = numStr.toDouble()
                val unit = normalizeUnit(unitRaw)
                return parseRemainder(amount, unit, rest)
            }
        }

        // Parse leading number (integer, decimal, fraction, or mixed number)
        val parseResult = parseAmount(remaining) ?: return unparseable(cleaned)
        amount = parseResult.first
        remaining = parseResult.second.trim()

        // If remaining is empty, unparseable
        if (remaining.isEmpty()) {
            return unparseable(cleaned)
        }

        // Check if next token is a unit
        val tokens = remaining.split(" ", limit = 2)
        val possibleUnit = tokens[0]

        // Handle adjective+unit like "big can" — check if first two tokens form a known pattern
        if (tokens.size >= 1 && possibleUnit.lowercase() in KNOWN_UNITS || possibleUnit in UNIT_NORMALIZATION) {
            val unit = normalizeUnit(possibleUnit)
            val rest = if (tokens.size > 1) tokens[1] else ""
            return parseRemainder(amount, unit, rest)
        }

        // Check for adjective + unit pattern like "big can"
        if (tokens.size >= 2) {
            val secondTokens = tokens[1].split(" ", limit = 2)
            val possibleUnit2 = secondTokens[0]
            if (possibleUnit2.lowercase() in KNOWN_UNITS || possibleUnit2 in UNIT_NORMALIZATION) {
                // Treat "big can" — skip the adjective, use the unit
                val unit = normalizeUnit(possibleUnit2)
                val rest = if (secondTokens.size > 1) secondTokens[1] else ""
                return parseRemainder(amount, unit, rest)
            }
        }

        // No unit found — treat as count-based (e.g., "3 eggs" or "2 cucumbers, peeled")
        // Check for compound amounts with "+" sign — if there's a plus, mark unparseable
        if (remaining.contains("+")) {
            return unparseable(cleaned)
        }

        // The next token(s) are the item
        return parseRemainder(amount, null, remaining)
    }

    private fun parseRemainder(amount: Double, unit: String?, remainder: String): ParsedIngredient {
        var rest = remainder.trim()

        // Strip parenthetical content like "(2 cups)" or "(28 ounce)"
        rest = rest.replace(Regex("""\([^)]*\)\s*"""), "").trim()

        // Strip leading "of " (e.g., "of heavy whipping cream")
        rest = rest.replace(Regex("""^of\s+""", RegexOption.IGNORE_CASE), "").trim()

        // Strip "(optional)" that might remain
        rest = rest.replace(Regex("""\s*\(optional\)\s*$""", RegexOption.IGNORE_CASE), "").trim()

        // Split on comma for prep notes
        val commaIdx = rest.indexOf(',')
        val item: String
        val prep: String?

        if (commaIdx >= 0) {
            val beforeComma = rest.substring(0, commaIdx).trim()
            val afterComma = rest.substring(commaIdx + 1).trim()

            // If there are multiple commas, this is likely a list of items — unparseable
            val afterCommaCommaCount = afterComma.count { it == ',' }
            if (afterCommaCommaCount > 0) {
                return ParsedIngredient(
                    amount = null,
                    unit = null,
                    item = "$rest",
                    prep = null,
                    parseable = false
                )
            }

            item = beforeComma
            prep = if (afterComma.isNotEmpty()) afterComma else null
        } else {
            item = rest
            prep = null
        }

        if (item.isEmpty()) {
            return ParsedIngredient(
                amount = amount,
                unit = unit,
                item = "",
                prep = prep,
                parseable = true
            )
        }

        return ParsedIngredient(
            amount = amount,
            unit = unit,
            item = item,
            prep = prep,
            parseable = true
        )
    }

    /**
     * Parse a numeric amount from the beginning of a string.
     * Returns (amount, remaining_string) or null if no amount found.
     * Handles: integers, decimals, fractions, mixed numbers.
     */
    private fun parseAmount(input: String): Pair<Double, String>? {
        var s = input

        // Try mixed number: "1 1/2" (whole + space + fraction, followed by non-digit)
        val mixedMatch = Regex("""^(\d+)\s+(\d+)/(\d+)\s+(.*)$""").matchEntire(s)
        if (mixedMatch != null) {
            val whole = mixedMatch.groupValues[1].toDouble()
            val num = mixedMatch.groupValues[2].toDouble()
            val den = mixedMatch.groupValues[3].toDouble()
            if (den != 0.0) {
                return Pair(whole + num / den, mixedMatch.groupValues[4])
            }
        }

        // Try fraction: "1/2"
        val fracMatch = Regex("""^(\d+)/(\d+)\s*(.*)$""").matchEntire(s)
        if (fracMatch != null) {
            val num = fracMatch.groupValues[1].toDouble()
            val den = fracMatch.groupValues[2].toDouble()
            if (den != 0.0) {
                return Pair(num / den, fracMatch.groupValues[3])
            }
        }

        // Try decimal or integer: "1.5" or "250"
        val numMatch = Regex("""^(\d+(?:\.\d+)?)\s*(.*)$""").matchEntire(s)
        if (numMatch != null) {
            return Pair(numMatch.groupValues[1].toDouble(), numMatch.groupValues[2])
        }

        return null
    }

    private fun normalizeUnit(unit: String): String {
        // Check exact match first (for case-sensitive like "Tbsp")
        UNIT_NORMALIZATION[unit]?.let { return it }
        // Check lowercase
        UNIT_NORMALIZATION[unit.lowercase()]?.let { return it }
        return unit.lowercase()
    }

    private fun unparseable(line: String): ParsedIngredient {
        return ParsedIngredient(
            amount = null,
            unit = null,
            item = line,
            prep = null,
            parseable = false
        )
    }
}
