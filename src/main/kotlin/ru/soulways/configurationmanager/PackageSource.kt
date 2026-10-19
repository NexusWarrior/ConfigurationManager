package ru.soulways.configurationmanager

/** Loads package metadata without invoking an external package manager. */
fun interface PackageSource {
    fun load(packageName: String): PackageMetadata
}
