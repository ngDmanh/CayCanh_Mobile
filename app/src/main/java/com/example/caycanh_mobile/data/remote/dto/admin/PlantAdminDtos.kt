package com.example.caycanh_mobile.data.remote.dto.admin

import kotlinx.serialization.Serializable

@Serializable
data class PlantImagePayload(
    val imageUrl: String,
    val isPrimary: Boolean = false,
    val sortOrder: Int = 0
)

@Serializable
data class CreatePlantRequest(
    val categoryId: String,
    val name: String,
    val description: String? = null,
    val listingType: String,        // sale | rent | both
    val priceSale: Long? = null,
    val pricePerDay: Long? = null,
    val pricePerWeek: Long? = null,
    val pricePerMonth: Long? = null,
    val stockQuantity: Int = 0,
    val rentAvailableQty: Int = 0,
    val images: List<PlantImagePayload> = emptyList()
)

// Update dùng cùng cấu trúc
@Serializable
data class UpdatePlantRequest(
    val categoryId: String,
    val name: String,
    val description: String? = null,
    val listingType: String,
    val priceSale: Long? = null,
    val pricePerDay: Long? = null,
    val pricePerWeek: Long? = null,
    val pricePerMonth: Long? = null,
    val stockQuantity: Int = 0,
    val rentAvailableQty: Int = 0,
    val images: List<PlantImagePayload> = emptyList()
)

// Response upload ảnh
@Serializable
data class UploadImageResponse(
    val imageUrl: String
)