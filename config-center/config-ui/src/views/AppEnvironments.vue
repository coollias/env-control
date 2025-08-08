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
                <el-button size="small" type="warning" @click.stop="viewVersionDiff(app, env)">
                  版本对比
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
            <el-button @click="stageConfig" type="primary">
              <el-icon><DocumentAdd /></el-icon>
              暂存配置
            </el-button>
            <el-button @click="publishConfig" type="success">
              <el-icon><Upload /></el-icon>
              发布配置
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
                <el-statistic title="最后更新" :value="configStats.lastUpdated ? 1 : 0" />
                <div style="margin-top: 8px; font-size: 12px; color: #666;">
                  {{ configStats.lastUpdated || '暂无' }}
                </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>

    <!-- 版本对比对话框 -->
    <el-dialog
      v-model="showVersionDiffDialog"
      :title="`${selectedAppInfo?.appName} - ${selectedEnvInfo?.envName} 版本对比`"
      width="90%"
      top="3vh"
      class="version-diff-dialog"
    >
      <div class="version-diff-container">
        <!-- 版本选择器 -->
        <div class="version-selector">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="版本1">
                <el-select v-model="diffForm.version1" placeholder="选择版本1" @change="loadVersionDiff">
                  <el-option
                    v-for="version in versions"
                    :key="version.versionNumber"
                    :label="`${version.versionNumber} - ${version.versionName}`"
                    :value="version.versionNumber"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="版本2">
                <el-select v-model="diffForm.version2" placeholder="选择版本2" @change="loadVersionDiff">
                  <el-option
                    v-for="version in versions"
                    :key="version.versionNumber"
                    :label="`${version.versionNumber} - ${version.versionName}`"
                    :value="version.versionNumber"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- Diff组件 -->
        <div class="diff-container">
          <GitStyleDiff
            v-if="showVersionDiffDialog && diffForm.version1 && diffForm.version2"
            :version1="diffForm.version1"
            :version2="diffForm.version2"
            :diff-data="diffResult"
            :loading="diffLoading"
            @refresh="loadVersionDiff"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 暂存配置对话框 -->
    <el-dialog
      v-model="showStageDialog"
      title="暂存配置"
      width="60%"
      top="5vh"
    >
              <el-form :model="stageForm" :rules="stageRules" ref="stageFormRef" label-width="100px">
        <el-form-item label="快照名称" prop="snapshotName">
          <el-input v-model="stageForm.snapshotName" placeholder="请输入快照名称"></el-input>
        </el-form-item>
        <el-form-item label="快照描述" prop="snapshotDesc">
          <el-input 
            type="textarea" 
            v-model="stageForm.snapshotDesc" 
            placeholder="请输入快照描述"
            :rows="3">
          </el-input>
        </el-form-item>
        <el-form-item label="配置数据">
          <div class="config-preview">
            <el-alert
              title="当前配置预览"
              type="info"
              :closable="false"
              show-icon
            >
              <template #default>
                <div class="config-summary">
                  <p>应用：{{ selectedAppInfo?.appName }}</p>
                  <p>环境：{{ selectedEnvInfo?.envName }}</p>
                  <p>配置项数量：{{ configStats.totalItems }}</p>
                  <p>格式：{{ selectedFormat.toUpperCase() }}</p>
                </div>
              </template>
            </el-alert>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showStageDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmStageConfig" :loading="stagingLoading">
            暂存配置
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 发布配置对话框 -->
    <el-dialog
      v-model="showPublishDialog"
      title="发布配置"
      width="50%"
      top="5vh"
    >
              <el-form :model="publishForm" ref="publishFormRef" label-width="100px">
        <el-form-item label="选择快照">
          <el-select v-model="publishForm.snapshotId" placeholder="请选择要发布的快照">
            <el-option
              v-for="snapshot in stagedSnapshots"
              :key="snapshot.id"
              :label="`${snapshot.snapshotName} (${snapshot.versionNumber})`"
              :value="snapshot.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="发布人" prop="publishedBy">
          <el-input v-model="publishForm.publishedBy" placeholder="请输入发布人"></el-input>
        </el-form-item>
        <el-form-item label="推送方式">
          <el-radio-group v-model="publishForm.pushType">
            <el-radio label="all">推送到所有客户端</el-radio>
            <el-radio label="specific">推送到指定实例</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="目标实例" v-if="publishForm.pushType === 'specific'">
          <el-select v-model="publishForm.targetInstances" multiple placeholder="请选择目标实例">
            <el-option
              v-for="client in onlineClients"
              :key="client.instanceId"
              :label="`${client.instanceId} (${client.instanceIp})`"
              :value="client.instanceId">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showPublishDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmPublishConfig" :loading="publishingLoading">
            发布配置
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, Refresh, CopyDocument, Download, ArrowRight } from '@element-plus/icons-vue'
import { applicationApi, environmentApi, configApi, configVersionApi, configSnapshotApi, configPushApi } from '../api'
import MonacoEditor from '../components/MonacoEditor.vue'
import GitStyleDiff from '../components/GitStyleDiff.vue'

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
const showVersionDiffDialog = ref(false)
const versions = ref([])
const diffForm = reactive({
  version1: '',
  version2: ''
})
const diffResult = ref({})
const diffLoading = ref(false)

