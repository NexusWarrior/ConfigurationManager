package ru.soulways.configurationmanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NuGetProtocolParserTest {
    @Test
    fun `finds package base address`() {
        val json = """
            {"resources":[
              {"@id":"https://example/search","@type":"SearchQueryService/3.0.0"},
              {
                "@type":"PackageBaseAddress/3.0.0",
                "@id":"https://example/flat/",
                "comment":"Use {id-lower} in this URL"
              }
            ]}
        """.trimIndent()

        assertEquals("https://example/flat/", NuGetJsonParser.packageBaseAddress(json))
    }

    @Test
    fun `rejects service index without package resource`() {
        assertFailsWith<RepositoryException> {
            NuGetJsonParser.packageBaseAddress("{\"resources\":[]}")
        }
    }

    @Test
    fun `compares stable and prerelease versions`() {
        val stable = NuGetVersion.parse("2.0.0")
        val prerelease = NuGetVersion.parse("2.0.0-rc.2")
        val older = NuGetVersion.parse("1.12.9")

        kotlin.test.assertTrue(stable > prerelease)
        kotlin.test.assertTrue(prerelease > older)
    }
}
