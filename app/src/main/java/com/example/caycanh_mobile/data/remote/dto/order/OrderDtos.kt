package com.example.caycanh_mobile.data.remote.dto.order

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Request gửi đi khi checkout.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CheckoutRequest(
    val recipientName: String,
    val recipientPhone: String,
    val shippingAddress: String,
    @EncodeDefault val note: String = ""
)

/**
 * Response checkout — backend tạo 1 hoặc 2 đơn (tách sale/rent).
 */
@Serializable
data class CheckoutResponse(
    val orders: List<OrderResponse> = emptyList(),
    val message: String = ""
)

/**
 * 1 đơn hàng — dùng cho cả checkout response và list orders.
 */
@Serializable
data class OrderResponse(
    val id: String,
    val orderType: String,           // "sale" | "rental"
    val status: String,              // pending | awaiting_deposit | confirmed | delivering | completed | cancelled | delivery_failed
    val paymentStatus: String,       // unpaid | partial | paid
    val totalAmount: Long,
    val recipientName: String,
    val recipientPhone: String,
    val recipientEmail: String? = null,
    val shippingAddress: String,
    val note: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val items: List<OrderItemResponse> = emptyList(),
    val failureReason: String? = null
) {
    val isSale: Boolean get() = orderType == "sale"
    val isRental: Boolean get() = orderType == "rental"

    val canCancel: Boolean
        get() = status in listOf("pending", "awaiting_deposit", "awaiting_payment", "confirmed")
}

@Serializable
data class OrderItemResponse(
    val id: String,
    val plantId: String,
    val plantName: String,
    val primaryImageUrl: String? = null,
    val quantity: Int,
    val unitPrice: Long,
    val subtotal: Long,
    val rental: OrderItemRentalResponse? = null
)

@Serializable
data class OrderItemRentalResponse(
    val duration: Int,
    val durationUnit: String,        // "day" | "week" | "month"
    val startDate: String? = null,
    val endDate: String? = null
)