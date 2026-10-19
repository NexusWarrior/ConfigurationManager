package ru.soulways.configurationmanager

import kotlin.system.exitProcess
import java.net.URI

/** CLI entry point for the dependency graph application. */
fun main(args: Array<String>) {
    if (args.contentEquals(arrayOf("--help"))) {
        println(usage())
        return
    }
    try {
        val config = CliParser.parse(args)
        if (config.testMode) {
            throw ConfigException("Test repository mode becomes available at stage 3")
        }
        val metadata = NuGetPackageSource(URI(config.repository)).load(config.packageName)
        printDependencies(metadata)
    } catch (exception: ConfigException) {
        fail(exception.message.orEmpty(), includeUsage = true)
    } catch (exception: RepositoryException) {
        fail(exception.message.orEmpty(), includeUsage = false)
    }
}

private fun printDependencies(metadata: PackageMetadata) {
    println("${metadata.id} ${metadata.version} direct dependencies:")
    if (metadata.dependencies.isEmpty()) {
        println("(none)")
    }
    metadata.dependencies.forEach { dependency ->
        val framework = dependency.targetFramework?.let { " [$it]" }.orEmpty()
        println("- ${dependency.id} ${dependency.versionRange}$framework")
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
