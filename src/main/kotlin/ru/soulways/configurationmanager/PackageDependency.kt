package ru.soulways.configurationmanager

/** A direct NuGet dependency declared for an optional target framework. */
data class PackageDependency(
    val id: String,
    val versionRange: String,
    val targetFramework: String?,
)
