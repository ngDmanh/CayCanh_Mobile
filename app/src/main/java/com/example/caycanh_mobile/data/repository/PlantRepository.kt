package com.example.caycanh_mobile.data.repository

import com.example.caycanh_mobile.data.remote.api.PlantApi
import com.example.caycanh_mobile.data.remote.dto.plant.CategoryResponse
import com.example.caycanh_mobile.data.remote.dto.plant.PageResponse
import com.example.caycanh_mobile.data.remote.dto.plant.PlantResponse
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantRepository @Inject constructor(
    private val plantApi: PlantApi
) {

    /** Danh sách cây với filter */
    suspend fun getPlants(
        categoryId: String? = null,
        listingType: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<PageResponse<PlantResponse>> = runCatching {
        plantApi.getPlants(
            status = "active",
            categoryId = categoryId,
            listingType = listingType,
            page = page,
            size = size
        )
    }.recoverErrorMessage()

    /** Chi tiết 1 cây */
    suspend fun getPlantById(id: String): Result<PlantResponse> = runCatching {
        plantApi.getPlantById(id)
    }.recoverErrorMessage()

    /** Danh sách category */
    suspend fun getCategories(): Result<List<CategoryResponse>> = runCatching {
        plantApi.getCategories()
    }.recoverErrorMessage()

    // ── Error helpers ───────────────────────────────────────────

    private fun <T> Result<T>.recoverErrorMessage(): Result<T> = recoverCatching { e ->
        when (e) {
            is HttpException -> {
                val msg = when (e.code()) {
                    404 -> "Không tìm thấy cây"
                    500 -> "Lỗi máy chủ. Vui lòng thử lại sau"
                    else -> "Lỗi tải dữ liệu (mã ${e.code()})"
                }
                throw Exception(msg)
            }
            else -> throw Exception(e.message ?: "Không thể kết nối đến server")
        }
    }
}