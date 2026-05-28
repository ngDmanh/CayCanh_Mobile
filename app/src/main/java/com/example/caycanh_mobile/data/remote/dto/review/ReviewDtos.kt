package com.example.caycanh_mobile.data.remote.dto.review

import kotlinx.serialization.Serializable

@Serializable
data class ReviewResponse(
    val id: String,
    val plantId: String,
    val plantName: String,
    val orderId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String? = null,
    val createdAt: String
)

@Serializable
data class CreateReviewRequest(
    val orderId: String,
    val plantId: String,
    val rating: Int,
    val comment: String? = null
)

@Serializable
data class UpdateReviewRequest(
    val rating: Int,
    val comment: String? = null
)

@Serializable
data class PlantReviewSummary(
    val averageRating: Double? = null,
    val totalReviews: Long = 0
)