package ru.soulways.configurationmanager

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CliParserTest {
    @Test
    fun `parses production configuration`() {
        val config = CliParser.parse(validArguments())

        assertEquals("Newtonsoft.Json", config.packageName)
        assertEquals("https://api.nuget.org/v3/index.json", config.repository)
        assertEquals(false, config.testMode)
        assertEquals("graph.png", config.outputFile)
        assertEquals("System", config.filter)
    }

    @Test
    fun `parses test repository configuration`() {
        val repository = Files.createTempFile("repository", ".txt")
        val arguments = validArguments().also {
            it[it.indexOf("false")] = "true"
            it[it.indexOf("https://api.nuget.org/v3/index.json")] = repository.toString()
        }

        assertTrue(CliParser.parse(arguments).testMode)
    }

    @Test
    fun `rejects missing options`() {
        val exception = assertFailsWith<ConfigException> {
            CliParser.parse(validArguments().dropLast(2).toTypedArray())
        }

        assertTrue(exception.message.orEmpty().contains("--filter"))
    }

    @Test
    fun `rejects malformed package name`() {
        assertInvalid("--package", "bad package", "Package name")
    }

    @Test
    fun `rejects malformed repository URL`() {
        assertInvalid("--repository", "ftp://example.com", "HTTP or HTTPS")
    }

    @Test
    fun `rejects missing test repository file`() {
        val arguments = replace(validArguments(), "--test-mode", "true")
        val testArguments = replace(arguments, "--repository", "missing-repository.txt")

        val exception = assertFailsWith<ConfigException> {
            CliParser.parse(testArguments)
        }
        assertTrue(exception.message.orEmpty().contains("readable file"))
    }

    @Test
    fun `rejects malformed test mode`() {
        assertInvalid("--test-mode", "yes", "true or false")
    }

    @Test
    fun `rejects non PNG output file`() {
        assertInvalid("--output", "graph.svg", ".png")
    }

    @Test
    fun `rejects control characters in filter`() {
        assertInvalid("--filter", "bad\nfilter", "control characters")
    }

    @Test
    fun `rejects unknown and duplicate options`() {
        assertFailsWith<ConfigException> {
            CliParser.parse(validArguments() + arrayOf("--unknown", "value"))
        }
        assertFailsWith<ConfigException> {
            CliParser.parse(validArguments() + arrayOf("--filter", "Other"))
        }
    }

    @Test
    fun `rejects option without value`() {
        assertFailsWith<ConfigException> {
            CliParser.parse(arrayOf("--package"))
        }
    }

    private fun assertInvalid(option: String, value: String, messagePart: String) {
        val exception = assertFailsWith<ConfigException> {
            CliParser.parse(replace(validArguments(), option, value))
        }
        assertTrue(exception.message.orEmpty().contains(messagePart))
    }

    private fun replace(arguments: Array<String>, option: String, value: String): Array<String> {
        return arguments.copyOf().also { it[it.indexOf(option) + 1] = value }
    }

    private fun validArguments(): Array<String> = arrayOf(
        "--package",
        "Newtonsoft.Json",
        "--repository",
        "https://api.nuget.org/v3/index.json",
        "--test-mode",
        "false",
        "--output",
        "graph.png",
        "--filter",
        "System",
    )
}
