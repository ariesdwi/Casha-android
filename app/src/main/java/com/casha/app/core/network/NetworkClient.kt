package com.casha.app.core.network

import com.casha.app.core.config.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generic network client for non-Retrofit calls.
 * Fixed to ensure network calls are NOT on the main thread.
 */
@Singleton
class NetworkClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @PublishedApi internal val json: Json
) {

    /**
     * Perform a JSON request to [path] with optional [body].
     * Returns the raw response body as a String.
     */
    suspend fun request(
        path: String,
        method: String = "GET",
        body: String? = null,
        headers: Map<String, String> = emptyMap()
    ): String = withContext(Dispatchers.IO) {
        val url = if (path.startsWith("http")) path else AppConfig.baseUrl + path

        val requestBody = body?.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .apply {
                headers.forEach { (key, value) -> addHeader(key, value) }
                when (method.uppercase()) {
                    "GET" -> get()
                    "POST" -> post(requestBody ?: "".toRequestBody(null))
                    "PUT" -> put(requestBody ?: "".toRequestBody(null))
                    "PATCH" -> patch(requestBody ?: "".toRequestBody(null))
                    "DELETE" -> if (requestBody != null) delete(requestBody) else delete()
                }
            }
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            response.body?.string() ?: ""
        } catch (e: Exception) {
            throw IOException("Network request failed: ${e.message}", e)
        }
    }

    /**
     * Upload multipart form data.
     */
    suspend fun uploadForm(
        path: String,
        formFields: Map<String, String> = emptyMap(),
        files: List<UploadFile> = emptyList(),
        headers: Map<String, String> = emptyMap()
    ): String = withContext(Dispatchers.IO) {
        val url = if (path.startsWith("http")) path else AppConfig.baseUrl + path

        val multipartBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        formFields.forEach { (key, value) ->
            multipartBuilder.addFormDataPart(key, value)
        }

        files.forEach { uploadFile ->
            val mediaType = uploadFile.mimeType.toMediaType()
            val fileBody = uploadFile.file.asRequestBody(mediaType)
            multipartBuilder.addFormDataPart(
                uploadFile.fieldName,
                uploadFile.file.name,
                fileBody
            )
        }

        val request = Request.Builder()
            .url(url)
            .post(multipartBuilder.build())
            .apply {
                headers.forEach { (key, value) -> addHeader(key, value) }
            }
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            response.body?.string() ?: ""
        } catch (e: Exception) {
            throw IOException("Upload failed: ${e.message}", e)
        }
    }

    /**
     * Parse JSON response string into a typed object.
     */
    inline fun <reified T> decode(responseBody: String): T {
        return json.decodeFromString<T>(responseBody)
    }
}
