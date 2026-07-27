package com.walnutgeek.bygrams.data

import android.util.Log
import com.walnutgeek.bygrams.domain.RepoConfig
import com.walnutgeek.bygrams.parser.RecipeParser

private const val TAG = "RecipeRepository"

class RecipeRepository(
    private val api: GitHubApi,
    private val cache: RecipeCache,
    private val parser: RecipeParser = RecipeParser()
) {

    suspend fun sync(config: RepoConfig): SyncResult {
        val tree = api.fetchTree(config)
        if (tree.isEmpty()) {
            Log.w(TAG, "sync: fetchTree returned no entries for ${config.toDisplayString()} — see GitHubApi logs above for the cause")
        }

        val yamlEntries = tree.filter { entry ->
            entry.type == "blob" &&
                !entry.path.startsWith(".") &&
                !entry.path.split("/").any { it.startsWith(".") } &&
                (entry.path.endsWith(".yaml") || entry.path.endsWith(".yml"))
        }

        val conversionsEntry = yamlEntries.find { it.path == "conversions.yaml" }
        val recipeEntries = yamlEntries.filter { it.path != "conversions.yaml" }

        // Handle conversions.yaml separately
        if (conversionsEntry != null) {
            try {
                val content = api.fetchFileContent(config, conversionsEntry.path)
                cache.saveConversions(content)
            } catch (e: Exception) {
                Log.e(TAG, "sync: failed to fetch conversions.yaml", e)
            }
        }

        val cachedEntries = cache.getCachedEntries().associateBy { it.path }
        val remotePathSet = recipeEntries.map { it.path }.toSet()

        var added = 0
        var updated = 0
        var failed = 0

        for (entry in recipeEntries) {
            val cachedSha = cachedEntries[entry.path]?.sha
            if (cachedSha == entry.sha) continue

            try {
                val content = api.fetchFileContent(config, entry.path)
                cache.saveFile(entry.path, entry.sha, content)
                if (cachedSha == null) added++ else updated++
            } catch (e: Exception) {
                Log.e(TAG, "sync: failed to fetch ${entry.path}", e)
                failed++
            }
        }

        // Remove files no longer in tree
        var removed = 0
        for (cached in cachedEntries.keys) {
            if (cached !in remotePathSet) {
                cache.removeFile(cached)
                removed++
            }
        }

        val result = SyncResult(added = added, updated = updated, removed = removed, failed = failed)
        Log.i(TAG, "sync: $result for ${config.toDisplayString()}")
        return result
    }

    fun getRecipes(): List<RecipeEntry> {
        val files = cache.getAllRecipeFiles()
        return files.map { file ->
            val filename = file.path.substringAfterLast("/")
            val result = parser.parse(file.content, filename)
            RecipeEntry(path = file.path, result = result)
        }
    }

    fun getConversionsContent(): String? {
        return cache.getConversions()
    }
}
