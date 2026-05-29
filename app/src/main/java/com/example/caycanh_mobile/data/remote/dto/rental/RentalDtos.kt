package com.example.caycanh_mobile.data.remote.dto.rental

import kotlinx.serialization.Serializable

@Serializable
data class CustomerRentalResponse(
    val id: String,
    val plantId: String,
    val plantName: String,
    val primaryImageUrl: String? = null,
    val duration: Int = 0,
    val durationUnit: String = "month",
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String,                       // pending_delivery | active | returned | overdue
    val totalRentalFee: Long = 0,
    val actualReturnDate: String? = null,
    val conditionOnReturn: String? = null,
    val parentRentalId: String? = null,
    val createdAt: String
)