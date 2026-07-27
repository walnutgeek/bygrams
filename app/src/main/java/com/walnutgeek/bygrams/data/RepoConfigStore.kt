package com.walnutgeek.bygrams.data

import android.content.Context
import com.walnutgeek.bygrams.domain.RepoConfig

class RepoConfigStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getConfig(): RepoConfig? {
        val owner = prefs.getString(KEY_OWNER, null) ?: return null
        val repo = prefs.getString(KEY_REPO, null) ?: return null
        val branch = prefs.getString(KEY_BRANCH, "main") ?: "main"
        return RepoConfig(owner, repo, branch)
    }

    fun saveConfig(config: RepoConfig) {
        prefs.edit()
            .putString(KEY_OWNER, config.owner)
            .putString(KEY_REPO, config.repo)
            .putString(KEY_BRANCH, config.branch)
            .apply()
    }

    fun isShowGramsEnabled(): Boolean {
        return prefs.getBoolean(KEY_SHOW_GRAMS, false)
    }

    fun setShowGramsEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_SHOW_GRAMS, enabled)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "bygrams_repo_config"
        private const val KEY_OWNER = "owner"
        private const val KEY_REPO = "repo"
        private const val KEY_BRANCH = "branch"
        private const val KEY_SHOW_GRAMS = "show_grams"
    }
}
