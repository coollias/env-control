<template>
  <div class="client">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>客户端API测试</span>
        </div>
      </template>

      <!-- API测试区域 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>请求参数</span>
            </template>
            
            <el-form :model="requestForm" label-width="100px">
              <el-form-item label="应用ID">
                <el-select v-model="requestForm.appId" placeholder="选择应用" style="width: 100%">
                  <el-option
                    v-for="app in applications"
                    :key="app.id"
                    :label="`${app.appName} (${app.appCode})`"
                    :value="app.id"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="环境ID">
                <el-select v-model="requestForm.envId" placeholder="选择环境" style="width: 100%">
                  <el-option
                    v-for="env in environments"
                    :key="env.id"
                    :label="`${env.envName} (${env.envCode})`"
                    :value="env.id"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="配置键">
                <el-input v-model="requestForm.configKey" placeholder="可选，留空获取所有配置" />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="testGetAllConfigs" :loading="loading">
                  获取所有配置
                </el-button>
                <el-button type="success" @click="testGetConfigItem" :loading="loading" :disabled="!requestForm.configKey">
                  获取单个配置
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>响应结果</span>
            </template>
            
            <div v-if="response" class="response-container">
              <el-tag :type="response.success ? 'success' : 'danger'" style="margin-bottom: 10px;">
                {{ response.success ? '成功' : '失败' }}
              </el-tag>
              
              <div class="response-content">
                <pre>{{ JSON.stringify(response, null, 2) }}</pre>
              </div>
            </div>
            
            <div v-else class="no-response">
              <el-empty description="暂无响应数据" />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- API文档 -->
      <el-card style="margin-top: 20px;">
        <template #header>
          <span>API文档</span>
        </template>
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="获取所有配置">
            <code>GET /api/client/config/{appId}/{envId}</code>
          </el-descriptions-item>
          <el-descriptions-item label="获取单个配置">
            <code>GET /api/client/config/{appId}/{envId}/{configKey}</code>
          </el-descriptions-item>
          <el-descriptions-item label="请求头">
            <code>Content-Type: application/json</code>
          </el-descriptions-item>
          <el-descriptions-item label="响应格式">
            <code>JSON</code>
          </el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px;">
          <h4>响应示例：</h4>
          <pre class="api-example">
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "appId": 1,
    "envId": 1,
    "configs": [
      {
        "configKey": "database.url",
        "configValue": "jdbc:mysql://localhost:3306/test",
        "configType": 1,
        "isEncrypted": 0,
        "isRequired": 1
      }
    ]
  }
}</pre>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { clientApi, applicationApi, environmentApi } from '../api'

const loading = ref(false)
const response = ref(null)
const applications = ref([])
const environments = ref([])

const requestForm = reactive({
  appId: '',
  envId: '',
  configKey: ''
})

const loadApplications = async () => {
  try {
    const response = await applicationApi.getApplications({ size: 1000 })
    applications.value = response.data.content || []
  } catch (error) {
    console.error('加载应用列表失败:', error)
  }
}

const loadEnvironments = async () => {
  try {
    const response = await environmentApi.getEnvironments({ size: 1000 })
    environments.value = response.data.content || []
  } catch (error) {
    console.error('加载环境列表失败:', error)
  }
}

const testGetAllConfigs = async () => {
  if (!requestForm.appId || !requestForm.envId) {
    ElMessage.warning('请选择应用和环境')
    return
  }
  
  loading.value = true
  try {
    const result = await clientApi.getConfig(requestForm.appId, requestForm.envId)
    response.value = {
      success: true,
      data: result.data,
      timestamp: new Date().toISOString()
    }
    ElMessage.success('获取配置成功')
  } catch (error) {
    response.value = {
      success: false,
      error: error.message,
      timestamp: new Date().toISOString()
    }
    ElMessage.error('获取配置失败')
  } finally {
    loading.value = false
  }
}

const testGetConfigItem = async () => {
  if (!requestForm.appId || !requestForm.envId || !requestForm.configKey) {
    ElMessage.warning('请填写完整的请求参数')
    return
  }
  
  loading.value = true
  try {
    const result = await clientApi.getConfigItem(requestForm.appId, requestForm.envId, requestForm.configKey)
    response.value = {
      success: true,
      data: result.data,
      timestamp: new Date().toISOString()
    }
    ElMessage.success('获取配置项成功')
  } catch (error) {
    response.value = {
      success: false,
      error: error.message,
      timestamp: new Date().toISOString()
    }
    ElMessage.error('获取配置项失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadApplications()
  loadEnvironments()
})
</script>

<style scoped>
.client {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.response-container {
  max-height: 400px;
  overflow-y: auto;
}

.response-content {
  background-color: #f5f5f5;
  border-radius: 4px;
  padding: 10px;
  margin-top: 10px;
}

.response-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.no-response {
  text-align: center;
  padding: 40px 0;
}

.api-example {
  background-color: #f5f5f5;
  border-radius: 4px;
  padding: 15px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  overflow-x: auto;
}
</style> 