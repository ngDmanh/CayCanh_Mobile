package com.example.caycanh_mobile.ui.auth.verifyemail

data class VerifyEmailUiState(
    val email: String = "",
    val otp: String = "",
    val otpError: String? = null,
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isVerifySuccess: Boolean = false,
    val role: String? = null
)