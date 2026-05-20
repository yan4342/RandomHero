package com.example.random.data

object VersionComparator {
    private val versionRegex = Regex("""^v?(\d+(?:\.\d+)*)(?:[-+].*)?$""", RegexOption.IGNORE_CASE)

    fun compare(remoteVersion: String, currentVersion: String): Int? {
        val remote = parseVersion(remoteVersion) ?: return null
        val current = parseVersion(currentVersion) ?: return null
        val maxSize = maxOf(remote.size, current.size)

        for (index in 0 until maxSize) {
            val remotePart = remote.getOrElse(index) { 0 }
            val currentPart = current.getOrElse(index) { 0 }
            if (remotePart != currentPart) {
                return remotePart.compareTo(currentPart)
            }
        }

        return 0
    }

    fun isRemoteNewer(remoteVersion: String, currentVersion: String): Boolean {
        return compare(remoteVersion, currentVersion)?.let { it > 0 } == true
    }

    private fun parseVersion(version: String): List<Int>? {
        val normalized = version.trim()
        if (normalized.isEmpty()) return null

        val match = versionRegex.matchEntire(normalized) ?: return null
        return match.groupValues[1]
            .split(".")
            .map { part -> part.toIntOrNull() ?: return null }
    }
}
