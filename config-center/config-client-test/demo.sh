#!/bin/bash

echo "=== 配置客户端测试演示 ==="
echo ""

# 检查Java和Maven
if ! command -v java > /dev/null; then
    echo "✗ 未找到Java，请先安装Java"
    exit 1
fi

if ! command -v mvn > /dev/null; then
    echo "✗ 未找到Maven，请先安装Maven"
    exit 1
fi

echo "✓ Java版本: $(java -version 2>&1 | head -n 1)"
echo "✓ Maven版本: $(mvn -version 2>&1 | head -n 1)"
echo ""

# 检查配置中心服务
echo "1. 检查配置中心服务..."
if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "✓ 配置中心服务运行正常 (http://localhost:8080)"
else
    echo "✗ 配置中心服务未运行"
    echo "  请先启动配置中心服务:"
    echo "  cd ../config-server && mvn spring-boot:run"
    echo ""
    read -p "是否继续演示？(y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 编译项目
echo ""
echo "2. 编译测试项目..."
if mvn clean compile -q; then
    echo "✓ 编译成功"
else
    echo "✗ 编译失败"
    exit 1
fi

# 启动测试应用
echo ""
echo "3. 启动测试应用..."
echo "   应用将在 http://localhost:8081 启动"
echo "   按 Ctrl+C 停止应用"
echo ""

# 在后台启动应用
mvn spring-boot:run &
APP_PID=$!

# 等待应用启动
echo "等待应用启动..."
sleep 10

# 检查应用是否启动成功
if curl -s http://localhost:8081/api/config/health > /dev/null 2>&1; then
    echo "✓ 测试应用启动成功"
else
    echo "✗ 测试应用启动失败"
    kill $APP_PID 2>/dev/null
    exit 1
fi

echo ""
echo "4. 演示功能..."
echo ""

# 演示API接口
echo "=== API接口演示 ==="

echo "健康检查:"
curl -s http://localhost:8081/api/config/health | jq '.' 2>/dev/null || curl -s http://localhost:8081/api/config/health
echo ""

echo "启动客户端:"
curl -s -X POST http://localhost:8081/api/config/start | jq '.' 2>/dev/null || curl -s -X POST http://localhost:8081/api/config/start
echo ""

echo "获取所有配置:"
curl -s http://localhost:8081/api/config/all | jq '.' 2>/dev/null || curl -s http://localhost:8081/api/config/all
echo ""

echo "获取业务配置:"
curl -s http://localhost:8081/api/business/config | jq '.' 2>/dev/null || curl -s http://localhost:8081/api/business/config
echo ""

echo "获取监控指标:"
curl -s http://localhost:8081/api/config/metrics | jq '.' 2>/dev/null || curl -s http://localhost:8081/api/config/metrics
echo ""

echo "获取缓存信息:"
curl -s http://localhost:8081/api/config/cache | jq '.' 2>/dev/null || curl -s http://localhost:8081/api/config/cache
echo ""

echo ""
echo "=== Web界面演示 ==="
echo "现在可以打开Web界面进行可视化测试:"
echo "1. 运行: ./start-frontend.sh"
echo "2. 或者直接在浏览器中打开: frontend/index.html"
echo ""

echo "=== 演示完成 ==="
echo "测试应用仍在运行，可以继续测试"
echo "按 Ctrl+C 停止应用"
echo ""

# 等待用户中断
trap "echo ''; echo '正在停止应用...'; kill $APP_PID 2>/dev/null; exit 0" INT
wait $APP_PID 