package com.sheep.treasuretool.data.api

import com.sheep.treasuretool.data.api.model.ApiResponse
import com.sheep.treasuretool.data.model.Contact
import retrofit2.http.GET
import retrofit2.http.Query

interface ContactApi {

    @GET("user/contact/byUserId")
    suspend fun getContactByUserId(@Query("userId") userId: String): ApiResponse<List<Contact>>

}