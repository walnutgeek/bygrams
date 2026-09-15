package com.walnutgeek.bygrams.ui

import com.walnutgeek.bygrams.data.RecipeEntry
import com.walnutgeek.bygrams.data.RecipeRepository
import com.walnutgeek.bygrams.domain.RepoConfig
import com.walnutgeek.bygrams.domain.ParseResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeListViewModel(
    private val repository: RecipeRepository,
    private val getConfig: () -> RepoConfig?,
    private val scope: CoroutineScope,
    private val now: () -> Long = System::currentTimeMillis
) {

    /** When the last sync was started, used to throttle the on-resume sync. */
    private var lastSyncStartedAt: Long? = null

    private val _recipes = MutableStateFlow<List<RecipeEntry>>(emptyList())
    val recipes: StateFlow<List<RecipeEntry>> = _recipes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag

    val filteredRecipes: StateFlow<List<RecipeEntry>> = combine(
        _recipes, _searchQuery, _selectedTag
    ) { recipes, query, tag ->
        var result = recipes

        if (tag != null) {
            result = result.filter { entry ->
                val allTags = getAllTags(entry)
                allTags.any { it.equals(tag, ignoreCase = true) }
            }
        }

        if (query.isNotBlank()) {
            result = result.filter { entry -> matchesSearch(entry, query) }
        }

        result
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun loadRecipes() {
        _recipes.value = repository.getRecipes()
    }

    /** Syncs now, regardless of how recently the last sync ran. For explicit user actions. */
    fun sync() {
        val config = getConfig() ?: return
        if (_isLoading.value) return
        lastSyncStartedAt = now()
        scope.launch {
            _isLoading.value = true
            _syncError.value = null
            try {
                val result = repository.sync(config)
                loadRecipes()
                if (result.repoUnreachable) {
                    _syncError.value = if (_recipes.value.isEmpty()) {
                        "Couldn't reach ${config.toDisplayString()} — check Logcat tag GitHubApi/RecipeRepository for details"
                    } else {
                        "Couldn't reach ${config.toDisplayString()} — showing cached recipes"
                    }
                } else if (result.failed > 0) {
                    _syncError.value = "${result.failed} recipe file(s) failed to sync — check Logcat tag RecipeRepository"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Syncs only if the last sync is older than [MIN_SYNC_INTERVAL_MS]. Called when the app
     * returns to the foreground, where a sync on every resume would be wasteful — GitHub's
     * tree API is edge-cached for 60s anyway, and unauthenticated calls are capped at 60/hour.
     */
    fun syncIfStale() {
        val last = lastSyncStartedAt
        if (last != null && now() - last < MIN_SYNC_INTERVAL_MS) return
        sync()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = if (tag == _selectedTag.value) null else tag
    }

    companion object {

        /** Minimum gap between automatic on-resume syncs. */
        const val MIN_SYNC_INTERVAL_MS = 60_000L

        fun getAllTags(entry: RecipeEntry): List<String> {
            val folderTags = deriveFolderTags(entry.path)
            val yamlTags = when (val r = entry.result) {
                is ParseResult.ParsedRecipe -> r.recipe.tags
                is ParseResult.RawText -> emptyList()
            }
            val seen = mutableSetOf<String>()
            val result = mutableListOf<String>()
            for (t in yamlTags + folderTags) {
                val lower = t.lowercase()
                if (seen.add(lower)) {
                    result.add(t)
                }
            }
            return result
        }

        private fun matchesSearch(entry: RecipeEntry, query: String): Boolean {
            val q = query.lowercase()
            return when (val r = entry.result) {
                is ParseResult.ParsedRecipe -> {
                    r.recipe.name.lowercase().contains(q) ||
                        r.recipe.actions.any { action ->
                            action.ingredients.any { it.lowercase().contains(q) }
                        }
                }
                is ParseResult.RawText -> {
                    r.filename.lowercase().contains(q) ||
                        r.content.lowercase().contains(q)
                }
            }
        }
    }
}
