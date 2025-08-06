#!/bin/bash

# 配置客户端演示脚本

echo "=== 配置客户端演示 ==="

# 检查Java是否可用
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java，请先安装Java"
    exit 1
fi

# 检查Maven是否可用
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven，请先安装Maven"
    exit 1
fi

echo "1. 编译项目..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "编译失败"
    exit 1
fi

echo "2. 运行测试..."
mvn test

if [ $? -ne 0 ]; then
    echo "测试失败"
    exit 1
fi

echo "3. 运行示例..."
echo "注意: 示例需要配置中心服务器运行在 http://localhost:8080"
echo "如果服务器未运行，示例会显示连接错误，这是正常的"

# 运行示例
mvn exec:java -Dexec.mainClass="com.bank.config.client.example.ConfigClientExample" \
    -Dexec.args="" \
    -Dexec.classpathScope="compile"

echo "演示完成！" 