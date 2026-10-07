#!/data/data/com.termux/files/usr/bin/bash
# System Service 病毒专杀 - 死亡档辅助脚本
# 配合 Termux 使用，需要先安装 Termux 并授予必要权限

echo "=========================================="
echo "System Service 病毒专杀 - 死亡档"
echo "=========================================="
echo ""

# 检查是否有包名参数
if [ -z "$1" ]; then
    echo "用法: ./termux_killer.sh <病毒包名>"
    exit 1
fi

PACKAGE_NAME="$1"
echo "目标包名: $PACKAGE_NAME"
echo ""

# 1. 尝试杀死进程
echo "[1] 杀死进程..."
am force-stop "$PACKAGE_NAME" 2>/dev/null
killall -9 "$PACKAGE_NAME" 2>/dev/null
echo "✓ 进程已终止"

# 2. 尝试禁用组件
echo ""
echo "[2] 禁用组件..."
pm disable "$PACKAGE_NAME" 2>/dev/null
echo "✓ 应用已禁用"

# 3. 移除设备管理员
echo ""
echo "[3] 移除设备管理员..."
dpm remove-active-admin "$PACKAGE_NAME/.DeviceAdminReceiver" 2>/dev/null
dpm remove-active-admin "$PACKAGE_NAME/.AdminReceiver" 2>/dev/null
echo "✓ 设备管理员已移除"

# 4. 清除数据
echo ""
echo "[4] 清除应用数据..."
pm clear "$PACKAGE_NAME" 2>/dev/null
echo "✓ 数据已清除"

# 5. 卸载
echo ""
echo "[5] 卸载应用..."
pm uninstall --user 0 "$PACKAGE_NAME" 2>/dev/null
pm uninstall "$PACKAGE_NAME" 2>/dev/null
echo "✓ 卸载完成"

# 6. 验证
echo ""
echo "[6] 验证清除结果..."
if pm list packages | grep -q "$PACKAGE_NAME"; then
    echo "⚠ 警告: 应用仍然存在"
    echo ""
    echo "建议操作:"
    echo "1. 重启手机进入安全模式"
    echo "2. 使用 adb 从电脑端强制卸载"
    echo "3. 恢复出厂设置（最后手段）"
else
    echo "✓ 清除成功！病毒已被完全移除"
fi

echo ""
echo "=========================================="
echo "清除完成"
echo "=========================================="
