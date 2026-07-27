package com.walnutgeek.bygrams.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScalingEngineTest {

    @Test
    fun `scaleIngredientLine doubles amount`() {
        val result = ScalingEngine.scaleIngredientLine("250g flour", 2.0)
        assertEquals("500g flour", result)
    }

    @Test
    fun `scaleIngredientLine halves amount`() {
        val result = ScalingEngine.scaleIngredientLine("1 cup milk", 0.5)
        assertEquals("0.5 cup milk", result)
    }

    @Test
    fun `scaleIngredientLine returns unparseable line unchanged`() {
        val result = ScalingEngine.scaleIngredientLine("salt to taste", 2.0)
        assertEquals("salt to taste", result)
    }

    @Test
    fun `scaleIngredientLine count-based fractional adds tilde prefix`() {
        val result = ScalingEngine.scaleIngredientLine("3 eggs", 0.77)
        assertEquals("~2.3 eggs", result)
    }

    @Test
    fun `scaleIngredientLine at 1x returns line unchanged`() {
        val result = ScalingEngine.scaleIngredientLine("250g flour", 1.0)
        assertEquals("250g flour", result)
    }

    @Test
    fun `scaleIngredientLine with prep text whole number`() {
        val result = ScalingEngine.scaleIngredientLine("2 cucumbers, peeled", 2.0)
        assertEquals("4 cucumbers, peeled", result)
    }

    @Test
    fun `scaleIngredientLine with prep text fractional count`() {
        val result = ScalingEngine.scaleIngredientLine("2 cucumbers, peeled", 0.75)
        assertEquals("~1.5 cucumbers, peeled", result)
    }

    @Test
    fun `scaleIngredientLine strips parenthetical equivalents`() {
        val result = ScalingEngine.scaleIngredientLine("500g flour (2 cups)", 2.0)
        assertEquals("1000g flour", result)
    }

    @Test
    fun `scaleIngredientLine with unit separated by space`() {
        val result = ScalingEngine.scaleIngredientLine("2 tbsp olive oil", 3.0)
        assertEquals("6 tbsp olive oil", result)
    }

    @Test
    fun `scaleIngredientLine count-based whole number no tilde`() {
        val result = ScalingEngine.scaleIngredientLine("3 eggs", 2.0)
        assertEquals("6 eggs", result)
    }

    @Test
    fun `scaleIngredientLine cloves are count-like`() {
        val result = ScalingEngine.scaleIngredientLine("3 cloves garlic", 0.5)
        assertEquals("~1.5 cloves garlic", result)
    }

    @Test
    fun `calculateAnchorRatio returns correct ratio`() {
        val result = ScalingEngine.calculateAnchorRatio("250g flour", 500.0)
        assertEquals(2.0, result!!, 0.001)
    }

    @Test
    fun `calculateAnchorRatio returns null for unparseable`() {
        val result = ScalingEngine.calculateAnchorRatio("salt to taste", 500.0)
        assertNull(result)
    }

    @Test
    fun `scaleRecipe scales all actions ingredients`() {
        val recipe = Recipe(
            name = "Test",
            actions = listOf(
                Action(name = "Step 1", ingredients = listOf("250g flour", "1 cup milk")),
                Action(name = "Step 2", ingredients = listOf("3 eggs", "salt to taste"))
            )
        )
        val scaled = ScalingEngine.scaleRecipe(recipe, 2.0)
        assertEquals("500g flour", scaled.actions[0].ingredients[0])
        assertEquals("2 cup milk", scaled.actions[0].ingredients[1])
        assertEquals("6 eggs", scaled.actions[1].ingredients[0])
        assertEquals("salt to taste", scaled.actions[1].ingredients[1])
    }

    @Test
    fun `getScalableIngredients returns parseable ingredients with amounts`() {
        val recipe = Recipe(
            name = "Test",
            actions = listOf(
                Action(name = "Step 1", ingredients = listOf("250g flour", "salt to taste")),
                Action(name = "Step 2", ingredients = listOf("3 eggs"))
            )
        )
        val scalable = getScalableIngredients(recipe)
        assertEquals(2, scalable.size)
        assertEquals("250g flour", scalable[0].first)
        assertEquals("3 eggs", scalable[1].first)
    }

    @Test
    fun `getScalableIngredients deduplicates by original line`() {
        val recipe = Recipe(
            name = "Test",
            actions = listOf(
                Action(name = "Step 1", ingredients = listOf("250g flour")),
                Action(name = "Step 2", ingredients = listOf("250g flour"))
            )
        )
        val scalable = getScalableIngredients(recipe)
        assertEquals(1, scalable.size)
    }
}
