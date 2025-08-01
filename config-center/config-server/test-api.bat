@echo off
REM 配置中心API测试脚本 (Windows版本)
set BASE_URL=http://localhost:8080/api

echo === 配置中心API测试 ===

REM 1. 健康检查
echo 1. 健康检查
curl -s -X GET "%BASE_URL%/health"

REM 2. 创建应用
echo.
echo 2. 创建应用
curl -s -X POST "%BASE_URL%/applications" -H "Content-Type: application/json" -d "{\"appCode\": \"test-app\", \"appName\": \"测试应用\", \"appDesc\": \"用于测试的应用程序\", \"owner\": \"测试用户\", \"contactEmail\": \"test@example.com\"}"

REM 3. 创建环境
echo.
echo 3. 创建环境
curl -s -X POST "%BASE_URL%/environments" -H "Content-Type: application/json" -d "{\"envCode\": \"test\", \"envName\": \"测试环境\", \"envDesc\": \"用于测试的环境\", \"sortOrder\": 1}"

REM 4. 创建配置项
echo.
echo 4. 创建配置项
curl -s -X POST "%BASE_URL%/config-items" -H "Content-Type: application/json" -d "{\"appId\": 1, \"envId\": 1, \"configKey\": \"test.key\", \"configValue\": \"test-value\", \"configType\": 1, \"description\": \"测试配置项\"}"

REM 5. 查询应用列表
echo.
echo 5. 查询应用列表
curl -s -X GET "%BASE_URL%/applications"

REM 6. 查询环境列表
echo.
echo 6. 查询环境列表
curl -s -X GET "%BASE_URL%/environments"

REM 7. 查询配置项列表
echo.
echo 7. 查询配置项列表
curl -s -X GET "%BASE_URL%/config-items"

REM 8. 客户端API测试
echo.
echo 8. 客户端API测试
curl -s -X GET "%BASE_URL%/client/configs/test-app/test"

echo.
echo === 测试完成 ===
pause 