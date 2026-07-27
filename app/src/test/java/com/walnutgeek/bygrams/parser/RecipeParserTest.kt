package com.walnutgeek.bygrams.parser

import com.walnutgeek.bygrams.domain.Action
import com.walnutgeek.bygrams.domain.ParseResult
import com.walnutgeek.bygrams.domain.Recipe
import com.walnutgeek.bygrams.domain.Source
import org.junit.Assert.*
import org.junit.Test

class RecipeParserTest {

    private val parser = RecipeParser()

    // 1. Valid complete recipe with all fields
    @Test
    fun `valid complete recipe with all fields produces correct Recipe`() {
        val yaml = """
            name: Pancakes
            description: Fluffy breakfast pancakes
            src:
              - name: Grandma's cookbook
                url: https://example.com/pancakes
            tags:
              - breakfast
              - quick
            actions:
              - name: Mix dry ingredients
                ingredients:
                  - 2 cups flour
                  - 1 tbsp sugar
                time: 5 min
                note: Sift for best results
              - name: Cook
                ingredients:
                  - batter
                time: 3 min per side
        """.trimIndent()

        val result = parser.parse(yaml)
        assertTrue(result is ParseResult.ParsedRecipe)
        val recipe = (result as ParseResult.ParsedRecipe).recipe

        assertEquals("Pancakes", recipe.name)
        assertEquals("Fluffy breakfast pancakes", recipe.description)
        assertEquals(1, recipe.src.size)
        assertEquals("Grandma's cookbook", recipe.src[0].name)
        assertEquals("https://example.com/pancakes", recipe.src[0].url)
        assertEquals(listOf("breakfast", "quick"), recipe.tags)
        assertEquals(2, recipe.actions.size)

        val action1 = recipe.actions[0]
        assertEquals("Mix dry ingredients", action1.name)
        assertEquals(listOf("2 cups flour", "1 tbsp sugar"), action1.ingredients)
        assertEquals("5 min", action1.time)
        assertEquals("Sift for best results", action1.note)

        val action2 = recipe.actions[1]
        assertEquals("Cook", action2.name)
        assertEquals(listOf("batter"), action2.ingredients)
        assertEquals("3 min per side", action2.time)
        assertNull(action2.note)
    }

    // 2. Minimal recipe (just name + one action with name)
    @Test
    fun `minimal recipe with name and one action succeeds`() {
        val yaml = """
            name: Toast
            actions:
              - name: Toast bread
        """.trimIndent()

        val result = parser.parse(yaml)
        assertTrue(result is ParseResult.ParsedRecipe)
        val recipe = (result as ParseResult.ParsedRecipe).recipe

        assertEquals("Toast", recipe.name)
        assertNull(recipe.description)
        assertTrue(recipe.src.isEmpty())
        assertTrue(recipe.tags.isEmpty())
        assertEquals(1, recipe.actions.size)
        assertEquals("Toast bread", recipe.actions[0].name)
        assertTrue(recipe.actions[0].ingredients.isEmpty())
        assertNull(recipe.actions[0].time)
        assertNull(recipe.actions[0].note)
    }

    // 3. Missing name → falls back to RawText
    @Test
    fun `missing name falls back to RawText`() {
        val yaml = """
            description: No name here
            actions:
              - name: Do something
        """.trimIndent()

        val result = parser.parse(yaml, "recipe.yaml")
        assertTrue(result is ParseResult.RawText)
        val raw = result as ParseResult.RawText
        assertEquals(yaml, raw.content)
        assertEquals("recipe.yaml", raw.filename)
    }

    // 4. Missing actions → falls back to RawText
    @Test
    fun `missing actions falls back to RawText`() {
        val yaml = """
            name: Incomplete recipe
        """.trimIndent()

        val result = parser.parse(yaml)
        assertTrue(result is ParseResult.RawText)
    }

