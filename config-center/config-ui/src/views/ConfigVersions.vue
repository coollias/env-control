<template>
  <div class="config-versions">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配置版本管理</span>
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
            <el-button type="primary" @click="showCreateDialog = true" :disabled="!selectedApp || !selectedEnv">
              <el-icon><Plus /></el-icon>
              创建版本
            </el-button>
          </div>
        </div>
      </template>

      <!-- 版本时间线 -->
      <div class="version-timeline" v-loading="loading">
        <div v-if="versions.length === 0" class="empty-state">
          <el-empty description="暂无版本记录" />
        </div>
        
        <div v-else class="timeline-container">
          <div class="timeline">
            <div 
              v-for="(version, index) in versions" 
              :key="version.id" 
              class="timeline-item"
              :class="{ 'is-latest': index === 0 }"
            >
              <!-- 时间线节点 -->
              <div class="timeline-node">
                <div class="node-icon" :class="getChangeTypeClass(version.changeType)">
                  <el-icon>
                    <component :is="getChangeTypeIcon(version.changeType)" />
                  </el-icon>
                </div>
                <div class="node-line" v-if="index < versions.length - 1"></div>
              </div>

              <!-- 版本内容卡片 -->
              <div class="timeline-content">
                <div class="version-card">
                  <div class="version-header">
                    <div class="version-info">
                      <div class="version-number">
                        <el-tag type="primary" size="large">{{ version.versionNumber }}</el-tag>
                        <span v-if="index === 0" class="latest-badge">最新</span>
                      </div>
                      <div class="version-meta">
                        <span class="version-name">{{ version.versionName }}</span>
                        <el-tag :type="getChangeTypeColor(version.changeType)" size="small">
                          {{ getChangeTypeName(version.changeType) }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="version-actions">
                      <el-button size="small" @click="viewChanges(version)" text>
                        <el-icon><View /></el-icon>
                        查看变更
                      </el-button>
                      <el-button size="small" type="warning" @click="rollbackVersion(version)" text>
                        <el-icon><RefreshLeft /></el-icon>
                        回滚
                      </el-button>
                      <el-button size="small" type="danger" @click="deleteVersion(version)" text>
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-button>
                    </div>
                  </div>

                  <div class="version-body">
                    <div class="version-desc">{{ version.versionDesc }}</div>
                    <div class="version-summary">{{ version.changeSummary }}</div>
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

        <!-- 分页 -->
        <div style="margin-top: 30px; text-align: center;">
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
      </div>
    </el-card>

    <!-- 创建版本对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建配置版本"
      width="600px"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
      >
        <el-form-item label="版本名称" prop="versionName">
          <el-input v-model="createForm.versionName" placeholder="请输入版本名称" />
        </el-form-item>
        <el-form-item label="版本描述" prop="versionDesc">
          <el-input
            v-model="createForm.versionDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入版本描述"
          />
        </el-form-item>
        <el-form-item label="变更类型" prop="changeType">
          <el-radio-group v-model="createForm.changeType">
            <el-radio :label="1">新增</el-radio>
            <el-radio :label="2">修改</el-radio>
            <el-radio :label="3">删除</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="变更内容" prop="changes">
          <el-input
            v-model="createForm.changes"
            type="textarea"
            :rows="8"
            placeholder="请输入变更内容（JSON格式）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleCreateVersion">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看变更对话框 -->
    <el-dialog
      v-model="showChangesDialog"
      :title="`版本 ${selectedVersion?.versionNumber} 变更详情`"
      width="900px"
    >
      <div class="changes-timeline">
        <div 
          v-for="change in versionChanges" 
          :key="change.id" 
          class="change-item"
        >
          <div class="change-header">
            <div class="change-key">
              <el-tag type="info">{{ change.configKey }}</el-tag>
            </div>
            <div class="change-type">
              <el-tag :type="getChangeTypeColor(change.changeType)" size="small">
                {{ getChangeTypeName(change.changeType) }}
              </el-tag>
            </div>
          </div>
          
          <div class="change-content">
            <div class="change-values">
              <div class="old-value">
                <div class="value-label">原值：</div>
                <div class="value-content">
                  <el-input
                    v-model="change.oldValue"
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
              <div class="new-value">
                <div class="value-label">新值：</div>
                <div class="value-content">
                  <el-input
                    v-model="change.newValue"
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
      </div>
    </el-dialog>

    <!-- 版本比较对话框 -->
    <el-dialog
      v-model="showCompareDialog"
      title="版本比较"
      width="900px"
    >
      <div class="compare-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="版本1">
              <el-select v-model="compareForm.version1" placeholder="选择版本1">
                <el-option
                  v-for="version in versions"
                  :key="version.versionNumber"
                  :label="version.versionNumber"
                  :value="version.versionNumber"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本2">
              <el-select v-model="compareForm.version2" placeholder="选择版本2">
                <el-option
                  v-for="version in versions"
                  :key="version.versionNumber"
                  :label="version.versionNumber"
                  :value="version.versionNumber"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-button type="primary" @click="compareVersions" :disabled="!compareForm.version1 || !compareForm.version2">
          比较版本
        </el-button>
      </div>

      <!-- 比较结果 -->
      <div v-if="compareResult" class="compare-result">
        <h4>比较结果</h4>
        <el-table :data="compareResultList" style="width: 100%">
          <el-table-column prop="configKey" label="配置键" width="200" />
          <el-table-column prop="version1Value" label="版本1值" show-overflow-tooltip />
          <el-table-column prop="version2Value" label="版本2值" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'different' ? 'danger' : 'success'">
                {{ scope.row.status === 'different' ? '不同' : '相同' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, RefreshLeft, Delete, Clock, ArrowRight } from '@element-plus/icons-vue'
import { applicationApi, environmentApi, configVersionApi } from '../api'

const loading = ref(false)
const applications = ref([])
const environments = ref([])
const versions = ref([])
const selectedApp = ref('')
const selectedEnv = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showCreateDialog = ref(false)
const showChangesDialog = ref(false)
const showCompareDialog = ref(false)
const selectedVersion = ref(null)
const versionChanges = ref([])
const compareResult = ref(null)
const compareResultList = ref([])

const createForm = reactive({
  versionName: '',
  versionDesc: '',
  changeType: 2,
  changes: ''
})

const createRules = {
  versionName: [
    { required: true, message: '请输入版本名称', trigger: 'blur' }
  ],
  changeType: [
    { required: true, message: '请选择变更类型', trigger: 'change' }
  ]
}

const compareForm = reactive({
  version1: '',
  version2: ''
})

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

// 加载版本列表
const loadVersions = async () => {
  if (!selectedApp.value || !selectedEnv.value) {
    versions.value = []
    return
  }

  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const response = await configVersionApi.getVersionsByAppAndEnvPage(selectedApp.value, selectedEnv.value, params)
    versions.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('加载版本列表失败:', error)
    ElMessage.error('加载版本列表失败')
  } finally {
    loading.value = false
  }
}

// 应用筛选变化
const handleAppChange = () => {
  currentPage.value = 1
  loadVersions()
}

// 环境筛选变化
const handleEnvChange = () => {
  currentPage.value = 1
  loadVersions()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  loadVersions()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadVersions()
}

// 获取变更类型名称
const getChangeTypeName = (type) => {
  const types = {
    1: '新增',
    2: '修改',
    3: '删除'
  }
  return types[type] || '未知'
}

// 获取变更类型颜色
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

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

// 查看变更
const viewChanges = async (version) => {
  try {
    selectedVersion.value = version
    const response = await configVersionApi.getVersionChanges(version.id)
    versionChanges.value = response.data || []
    showChangesDialog.value = true
  } catch (error) {
    console.error('获取变更详情失败:', error)
    ElMessage.error('获取变更详情失败')
  }
}

// 回滚版本
const rollbackVersion = async (version) => {
  try {
    await ElMessageBox.confirm(
      `确定要回滚到版本 "${version.versionNumber}" 吗？`,
      '确认回滚',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await configVersionApi.rollbackToVersion(
      selectedApp.value, 
      selectedEnv.value, 
      version.versionNumber,
      { createdBy: 'admin' } // 这里应该从用户上下文获取
    )
    
    ElMessage.success('回滚成功')
    loadVersions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('回滚失败:', error)
      ElMessage.error('回滚失败')
    }
  }
}

// 删除版本
const deleteVersion = async (version) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除版本 "${version.versionNumber}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await configVersionApi.deleteVersion(version.id)
    ElMessage.success('删除成功')
    loadVersions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 创建版本
const handleCreateVersion = async () => {
  try {
    // 解析变更内容
    let changes = {}
    try {
      changes = JSON.parse(createForm.changes)
    } catch (e) {
      ElMessage.error('变更内容格式不正确，请使用有效的JSON格式')
      return
    }
    
    const data = {
      versionName: createForm.versionName,
      versionDesc: createForm.versionDesc,
      createdBy: 'admin', // 这里应该从用户上下文获取
      changes: changes,
      changeType: createForm.changeType
    }
    
    await configVersionApi.createVersionWithChanges(selectedApp.value, selectedEnv.value, data)
    ElMessage.success('版本创建成功')
    showCreateDialog.value = false
    loadVersions()
    
    // 重置表单
    Object.assign(createForm, {
      versionName: '',
      versionDesc: '',
      changeType: 2,
      changes: ''
    })
  } catch (error) {
    console.error('创建版本失败:', error)
    ElMessage.error('创建版本失败')
  }
}

// 比较版本
const compareVersions = async () => {
  try {
    const response = await configVersionApi.compareVersions(
      selectedApp.value,
      selectedEnv.value,
      compareForm.version1,
      compareForm.version2
    )
    
    compareResult.value = response.data
    compareResultList.value = Object.entries(response.data.differences).map(([key, diff]) => ({
      configKey: key,
      version1Value: diff.version1 || '',
      version2Value: diff.version2 || '',
      status: 'different'
    }))
    
    ElMessage.success('版本比较完成')
  } catch (error) {
    console.error('版本比较失败:', error)
    ElMessage.error('版本比较失败')
  }
}

onMounted(() => {
  loadApplications()
  loadEnvironments()
})
</script>

<style scoped>
.config-versions {
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
  align-items: center;
}

.version-timeline {
  margin-top: 20px;
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

.timeline-item.is-latest .version-card {
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

.latest-badge {
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
  margin-bottom: 8px;
  line-height: 1.5;
}

.version-summary {
  color: #909399;
  font-size: 13px;
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

/* 变更详情样式 */
.changes-timeline {
  max-height: 500px;
  overflow-y: auto;
}

.change-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
}

.change-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.change-key {
  font-weight: 500;
}

.change-content {
  background: white;
  border-radius: 6px;
  padding: 15px;
}

.change-values {
  display: flex;
  align-items: center;
  gap: 15px;
}

.old-value,
.new-value {
  flex: 1;
}

.value-label {
  font-weight: 500;
  margin-bottom: 8px;
  color: #606266;
}

.value-content {
  flex: 1;
}

.arrow-icon {
  color: #409eff;
  font-size: 20px;
  margin: 0 10px;
}

.compare-form {
  margin-bottom: 20px;
}

.compare-result {
  margin-top: 20px;
}

.compare-result h4 {
  margin-bottom: 15px;
  color: #303133;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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
  
  .change-values {
    flex-direction: column;
    gap: 10px;
  }
  
  .arrow-icon {
    transform: rotate(90deg);
  }
}
</style> 