package com.walnutgeek.bygrams.parser

import org.junit.Assert.*
import org.junit.Test

class IngredientParserTest {

    private fun parse(line: String) = IngredientParser.parse(line)

    // --- Grams ---

    @Test
    fun `parse grams without space`() {
        val result = parse("250g flour")
        assertTrue(result.parseable)
        assertEquals(250.0, result.amount!!, 0.001)
        assertEquals("g", result.unit)
        assertEquals("flour", result.item)
        assertNull(result.prep)
    }

    // --- Grams with parenthetical volume ---

    @Test
    fun `parse grams with parenthetical volume`() {
        val result = parse("250g (2 cups) flour")
        assertTrue(result.parseable)
        assertEquals(250.0, result.amount!!, 0.001)
        assertEquals("g", result.unit)
        assertEquals("flour", result.item)
    }

    @Test
    fun `parse grams with parenthetical tbsp`() {
        val result = parse("14g (1 tbsp) olive oil")
        assertTrue(result.parseable)
        assertEquals(14.0, result.amount!!, 0.001)
        assertEquals("g", result.unit)
        assertEquals("olive oil", result.item)
    }

    // --- Volume units ---

    @Test
    fun `parse fraction cup`() {
        val result = parse("1/2 cup milk")
        assertTrue(result.parseable)
        assertEquals(0.5, result.amount!!, 0.001)
        assertEquals("cup", result.unit)
        assertEquals("milk", result.item)
    }

    // --- Tablespoons and teaspoons ---

    @Test
    fun `parse tbsp`() {
        val result = parse("2 tbsp olive oil")
        assertTrue(result.parseable)
        assertEquals(2.0, result.amount!!, 0.001)
        assertEquals("tbsp", result.unit)
        assertEquals("olive oil", result.item)
    }

    @Test
    fun `parse tsp`() {
        val result = parse("1 tsp salt")
        assertTrue(result.parseable)
        assertEquals(1.0, result.amount!!, 0.001)
        assertEquals("tsp", result.unit)
        assertEquals("salt", result.item)
    }

    @Test
    fun `normalize Tbsp to tbsp`() {
        val result = parse("2 Tbsp sugar")
        assertTrue(result.parseable)
        assertEquals("tbsp", result.unit)
    }

    @Test
    fun `normalize tablespoon to tbsp`() {
        val result = parse("1 tablespoon butter")
        assertTrue(result.parseable)
        assertEquals("tbsp", result.unit)
        assertEquals("butter", result.item)
    }

    @Test
    fun `normalize teaspoon to tsp`() {
        val result = parse("1 teaspoon vanilla")
        assertTrue(result.parseable)
        assertEquals("tsp", result.unit)
        assertEquals("vanilla", result.item)
    }

    // --- Fractions ---

    @Test
    fun `parse one quarter fraction`() {
        val result = parse("1/4 cup sugar")
        assertTrue(result.parseable)
        assertEquals(0.25, result.amount!!, 0.001)
    }

    @Test
    fun `parse three quarters fraction`() {
        val result = parse("3/4 cup cream")
        assertTrue(result.parseable)
        assertEquals(0.75, result.amount!!, 0.001)
    }

    // --- Mixed numbers ---

    @Test
    fun `parse mixed number`() {
        val result = parse("1 1/2 cups rice")
        assertTrue(result.parseable)
        assertEquals(1.5, result.amount!!, 0.001)
        assertEquals("cups", result.unit)
        assertEquals("rice", result.item)
    }

    // --- Count-based with prep ---

    @Test
    fun `parse count based with prep`() {
        val result = parse("3 cloves garlic, crushed")
        assertTrue(result.parseable)
        assertEquals(3.0, result.amount!!, 0.001)
        assertEquals("cloves", result.unit)
        assertEquals("garlic", result.item)
        assertEquals("crushed", result.prep)
    }

    // --- Weight units ---

    @Test
    fun `parse oz`() {
        val result = parse("12 oz feta")
        assertTrue(result.parseable)
        assertEquals(12.0, result.amount!!, 0.001)
        assertEquals("oz", result.unit)
        assertEquals("feta", result.item)
    }

    @Test
    fun `parse lbs decimal`() {
        val result = parse("1.5 lbs chicken")
        assertTrue(result.parseable)
        assertEquals(1.5, result.amount!!, 0.001)
        assertEquals("lbs", result.unit)
        assertEquals("chicken", result.item)
    }

    // --- Prep after comma ---

    @Test
    fun `parse prep after comma no unit`() {
        val result = parse("2 cucumbers, peeled and grated")
        assertTrue(result.parseable)
        assertEquals(2.0, result.amount!!, 0.001)
        assertNull(result.unit)
        assertEquals("cucumbers", result.item)
        assertEquals("peeled and grated", result.prep)
    }

    // --- Unparseable ---

    @Test
    fun `unparseable salt and pepper`() {
        val result = parse("salt and pepper to taste")
        assertFalse(result.parseable)
        assertEquals("salt and pepper to taste", result.item)
    }

    @Test
    fun `unparseable list of items`() {
        val result = parse("diced celery, kale, sweet potato")
        assertFalse(result.parseable)
    }

    @Test
    fun `unparseable percentage`() {
        val result = parse("2% of salt by weight")
        assertFalse(result.parseable)
    }

    // --- Optional handling ---

    @Test
    fun `strip optional from end`() {
        val result = parse("1 tsp paprika (optional)")
        assertTrue(result.parseable)
        assertEquals(1.0, result.amount!!, 0.001)
        assertEquals("tsp", result.unit)
        assertEquals("paprika", result.item)
    }

    // --- Edge cases ---

    @Test
    fun `big can with ounce parenthetical`() {
        // "1 big can (28 ounce) crushed San Marzano tomatoes"
        // Should parse reasonably — at minimum extract amount=1 and be parseable
        val result = parse("1 big can (28 ounce) crushed San Marzano tomatoes")
        assertTrue(result.parseable)
        assertNotNull(result.amount)
    }

    @Test
    fun `compound amount with plus`() {
        // "1/2 cup + 2 tbsp of heavy whipping cream"
        // Either parse as first part (0.5 cup) or mark unparseable — both acceptable
        val result = parse("1/2 cup + 2 tbsp of heavy whipping cream")
        // Just verify it doesn't crash and returns something reasonable
        assertNotNull(result)
    }

    @Test
    fun `parse plain integer amount no unit`() {
        val result = parse("3 eggs")
        assertTrue(result.parseable)
        assertEquals(3.0, result.amount!!, 0.001)
        assertNull(result.unit)
        assertEquals("eggs", result.item)
    }

    @Test
    fun `parse with extra whitespace`() {
        val result = parse("  2   tbsp   olive oil  ")
        assertTrue(result.parseable)
        assertEquals(2.0, result.amount!!, 0.001)
        assertEquals("tbsp", result.unit)
        assertEquals("olive oil", result.item)
    }
}
