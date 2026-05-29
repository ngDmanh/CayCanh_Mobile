package com.example.caycanh_mobile.data.remote.api

import com.example.caycanh_mobile.data.remote.dto.admin.CategoryResponse
import com.example.caycanh_mobile.data.remote.dto.admin.CreateCategoryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CategoryApi {

    @GET("api/plants/categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>

    @POST("api/admin/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<CategoryResponse>

    @PUT("api/admin/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body request: CreateCategoryRequest
    ): Response<CategoryResponse>

    @DELETE("api/admin/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<Unit>
}