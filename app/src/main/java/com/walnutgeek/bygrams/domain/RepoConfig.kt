package com.walnutgeek.bygrams.domain

data class RepoConfig(
    val owner: String,
    val repo: String,
    val branch: String = "main"
) {
    fun toDisplayString(): String =
        if (branch == "main") "$owner/$repo" else "$owner/$repo:$branch"

    companion object {
        fun parse(input: String): RepoConfig {
            val trimmed = input.trim()
            require(trimmed.contains('/')) { "Input must contain a '/' separating owner and repo" }
            val slashIndex = trimmed.indexOf('/')
            val owner = trimmed.substring(0, slashIndex)
            val rest = trimmed.substring(slashIndex + 1)
            return if (rest.contains(':')) {
                val colonIndex = rest.indexOf(':')
                RepoConfig(owner, rest.substring(0, colonIndex), rest.substring(colonIndex + 1))
            } else {
                RepoConfig(owner, rest)
            }
        }
    }
}
