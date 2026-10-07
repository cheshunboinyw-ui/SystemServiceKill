# System Service 病毒专杀

针对 Android "System Service" 远控木马的专业清除工具。

## 病毒简介

**System Service** 是一种伪装成系统服务的 Android 远程监控木马（RAT），于 2026年10月集中爆发，主要针对 vivo/iQOO/小米设备。

### 病毒能力
- 后台录音、偷拍、截屏
- 读取短信验证码、通讯录
- 全屏钓鱼界面窃取密码
- 自我恢复、静默推广
- 设备管理员持久化

### 传播方式
- 非法网站、山寨 App
- 广告 SDK 分发
- 安装包极小（约30KB）

## 四档杀毒模式

### 1. 普通档 ✓
- **权限要求**：管理所有文件
- **功能**：基础扫描，检测已知病毒特征
- **适用场景**：日常扫描，快速检测

### 2. 高级档 ✓✓
- **权限要求**：无障碍服务
- **功能**：深度检测可疑组件，自动化卸载
- **适用场景**：发现可疑应用，需要深度清理

### 3. 终极档 ✓✓✓
- **权限要求**：无障碍 + Shizuku
- **功能**：系统级权限强制卸载
- **适用场景**：病毒有设备管理员权限，普通方式无法卸载

### 4. 死亡档 ✓✓✓✓
- **权限要求**：全部非root权限 + Termux
- **功能**：多管齐下，不择手段清除
- **适用场景**：病毒顽固，前三档无法清除

## 使用方法

### 普通档
1. 安装 APK
2. 授予「管理所有文件」权限
3. 选择「普通档」
4. 点击「开始扫描」

### 高级档
1. 完成普通档步骤
2. 进入 设置 → 无障碍 → System Service 病毒专杀
3. 开启无障碍服务
4. 选择「高级档」扫描

### 终极档
1. 安装 Shizuku：https://shizuku.rikka.app/
2. 启动 Shizuku（通过 Wireless debugging 或 USB）
3. 在本应用中授权 Shizuku 权限
4. 选择「终极档」扫描

### 死亡档
1. 安装 Termux：https://f-droid.org/packages/com.termux/
2. 完成终极档步骤
3. 选择「死亡档」扫描
4. 按照提示在 Termux 中执行清除脚本

详细步骤见：[死亡档使用指南](DEATH_MODE_GUIDE.md)

## 病毒特征识别

本工具通过以下特征识别 System Service 病毒：

### 包名特征
- `com.system.service`
- `com.android.systemservice`
- `com.system.update`
- 包含 "system" + "service/update/core"

### 应用名称
- "System Service"
- "System Update"
- "系统服务"
- "系统更新"

### 行为特征
- 请求大量敏感权限（10+ 项）
- 无桌面图标
- APK 极小（< 100KB）
- 包含开机启动、设备管理员组件
- 未知安装来源

## 编译构建

### 环境要求
- Android Studio Arctic Fox 及以上
- JDK 11+
- Android SDK 34
- Gradle 8.0

### 构建步骤
```bash
# 克隆项目
cd SystemServiceKiller

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 输出位置
# Debug: app/build/outputs/apk/debug/app-debug.apk
# Release: app/build/outputs/apk/release/app-release.apk
```

## 技术实现

### 扫描引擎
- 多层特征匹配
- 权限组合分析
- 组件行为检测
- APK 静态分析

### 清除机制

**普通档**
- `ACTION_DELETE` Intent

**高级档**
- AccessibilityService 自动化点击

**终极档**
- Shizuku IPackageManager API
- 强制停止进程
- 清除应用数据

**死亡档**
- 上述所有方法
- Termux 系统级命令
- `pm uninstall --user 0`
- `dpm remove-active-admin`

## 依赖库

- AndroidX Core KTX
- Material Components
- Shizuku API
- Kotlin Coroutines

## 免责声明

本工具仅用于检测和清除 System Service 恶意软件，不对误判或数据丢失负责。

使用前请：
- 备份重要数据
- 确认要卸载的应用确实是病毒
- 了解各档位的权限要求

## 开源协议

MIT License

## 支持

如有问题或建议，请提交 Issue。

---

**警告**：使用「死亡档」前请仔细阅读使用指南，不当操作可能影响系统稳定性。
