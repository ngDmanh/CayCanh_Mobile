package com.example.caycanh_mobile.data.repository

import com.example.caycanh_mobile.data.remote.api.NotificationApi
import com.example.caycanh_mobile.data.remote.dto.common.MessageResponse
import com.example.caycanh_mobile.data.remote.dto.notification.NotificationResponse
import com.example.caycanh_mobile.data.remote.dto.plant.PageResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApi: NotificationApi
) {

    suspend fun getMyNotifications(
        page: Int = 0,
        size: Int = 20
    ): Result<PageResponse<NotificationResponse>> = runCatching {
        val response = notificationApi.getMyNotifications(page, size)
        if (response.isSuccessful) {
            response.body() ?: error("Empty response")
        } else {
            error("Lỗi tải thông báo: ${response.code()}")
        }
    }

    suspend fun getUnreadCount(): Result<Long> = runCatching {
        val response = notificationApi.getUnreadCount()
        if (response.isSuccessful) {
            response.body()?.count ?: 0L
        } else {
            error("Lỗi tải số thông báo chưa đọc: ${response.code()}")
        }
    }

    suspend fun markAllAsRead(): Result<MessageResponse> = runCatching {
        val response = notificationApi.markAllAsRead()
        if (response.isSuccessful) {
            response.body() ?: error("Empty response")
        } else {
            error("Lỗi đánh dấu tất cả: ${response.code()}")
        }
    }

    suspend fun markOneAsRead(id: String): Result<MessageResponse> = runCatching {
        val response = notificationApi.markOneAsRead(id)
        if (response.isSuccessful) {
            response.body() ?: error("Empty response")
        } else {
            error("Lỗi đánh dấu đã đọc: ${response.code()}")
        }
    }
}