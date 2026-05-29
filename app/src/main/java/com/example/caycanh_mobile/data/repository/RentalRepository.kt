package com.example.caycanh_mobile.data.repository

import com.example.caycanh_mobile.data.remote.api.RentalApi
import com.example.caycanh_mobile.data.remote.dto.rental.CustomerRentalResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RentalRepository @Inject constructor(
    private val rentalApi: RentalApi
) {
    suspend fun getMyRentals(): Result<List<CustomerRentalResponse>> = runCatching {
        val response = rentalApi.getMyRentals(page = 0, size = 100)
        if (response.isSuccessful) response.body()?.content ?: emptyList()
        else error("Lỗi tải rental: ${response.code()}")
    }

    suspend fun getMyRentalById(id: String): Result<CustomerRentalResponse> = runCatching {
        val response = rentalApi.getMyRentalById(id)
        if (response.isSuccessful) response.body() ?: error("Empty response")
        else error("Lỗi: ${response.code()}")
    }
}