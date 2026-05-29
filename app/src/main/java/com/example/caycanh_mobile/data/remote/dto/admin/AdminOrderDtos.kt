package com.example.caycanh_mobile.data.remote.dto.admin

import kotlinx.serialization.Serializable

@Serializable
data class AdminOrderResponse(
    val id: String,
    val orderType: String,
    val status: String,
    val paymentStatus: String,
    val totalAmount: Long,
    val shippingAddress: String? = null,
    val note: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val customer: AdminCustomerInfo? = null,
    val items: List<AdminOrderItem> = emptyList()
)

@Serializable
data class AdminCustomerInfo(
    val id: String,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null
)

@Serializable
data class AdminOrderItem(
    val id: String,
    val plantId: String,
    val plantName: String,
    val primaryImageUrl: String? = null,
    val quantity: Int,
    val unitPrice: Long,
    val subtotal: Long,
    val rental: AdminRentalInfo? = null
)

@Serializable
data class AdminRentalInfo(
    val rentalId: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val duration: Int = 0,
    val durationUnit: String = "month",
    val status: String? = null,
    val actualReturnDate: String? = null
)

@Serializable
data class DeliveryFailedRequest(
    val reason: String
)