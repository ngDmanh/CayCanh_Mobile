package com.example.caycanh_mobile.ui.customer.orders

import com.example.caycanh_mobile.data.remote.dto.order.OrderResponse

data class OrdersUiState(
    val orders: List<OrderResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: OrderTab = OrderTab.Active,
    val reviewedPlantIds: Set<String> = emptySet()
)

/**
 * Tab phân loại đơn — đang theo dõi vs đã hoàn thành.
 */
enum class OrderTab(val label: String) {
    Active("Đang theo dõi"),
    Renting("Đang thuê"),
    Completed("Đã hoàn thành")
}

/**
 * Helper xác định đơn có "đang theo dõi" không.
 * Đơn đang theo dõi: chưa kết thúc, cần khách quan tâm.
 */
val OrderResponse.isActive: Boolean
    get() = status in listOf(
        "pending",
        "awaiting_deposit",
        "awaiting_payment",
        "confirmed",
        "delivering"
    )

/**
 * Đơn đã hoàn thành (completed, cancelled, delivery_failed).
 */
val OrderResponse.isFinished: Boolean
    get() = status in listOf("completed", "cancelled", "delivery_failed")