package com.example.caycanh_mobile.ui.customer.checkout

import com.example.caycanh_mobile.data.remote.dto.cart.CartItemResponse
import com.example.caycanh_mobile.data.remote.dto.order.OrderResponse

data class CheckoutUiState(
    // Cart preview
    val cartItems: List<CartItemResponse> = emptyList(),
    val totalAmount: Long = 0,
    val isCartLoading: Boolean = false,

    // Form
    val recipientName: String = "",
    val recipientPhone: String = "",
    val shippingAddress: String = "",
    val note: String = "",

    // Errors
    val nameError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,

    // Checkout state
    val isCheckingOut: Boolean = false,
    val checkoutErrorMessage: String? = null,
    val createdOrders: List<OrderResponse> = emptyList(),
    val successMessage: String? = null,
    val isCheckoutSuccess: Boolean = false
)