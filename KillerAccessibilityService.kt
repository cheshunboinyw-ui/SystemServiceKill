package com.killer.systemservice.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class KillerAccessibilityService : AccessibilityService() {
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 当需要自动化卸载时，监听系统卸载界面的事件
        event?.let {
            if (it.packageName == "com.android.packageinstaller" ||
                it.packageName == "com.google.android.packageinstaller" ||
                it.packageName == "com.android.settings") {
                
                // 自动点击"确定"/"卸载"按钮
                // 这里需要根据具体的系统 UI 来实现
            }
        }
    }
    
    override fun onInterrupt() {
        // 服务中断时的处理
    }
    
    companion object {
        fun isEnabled(context: Context): Boolean {
            val service = "${context.packageName}/${KillerAccessibilityService::class.java.name}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            
            return enabledServices.contains(service)
        }
    }
}
