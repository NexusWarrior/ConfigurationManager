package ru.soulways.configurationmanager

import kotlin.test.Test
import kotlin.test.assertEquals

class DependencyGraphTest {
    @Test
    fun `finds reverse dependencies in an acyclic graph`() {
        val graph = DependencyGraph(
            linkedMapOf(
                "A" to setOf("B", "C"),
                "B" to setOf("C"),
                "C" to emptySet(),
            ),
        )

        assertEquals(emptySet(), graph.reverseDependenciesOf("A"))
        assertEquals(setOf("A"), graph.reverseDependenciesOf("B"))
        assertEquals(setOf("A", "B"), graph.reverseDependenciesOf("C"))
    }

    @Test
    fun `finds reverse dependencies in a cycle`() {
        val graph = DependencyGraph(
            linkedMapOf(
                "A" to setOf("B"),
                "B" to setOf("C"),
                "C" to setOf("A"),
            ),
        )

        assertEquals(setOf("C"), graph.reverseDependenciesOf("A"))
        assertEquals(setOf("A"), graph.reverseDependenciesOf("B"))
        assertEquals(setOf("B"), graph.reverseDependenciesOf("C"))
    }
}
