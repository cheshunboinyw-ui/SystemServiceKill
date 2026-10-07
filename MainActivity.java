package com.antivirus.systemservicekiller;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private TextView logView;
    private ScrollView scrollView;
    private Button btnLevel1, btnLevel2, btnLevel3, btnLevel4;
    private StringBuilder logBuilder = new StringBuilder();
    
    // 病毒特征库
    private static final List<String> VIRUS_PACKAGE_PATTERNS = Arrays.asList(
        "com.system.service",
        "system.service",
        "systemservice",
        "com.android.systemupdate",
        "system.update"
    );
    
    private static final List<String> VIRUS_APP_NAMES = Arrays.asList(
        "System Service",
        "System Update",
        "系统服务",
        "系统更新"
    );
    
    private static final int MIN_VIRUS_PERMISSIONS = 20; // 病毒通常请求25-46项权限
    private static final int MAX_NORMAL_APK_SIZE = 100 * 1024; // 100KB，病毒约30KB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        logView = findViewById(R.id.logView);
        scrollView = findViewById(R.id.scrollView);
        btnLevel1 = findViewById(R.id.btnLevel1);
        btnLevel2 = findViewById(R.id.btnLevel2);
        btnLevel3 = findViewById(R.id.btnLevel3);
        btnLevel4 = findViewById(R.id.btnLevel4);
        
        btnLevel1.setOnClickListener(v -> executeLevel1());
        btnLevel2.setOnClickListener(v -> executeLevel2());
        btnLevel3.setOnClickListener(v -> executeLevel3());
        btnLevel4.setOnClickListener(v -> executeLevel4());
        
        log("System Service 病毒专杀工具已启动\n");
        log("病毒特征：\n- 包名含 system.service\n- 应用名伪装系统服务\n- 请求20+敏感权限\n- 安装包<100KB\n- 无桌面图标\n\n");
    }
    
    private void log(String msg) {
        logBuilder.append(msg).append("\n");
        runOnUiThread(() -> {
            logView.setText(logBuilder.toString());
            scrollView.fullScroll(View.FOCUS_DOWN);
        });
    }
    
    // ========== 档位 1：普通档 ==========
    private void executeLevel1() {
        log("\n========== 开始普通档扫描 ==========");
        log("权限：基础应用权限 + 文件访问");
        
        if (!checkStoragePermission()) {
            requestStoragePermission();
            return;
        }
        
        List<SuspiciousApp> suspects = scanInstalledApps();
        if (suspects.isEmpty()) {
            log("✓ 未发现可疑应用");
            Toast.makeText(this, "未发现病毒", Toast.LENGTH_SHORT).show();
        } else {
            log("\n发现 " + suspects.size() + " 个可疑应用：");
            for (SuspiciousApp app : suspects) {
                log("- " + app.appName + " (" + app.packageName + ")");
                log("  风险指数：" + app.riskScore + "/100");
                uninstallApp(app.packageName);
            }
        }
        log("========== 普通档扫描完成 ==========\n");
    }
    
    // ========== 档位 2：高级档 ==========
    private void executeLevel2() {
        log("\n========== 开始高级档扫描 ==========");
        log("权限：基础 + 无障碍服务");
        
        if (!isAccessibilityServiceEnabled()) {
            log("⚠ 需要开启无障碍权限");
            openAccessibilitySettings();
            return;
        }
        
        executeLevel1();
        
        log("执行高级清理...");
        List<SuspiciousApp> suspects = scanInstalledApps();
        for (SuspiciousApp app : suspects) {
            forceStopViaAccessibility(app.packageName);
            clearAppDataViaAccessibility(app.packageName);
        }
        
        scanAndKillProcesses();
        log("========== 高级档扫描完成 ==========\n");
    }
    
    // ========== 档位 3：终极档 ==========
    private void executeLevel3() {
        log("\n========== 开始终极档扫描 ==========");
        log("权限：基础 + 无障碍 + Shizuku");
        
        if (!isShizukuAvailable()) {
            log("⚠ 需要开启 Shizuku");
            Toast.makeText(this, "请安装并启动 Shizuku", Toast.LENGTH_LONG).show();
            return;
        }
        
        executeLevel2();
        
        log("使用 Shizuku 强制清除...");
        List<SuspiciousApp> suspects = scanInstalledApps();
        for (SuspiciousApp app : suspects) {
            uninstallViaShizuku(app.packageName);
            killProcessViaShizuku(app.packageName);
        }
        
        cleanVirusFiles();
        log("========== 终极档扫描完成 ==========\n");
    }
    
    // ========== 档位 4：死亡档 ==========
    private void executeLevel4() {
        log("\n========== 开始死亡档扫描 ==========");
        log("权限：全部非 root 权限 + Termux 协同");
        log("警告：将使用一切手段清除病毒");
        
        executeLevel3();
        
        log("执行深度清理...");
        deepScanAndKill();
        cleanSystemCache();
        resetDeviceAdminApps();
        scanAndRemoveAutoStart();
        cleanContentProviders();
        
        log("========== 死亡档扫描完成 ==========\n");
        Toast.makeText(this, "已完成最高强度清理", Toast.LENGTH_LONG).show();
    }
    
    // ========== 核心扫描逻辑 ==========
    private List<SuspiciousApp> scanInstalledApps() {
        List<SuspiciousApp> suspects = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        
        log("正在扫描已安装应用...");
        
        for (PackageInfo pkg : packages) {
            try {
                String packageName = pkg.packageName;
                ApplicationInfo appInfo = pkg.applicationInfo;
                String appName = pm.getApplicationLabel(appInfo).toString();
                
                int riskScore = calculateRiskScore(pkg, appName, packageName);
                
                if (riskScore >= 60) {
                    SuspiciousApp app = new SuspiciousApp();
                    app.packageName = packageName;
                    app.appName = appName;
                    app.riskScore = riskScore;
                    suspects.add(app);
                }
            } catch (Exception e) {
                // 跳过异常
            }
        }
        
        return suspects;
    }
    
    private int calculateRiskScore(PackageInfo pkg, String appName, String packageName) {
        int score = 0;
        
        // 1. 包名匹配
        for (String pattern : VIRUS_PACKAGE_PATTERNS) {
            if (packageName.toLowerCase().contains(pattern.toLowerCase())) {
                score += 40;
                break;
            }
        }
        
        // 2. 应用名匹配
        for (String virusName : VIRUS_APP_NAMES) {
            if (appName.equalsIgnoreCase(virusName)) {
                score += 30;
                break;
            }
        }
        
        // 3. 权限数量
        if (pkg.requestedPermissions != null) {
            int permCount = pkg.requestedPermissions.length;
            if (permCount >= MIN_VIRUS_PERMISSIONS) {
                score += 20;
            }
            
            // 检查危险权限
            List<String> dangerousPerms = Arrays.asList(
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO",
                "android.permission.READ_SMS",
                "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.BIND_ACCESSIBILITY_SERVICE"
            );
            
            int dangerCount = 0;
            for (String perm : pkg.requestedPermissions) {
                if (dangerousPerms.contains(perm)) dangerCount++;
            }
            if (dangerCount >= 3) score += 15;
        }
        
        // 4. APK 大小
        try {
            File apkFile = new File(pkg.applicationInfo.sourceDir);
            if (apkFile.length() < MAX_NORMAL_APK_SIZE) {
                score += 10;
            }
        } catch (Exception e) {}
        
        // 5. 无桌面图标
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            score += 15;
        }
        
        // 6. 系统应用标识（非系统应用伪装）
        if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            if (packageName.contains("android") || packageName.contains("system")) {
                score += 20;
            }
        }
        
        return Math.min(score, 100);
    }
    
    // ========== 卸载/清除方法 ==========
    private void uninstallApp(String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            log("已发起卸载请求：" + packageName);
        } catch (Exception e) {
            log("卸载失败：" + e.getMessage());
        }
    }
    
    private void forceStopViaAccessibility(String packageName) {
        // 通过无障碍服务强制停止
        Intent intent = new Intent(this, KillerAccessibilityService.class);
        intent.putExtra("action", "force_stop");
        intent.putExtra("package", packageName);
        startService(intent);
    }
    
    private void clearAppDataViaAccessibility(String packageName) {
        Intent intent = new Intent(this, KillerAccessibilityService.class);
        intent.putExtra("action", "clear_data");
        intent.putExtra("package", packageName);
        startService(intent);
    }
    
    private void uninstallViaShizuku(String packageName) {
        try {
            executeShellCommand("pm uninstall --user 0 " + packageName);
            log("Shizuku 卸载：" + packageName);
        } catch (Exception e) {
            log("Shizuku 卸载失败：" + e.getMessage());
        }
    }
    
    private void killProcessViaShizuku(String packageName) {
        try {
            executeShellCommand("am force-stop " + packageName);
            executeShellCommand("killall " + packageName);
            log("Shizuku 强杀进程：" + packageName);
        } catch (Exception e) {}
    }
    
    private void scanAndKillProcesses() {
        try {
            String result = executeShellCommand("ps -A");
            for (String pattern : VIRUS_PACKAGE_PATTERNS) {
                if (result.contains(pattern)) {
                    log("发现可疑进程：" + pattern);
                    executeShellCommand("killall " + pattern);
                }
            }
        } catch (Exception e) {}
    }
    
    private void cleanVirusFiles() {
        log("清理病毒文件...");
        List<String> suspiciousPaths = Arrays.asList(
            "/sdcard/Android/data/com.system.service",
            "/data/local/tmp/system_service",
            "/sdcard/.system_cache"
        );
        
        for (String path : suspiciousPaths) {
            File file = new File(path);
            if (file.exists()) {
                deleteRecursive(file);
                log("已删除：" + path);
            }
        }
    }
    
    private void deepScanAndKill() {
        log("深度扫描系统...");
        
        // 扫描所有用户空间
        try {
            String users = executeShellCommand("pm list users");
            log("用户列表：" + users);
            
            for (SuspiciousApp app : scanInstalledApps()) {
                executeShellCommand("pm uninstall --user 0 " + app.packageName);
                executeShellCommand("pm clear " + app.packageName);
            }
        } catch (Exception e) {}
    }
    
    private void cleanSystemCache() {
        log("清理系统缓存...");
        try {
            executeShellCommand("rm -rf /data/system/package_cache");
            executeShellCommand("rm -rf /data/dalvik-cache");
        } catch (Exception e) {}
    }
    
    private void resetDeviceAdminApps() {
        log("检查设备管理员...");
        try {
            String result = executeShellCommand("dumpsys device_policy");
            for (String pattern : VIRUS_PACKAGE_PATTERNS) {
                if (result.contains(pattern)) {
                    log("移除设备管理员：" + pattern);
                    executeShellCommand("dpm remove-active-admin " + pattern);
                }
            }
        } catch (Exception e) {}
    }
    
    private void scanAndRemoveAutoStart() {
        log("清理自启动项...");
        try {
            executeShellCommand("pm disable com.system.service/.BootReceiver");
            executeShellCommand("pm disable system.service/.AutoStartReceiver");
        } catch (Exception e) {}
    }
    
    private void cleanContentProviders() {
        log("清理 ContentProvider...");
        // ContentProvider 通常与应用一起卸载
    }
    
    // ========== 工具方法 ==========
    private String executeShellCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        process.waitFor();
        return output.toString();
    }
    
    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursive(child);
            }
        }
        file.delete();
    }
    
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else {
            requestPermissions(new String[]{
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        }
    }
    
    private boolean isAccessibilityServiceEnabled() {
        String service = getPackageName() + "/" + KillerAccessibilityService.class.getName();
        String enabledServices = Settings.Secure.getString(
            getContentResolver(), 
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        return enabledServices != null && enabledServices.contains(service);
    }
    
    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        Toast.makeText(this, "请开启 System Service 专杀的无障碍权限", Toast.LENGTH_LONG).show();
    }
    
    private boolean isShizukuAvailable() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("moe.shizuku.privileged.api", 0);
            return info != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    static class SuspiciousApp {
        String packageName;
        String appName;
        int riskScore;
    }
}
