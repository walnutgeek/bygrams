package com.walnutgeek.bygrams.parser

data class ParsedIngredient(
    val amount: Double?,
    val unit: String?,
    val item: String,
    val prep: String?,
    val parseable: Boolean
)
