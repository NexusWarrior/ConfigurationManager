package ru.soulways.configurationmanager

import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals

class NuGetPackageSourceTest {
    @Test
    fun `loads latest stable package dependencies`() {
        val responses = mapOf(
            SERVICE_INDEX to serviceIndexJson(),
            URI("https://packages.example/flat/sample/index.json") to versionsJson(),
            URI("https://packages.example/flat/sample/2.1.0/sample.nuspec") to nuspecXml(),
        )
        val source = NuGetPackageSource(SERVICE_INDEX) { uri ->
            responses[uri] ?: error("Unexpected request: $uri")
        }

        val metadata = source.load("Sample")

        assertEquals("2.1.0", metadata.version)
        assertEquals(2, metadata.dependencies.size)
        assertEquals("Common", metadata.dependencies[0].id)
        assertEquals("net8.0", metadata.dependencies[1].targetFramework)
    }

    private fun serviceIndexJson(): String = """
        {
          "version": "3.0.0",
          "resources": [
            {
              "@id": "https://packages.example/flat/",
              "@type": "PackageBaseAddress/3.0.0"
            }
          ]
        }
    """.trimIndent()

    private fun versionsJson(): String = """
        {"versions":["1.0.0","2.1.0-beta.1","2.0.9","2.1.0"]}
    """.trimIndent()

    private fun nuspecXml(): String = """
        <package xmlns="http://schemas.microsoft.com/packaging/2013/05/nuspec.xsd">
          <metadata>
            <dependencies>
              <dependency id="Common" version="[1.0.0, 2.0.0)" />
              <group targetFramework="net8.0">
                <dependency id="Runtime" version="8.0.0" />
              </group>
            </dependencies>
          </metadata>
        </package>
    """.trimIndent()

    private companion object {
        val SERVICE_INDEX: URI = URI("https://packages.example/v3/index.json")
    }
}
