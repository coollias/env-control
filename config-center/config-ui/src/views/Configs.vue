<template>
  <div class="configs">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配置管理</span>
          <div class="header-buttons">
            <el-button type="warning" @click="showEditorDialog = true">
              <el-icon><Edit /></el-icon>
              编写配置
            </el-button>
            <el-button type="success" @click="showUploadDialog = true">
              <el-icon><Upload /></el-icon>
              导入配置
            </el-button>
            <el-button type="primary" @click="showCreateDialog = true">
              <el-icon><Plus /></el-icon>
              创建配置
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-select v-model="appFilter" placeholder="选择应用" clearable @change="handleSearch">
            <el-option
              v-for="app in applications"
              :key="app.id"
              :label="app.appName"
              :value="app.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="envFilter" placeholder="选择环境" clearable @change="handleSearch">
            <el-option
              v-for="env in environments"
              :key="env.id"
              :label="env.envName"
              :value="env.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索配置键"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-col>
      </el-row>

      <!-- 配置列表 -->
      <el-table :data="configs" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="configKey" label="配置键" width="200" />
        <el-table-column prop="configValue" label="配置值" width="300" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.isEncrypted === 1">***加密***</span>
            <span v-else>{{ scope.row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="configType" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getConfigTypeColor(scope.row.configType)">
              {{ getConfigTypeName(scope.row.configType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRequired" label="必填" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isRequired === 1 ? 'danger' : 'info'">
              {{ scope.row.isRequired === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isEncrypted" label="加密" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isEncrypted === 1 ? 'warning' : 'info'">
              {{ scope.row.isEncrypted === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="info" @click="handleVersionHistory(scope.row)">版本</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 20px; text-align: right;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingConfig ? '编辑配置' : '创建配置'"
      width="800px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="应用" prop="appId">
              <el-select v-model="form.appId" placeholder="选择应用" style="width: 100%">
                <el-option
                  v-for="app in applications"
                  :key="app.id"
                  :label="app.appName"
                  :value="app.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="环境" prop="envId">
              <el-select v-model="form.envId" placeholder="选择环境" style="width: 100%">
                <el-option
                  v-for="env in environments"
                  :key="env.id"
                  :label="env.envName"
                  :value="env.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" placeholder="请输入配置键" />
        </el-form-item>
        
        <el-form-item label="配置值" prop="configValue">
          <MonacoEditor
            v-if="showCreateDialog"
            v-model="form.configValue"
            :language="getMonacoLanguage(form.configType)"
            :options="monacoOptions"
          />
          <!-- 备用方案：如果MonacoEditor有问题，可以使用这个textarea -->
          <el-input
            v-else
            v-model="form.configValue"
            type="textarea"
            :rows="10"
            placeholder="请输入配置值"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="配置类型" prop="configType">
              <el-select v-model="form.configType" placeholder="选择类型" style="width: 100%">
                <el-option label="字符串" :value="1" />
                <el-option label="数字" :value="2" />
                <el-option label="布尔" :value="3" />
                <el-option label="JSON" :value="4" />
                <el-option label="YAML" :value="5" />
                <el-option label="Properties" :value="6" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否必填" prop="isRequired">
              <el-switch v-model="form.isRequired" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否加密" prop="isEncrypted">
              <el-switch v-model="form.isEncrypted" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="默认值" prop="defaultValue">
          <el-input
            v-model="form.defaultValue"
            type="textarea"
            :rows="3"
            placeholder="请输入默认值"
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 文件上传对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="导入配置文件"
      width="600px"
    >
      <el-form
        ref="uploadFormRef"
        :model="uploadForm"
        :rules="uploadRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="应用" prop="appId">
              <el-select v-model="uploadForm.appId" placeholder="选择应用" style="width: 100%">
                <el-option
                  v-for="app in applications"
                  :key="app.id"
                  :label="app.appName"
                  :value="app.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="环境" prop="envId">
              <el-select v-model="uploadForm.envId" placeholder="选择环境" style="width: 100%">
                <el-option
                  v-for="env in environments"
                  :key="env.id"
                  :label="env.envName"
                  :value="env.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="配置文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            :file-list="fileList"
            accept=".json,.yaml,.yml,.xml,.properties"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持的文件格式：JSON、YAML、XML、Properties
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showUploadDialog = false">取消</el-button>
          <el-button type="primary" @click="handleUpload" :loading="uploading">
            开始导入
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 版本历史对话框 -->
    <el-dialog
      v-model="showVersionDialog"
      :title="`配置项版本历史 - ${selectedConfigItem?.configKey}`"
      width="1000px"
    >
      <div class="version-history-timeline">
        <div v-if="versionHistory.length === 0" class="empty-state">
          <el-empty description="暂无版本历史记录" />
        </div>
        
        <div v-else class="timeline-container">
          <div class="timeline">
            <div 
              v-for="(version, index) in versionHistory" 
              :key="index" 
              class="timeline-item"
              :class="{ 'is-current': version.versionNumber === currentVersion }"
            >
              <!-- 时间线节点 -->
              <div class="timeline-node">
                <div class="node-icon" :class="getChangeTypeClass(version.changeType)">
                  <el-icon>
                    <component :is="getChangeTypeIcon(version.changeType)" />
                  </el-icon>
                </div>
                <div class="node-line" v-if="index < versionHistory.length - 1"></div>
              </div>

              <!-- 版本内容卡片 -->
              <div class="timeline-content">
                <div class="version-card">
                  <div class="version-header">
                    <div class="version-info">
                      <div class="version-number">
                        <el-tag type="primary" size="large">{{ version.versionNumber }}</el-tag>
                        <span v-if="version.versionNumber === currentVersion" class="current-badge">当前版本</span>
                      </div>
                      <div class="version-meta">
                        <span class="version-name">{{ version.versionName }}</span>
                        <el-tag :type="getChangeTypeColor(version.changeType)" size="small">
                          {{ getChangeTypeName(version.changeType) }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="version-actions">
                      <el-button 
                        size="small" 
                        type="warning" 
                        @click="handleRollback(version)"
                        :disabled="version.versionNumber === currentVersion"
                        text
                      >
                        <el-icon><RefreshLeft /></el-icon>
                        回滚到此版本
                      </el-button>
                    </div>
                  </div>

                  <div class="version-body">
                    <div class="version-desc">{{ version.versionDesc }}</div>
                    
                    <!-- 值变更对比 -->
                    <div class="value-comparison">
                      <div class="comparison-header">
                        <span class="comparison-title">配置值变更</span>
                      </div>
                      <div class="comparison-content">
                        <div class="old-value-section">
                          <div class="value-label">原值：</div>
                          <div class="value-content">
                            <el-input
                              v-model="version.oldValue"
                              type="textarea"
                              :rows="3"
                              readonly
                              placeholder="无"
                            />
                          </div>
                        </div>
                        <div class="arrow-icon">
                          <el-icon><ArrowRight /></el-icon>
                        </div>
                        <div class="new-value-section">
                          <div class="value-label">新值：</div>
                          <div class="value-content">
                            <el-input
                              v-model="version.newValue"
                              type="textarea"
                              :rows="3"
                              readonly
                              placeholder="无"
                            />
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="version-footer">
                    <div class="version-author">
                      <el-avatar :size="24" class="author-avatar">
                        {{ version.createdBy?.charAt(0)?.toUpperCase() }}
                      </el-avatar>
                      <span class="author-name">{{ version.createdBy }}</span>
                    </div>
                    <div class="version-time">
                      <el-icon><Clock /></el-icon>
                      {{ formatDate(version.createdAt) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 配置编辑器对话框 -->
    <el-dialog
      v-model="showEditorDialog"
      title="编写配置文件"
      width="900px"
    >
      <el-form
        ref="editorFormRef"
        :model="editorForm"
        :rules="editorRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="应用" prop="appId">
              <el-select v-model="editorForm.appId" placeholder="选择应用" style="width: 100%">
                <el-option
                  v-for="app in applications"
                  :key="app.id"
                  :label="app.appName"
                  :value="app.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="环境" prop="envId">
              <el-select v-model="editorForm.envId" placeholder="选择环境" style="width: 100%">
                <el-option
                  v-for="env in environments"
                  :key="env.id"
                  :label="env.envName"
                  :value="env.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="配置格式" prop="configType">
          <el-radio-group v-model="editorForm.configType" @change="handleConfigTypeChange">
            <el-radio label="yaml">YAML</el-radio>
            <el-radio label="json">JSON</el-radio>
            <el-radio label="properties">Properties</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="配置内容" prop="content">
          <MonacoEditor
            v-if="showEditorDialog"
            v-model="editorForm.content"
            :language="getEditorLanguage(editorForm.configType)"
            :options="monacoOptions"
          />
          <!-- 备用方案：如果MonacoEditor有问题，可以使用这个textarea -->
          <el-input
            v-else
            v-model="editorForm.content"
            type="textarea"
            :rows="15"
            placeholder="请输入配置内容"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button @click="loadTemplate" type="info">加载模板</el-button>
          <el-button @click="formatContent" type="warning">格式化</el-button>
          <el-button @click="validateContent" type="success">验证格式</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditorDialog = false">取消</el-button>
          <el-button type="primary" @click="handleEditorSubmit" :loading="editorLoading">
            保存配置
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Upload, UploadFilled, Edit, RefreshLeft, Clock, ArrowRight } from '@element-plus/icons-vue'
import { configApi, applicationApi, environmentApi } from '../api'
import MonacoEditor from '../components/MonacoEditor.vue'
import yaml from 'js-yaml'

const loading = ref(false)
const configs = ref([])
const applications = ref([])
const environments = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const appFilter = ref('')
const envFilter = ref('')
const searchKeyword = ref('')
const statusFilter = ref('')
const showCreateDialog = ref(false)
const showUploadDialog = ref(false)
const showEditorDialog = ref(false)
const showVersionDialog = ref(false)
const editingConfig = ref(null)
const selectedConfigItem = ref(null)
const versionHistory = ref([])
const currentVersion = ref('')
const formRef = ref()
const uploadFormRef = ref()
const editorFormRef = ref()
const uploadRef = ref()
const fileList = ref([])
const uploading = ref(false)
const editorLoading = ref(false)

const form = reactive({
  appId: '',
  envId: '',
  configKey: '',
  configValue: '',
  configType: 1,
  isRequired: false,
  isEncrypted: false,
  defaultValue: '',
  description: '',
  status: 1
})

const uploadForm = reactive({
  appId: '',
  envId: '',
  file: null
})

const uploadRules = {
  appId: [
    { required: true, message: '请选择应用', trigger: 'change' }
  ],
  envId: [
    { required: true, message: '请选择环境', trigger: 'change' }
  ],
  file: [
    { required: true, message: '请选择文件', trigger: 'change' }
  ]
}

const editorForm = reactive({
  appId: '',
  envId: '',
  configType: 'yaml',
  content: ''
})

const editorRules = {
  appId: [
    { required: true, message: '请选择应用', trigger: 'change' }
  ],
  envId: [
    { required: true, message: '请选择环境', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入配置内容', trigger: 'blur' }
  ]
}

const rules = {
  appId: [
    { required: true, message: '请选择应用', trigger: 'change' }
  ],
  envId: [
    { required: true, message: '请选择环境', trigger: 'change' }
  ],
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' },
    { min: 1, max: 128, message: '长度在 1 到 128 个字符', trigger: 'blur' }
  ]
}

const monacoOptions = {
  theme: 'vs',
  fontSize: 14,
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  automaticLayout: true
}

const getConfigTypeName = (type) => {
  const types = {
    1: '字符串',
    2: '数字',
    3: '布尔',
    4: 'JSON',
    5: 'YAML',
    6: 'Properties'
  }
  return types[type] || '未知'
}

const getConfigTypeColor = (type) => {
  const colors = {
    1: 'info',
    2: 'success',
    3: 'warning',
    4: 'primary',
    5: 'info',
    6: 'danger'
  }
  return colors[type] || 'info'
}

const getMonacoLanguage = (type) => {
  const languages = {
    1: 'plaintext',
    2: 'plaintext',
    3: 'plaintext',
    4: 'json',
    5: 'yaml',
    6: 'properties'
  }
  return languages[type] || 'plaintext'
}

const getEditorLanguage = (configType) => {
  const languages = {
    'yaml': 'yaml',
    'json': 'json',
    'properties': 'properties'
  }
  return languages[configType] || 'yaml'
}

const loadConfigs = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      appId: appFilter.value,
      envId: envFilter.value,
      keyword: searchKeyword.value,
      status: statusFilter.value
    }
    const response = await configApi.getConfigs(params)
    configs.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('加载配置列表失败:', error)
  } finally {
    loading.value = false
  }
}

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

const handleSearch = () => {
  currentPage.value = 1
  loadConfigs()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadConfigs()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadConfigs()
}

const handleEdit = (row) => {
  editingConfig.value = row
  Object.assign(form, {
    ...row,
    isRequired: row.isRequired === 1,
    isEncrypted: row.isEncrypted === 1
  })
  showCreateDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除配置 "${row.configKey}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await configApi.deleteConfig(row.id)
    ElMessage.success('删除成功')
    loadConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    const submitData = {
      ...form,
      isRequired: form.isRequired ? 1 : 0,
      isEncrypted: form.isEncrypted ? 1 : 0,
      createdBy: 'admin' // 这里应该从用户上下文获取
    }
    
    if (editingConfig.value) {
      await configApi.updateConfigWithVersion(editingConfig.value.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await configApi.createConfigWithVersion(submitData)
      ElMessage.success('创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    loadConfigs()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

const resetForm = () => {
  editingConfig.value = null
  Object.assign(form, {
    appId: '',
    envId: '',
    configKey: '',
    configValue: '',
    configType: 1,
    isRequired: false,
    isEncrypted: false,
    defaultValue: '',
    description: '',
    status: 1
  })
  formRef.value?.resetFields()
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

// 文件上传相关方法
const handleFileChange = (file) => {
  uploadForm.file = file.raw
}

const beforeUpload = (file) => {
  const isValidFormat = /\.(json|yaml|yml|xml|properties)$/i.test(file.name)
  if (!isValidFormat) {
    ElMessage.error('只支持上传 JSON、YAML、XML、Properties 格式的文件')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

const handleUpload = async () => {
  try {
    await uploadFormRef.value.validate()
    
    if (!uploadForm.file) {
      ElMessage.error('请选择要上传的文件')
      return
    }
    
    uploading.value = true
    
    const formData = new FormData()
    formData.append('file', uploadForm.file)
    formData.append('appId', uploadForm.appId)
    formData.append('envId', uploadForm.envId)
    
    const response = await configApi.uploadConfigFile(formData)
    ElMessage.success(response.message || '文件导入成功')
    
    // 重置表单
    uploadForm.appId = ''
    uploadForm.envId = ''
    uploadForm.file = null
    fileList.value = []
    showUploadDialog.value = false
    
    // 重新加载配置列表
    loadConfigs()
  } catch (error) {
    console.error('文件上传失败:', error)
    ElMessage.error('文件上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

// 编辑器相关方法
const handleConfigTypeChange = () => {
  // 当配置类型改变时，可以清空内容或加载对应模板
  if (editorForm.content.trim() === '') {
    loadTemplate()
  }
}

const loadTemplate = () => {
  const templates = {
    yaml: `# 应用配置模板
database:
  url: jdbc:mysql://localhost:3306/myapp
  username: root
  password: password
  pool:
    maxConnections: 20
    minConnections: 5
    timeout: 30000

redis:
  host: localhost
  port: 6379
  password: ""
  database: 0
  timeout: 2000

logging:
  level: INFO
  file: /var/log/app.log
  maxSize: 100MB
  retention: 30d

security:
  jwt:
    secret: your-secret-key
    expiration: 86400
  cors:
    allowedOrigins:
      - http://localhost:3000
      - https://yourdomain.com
    allowedMethods:
      - GET
      - POST
      - PUT
      - DELETE

features:
  enableCache: true
  enableMetrics: true
  enableHealthCheck: true`,
    
    json: `{
  "database": {
    "url": "jdbc:mysql://localhost:3306/myapp",
    "username": "root",
    "password": "password",
    "pool": {
      "maxConnections": 20,
      "minConnections": 5,
      "timeout": 30000
    }
  },
  "redis": {
    "host": "localhost",
    "port": 6379,
    "password": "",
    "database": 0,
    "timeout": 2000
  },
  "logging": {
    "level": "INFO",
    "file": "/var/log/app.log",
    "maxSize": "100MB",
    "retention": "30d"
  },
  "security": {
    "jwt": {
      "secret": "your-secret-key",
      "expiration": 86400
    },
    "cors": {
      "allowedOrigins": ["http://localhost:3000", "https://yourdomain.com"],
      "allowedMethods": ["GET", "POST", "PUT", "DELETE"]
    }
  },
  "features": {
    "enableCache": true,
    "enableMetrics": true,
    "enableHealthCheck": true
  }
}`,
    
    properties: `# 应用配置模板
# Database Configuration
database.url=jdbc:mysql://localhost:3306/myapp
database.username=root
database.password=password
database.pool.maxConnections=20
database.pool.minConnections=5
database.pool.timeout=30000

# Redis Configuration
redis.host=localhost
redis.port=6379
redis.password=
redis.database=0
redis.timeout=2000

# Logging Configuration
logging.level=INFO
logging.file=/var/log/app.log
logging.maxSize=100MB
logging.retention=30d

# Security Configuration
security.jwt.secret=your-secret-key
security.jwt.expiration=86400
security.cors.allowedOrigins=http://localhost:3000,https://yourdomain.com
security.cors.allowedMethods=GET,POST,PUT,DELETE

# Feature Flags
features.enableCache=true
features.enableMetrics=true
features.enableHealthCheck=true`
  }
  
  editorForm.content = templates[editorForm.configType] || templates.yaml
}

const formatContent = () => {
  try {
    if (editorForm.configType === 'json') {
      const parsed = JSON.parse(editorForm.content)
      editorForm.content = JSON.stringify(parsed, null, 2)
      ElMessage.success('JSON格式化成功')
    } else if (editorForm.configType === 'yaml') {
      // 使用js-yaml库进行YAML格式化
      const parsed = yaml.load(editorForm.content)
      editorForm.content = yaml.dump(parsed, {
        indent: 2,
        lineWidth: 80,
        noRefs: true
      })
      ElMessage.success('YAML格式化成功')
    } else {
      ElMessage.info('Properties格式无需格式化')
    }
  } catch (error) {
    ElMessage.error('格式化失败: ' + error.message)
  }
}

const validateContent = () => {
  try {
    if (editorForm.configType === 'json') {
      JSON.parse(editorForm.content)
      ElMessage.success('JSON格式正确')
    } else if (editorForm.configType === 'yaml') {
      // 使用js-yaml库进行YAML验证
      yaml.load(editorForm.content)
      ElMessage.success('YAML格式正确')
    } else {
      ElMessage.success('Properties格式验证通过')
    }
  } catch (error) {
    ElMessage.error('格式验证失败: ' + error.message)
  }
}

const handleEditorSubmit = async () => {
  try {
    await editorFormRef.value.validate()
    
    if (!editorForm.content.trim()) {
      ElMessage.error('请输入配置内容')
      return
    }
    
    editorLoading.value = true
    
    // 创建FormData对象，模拟文件上传
    const formData = new FormData()
    
    // 创建Blob对象，模拟文件
    const blob = new Blob([editorForm.content], { 
      type: `text/${editorForm.configType}` 
    })
    
    // 创建File对象
    const file = new File([blob], `config.${editorForm.configType}`, { 
      type: `text/${editorForm.configType}` 
    })
    
    formData.append('file', file)
    formData.append('appId', editorForm.appId)
    formData.append('envId', editorForm.envId)
    
    const response = await configApi.uploadConfigFile(formData)
    ElMessage.success(response.message || '配置保存成功')
    
    // 重置表单
    editorForm.appId = ''
    editorForm.envId = ''
    editorForm.content = ''
    showEditorDialog.value = false
    
    // 重新加载配置列表
    loadConfigs()
  } catch (error) {
    console.error('配置保存失败:', error)
    ElMessage.error('配置保存失败: ' + (error.message || '未知错误'))
  } finally {
    editorLoading.value = false
  }
}

// 版本管理相关方法
const handleVersionHistory = async (configItem) => {
  try {
    selectedConfigItem.value = configItem
    currentVersion.value = configItem.versionNumber || ''
    
    const response = await configApi.getConfigVersionHistory(
      configItem.appId, 
      configItem.envId, 
      configItem.configKey
    )
    
    versionHistory.value = response.data || []
    showVersionDialog.value = true
  } catch (error) {
    console.error('获取版本历史失败:', error)
    ElMessage.error('获取版本历史失败')
  }
}

const handleRollback = async (versionItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要回滚配置项 "${selectedConfigItem.value.configKey}" 到版本 "${versionItem.versionNumber}" 吗？`,
      '确认回滚',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await configApi.rollbackConfigToVersion(
      selectedConfigItem.value.appId,
      selectedConfigItem.value.envId,
      selectedConfigItem.value.configKey,
      versionItem.versionNumber,
      { createdBy: 'admin' } // 这里应该从用户上下文获取
    )
    
    ElMessage.success('回滚成功')
    showVersionDialog.value = false
    loadConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('回滚失败:', error)
      ElMessage.error('回滚失败')
    }
  }
}

const getChangeTypeName = (type) => {
  const types = {
    1: '新增',
    2: '修改',
    3: '删除'
  }
  return types[type] || '未知'
}

const getChangeTypeColor = (type) => {
  const colors = {
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return colors[type] || 'info'
}

// 获取变更类型图标
const getChangeTypeIcon = (type) => {
  const icons = {
    1: 'Plus',
    2: 'Edit',
    3: 'Delete'
  }
  return icons[type] || 'InfoFilled'
}

// 获取变更类型CSS类
const getChangeTypeClass = (type) => {
  const classes = {
    1: 'type-add',
    2: 'type-edit',
    3: 'type-delete'
  }
  return classes[type] || 'type-default'
}

onMounted(() => {
  loadConfigs()
  loadApplications()
  loadEnvironments()
})
</script>

<style scoped>
.configs {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 版本历史时间线样式 */
.version-history-timeline {
  max-height: 600px;
  overflow-y: auto;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.timeline-container {
  position: relative;
  padding: 20px 0;
}

.timeline {
  position: relative;
}

.timeline-item {
  display: flex;
  margin-bottom: 30px;
  position: relative;
}

.timeline-item.is-current .version-card {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.timeline-node {
  position: relative;
  margin-right: 20px;
  flex-shrink: 0;
}

.node-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  position: relative;
  z-index: 2;
}

.node-icon.type-add {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.node-icon.type-edit {
  background: linear-gradient(135deg, #e6a23c, #f0c78a);
}

.node-icon.type-delete {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.node-icon.type-default {
  background: linear-gradient(135deg, #909399, #c0c4cc);
}

.node-line {
  position: absolute;
  top: 50px;
  left: 50%;
  width: 2px;
  height: 30px;
  background: linear-gradient(to bottom, #e4e7ed, transparent);
  transform: translateX(-50%);
}

.timeline-content {
  flex: 1;
  min-width: 0;
}

.version-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
  position: relative;
}

.version-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.version-info {
  flex: 1;
}

.version-number {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.current-badge {
  background: #67c23a;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.version-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.version-name {
  font-weight: 500;
  color: #303133;
}

.version-actions {
  display: flex;
  gap: 5px;
}

.version-body {
  margin-bottom: 15px;
}

.version-desc {
  color: #606266;
  margin-bottom: 15px;
  line-height: 1.5;
}

.value-comparison {
  background: #fafafa;
  border-radius: 8px;
  padding: 15px;
}

.comparison-header {
  margin-bottom: 15px;
}

.comparison-title {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.comparison-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.old-value-section,
.new-value-section {
  flex: 1;
}

.value-label {
  font-weight: 500;
  margin-bottom: 8px;
  color: #606266;
  font-size: 13px;
}

.value-content {
  flex: 1;
}

.arrow-icon {
  color: #409eff;
  font-size: 20px;
  margin: 0 10px;
  flex-shrink: 0;
}

.version-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.version-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  background: #409eff;
  color: white;
  font-weight: 500;
}

.author-name {
  color: #606266;
  font-size: 14px;
}

.version-time {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
  font-size: 13px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .timeline-item {
    flex-direction: column;
  }
  
  .timeline-node {
    margin-right: 0;
    margin-bottom: 15px;
    align-self: center;
  }
  
  .version-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .version-actions {
    align-self: flex-end;
  }
  
  .comparison-content {
    flex-direction: column;
    gap: 10px;
  }
  
  .arrow-icon {
    transform: rotate(90deg);
  }
}
</style> 