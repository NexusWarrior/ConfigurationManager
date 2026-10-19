package ru.soulways.configurationmanager

import kotlin.system.exitProcess

/** CLI entry point for the dependency graph application. */
fun main(args: Array<String>) {
    if (args.contentEquals(arrayOf("--help"))) {
        println(usage())
        return
    }
    try {
        printConfig(CliParser.parse(args))
    } catch (exception: ConfigException) {
        System.err.println("Error: ${exception.message}")
        System.err.println(usage())
        exitProcess(2)
    }
}

private fun printConfig(config: AppConfig) {
    println("package=${config.packageName}")
    println("repository=${config.repository}")
    println("testMode=${config.testMode}")
    println("output=${config.outputFile}")
    println("filter=${config.filter}")
}

private fun usage(): String = """
    Usage:
      ./run.sh --package <name> --repository <url-or-file> \
        --test-mode <true|false> --output <file.png> --filter <substring>
""".trimIndent()
