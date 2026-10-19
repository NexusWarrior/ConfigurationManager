package ru.soulways.configurationmanager

import java.nio.file.Files
import java.nio.file.Path

/** Loads package dependencies from a local uppercase-letter test graph. */
class GraphFilePackageSource(path: Path) : PackageSource {
    private val adjacency = try {
        TestRepositoryParser.parse(Files.readString(path))
    } catch (exception: RepositoryException) {
        throw exception
    } catch (exception: Exception) {
        throw RepositoryException("Unable to read test repository: $path", exception)
    }

    override fun load(packageName: String): PackageMetadata {
        val dependencies = adjacency[packageName]
            ?: throw RepositoryException("Package is absent from test repository: $packageName")
        return PackageMetadata(
            id = packageName,
            version = "test",
            dependencies = dependencies.map { PackageDependency(it, "*", null) },
        )
    }
}
