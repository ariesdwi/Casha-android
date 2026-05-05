package com.casha.app.domain.model

data class EmailSyncStatus(
    val connected: Boolean,
    val emailAddress: String?,
    val authType: String?,
    val isActive: Boolean,
    val lastSyncAt: String?,
    val bankSenders: List<String>
)
