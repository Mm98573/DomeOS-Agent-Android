package com.domeos.agent

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * DomeOS 应用程序类
 */
@HiltAndroidApp
class DomeOSApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化工作
    }
}