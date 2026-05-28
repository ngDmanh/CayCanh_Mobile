package com.example.caycanh_mobile.ui.customer.plantdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.repository.CartRepository
import com.example.caycanh_mobile.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val plantRepository: PlantRepository,
    private val cartRepository: CartRepository,
    private val reviewRepository: com.example.caycanh_mobile.data.repository.ReviewRepository
) : ViewModel() {

    private val plantId: String = checkNotNull(savedStateHandle["id"]) {
        "Thiếu plantId trong route"
    }

    private val _uiState = MutableStateFlow(PlantDetailUiState())
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()

    init {
        loadPlant()
    }

    fun loadPlant() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            plantRepository.getPlantById(plantId)
                .onSuccess { plant ->
                    val defaultAction = when {
                        plant.isForSale -> ActionType.Sale
                        plant.isForRent -> ActionType.Rent
                        else -> ActionType.Sale
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            plant = plant,
                            selectedActionType = defaultAction
                        )
                    }
                    loadReviews()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Không tải được")
                    }
                }
        }
    }

    fun onActionTypeChange(type: ActionType) {
        _uiState.update { it.copy(selectedActionType = type) }
    }

    fun onRentUnitChange(unit: RentUnit) {
        _uiState.update { it.copy(selectedRentUnit = unit) }
    }

    fun onRentQuantityChange(delta: Int) {
        val current = _uiState.value.rentQuantity
        val newQty = (current + delta).coerceIn(1, 12)
        _uiState.update { it.copy(rentQuantity = newQty) }
    }

    fun calculateRentTotal(): Long {
        val state = _uiState.value
        val plant = state.plant ?: return 0
        val unitPrice = when (state.selectedRentUnit) {
            RentUnit.Day -> plant.pricePerDay
            RentUnit.Week -> plant.pricePerWeek
            RentUnit.Month -> plant.pricePerMonth
        } ?: 0L
        return unitPrice * state.rentQuantity
    }

    /** Thêm cây hiện tại vào giỏ — tự xử lý theo Sale hay Rent */
    fun addToCart(onSuccess: () -> Unit) {
        val state = _uiState.value
        val plant = state.plant ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToCart = true, addToCartError = null) }

            val result = if (state.selectedActionType == ActionType.Sale) {
                cartRepository.addToCart(
                    plantId = plant.id,
                    itemType = "sale",
                    quantity = 1
                )
            } else {
                cartRepository.addToCart(
                    plantId = plant.id,
                    itemType = "rent",
                    quantity = 1,
                    duration = state.rentQuantity,
                    durationUnit = state.selectedRentUnit.backendValue
                )
            }

            result.onSuccess {
                _uiState.update { it.copy(isAddingToCart = false) }
                onSuccess()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isAddingToCart = false,
                        addToCartError = e.message ?: "Không thêm được vào giỏ"
                    )
                }
            }
        }
    }

    fun consumeAddToCartError() {
        _uiState.update { it.copy(addToCartError = null) }
    }

    /** Load review + summary của cây này */
    private fun loadReviews() {
        viewModelScope.launch {
            reviewRepository.getPlantReviewSummary(plantId)
                .onSuccess { summary ->
                    _uiState.update {
                        it.copy(
                            averageRating = summary.averageRating,
                            totalReviews = summary.totalReviews
                        )
                    }
                }
            reviewRepository.getPlantReviews(plantId, page = 0, size = 20)
                .onSuccess { list ->
                    _uiState.update { it.copy(reviews = list) }
                }
        }
    }
}