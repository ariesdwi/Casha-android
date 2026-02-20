package com.casha.app.core.network

import com.casha.app.core.config.AppConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generic network client for non-Retrofit calls (dynamic URLs, multipart uploads).
 * Equivalent to iOS `NetworkClient` protocol + `APIClient` implementation.
 *
 * For standard API calls, use Retrofit service interfaces directly (e.g. `AuthApiService`).
 * Use this client for:
 * - Multipart form uploads (e.g. image upload for chat/parse-image)
 * - Dynamic endpoint calls that don't fit Retrofit annotations
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
    ): String {
        val url = AppConfig.baseUrl + path

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

        val response = okHttpClient.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    /**
     * Upload multipart form data.
     * Equivalent to iOS `APIClient.uploadForm()`.
     *
     * @param path API path (e.g. "chat/parse-image")
     * @param formFields string key-value pairs
     * @param files list of [UploadFile] to attach
     * @param headers additional headers
     * @return raw response body as String
     */
    suspend fun uploadForm(
        path: String,
        formFields: Map<String, String> = emptyMap(),
        files: List<UploadFile> = emptyList(),
        headers: Map<String, String> = emptyMap()
    ): String {
        val url = AppConfig.baseUrl + path

        val multipartBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        // Add form fields
        formFields.forEach { (key, value) ->
            multipartBuilder.addFormDataPart(key, value)
        }

        // Add files
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

        val response = okHttpClient.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    /**
     * Parse JSON response string into a typed object.
     */
    inline fun <reified T> decode(responseBody: String): T {
        return json.decodeFromString<T>(responseBody)
    }
}
