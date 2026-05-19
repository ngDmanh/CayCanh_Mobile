package com.example.caycanh_mobile.data.remote.api

import com.example.caycanh_mobile.data.remote.dto.auth.*
import com.example.caycanh_mobile.data.remote.dto.common.MessageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Khai báo các API auth của backend.
 */
interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): MessageResponse

    @POST("api/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): AuthResponse

    @POST("api/auth/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): MessageResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("api/me")
    suspend fun getMe(): UserResponse
}

/**
 * Request gửi lại OTP khi đăng ký.
 */
@kotlinx.serialization.Serializable
data class ResendOtpRequest(
    val email: String
)