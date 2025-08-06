#!/bin/bash

# 配置客户端构建脚本

echo "开始构建配置客户端..."

# 检查Java版本
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
echo "Java版本: $java_version"

# 清理之前的构建
echo "清理之前的构建..."
rm -rf target/

# 编译
echo "编译项目..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "编译成功"
else
    echo "编译失败"
    exit 1
fi

# 运行测试
echo "运行测试..."
mvn test

if [ $? -eq 0 ]; then
    echo "测试通过"
else
    echo "测试失败"
    exit 1
fi

# 打包
echo "打包项目..."
mvn package -DskipTests

if [ $? -eq 0 ]; then
    echo "打包成功"
    echo "JAR文件位置: target/config-client-1.0.0.jar"
else
    echo "打包失败"
    exit 1
fi

echo "构建完成！" 