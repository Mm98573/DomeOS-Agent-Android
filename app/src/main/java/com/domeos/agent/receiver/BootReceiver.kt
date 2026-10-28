package com.domeos.agent.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.domeos.agent.service.QuickAccessService
import com.domeos.agent.util.PreferencesUtil

/**
 * 开机自启动接收器
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_QUICKBOOT_POWERON) {
            // 检查是否启用了自动启动快速访问
            val autoStart = PreferencesUtil.getBoolean(context, "auto_start_quick_access", false)
            if (autoStart) {
                // 延迟启动，避免系统刚启动时资源紧张
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    QuickAccessService.start(context)
                }, 5000)
            }
        }
    }
}