// 暂存和发布相关数据
const showStageDialog = ref(false)
const showPublishDialog = ref(false)
const stagingLoading = ref(false)
const publishingLoading = ref(false)
const stagedSnapshots = ref([])
const onlineClients = ref([])
const stageFormRef = ref()
const publishFormRef = ref()

const stageForm = reactive({
  snapshotName: '',
  snapshotDesc: ''
})

const publishForm = reactive({
  snapshotId: null,
  publishedBy: '',
  pushType: 'all',
  targetInstances: []
})

const stageRules = {
  snapshotName: [
    { required: true, message: '请输入快照名称', trigger: 'input' }
  ]
}

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

// 查看版本对比
const viewVersionDiff = async (app, env) => {
  selectedAppInfo.value = app
  selectedEnvInfo.value = env
  showVersionDiffDialog.value = true
  await loadVersions(app.id, env.id)
}

// 加载版本列表
const loadVersions = async (appId, envId) => {
  try {
    const response = await configVersionApi.getVersionsByAppAndEnv(appId, envId)
    versions.value = response.data || []
    
    // 如果有版本，默认选择最新的两个版本进行对比
    if (versions.value.length >= 2) {
      diffForm.version1 = versions.value[1].versionNumber
      diffForm.version2 = versions.value[0].versionNumber
      await loadVersionDiff()
    } else if (versions.value.length === 1) {
      diffForm.version2 = versions.value[0].versionNumber
    }
  } catch (error) {
    console.error('加载版本列表失败:', error)
    ElMessage.error('加载版本列表失败')
  }
}

