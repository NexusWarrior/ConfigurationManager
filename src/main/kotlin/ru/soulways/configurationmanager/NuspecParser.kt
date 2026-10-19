package ru.soulways.configurationmanager

import java.io.StringReader
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource

/** Reads dependency declarations from a NuGet package manifest. */
object NuspecParser {
    /** Parses flat and target-framework-specific dependency declarations. */
    fun dependencies(xml: String): List<PackageDependency> {
        val document = try {
            newDocumentBuilderFactory().newDocumentBuilder()
                .parse(InputSource(StringReader(xml)))
        } catch (exception: Exception) {
            throw RepositoryException("Unable to parse NuGet package manifest", exception)
        }
        val nodes = document.getElementsByTagNameNS("*", "dependency")
        return (0 until nodes.length).map { index ->
            dependency(nodes.item(index) as Element)
        }.distinct().sortedWith(
            compareBy(PackageDependency::id, PackageDependency::targetFramework),
        )
    }

    private fun dependency(element: Element): PackageDependency {
        val id = element.getAttribute("id")
        if (id.isBlank()) {
            throw RepositoryException("NuGet dependency has no package id")
        }
        val version = element.getAttribute("version").ifBlank { "*" }
        val parent = element.parentNode as? Element
        val framework = parent?.takeIf { it.localName == "group" }
            ?.getAttribute("targetFramework")?.ifBlank { null }
        return PackageDependency(id, version, framework)
    }

    private fun newDocumentBuilderFactory(): DocumentBuilderFactory {
        return DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isXIncludeAware = false
            isExpandEntityReferences = false
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
        }
    }
}
