package com.example.caycanh_mobile.ui.customer.notifications

import com.example.caycanh_mobile.data.remote.dto.notification.NotificationResponse

data class NotificationUiState(
    val notifications: List<NotificationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)