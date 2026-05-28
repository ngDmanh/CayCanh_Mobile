package com.example.caycanh_mobile.ui.customer.plantdetail

import com.example.caycanh_mobile.data.remote.dto.plant.PlantResponse
import com.example.caycanh_mobile.data.remote.dto.review.ReviewResponse

data class PlantDetailUiState(
    val plant: PlantResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // User chọn mua hay thuê (khi cây listingType = "both")
    val selectedActionType: ActionType = ActionType.Sale,

    // Cho thuê — user chọn khung thời gian
    val selectedRentUnit: RentUnit = RentUnit.Month,
    val rentQuantity: Int = 1,
    val isAddingToCart: Boolean = false,
    val addToCartError: String? = null,
    val reviews: List<ReviewResponse> = emptyList(),      // ← thêm
    val averageRating: Double? = null,                    // ← thêm
    val totalReviews: Long = 0
)

enum class ActionType { Sale, Rent }
enum class RentUnit(val label: String, val backendValue: String) {
    Day("Ngày", "day"),
    Week("Tuần", "week"),
    Month("Tháng", "month")
}

