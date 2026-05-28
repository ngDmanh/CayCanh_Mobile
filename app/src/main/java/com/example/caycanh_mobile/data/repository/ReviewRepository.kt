package com.example.caycanh_mobile.data.repository

import com.example.caycanh_mobile.data.remote.api.ReviewApi
import com.example.caycanh_mobile.data.remote.dto.review.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val reviewApi: ReviewApi
) {

    suspend fun createReview(
        orderId: String,
        plantId: String,
        rating: Int,
        comment: String?
    ): Result<ReviewResponse> = runCatching {
        val response = reviewApi.createReview(
            CreateReviewRequest(
                orderId = orderId,
                plantId = plantId,
                rating = rating,
                comment = comment?.takeIf { it.isNotBlank() }
            )
        )
        if (response.isSuccessful) {
            response.body() ?: error("Empty response")
        } else {
            val errorBody = response.errorBody()?.string().orEmpty()
            val msg = extractErrorMessage(errorBody) ?: defaultMessage(response.code())
            error(msg)
        }
    }

    suspend fun updateReview(
        reviewId: String,
        rating: Int,
        comment: String?
    ): Result<ReviewResponse> = runCatching {
        val response = reviewApi.updateReview(
            id = reviewId,
            request = UpdateReviewRequest(
                rating = rating,
                comment = comment?.takeIf { it.isNotBlank() }
            )
        )
        if (response.isSuccessful) {
            response.body() ?: error("Empty response")
        } else {
            val errorBody = response.errorBody()?.string().orEmpty()
            val msg = extractErrorMessage(errorBody) ?: defaultMessage(response.code())
            error(msg)
        }
    }

    suspend fun getMyReviews(page: Int = 0, size: Int = 50): Result<List<ReviewResponse>> = runCatching {
        val response = reviewApi.getMyReviews(page, size)
        if (response.isSuccessful) {
            response.body()?.content ?: emptyList()
        } else {
            error("Lỗi tải đánh giá: ${response.code()}")
        }
    }

    suspend fun getPlantReviewSummary(plantId: String): Result<PlantReviewSummary> = runCatching {
        val response = reviewApi.getPlantReviewSummary(plantId)
        if (response.isSuccessful) {
            response.body() ?: PlantReviewSummary()
        } else {
            error("Lỗi: ${response.code()}")
        }
    }

    suspend fun getPlantReviews(plantId: String, page: Int = 0, size: Int = 10): Result<List<ReviewResponse>> = runCatching {
        val response = reviewApi.getPlantReviews(plantId, page, size)
        if (response.isSuccessful) {
            response.body()?.content ?: emptyList()
        } else {
            error("Lỗi: ${response.code()}")
        }
    }

    private fun extractErrorMessage(body: String): String? {
        return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
    }

    private fun defaultMessage(code: Int): String = when (code) {
        400 -> "Dữ liệu không hợp lệ"
        401, 403 -> "Bạn không có quyền"
        404 -> "Không tìm thấy"
        else -> "Lỗi $code"
    }
}