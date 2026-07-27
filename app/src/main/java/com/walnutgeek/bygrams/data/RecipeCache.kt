package com.walnutgeek.bygrams.data

import java.io.File
import java.util.Properties

class RecipeCache(private val cacheDir: File) {

    private val recipesDir = File(cacheDir, "recipes")
    private val metadataFile = File(cacheDir, "metadata.properties")
    private val conversionsFile = File(cacheDir, "conversions.yaml")

    init {
        recipesDir.mkdirs()
    }

    fun getCachedEntries(): List<CachedEntry> {
        val metadata = loadMetadata()
        return metadata.map { (key, value) -> CachedEntry(path = key, sha = value) }
    }

    fun saveFile(path: String, sha: String, content: String) {
        val file = fileForPath(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
        val metadata = loadMetadata().toMutableMap()
        metadata[path] = sha
        saveMetadata(metadata)
    }

    fun getFile(path: String): String? {
        val file = fileForPath(path)
        return if (file.exists()) file.readText() else null
    }

    fun removeFile(path: String) {
        val file = fileForPath(path)
        file.delete()
        val metadata = loadMetadata().toMutableMap()
        metadata.remove(path)
        saveMetadata(metadata)
        // Clean up empty parent directories
        var parent = file.parentFile
        while (parent != null && parent != recipesDir && parent.isDirectory && (parent.listFiles()?.isEmpty() == true)) {
            parent.delete()
            parent = parent.parentFile
        }
    }

    fun getAllRecipeFiles(): List<CachedRecipeFile> {
        val metadata = loadMetadata()
        return metadata.keys.mapNotNull { key ->
            val file = fileForPath(key)
            if (file.exists()) {
                CachedRecipeFile(path = key, content = file.readText())
            } else null
        }
    }

    fun saveConversions(content: String) {
        cacheDir.mkdirs()
        conversionsFile.writeText(content)
    }

    fun getConversions(): String? {
        return if (conversionsFile.exists()) conversionsFile.readText() else null
    }

    private fun fileForPath(path: String): File {
        return File(recipesDir, path)
    }

    private fun loadMetadata(): Map<String, String> {
        if (!metadataFile.exists()) return emptyMap()
        val props = Properties()
        metadataFile.inputStream().use { props.load(it) }
        return props.entries.associate { (k, v) -> k.toString() to v.toString() }
    }

    private fun saveMetadata(metadata: Map<String, String>) {
        cacheDir.mkdirs()
        val props = Properties()
        metadata.forEach { (k, v) -> props.setProperty(k, v) }
        metadataFile.outputStream().use { props.store(it, null) }
    }
}
