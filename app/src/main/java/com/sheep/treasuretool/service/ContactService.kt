package com.sheep.treasuretool.service

import android.util.LruCache
import com.sheep.treasuretool.data.local.ContactStore
import com.sheep.treasuretool.data.model.Contact
import com.sheep.treasuretool.data.model.OnlineStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ContactService(
    private val contactStore: ContactStore,
) {
    companion object {
        private const val MAX_MEMORY_CACHE_SIZE = 20 // 最大缓存20个联系人
    }

    private val contactsUpdateFlow = MutableStateFlow<Contact?>(null)

    // 使用 LruCache 替代简单的 Map
    private val contactCache = object : LruCache<String, Contact>(MAX_MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, contact: Contact): Int {
            return 1 // 每个头像计为1个单位
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val contacts: Flow<Contact> = contactsUpdateFlow.flatMapLatest {
        flow {
            contactCache.snapshot().values.forEach {
                emit(it)
            }
        }
    }.flowOn(Dispatchers.IO)


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getContactStatus(contact: Contact) : Flow<Boolean> {
        val contactStateFlow = contact.statusFlow
        return contactStateFlow.flatMapLatest {
            flow {
                emit(it)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveContactCache(contacts: List<Contact>) {
        contacts.forEach { contact->
            contact.initStatusFlow()
            contactCache.put(contact.userId, contact)
            contactsUpdateFlow.value = contact
        }
        contactStore.saveContacts(contacts)
    }

    fun updateContactStatus(userId: String, status: OnlineStatus) {
        val contact = contactCache.get(userId)
        contact.status = status
        val contactStateFlow = contact.statusFlow
        contactStateFlow.value = contact.isOnline
    }


}