// 加载版本差异
const loadVersionDiff = async () => {
  if (!diffForm.version1 || !diffForm.version2 || !selectedAppInfo.value || !selectedEnvInfo.value) {
    return
  }
  
  try {
    diffLoading.value = true
    const response = await configVersionApi.compareVersions(
      selectedAppInfo.value.id,
      selectedEnvInfo.value.id,
      diffForm.version1,
      diffForm.version2
    )
    diffResult.value = response.data.differences || {}
  } catch (error) {
    console.error('加载版本差异失败:', error)
    ElMessage.error('加载版本差异失败')
    diffResult.value = {}
  } finally {
    diffLoading.value = false
  }
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
      ? new Date(Math.max(...configItems.map(item => new Date(item.updatedAt || item.createdAt)))).toLocaleString()
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

// 暂存配置
const stageConfig = () => {
  if (!selectedAppInfo.value || !selectedEnvInfo.value) {
    ElMessage.warning('请先选择应用和环境')
    return
  }
  
  // 设置默认快照名称
  stageForm.snapshotName = `${selectedAppInfo.value.appName}-${selectedEnvInfo.value.envName}-${new Date().toLocaleDateString()}`
  stageForm.snapshotDesc = `${selectedAppInfo.value.appName} ${selectedEnvInfo.value.envName} 环境配置暂存`
  
  console.log('设置表单值:', stageForm.snapshotName, stageForm.snapshotDesc)
  showStageDialog.value = true
}

// 发布配置
const publishConfig = async () => {
  if (!selectedAppInfo.value || !selectedEnvInfo.value) {
    ElMessage.warning('请先选择应用和环境')
    return
  }
  
  try {
    // 加载暂存快照列表
    const response = await configSnapshotApi.getSnapshotsByAppAndEnv(
      selectedAppInfo.value.id, 
      selectedEnvInfo.value.id
    )
    stagedSnapshots.value = response.data.filter(s => s.snapshotType === 1)
    
    if (stagedSnapshots.value.length === 0) {
      ElMessage.warning('没有找到暂存快照，请先暂存配置')
      return
    }
    
    // 加载在线客户端
    await loadOnlineClients()
    
    showPublishDialog.value = true
  } catch (error) {
    ElMessage.error('加载快照列表失败')
  }
}

// 确认暂存配置
const confirmStageConfig = async () => {
  try {
    stagingLoading.value = true
    
    // 将当前配置转换为JSON格式
    const configData = parseConfigToJson(configContent.value, selectedFormat.value)
    
    const data = {
      appId: selectedAppInfo.value.id,
      envId: selectedEnvInfo.value.id,
      snapshotName: stageForm.snapshotName,
      snapshotDesc: stageForm.snapshotDesc,
      configData: configData,
      createdBy: 'admin' // 这里应该从用户信息获取
    }
    
    await configSnapshotApi.createStagedSnapshot(data)
    ElMessage.success('暂存配置成功')
    showStageDialog.value = false
    
    // 重置表单
    stageForm.snapshotName = ''
    stageForm.snapshotDesc = ''
  } catch (error) {
    ElMessage.error('暂存配置失败: ' + error.message)
  } finally {
    stagingLoading.value = false
  }
}

// 确认发布配置
const confirmPublishConfig = async () => {
  if (!publishForm.snapshotId) {
    ElMessage.warning('请选择要发布的快照')
    return
  }
  
  if (!publishForm.publishedBy) {
    ElMessage.warning('请输入发布人')
    return
  }
  
  try {
    publishingLoading.value = true
    
    // 发布快照
    await configSnapshotApi.publishSnapshot(
      publishForm.snapshotId, 
      { publishedBy: publishForm.publishedBy }
    )
    
    // 推送配置
    const pushData = {}
    if (publishForm.pushType === 'specific') {
      pushData.targetInstances = publishForm.targetInstances
    }
    
    await configPushApi.pushSnapshotConfig(publishForm.snapshotId, pushData)
    
    ElMessage.success('发布配置成功')
    showPublishDialog.value = false
    
    // 重置表单
    publishForm.snapshotId = null
    publishForm.publishedBy = ''
    publishForm.pushType = 'all'
    publishForm.targetInstances = []
  } catch (error) {
    ElMessage.error('发布配置失败: ' + error.message)
  } finally {
    publishingLoading.value = false
  }
}

// 加载在线客户端
const loadOnlineClients = async () => {
  if (!selectedAppInfo.value) return
  
  try {
    const response = await configPushApi.getOnlineClients(selectedAppInfo.value.id)
    onlineClients.value = response.data
  } catch (error) {
    console.error('加载在线客户端失败:', error)
  }
}

// 解析配置为JSON格式
const parseConfigToJson = (content, format) => {
  try {
    switch (format) {
      case 'json':
        return JSON.parse(content)
      case 'yaml':
        // 简单的YAML解析（支持基本的键值对和嵌套结构）
        return parseYamlToJson(content)
      case 'properties':
        // 解析properties格式
        const lines = content.split('\n')
        const result = {}
        lines.forEach(line => {
          const trimmed = line.trim()
          if (trimmed && !trimmed.startsWith('#')) {
            const index = trimmed.indexOf('=')
            if (index > 0) {
              const key = trimmed.substring(0, index).trim()
              const value = trimmed.substring(index + 1).trim()
              result[key] = value
            }
          }
        })
        return result
      default:
        return { content: content }
    }
  } catch (error) {
    console.error('解析配置失败:', error)
    return { content: content }
  }
}

// 简单的YAML解析函数
const parseYamlToJson = (yamlContent) => {
  const lines = yamlContent.split('\n')
  const result = {}
  const stack = []
  
  lines.forEach(line => {
    const trimmed = line.trim()
    if (!trimmed || trimmed.startsWith('#')) return
    
    // 计算缩进级别
    const indent = line.length - line.trimStart().length
    const level = Math.floor(indent / 2)
    
    // 调整栈的大小
    while (stack.length > level) {
      stack.pop()
    }
    
    // 解析键值对
    const colonIndex = trimmed.indexOf(':')
    if (colonIndex > 0) {
      const key = trimmed.substring(0, colonIndex).trim()
      const value = trimmed.substring(colonIndex + 1).trim()
      
      // 移除引号
      const cleanValue = value.replace(/^["']|["']$/g, '')
      
      // 构建嵌套路径
      let current = result
      stack.forEach(stackKey => {
        if (!current[stackKey]) {
          current[stackKey] = {}
        }
        current = current[stackKey]
      })
      
      if (value === '') {
        // 空值表示这是一个对象
        current[key] = {}
        stack.push(key)
      } else {
        // 有值表示这是一个叶子节点
        current[key] = cleanValue
      }
    }
  })
  
  return result
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

/* 版本对比对话框样式 */
.version-diff-dialog {
  .el-dialog__body {
    padding: 0;
  }
}

.version-diff-container {
  height: 80vh;
  display: flex;
  flex-direction: column;
}

.version-selector {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.diff-container {
  flex: 1;
  overflow: hidden;
  padding: 0;
}
</style> 