package com.example.caycanh_mobile.data.remote.dto.notification

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val id: String,
    val title: String,
    val body: String,
    val type: String,             // order | rental | review | system
    val refType: String? = null,  // orders | rentals | null
    val refId: String? = null,    // UUID liên quan
    val isRead: Boolean = false,
    val createdAt: String
)

@Serializable
data class UnreadCountResponse(
    val count: Long = 0
)