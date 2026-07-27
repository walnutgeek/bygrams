package com.walnutgeek.bygrams.domain

data class Recipe(
    val name: String,
    val description: String? = null,
    val src: List<Source> = emptyList(),
    val tags: List<String> = emptyList(),
    val actions: List<Action> = emptyList()
)

data class Action(
    val name: String,
    val ingredients: List<String> = emptyList(),
    val time: String? = null,
    val note: String? = null
)

data class Source(
    val name: String? = null,
    val url: String? = null
)

sealed class ParseResult {
    data class ParsedRecipe(val recipe: Recipe) : ParseResult()
    data class RawText(val content: String, val filename: String) : ParseResult()
}
