package com.walnutgeek.bygrams.domain

import com.walnutgeek.bygrams.parser.IngredientParser

/**
 * Merges and sums ingredient lines across all actions in a recipe.
 *
 * Parseable ingredients are grouped by (unit, item) case-insensitively,
 * and their amounts are summed. Prep text is taken from the first occurrence.
 * Unparseable lines pass through as-is, deduplicated.
 */
fun mergeIngredients(actions: List<Action>): List<String> {
    // Key: (unit?.lowercase(), item.lowercase()) -> (summedAmount, originalUnit, originalItem, prep)
    data class MergedEntry(
        var amount: Double,
        val unit: String?,
        val item: String,
        val prep: String?
    )

    val mergedMap = LinkedHashMap<Pair<String?, String>, MergedEntry>()
    val unparseableLines = LinkedHashSet<String>()

    for (action in actions) {
        for (line in action.ingredients) {
            val parsed = IngredientParser.parse(line)
            if (!parsed.parseable || parsed.amount == null) {
                unparseableLines.add(line.trim())
            } else {
                val key = Pair(parsed.unit?.lowercase(), parsed.item.lowercase())
                val existing = mergedMap[key]
                if (existing != null) {
                    existing.amount += parsed.amount
                } else {
                    mergedMap[key] = MergedEntry(
                        amount = parsed.amount,
                        unit = parsed.unit,
                        item = parsed.item,
                        prep = parsed.prep
                    )
                }
            }
        }
    }

    val result = mutableListOf<String>()

    for (entry in mergedMap.values) {
        val amountStr = formatAmount(entry.amount)
        val parts = buildString {
            append(amountStr)
            if (entry.unit != null) {
                append(" ")
                append(entry.unit)
            }
            append(" ")
            append(entry.item)
            if (entry.prep != null) {
                append(", ")
                append(entry.prep)
            }
        }
        result.add(parts)
    }

    for (line in unparseableLines) {
        result.add(line)
    }

    return result
}

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        amount.toString()
    }
}
