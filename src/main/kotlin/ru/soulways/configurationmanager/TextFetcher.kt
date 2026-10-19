package ru.soulways.configurationmanager

import java.net.URI

/** Retrieves a textual repository resource. */
fun interface TextFetcher {
    fun fetch(uri: URI): String
}
