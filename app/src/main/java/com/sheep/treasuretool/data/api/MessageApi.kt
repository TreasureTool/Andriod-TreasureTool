package com.sheep.treasuretool.data.api

import com.sheep.treasuretool.data.api.model.ApiResponse
import com.sheep.treasuretool.data.model.entity.ChatMessage
import retrofit2.http.GET
import retrofit2.http.Query

interface MessageApi {
    @GET("message/history")
    suspend fun getMessageHistory(
        @Query("userId") userId: String,
        @Query("contactId") contactId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ApiResponse<List<ChatMessage>>
} 