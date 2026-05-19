package com.example.caycanh_mobile.data.repository

import com.example.caycanh_mobile.data.local.TokenManager
import com.example.caycanh_mobile.data.remote.api.AuthApi
import com.example.caycanh_mobile.data.remote.api.ResendOtpRequest
import com.example.caycanh_mobile.data.remote.dto.auth.*
import com.example.caycanh_mobile.data.remote.dto.common.MessageResponse
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cầu nối giữa ViewModel và data source (API + DataStore).
 *
 * ViewModel chỉ gọi vào đây — không bao giờ gọi trực tiếp AuthApi hay TokenManager.
 *
 * Trả về Result<T> để ViewModel dễ xử lý success/failure mà không cần try-catch.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    /** Đăng ký — backend gửi OTP về email */
    suspend fun register(req: RegisterRequest): Result<MessageResponse> = runCatching {
        authApi.register(req)
    }.recoverErrorMessage()

    /** Verify OTP — đăng ký thành công, trả về token + user, tự lưu vào DataStore */
    suspend fun verifyEmail(req: VerifyEmailRequest): Result<AuthResponse> = runCatching {
        val response = authApi.verifyEmail(req)
        saveAuthToLocal(response)
        response
    }.recoverErrorMessage()

    /** Gửi lại OTP */
    suspend fun resendOtp(email: String): Result<MessageResponse> = runCatching {
        authApi.resendOtp(ResendOtpRequest(email))
    }.recoverErrorMessage()

    /** Login — lưu token và user info vào DataStore */
    suspend fun login(email: String, password: String): Result<AuthResponse> = runCatching {
        val response = authApi.login(LoginRequest(email, password))
        saveAuthToLocal(response)
        response
    }.recoverErrorMessage()

    /** Logout — xóa token */
    suspend fun logout() {
        tokenManager.clearAuth()
    }

    /** Check đã login chưa — dùng cho splash/auto-navigate */
    suspend fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    // ── Private helpers ────────────────────────────────────────

    private suspend fun saveAuthToLocal(response: AuthResponse) {
        tokenManager.saveAuth(
            token = response.accessToken,
            userId = response.user.id,
            role = response.user.role,
            fullName = response.user.fullName
        )
    }

    /**
     * Trích error message thân thiện từ HttpException.
     * Backend trả về JSON dạng { "message": "...", "status": 400 } khi lỗi.
     */
    private fun <T> Result<T>.recoverErrorMessage(): Result<T> = recoverCatching { e ->
        when (e) {
            is HttpException -> {
                val body = e.response()?.errorBody()?.string().orEmpty()
                val msg = extractMessage(body) ?: defaultMessageForCode(e.code())
                throw Exception(msg)
            }
            else -> throw Exception(e.message ?: "Lỗi không xác định. Vui lòng thử lại.")
        }
    }

    private fun extractMessage(body: String): String? {
        val match = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)
        return match?.groupValues?.getOrNull(1)
    }

    private fun defaultMessageForCode(code: Int): String = when (code) {
        400 -> "Dữ liệu không hợp lệ"
        401 -> "Email hoặc mật khẩu không đúng"
        403 -> "Bạn không có quyền thực hiện"
        404 -> "Không tìm thấy"
        500 -> "Lỗi máy chủ. Vui lòng thử lại sau"
        else -> "Lỗi kết nối (mã $code)"
    }
}