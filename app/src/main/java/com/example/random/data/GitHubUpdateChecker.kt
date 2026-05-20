package com.example.random.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

data class GitHubReleaseInfo(
    val tagName: String,
    val name: String,
    val htmlUrl: String,
    val publishedAt: String,
    val body: String
)

sealed class UpdateCheckResult {
    data class UpToDate(
        val currentVersion: String,
        val release: GitHubReleaseInfo
    ) : UpdateCheckResult()

    data class UpdateAvailable(
        val currentVersion: String,
        val release: GitHubReleaseInfo
    ) : UpdateCheckResult()

    data object NoRelease : UpdateCheckResult()

    data class NetworkError(
        val message: String
    ) : UpdateCheckResult()
}

object GitHubUpdateChecker {
    const val REPOSITORY_URL = "https://github.com/yan4342/RandomHero"
    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/yan4342/RandomHero/releases/latest"

    suspend fun checkForUpdates(currentVersion: String): UpdateCheckResult {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                }

                val code = connection.responseCode
                when {
                    code == HttpURLConnection.HTTP_NOT_FOUND -> UpdateCheckResult.NoRelease
                    code !in 200..299 -> UpdateCheckResult.NetworkError("GitHub 返回 $code")
                    else -> parseRelease(connection, currentVersion)
                }
            } catch (_: UnknownHostException) {
                UpdateCheckResult.NetworkError("网络不可用或无法连接 GitHub")
            } catch (e: IOException) {
                UpdateCheckResult.NetworkError(e.localizedMessage ?: "网络请求失败")
            } catch (_: JSONException) {
                UpdateCheckResult.NetworkError("GitHub 响应解析失败")
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun parseRelease(
        connection: HttpURLConnection,
        currentVersion: String
    ): UpdateCheckResult {
        val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val json = JSONObject(body)
        val tagName = json.optString("tag_name")

        if (tagName.isBlank()) {
            return UpdateCheckResult.NoRelease
        }

        val release = GitHubReleaseInfo(
            tagName = tagName,
            name = json.optString("name").ifBlank { tagName },
            htmlUrl = json.optString("html_url").ifBlank { "$REPOSITORY_URL/releases" },
            publishedAt = json.optString("published_at"),
            body = json.optString("body")
        )

        val comparison = VersionComparator.compare(tagName, currentVersion)
            ?: return UpdateCheckResult.NetworkError("无法识别版本号：$tagName")

        return if (comparison > 0) {
            UpdateCheckResult.UpdateAvailable(currentVersion, release)
        } else {
            UpdateCheckResult.UpToDate(currentVersion, release)
        }
    }
}
