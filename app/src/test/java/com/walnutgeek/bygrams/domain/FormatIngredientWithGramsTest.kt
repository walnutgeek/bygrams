package com.walnutgeek.bygrams.domain

import org.junit.Assert.*
import org.junit.Test

class FormatIngredientWithGramsTest {

    private val table = ConversionTable(
        listOf(
            ConversionEntry(
                names = listOf("flour", "all-purpose flour"),
                conversions = mapOf("cup" to 125.0, "tbsp" to 8.0)
            ),
            ConversionEntry(
                names = listOf("butter"),
                conversions = mapOf("cup" to 227.0, "tbsp" to 14.0)
            )
        )
    )

    @Test
    fun `1 cup flour with table returns line with gram equivalent`() {
        val result = formatIngredientWithGrams("1 cup flour", table)
        assertEquals("1 cup flour \u2192 125g", result)
    }

    @Test
    fun `3 cloves garlic with no conversion returns unchanged`() {
        val result = formatIngredientWithGrams("3 cloves garlic", table)
        assertEquals("3 cloves garlic", result)
    }

    @Test
    fun `fractional amount formats correctly`() {
        val result = formatIngredientWithGrams("1/2 cup flour", table)
        assertEquals("1/2 cup flour \u2192 63g", result)
    }

    @Test
    fun `unparseable line returns unchanged`() {
        val result = formatIngredientWithGrams("a pinch of salt", table)
        assertEquals("a pinch of salt", result)
    }
}
