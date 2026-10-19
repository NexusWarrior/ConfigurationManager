package ru.soulways.configurationmanager

/** User-controlled settings of the dependency graph application. */
data class AppConfig(
    val packageName: String,
    val repository: String,
    val testMode: Boolean,
    val outputFile: String,
    val filter: String,
)
