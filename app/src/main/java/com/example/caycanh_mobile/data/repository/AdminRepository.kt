package com.example.caycanh_mobile.data.repository

import com.example.caycanh_mobile.data.remote.api.AdminApi
import com.example.caycanh_mobile.data.remote.dto.admin.*
import com.example.caycanh_mobile.data.remote.dto.plant.PlantResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val adminApi: AdminApi
) {
    suspend fun getOrderSummary(): Result<List<OrderSummaryItem>> = runCatching {
        val response = adminApi.getOrderSummary()
        if (response.isSuccessful) response.body() ?: emptyList()
        else error("Lỗi tải thống kê: ${response.code()}")
    }

    suspend fun createPlant(request: CreatePlantRequest): Result<PlantResponse> = runCatching {
        val response = adminApi.createPlant(request)
        if (response.isSuccessful) response.body() ?: error("Empty response")
        else error(extractError(response.errorBody()?.string()) ?: "Lỗi tạo cây: ${response.code()}")
    }

    suspend fun updatePlant(id: String, request: UpdatePlantRequest): Result<PlantResponse> = runCatching {
        val response = adminApi.updatePlant(id, request)
        if (response.isSuccessful) response.body() ?: error("Empty response")
        else error(extractError(response.errorBody()?.string()) ?: "Lỗi sửa cây: ${response.code()}")
    }

    suspend fun deletePlant(id: String): Result<Unit> = runCatching {
        val response = adminApi.deletePlant(id)
        if (response.isSuccessful) Unit
        else error("Lỗi xóa cây: ${response.code()}")
    }

    /** Upload 1 file ảnh, trả về URL Cloudinary */
    suspend fun uploadImage(file: File): Result<String> = runCatching {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val response = adminApi.uploadImage(part)
        if (response.isSuccessful) {
            response.body()?.imageUrl ?: error("Không nhận được URL ảnh")
        } else {
            error("Lỗi upload ảnh: ${response.code()}")
        }
    }

    private fun extractError(body: String?): String? {
        if (body == null) return null
        return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
    }

    suspend fun getOrders(status: String? = null): Result<List<AdminOrderResponse>> = runCatching {
        val response = adminApi.getOrders(status = status, page = 0, size = 100)
        if (response.isSuccessful) response.body()?.content ?: emptyList()
        else error("Lỗi tải đơn: ${response.code()}")
    }

    suspend fun getOrderDetail(id: String): Result<AdminOrderResponse> = runCatching {
        val response = adminApi.getOrderDetail(id)
        if (response.isSuccessful) response.body() ?: error("Empty response")
        else error("Lỗi tải đơn: ${response.code()}")
    }

    private suspend fun runAction(call: suspend () -> retrofit2.Response<AdminOrderResponse>): Result<AdminOrderResponse> = runCatching {
        val response = call()
        if (response.isSuccessful) response.body() ?: error("Empty response")
        else error(extractError(response.errorBody()?.string()) ?: "Lỗi: ${response.code()}")
    }

    suspend fun confirmOrder(id: String) = runAction { adminApi.confirmOrder(id) }
    suspend fun confirmDeposit(id: String) = runAction { adminApi.confirmDeposit(id) }
    suspend fun startDelivery(id: String) = runAction { adminApi.startDelivery(id) }
    suspend fun markPaid(id: String) = runAction { adminApi.markPaid(id) }
    suspend fun completeOrder(id: String) = runAction { adminApi.completeOrder(id) }
    suspend fun markDeliveryFailed(id: String, reason: String) =
        runAction { adminApi.markDeliveryFailed(id, DeliveryFailedRequest(reason)) }

    suspend fun getRentals(status: String? = null): Result<List<AdminRentalResponse>> = runCatching {
        val response = adminApi.getRentals(status = status, page = 0, size = 200)
        if (response.isSuccessful) response.body()?.content ?: emptyList()
        else error("Lỗi tải rental: ${response.code()}")
    }

    suspend fun markRentalDelivered(id: String): Result<AdminRentalResponse> = runCatching {
        val response = adminApi.markRentalDelivered(id)
        if (response.isSuccessful) response.body() ?: error("Empty response")
        else error(extractError(response.errorBody()?.string()) ?: "Lỗi: ${response.code()}")
    }

    suspend fun markRentalCollected(id: String, condition: String): Result<AdminRentalResponse> = runCatching {
        val response = adminApi.markRentalCollected(id, CollectRentalRequest(conditionOnReturn = condition))
        if (response.isSuccessful) response.body() ?: error("Empty response")
        else error(extractError(response.errorBody()?.string()) ?: "Lỗi: ${response.code()}")
    }

    suspend fun getRevenueMonthly(): Result<List<RevenueMonthlyItem>> = runCatching {
        val response = adminApi.getRevenueMonthly()
        if (response.isSuccessful) response.body() ?: emptyList()
        else error("Lỗi tải doanh thu tháng: ${response.code()}")
    }

    suspend fun getRevenueByType(): Result<List<RevenueByTypeItem>> = runCatching {
        val response = adminApi.getRevenueByType()
        if (response.isSuccessful) response.body() ?: emptyList()
        else error("Lỗi tải doanh thu theo loại: ${response.code()}")
    }

    suspend fun getTopPlants(): Result<List<TopPlantItem>> = runCatching {
        val response = adminApi.getTopPlants()
        if (response.isSuccessful) response.body() ?: emptyList()
        else error("Lỗi tải top cây: ${response.code()}")
    }

    suspend fun getLowStock(): Result<List<LowStockItem>> = runCatching {
        val response = adminApi.getLowStock()
        if (response.isSuccessful) response.body() ?: emptyList()
        else error("Lỗi tải tồn kho: ${response.code()}")
    }

    suspend fun getCustomers(): Result<List<AdminCustomer>> = runCatching {
        val response = adminApi.getCustomers(page = 0, size = 200)
        if (response.isSuccessful) response.body()?.content ?: emptyList()
        else error("Lỗi tải khách hàng: ${response.code()}")
    }


}