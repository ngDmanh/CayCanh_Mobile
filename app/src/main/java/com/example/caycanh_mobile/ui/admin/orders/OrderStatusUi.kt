package com.example.caycanh_mobile.ui.admin.orders

import androidx.compose.ui.graphics.Color

data class StatusDisplay(val label: String, val color: Color)

fun orderStatusDisplay(status: String): StatusDisplay = when (status) {
    "pending" -> StatusDisplay("Chờ xác nhận", Color(0xFFFF9800))
    "awaiting_deposit" -> StatusDisplay("Chờ cọc", Color(0xFFFF9800))
    "awaiting_payment" -> StatusDisplay("Chờ thanh toán", Color(0xFFFF9800))
    "confirmed" -> StatusDisplay("Đã xác nhận", Color(0xFF1976D2))
    "delivering" -> StatusDisplay("Đang giao", Color(0xFF7B1FA2))
    "completed" -> StatusDisplay("Hoàn thành", Color(0xFF4CAF50))
    "cancelled" -> StatusDisplay("Đã hủy", Color(0xFF9E9E9E))
    "delivery_failed" -> StatusDisplay("Giao thất bại", Color(0xFFE53935))
    else -> StatusDisplay(status, Color.Gray)
}

fun paymentStatusLabel(status: String): String = when (status) {
    "paid" -> "Đã thanh toán"
    "partial" -> "Đã cọc một phần"
    else -> "Chưa thanh toán"
}