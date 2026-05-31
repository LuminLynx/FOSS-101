package com.perpenda.data.version

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class LatestVersion(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
)

class VersionCheckRepository(
    private val manifestUrl: String = "https://perpenda.com/latest.json",
) {
    suspend fun fetchLatest(): LatestVersion? = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(manifestUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5_000
                readTimeout = 5_000
                setRequestProperty("Accept", "application/json")
            }
            try {
                if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                LatestVersion(
                    versionCode = json.getInt("versionCode"),
                    versionName = json.getString("versionName"),
                    downloadUrl = json.getString("downloadUrl"),
                )
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            // Silent failure: an unreachable update endpoint should never
            // surface as an error to the user — they just don't see a banner.
            null
        }
    }
}
