package com.example.caycanh_mobile.data.remote.dto.admin

import kotlinx.serialization.Serializable

@Serializable
data class AdminCustomer(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val totalCompletedOrders: Int = 0,
    val failedDeliveryCount: Int = 0
)