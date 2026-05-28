package com.example.caycanh_mobile.data.remote.api

import com.example.caycanh_mobile.data.remote.dto.common.MessageResponse
import com.example.caycanh_mobile.data.remote.dto.notification.NotificationResponse
import com.example.caycanh_mobile.data.remote.dto.notification.UnreadCountResponse
import com.example.caycanh_mobile.data.remote.dto.plant.PageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {

    @GET("api/notifications")
    suspend fun getMyNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<NotificationResponse>>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @PATCH("api/notifications/mark-all-read")
    suspend fun markAllAsRead(): Response<MessageResponse>

    @PATCH("api/notifications/{id}/read")
    suspend fun markOneAsRead(@Path("id") id: String): Response<MessageResponse>
}
