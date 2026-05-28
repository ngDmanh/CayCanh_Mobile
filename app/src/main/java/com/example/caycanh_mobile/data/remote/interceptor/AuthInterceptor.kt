package com.example.caycanh_mobile.data.remote.interceptor

import com.example.caycanh_mobile.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Tự động gắn header Authorization: Bearer <token> vào mọi request,
 * trừ các endpoint auth (không cần token).
 *
 * Khi backend trả 401/403 → token đã hết hạn hoặc bị thu hồi.
 * Interceptor tự động xóa local token để Splash lần sau bắt buộc Login.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    /** Các path không cần gắn token */
    private val noAuthPaths = listOf(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/verify-email",
        "/api/auth/resend-otp",
        "/api/auth/forgot-password",
        "/api/auth/verify-reset-otp",
        "/api/auth/reset-password"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        if (noAuthPaths.any { path.startsWith(it) }) {
            return chain.proceed(request)
        }

        val token = runBlocking { tokenManager.getAccessTokenOnce() }

        val authenticatedRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        val response = chain.proceed(authenticatedRequest)

        // Token expired/invalid → clear local để Splash sau bắt Login
        if (response.code == 401 || response.code == 403) {
            runBlocking { tokenManager.clearAuth() }
        }

        return response
    }
}