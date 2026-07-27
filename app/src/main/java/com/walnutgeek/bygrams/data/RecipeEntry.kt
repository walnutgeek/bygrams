package com.walnutgeek.bygrams.data

import com.walnutgeek.bygrams.domain.ParseResult

data class RecipeEntry(
    val path: String,
    val result: ParseResult
)
