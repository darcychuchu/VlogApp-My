package com.vlog.my.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ApiResult is a sealed class to represent the result of an API call.
 * It can either be a Success with data or an Error with a message and optional status code.
 */
sealed class ApiResult {
    data class Success(val data: String) : ApiResult()
    data class Error(val message: String, val statusCode: Int? = null) : ApiResult()
}

/**
 * Fetches JSON data from the given API URL.
 *
 * This function performs a GET request and handles basic error scenarios including
 * URL malformation, network issues, and HTTP error responses.
 *
 * @param apiUrl The URL string to fetch data from.
 * @return ApiResult which is either ApiResult.Success containing the response data
 *         or ApiResult.Error containing an error message and optional HTTP status code.
 *
 * Note: Ensure the application has the `<uses-permission android:name="android.permission.INTERNET" />`
 * permission in its AndroidManifest.xml.
 */
suspend fun fetchJsonFromApi(apiUrl: String): ApiResult = withContext(Dispatchers.IO) {
    val url: URL
    try {
        url = URL(apiUrl)
    } catch (e: MalformedURLException) {
        return@withContext ApiResult.Error("Invalid URL format: ${e.message}")
    }

    var connection: HttpURLConnection? = null
    try {
        connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000 // 15 seconds
        connection.readTimeout = 15000  // 15 seconds
        // connection.setRequestProperty("Accept", "application/json") // Optional: set if API requires it

        connection.connect() // Explicitly connect, though getResponseCode() implicitly connects

        val responseCode = connection.responseCode

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Use 'use' block for automatic resource management (closing streams)
            val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                val stringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                stringBuilder.toString()
            }
            ApiResult.Success(response)
        } else {
            val errorStream = connection.errorStream
            val errorMessage = try {
                errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "No error details from server."
            } catch (e: Exception) {
                "Failed to read error stream: ${e.message}"
            }
            ApiResult.Error("HTTP Error: $responseCode. $errorMessage", statusCode = responseCode)
        }
    } catch (e: Exception) { // Catch more specific exceptions like SocketTimeoutException, IOException if needed
        ApiResult.Error("Network request failed: ${e.message ?: "Unknown error"}")
    } finally {
        connection?.disconnect()
    }
}
