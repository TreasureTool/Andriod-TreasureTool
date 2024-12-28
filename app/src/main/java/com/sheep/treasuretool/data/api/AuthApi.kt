package com.sheep.treasuretool.data.api

import com.sheep.treasuretool.data.api.model.LoginRequest
import com.sheep.treasuretool.data.api.model.ApiResponse
import com.sheep.treasuretool.data.model.entity.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<User>
} 