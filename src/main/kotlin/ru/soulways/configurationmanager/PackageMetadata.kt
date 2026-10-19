package ru.soulways.configurationmanager

/** Metadata required to build a package dependency graph. */
data class PackageMetadata(
    val id: String,
    val version: String,
    val dependencies: List<PackageDependency>,
)
