package ru.soulways.configurationmanager

/** Extracts the small JSON fragments required by the NuGet V3 protocol. */
object NuGetJsonParser {
    private val propertyPattern = Regex("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"")
    private val versionsPattern = Regex(
        "\"versions\"\\s*:\\s*\\[([^]]*)]",
        RegexOption.DOT_MATCHES_ALL,
    )
    private val stringPattern = Regex("\"([^\"]+)\"")

    /** Finds the package content endpoint advertised by a service index. */
    fun packageBaseAddress(json: String): String {
        val resource = jsonObjects(json)
            .map(::properties)
            .firstOrNull { it["@type"]?.startsWith("PackageBaseAddress/") == true }
        return resource?.get("@id")
            ?: throw RepositoryException("NuGet service index has no PackageBaseAddress resource")
    }

    /** Reads normalized package versions from a flat-container response. */
    fun versions(json: String): List<String> {
        val array = versionsPattern.find(json)?.groupValues?.get(1)
            ?: throw RepositoryException("NuGet version response has no versions array")
        val versions = stringPattern.findAll(array).map { it.groupValues[1] }.toList()
        if (versions.isEmpty()) {
            throw RepositoryException("NuGet package has no available versions")
        }
        return versions
    }

    private fun properties(jsonObject: String): Map<String, String> {
        return propertyPattern.findAll(jsonObject).associate {
            it.groupValues[1] to it.groupValues[2]
        }
    }

    private fun jsonObjects(json: String): List<String> {
        val starts = ArrayDeque<Int>()
        val objects = mutableListOf<String>()
        var inString = false
        var escaped = false
        json.forEachIndexed { index, character ->
            if (inString) {
                if (character == '\\' && !escaped) {
                    escaped = true
                } else {
                    if (character == '"' && !escaped) inString = false
                    escaped = false
                }
            } else if (character == '"') {
                inString = true
            } else if (character == '{') {
                starts.addLast(index)
            } else if (character == '}' && starts.isNotEmpty()) {
                objects += json.substring(starts.removeLast(), index + 1)
            }
        }
        return objects
    }
}
