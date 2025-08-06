#!/bin/bash

echo "=== 配置客户端GUI调试工具 ==="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java环境"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven环境"
    exit 1
fi

echo "1. 编译项目..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

echo "2. 启动GUI界面..."
echo "注意: 确保配置中心服务器运行在 http://localhost:8080"
echo "GUI界面将显示配置客户端的实时状态和监控信息"
echo ""

mvn exec:java -Dexec.mainClass="com.bank.config.client.gui.ConfigClientGUI" \
    -Dexec.args="" \
    -Dexec.classpathScope="compile"

echo "GUI界面已关闭" 