package com.walnutgeek.bygrams.data

import com.walnutgeek.bygrams.domain.ConversionEntry
import com.walnutgeek.bygrams.domain.ConversionTable
import com.walnutgeek.bygrams.domain.DefaultConversions
import com.walnutgeek.bygrams.parser.ConversionTableParser

object ConversionRepository {

    /**
     * Builds a ConversionTable from defaults, optionally merged with repo overrides.
     * Repo entries override defaults when any alias overlaps (case-insensitive).
     */
    fun getConversionTable(recipeRepository: RecipeRepository): ConversionTable {
        val defaults = DefaultConversions.get()
        val repoContent = recipeRepository.getConversionsContent()

        if (repoContent.isNullOrBlank()) {
            return ConversionTable(defaults)
        }

        val repoEntries = try {
            ConversionTableParser.parse(repoContent)
        } catch (_: Exception) {
            return ConversionTable(defaults)
        }

        val merged = mergeEntries(defaults, repoEntries)
        return ConversionTable(merged)
    }

    private fun mergeEntries(
        defaults: List<ConversionEntry>,
        overrides: List<ConversionEntry>
    ): List<ConversionEntry> {
        // Build a set of all override alias names (lowercased)
        val overrideAliases = overrides.flatMap { entry ->
            entry.names.map { it.lowercase() }
        }.toSet()

        // Keep defaults that don't overlap with any override alias
        val kept = defaults.filter { default ->
            default.names.none { name -> name.lowercase() in overrideAliases }
        }

        return kept + overrides
    }
}
