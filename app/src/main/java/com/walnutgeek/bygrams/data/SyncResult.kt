package com.walnutgeek.bygrams.data

data class SyncResult(
    val added: Int,
    val updated: Int,
    val removed: Int,
    val failed: Int,
    /** True when the repo tree could not be fetched, so the cache was left untouched. */
    val repoUnreachable: Boolean = false
)
