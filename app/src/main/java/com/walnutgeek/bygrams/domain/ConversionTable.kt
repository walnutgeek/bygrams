package com.walnutgeek.bygrams.domain

import com.walnutgeek.bygrams.parser.ParsedIngredient

class ConversionTable(val entries: List<ConversionEntry>) {

    /**
     * Looks up the item against all aliases (case-insensitive substring match),
     * then the unit in that entry's conversions map.
     * Returns grams-per-unit or null.
     */
    fun findConversion(item: String, unit: String): Double? {
        val itemLower = item.lowercase()
        for (entry in entries) {
            val matched = entry.names.any { alias ->
                val aliasLower = alias.lowercase()
                itemLower.contains(aliasLower) || aliasLower.contains(itemLower)
            }
            if (matched) {
                return entry.conversions[unit.lowercase()]
            }
        }
        return null
    }

    /**
     * If the parsed ingredient has an amount and unit, tries to find a conversion
     * and returns amount * gramsPerUnit, or null.
     */
    fun convertToGrams(parsed: ParsedIngredient): Double? {
        if (!parsed.parseable) return null
        val amount = parsed.amount ?: return null
        val unit = parsed.unit ?: return null
        val gramsPerUnit = findConversion(parsed.item, unit) ?: return null
        return amount * gramsPerUnit
    }
}
