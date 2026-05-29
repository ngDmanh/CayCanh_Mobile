package com.example.caycanh_mobile.data.remote.dto.admin

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    val id: String,
    val name: String,
    val slug: String? = null,
    val description: String? = null
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val slug: String? = null,
    val description: String? = null
)