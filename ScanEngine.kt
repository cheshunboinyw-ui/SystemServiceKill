package com.killer.systemservice.engine

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

enum class ScanMode {
    NORMAL,    // 普通档
    ADVANCED,  // 高级档
    ULTIMATE,  // 终极档
    DEATH      // 死亡档
}

data class ThreatInfo(
    val packageName: String,
    val appName: String,
    val reason: String,
    val threatLevel: Int, // 1-5
    val signatures: List<String>
)

data class CleanResult(
    val success: Int,
    val failed: Int,
    val details: List<String>
)

class ScanEngine(private val context: Context) {
    
    // System Service 病毒特征库
    private val virusSignatures = VirusSignatures()
    
    suspend fun scan(mode: ScanMode): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        
        for (pkg in packages) {
            val threat = analyzePackage(pkg, mode)
            if (threat != null) {
                threats.add(threat)
            }
        }
        
        return threats
    }
    
    private fun analyzePackage(pkg: PackageInfo, mode: ScanMode): ThreatInfo? {
        val signatures = mutableListOf<String>()
        var threatLevel = 0
        
        // 1. 检查包名特征
        if (virusSignatures.isVirusPackageName(pkg.packageName)) {
            signatures.add("恶意包名特征")
            threatLevel += 3
        }
        
        // 2. 检查应用名称
        val appName = pkg.applicationInfo.loadLabel(context.packageManager).toString()
        if (virusSignatures.isVirusAppName(appName)) {
            signatures.add("伪装系统服务")
            threatLevel += 3
        }
        
        // 3. 检查权限组合（System Service 病毒的标志性权限）
        val permissions = pkg.requestedPermissions ?: emptyArray()
        val dangerousPerms = virusSignatures.getDangerousPermissions(permissions.toList())
        if (dangerousPerms.size >= 10) {
            signatures.add("请求 ${dangerousPerms.size} 项高危权限")
            threatLevel += 2
        }
        
        // 4. 检查是否隐藏图标
        val hasLauncher = pkg.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 &&
                context.packageManager.getLaunchIntentForPackage(pkg.packageName) == null
        if (hasLauncher) {
            signatures.add("无桌面图标")
            threatLevel += 2
        }
        
        // 5. 检查 APK 大小（System Service 通常很小）
        val apkPath = pkg.applicationInfo.sourceDir
        val apkSize = File(apkPath).length()
        if (apkSize < 100 * 1024) { // < 100KB
            signatures.add("APK 极小 (${apkSize / 1024}KB)")
            threatLevel += 1
        }
        
        // 6. 高级档：检查组件特征
        if (mode == ScanMode.ADVANCED || mode == ScanMode.ULTIMATE || mode == ScanMode.DEATH) {
            val componentSignatures = virusSignatures.checkComponents(pkg)
            signatures.addAll(componentSignatures)
            threatLevel += componentSignatures.size
        }
        
        // 7. 终极档/死亡档：深度检查
        if (mode == ScanMode.ULTIMATE || mode == ScanMode.DEATH) {
            val deepSignatures = virusSignatures.deepScan(pkg, context)
            signatures.addAll(deepSignatures)
            threatLevel += deepSignatures.size
        }
        
        // 判定威胁
        if (threatLevel >= 5 || signatures.isNotEmpty()) {
            return ThreatInfo(
                packageName = pkg.packageName,
                appName = appName,
                reason = signatures.joinToString(", "),
                threatLevel = minOf(threatLevel, 5),
                signatures = signatures
            )
        }
        
        return null
    }
    
    suspend fun clean(threats: List<ThreatInfo>, mode: ScanMode): CleanResult {
        var success = 0
        var failed = 0
        val details = mutableListOf<String>()
        
        for (threat in threats) {
            val result = when (mode) {
                ScanMode.NORMAL -> cleanNormal(threat)
                ScanMode.ADVANCED -> cleanAdvanced(threat)
                ScanMode.ULTIMATE -> cleanUltimate(threat)
                ScanMode.DEATH -> cleanDeath(threat)
            }
            
            if (result) {
                success++
                details.add("✓ ${threat.appName}")
            } else {
                failed++
                details.add("✗ ${threat.appName}")
            }
        }
        
        return CleanResult(success, failed, details)
    }
    
    private fun cleanNormal(threat: ThreatInfo): Boolean {
        // 普通档：尝试普通卸载
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_DELETE)
            intent.data = android.net.Uri.parse("package:${threat.packageName}")
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun cleanAdvanced(threat: ThreatInfo): Boolean {
        // 高级档：使用无障碍服务自动化点击卸载
        // 这里需要配合 AccessibilityService 实现
        return cleanNormal(threat)
    }
    
    private fun cleanUltimate(threat: ThreatInfo): Boolean {
        // 终极档：使用 Shizuku 强制卸载
        return try {
            ShizukuHelper.uninstallPackage(threat.packageName)
            true
        } catch (e: Exception) {
            cleanAdvanced(threat)
        }
    }
    
    private fun cleanDeath(threat: ThreatInfo): Boolean {
        // 死亡档：多管齐下
        return try {
            // 1. 尝试 Shizuku
            if (ShizukuHelper.uninstallPackage(threat.packageName)) {
                return true
            }
            
            // 2. 尝试禁用设备管理员
            ShizukuHelper.removeDeviceAdmin(threat.packageName)
            
            // 3. 杀死进程
            ShizukuHelper.forceStopPackage(threat.packageName)
            
            // 4. 清除数据
            ShizukuHelper.clearPackageData(threat.packageName)
            
            // 5. 再次尝试卸载
            ShizukuHelper.uninstallPackage(threat.packageName)
            
        } catch (e: Exception) {
            false
        }
    }
}
