package com.example.caycanh_mobile.data.repository

import com.example.caycanh_mobile.data.remote.api.CartApi
import com.example.caycanh_mobile.data.remote.dto.cart.AddToCartRequest
import com.example.caycanh_mobile.data.remote.dto.cart.CartResponse
import com.example.caycanh_mobile.data.remote.dto.cart.UpdateCartItemRequest
import com.example.caycanh_mobile.data.remote.dto.common.MessageResponse
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val cartApi: CartApi
) {

    suspend fun getCart(): Result<CartResponse> = runCatching {
        cartApi.getCart()
    }.recoverErrorMessage()

    suspend fun addToCart(
        plantId: String,
        itemType: String,
        quantity: Int = 1,
        duration: Int? = null,
        durationUnit: String? = null
    ): Result<CartResponse> = runCatching {
        cartApi.addToCart(
            AddToCartRequest(
                plantId = plantId,
                itemType = itemType,
                quantity = quantity,
                duration = duration,
                durationUnit = durationUnit
            )
        )
    }.recoverErrorMessage()

    suspend fun updateCartItem(
        itemId: String,
        quantity: Int? = null,
        duration: Int? = null,
        durationUnit: String? = null
    ): Result<CartResponse> = runCatching {
        cartApi.updateCartItem(
            itemId,
            UpdateCartItemRequest(quantity, duration, durationUnit)
        )
    }.recoverErrorMessage()

    suspend fun removeCartItem(itemId: String): Result<CartResponse> = runCatching {
        cartApi.removeCartItem(itemId)
    }.recoverErrorMessage()

    suspend fun clearCart(): Result<MessageResponse> = runCatching {
        cartApi.clearCart()
    }.recoverErrorMessage()

    // ── Error helpers ───────────────────────────────────────────

    private fun <T> Result<T>.recoverErrorMessage(): Result<T> = recoverCatching { e ->
        when (e) {
            is HttpException -> {
                val body = e.response()?.errorBody()?.string().orEmpty()
                val msg = extractMessage(body) ?: when (e.code()) {
                    400 -> "Dữ liệu không hợp lệ"
                    401 -> "Vui lòng đăng nhập lại"
                    404 -> "Không tìm thấy"
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