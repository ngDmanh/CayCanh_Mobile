package com.example.caycanh_mobile.data.remote.api

import com.example.caycanh_mobile.data.remote.dto.plant.PageResponse
import com.example.caycanh_mobile.data.remote.dto.review.*
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {

    @POST("api/reviews")
    suspend fun createReview(
        @Body request: CreateReviewRequest
    ): Response<ReviewResponse>

    @GET("api/reviews/my")
    suspend fun getMyReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<ReviewResponse>>

    @GET("api/plants/{plantId}/reviews")
    suspend fun getPlantReviews(
        @Path("plantId") plantId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<ReviewResponse>>

    @GET("api/plants/{plantId}/reviews/summary")
    suspend fun getPlantReviewSummary(
        @Path("plantId") plantId: String
    ): Response<PlantReviewSummary>

    @PUT("api/reviews/{id}")
    suspend fun updateReview(
        @Path("id") id: String,
        @Body request: UpdateReviewRequest
    ): Response<ReviewResponse>
}