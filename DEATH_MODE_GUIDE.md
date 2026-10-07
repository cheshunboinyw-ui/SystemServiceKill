# 死亡档使用指南

## 前置要求

### 1. 安装 Termux
- 从 F-Droid 下载：https://f-droid.org/packages/com.termux/
- 或从 GitHub：https://github.com/termux/termux-app/releases

### 2. 在 Termux 中安装必要工具
打开 Termux，执行以下命令：

```bash
# 更新包列表
pkg update && pkg upgrade

# 安装必要工具
pkg install tsu
```

### 3. 授权 Termux 访问存储
```bash
termux-setup-storage
```

## 使用步骤

### 1. 复制脚本到 Termux
专杀工具会自动将 `termux_killer.sh` 复制到 Termux 可访问的位置：
```
/storage/emulated/0/Download/termux_killer.sh
```

### 2. 在 Termux 中执行脚本

```bash
# 进入下载目录
cd ~/storage/downloads

# 给脚本执行权限
chmod +x termux_killer.sh

# 执行脚本（替换包名）
./termux_killer.sh com.system.service
```

### 3. 如果病毒有设备管理员权限

死亡档会尝试自动移除，但如果失败，需要手动操作：

1. 打开 设置 → 安全 → 设备管理器
2. 找到 "System Service" 或可疑应用
3. 取消勾选，点击停用
4. 再次运行专杀脚本

## 死亡档清除策略

死亡档采用多重手段：

1. **Shizuku 强制卸载** - 使用系统级权限直接卸载
2. **禁用设备管理员** - 移除病毒的保护机制
3. **强制停止进程** - 杀死后台运行的病毒进程
4. **清除应用数据** - 删除病毒存储的数据
5. **Termux 系统级命令** - 使用 Android 底层命令强制清除
6. **无障碍服务自动化** - 自动点击卸载界面

## 故障排除

### 问题1：脚本执行失败
```bash
# 检查脚本是否有执行权限
ls -l termux_killer.sh

# 重新赋予权限
chmod 755 termux_killer.sh
```

### 问题2：权限不足
```bash
# 尝试使用 tsu（类似 sudo）
tsu
./termux_killer.sh com.system.service
```

### 问题3：病毒仍然存在

最后手段：

1. **安全模式启动**
   - 关机后按电源键，长按"关机"按钮
   - 选择"重启到安全模式"
   - 在安全模式下卸载病毒

2. **ADB 卸载**（需要电脑）
   ```bash
   adb shell pm uninstall com.system.service
   ```

3. **恢复出厂设置**（最终手段）
   - 备份重要数据
   - 设置 → 系统 → 重置 → 恢复出厂设置

## 预防建议

1. 只从官方应用商店下载应用
2. 安装前检查权限请求是否合理
3. 定期使用本工具扫描
4. 及时更新系统安全补丁
5. 不要点击可疑链接或广告
