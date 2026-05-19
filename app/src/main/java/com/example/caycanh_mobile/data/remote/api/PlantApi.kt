package com.example.caycanh_mobile.data.remote.api

import com.example.caycanh_mobile.data.remote.dto.plant.CategoryResponse
import com.example.caycanh_mobile.data.remote.dto.plant.PageResponse
import com.example.caycanh_mobile.data.remote.dto.plant.PlantResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PlantApi {

    /**
     * Danh sách cây — có phân trang + filter.
     * Backend hỗ trợ: status, categoryId, listingType, page, size.
     */
    @GET("api/plants")
    suspend fun getPlants(
        @Query("status") status: String? = "active",
        @Query("categoryId") categoryId: String? = null,
        @Query("listingType") listingType: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<PlantResponse>

    /** Chi tiết 1 cây */
    @GET("api/plants/{id}")
    suspend fun getPlantById(@Path("id") id: String): PlantResponse

    /** Danh sách danh mục — trả về array, không phân trang */
    @GET("api/plants/categories")
    suspend fun getCategories(): List<CategoryResponse>
}