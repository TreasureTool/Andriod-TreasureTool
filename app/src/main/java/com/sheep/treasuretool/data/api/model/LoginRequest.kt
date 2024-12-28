package com.sheep.treasuretool.data.api.model

data class LoginRequest(
    val username: String,
    val password: String,
    val deviceId: String? = null
) 