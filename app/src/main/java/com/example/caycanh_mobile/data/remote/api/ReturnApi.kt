package com.example.caycanh_mobile.data.remote.api

import com.example.caycanh_mobile.data.remote.dto.plant.PageResponse
import com.example.caycanh_mobile.data.remote.dto.returns.ApproveReturnRequest
import com.example.caycanh_mobile.data.remote.dto.returns.CompleteReturnRequest
import com.example.caycanh_mobile.data.remote.dto.returns.CreateReturnRequest
import com.example.caycanh_mobile.data.remote.dto.returns.RejectReturnRequest
import com.example.caycanh_mobile.data.remote.dto.returns.ReturnRequestResponse
import retrofit2.http.*

interface ReturnApi {

    // ── Customer ──
    @POST("api/returns")
    suspend fun createReturn(@Body request: CreateReturnRequest): ReturnRequestResponse

    @GET("api/returns/my")
    suspend fun getMyReturns(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<ReturnRequestResponse>

    @GET("api/returns/my/{id}")
    suspend fun getMyReturnById(@Path("id") id: String): ReturnRequestResponse

    // ── Admin ──
    @GET("api/admin/returns")
    suspend fun getAllReturns(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<ReturnRequestResponse>

    @GET("api/admin/returns/{id}")
    suspend fun getReturnById(@Path("id") id: String): ReturnRequestResponse

    @PATCH("api/admin/returns/{id}/approve")
    suspend fun approveReturn(
        @Path("id") id: String,
        @Body request: ApproveReturnRequest
    ): ReturnRequestResponse

    @PATCH("api/admin/returns/{id}/reject")
    suspend fun rejectReturn(
        @Path("id") id: String,
        @Body request: RejectReturnRequest
    ): ReturnRequestResponse

    @PATCH("api/admin/returns/{id}/complete")
    suspend fun completeReturn(
        @Path("id") id: String,
        @Body request: CompleteReturnRequest
    ): ReturnRequestResponse
}
