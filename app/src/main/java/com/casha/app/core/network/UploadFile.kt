package com.casha.app.core.network

import java.io.File

/**
 * Represents a file to upload in multipart form requests.
 * Equivalent to iOS `UploadFile`.
 */
data class UploadFile(
    val fieldName: String,
    val file: File,
    val mimeType: String = "application/octet-stream"
)
