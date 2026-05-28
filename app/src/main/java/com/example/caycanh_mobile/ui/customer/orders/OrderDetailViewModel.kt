package com.example.caycanh_mobile.ui.customer.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    private val reviewRepository: com.example.caycanh_mobile.data.repository.ReviewRepository
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["id"]) {
        "Thiếu orderId trong route"
    }

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadOrder()
    }

    fun loadOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            orderRepository.getOrderById(orderId)
                .onSuccess { order ->
                    _uiState.update { it.copy(isLoading = false, order = order) }
                    if (order.status == "completed") {       // ← thêm
                        loadReviewedPlants()                  // ← thêm
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Không tải được đơn hàng"
                        )
                    }
                }
        }
    }

    fun onCancelClick() {
        _uiState.update { it.copy(showCancelDialog = true) }
    }

    fun onCancelDismiss() {
        _uiState.update { it.copy(showCancelDialog = false) }
    }

    fun onCancelConfirm() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isCancelling = true, showCancelDialog = false, cancelErrorMessage = null)
            }
            orderRepository.cancelOrder(orderId)
                .onSuccess {
                    _uiState.update {
                        it.copy(isCancelling = false, cancelSuccess = true)
                    }
                    // Reload để cập nhật status
                    loadOrder()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            cancelErrorMessage = e.message ?: "Không thể hủy đơn"
                        )
                    }
                }
        }
    }

    fun consumeCancelSuccess() {
        _uiState.update { it.copy(cancelSuccess = false) }
    }

    fun consumeCancelError() {
        _uiState.update { it.copy(cancelErrorMessage = null) }
    }

    /** Load review của mình → lọc ra plantId đã review trong đơn này */
    private fun loadReviewedPlants() {
        viewModelScope.launch {
            reviewRepository.getMyReviews(page = 0, size = 100)
                .onSuccess { reviews ->
                    val reviewedInThisOrder = reviews
                        .filter { it.orderId == orderId }
                        .map { it.plantId }
                        .toSet()
                    _uiState.update { it.copy(reviewedPlantIds = reviewedInThisOrder) }
                }
        }
    }
}