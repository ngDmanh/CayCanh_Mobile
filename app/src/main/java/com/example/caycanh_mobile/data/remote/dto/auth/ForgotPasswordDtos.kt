package com.example.caycanh_mobile.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class VerifyResetOtpRequest(
    val email: String,
    val otp: String
)

@Serializable
data class VerifyResetOtpResponse(
    val resetToken: String,
    val expiresInSeconds: Long = 600,
    val message: String = ""
)

@Serializable
data class ResetPasswordRequest(
    val resetToken: String,
    val newPassword: String
)