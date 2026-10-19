package ru.soulways.configurationmanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphvizExporterTest {
    @Test
    fun `exports nodes and directed edges deterministically`() {
        val graph = DependencyGraph(
            mapOf(
                "B" to emptySet(),
                "A" to setOf("B"),
            ),
        )

        val dot = GraphvizExporter.toDot(graph)

        assertEquals(1, dot.split("\"A\" -> \"B\";").size - 1)
        assertTrue(dot.indexOf("\"A\";") < dot.indexOf("\"B\";"))
    }

    @Test
    fun `escapes Graphviz identifiers`() {
        val graph = DependencyGraph(mapOf("A\"B" to emptySet()))

        assertTrue("\"A\\\"B\";" in GraphvizExporter.toDot(graph))
    }
}
