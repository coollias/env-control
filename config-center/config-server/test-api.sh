#!/bin/bash

# 配置中心API测试脚本
BASE_URL="http://localhost:8080/api"

echo "=== 配置中心API测试 ==="

# 1. 健康检查
echo "1. 健康检查"
curl -s -X GET "$BASE_URL/health" | jq '.'

# 2. 创建应用
echo -e "\n2. 创建应用"
curl -s -X POST "$BASE_URL/applications" \
  -H "Content-Type: application/json" \
  -d '{
    "appCode": "test-app",
    "appName": "测试应用",
    "appDesc": "用于测试的应用程序",
    "owner": "测试用户",
    "contactEmail": "test@example.com"
  }' | jq '.'

# 3. 创建环境
echo -e "\n3. 创建环境"
curl -s -X POST "$BASE_URL/environments" \
  -H "Content-Type: application/json" \
  -d '{
    "envCode": "test",
    "envName": "测试环境",
    "envDesc": "用于测试的环境",
    "sortOrder": 1
  }' | jq '.'

# 4. 创建配置项
echo -e "\n4. 创建配置项"
curl -s -X POST "$BASE_URL/config-items" \
  -H "Content-Type: application/json" \
  -d '{
    "appId": 1,
    "envId": 1,
    "configKey": "test.key",
    "configValue": "test-value",
    "configType": 1,
    "description": "测试配置项"
  }' | jq '.'

# 5. 查询应用列表
echo -e "\n5. 查询应用列表"
curl -s -X GET "$BASE_URL/applications" | jq '.'

# 6. 查询环境列表
echo -e "\n6. 查询环境列表"
curl -s -X GET "$BASE_URL/environments" | jq '.'

# 7. 查询配置项列表
echo -e "\n7. 查询配置项列表"
curl -s -X GET "$BASE_URL/config-items" | jq '.'

# 8. 客户端API测试
echo -e "\n8. 客户端API测试"
curl -s -X GET "$BASE_URL/client/configs/test-app/test" | jq '.'

echo -e "\n=== 测试完成 ===" 