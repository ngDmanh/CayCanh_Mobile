package com.example.caycanh_mobile.data.remote.dto.admin

import kotlinx.serialization.Serializable

/** 1 dòng trong view v_order_summary — đại diện 1 đơn hàng */
@Serializable
data class OrderSummaryItem(
    val id: String,
    val orderType: String,
    val status: String,
    val paymentStatus: String,
    val totalAmount: Long,
    val createdAt: String,
    val customerName: String,
    val customerPhone: String,
    val itemCount: Int
)
@Serializable
data class RevenueMonthlyItem(
    val month: String,                // ISO datetime, vd "2026-04-30T17:00:00Z"
    val orderType: String,            // "sale" | "rental"
    val totalOrders: Long = 0,
    val revenue: Long = 0
)

@Serializable
data class RevenueByTypeItem(
    val orderType: String,            // "sale" | "rental"
    val totalOrders: Long = 0,
    val totalRevenue: Long = 0,
    val avgOrderValue: Double = 0.0
)

@Serializable
data class TopPlantItem(
    val id: String,
    val name: String,
    val listingType: String,          // "sale" | "rent" | "both"
    val totalQuantity: Long = 0,
    val totalRevenue: Long = 0,
    val avgRating: Double? = null,
    val reviewCount: Long = 0
)

@Serializable
data class LowStockItem(
    val id: String,
    val name: String,
    val listingType: String,
    val stockQuantity: Int = 0,
    val rentAvailableQty: Int = 0,
    val categoryName: String? = null
)