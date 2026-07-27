package com.walnutgeek.bygrams.data

import android.util.Log
import com.walnutgeek.bygrams.domain.RepoConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "GitHubApi"

open class GitHubApi {

    open suspend fun fetchTree(config: RepoConfig): List<TreeEntry> = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/repos/${config.owner}/${config.repo}/git/trees/${config.branch}?recursive=1"
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            connectTimeout = 15_000
            readTimeout = 15_000
        }
        try {
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "fetchTree failed for ${config.toDisplayString()}: HTTP $responseCode $url\n$errorBody")
                return@withContext emptyList()
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val treeArray = json.getJSONArray("tree")
            val entries = mutableListOf<TreeEntry>()
            for (i in 0 until treeArray.length()) {
                val obj = treeArray.getJSONObject(i)
                entries.add(
                    TreeEntry(
                        path = obj.getString("path"),
                        sha = obj.getString("sha"),
                        type = obj.getString("type")
                    )
                )
            }
            Log.i(TAG, "fetchTree ok for ${config.toDisplayString()}: ${entries.size} entries")
            entries
        } catch (e: IOException) {
            Log.e(TAG, "fetchTree threw for ${config.toDisplayString()}: $url", e)
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    open suspend fun fetchFileContent(config: RepoConfig, path: String): String = withContext(Dispatchers.IO) {
        val url = "https://raw.githubusercontent.com/${config.owner}/${config.repo}/${config.branch}/$path"
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
        }
        try {
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "fetchFileContent failed for $path: HTTP $responseCode $url")
                throw IOException("Failed to fetch $path: HTTP $responseCode")
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e(TAG, "fetchFileContent threw for $path: $url", e)
            throw e
        } finally {
            connection.disconnect()
        }
    }
}
