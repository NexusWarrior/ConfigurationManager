package ru.soulways.configurationmanager

/** Builds a transitive dependency graph using recursive depth-first search. */
class DependencyGraphBuilder(
    private val source: PackageSource,
    private val filter: String,
) {
    /** Traverses dependencies recursively and safely terminates on cycles. */
    fun build(rootPackage: String): DependencyGraph {
        val adjacency = linkedMapOf<String, Set<String>>()
        visit(rootPackage, adjacency, mutableSetOf())
        return DependencyGraph(adjacency)
    }

    private fun visit(
        packageName: String,
        adjacency: MutableMap<String, Set<String>>,
        visited: MutableSet<String>,
    ) {
        if (!visited.add(packageName)) return
        val dependencies = source.load(packageName).dependencies
            .map(PackageDependency::id)
            .filterNot(::isFiltered)
            .toCollection(linkedSetOf())
        adjacency[packageName] = dependencies
        dependencies.forEach { dependency ->
            visit(dependency, adjacency, visited)
        }
    }

    private fun isFiltered(packageName: String): Boolean {
        return filter.isNotEmpty() && packageName.contains(filter, ignoreCase = true)
    }
}
