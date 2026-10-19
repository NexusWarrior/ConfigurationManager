package ru.soulways.configurationmanager

import java.net.URI
import java.nio.file.Path
import kotlin.system.exitProcess

/** CLI entry point for the dependency graph application. */
fun main(args: Array<String>) {
    if (args.contentEquals(arrayOf("--help"))) {
        println(usage())
        return
    }
    try {
        val config = CliParser.parse(args)
        val graph = DependencyGraphBuilder(packageSource(config), config.filter)
            .build(config.packageName)
        printGraph(graph)
    } catch (exception: ConfigException) {
        fail(exception.message.orEmpty(), includeUsage = true)
    } catch (exception: RepositoryException) {
        fail(exception.message.orEmpty(), includeUsage = false)
    }
}

private fun packageSource(config: AppConfig): PackageSource {
    return if (config.testMode) {
        GraphFilePackageSource(Path.of(config.repository))
    } else {
        NuGetPackageSource(URI(config.repository))
    }
}

private fun printGraph(graph: DependencyGraph) {
    graph.adjacency.forEach { (packageName, dependencies) ->
        val values = dependencies.takeIf(Set<String>::isNotEmpty)
            ?.joinToString().orEmpty().ifBlank { "(none)" }
        println("$packageName -> $values")
    }
}

private fun fail(message: String, includeUsage: Boolean): Nothing {
    System.err.println("Error: $message")
    if (includeUsage) {
        System.err.println(usage())
    }
    exitProcess(2)
}

private fun usage(): String = """
    Usage:
      ./run.sh --package <name> --repository <url-or-file> \
        --test-mode <true|false> --output <file.png> --filter <substring>
""".trimIndent()
