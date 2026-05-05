package com.casha.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectOAuthRequestDTO(
    @SerialName("serverAuthCode") val serverAuthCode: String
)

@Serializable
data class EmailSyncStatusDTO(
    @SerialName("connected") val connected: Boolean = false,
    @SerialName("emailAddress") val emailAddress: String? = null,
    @SerialName("authType") val authType: String? = null,
    @SerialName("isActive") val isActive: Boolean = false,
    @SerialName("lastSyncAt") val lastSyncAt: String? = null,
    @SerialName("bankSenders") val bankSenders: List<String> = emptyList()
)
