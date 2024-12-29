package com.sheep.treasuretool.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sheep.treasuretool.data.model.Contact
import com.sheep.treasuretool.data.model.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ContactStore(
    private val context: Context,
    private val userPreferences: UserPreferences,
) {
    companion object {
        private val Context.contactDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_contacts")
        private val gson = Gson()
    }

    /**
     * 获取联系人列表
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val contacts: Flow<List<Contact>> = userPreferences.currentUser.flatMapLatest { user ->
        context.contactDataStore.data.map { preferences ->
            val key = stringPreferencesKey("${user.id}_contacts")
            val contactsJson = preferences[key] ?: "[]"
            val contactsType = object : TypeToken<List<Contact>>() {}.type
            val contacts = gson.fromJson<List<Contact>>(contactsJson, contactsType)
            contacts
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 获取联系人列表
     */
    fun getContacts(user: User): Flow<List<Contact>> {
        return context.contactDataStore.data.map { preferences ->
            val key = stringPreferencesKey("${user.id}_contacts")
            val contactsJson = preferences[key] ?: "[]"
            val contactsType = object : TypeToken<List<Contact>>() {}.type
            val contacts = gson.fromJson<List<Contact>>(contactsJson, contactsType)
            contacts
        }
    }

    /**
     * 保存联系人列表
     */
    suspend fun saveContacts(contacts: List<Contact>) {
        val currentUser = userPreferences.currentUser.first()
        context.contactDataStore.edit { preferences ->
            val key = stringPreferencesKey("${currentUser.id}_contacts")
            preferences[key] = gson.toJson(contacts)
        }

    }

    /**
     * 清除缓存
     */
    suspend fun clearCache() {
        try {
            val currentUser = userPreferences.currentUser.first()
            val key = stringPreferencesKey("${currentUser.id}_contacts")
            context.contactDataStore.edit { preferences ->
                preferences.remove(key)
            }
            Log.d("ContactStore", "缓存清除成功")
        } catch (e: Exception) {
            Log.e("ContactStore", "清除缓存失败", e)
        }
    }
} 