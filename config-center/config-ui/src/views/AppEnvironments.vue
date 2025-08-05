<template>
  <div class="app-environments">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>应用环境配置</span>
          <div class="header-actions">
            <el-select v-model="selectedApp" placeholder="选择应用" clearable @change="handleAppChange" style="width: 200px;">
              <el-option
                v-for="app in applications"
                :key="app.id"
                :label="app.appName"
                :value="app.id"
              />
            </el-select>
            <el-select v-model="selectedEnv" placeholder="选择环境" clearable @change="handleEnvChange" style="width: 150px;">
              <el-option
                v-for="env in environments"
                :key="env.id"
                :label="env.envName"
                :value="env.id"
              />
            </el-select>
          </div>
        </div>
      </template>

      <!-- 应用卡片网格 -->
      <div class="app-grid" v-loading="loading">
        <el-card
          v-for="app in filteredApplications"
          :key="app.id"
          class="app-card"
          :class="{ 'selected': selectedApp === app.id }"
          @click="selectApp(app)"
        >
          <template #header>
            <div class="app-card-header">
              <div class="app-info">
                <h3>{{ app.appName }}</h3>
                <p class="app-code">{{ app.appCode }}</p>
                <p class="app-desc">{{ app.appDesc || '暂无描述' }}</p>
              </div>
              <div class="app-status">
                <el-tag :type="app.status === 1 ? 'success' : 'danger'">
                  {{ app.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </div>
            </div>
          </template>

          <!-- 环境列表 -->
          <div class="environment-list">
            <div
              v-for="env in getAppEnvironments(app.id)"
              :key="env.id"
              class="env-item"
              :class="{ 'selected': selectedEnv === env.id }"
              @click.stop="selectEnvironment(app, env)"
            >
              <div class="env-info">
                <el-icon class="env-icon"><Setting /></el-icon>
                <span class="env-name">{{ env.envName }}</span>
                <span class="env-code">({{ env.envCode }})</span>
              </div>
              <div class="env-actions">
                <el-button size="small" type="primary" @click.stop="viewConfig(app, env)">
                  查看配置
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 配置查看对话框 -->
    <el-dialog
      v-model="showConfigDialog"
      :title="`${selectedAppInfo?.appName} - ${selectedEnvInfo?.envName} 配置`"
      width="80%"
      top="5vh"
    >
      <div class="config-viewer">
        <!-- 格式选择 -->
        <div class="format-selector">
          <el-radio-group v-model="selectedFormat" @change="handleFormatChange">
            <el-radio-button label="yaml">YAML</el-radio-button>
            <el-radio-button label="json">JSON</el-radio-button>
            <el-radio-button label="properties">Properties</el-radio-button>
          </el-radio-group>
          <div class="format-actions">
            <el-button @click="refreshConfig" :loading="refreshing">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button @click="copyConfig" type="success">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
            <el-button @click="downloadConfig" type="warning">
              <el-icon><Download /></el-icon>
              下载
            </el-button>
          </div>
        </div>

        <!-- 配置内容 -->
        <div class="config-content">
          <MonacoEditor
            v-if="showConfigDialog"
            v-model="configContent"
            :language="getEditorLanguage(selectedFormat)"
            :options="monacoOptions"
            :readonly="true"
          />
        </div>

        <!-- 环境继承链信息 -->
        <div class="inheritance-info" v-if="inheritanceChain.length > 0">
          <el-alert
            title="环境继承链"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="inheritance-chain">
                <span>继承顺序：</span>
                <el-tag
                  v-for="(envId, index) in inheritanceChain"
                  :key="envId"
                  :type="envId === selectedEnvInfo?.id ? 'primary' : 'info'"
                  size="small"
                  style="margin-right: 8px;"
                >
                  {{ getEnvNameById(envId) }}
                  <el-icon v-if="index < inheritanceChain.length - 1" style="margin-left: 4px;">
                    <ArrowRight />
                  </el-icon>
                </el-tag>
                <span style="margin-left: 8px; color: #909399; font-size: 12px;">
                  （排序越大的环境会覆盖前面环境的配置）
                </span>
              </div>
            </template>
          </el-alert>
        </div>

        <!-- 配置统计信息 -->
        <div class="config-stats">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="配置项总数" :value="configStats.totalItems" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="加密配置" :value="configStats.encryptedItems" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="必填配置" :value="configStats.requiredItems" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="最后更新" :value="configStats.lastUpdated" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, Refresh, CopyDocument, Download, ArrowRight } from '@element-plus/icons-vue'
import { applicationApi, environmentApi, configApi } from '../api'
import MonacoEditor from '../components/MonacoEditor.vue'

const loading = ref(false)
const refreshing = ref(false)
const applications = ref([])
const environments = ref([])
const configs = ref([])
const selectedApp = ref('')
const selectedEnv = ref('')
const showConfigDialog = ref(false)
const selectedFormat = ref('yaml')
const configContent = ref('')
const selectedAppInfo = ref(null)
const selectedEnvInfo = ref(null)
const inheritanceChain = ref([])

const configStats = reactive({
  totalItems: 0,
  encryptedItems: 0,
  requiredItems: 0,
  lastUpdated: ''
})

const monacoOptions = {
  theme: 'vs',
  fontSize: 14,
  minimap: { enabled: true },
  scrollBeyondLastLine: false,
  automaticLayout: true,
  readOnly: true,
  wordWrap: 'on'
}

// 过滤后的应用列表
const filteredApplications = computed(() => {
  if (!selectedApp.value) {
    return applications.value
  }
  return applications.value.filter(app => app.id === selectedApp.value)
})

// 获取应用对应的环境
const getAppEnvironments = (appId) => {
  // 这里可以根据实际业务逻辑来过滤环境
  // 目前返回所有环境，您可以根据需要修改
  return environments.value
}

// 获取编辑器语言
const getEditorLanguage = (format) => {
  const languages = {
    'yaml': 'yaml',
    'json': 'json',
    'properties': 'properties'
  }
  return languages[format] || 'yaml'
}

// 加载应用列表
const loadApplications = async () => {
  try {
    const response = await applicationApi.getApplications({ size: 1000 })
    applications.value = response.data.content || []
  } catch (error) {
    console.error('加载应用列表失败:', error)
    ElMessage.error('加载应用列表失败')
  }
}

// 加载环境列表
const loadEnvironments = async () => {
  try {
    const response = await environmentApi.getEnvironments({ size: 1000 })
    environments.value = response.data.content || []
  } catch (error) {
    console.error('加载环境列表失败:', error)
    ElMessage.error('加载环境列表失败')
  }
}

// 选择应用
const selectApp = (app) => {
  selectedApp.value = app.id
  selectedAppInfo.value = app
}

// 选择环境
const selectEnvironment = (app, env) => {
  selectedEnv.value = env.id
  selectedEnvInfo.value = env
  selectedAppInfo.value = app
}

// 应用筛选变化
const handleAppChange = () => {
  selectedEnv.value = ''
  selectedEnvInfo.value = null
}

// 环境筛选变化
const handleEnvChange = () => {
  // 可以在这里添加环境筛选逻辑
}

// 查看配置
const viewConfig = async (app, env) => {
  selectedAppInfo.value = app
  selectedEnvInfo.value = env
  showConfigDialog.value = true
  await loadConfigContent(app.id, env.id)
  await loadInheritanceChain(app.id, env.id)
}

// 加载环境继承链
const loadInheritanceChain = async (appId, envId) => {
  try {
    const response = await configApi.getEnvironmentInheritanceChain(appId, envId)
    inheritanceChain.value = response.data || []
  } catch (error) {
    console.error('加载环境继承链失败:', error)
    inheritanceChain.value = []
  }
}

// 根据环境ID获取环境名称
const getEnvNameById = (envId) => {
  const env = environments.value.find(e => e.id === envId)
  return env ? env.envName : `环境${envId}`
}

// 加载配置内容
const loadConfigContent = async (appId, envId) => {
  try {
    refreshing.value = true
    
    // 获取合并后的配置列表（包含继承的配置）
    const response = await configApi.getMergedConfigs(appId, envId)
    
    const configItems = response.data || []
    
    // 更新统计信息
    configStats.totalItems = configItems.length
    configStats.encryptedItems = configItems.filter(item => item.isEncrypted === 1).length
    configStats.requiredItems = configItems.filter(item => item.isRequired === 1).length
    configStats.lastUpdated = configItems.length > 0 
      ? new Date(Math.max(...configItems.map(item => new Date(item.updatedAt || item.createdAt))))
      : '暂无数据'
    
    // 根据选择的格式生成配置内容
    await generateConfigContent(configItems)
    
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载配置失败')
    configContent.value = '# 配置加载失败\n# 请检查网络连接或联系管理员'
  } finally {
    refreshing.value = false
  }
}

// 生成配置内容
const generateConfigContent = async (configItems) => {
  try {
    switch (selectedFormat.value) {
      case 'yaml':
        configContent.value = generateYamlContent(configItems)
        break
      case 'json':
        configContent.value = generateJsonContent(configItems)
        break
      case 'properties':
        configContent.value = generatePropertiesContent(configItems)
        break
      default:
        configContent.value = generateYamlContent(configItems)
    }
  } catch (error) {
    console.error('生成配置内容失败:', error)
    configContent.value = '# 配置格式转换失败\n# 请检查配置数据格式'
  }
}

// 生成YAML格式
const generateYamlContent = (configItems) => {
  const config = {}
  
  configItems.forEach(item => {
    const keys = item.configKey.split('.')
    let current = config
    
    keys.forEach((key, index) => {
      if (index === keys.length - 1) {
        // 最后一个键，设置值
        if (item.isEncrypted === 1) {
          current[key] = '***加密***'
        } else {
          // 尝试解析值
          try {
            if (item.configType === 3) { // 布尔类型
              current[key] = item.configValue === 'true' || item.configValue === '1'
            } else if (item.configType === 2) { // 数字类型
              current[key] = parseFloat(item.configValue)
            } else {
              current[key] = item.configValue
            }
          } catch {
            current[key] = item.configValue
          }
        }
      } else {
        // 中间键，创建对象
        if (!current[key]) {
          current[key] = {}
        }
        current = current[key]
      }
    })
  })
  
  // 转换为YAML字符串
  return yamlDump(config)
}

// 生成JSON格式
const generateJsonContent = (configItems) => {
  const config = {}
  
  configItems.forEach(item => {
    const keys = item.configKey.split('.')
    let current = config
    
    keys.forEach((key, index) => {
      if (index === keys.length - 1) {
        if (item.isEncrypted === 1) {
          current[key] = '***加密***'
        } else {
          try {
            if (item.configType === 3) {
              current[key] = item.configValue === 'true' || item.configValue === '1'
            } else if (item.configType === 2) {
              current[key] = parseFloat(item.configValue)
            } else {
              current[key] = item.configValue
            }
          } catch {
            current[key] = item.configValue
          }
        }
      } else {
        if (!current[key]) {
          current[key] = {}
        }
        current = current[key]
      }
    })
  })
  
  return JSON.stringify(config, null, 2)
}

// 生成Properties格式
const generatePropertiesContent = (configItems) => {
  let content = `# ${selectedAppInfo.value?.appName} - ${selectedEnvInfo.value?.envName} 配置\n`
  content += `# 生成时间: ${new Date().toLocaleString()}\n\n`
  
  configItems.forEach(item => {
    if (item.description) {
      content += `# ${item.description}\n`
    }
    if (item.isEncrypted === 1) {
      content += `${item.configKey}=***加密***\n`
    } else {
      content += `${item.configKey}=${item.configValue}\n`
    }
    content += '\n'
  })
  
  return content
}

// 简单的YAML转换函数
const yamlDump = (obj, indent = 0) => {
  let result = ''
  const spaces = '  '.repeat(indent)
  
  for (const [key, value] of Object.entries(obj)) {
    if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
      result += `${spaces}${key}:\n${yamlDump(value, indent + 1)}`
    } else {
      if (typeof value === 'string') {
        result += `${spaces}${key}: "${value}"\n`
      } else {
        result += `${spaces}${key}: ${value}\n`
      }
    }
  }
  
  return result
}

