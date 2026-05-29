package com.example.caycanh_mobile.ui.admin.rentals

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class RentalStatusDisplay(val label: String, val color: Color)

fun rentalStatusDisplay(status: String): RentalStatusDisplay = when (status) {
    "pending_delivery" -> RentalStatusDisplay("Chờ giao cây", Color(0xFFFF9800))
    "active" -> RentalStatusDisplay("Đang thuê", Color(0xFF1976D2))
    "overdue" -> RentalStatusDisplay("Quá hạn", Color(0xFFE53935))
    "returned" -> RentalStatusDisplay("Đã trả", Color(0xFF9E9E9E))
    else -> RentalStatusDisplay(status, Color.Gray)
}

fun durationUnitLabel(unit: String): String = when (unit) {
    "day" -> "ngày"
    "week" -> "tuần"
    "month" -> "tháng"
    else -> unit
}

/** Trả về số ngày còn lại (>0) hoặc số ngày quá hạn (<0) */
fun daysToEnd(endDateIso: String?): Int? {
    if (endDateIso == null) return null
    return try {
        val end = LocalDate.parse(endDateIso)
        ChronoUnit.DAYS.between(LocalDate.now(), end).toInt()
    } catch (e: Exception) { null }
}