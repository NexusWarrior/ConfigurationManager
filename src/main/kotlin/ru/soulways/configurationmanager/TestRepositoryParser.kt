package ru.soulways.configurationmanager

/** Parses lines in the form `A: B C` into a test dependency repository. */
object TestRepositoryParser {
    private const val DECLARATION_PARTS = 2
    private val packagePattern = Regex("[A-Z]")

    /** Validates and parses all package declarations. */
    fun parse(text: String): Map<String, Set<String>> {
        val adjacency = linkedMapOf<String, Set<String>>()
        text.lineSequence().forEachIndexed { index, rawLine ->
            if (rawLine.isNotBlank()) {
                val (packageName, dependencies) = parseLine(rawLine, index + 1)
                if (adjacency.put(packageName, dependencies) != null) {
                    throw RepositoryException("Duplicate test package: $packageName")
                }
            }
        }
        validateReferences(adjacency)
        return adjacency
    }

    private fun parseLine(line: String, lineNumber: Int): Pair<String, Set<String>> {
        val parts = line.split(':', limit = DECLARATION_PARTS)
        if (parts.size != DECLARATION_PARTS) {
            throw RepositoryException("Invalid test repository line $lineNumber: $line")
        }
        val packageName = parts[0].trim()
        val dependencies = parts[1].trim()
            .takeIf(String::isNotEmpty)
            ?.split(Regex("\\s+"))
            .orEmpty()
        validatePackage(packageName, lineNumber)
        dependencies.forEach { validatePackage(it, lineNumber) }
        return packageName to dependencies.toCollection(linkedSetOf())
    }

    private fun validatePackage(packageName: String, lineNumber: Int) {
        if (!packagePattern.matches(packageName)) {
            throw RepositoryException(
                "Test package must be one uppercase Latin letter at line $lineNumber: $packageName",
            )
        }
    }

    private fun validateReferences(adjacency: Map<String, Set<String>>) {
        val missing = adjacency.values.flatten().toSet() - adjacency.keys
        if (missing.isNotEmpty()) {
            val packageNames = missing.sorted().joinToString()
            throw RepositoryException("Undeclared test packages: $packageNames")
        }
    }
}
