package com.example.caycanh_mobile.data.remote.api

import com.example.caycanh_mobile.data.remote.dto.plant.PageResponse
import com.example.caycanh_mobile.data.remote.dto.rental.CustomerRentalResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RentalApi {

    @GET("api/rentals/my")
    suspend fun getMyRentals(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<PageResponse<CustomerRentalResponse>>

    @GET("api/rentals/my/{id}")
    suspend fun getMyRentalById(@Path("id") id: String): Response<CustomerRentalResponse>
}