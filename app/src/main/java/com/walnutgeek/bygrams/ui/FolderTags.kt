package com.walnutgeek.bygrams.ui

/**
 * Extracts directory components from a recipe file path as tags.
 * e.g., "indian/tikka.yaml" -> ["indian"], "a/b/c.yaml" -> ["a", "b"], "hummus.yaml" -> []
 */
fun deriveFolderTags(path: String): List<String> {
    val parts = path.split("/")
    return if (parts.size <= 1) emptyList() else parts.dropLast(1)
}
