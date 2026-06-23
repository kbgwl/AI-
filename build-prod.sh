#!/bin/bash
# 生产环境构建脚本 - 前端JS混淆 + 后端JAR混淆
set -e

echo "=========================================="
echo "  AI智能客服系统 - 生产构建"
echo "=========================================="

# 1. 构建前端（JS混淆加密）
echo ""
echo "[1/4] 构建前端（JS代码混淆加密）..."
cd "$(dirname "$0")/客服系统-前端"
NODE_ENV=production npm run build
echo "  前端构建完成，输出到 dist/"

# 2. 同步到后端静态资源
echo ""
echo "[2/4] 同步前端文件到后端..."
rm -rf "../客服系统-后端/target/classes/static"
mkdir -p "../客服系统-后端/target/classes/static"
cp -r dist/* "../客服系统-后端/target/classes/static/"

# 3. 构建后端JAR
echo ""
echo "[3/4] 构建后端..."
cd "../客服系统-后端"
mvn clean package -DskipTests -q
echo "  后端JAR构建完成"

# 4. 后端混淆（如果有proguard）
echo ""
echo "[4/4] 后端代码混淆..."
if command -v proguard &> /dev/null; then
    proguard @proguard.pro
    echo "  后端混淆完成"
else
    echo "  ProGuard未安装，跳过混淆（可单独执行混淆）"
fi

echo ""
echo "=========================================="
echo "  构建完成!"
echo "  前端: 客服系统-前端/dist/ (已混淆)"
echo "  后端: 客服系统-后端/target/*.jar"
echo "=========================================="
