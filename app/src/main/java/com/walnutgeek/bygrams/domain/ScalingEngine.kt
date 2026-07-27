package com.walnutgeek.bygrams.domain

import com.walnutgeek.bygrams.parser.IngredientParser
import com.walnutgeek.bygrams.parser.ParsedIngredient
import kotlin.math.roundToInt

/**
 * Count-like units where fractional results are approximate and should
 * be prefixed with "~".
 */
private val COUNT_LIKE_UNITS: Set<String?> = setOf(
    null, "cloves", "clove", "can", "cans", "slice", "slices",
    "piece", "pieces", "bunch", "bunches", "head", "heads",
    "stalk", "stalks", "sprig", "sprigs", "eggs", "egg"
)

object ScalingEngine {

    /**
     * Parse and scale a single ingredient line by [ratio].
     * If the line is unparseable, returns it as-is.
     */
    fun scaleIngredientLine(line: String, ratio: Double): String {
        if (ratio == 1.0) return line

        val parsed = IngredientParser.parse(line)
        if (!parsed.parseable || parsed.amount == null) return line

        val newAmount = parsed.amount * ratio
        return formatScaledIngredient(newAmount, parsed)
    }

    /**
     * Returns a new [Recipe] with all ingredient lines scaled by [ratio].
     */
    fun scaleRecipe(recipe: Recipe, ratio: Double): Recipe {
        if (ratio == 1.0) return recipe
        return recipe.copy(
            actions = recipe.actions.map { action ->
                action.copy(
                    ingredients = action.ingredients.map { line ->
                        scaleIngredientLine(line, ratio)
                    }
                )
            }
        )
    }

    /**
     * Given an original ingredient line and a desired new amount,
     * calculates the scaling ratio. Returns null if the line is unparseable.
     */
    fun calculateAnchorRatio(originalLine: String, newAmount: Double): Double? {
        val parsed = IngredientParser.parse(originalLine)
        if (!parsed.parseable || parsed.amount == null) return null
        return newAmount / parsed.amount
    }

    private fun formatScaledIngredient(amount: Double, parsed: ParsedIngredient): String {
        val isCountLike = parsed.unit?.lowercase() in COUNT_LIKE_UNITS
        val isWholeNumber = amount == amount.toLong().toDouble()
        val isFractionalCount = isCountLike && !isWholeNumber

        val amountStr = formatAmount(amount)
        val prefix = if (isFractionalCount) "~" else ""

        return buildString {
            append(prefix)
            append(amountStr)
            if (parsed.unit != null) {
                // Detect if original had the unit attached (no space), e.g. "250g"
                // We check if the unit is a weight/volume short unit attached directly
                if (isAttachedUnit(parsed.unit)) {
                    append(parsed.unit)
                } else {
                    append(" ")
                    append(parsed.unit)
                }
            }
            append(" ")
            append(parsed.item)
            if (parsed.prep != null) {
                append(", ")
                append(parsed.prep)
            }
        }
    }

    /**
     * Units that are typically written attached to the number (no space),
     * e.g. "250g", "500ml", "1kg".
     */
    private fun isAttachedUnit(unit: String): Boolean {
        return unit.lowercase() in setOf("g", "kg", "mg", "ml", "l")
    }

    private fun formatAmount(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            amount.toLong().toString()
        } else {
            // Round to 1 decimal place
            val rounded = (amount * 10).roundToInt() / 10.0
            if (rounded == rounded.toLong().toDouble()) {
                rounded.toLong().toString()
            } else {
                String.format("%.1f", rounded)
            }
        }
    }
}

/**
 * Returns all scalable (parseable with non-null amount) ingredient lines
 * from the recipe, deduplicated by the original line text.
 */
fun getScalableIngredients(recipe: Recipe): List<Pair<String, ParsedIngredient>> {
    val seen = LinkedHashSet<String>()
    val result = mutableListOf<Pair<String, ParsedIngredient>>()

    for (action in recipe.actions) {
        for (line in action.ingredients) {
            if (line in seen) continue
            val parsed = IngredientParser.parse(line)
            if (parsed.parseable && parsed.amount != null) {
                seen.add(line)
                result.add(Pair(line, parsed))
            }
        }
    }

    return result
}
