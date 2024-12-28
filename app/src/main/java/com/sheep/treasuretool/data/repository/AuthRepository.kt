package com.sheep.treasuretool.data.repository

import android.util.Log
import com.sheep.treasuretool.data.api.ApiService
import com.sheep.treasuretool.data.local.UserPreferences
import com.sheep.treasuretool.data.api.model.LoginRequest
import com.sheep.treasuretool.data.api.model.ApiResponse
import com.sheep.treasuretool.data.model.entity.User
import kotlinx.coroutines.*

class AuthRepository(
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository,
) {

    suspend fun login(username: String, password: String): Result<ApiResponse<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiService.authApi.login(
                    LoginRequest(
                        username = username,
                        password = password
                    )
                )
                if (response.success && response.data != null) {
                    // 1. 保存用户缓存信息
                    userPreferences.saveLoginUser(response.data)
                    // 2. 更新联系人缓存
                    userRepository.updateUserContact()
                }
                Result.success(response)
            } catch (e: Exception) {
                Log.e("AuthRepository", "登录失败", e)
                Result.failure(e)
            }
        }
    }

} 