package com.walnutgeek.bygrams.ui

import com.walnutgeek.bygrams.data.FakeGitHubApi
import com.walnutgeek.bygrams.data.RecipeCache
import com.walnutgeek.bygrams.data.RecipeRepository
import com.walnutgeek.bygrams.data.TreeEntry
import com.walnutgeek.bygrams.domain.RepoConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeListViewModelSyncTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val config = RepoConfig("owner", "recipes")

    private class Fixture(
        val viewModel: RecipeListViewModel,
        val api: FakeGitHubApi,
        val scope: CoroutineScope
    )

    /**
     * The ViewModel owns a `stateIn(Eagerly)` collector that never completes, so it gets a scope
     * of its own rather than runTest's — an uncancelled child of the TestScope would fail the
     * test with UncompletedCoroutinesError. Unconfined means `sync()` runs to completion
     * synchronously, so tests can assert straight after calling it.
     */
    private fun TestScope.fixture(clock: () -> Long): Fixture {
        val api = FakeGitHubApi()
        api.treeEntries = listOf(TreeEntry("a.yaml", "sha1", "blob"))
        api.fileContents = mapOf("a.yaml" to "name: A\nactions:\n  - name: do")
        val repo = RecipeRepository(api, RecipeCache(tempFolder.newFolder()))
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        return Fixture(RecipeListViewModel(repo, { config }, scope, clock), api, scope)
    }

    @Test
    fun `syncIfStale syncs when nothing has synced yet`() = runTest {
        val f = fixture { 0L }
        try {
            f.viewModel.syncIfStale()

            assertEquals(1, f.viewModel.recipes.value.size)
            assertTrue(f.api.fetchCount > 0)
        } finally {
            f.scope.cancel()
        }
    }

    @Test
    fun `syncIfStale skips a sync inside the throttle window`() = runTest {
        var nowMs = 0L
        val f = fixture { nowMs }
        try {
            f.viewModel.sync()
            f.api.fetchCount = 0

            nowMs = RecipeListViewModel.MIN_SYNC_INTERVAL_MS - 1
            f.viewModel.syncIfStale()

            assertEquals(0, f.api.fetchCount)
        } finally {
            f.scope.cancel()
        }
    }

    @Test
    fun `syncIfStale syncs once the throttle window has passed`() = runTest {
        var nowMs = 0L
        val f = fixture { nowMs }
        try {
            f.viewModel.sync()
            f.api.fetchCount = 0
            // Change the sha so a stale-check that does sync must refetch the file.
            f.api.treeEntries = listOf(TreeEntry("a.yaml", "sha2", "blob"))

            nowMs = RecipeListViewModel.MIN_SYNC_INTERVAL_MS
            f.viewModel.syncIfStale()

            assertEquals(1, f.api.fetchCount)
        } finally {
            f.scope.cancel()
        }
    }

    @Test
    fun `explicit sync ignores the throttle window`() = runTest {
        var nowMs = 0L
        val f = fixture { nowMs }
        try {
            f.viewModel.sync()
            f.api.fetchCount = 0
            f.api.treeEntries = listOf(TreeEntry("a.yaml", "sha2", "blob"))

            nowMs = 1L
            f.viewModel.sync()

            assertEquals(1, f.api.fetchCount)
        } finally {
            f.scope.cancel()
        }
    }

    @Test
    fun `unreachable repo keeps recipes and reports them as cached`() = runTest {
        var nowMs = 0L
        val f = fixture { nowMs }
        try {
            f.viewModel.sync()
            assertEquals(1, f.viewModel.recipes.value.size)
            assertNull(f.viewModel.syncError.value)

            f.api.treeEntries = null
            nowMs = RecipeListViewModel.MIN_SYNC_INTERVAL_MS
            f.viewModel.syncIfStale()

            assertEquals(1, f.viewModel.recipes.value.size)
            assertTrue(f.viewModel.syncError.value!!.contains("showing cached recipes"))
        } finally {
            f.scope.cancel()
        }
    }
}
