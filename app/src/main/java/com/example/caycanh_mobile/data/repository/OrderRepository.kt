package com.example.caycanh_mobile.data.repository

import com.example.caycanh_mobile.data.remote.api.OrderApi
import com.example.caycanh_mobile.data.remote.dto.common.MessageResponse
import com.example.caycanh_mobile.data.remote.dto.order.CheckoutRequest
import com.example.caycanh_mobile.data.remote.dto.order.CheckoutResponse
import com.example.caycanh_mobile.data.remote.dto.order.OrderResponse
import com.example.caycanh_mobile.data.remote.dto.plant.PageResponse
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderApi: OrderApi
) {

    suspend fun checkout(
        recipientName: String,
        recipientPhone: String,
        shippingAddress: String,
        note: String
    ): Result<CheckoutResponse> = runCatching {
        orderApi.checkout(
            CheckoutRequest(
                recipientName = recipientName.trim(),
                recipientPhone = recipientPhone.trim(),
                shippingAddress = shippingAddress.trim(),
                note = note.trim()
            )
        )
    }.recoverErrorMessage()

    suspend fun getMyOrders(
        status: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<PageResponse<OrderResponse>> = runCatching {
        orderApi.getMyOrders(status, page, size)
    }.recoverErrorMessage()

    suspend fun getOrderById(id: String): Result<OrderResponse> = runCatching {
        orderApi.getOrderById(id)
    }.recoverErrorMessage()

    suspend fun cancelOrder(id: String): Result<MessageResponse> = runCatching {
        orderApi.cancelOrder(id)
    }.recoverErrorMessage()

    private fun <T> Result<T>.recoverErrorMessage(): Result<T> = recoverCatching { e ->
        when (e) {
            is HttpException -> {
                val body = e.response()?.errorBody()?.string().orEmpty()
                val msg = extractMessage(body) ?: when (e.code()) {
                    400 -> "Dữ liệu không hợp lệ"
                    401 -> "Vui lòng đăng nhập lại"
                    404 -> "Không tìm thấy đơn hàng"
                    else -> "Lỗi (mã ${e.code()})"
                }
                throw Exception(msg)
            }
            else -> throw Exception(e.message ?: "Không thể kết nối")
        }
    }

    private fun extractMessage(body: String): String? {
        val match = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)
        return match?.groupValues?.getOrNull(1)
    }
}