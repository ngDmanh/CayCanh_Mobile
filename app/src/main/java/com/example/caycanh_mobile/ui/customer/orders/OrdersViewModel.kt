package com.example.caycanh_mobile.ui.customer.orders

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
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            orderRepository.getMyOrders(status = null, page = 0, size = 50)
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(isLoading = false, orders = page.content)
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

    fun onTabChange(tab: OrderTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /** Lọc đơn theo tab đang chọn */
    fun getFilteredOrders(): List<com.example.caycanh_mobile.data.remote.dto.order.OrderResponse> {
        val state = _uiState.value
        return when (state.selectedTab) {
            OrderTab.Active -> state.orders.filter { it.isActive }
            OrderTab.Renting -> emptyList()
            OrderTab.Completed -> state.orders.filter { it.isFinished }
        }
    }
}