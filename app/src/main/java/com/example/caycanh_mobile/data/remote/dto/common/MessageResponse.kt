package com.example.caycanh_mobile.data.remote.dto.common

import kotlinx.serialization.Serializable

/**
 * Response dùng chung cho các API chỉ trả về 1 message ngắn.
 * Backend dùng cho register, OTP, forgot-password, mark-read, v.v.
 */
@Serializable
data class MessageResponse(
    val message: String
)