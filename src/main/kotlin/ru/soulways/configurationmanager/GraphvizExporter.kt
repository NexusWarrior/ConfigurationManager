package ru.soulways.configurationmanager

/** Converts a dependency graph into Graphviz DOT notation. */
object GraphvizExporter {
    /** Produces a deterministic directed graph, including isolated nodes. */
    fun toDot(graph: DependencyGraph): String = buildString {
        appendLine("digraph dependencies {")
        appendLine("  rankdir=LR;")
        appendLine("  node [shape=box];")
        graph.nodes.sorted().forEach { packageName ->
            appendLine("  ${quoted(packageName)};")
        }
        graph.adjacency.toSortedMap().forEach { (packageName, dependencies) ->
            dependencies.sorted().forEach { dependency ->
                appendLine("  ${quoted(packageName)} -> ${quoted(dependency)};")
            }
        }
        appendLine("}")
    }

    private fun quoted(value: String): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
        return "\"$escaped\""
    }
}
