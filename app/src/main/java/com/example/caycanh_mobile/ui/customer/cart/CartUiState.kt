package com.example.caycanh_mobile.ui.customer.cart

import com.example.caycanh_mobile.data.remote.dto.cart.CartItemResponse

data class CartUiState(
    val items: List<CartItemResponse> = emptyList(),
    val totalItems: Int = 0,
    val totalAmount: Long = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val processingItemId: String? = null
)