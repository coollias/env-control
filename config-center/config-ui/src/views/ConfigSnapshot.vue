<template>
  <div class="config-snapshot">
    <div class="page-header">
      <h2>配置快照管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showStagedDialog = true">
          <i class="el-icon-plus"></i> 暂存配置
        </el-button>
        <el-button type="success" @click="showPublishDialog = true" :disabled="!selectedSnapshot">
          <i class="el-icon-upload"></i> 发布配置
        </el-button>
        <el-button type="warning" @click="openCompareDialog" :disabled="snapshots.length < 2">
          <i class="el-icon-s-operation"></i> 版本对比
        </el-button>
      </div>
    </div>

    <!-- 应用环境选择 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-select v-model="selectedApp" placeholder="选择应用" @change="loadSnapshots">
            <el-option
              v-for="app in applications"
              :key="app.id"
              :label="app.appName"
              :value="app.id || ''">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-select v-model="selectedEnv" placeholder="选择环境" @change="loadSnapshots">
            <el-option
              v-for="env in environments"
              :key="env.id"
              :label="env.envName"
              :value="env.id || ''">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-select v-model="snapshotType" placeholder="快照类型" @change="loadSnapshots">
            <el-option label="全部" value=""></el-option>
            <el-option label="暂存" value="1"></el-option>
            <el-option label="发布" value="2"></el-option>
          </el-select>
        </el-col>
      </el-row>
    </div>

    <!-- 快照列表 -->
    <div class="snapshot-list">
      <el-table
        :data="snapshots"
        @selection-change="handleSelectionChange"
        v-loading="loading">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="versionNumber" label="版本号" width="120"></el-table-column>
        <el-table-column prop="snapshotName" label="快照名称" width="200"></el-table-column>
        <el-table-column prop="snapshotDesc" label="描述" show-overflow-tooltip></el-table-column>
        <el-table-column prop="configCount" label="配置项数量" width="120"></el-table-column>
        <el-table-column prop="snapshotType" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.snapshotType === 1 ? 'warning' : 'success'">
              {{ scope.row.snapshotType === 1 ? '暂存' : '发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdBy" label="创建人" width="100"></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="mini" @click="viewSnapshot(scope.row)">查看</el-button>
            <el-button 
              size="mini" 
              type="success" 
              @click="publishSnapshot(scope.row)"
              v-if="scope.row.snapshotType === 1">
              发布
            </el-button>
            <el-button 
              size="mini" 
              type="warning" 
              @click="compareSnapshot(scope.row)">
              对比
            </el-button>
            <el-button 
              size="mini" 
              type="danger" 
              @click="deleteSnapshot(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
      </div>
    </div>

    <!-- 暂存配置对话框 -->
    <el-dialog title="暂存配置" v-model="showStagedDialog" width="60%">
      <el-form :model="stagedForm" :rules="stagedRules" ref="stagedForm" label-width="100px">
        <el-form-item label="快照名称" prop="snapshotName">
          <el-input v-model="stagedForm.snapshotName" placeholder="请输入快照名称"></el-input>
        </el-form-item>
        <el-form-item label="快照描述" prop="snapshotDesc">
          <el-input 
            type="textarea" 
            v-model="stagedForm.snapshotDesc" 
            placeholder="请输入快照描述"
            :rows="3">
          </el-input>
        </el-form-item>
        <el-form-item label="配置数据">
          <div class="config-editor">
            <el-input
              type="textarea"
              v-model="stagedForm.configDataJson"
              placeholder="请输入JSON格式的配置数据"
              :rows="15"
              @input="validateJson">
            </el-input>
            <div class="json-error" v-if="jsonError">{{ jsonError }}</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showStagedDialog = false">取消</el-button>
          <el-button type="primary" @click="createStagedSnapshot" :loading="stagingLoading">
            暂存配置
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 发布配置对话框 -->
    <el-dialog title="发布配置" v-model="showPublishDialog" width="50%">
      <el-form :model="publishForm" ref="publishForm" label-width="100px">
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
          <el-button type="primary" @click="publishSnapshot" :loading="publishingLoading">
            发布配置
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 快照详情对话框 -->
    <el-dialog title="快照详情" v-model="showDetailDialog" width="70%">
      <div v-if="selectedSnapshotDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="版本号">{{ selectedSnapshotDetail.versionNumber }}</el-descriptions-item>
          <el-descriptions-item label="快照名称">{{ selectedSnapshotDetail.snapshotName }}</el-descriptions-item>
          <el-descriptions-item label="快照描述">{{ selectedSnapshotDetail.snapshotDesc }}</el-descriptions-item>
          <el-descriptions-item label="配置项数量">{{ selectedSnapshotDetail.configCount }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ selectedSnapshotDetail.createdBy }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(selectedSnapshotDetail.createdAt) }}</el-descriptions-item>
        </el-descriptions>

        <div class="config-items" style="margin-top: 20px;">
          <h4>配置项详情</h4>
          <el-table :data="snapshotItems" border>
            <el-table-column prop="configKey" label="配置键" width="200"></el-table-column>
            <el-table-column prop="configValue" label="配置值" show-overflow-tooltip></el-table-column>
            <el-table-column prop="configType" label="类型" width="100">
              <template #default="scope">
                {{ getConfigTypeName(scope.row.configType) }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 版本比较对话框 -->
    <el-dialog title="快照版本对比" v-model="showCompareDialog" width="80%">
      <div class="compare-section">
        <div class="compare-header" style="margin-bottom: 20px;">
          <el-alert 
            title="选择要对比的两个快照版本" 
            type="info" 
            :closable="false"
            show-icon>
          </el-alert>
        </div>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="compare-selector">
              <h4>源快照</h4>
              <el-select 
                v-model="compareForm.snapshotId1" 
                placeholder="选择源快照"
                @change="onSnapshot1Change"
                style="width: 100%;">
                <el-option
                  v-for="snapshot in snapshots"
                  :key="snapshot.id"
                  :label="`${snapshot.snapshotName} (${snapshot.versionNumber})`"
                  :value="snapshot.id">
                  <span style="float: left">{{ snapshot.snapshotName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ snapshot.versionNumber }}
                  </span>
                </el-option>
              </el-select>
              <div v-if="compareSnapshot1" class="snapshot-info" style="margin-top: 10px; padding: 10px; background: #f5f7fa; border-radius: 4px;">
                <p><strong>版本号:</strong> {{ compareSnapshot1.versionNumber }}</p>
                <p><strong>类型:</strong> {{ compareSnapshot1.snapshotType === 1 ? '暂存' : '发布' }}</p>
                <p><strong>创建时间:</strong> {{ formatDate(compareSnapshot1.createdAt) }}</p>
                <p><strong>创建人:</strong> {{ compareSnapshot1.createdBy }}</p>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="compare-selector">
              <h4>目标快照</h4>
              <el-select 
                v-model="compareForm.snapshotId2" 
                placeholder="选择目标快照"
                @change="onSnapshot2Change"
                style="width: 100%;">
                <el-option
                  v-for="snapshot in snapshots"
                  :key="snapshot.id"
                  :label="`${snapshot.snapshotName} (${snapshot.versionNumber})`"
                  :value="snapshot.id">
                  <span style="float: left">{{ snapshot.snapshotName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ snapshot.versionNumber }}
                  </span>
                </el-option>
              </el-select>
              <div v-if="compareSnapshot2" class="snapshot-info" style="margin-top: 10px; padding: 10px; background: #f5f7fa; border-radius: 4px;">
                <p><strong>版本号:</strong> {{ compareSnapshot2.versionNumber }}</p>
                <p><strong>类型:</strong> {{ compareSnapshot2.snapshotType === 1 ? '暂存' : '发布' }}</p>
                <p><strong>创建时间:</strong> {{ formatDate(compareSnapshot2.createdAt) }}</p>
                <p><strong>创建人:</strong> {{ compareSnapshot2.createdBy }}</p>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <div class="compare-actions" style="margin: 20px 0; text-align: center;">
          <el-button 
            type="primary" 
            @click="compareSnapshots" 
            :loading="comparingLoading"
            :disabled="!compareForm.snapshotId1 || !compareForm.snapshotId2 || compareForm.snapshotId1 === compareForm.snapshotId2">
            开始对比
          </el-button>
          <el-button @click="clearCompareResult" v-if="compareResult">清除结果</el-button>
        </div>

        <div v-if="compareResult" class="compare-result">
          <el-divider content-position="left">对比结果</el-divider>
          <div class="compare-summary" style="margin-bottom: 20px;">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-statistic title="总配置项" :value="compareResult.totalCount || 0" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="相同项" :value="compareResult.sameCount || 0" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="差异项" :value="compareResult.diffCount || 0" />
              </el-col>
            </el-row>
          </div>
          
          <el-table :data="compareResult.differences" border stripe>
            <el-table-column prop="configKey" label="配置键" width="200" fixed="left">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.hasDiff ? 'danger' : 'success'">
                  {{ scope.row.configKey }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="源快照值" width="250">
              <template #default="scope">
                <div :class="{ 'diff-value': scope.row.hasDiff }">
                  <el-tag v-if="scope.row.snapshot1 === null || scope.row.snapshot1 === undefined" size="small" type="info">无</el-tag>
                  <span v-else>{{ scope.row.snapshot1 }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="目标快照值" width="250">
              <template #default="scope">
                <div :class="{ 'diff-value': scope.row.hasDiff }">
                  <el-tag v-if="scope.row.snapshot2 === null || scope.row.snapshot2 === undefined" size="small" type="info">无</el-tag>
                  <span v-else>{{ scope.row.snapshot2 }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.hasDiff ? 'danger' : 'success'" size="small">
                  {{ scope.row.hasDiff ? '有差异' : '相同' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { configSnapshotApi, configPushApi, applicationApi, environmentApi } from '@/api'

export default {
  name: 'ConfigSnapshot',
  data() {
    return {
      // 基础数据
      applications: [],
      environments: [],
      snapshots: [],
      onlineClients: [],
      
      // 选择器
      selectedApp: null,
      selectedEnv: null,
      snapshotType: '',
      selectedSnapshot: null,
      
      // 分页
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      
      // 加载状态
      loading: false,
      stagingLoading: false,
      publishingLoading: false,
      comparingLoading: false,
      
      // 对话框
      showStagedDialog: false,
      showPublishDialog: false,
      showDetailDialog: false,
      showCompareDialog: false,
      
      // 表单数据
      stagedForm: {
        snapshotName: '',
        snapshotDesc: '',
        configDataJson: ''
      },
      
      publishForm: {
        snapshotId: null,
        publishedBy: '',
        pushType: 'all',
        targetInstances: []
      },
      
      compareForm: {
        snapshotId1: null,
        snapshotId2: null
      },
      
      // 详情数据
      selectedSnapshotDetail: null,
      snapshotItems: [],
      compareResult: null,
      compareSnapshot1: null,
      compareSnapshot2: null,
      
      // 验证
      stagedRules: {
        snapshotName: [
          { required: true, message: '请输入快照名称', trigger: 'blur' }
        ]
      },
      
      jsonError: ''
    }
  },
  
  computed: {
    stagedSnapshots() {
      return this.snapshots.filter(s => s.snapshotType === 1)
    }
  },
  
  watch: {
    selectedApp(newVal, oldVal) {
      if (newVal && this.selectedEnv) {
        this.loadSnapshots()
      }
    },
    selectedEnv(newVal, oldVal) {
      if (newVal && this.selectedApp) {
        this.loadSnapshots()
      }
    }
  },
  
  mounted() {
    this.loadApplications()
    this.loadEnvironments()
    // 如果有默认选择的应用和环境，自动加载快照
    this.$nextTick(() => {
      if (this.selectedApp && this.selectedEnv) {
        this.loadSnapshots()
      }
    })
  },
  
  methods: {
    // 加载应用列表
    async loadApplications() {
      try {
        // 检查token
        const token = localStorage.getItem('token')
        console.log('当前token:', token)
        
        const response = await applicationApi.getApplications({ page: 0, size: 100 })
        this.applications = (response.data?.content || response.data || []).filter(app => app && app.id)
        console.log('加载的应用列表:', this.applications)
      } catch (error) {
        console.error('加载应用列表失败:', error)
        this.$message.error('加载应用列表失败')
        this.applications = []
      }
    },
    
    // 加载环境列表
    async loadEnvironments() {
      try {
        const response = await environmentApi.getEnvironments({ page: 0, size: 100 })
        this.environments = (response.data?.content || response.data || []).filter(env => env && env.id)
        console.log('加载的环境列表:', this.environments)
      } catch (error) {
        console.error('加载环境列表失败:', error)
        this.$message.error('加载环境列表失败')
        this.environments = []
      }
    },
    
    // 加载快照列表
    async loadSnapshots() {
      if (!this.selectedApp || !this.selectedEnv) {
        console.log('应用或环境未选择，跳过加载快照')
        return
      }
      
      this.loading = true
      try {
        const params = {
          page: this.pagination.page - 1,
          size: this.pagination.size
        }
        
        const response = await configSnapshotApi.getSnapshotsByAppAndEnvPage(
          this.selectedApp, 
          this.selectedEnv, 
          params
        )
        
        console.log('快照列表响应:', response)
        console.log('响应数据结构:', JSON.stringify(response, null, 2))
        // Spring Data Page对象的结构
        this.snapshots = response.data?.content || response.data || []
        this.pagination.total = response.data?.totalElements || response.data?.total || 0
        console.log('加载的快照列表:', this.snapshots)
      } catch (error) {
        console.error('加载快照列表失败:', error)
        this.$message.error('加载快照列表失败')
        this.snapshots = []
      } finally {
        this.loading = false
      }
    },
    
    // 加载在线客户端
    async loadOnlineClients() {
      if (!this.selectedApp) return
      
      try {
        const response = await configPushApi.getOnlineClients(this.selectedApp)
        this.onlineClients = response.data
      } catch (error) {
        console.error('加载在线客户端失败:', error)
      }
    },
    
    // 创建暂存快照
    async createStagedSnapshot() {
      this.$refs.stagedForm.validate(async (valid) => {
        if (!valid) return
        
        if (this.jsonError) {
          this.$message.error('JSON格式错误，请检查配置数据')
          return
        }
        
        this.stagingLoading = true
        try {
          const configData = JSON.parse(this.stagedForm.configDataJson)
          
          // 获取当前用户信息
          const userInfoStr = localStorage.getItem('userInfo')
          let createdBy = 'admin' // 默认值
          if (userInfoStr) {
            try {
              const userInfo = JSON.parse(userInfoStr)
              createdBy = userInfo.username || userInfo.name || 'admin'
            } catch (e) {
              console.error('解析用户信息失败:', e)
            }
          }
          
          const data = {
            appId: this.selectedApp,
            envId: this.selectedEnv,
            snapshotName: this.stagedForm.snapshotName,
            snapshotDesc: this.stagedForm.snapshotDesc,
            configData: configData,
            createdBy: createdBy
          }
          
          await configSnapshotApi.createStagedSnapshot(data)
          this.$message.success('暂存配置成功')
          this.showStagedDialog = false
          this.loadSnapshots()
          this.resetStagedForm()
        } catch (error) {
          this.$message.error('暂存配置失败: ' + error.message)
        } finally {
          this.stagingLoading = false
        }
      })
    },
    
    // 发布快照
    async publishSnapshot(snapshot = null) {
      if (snapshot) {
        this.publishForm.snapshotId = snapshot.id
      }
      
      this.publishingLoading = true
      try {
        // 获取当前用户信息
        const userInfoStr = localStorage.getItem('userInfo')
        let publishedBy = this.publishForm.publishedBy || 'admin'
        if (userInfoStr) {
          try {
            const userInfo = JSON.parse(userInfoStr)
            publishedBy = userInfo.username || userInfo.name || publishedBy
          } catch (e) {
            console.error('解析用户信息失败:', e)
          }
        }
        
        // 发布快照
        await configSnapshotApi.publishSnapshot(
          this.publishForm.snapshotId, 
          { publishedBy: publishedBy }
        )
        
        // 推送配置
        const pushData = {}
        if (this.publishForm.pushType === 'specific') {
          pushData.targetInstances = this.publishForm.targetInstances
        }
        
        await configPushApi.pushSnapshotConfig(this.publishForm.snapshotId, pushData)
        
        this.$message.success('发布配置成功')
        this.showPublishDialog = false
        this.loadSnapshots()
        this.resetPublishForm()
      } catch (error) {
        this.$message.error('发布配置失败: ' + error.message)
      } finally {
        this.publishingLoading = false
      }
    },
    
    // 查看快照详情
    async viewSnapshot(snapshot) {
      this.selectedSnapshotDetail = snapshot
      this.showDetailDialog = true
      
      try {
        const response = await configSnapshotApi.getSnapshotItems(snapshot.id)
        this.snapshotItems = response.data
        console.log('快照详情 - 配置项数量:', snapshot.configCount)
        console.log('快照详情 - 配置项列表:', this.snapshotItems)
      } catch (error) {
        this.$message.error('加载快照详情失败')
      }
    },
    
    // 打开比较对话框（从表格行）
    compareSnapshot(snapshot) {
      this.showCompareDialog = true
      // 默认选择当前快照作为源快照
      this.compareForm.snapshotId1 = snapshot.id
      this.compareSnapshot1 = snapshot
      this.compareResult = null
    },
    
    // 打开比较对话框（从顶部按钮）
    openCompareDialog() {
      this.showCompareDialog = true
      // 清空之前的选择
      this.compareForm.snapshotId1 = null
      this.compareForm.snapshotId2 = null
      this.compareSnapshot1 = null
      this.compareSnapshot2 = null
      this.compareResult = null
    },
    
    // 比较快照
    async compareSnapshots() {
      if (!this.compareForm.snapshotId1 || !this.compareForm.snapshotId2) {
        this.$message.warning('请选择两个快照进行比较')
        return
      }
      
      if (this.compareForm.snapshotId1 === this.compareForm.snapshotId2) {
        this.$message.warning('请选择两个不同的快照进行比较')
        return
      }
      
      this.comparingLoading = true
      try {
        const response = await configSnapshotApi.compareSnapshots(
          this.compareForm.snapshotId1,
          this.compareForm.snapshotId2
        )
        
        this.compareResult = response.data
        this.compareSnapshot1 = this.snapshots.find(s => s.id === this.compareForm.snapshotId1)
        this.compareSnapshot2 = this.snapshots.find(s => s.id === this.compareForm.snapshotId2)
        
        // 处理比较结果，添加统计信息
        if (this.compareResult && this.compareResult.differences) {
          // 将Map转换为数组格式，便于表格显示
          const differencesMap = this.compareResult.differences
          const differencesArray = Object.keys(differencesMap).map(key => {
            const diff = differencesMap[key]
            return {
              configKey: key,
              snapshot1: diff.snapshot1,
              snapshot2: diff.snapshot2,
              hasDiff: diff.hasDiff
            }
          })
          
          this.compareResult.differences = differencesArray
          this.compareResult.totalCount = differencesArray.length
          this.compareResult.sameCount = differencesArray.filter(item => !item.hasDiff).length
          this.compareResult.diffCount = differencesArray.filter(item => item.hasDiff).length
        }
        
        this.$message.success('快照对比完成')
      } catch (error) {
        this.$message.error('比较快照失败: ' + error.message)
      } finally {
        this.comparingLoading = false
      }
    },
    
    // 快照1选择变化
    onSnapshot1Change(snapshotId) {
      this.compareSnapshot1 = this.snapshots.find(s => s.id === snapshotId)
      this.compareResult = null // 清除之前的比较结果
    },
    
    // 快照2选择变化
    onSnapshot2Change(snapshotId) {
      this.compareSnapshot2 = this.snapshots.find(s => s.id === snapshotId)
      this.compareResult = null // 清除之前的比较结果
    },
    
    // 清除比较结果
    clearCompareResult() {
      this.compareResult = null
      this.compareForm.snapshotId1 = null
      this.compareForm.snapshotId2 = null
      this.compareSnapshot1 = null
      this.compareSnapshot2 = null
    },
    
    // 删除快照
    async deleteSnapshot(snapshot) {
      this.$confirm('确定要删除这个快照吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await configSnapshotApi.deleteSnapshot(snapshot.id)
          this.$message.success('删除快照成功')
          this.loadSnapshots()
        } catch (error) {
          this.$message.error('删除快照失败: ' + error.message)
        }
      })
    },
    
    // 选择变化处理
    handleSelectionChange(selection) {
      this.selectedSnapshot = selection.length > 0 ? selection[0] : null
    },
    
    // 分页处理
    handleSizeChange(size) {
      this.pagination.size = size
      this.loadSnapshots()
    },
    
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadSnapshots()
    },
    
    // 表单重置
    resetStagedForm() {
      this.stagedForm = {
        snapshotName: '',
        snapshotDesc: '',
        configDataJson: ''
      }
      this.jsonError = ''
      this.$refs.stagedForm.resetFields()
    },
    
    resetPublishForm() {
      // 获取当前用户信息
      const userInfoStr = localStorage.getItem('userInfo')
      let publishedBy = ''
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr)
          publishedBy = userInfo.username || userInfo.name || ''
        } catch (e) {
          console.error('解析用户信息失败:', e)
        }
      }
      
      this.publishForm = {
        snapshotId: null,
        publishedBy: publishedBy,
        pushType: 'all',
        targetInstances: []
      }
    },
    
    // JSON验证
    validateJson() {
      try {
        if (this.stagedForm.configDataJson) {
          JSON.parse(this.stagedForm.configDataJson)
          this.jsonError = ''
        }
      } catch (error) {
        this.jsonError = 'JSON格式错误: ' + error.message
      }
    },
    
    // 工具方法
    formatDate(date) {
      return new Date(date).toLocaleString()
    },
    
    getConfigTypeName(type) {
      const typeMap = {
        1: '字符串',
        2: '数字',
        3: '布尔',
        4: 'JSON',
        5: 'YAML',
        6: 'Properties'
      }
      return typeMap[type] || '未知'
    }
  },
  
  watch: {
    selectedApp() {
      this.loadOnlineClients()
    }
  }
}
</script>

<style scoped>
.config-snapshot {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

.snapshot-list {
  background: white;
  border-radius: 4px;
  padding: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

.config-editor {
  position: relative;
}

.json-error {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

.compare-section {
  padding: 20px 0;
}

.compare-result {
  margin-top: 20px;
}

.diff-value {
  color: #f56c6c;
  font-weight: bold;
  background-color: #fef0f0;
  padding: 2px 4px;
  border-radius: 2px;
}

.config-items {
  margin-top: 20px;
}

.compare-selector {
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fafafa;
}

.snapshot-info {
  font-size: 12px;
  line-height: 1.5;
}

.snapshot-info p {
  margin: 5px 0;
}

.compare-summary {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #e9ecef;
}

.compare-actions {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
}
</style>
