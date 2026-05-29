package com.example.caycanh_mobile.ui.customer.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.remote.dto.cart.CartResponse
import com.example.caycanh_mobile.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            cartRepository.getCart()
                .onSuccess { cart -> applyCart(cart) }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Không tải được giỏ hàng"
                        )
                    }
                }
        }
    }

    /** Tăng/giảm quantity cho item sale, hoặc tăng/giảm duration cho item rent */
    fun onQuantityChange(itemId: String, delta: Int) {
        val item = _uiState.value.items.firstOrNull { it.id == itemId } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(processingItemId = itemId, errorMessage = null) }

            val result = if (item.isRent) {
                val newDuration = (item.duration ?: 1) + delta
                if (newDuration < 1) {
                    _uiState.update { it.copy(processingItemId = null) }
                    return@launch
                }
                cartRepository.updateCartItem(
                    itemId = itemId,
                    quantity = item.quantity,
                    duration = newDuration,
                    durationUnit = item.durationUnit
                )
            } else {
                val newQty = item.quantity + delta
                if (newQty < 1) {
                    _uiState.update { it.copy(processingItemId = null) }
                    return@launch
                }
                cartRepository.updateCartItem(
                    itemId = itemId,
                    quantity = newQty
                )
            }

            result.onSuccess { cart -> applyCart(cart) }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            processingItemId = null,
                            errorMessage = e.message ?: "Không cập nhật được"
                        )
                    }
                }
        }
    }

    fun onRemoveClick(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(processingItemId = itemId, errorMessage = null) }
            cartRepository.removeCartItem(itemId)
                .onSuccess { cart -> applyCart(cart) }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            processingItemId = null,
                            errorMessage = e.message ?: "Không xóa được"
                        )
                    }
                }
        }
    }

    fun consumeError() = _uiState.update { it.copy(errorMessage = null) }

    private fun applyCart(cart: CartResponse) {
        _uiState.update {
            it.copy(
                isLoading = false,
                processingItemId = null,
                items = cart.items,
                totalItems = cart.totalItems,
                totalAmount = cart.totalAmount
            )
        }
    }
}