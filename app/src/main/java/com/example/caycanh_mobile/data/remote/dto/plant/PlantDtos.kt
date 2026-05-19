package com.example.caycanh_mobile.data.remote.dto.plant

import kotlinx.serialization.Serializable

/**
 * Plant response từ backend.
 * Cấu trúc khớp với GET /api/plants
 */
@Serializable
data class PlantResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val listingType: String = "sale",        // "sale" | "rent" | "both"


    val priceSale: Long? = null,
    val pricePerDay: Long? = null,
    val pricePerWeek: Long? = null,
    val pricePerMonth: Long? = null,

    // Tồn kho
    val stockQuantity: Int = 0,
    val rentAvailableQty: Int = 0,

    // Category
    val categoryId: String? = null,
    val categoryName: String? = null,

    // Images — array object PlantImage
    val images: List<PlantImageResponse> = emptyList(),

    // Trạng thái
    val status: String = "active",
    val createdAt: String? = null
) {
    /** Lấy URL ảnh chính — ảnh đầu tiên hoặc ảnh có isPrimary=true */
    val primaryImageUrl: String?
        get() = images.firstOrNull { it.isPrimary }?.imageUrl
            ?: images.firstOrNull()?.imageUrl

    /** Lấy danh sách URL — cho gallery */
    val imageUrls: List<String>
        get() = images.sortedBy { it.sortOrder }.map { it.imageUrl }

    /** Kiểm tra cây có bán không */
    val isForSale: Boolean
        get() = listingType == "sale" || listingType == "both"

    /** Kiểm tra cây có cho thuê không */
    val isForRent: Boolean
        get() = listingType == "rent" || listingType == "both"
}

@Serializable
data class PlantImageResponse(
    val id: String,
    val imageUrl: String,
    val isPrimary: Boolean = false,
    val sortOrder: Int = 0
)

@Serializable
data class CategoryResponse(
    val id: String,
    val name: String,
    val slug: String? = null,
    val description: String? = null
)

/**
 * Wrapper cho response phân trang của Spring Boot.
 */
@Serializable
data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,       // page hiện tại
    val size: Int = 20,
    val first: Boolean = true,
    val last: Boolean = true,
    val empty: Boolean = false,
    val numberOfElements: Int = 0
)