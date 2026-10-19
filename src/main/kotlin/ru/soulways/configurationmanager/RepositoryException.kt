package ru.soulways.configurationmanager

/** Reports an error while reading package repository metadata. */
class RepositoryException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
