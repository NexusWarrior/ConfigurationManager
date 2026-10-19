package ru.soulways.configurationmanager

/** Directed package dependency graph with stable insertion order. */
data class DependencyGraph(
    val adjacency: Map<String, Set<String>>,
) {
    val nodes: Set<String>
        get() = adjacency.keys + adjacency.values.flatten()

    fun dependenciesOf(packageName: String): Set<String> {
        return adjacency[packageName].orEmpty()
    }

    /** Finds packages in this graph that directly depend on the requested package. */
    fun reverseDependenciesOf(packageName: String): Set<String> {
        return adjacency.entries
            .filter { packageName in it.value }
            .mapTo(linkedSetOf(), Map.Entry<String, Set<String>>::key)
    }
}
