package ru.soulways.configurationmanager

import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

/** Loads package metadata directly from a NuGet V3 HTTP repository. */
class NuGetPackageSource(
    private val serviceIndex: URI,
    private val fetcher: TextFetcher = HttpTextFetcher(),
) : PackageSource {
    override fun load(packageName: String): PackageMetadata {
        val baseAddress = NuGetJsonParser.packageBaseAddress(fetcher.fetch(serviceIndex))
        val normalizedId = packageName.lowercase(Locale.ROOT)
        val encodedId = URLEncoder.encode(normalizedId, StandardCharsets.UTF_8)
        val packageBase = "${baseAddress.trimEnd('/')}/$encodedId"
        val version = latestStableVersion(fetcher.fetch(URI("$packageBase/index.json")))
        val manifestUri = URI("$packageBase/$version/$encodedId.nuspec")
        val dependencies = NuspecParser.dependencies(fetcher.fetch(manifestUri))
        return PackageMetadata(packageName, version, dependencies)
    }

    private fun latestStableVersion(json: String): String {
        val stableVersions = NuGetJsonParser.versions(json)
            .map(NuGetVersion::parse)
            .filter { '-' !in it.original }
        return stableVersions.maxOrNull()?.original
            ?: throw RepositoryException("NuGet package has no stable versions")
    }
}
