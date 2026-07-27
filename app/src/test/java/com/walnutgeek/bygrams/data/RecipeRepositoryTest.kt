package com.walnutgeek.bygrams.data

import com.walnutgeek.bygrams.domain.ParseResult
import com.walnutgeek.bygrams.domain.RepoConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RecipeRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var cache: RecipeCache
    private lateinit var fakeApi: FakeGitHubApi
    private lateinit var repo: RecipeRepository

    private val config = RepoConfig("owner", "recipes")

    @Before
    fun setUp() {
        cache = RecipeCache(tempFolder.newFolder("cache"))
        fakeApi = FakeGitHubApi()
        repo = RecipeRepository(fakeApi, cache)
    }

    @Test
    fun `initial sync downloads all yaml files`() = runBlocking {
        fakeApi.treeEntries = listOf(
            TreeEntry("breakfast/eggs.yaml", "sha1", "blob"),
            TreeEntry("dinner/pasta.yml", "sha2", "blob")
        )
        fakeApi.fileContents = mapOf(
            "breakfast/eggs.yaml" to "name: Eggs\nactions:\n  - name: cook",
            "dinner/pasta.yml" to "name: Pasta\nactions:\n  - name: boil"
        )

        val result = repo.sync(config)

        assertEquals(2, result.added)
        assertEquals(0, result.updated)
        assertEquals(0, result.removed)
        assertEquals(0, result.failed)

        val recipes = repo.getRecipes()
        assertEquals(2, recipes.size)
    }

    @Test
    fun `incremental sync only downloads changed files`() = runBlocking {
        // Initial sync
        fakeApi.treeEntries = listOf(
            TreeEntry("a.yaml", "sha1", "blob"),
            TreeEntry("b.yaml", "sha2", "blob")
        )
        fakeApi.fileContents = mapOf(
            "a.yaml" to "name: A\nactions:\n  - name: do",
            "b.yaml" to "name: B\nactions:\n  - name: do"
        )
        repo.sync(config)
        fakeApi.fetchCount = 0

        // Second sync — only b changed
        fakeApi.treeEntries = listOf(
            TreeEntry("a.yaml", "sha1", "blob"),
            TreeEntry("b.yaml", "sha3", "blob")
        )
        fakeApi.fileContents = mapOf(
            "b.yaml" to "name: B updated\nactions:\n  - name: do"
        )
        val result = repo.sync(config)

        assertEquals(0, result.added)
        assertEquals(1, result.updated)
        assertEquals(0, result.removed)
        // Only b.yaml should have been fetched (plus the tree fetch)
        assertEquals(1, fakeApi.fetchCount)
    }

    @Test
    fun `sync removes deleted files`() = runBlocking {
        fakeApi.treeEntries = listOf(
            TreeEntry("a.yaml", "sha1", "blob"),
            TreeEntry("b.yaml", "sha2", "blob")
        )
        fakeApi.fileContents = mapOf(
            "a.yaml" to "name: A\nactions:\n  - name: do",
            "b.yaml" to "name: B\nactions:\n  - name: do"
        )
        repo.sync(config)

        // Second sync — b removed
        fakeApi.treeEntries = listOf(
            TreeEntry("a.yaml", "sha1", "blob")
        )
        val result = repo.sync(config)

        assertEquals(0, result.added)
        assertEquals(1, result.removed)
        assertEquals(1, repo.getRecipes().size)
    }

    @Test
    fun `filters out non-yaml files`() = runBlocking {
        fakeApi.treeEntries = listOf(
            TreeEntry("README.md", "sha1", "blob"),
            TreeEntry("recipe.yaml", "sha2", "blob"),
            TreeEntry("image.png", "sha3", "blob")
        )
        fakeApi.fileContents = mapOf(
            "recipe.yaml" to "name: R\nactions:\n  - name: do"
        )
        val result = repo.sync(config)

        assertEquals(1, result.added)
    }

    @Test
    fun `filters out dot-directories`() = runBlocking {
        fakeApi.treeEntries = listOf(
            TreeEntry(".github/workflows/test.yaml", "sha1", "blob"),
            TreeEntry(".hidden/secret.yaml", "sha2", "blob"),
            TreeEntry("visible/recipe.yaml", "sha3", "blob")
        )
        fakeApi.fileContents = mapOf(
            "visible/recipe.yaml" to "name: R\nactions:\n  - name: do"
        )
        val result = repo.sync(config)

        assertEquals(1, result.added)
    }

    @Test
    fun `conversions yaml stored separately`() = runBlocking {
        fakeApi.treeEntries = listOf(
            TreeEntry("conversions.yaml", "sha-conv", "blob"),
            TreeEntry("recipe.yaml", "sha1", "blob")
        )
        fakeApi.fileContents = mapOf(
            "conversions.yaml" to "units:\n  cup: 240",
            "recipe.yaml" to "name: R\nactions:\n  - name: do"
        )
        val result = repo.sync(config)

        // conversions.yaml should not count as a recipe
        assertEquals(1, result.added)
        assertEquals("units:\n  cup: 240", repo.getConversionsContent())
        // recipes list should not include conversions
        assertEquals(1, repo.getRecipes().size)
    }

    @Test
    fun `filters out tree type entries`() = runBlocking {
        fakeApi.treeEntries = listOf(
            TreeEntry("breakfast", "sha-dir", "tree"),
            TreeEntry("breakfast/eggs.yaml", "sha1", "blob")
        )
        fakeApi.fileContents = mapOf(
            "breakfast/eggs.yaml" to "name: Eggs\nactions:\n  - name: cook"
        )
        val result = repo.sync(config)

        assertEquals(1, result.added)
    }

    @Test
    fun `getRecipes preserves file path`() = runBlocking {
        fakeApi.treeEntries = listOf(
            TreeEntry("breakfast/eggs.yaml", "sha1", "blob")
        )
        fakeApi.fileContents = mapOf(
            "breakfast/eggs.yaml" to "name: Eggs\nactions:\n  - name: cook"
        )
        repo.sync(config)

        val recipes = repo.getRecipes()
        assertEquals(1, recipes.size)
        assertEquals("breakfast/eggs.yaml", recipes[0].path)
        assertTrue(recipes[0].result is ParseResult.ParsedRecipe)
    }

    @Test
    fun `getRecipes returns RawText for unparseable files`() = runBlocking {
        fakeApi.treeEntries = listOf(
            TreeEntry("notes.yaml", "sha1", "blob")
        )
        fakeApi.fileContents = mapOf(
            "notes.yaml" to "this is not a recipe"
        )
        repo.sync(config)

        val recipes = repo.getRecipes()
        assertEquals(1, recipes.size)
        assertTrue(recipes[0].result is ParseResult.RawText)
    }
}

class FakeGitHubApi : GitHubApi() {
    var treeEntries: List<TreeEntry> = emptyList()
    var fileContents: Map<String, String> = emptyMap()
    var fetchCount: Int = 0

    override suspend fun fetchTree(config: RepoConfig): List<TreeEntry> {
        return treeEntries
    }

    override suspend fun fetchFileContent(config: RepoConfig, path: String): String {
        fetchCount++
        return fileContents[path] ?: throw java.io.IOException("Not found: $path")
    }
}
