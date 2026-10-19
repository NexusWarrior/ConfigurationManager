package ru.soulways.configurationmanager

import java.net.URI
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

/** Parses and validates application command-line options. */
object CliParser {
    private const val VALUE_OFFSET = 1
    private const val OPTION_WIDTH = 2
    private val optionNames = setOf(
        "--package",
        "--repository",
        "--test-mode",
        "--output",
        "--filter",
    )
    private val packagePattern = Regex("[A-Za-z0-9][A-Za-z0-9._-]*")

    /** Converts command-line arguments into a validated configuration. */
    fun parse(args: Array<String>): AppConfig {
        val options = collectOptions(args)
        val packageName = requireOption(options, "--package")
        val repository = requireOption(options, "--repository")
        val testMode = parseBoolean(requireOption(options, "--test-mode"))
        val outputFile = requireOption(options, "--output")
        val filter = requireOption(options, "--filter")

        validatePackageName(packageName)
        validateRepository(repository, testMode)
        validateOutputFile(outputFile)
        validateFilter(filter)
        return AppConfig(packageName, repository, testMode, outputFile, filter)
    }

    private fun collectOptions(args: Array<String>): Map<String, String> {
        val options = mutableMapOf<String, String>()
        var index = 0
        while (index < args.size) {
            val option = args[index]
            if (option !in optionNames) {
                throw ConfigException("Unknown option: $option")
            }
            if (option in options) {
                throw ConfigException("Option is specified more than once: $option")
            }
            if (index + VALUE_OFFSET >= args.size || args[index + VALUE_OFFSET] in optionNames) {
                throw ConfigException("Option requires a value: $option")
            }
            options[option] = args[index + VALUE_OFFSET]
            index += OPTION_WIDTH
        }
        return options
    }

    private fun requireOption(options: Map<String, String>, name: String): String {
        return options[name]?.takeIf(String::isNotBlank)
            ?: throw ConfigException("Required option is missing or blank: $name")
    }

    private fun parseBoolean(value: String): Boolean = when (value.lowercase()) {
        "true" -> true
        "false" -> false
        else -> throw ConfigException("--test-mode must be true or false")
    }

    private fun validatePackageName(value: String) {
        if (!packagePattern.matches(value)) {
            throw ConfigException("Package name contains unsupported characters: $value")
        }
    }

    private fun validateRepository(value: String, testMode: Boolean) {
        if (testMode) {
            validateTestRepository(value)
        } else {
            validateRepositoryUrl(value)
        }
    }

    private fun validateTestRepository(value: String) {
        val path = try {
            Path.of(value)
        } catch (_: InvalidPathException) {
            throw ConfigException("Test repository path is invalid: $value")
        }
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw ConfigException("Test repository must be a readable file: $value")
        }
    }

    private fun validateRepositoryUrl(value: String) {
        val uri = try {
            URI(value)
        } catch (_: IllegalArgumentException) {
            throw ConfigException("Repository URL is invalid: $value")
        }
        if (uri.scheme !in setOf("http", "https") || uri.host.isNullOrBlank()) {
            throw ConfigException("Repository must be an HTTP or HTTPS URL: $value")
        }
    }

    private fun validateOutputFile(value: String) {
        val path = try {
            Path.of(value)
        } catch (_: InvalidPathException) {
            throw ConfigException("Output file path is invalid: $value")
        }
        if (!path.fileName.toString().lowercase().endsWith(".png")) {
            throw ConfigException("Output file must have the .png extension: $value")
        }
        val parent = path.parent
        if (parent != null && (!Files.isDirectory(parent) || !Files.isWritable(parent))) {
            throw ConfigException("Output directory must exist and be writable: $parent")
        }
    }

    private fun validateFilter(value: String) {
        if (value.any(Char::isISOControl)) {
            throw ConfigException("Filter must not contain control characters")
        }
    }
}
