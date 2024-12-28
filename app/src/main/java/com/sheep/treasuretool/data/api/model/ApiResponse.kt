package com.sheep.treasuretool.data.api.model

data class ApiResponse<T>(
    val code: Int,
    val message: String?,
    val data: T?
) {
    // 根据 code 自动计算 success
    val success: Boolean
        get() = code == 0
}
