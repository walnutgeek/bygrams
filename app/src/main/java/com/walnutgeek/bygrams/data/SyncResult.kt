package com.walnutgeek.bygrams.data

data class SyncResult(
    val added: Int,
    val updated: Int,
    val removed: Int,
    val failed: Int
)
