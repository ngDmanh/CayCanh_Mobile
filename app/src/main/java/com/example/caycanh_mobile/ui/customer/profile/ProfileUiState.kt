package com.example.caycanh_mobile.ui.customer.profile

import com.example.caycanh_mobile.data.remote.dto.auth.UserResponse

data class ProfileUiState(
    val user: UserResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showLogoutDialog: Boolean = false
)