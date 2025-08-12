#!/bin/bash

# 配置热更新功能演示脚本
# 展示如何使用配置热更新功能

echo "=========================================="
echo "配置热更新功能演示"
echo "=========================================="

echo ""
echo "1. 编译项目..."
mvn clean compile

echo ""
echo "2. 运行热更新示例..."
echo "   - 普通Java应用示例"
echo "   - Spring Boot集成示例"
echo ""

echo "选择运行模式:"
echo "1) 普通Java应用示例"
echo "2) Spring Boot集成示例"
echo "3) 运行测试"
echo ""

read -p "请输入选择 (1-3): " choice

case $choice in
    1)
        echo "运行普通Java应用示例..."
        mvn exec:java -Dexec.mainClass="com.bank.config.client.example.HotUpdateExample"
        ;;
    2)
        echo "运行Spring Boot集成示例..."
        echo "注意：Spring Boot示例将使用端口8081，避免与配置中心服务端冲突"
        mvn spring-boot:run -pl config-client -Dspring-boot.run.main-class="com.bank.config.client.example.SpringHotUpdateExample" -Dspring-boot.run.jvmArguments="-Dserver.port=8081"
        ;;
    3)
        echo "运行测试..."
        mvn test -Dtest=ConfigHotUpdateTest
        ;;
    *)
        echo "无效选择，退出"
        exit 1
        ;;
esac

echo ""
echo "演示完成！"
echo ""
echo "使用说明："
echo "1. 确保配置中心服务已启动 (config-server)"
echo "2. 在配置中心创建相应的配置项"
echo "3. 发布配置变更，观察客户端的自动更新"
echo ""
echo "更多信息请查看: HOT_UPDATE_GUIDE.md"
