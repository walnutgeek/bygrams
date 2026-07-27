package com.walnutgeek.bygrams.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MergeIngredientsTest {

    @Test
    fun `same ingredient across actions is summed`() {
        val actions = listOf(
            Action(name = "Step 1", ingredients = listOf("3 cloves garlic")),
            Action(name = "Step 2", ingredients = listOf("5 cloves garlic"))
        )
        val result = mergeIngredients(actions)
        assertEquals(listOf("8 cloves garlic"), result)
    }

    @Test
    fun `ingredients with different units stay separate`() {
        val actions = listOf(
            Action(name = "Step 1", ingredients = listOf("1 cup flour")),
            Action(name = "Step 2", ingredients = listOf("2 tbsp flour"))
        )
        val result = mergeIngredients(actions)
        assertEquals(2, result.size)
        assertEquals("1 cup flour", result[0])
        assertEquals("2 tbsp flour", result[1])
    }

    @Test
    fun `unparseable ingredients pass through`() {
        val actions = listOf(
            Action(name = "Step 1", ingredients = listOf("salt to taste"))
        )
        val result = mergeIngredients(actions)
        assertEquals(listOf("salt to taste"), result)
    }

    @Test
    fun `same ingredient in same action appears once`() {
        val actions = listOf(
            Action(name = "Step 1", ingredients = listOf("2 cups water", "2 cups water"))
        )
        val result = mergeIngredients(actions)
        // They get summed to 4 cups water since they are parseable with same key
        assertEquals(listOf("4 cups water"), result)
    }

    @Test
    fun `empty actions produce empty result`() {
        val result = mergeIngredients(emptyList())
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `unparseable lines are deduplicated`() {
        val actions = listOf(
            Action(name = "Step 1", ingredients = listOf("salt to taste")),
            Action(name = "Step 2", ingredients = listOf("salt to taste"))
        )
        val result = mergeIngredients(actions)
        assertEquals(listOf("salt to taste"), result)
    }
}
