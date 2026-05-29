package com.example.caycanh_mobile.data.remote.api

import com.example.caycanh_mobile.data.remote.dto.admin.*
import com.example.caycanh_mobile.data.remote.dto.plant.PageResponse
import com.example.caycanh_mobile.data.remote.dto.plant.PlantResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AdminApi {

    @GET("api/admin/stats/order-summary")
    suspend fun getOrderSummary(): Response<List<OrderSummaryItem>>

    // ── Quản lý cây ──
    @POST("api/admin/plants")
    suspend fun createPlant(@Body request: CreatePlantRequest): Response<PlantResponse>

    @PUT("api/admin/plants/{id}")
    suspend fun updatePlant(
        @Path("id") id: String,
        @Body request: UpdatePlantRequest
    ): Response<PlantResponse>

    @DELETE("api/admin/plants/{id}")
    suspend fun deletePlant(@Path("id") id: String): Response<Unit>

    // ── Upload ảnh ──
    @Multipart
    @POST("api/admin/upload-image")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<UploadImageResponse>

    @GET("api/admin/orders")
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("orderType") orderType: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<PageResponse<AdminOrderResponse>>

    @GET("api/admin/orders/{id}")
    suspend fun getOrderDetail(@Path("id") id: String): Response<AdminOrderResponse>

    @PATCH("api/admin/orders/{id}/confirm")
    suspend fun confirmOrder(@Path("id") id: String): Response<AdminOrderResponse>

    @PATCH("api/admin/orders/{id}/confirm-deposit")
    suspend fun confirmDeposit(@Path("id") id: String): Response<AdminOrderResponse>

    @PATCH("api/admin/orders/{id}/start-delivery")
    suspend fun startDelivery(@Path("id") id: String): Response<AdminOrderResponse>

    @PATCH("api/admin/orders/{id}/payment")
    suspend fun markPaid(@Path("id") id: String): Response<AdminOrderResponse>

    @PATCH("api/admin/orders/{id}/complete")
    suspend fun completeOrder(@Path("id") id: String): Response<AdminOrderResponse>

    @PATCH("api/admin/orders/{id}/delivery-failed")
    suspend fun markDeliveryFailed(
        @Path("id") id: String,
        @Body request: DeliveryFailedRequest
    ): Response<AdminOrderResponse>

    @GET("api/admin/rentals")
    suspend fun getRentals(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<PageResponse<AdminRentalResponse>>

    @PATCH("api/admin/rentals/{id}/deliver")
    suspend fun markRentalDelivered(@Path("id") id: String): Response<AdminRentalResponse>

    @PATCH("api/admin/rentals/{id}/collect")
    suspend fun markRentalCollected(
        @Path("id") id: String,
        @Body request: CollectRentalRequest
    ): Response<AdminRentalResponse>

    @GET("api/admin/stats/revenue-monthly")
    suspend fun getRevenueMonthly(): Response<List<RevenueMonthlyItem>>

    @GET("api/admin/stats/revenue-by-type")
    suspend fun getRevenueByType(): Response<List<RevenueByTypeItem>>

    @GET("api/admin/stats/top-plants")
    suspend fun getTopPlants(): Response<List<TopPlantItem>>

    @GET("api/admin/stats/low-stock")
    suspend fun getLowStock(): Response<List<LowStockItem>>

    @GET("api/admin/customers")
    suspend fun getCustomers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<PageResponse<AdminCustomer>>
}