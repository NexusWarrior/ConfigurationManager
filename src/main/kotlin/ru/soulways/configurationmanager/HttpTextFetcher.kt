package ru.soulways.configurationmanager

import java.net.URI
import java.net.HttpURLConnection

/** Retrieves NuGet metadata through the JDK HTTP client. */
class HttpTextFetcher : TextFetcher {
    override fun fetch(uri: URI): String {
        val connection = uri.toURL().openConnection() as HttpURLConnection
        return try {
            configure(connection)
            validateStatus(uri, connection.responseCode)
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (exception: RepositoryException) {
            throw exception
        } catch (exception: Exception) {
            val reason = exception.message ?: exception::class.simpleName.orEmpty()
            throw RepositoryException(
                "Unable to read repository resource: $uri ($reason)",
                exception,
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun configure(connection: HttpURLConnection) {
        connection.connectTimeout = CONNECT_TIMEOUT_MILLISECONDS
        connection.readTimeout = REQUEST_TIMEOUT_MILLISECONDS
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("Accept", "application/json, application/xml, text/xml")
        connection.setRequestProperty("User-Agent", "configuration-manager/1.0")
    }

    private fun validateStatus(uri: URI, statusCode: Int) {
        if (statusCode !in SUCCESS_STATUS_RANGE) {
            throw RepositoryException("Repository returned HTTP $statusCode: $uri")
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MILLISECONDS = 30_000
        const val REQUEST_TIMEOUT_MILLISECONDS = 60_000
        val SUCCESS_STATUS_RANGE = 200..299
    }
}
