package ru.soulways.configurationmanager

/** Comparable normalized NuGet version used to select the latest stable package. */
class NuGetVersion private constructor(
    val original: String,
    private val release: List<Int>,
    private val prerelease: List<String>?,
) : Comparable<NuGetVersion> {
    override fun compareTo(other: NuGetVersion): Int {
        compareRelease(other)?.let { return it }
        return comparePrerelease(prerelease, other.prerelease)
    }

    private fun compareRelease(other: NuGetVersion): Int? {
        repeat(RELEASE_PARTS) { index ->
            val comparison = release.getOrElse(index) { 0 }
                .compareTo(other.release.getOrElse(index) { 0 })
            if (comparison != COMPARISON_EQUAL) {
                return comparison
            }
        }
        return null
    }

    companion object {
        private const val RELEASE_PARTS = 4

        fun parse(value: String): NuGetVersion {
            val withoutMetadata = value.substringBefore('+')
            val releaseText = withoutMetadata.substringBefore('-')
            val release = releaseText.split('.').map { part ->
                part.toIntOrNull()
                    ?: throw RepositoryException("Invalid NuGet version: $value")
            }
            val prereleaseText = withoutMetadata.substringAfter('-', "")
            val prerelease = prereleaseText.takeIf(String::isNotEmpty)?.split('.')
            return NuGetVersion(value, release, prerelease)
        }

        private fun comparePrerelease(first: List<String>?, second: List<String>?): Int {
            if (first == null && second == null) return COMPARISON_EQUAL
            if (first == null) return FIRST_GREATER
            if (second == null) return FIRST_LOWER
            first.zip(second).forEach { (left, right) ->
                compareIdentifier(left, right)
                    .takeIf { it != COMPARISON_EQUAL }
                    ?.let { return it }
            }
            return first.size.compareTo(second.size)
        }

        private fun compareIdentifier(first: String, second: String): Int {
            val firstNumber = first.toIntOrNull()
            val secondNumber = second.toIntOrNull()
            if (firstNumber != null && secondNumber != null) {
                return firstNumber.compareTo(secondNumber)
            }
            if (firstNumber != null) return FIRST_LOWER
            if (secondNumber != null) return FIRST_GREATER
            return first.compareTo(second, ignoreCase = true)
        }

        private const val COMPARISON_EQUAL = 0
        private const val FIRST_GREATER = 1
        private const val FIRST_LOWER = -1
    }
}
