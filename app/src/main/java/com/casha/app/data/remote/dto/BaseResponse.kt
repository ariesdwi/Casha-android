package com.casha.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Standard API response envelope.
 * All Casha API endpoints return responses in this format.
 *
 * @param T The type of the data payload.
 */
@Serializable
data class BaseResponse<T>(
    val code: Int = 0,
    val status: String = "error",
    val message: String = "Unknown response",
    val data: T? = null
)
