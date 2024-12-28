package com.sheep.treasuretool.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sheep.treasuretool.data.model.entity.User
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val USER_DATA = stringPreferencesKey("user_data")  // 用户数据
        private val LOGIN_TIME = longPreferencesKey("login_time")  // 登录时间
        // 登录有效期为7天
        private const val LOGIN_VALIDITY_DAYS = 7L
    }

    private val gson = Gson()

    /**
     * 保存用户登录信息
     */
    suspend fun saveLoginUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_DATA] = gson.toJson(user)
            preferences[LOGIN_TIME] = System.currentTimeMillis()
        }
        Log.d("UserPreferences", "保存用户登录信息, 当前登录用户：${user.nickname}")
    }

    /**
     * 检查用户是否已登录且未过期
     */
    val isUserLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val loginTime = preferences[LOGIN_TIME] ?: 0L
        val expiryTime = loginTime + TimeUnit.DAYS.toMillis(LOGIN_VALIDITY_DAYS)
        try {
            val userData = preferences[USER_DATA]
            if (userData != null) {
                val user = gson.fromJson(userData, User::class.java)
                // 检查登录是否过期且 userId 不为空
                System.currentTimeMillis() < expiryTime && user.id.isNotBlank()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取当前登录用户信息
     */
    val currentUser: Flow<User> = context.dataStore.data.map { preferences ->
        preferences[USER_DATA].let { userData ->
            gson.fromJson(userData, User::class.java)
        }
    }

    /**
     * 清除用户登录信息
     */
    suspend fun clearUserLogin() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
} 