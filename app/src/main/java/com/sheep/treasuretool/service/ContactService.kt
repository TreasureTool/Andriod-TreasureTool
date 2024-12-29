package com.sheep.treasuretool.service

import android.util.LruCache
import com.sheep.treasuretool.data.local.ContactStore
import com.sheep.treasuretool.data.local.UserPreferences
import com.sheep.treasuretool.data.model.Contact
import com.sheep.treasuretool.data.model.OnlineStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ContactService(
    private val contactStore: ContactStore,
    private val userPreferences: UserPreferences,
) {
    companion object {
        private const val MAX_MEMORY_CACHE_SIZE = 20 // 最大缓存20个联系人
    }

    // 使用 LruCache 替代简单的 Map
    private val contactCache = object : LruCache<String, Contact>(MAX_MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, contact: Contact): Int {
            return 1 // 每个头像计为1个单位
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val contacts: Flow<Contact> = userPreferences.currentUser.flatMapLatest {
        flow {
            contactCache.snapshot().values.forEach {
                emit(it)
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun saveContactCache(contacts: List<Contact>) {
        contacts.forEach { contact->
            contactCache.put(contact.userId, contact)
        }
        contactStore.saveContacts(contacts)
    }

    fun updateContactStatus(userId:String, status: OnlineStatus) {
        val get = contactCache.get(userId)
        get.status = status
    }


}