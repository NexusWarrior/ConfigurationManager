package ru.soulways.configurationmanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DependencyGraphBuilderTest {
    @Test
    fun `builds a transitive graph with recursive DFS`() {
        val source = sourceOf(
            "A" to listOf("B", "C"),
            "B" to listOf("D"),
            "C" to listOf("D"),
            "D" to emptyList(),
        )

        val graph = DependencyGraphBuilder(source, "Z").build("A")

        assertEquals(setOf("A", "B", "C", "D"), graph.nodes)
        assertEquals(setOf("D"), graph.dependenciesOf("B"))
    }

    @Test
    fun `terminates when dependencies contain a cycle`() {
        val source = sourceOf(
            "A" to listOf("B"),
            "B" to listOf("C"),
            "C" to listOf("A"),
        )

        val graph = DependencyGraphBuilder(source, "Z").build("A")

        assertEquals(setOf("A", "B", "C"), graph.nodes)
        assertEquals(setOf("A"), graph.dependenciesOf("C"))
    }

    @Test
    fun `excludes packages containing filter substring`() {
        val source = sourceOf(
            "Root" to listOf("Keep", "System.Hidden"),
            "Keep" to emptyList(),
            "System.Hidden" to emptyList(),
        )

        val graph = DependencyGraphBuilder(source, "system").build("Root")

        assertFalse("System.Hidden" in graph.nodes)
        assertEquals(setOf("Keep"), graph.dependenciesOf("Root"))
    }

    private fun sourceOf(vararg entries: Pair<String, List<String>>): PackageSource {
        val packages = entries.toMap()
        return PackageSource { name ->
            val dependencies = packages.getValue(name)
                .map { PackageDependency(it, "*", null) }
            PackageMetadata(name, "test", dependencies)
        }
    }
}
