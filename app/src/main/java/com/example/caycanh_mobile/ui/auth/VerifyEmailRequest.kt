package com.example.caycanh_mobile.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailRequest(
    val email: String,
    val otp: String
)