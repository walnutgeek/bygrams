package com.walnutgeek.bygrams.parser

import com.walnutgeek.bygrams.domain.ConversionEntry
import org.yaml.snakeyaml.Yaml

object ConversionTableParser {

    /**
     * Parses YAML content in conversions.yaml format into a list of ConversionEntry.
     *
     * Expected format:
     * ```yaml
     * - names:
     *     - flour
     *     - all-purpose flour
     *   conversions:
     *     cup: 125.0
     *     tbsp: 8.0
     * ```
     */
    @Suppress("UNCHECKED_CAST")
    fun parse(yamlContent: String): List<ConversionEntry> {
        if (yamlContent.isBlank()) return emptyList()

        val yaml = Yaml()
        val parsed = yaml.load<Any>(yamlContent) ?: return emptyList()

        if (parsed !is List<*>) return emptyList()

        return parsed.mapNotNull { item ->
            if (item !is Map<*, *>) return@mapNotNull null

            val names = (item["names"] as? List<*>)?.mapNotNull { it?.toString() }
                ?: return@mapNotNull null

            val conversionsRaw = (item["conversions"] as? Map<*, *>)
                ?: return@mapNotNull null

            val conversions = conversionsRaw.mapNotNull { (key, value) ->
                val k = key?.toString() ?: return@mapNotNull null
                val v = (value as? Number)?.toDouble() ?: return@mapNotNull null
                k to v
            }.toMap()

            ConversionEntry(names = names, conversions = conversions)
        }
    }
}
