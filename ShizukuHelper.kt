package com.killer.systemservice.engine

import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import android.content.pm.IPackageManager
import android.os.Build

object ShizukuHelper {
    
    fun uninstallPackage(packageName: String): Boolean {
        return try {
            val pm = IPackageManager.Stub.asInterface(
                ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package"))
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                pm.deletePackageAsUser(packageName, null, 0, 0)
            } else {
                @Suppress("DEPRECATION")
                pm.deletePackageAsUser(packageName, null, 0, 0)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun forceStopPackage(packageName: String): Boolean {
        return try {
            val am = android.app.IActivityManager.Stub.asInterface(
                ShizukuBinderWrapper(SystemServiceHelper.getSystemService("activity"))
            )
            am.forceStopPackage(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun clearPackageData(packageName: String): Boolean {
        return try {
            val pm = IPackageManager.Stub.asInterface(
                ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package"))
            )
            // 清除应用数据
            pm.clearApplicationUserData(packageName, null, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun removeDeviceAdmin(packageName: String): Boolean {
        return try {
            // 通过 Shizuku 执行 shell 命令移除设备管理员
            val command = "dpm remove-active-admin $packageName/.DeviceAdminReceiver"
            executeShellCommand(command)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun executeShellCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            process.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            ""
        }
    }
}
