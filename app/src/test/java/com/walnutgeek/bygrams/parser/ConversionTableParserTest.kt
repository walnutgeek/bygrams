package com.walnutgeek.bygrams.parser

import com.walnutgeek.bygrams.domain.ConversionEntry
import org.junit.Assert.*
import org.junit.Test

class ConversionTableParserTest {

    @Test
    fun `valid YAML with two entries parses correctly`() {
        val yaml = """
            - names:
                - flour
                - all-purpose flour
              conversions:
                cup: 125.0
                tbsp: 8.0
            - names:
                - butter
              conversions:
                cup: 227.0
                stick: 113.0
        """.trimIndent()

        val result = ConversionTableParser.parse(yaml)
        assertEquals(2, result.size)

        assertEquals(listOf("flour", "all-purpose flour"), result[0].names)
        assertEquals(125.0, result[0].conversions["cup"]!!, 0.001)
        assertEquals(8.0, result[0].conversions["tbsp"]!!, 0.001)

        assertEquals(listOf("butter"), result[1].names)
        assertEquals(227.0, result[1].conversions["cup"]!!, 0.001)
        assertEquals(113.0, result[1].conversions["stick"]!!, 0.001)
    }

    @Test
    fun `empty YAML returns empty list`() {
        val result = ConversionTableParser.parse("")
        assertTrue(result.isEmpty())
    }
}
