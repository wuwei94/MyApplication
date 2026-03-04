#!/bin/bash
# 自动为 Flutter 模块的 Android 项目添加 coreLibraryDesugaring 配置
# 使用方法: ./apply_desugaring.sh

echo "🚀 正在应用 coreLibraryDesugaring 配置..."

# 检查 dart 是否可用
if ! command -v dart &> /dev/null; then
    echo "❌ 错误: 找不到 dart 命令"
    echo "   请确保 Flutter SDK 已正确安装并添加到 PATH"
    exit 1
fi

# 运行 dart 脚本
dart apply_desugaring.dart

# 检查执行结果
if [ $? -eq 0 ]; then
    echo ""
    echo "✨ 完成！你可以继续使用 flutter run 或 flutter build 命令"
else
    echo ""
    echo "❌ 应用配置失败"
    exit 1
fi
