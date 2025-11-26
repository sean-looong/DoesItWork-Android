package com.seanlooong.doesitwork

import LoggerConfig
import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 初始化日志系统
        LoggerFactory.initialize(
            context = this,
            config = LoggerConfig(
                minLogLevel = LogLevel.DEBUG,
                enableConsoleLogging = true,
                enableFileLogging = true,
                maxFileSize = 10 * 1024 * 1024,
                maxFiles = 10
            )
        )

        // 设置全局异常处理器
        setupGlobalExceptionHandler()
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // 记录崩溃日志
            LoggerFactory.getLogger().e(
                "Crash",
                "App crashed in thread: ${thread.name}",
                throwable
            )

            // 调用原有处理器
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        LoggerFactory.release()
    }
}