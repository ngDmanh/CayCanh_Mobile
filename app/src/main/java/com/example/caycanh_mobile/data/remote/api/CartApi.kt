package com.example.caycanh_mobile.data.remote.api

import com.example.caycanh_mobile.data.remote.dto.cart.AddToCartRequest
import com.example.caycanh_mobile.data.remote.dto.cart.CartResponse
import com.example.caycanh_mobile.data.remote.dto.cart.UpdateCartItemRequest
import com.example.caycanh_mobile.data.remote.dto.common.MessageResponse
import retrofit2.http.*

interface CartApi {

    @GET("api/cart")
    suspend fun getCart(): CartResponse

    @POST("api/cart/items")
    suspend fun addToCart(@Body request: AddToCartRequest): CartResponse

    @PUT("api/cart/items/{id}")
    suspend fun updateCartItem(
        @Path("id") itemId: String,
        @Body request: UpdateCartItemRequest
    ): CartResponse

    @DELETE("api/cart/items/{id}")
    suspend fun removeCartItem(@Path("id") itemId: String): CartResponse

    @DELETE("api/cart")
    suspend fun clearCart(): MessageResponse
}