package com.walnutgeek.bygrams.domain

object DefaultConversions {

    fun get(): List<ConversionEntry> = listOf(
        ConversionEntry(
            names = listOf("flour", "all-purpose flour", "ap flour", "bread flour", "cake flour"),
            conversions = mapOf("cup" to 125.0, "tbsp" to 8.0, "tsp" to 2.6)
        ),
        ConversionEntry(
            names = listOf("butter", "unsalted butter", "salted butter"),
            conversions = mapOf("cup" to 227.0, "tbsp" to 14.0, "tsp" to 4.7, "stick" to 113.0)
        ),
        ConversionEntry(
            names = listOf("milk", "whole milk", "skim milk"),
            conversions = mapOf("cup" to 244.0, "tbsp" to 15.0, "tsp" to 5.0)
        ),
        ConversionEntry(
            names = listOf("water"),
            conversions = mapOf("cup" to 237.0, "tbsp" to 15.0, "tsp" to 5.0)
        ),
        ConversionEntry(
            names = listOf("oil", "olive oil", "vegetable oil", "canola oil", "coconut oil"),
            conversions = mapOf("cup" to 218.0, "tbsp" to 14.0, "tsp" to 4.5)
        ),
        ConversionEntry(
            names = listOf("sugar", "granulated sugar", "white sugar"),
            conversions = mapOf("cup" to 200.0, "tbsp" to 12.5, "tsp" to 4.2)
        ),
        ConversionEntry(
            names = listOf("cream", "heavy cream", "whipping cream", "heavy whipping cream"),
            conversions = mapOf("cup" to 238.0, "tbsp" to 15.0, "tsp" to 5.0)
        ),
        ConversionEntry(
            names = listOf("yogurt", "greek yogurt", "plain yogurt"),
            conversions = mapOf("cup" to 245.0, "tbsp" to 15.3, "tsp" to 5.1)
        ),
        ConversionEntry(
            names = listOf("honey"),
            conversions = mapOf("cup" to 340.0, "tbsp" to 21.0, "tsp" to 7.0)
        ),
        ConversionEntry(
            names = listOf("tahini"),
            conversions = mapOf("cup" to 240.0, "tbsp" to 15.0, "tsp" to 5.0)
        )
    )
}
