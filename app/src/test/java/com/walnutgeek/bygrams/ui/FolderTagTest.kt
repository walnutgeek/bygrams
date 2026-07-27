package com.walnutgeek.bygrams.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FolderTagTest {

    @Test
    fun `file at root has no folder tags`() {
        assertEquals(emptyList<String>(), deriveFolderTags("hummus.yaml"))
    }

    @Test
    fun `file in single folder returns that folder`() {
        assertEquals(listOf("indian"), deriveFolderTags("indian/tikka.yaml"))
    }

    @Test
    fun `file in nested folders returns all directories`() {
        assertEquals(listOf("a", "b"), deriveFolderTags("a/b/c.yaml"))
    }
}
