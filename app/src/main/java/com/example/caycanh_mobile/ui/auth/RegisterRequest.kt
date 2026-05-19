package com.example.caycanh_mobile.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String
)