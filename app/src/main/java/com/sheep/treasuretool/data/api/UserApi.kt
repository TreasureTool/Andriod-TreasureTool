package com.sheep.treasuretool.data.api

import com.sheep.treasuretool.data.api.model.ApiResponse
import com.sheep.treasuretool.data.model.entity.User
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {

    @GET("user/info")
    suspend fun getUserInfo(@Query("userId") userId: String): ApiResponse<User>

}