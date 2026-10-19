package ru.soulways.configurationmanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestRepositoryParserTest {
    @Test
    fun `parses uppercase package graph`() {
        val graph = TestRepositoryParser.parse("A: B C\nB: C\nC:\n")

        assertEquals(setOf("B", "C"), graph.getValue("A"))
        assertEquals(emptySet(), graph.getValue("C"))
    }

    @Test
    fun `rejects malformed and duplicate declarations`() {
        assertFailsWith<RepositoryException> {
            TestRepositoryParser.parse("A B\n")
        }
        assertFailsWith<RepositoryException> {
            TestRepositoryParser.parse("A:\nA:\n")
        }
    }

    @Test
    fun `rejects non uppercase and undeclared packages`() {
        assertFailsWith<RepositoryException> {
            TestRepositoryParser.parse("A: package\npackage:\n")
        }
        assertFailsWith<RepositoryException> {
            TestRepositoryParser.parse("A: B\n")
        }
    }
}
