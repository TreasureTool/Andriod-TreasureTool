package com.sheep.treasuretool.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

private val Context.avatarDataStore: DataStore<Preferences> by preferencesDataStore(name = "avatar_cache")

class AvatarCache(private val context: Context) {
    companion object {
        private val AVATAR_MAP = stringPreferencesKey("avatar_map")
        private const val AVATAR_CACHE_DIR = "avatar_cache"
        private const val MAX_MEMORY_CACHE_SIZE = 20 // 最大缓存20个头像
    }

    private val gson = Gson()
    private val cacheDir = File(context.cacheDir, AVATAR_CACHE_DIR).apply { 
        if (!exists()) mkdirs() 
    }
    
    // 使用 LruCache 替代简单的 Map
    private val bitmapCache = object : LruCache<String, Bitmap>(MAX_MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return 1 // 每个头像计为1个单位
        }
    }

    /**
     * 获取头像缓存文件
     */
    private fun getCacheFile(userId: String, url: String): File {
        val split = url.split("/")
        return File(cacheDir, "${userId}_${split.last()}")
    }

    /**
     * 保存头像到本地缓存
     */
    suspend fun saveAvatar(userId: String, url: String) = withContext(Dispatchers.IO) {
        try {
            val cacheFile = getCacheFile(userId, url)
            Log.i("AvatarCache", "保存头像到本地缓存 ${cacheFile.absolutePath}")

            if (cacheFile.exists()) {
                // 更新内存缓存
                loadBitMapCache(userId, cacheFile)
                return@withContext
            }

            Log.i("AvatarCache", "下载头像${url}")
            URL(url).openStream().use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }

            loadBitMapCache(userId, cacheFile)

            // 保存映射关系
            context.avatarDataStore.edit { preferences ->
                val currentMap = getAvatarMapFromPreferences(preferences)
                val updatedMap = currentMap.toMutableMap().apply {
                    put(userId, cacheFile.absolutePath)
                }
                preferences[AVATAR_MAP] = gson.toJson(updatedMap)
            }
        } catch (e: Exception) {
            Log.e("AvatarCache", "保存头像失败: ${e.message}")
        }
    }

    /**
     * 加载用户头像
     */
    suspend fun preloadAvatars() = withContext(Dispatchers.IO) {
        context.avatarDataStore.data.first().let { preferences->
            preferences[AVATAR_MAP].let { json ->
                val type = object : TypeToken<Map<String, String>>() {}.type
                val fromJson = gson.fromJson<Map<String, String>>(json, type)
                fromJson?.map {
                    Log.i("AvatarCache", "预加载用户[${it.key}]头像 -> 地址 [${it.value}]")
                    loadBitMapCache(it.key, File(it.value))
                }
            }
        }
    }

    private fun loadBitMapCache (userId: String, file: File) {
        try {
            // 确保内存缓存已加载
            BitmapFactory.decodeFile(file.absolutePath)?.let { bitmap ->
                bitmapCache.put(userId, bitmap)
            }
        } catch (e: Exception) {
            Log.e("AvatarCache", "内存缓存加载失败: ${e.message}")
        }
    }


    private fun getAvatarMapFromPreferences(preferences: Preferences): Map<String, String> {
        return preferences[AVATAR_MAP]?.let { json ->
            try {
                val type = object : TypeToken<Map<String, String>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyMap()
            }
        } ?: emptyMap()
    }

    /**
     * 清除头像缓存
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        try {
            // 清除文件
            cacheDir.listFiles()?.forEach { it.delete() }
            // 清除映射关系
            context.avatarDataStore.edit { preferences ->
                preferences.clear()
            }
            bitmapCache.evictAll()
        } catch (e: Exception) {
            Log.e("AvatarCache", "清除缓存失败: ${e.message}")
        }
    }


    /**
     * 获取内存中的位图
     */
    fun getBitmapSync(userId: String): Bitmap? {
        val bitmap = bitmapCache.get(userId)
        return bitmap
    }
} 