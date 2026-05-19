package com.example.caycanh_mobile.ui.auth.login

/**
 * Snapshot trạng thái màn Login.
 * Mỗi field tương ứng một thứ trên UI.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val role: String? = null
)