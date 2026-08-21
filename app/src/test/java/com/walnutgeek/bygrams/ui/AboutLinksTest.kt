package com.walnutgeek.bygrams.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The About screen links into README.md by anchor. Nothing in the compiler or the app
 * notices when a heading is renamed — the link just quietly 404s in a shipped APK.
 *
 * These tests derive the expected anchors from the URL constants themselves and check
 * them against the headings actually present in the README, so the constants stay the
 * single source of truth. Renaming a heading without updating the constant fails here.
 */
class AboutLinksTest {

    // Gradle runs unit tests with the module directory (app/) as the working directory.
    private val readme = File("../README.md").readText()

    /** GitHub's heading-to-anchor rule: lowercase, drop punctuation, spaces to hyphens. */
    private fun slugify(heading: String): String =
        heading.lowercase()
            .filter { it.isLetterOrDigit() || it == ' ' || it == '-' }
            .trim()
            .replace(' ', '-')

    private val readmeAnchors: Set<String> =
        readme.lineSequence()
            .filter { it.startsWith("#") }
            .map { slugify(it.trimStart('#').trim()) }
            .toSet()

    private fun fragmentOf(url: String): String =
        url.substringAfter('#', missingDelimiterValue = "")

    @Test
    fun `readme is found and has headings`() {
        assertTrue(
            "No headings parsed from README.md — is the working directory wrong?",
            readmeAnchors.isNotEmpty()
        )
    }

    @Test
    fun `setup repo link points at a real README heading`() {
        val anchor = fragmentOf(SETUP_REPO_URL)
        assertTrue(
            "SETUP_REPO_URL has no #fragment: $SETUP_REPO_URL",
            anchor.isNotEmpty()
        )
        assertTrue(
            "README.md has no heading matching #$anchor. Headings present: $readmeAnchors",
            anchor in readmeAnchors
        )
    }

    @Test
    fun `convert recipe link points at a real README heading`() {
        val anchor = fragmentOf(CONVERT_RECIPE_URL)
        assertTrue(
            "CONVERT_RECIPE_URL has no #fragment: $CONVERT_RECIPE_URL",
            anchor.isNotEmpty()
        )
        assertTrue(
            "README.md has no heading matching #$anchor. Headings present: $readmeAnchors",
            anchor in readmeAnchors
        )
    }

    @Test
    fun `source link is the bare repo root`() {
        assertEquals("https://github.com/walnutgeek/bygrams", SOURCE_URL)
    }

    @Test
    fun `documentation links are distinct`() {
        assertTrue(
            "Both how-to links resolve to the same anchor; they should point at " +
                "separate README sections.",
            fragmentOf(SETUP_REPO_URL) != fragmentOf(CONVERT_RECIPE_URL)
        )
    }
}
