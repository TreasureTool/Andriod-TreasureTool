package com.sheep.treasuretool

import android.annotation.SuppressLint
import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.sheep.treasuretool.data.api.ApiService
import com.sheep.treasuretool.data.local.AvatarCache
import com.sheep.treasuretool.data.local.ContactStore
import com.sheep.treasuretool.data.local.MessageStore
import com.sheep.treasuretool.data.local.UserPreferences
import com.sheep.treasuretool.data.repository.AuthRepository
import com.sheep.treasuretool.data.repository.ChatRepository
import com.sheep.treasuretool.data.repository.UserRepository
import com.sheep.treasuretool.data.websocket.ChatWebSocket
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class App : Application(), ImageLoaderFactory {

    private val appModule = module {
        single { UserPreferences(androidContext()) }
        single { AvatarCache(androidContext()) }
        single { MessageStore(androidContext(), get()) }
        single { ContactStore(androidContext(), get()) }
        single { ApiService }
        single { AuthRepository(get(), get()) }
        single { ChatRepository(get(), get()) }
        single { UserRepository(get(), get(), get()) }
        single { 
            ChatWebSocket(
                baseUrl = "ws://192.168.31.162:8081/websocket",
                userPreferences = get(),
                messageStore = get(),
                contactStore = get()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        // 初始化 Koin
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                // 添加日志拦截器
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                // 信任所有证书（仅用于开发测试）
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })

                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())

                OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                    .hostnameVerifier { _, _ -> true }
                    .build()
            }
            .respectCacheHeaders(false)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .crossfade(true)
            .build()
    }
} 