package com.sheep.treasuretool.data.websocket

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sheep.treasuretool.MainActivity
import com.sheep.treasuretool.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class WebSocketService : Service() {
    private val treasureWebSocket: TreasureWebSocket by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationManager by lazy { 
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager 
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // 立即启动前台服务
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        serviceScope.launch {
            treasureWebSocket.connect()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        treasureWebSocket.disconnect()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WebSocket Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "保持WebSocket连接的通知"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TreasureTool")
            .setContentText("正在保持连接...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }

        return builder.build()
    }

    companion object {
        private const val CHANNEL_ID = "websocket_service"
        private const val NOTIFICATION_ID = 1
    }
} 