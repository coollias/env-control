#!/bin/bash

echo "=== 配置客户端测试前端启动脚本 ==="
echo ""

# 检查后端服务是否运行
echo "1. 检查后端服务状态..."
if curl -s http://localhost:8081/api/config/health > /dev/null; then
    echo "✓ 后端服务运行正常"
else
    echo "✗ 后端服务未运行，请先启动后端服务"
    echo "  运行命令: mvn spring-boot:run"
    echo "  或者检查应用是否在8081端口运行"
    exit 1
fi

echo ""
echo "2. 启动前端界面..."
echo "   前端界面将在浏览器中打开"
echo "   如果浏览器没有自动打开，请手动访问: file://$(pwd)/frontend/index.html"
echo ""

# 尝试在浏览器中打开前端
if command -v open > /dev/null; then
    # macOS
    open frontend/index.html
elif command -v xdg-open > /dev/null; then
    # Linux
    xdg-open frontend/index.html
elif command -v start > /dev/null; then
    # Windows
    start frontend/index.html
else
    echo "无法自动打开浏览器，请手动访问:"
    echo "file://$(pwd)/frontend/index.html"
fi

echo ""
echo "3. 前端功能说明:"
echo "   - 客户端状态监控: 实时显示客户端运行状态"
echo "   - 操作控制: 启动/停止客户端，刷新配置"
echo "   - 配置展示: 显示所有配置和业务配置"
echo "   - 监控指标: 显示各种监控数据"
echo "   - 缓存信息: 显示本地缓存状态"
echo "   - 实时日志: 显示操作日志和系统日志"
echo ""
echo "4. 注意事项:"
echo "   - 界面会自动刷新状态和配置"
echo "   - 可以手动点击按钮进行各种操作"
echo "   - 实时日志会显示所有操作结果"
echo ""
echo "前端界面已启动，按 Ctrl+C 退出" 