package com.example.caycanh_mobile.data.remote.dto.auth

import kotlinx.serialization.Serializable

/**
 * Response sau khi login/verify-email thành công.
 * Backend trả về kèm token và thông tin user.
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long = 7200,
    val user: UserResponse
)

/**
 * Thông tin user — dùng cho cả /api/me và phần user trong AuthResponse.
 */
@Serializable
data class UserResponse(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val role: String = "customer",
    val isActive: Boolean = true
)