package com.example.caycanh_mobile.data.remote.api

import com.example.caycanh_mobile.data.remote.dto.common.MessageResponse
import com.example.caycanh_mobile.data.remote.dto.order.CheckoutRequest
import com.example.caycanh_mobile.data.remote.dto.order.CheckoutResponse
import com.example.caycanh_mobile.data.remote.dto.order.OrderResponse
import com.example.caycanh_mobile.data.remote.dto.plant.PageResponse
import retrofit2.http.*

interface OrderApi {

    @POST("api/orders/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): CheckoutResponse

    @GET("api/orders/my")
    suspend fun getMyOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<OrderResponse>

    @GET("api/orders/my/{id}")
    suspend fun getOrderById(@Path("id") id: String): OrderResponse

    @PATCH("api/orders/my/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): OrderResponse
}