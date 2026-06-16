package com.example.caycanh_mobile.data.repository

import com.example.caycanh_mobile.data.remote.api.ReturnApi
import com.example.caycanh_mobile.data.remote.dto.plant.PageResponse
import com.example.caycanh_mobile.data.remote.dto.returns.ApproveReturnRequest
import com.example.caycanh_mobile.data.remote.dto.returns.CompleteReturnRequest
import com.example.caycanh_mobile.data.remote.dto.returns.CreateReturnRequest
import com.example.caycanh_mobile.data.remote.dto.returns.RejectReturnRequest
import com.example.caycanh_mobile.data.remote.dto.returns.ReturnRequestResponse
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReturnRepository @Inject constructor(
    private val returnApi: ReturnApi
) {

    // ── Customer ──
    suspend fun createReturn(
        orderItemId: String,
        quantity: Int,
        reason: String
    ): Result<ReturnRequestResponse> = runCatching {
        returnApi.createReturn(
            CreateReturnRequest(
                orderItemId = orderItemId,
                quantity = quantity,
                reason = reason.trim()
            )
        )
    }.recoverErrorMessage()

    suspend fun getMyReturns(
        page: Int = 0,
        size: Int = 20
    ): Result<PageResponse<ReturnRequestResponse>> = runCatching {
        returnApi.getMyReturns(page, size)
    }.recoverErrorMessage()

    suspend fun getMyReturnById(id: String): Result<ReturnRequestResponse> = runCatching {
        returnApi.getMyReturnById(id)
    }.recoverErrorMessage()

    // ── Admin ──
    suspend fun getAllReturns(
        status: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<PageResponse<ReturnRequestResponse>> = runCatching {
        returnApi.getAllReturns(status, page, size)
    }.recoverErrorMessage()

    suspend fun getReturnById(id: String): Result<ReturnRequestResponse> = runCatching {
        returnApi.getReturnById(id)
    }.recoverErrorMessage()

    suspend fun approveReturn(
        id: String,
        refundAmount: Long,
        adminNote: String?
    ): Result<ReturnRequestResponse> = runCatching {
        returnApi.approveReturn(id, ApproveReturnRequest(refundAmount, adminNote?.trim()))
    }.recoverErrorMessage()

    suspend fun rejectReturn(
        id: String,
        adminNote: String
    ): Result<ReturnRequestResponse> = runCatching {
        returnApi.rejectReturn(id, RejectReturnRequest(adminNote.trim()))
    }.recoverErrorMessage()

    suspend fun completeReturn(
        id: String,
        restock: Boolean,
        adminNote: String?
    ): Result<ReturnRequestResponse> = runCatching {
        returnApi.completeReturn(id, CompleteReturnRequest(restock, adminNote?.trim()))
    }.recoverErrorMessage()

    private fun <T> Result<T>.recoverErrorMessage(): Result<T> = recoverCatching { e ->
        when (e) {
            is HttpException -> {
                val body = e.response()?.errorBody()?.string().orEmpty()
                val msg = extractMessage(body) ?: when (e.code()) {
                    400 -> "Dữ liệu không hợp lệ"
                    401 -> "Vui lòng đăng nhập lại"
                    404 -> "Không tìm thấy yêu cầu trả hàng"
                    else -> "Lỗi (mã ${e.code()})"
                }
                throw Exception(msg)
            }
            else -> throw Exception(e.message ?: "Không thể kết nối")
        }
    }

    private fun extractMessage(body: String): String? {
        val match = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)
        return match?.groupValues?.getOrNull(1)
    }
}
