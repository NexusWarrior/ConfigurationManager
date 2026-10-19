package ru.soulways.configurationmanager

/** Reports an invalid command-line configuration. */
class ConfigException(message: String) : IllegalArgumentException(message)
