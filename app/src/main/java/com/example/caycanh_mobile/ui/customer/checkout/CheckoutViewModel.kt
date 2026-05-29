package com.example.caycanh_mobile.ui.customer.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caycanh_mobile.data.repository.CartRepository
import com.example.caycanh_mobile.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCartLoading = true) }
            cartRepository.getCart()
                .onSuccess { cart ->
                    _uiState.update {
                        it.copy(
                            isCartLoading = false,
                            cartItems = cart.items,
                            totalAmount = cart.totalAmount
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isCartLoading = false) }
                }
        }
    }

    fun onNameChange(v: String) =
        _uiState.update { it.copy(recipientName = v, nameError = null) }

    fun onPhoneChange(v: String) =
        _uiState.update { it.copy(recipientPhone = v, phoneError = null) }

    fun onAddressChange(v: String) =
        _uiState.update { it.copy(shippingAddress = v, addressError = null) }

    fun onNoteChange(v: String) =
        _uiState.update { it.copy(note = v) }

    fun onCheckoutClick() {
        if (!validate()) return

        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isCheckingOut = true, checkoutErrorMessage = null) }

            orderRepository.checkout(
                recipientName = state.recipientName,
                recipientPhone = state.recipientPhone,
                shippingAddress = state.shippingAddress,
                note = state.note
            )
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isCheckingOut = false,
                            createdOrders = response.orders,
                            successMessage = response.message,
                            isCheckoutSuccess = true
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isCheckingOut = false,
                            checkoutErrorMessage = e.message ?: "Đặt hàng thất bại"
                        )
                    }
                }
        }
    }

    fun consumeError() = _uiState.update { it.copy(checkoutErrorMessage = null) }

    private fun validate(): Boolean {
        val state = _uiState.value
        var ok = true

        if (state.recipientName.isBlank()) {
            _uiState.update { it.copy(nameError = "Vui lòng nhập tên người nhận") }
            ok = false
        }
        if (state.recipientPhone.isBlank()) {
            _uiState.update { it.copy(phoneError = "Vui lòng nhập số điện thoại") }
            ok = false
        } else if (!state.recipientPhone.matches(Regex("^0\\d{9,10}$"))) {
            _uiState.update { it.copy(phoneError = "Số điện thoại không hợp lệ") }
            ok = false
        }
        if (state.shippingAddress.isBlank()) {
            _uiState.update { it.copy(addressError = "Vui lòng nhập địa chỉ") }
            ok = false
        } else if (state.shippingAddress.length < 10) {
            _uiState.update { it.copy(addressError = "Địa chỉ tối thiểu 10 ký tự") }
            ok = false
        }
        return ok
    }
}