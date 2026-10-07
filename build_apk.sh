#!/bin/bash
# System Service 病毒专杀 - APK 构建脚本

echo "=========================================="
echo "System Service 病毒专杀 - 构建脚本"
echo "=========================================="
echo ""

# 检查 Gradle
if [ ! -f "./gradlew" ]; then
    echo "✗ 错误: gradlew 不存在"
    echo "请确保在项目根目录运行此脚本"
    exit 1
fi

# 赋予 gradlew 执行权限
chmod +x ./gradlew

# 选择构建类型
echo "选择构建类型:"
echo "1. Debug (调试版本，未签名)"
echo "2. Release (发布版本，需要签名密钥)"
echo ""
read -p "输入选择 [1/2]: " BUILD_TYPE

case $BUILD_TYPE in
    1)
        echo ""
        echo "[1/3] 清理旧构建..."
        ./gradlew clean
        
        echo ""
        echo "[2/3] 构建 Debug APK..."
        ./gradlew assembleDebug
        
        echo ""
        echo "[3/3] 完成"
        echo ""
        echo "APK 位置: app/build/outputs/apk/debug/app-debug.apk"
        ;;
    2)
        echo ""
        echo "[1/3] 清理旧构建..."
        ./gradlew clean
        
        echo ""
        echo "[2/3] 构建 Release APK..."
        ./gradlew assembleRelease
        
        echo ""
        echo "[3/3] 完成"
        echo ""
        echo "APK 位置: app/build/outputs/apk/release/app-release.apk"
        echo ""
        echo "注意: Release 版本需要签名密钥"
        echo "如果未配置签名，APK 将无法安装"
        ;;
    *)
        echo "无效选择"
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo "构建完成"
echo "=========================================="
