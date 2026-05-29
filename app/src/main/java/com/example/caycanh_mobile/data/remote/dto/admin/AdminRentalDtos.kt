package com.example.caycanh_mobile.data.remote.dto.admin

import kotlinx.serialization.Serializable

@Serializable
data class AdminRentalResponse(
    val id: String,
    val plantId: String,
    val plantName: String,
    val primaryImageUrl: String? = null,
    val duration: Int,
    val durationUnit: String,         // day | week | month
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String,               // pending_delivery | active | returned | overdue
    val totalRentalFee: Long,
    val actualReturnDate: String? = null,
    val conditionOnReturn: String? = null,
    val parentRentalId: String? = null,
    val createdAt: String,
    val customer: AdminCustomerInfo? = null
)

@Serializable
data class CollectRentalRequest(
    val conditionOnReturn: String
)