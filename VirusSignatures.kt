package com.killer.systemservice.engine

import android.Manifest
import android.content.Context
import android.content.pm.PackageInfo
import java.io.File

class VirusSignatures {
    
    // System Service 病毒已知包名特征
    private val knownVirusPackages = setOf(
        "com.system.service",
        "com.android.systemservice",
        "com.system.update",
        "com.android.systemupdate",
        "com.system.core",
        "com.android.systemcore",
        "com.mobile.service",
        "com.phone.service"
    )
    
    // 可疑应用名称
    private val suspiciousNames = setOf(
        "System Service",
        "System Update",
        "系统服务",
        "系统更新",
        "系统组件",
        "System Core",
        "Mobile Service",
        "Phone Service"
    )
    
    // System Service 典型的高危权限组合
    private val dangerousPermissions = setOf(
        Manifest.permission.RECORD_AUDIO,           // 录音
        Manifest.permission.CAMERA,                 // 摄像头
        Manifest.permission.READ_SMS,               // 读短信
        Manifest.permission.RECEIVE_SMS,            // 接收短信
        Manifest.permission.READ_CONTACTS,          // 读通讯录
        Manifest.permission.READ_CALL_LOG,          // 读通话记录
        Manifest.permission.ACCESS_FINE_LOCATION,   // 精确位置
        Manifest.permission.SYSTEM_ALERT_WINDOW,    // 悬浮窗
        Manifest.permission.BIND_ACCESSIBILITY_SERVICE, // 无障碍
        "android.permission.BIND_DEVICE_ADMIN",     // 设备管理员
        Manifest.permission.REQUEST_INSTALL_PACKAGES, // 安装应用
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    
    // 可疑组件特征
    private val suspiciousComponents = listOf(
        "BootReceiver",           // 开机启动
        "ContentProvider",        // 内容提供者唤醒
        "AccessibilityService",   // 无障碍服务
        "DeviceAdminReceiver",    // 设备管理员
        "NotificationListener"    // 通知监听
    )
    
    fun isVirusPackageName(packageName: String): Boolean {
        // 精确匹配已知病毒包名
        if (knownVirusPackages.contains(packageName)) {
            return true
        }
        
        // 模糊匹配可疑特征
        val lower = packageName.lowercase()
        return (lower.contains("system") && lower.contains("service")) ||
               (lower.contains("system") && lower.contains("update")) ||
               (lower.contains("system") && lower.contains("core"))
    }
    
    fun isVirusAppName(appName: String): Boolean {
        return suspiciousNames.any { appName.contains(it, ignoreCase = true) }
    }
    
    fun getDangerousPermissions(requestedPerms: List<String>): List<String> {
        return requestedPerms.filter { dangerousPermissions.contains(it) }
    }
    
    fun checkComponents(pkg: PackageInfo): List<String> {
        val found = mutableListOf<String>()
        
        // 检查 Receiver
        pkg.receivers?.forEach { receiver ->
            val name = receiver.name
            if (suspiciousComponents.any { name.contains(it) }) {
                found.add("可疑组件: ${receiver.name}")
            }
        }
        
        // 检查 Service
        pkg.services?.forEach { service ->
            val name = service.name
            if (suspiciousComponents.any { name.contains(it) }) {
                found.add("可疑服务: ${service.name}")
            }
        }
        
        // 检查 Provider
        pkg.providers?.forEach { provider ->
            found.add("ContentProvider 唤醒")
        }
        
        return found
    }
    
    fun deepScan(pkg: PackageInfo, context: Context): List<String> {
        val signatures = mutableListOf<String>()
        
        // 1. 检查 native library（一些病毒会携带 so 文件）
        val nativeLibDir = pkg.applicationInfo.nativeLibraryDir
        if (nativeLibDir != null) {
            val libDir = File(nativeLibDir)
            if (libDir.exists() && libDir.listFiles()?.isNotEmpty() == true) {
                signatures.add("包含 Native 库")
            }
        }
        
        // 2. 检查 dex 数量（多 dex 可能用于混淆）
        val apkPath = pkg.applicationInfo.sourceDir
        // 简单检查：这里可以扩展为解析 APK
        
        // 3. 检查安装来源
        val installer = try {
            context.packageManager.getInstallerPackageName(pkg.packageName)
        } catch (e: Exception) {
            null
        }
        if (installer == null || installer.isEmpty()) {
            signatures.add("未知安装来源")
        }
        
        // 4. 检查签名（System Service 病毒通常是自签名）
        // 这里可以添加签名校验逻辑
        
        return signatures
    }
}
