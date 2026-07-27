package com.walnutgeek.bygrams.parser

import com.walnutgeek.bygrams.domain.Action
import com.walnutgeek.bygrams.domain.ParseResult
import com.walnutgeek.bygrams.domain.Recipe
import com.walnutgeek.bygrams.domain.Source
import org.yaml.snakeyaml.Yaml

class RecipeParser {

    private val yaml = Yaml()

    fun parse(content: String, filename: String = ""): ParseResult {
        return try {
            val data = yaml.load<Any>(content) ?: return rawText(content, filename)
            if (data !is Map<*, *>) return rawText(content, filename)

            @Suppress("UNCHECKED_CAST")
            val map = data as Map<String, Any?>

            val name = map["name"] as? String ?: return rawText(content, filename)

            val rawActions = map["actions"] as? List<*> ?: return rawText(content, filename)
            if (rawActions.isEmpty()) return rawText(content, filename)

            val actions = rawActions.map { entry ->
                if (entry !is Map<*, *>) return rawText(content, filename)
                @Suppress("UNCHECKED_CAST")
                val actionMap = entry as Map<String, Any?>
                val actionName = actionMap["name"] as? String ?: return rawText(content, filename)
                val ingredients = (actionMap["ingredients"] as? List<*>)
                    ?.mapNotNull { it?.toString() } ?: emptyList()
                Action(
                    name = actionName,
                    ingredients = ingredients,
                    time = actionMap["time"]?.toString(),
                    note = actionMap["note"]?.toString()
                )
            }

            val description = map["description"] as? String

            val src = (map["src"] as? List<*>)?.map { entry ->
                if (entry is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val srcMap = entry as Map<String, Any?>
                    Source(
                        name = srcMap["name"] as? String,
                        url = srcMap["url"] as? String
                    )
                } else {
                    Source()
                }
            } ?: emptyList()

            val tags = (map["tags"] as? List<*>)
                ?.mapNotNull { it?.toString() } ?: emptyList()

            ParseResult.ParsedRecipe(
                Recipe(
                    name = name,
                    description = description,
                    src = src,
                    tags = tags,
                    actions = actions
                )
            )
        } catch (_: Exception) {
            rawText(content, filename)
        }
    }

    private fun rawText(content: String, filename: String): ParseResult.RawText {
        return ParseResult.RawText(content = content, filename = filename)
    }
}
