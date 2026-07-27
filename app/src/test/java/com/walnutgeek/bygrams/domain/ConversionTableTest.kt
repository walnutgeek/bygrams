package com.walnutgeek.bygrams.domain

import org.junit.Assert.*
import org.junit.Test

class ConversionTableTest {

    private val entries = listOf(
        ConversionEntry(
            names = listOf("flour", "all-purpose flour", "ap flour"),
            conversions = mapOf("cup" to 125.0, "tbsp" to 8.0, "tsp" to 2.6)
        ),
        ConversionEntry(
            names = listOf("butter"),
            conversions = mapOf("cup" to 227.0, "tbsp" to 14.0, "stick" to 113.0)
        )
    )

    private val table = ConversionTable(entries)

    @Test
    fun `exact alias match finds conversion`() {
        val result = table.findConversion("flour", "cup")
        assertEquals(125.0, result!!, 0.001)
    }

    @Test
    fun `case-insensitive match works`() {
        val result = table.findConversion("Flour", "cup")
        assertEquals(125.0, result!!, 0.001)
    }

    @Test
    fun `substring alias match works`() {
        val result = table.findConversion("all-purpose flour", "tbsp")
        assertEquals(8.0, result!!, 0.001)
    }

    @Test
    fun `unknown ingredient returns null`() {
        val result = table.findConversion("paprika", "cup")
        assertNull(result)
    }

    @Test
    fun `unknown unit returns null`() {
        val result = table.findConversion("flour", "gallon")
        assertNull(result)
    }

    @Test
    fun `convertToGrams 1 cup flour returns 125`() {
        val parsed = com.walnutgeek.bygrams.parser.IngredientParser.parse("1 cup flour")
        val result = table.convertToGrams(parsed)
        assertEquals(125.0, result!!, 0.001)
    }

    @Test
    fun `convertToGrams unparseable returns null`() {
        val parsed = com.walnutgeek.bygrams.parser.ParsedIngredient(
            amount = null, unit = null, item = "some stuff", prep = null, parseable = false
        )
        val result = table.convertToGrams(parsed)
        assertNull(result)
    }

    @Test
    fun `convertToGrams with no unit returns null`() {
        val parsed = com.walnutgeek.bygrams.parser.ParsedIngredient(
            amount = 3.0, unit = null, item = "eggs", prep = null, parseable = true
        )
        val result = table.convertToGrams(parsed)
        assertNull(result)
    }

    @Test
    fun `convertToGrams multiplies amount by grams per unit`() {
        val parsed = com.walnutgeek.bygrams.parser.IngredientParser.parse("2 tbsp butter")
        val result = table.convertToGrams(parsed)
        assertEquals(28.0, result!!, 0.001)
    }
}