    // 5. Empty actions list → falls back to RawText
    @Test
    fun `empty actions list falls back to RawText`() {
        val yaml = """
            name: Empty actions
            actions: []
        """.trimIndent()

        val result = parser.parse(yaml)
        assertTrue(result is ParseResult.RawText)
    }

    // 6. Action missing name → falls back to RawText
    @Test
    fun `action missing name falls back to RawText`() {
        val yaml = """
            name: Bad action
            actions:
              - ingredients:
                  - flour
        """.trimIndent()

        val result = parser.parse(yaml)
        assertTrue(result is ParseResult.RawText)
    }

    // 7. Extra/unknown fields in YAML → tolerated
    @Test
    fun `extra unknown fields in YAML are tolerated`() {
        val yaml = """
            name: Tolerant recipe
            author: Chef Bob
            rating: 5
            actions:
              - name: Step one
                temperature: 350F
        """.trimIndent()

        val result = parser.parse(yaml)
        assertTrue(result is ParseResult.ParsedRecipe)
        val recipe = (result as ParseResult.ParsedRecipe).recipe
        assertEquals("Tolerant recipe", recipe.name)
        assertEquals(1, recipe.actions.size)
        assertEquals("Step one", recipe.actions[0].name)
    }

    // 8. Plain text (not YAML) → falls back to RawText
    @Test
    fun `plain text not YAML falls back to RawText`() {
        val text = "This is just some plain text, not YAML at all."

        val result = parser.parse(text, "notes.txt")
        assertTrue(result is ParseResult.RawText)
        val raw = result as ParseResult.RawText
        assertEquals(text, raw.content)
        assertEquals("notes.txt", raw.filename)
    }

    // 9. Empty string → falls back to RawText
    @Test
    fun `empty string falls back to RawText`() {
        val result = parser.parse("")
        assertTrue(result is ParseResult.RawText)
    }

    // 10. Recipe with src as list of objects → parsed correctly
    @Test
    fun `recipe with src list of objects parsed correctly`() {
        val yaml = """
            name: Multi-source
            src:
              - name: Book A
                url: https://a.com
              - name: Blog B
              - url: https://c.com
            actions:
              - name: Do it
        """.trimIndent()

        val result = parser.parse(yaml)
        assertTrue(result is ParseResult.ParsedRecipe)
        val recipe = (result as ParseResult.ParsedRecipe).recipe

        assertEquals(3, recipe.src.size)
        assertEquals("Book A", recipe.src[0].name)
        assertEquals("https://a.com", recipe.src[0].url)
        assertEquals("Blog B", recipe.src[1].name)
        assertNull(recipe.src[1].url)
        assertNull(recipe.src[2].name)
        assertEquals("https://c.com", recipe.src[2].url)
    }

    // 11. Recipe with tags → parsed correctly
    @Test
    fun `recipe with tags parsed correctly`() {
        val yaml = """
            name: Tagged recipe
            tags:
              - vegan
              - gluten-free
              - easy
            actions:
              - name: Prepare
        """.trimIndent()

        val result = parser.parse(yaml)
        assertTrue(result is ParseResult.ParsedRecipe)
        val recipe = (result as ParseResult.ParsedRecipe).recipe
        assertEquals(listOf("vegan", "gluten-free", "easy"), recipe.tags)
    }

    // 12. Action with time, note, no ingredients → parsed correctly
    @Test
    fun `action with time and note but no ingredients parsed correctly`() {
        val yaml = """
            name: Simple recipe
            actions:
              - name: Wait
                time: 30 min
                note: Be patient
        """.trimIndent()

        val result = parser.parse(yaml)
        assertTrue(result is ParseResult.ParsedRecipe)
        val action = (result as ParseResult.ParsedRecipe).recipe.actions[0]

        assertEquals("Wait", action.name)
        assertTrue(action.ingredients.isEmpty())
        assertEquals("30 min", action.time)
        assertEquals("Be patient", action.note)
    }
}
