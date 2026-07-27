package com.walnutgeek.bygrams.domain

import org.junit.Assert.*
import org.junit.Test

class RepoConfigTest {

    @Test
    fun `owner slash repo parses with default main branch`() {
        val config = RepoConfig.parse("owner/repo")
        assertEquals("owner", config.owner)
        assertEquals("repo", config.repo)
        assertEquals("main", config.branch)
    }

    @Test
    fun `owner slash repo colon branch parses all three fields`() {
        val config = RepoConfig.parse("owner/repo:dev")
        assertEquals("owner", config.owner)
        assertEquals("repo", config.repo)
        assertEquals("dev", config.branch)
    }

    @Test
    fun `walnutgeek recipes parses correctly`() {
        val config = RepoConfig.parse("walnutgeek/recipes")
        assertEquals("walnutgeek", config.owner)
        assertEquals("recipes", config.repo)
        assertEquals("main", config.branch)
    }

    @Test
    fun `input with surrounding whitespace is trimmed`() {
        val config = RepoConfig.parse(" walnutgeek/recipes ")
        assertEquals("walnutgeek", config.owner)
        assertEquals("recipes", config.repo)
        assertEquals("main", config.branch)
    }

    @Test
    fun `toDisplayString omits branch when main`() {
        val config = RepoConfig("owner", "repo", "main")
        assertEquals("owner/repo", config.toDisplayString())
    }

    @Test
    fun `toDisplayString includes branch when not main`() {
        val config = RepoConfig("owner", "repo", "dev")
        assertEquals("owner/repo:dev", config.toDisplayString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `input without slash throws IllegalArgumentException`() {
        RepoConfig.parse("noslash")
    }
}