// 格式变化处理
const handleFormatChange = async () => {
  if (selectedAppInfo.value && selectedEnvInfo.value) {
    await loadConfigContent(selectedAppInfo.value.id, selectedEnvInfo.value.id)
  }
}

// 刷新配置
const refreshConfig = async () => {
  if (selectedAppInfo.value && selectedEnvInfo.value) {
    await loadConfigContent(selectedAppInfo.value.id, selectedEnvInfo.value.id)
  }
}

// 复制配置
const copyConfig = async () => {
  try {
    await navigator.clipboard.writeText(configContent.value)
    ElMessage.success('配置已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

// 下载配置
const downloadConfig = () => {
  const blob = new Blob([configContent.value], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${selectedAppInfo.value?.appCode || 'app'}-${selectedEnvInfo.value?.envCode || 'env'}.${selectedFormat.value}`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('配置文件已下载')
}

onMounted(() => {
  loadApplications()
  loadEnvironments()
})
</script>

<style scoped>
.app-environments {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.app-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.app-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.app-card.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.app-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.app-info h3 {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 16px;
}

.app-code {
  margin: 0 0 5px 0;
  color: #909399;
  font-size: 12px;
  font-family: monospace;
}

.app-desc {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.environment-list {
  margin-top: 15px;
}

.env-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  margin-bottom: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.env-item:hover {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
}

.env-item.selected {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.env-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.env-icon {
  color: #409eff;
  font-size: 16px;
}

.env-name {
  font-weight: 500;
  color: #303133;
}

.env-code {
  color: #909399;
  font-size: 12px;
  font-family: monospace;
}

.config-viewer {
  height: 70vh;
  display: flex;
  flex-direction: column;
}

.format-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.format-actions {
  display: flex;
  gap: 10px;
}

.config-content {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.inheritance-info {
  margin-bottom: 15px;
}

.inheritance-chain {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.config-stats {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
}
</style> 