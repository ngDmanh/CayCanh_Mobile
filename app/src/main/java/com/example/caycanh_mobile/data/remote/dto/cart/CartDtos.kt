package com.example.caycanh_mobile.data.remote.dto.cart

import kotlinx.serialization.Serializable

/**
 * Response giỏ hàng — backend trả về cấu trúc đầy đủ.
 */
@Serializable
data class CartResponse(
    val cartId: String,
    val items: List<CartItemResponse> = emptyList(),
    val totalItems: Int = 0,
    val totalAmount: Long = 0
)

@Serializable
data class CartItemResponse(
    val id: String,
    val plantId: String,
    val plantName: String,
    val primaryImageUrl: String? = null,
    val itemType: String,           // "sale" | "rent"
    val quantity: Int,
    val duration: Int? = null,      // chỉ có khi rent
    val durationUnit: String? = null, // "day" | "week" | "month"
    val unitPrice: Long,
    val subtotal: Long
) {
    val isRent: Boolean get() = itemType == "rent"
}

/**
 * Request thêm vào giỏ — gửi từ Plant Detail.
 */
@Serializable
data class AddToCartRequest(
    val plantId: String,
    val itemType: String,                  // "sale" | "rent"
    val quantity: Int = 1,
    val duration: Int? = null,
    val durationUnit: String? = null       // "day" | "week" | "month"
)

/**
 * Request sửa item trong giỏ.
 */
@Serializable
data class UpdateCartItemRequest(
    val quantity: Int? = null,
    val duration: Int? = null,
    val durationUnit: String? = null
)