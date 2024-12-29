package com.sheep.treasuretool.data.repository

import android.util.Log
import com.sheep.treasuretool.data.api.ApiService
import com.sheep.treasuretool.data.local.AvatarCache
import com.sheep.treasuretool.data.local.ContactStore
import com.sheep.treasuretool.data.local.UserPreferences
import com.sheep.treasuretool.service.ContactService
import kotlinx.coroutines.flow.first

class UserRepository(
    private val userPreferences: UserPreferences,
    private val avatarCache: AvatarCache,
    private val contactService: ContactService
) {

    /**
     * 更新用户信息
     */
    suspend fun updateUserInfo() {
        try {
            val currentUser = userPreferences.currentUser.first()
            Log.d("UserRepository", "查询用户信息")
            val response = ApiService.userApi.getUserInfo(currentUser.id)
            if (response.success && response.data != null) {
                val data = response.data
                // 1. 保存用户缓存信息
                userPreferences.saveLoginUser(data)
                // 2. 更新用户头像
                avatarCache.saveAvatar(data.id, data.avatar)
            } else {
                Log.w("UserRepository", "获取用户信息失败: ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "获取用户信息出错", e)
        }
    }

    /**
     * 更新用户联系人信息
     */
    suspend fun updateUserContact() {
        try {
            val currentUser = userPreferences.currentUser.first()
            Log.d("UserRepository", "查询并更新联系人缓存")
            val response = ApiService.contactApi.getContactByUserId(currentUser.id)
            if (response.success && response.data != null) {
                val data = response.data
                // 1. 保存联系人列表
                contactService.saveContactCache(data)
                // 2. 保存联系人头像缓存
                data.forEach {
                    avatarCache.saveAvatar(it.userId, it.avatar)
                }
                Log.d("UserRepository", "用户联系人缓存更新成功")
            } else {
                Log.w("UserRepository", "获取用户联系人信息失败: ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "获取用户联系人信息出错", e)
        }
    }

}