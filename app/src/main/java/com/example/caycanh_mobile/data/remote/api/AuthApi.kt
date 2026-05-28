package com.example.caycanh_mobile.data.remote.api

import com.example.caycanh_mobile.data.remote.dto.auth.*
import com.example.caycanh_mobile.data.remote.dto.common.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
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
    suspend fun getMe(): Response<UserResponse>

    @PATCH("api/me")
    suspend fun updateMyProfile(
        @Body request: UpdateProfileRequest
    ): Response<UserResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<MessageResponse>

    @POST("api/auth/verify-reset-otp")
    suspend fun verifyResetOtp(
        @Body request: VerifyResetOtpRequest
    ): Response<VerifyResetOtpResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<MessageResponse>
}

/**
 * Request gửi lại OTP khi đăng ký.
 */
@kotlinx.serialization.Serializable
data class ResendOtpRequest(
    val email: String
)