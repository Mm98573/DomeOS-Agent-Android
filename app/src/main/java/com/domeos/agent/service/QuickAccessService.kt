package com.domeos.agent.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.domeos.agent.R
import com.domeos.agent.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 快速访问悬浮窗服务
 * 类似 WinUI 版本的系统托盘/全局热键功能
 */
class QuickAccessService : Service() {

    private var floatingView: View? = null
    private var windowManager: WindowManager? = null
    private var initialX = 0f
    private var initialY = 0f
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "quick_access_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        showFloatingView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeFloatingView()
        super.onDestroy()
    }

    /** 创建通知通道 */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "快速访问",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "DomeOS 快速访问悬浮窗"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /** 创建前台服务通知 */
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "com.domeos.agent.QUICK_ACCESS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("DomeOS Agent")
            .setContentText("点击打开快速访问")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()
    }

    /** 显示悬浮视图 */
    private fun showFloatingView() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.START or Gravity.TOP
            x = 0
            y = 200 // 默认位置
        }

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_quick_access, null)
        windowManager?.addView(floatingView, params)

        setupFloatingViewTouch()
        setupFloatingViewClick()
    }

    /** 设置悬浮视图触摸拖拽 */
    private fun setupFloatingViewTouch() {
        floatingView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x.toFloat()
                    initialY = params.y.toFloat()
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = (initialX + event.rawX - initialTouchX).toInt()
                    params.y = (initialY + event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(v, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // 吸附到边缘
                    snapToEdge()
                    true
                }
            }
        }
    }

    /** 吸附到屏幕边缘 */
    private fun snapToEdge() {
        val screenWidth = resources.displayMetrics.widthPixels
        val viewWidth = floatingView?.width ?: 0
        val margin = 20

        CoroutineScope(Dispatchers.Main).launch {
            var targetX = params.x
            if (params.x < screenWidth / 2) {
                targetX = margin
            } else {
                targetX = screenWidth - viewWidth - margin
            }

            // 动画吸附（简化版）
            params.x = targetX
            floatingView?.let { windowManager?.updateViewLayout(it, params) }
        }
    }

    /** 设置悬浮视图点击 */
    private fun setupFloatingViewClick() {
        floatingView?.findViewById<ImageView>(R.id.iv_floating_icon)?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                action = "com.domeos.agent.QUICK_ACCESS"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }

        floatingView?.findViewById<ImageView>(R.id.iv_floating_close)?.setOnClickListener {
            stopSelf()
        }
    }

    /** 移除悬浮视图 */
    private fun removeFloatingView() {
        floatingView?.let { view ->
            windowManager?.removeView(view)
            floatingView = null
        }
    }

    companion object {
        /** 启动快速访问服务 */
        fun start(context: Context) {
            val intent = Intent(context, QuickAccessService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** 停止快速访问服务 */
        fun stop(context: Context) {
            val intent = Intent(context, QuickAccessService::class.java)
            context.stopService(intent)
        }
    }
}