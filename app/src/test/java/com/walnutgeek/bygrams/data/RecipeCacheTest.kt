package com.walnutgeek.bygrams.data

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RecipeCacheTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var cache: RecipeCache

    @Before
    fun setUp() {
        cache = RecipeCache(tempFolder.newFolder("cache"))
    }

    @Test
    fun `save and retrieve file content`() {
        cache.saveFile("breakfast/eggs.yaml", "sha1", "name: Eggs")
        val content = cache.getFile("breakfast/eggs.yaml")
        assertEquals("name: Eggs", content)
    }

    @Test
    fun `getFile returns null for missing file`() {
        assertNull(cache.getFile("nonexistent.yaml"))
    }

    @Test
    fun `getCachedEntries returns saved SHAs`() {
        cache.saveFile("a.yaml", "sha-a", "content a")
        cache.saveFile("b.yaml", "sha-b", "content b")

        val entries = cache.getCachedEntries()
        assertEquals(2, entries.size)
        val map = entries.associateBy { it.path }
        assertEquals("sha-a", map["a.yaml"]?.sha)
        assertEquals("sha-b", map["b.yaml"]?.sha)
    }

    @Test
    fun `saving file with same path updates SHA`() {
        cache.saveFile("a.yaml", "sha-1", "v1")
        cache.saveFile("a.yaml", "sha-2", "v2")

        val entries = cache.getCachedEntries()
        assertEquals(1, entries.size)
        assertEquals("sha-2", entries[0].sha)
        assertEquals("v2", cache.getFile("a.yaml"))
    }

    @Test
    fun `remove file deletes content and metadata`() {
        cache.saveFile("a.yaml", "sha-a", "content")
        cache.removeFile("a.yaml")

        assertNull(cache.getFile("a.yaml"))
        assertTrue(cache.getCachedEntries().isEmpty())
    }

    @Test
    fun `getAllRecipeFiles returns all saved files`() {
        cache.saveFile("a.yaml", "sha-a", "content a")
        cache.saveFile("dir/b.yaml", "sha-b", "content b")

        val files = cache.getAllRecipeFiles()
        assertEquals(2, files.size)
        val map = files.associateBy { it.path }
        assertEquals("content a", map["a.yaml"]?.content)
        assertEquals("content b", map["dir/b.yaml"]?.content)
    }

    @Test
    fun `saveConversions and getConversions roundtrip`() {
        assertNull(cache.getConversions())
        cache.saveConversions("units:\n  cup: 240")
        assertEquals("units:\n  cup: 240", cache.getConversions())
    }

    @Test
    fun `conversions are separate from recipe files`() {
        cache.saveConversions("units: stuff")
        cache.saveFile("a.yaml", "sha", "recipe")

        val files = cache.getAllRecipeFiles()
        assertEquals(1, files.size)
        assertEquals("a.yaml", files[0].path)
    }
}
