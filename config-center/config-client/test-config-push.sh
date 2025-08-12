#!/bin/bash

# 测试配置推送脚本
# 用于验证配置中心是否正确推送配置变更

echo "=========================================="
echo "配置推送测试脚本"
echo "=========================================="

echo ""
echo "1. 检查配置中心服务状态..."
echo "   确保配置中心服务在端口8080运行"
echo ""

echo "2. 检查Spring Boot示例状态..."
echo "   确保Spring Boot示例在端口8081运行"
echo ""

echo "3. 测试步骤："
echo "   a) 在配置中心管理界面修改配置项"
echo "   b) 点击发布配置"
echo "   c) 观察Spring Boot示例的控制台输出"
echo ""

echo "4. 预期结果："
echo "   - 应该看到WebSocket连接成功的日志"
echo "   - 应该看到配置变更通知"
echo "   - 应该看到字段值自动更新"
echo ""

echo "5. 如果配置推送不工作，可能的原因："
echo "   - WebSocket连接失败"
echo "   - 配置推送服务未正确配置"
echo "   - 应用ID或环境ID不匹配"
echo "   - 网络连接问题"
echo ""

echo "6. 调试建议："
echo "   - 查看Spring Boot示例的详细日志"
echo "   - 检查配置中心的WebSocket配置"
echo "   - 验证配置推送服务的状态"
echo ""

echo "7. 手动测试："
echo "   在Spring Boot示例运行时，修改配置并发布，"
echo "   观察是否收到实时更新通知"
echo ""

echo "=========================================="
echo "开始测试..."
echo "=========================================="

# 等待用户操作
read -p "按回车键继续测试，或Ctrl+C退出..."

echo ""
echo "测试完成！"
echo "请检查Spring Boot示例的控制台输出"
