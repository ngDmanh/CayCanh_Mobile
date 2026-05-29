package com.example.caycanh_mobile.data.repository

import com.example.caycanh_mobile.data.remote.api.CategoryApi
import com.example.caycanh_mobile.data.remote.dto.admin.CategoryResponse
import com.example.caycanh_mobile.data.remote.dto.admin.CreateCategoryRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryApi: CategoryApi
) {
    suspend fun getCategories(): Result<List<CategoryResponse>> = runCatching {
        val response = categoryApi.getCategories()
        if (response.isSuccessful) response.body() ?: emptyList()
        else error("Lỗi tải danh mục: ${response.code()}")
    }

    suspend fun createCategory(name: String): Result<CategoryResponse> = runCatching {
        val response = categoryApi.createCategory(CreateCategoryRequest(name = name.trim()))
        if (response.isSuccessful) response.body() ?: error("Empty response")
        else error(extractError(response.errorBody()?.string()) ?: "Lỗi tạo danh mục: ${response.code()}")
    }

    private fun extractError(body: String?): String? {
        if (body == null) return null
        return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
    }

    suspend fun updateCategory(id: String, name: String): Result<CategoryResponse> = runCatching {
        val response = categoryApi.updateCategory(id, CreateCategoryRequest(name = name.trim()))
        if (response.isSuccessful) response.body() ?: error("Empty response")
        else error(extractError(response.errorBody()?.string()) ?: "Lỗi sửa: ${response.code()}")
    }

    suspend fun deleteCategory(id: String): Result<Unit> = runCatching {
        val response = categoryApi.deleteCategory(id)
        if (response.isSuccessful) Unit
        else error(extractError(response.errorBody()?.string()) ?: "Lỗi xóa: ${response.code()}")
    }
}