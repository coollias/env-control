<template>
  <div class="home">
    <!-- 用户信息栏 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="24">
        <el-card>
          <div class="user-info">
            <div class="user-avatar">
              <el-avatar :size="50" icon="el-icon-user" />
            </div>
            <div class="user-details">
              <h3>{{ userInfo.realName || userInfo.username }}</h3>
              <p>{{ userInfo.email }}</p>
            </div>
            <div class="user-actions">
              <el-button type="text" @click="logout">退出登录</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统概览</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-content">
                  <el-icon size="40" color="#409EFF"><Grid /></el-icon>
                  <div class="stat-info">
                    <div class="stat-number">{{ stats.applications || 0 }}</div>
                    <div class="stat-label">应用总数</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-content">
                  <el-icon size="40" color="#67C23A"><Setting /></el-icon>
                  <div class="stat-info">
                    <div class="stat-number">{{ stats.environments || 0 }}</div>
                    <div class="stat-label">环境总数</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-content">
                  <el-icon size="40" color="#E6A23C"><Document /></el-icon>
                  <div class="stat-info">
                    <div class="stat-number">{{ stats.configs || 0 }}</div>
                    <div class="stat-label">配置总数</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-content">
                  <el-icon size="40" color="#F56C6C"><Connection /></el-icon>
                  <div class="stat-info">
                    <div class="stat-number">{{ stats.active || 0 }}</div>
                    <div class="stat-label">活跃配置</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <el-space wrap>
            <el-button type="primary" @click="$router.push('/applications')">
              <el-icon><Plus /></el-icon>
              创建应用
            </el-button>
            <el-button type="success" @click="$router.push('/environments')">
              <el-icon><Plus /></el-icon>
              创建环境
            </el-button>
            <el-button type="warning" @click="$router.push('/configs')">
              <el-icon><Plus /></el-icon>
              创建配置
            </el-button>
            <el-button type="info" @click="$router.push('/client')">
              <el-icon><View /></el-icon>
              查看API
            </el-button>
          </el-space>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="后端服务">
              <el-tag :type="backendStatus ? 'success' : 'danger'">
                {{ backendStatus ? '正常' : '异常' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="数据库">
              <el-tag :type="dbStatus ? 'success' : 'danger'">
                {{ dbStatus ? '正常' : '异常' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Redis">
              <el-tag :type="redisStatus ? 'success' : 'danger'">
                {{ redisStatus ? '正常' : '异常' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Grid, Setting, Document, Connection, Plus, View } from '@element-plus/icons-vue'
import { healthApi } from '../api'

const router = useRouter()

// 用户信息
const userInfo = ref({})
const stats = ref({
  applications: 0,
  environments: 0,
  configs: 0,
  active: 0
})

const backendStatus = ref(false)
const dbStatus = ref(false)
const redisStatus = ref(false)

const loadStats = async () => {
  try {
    const response = await healthApi.getStats()
    if (response.data) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const checkSystemStatus = async () => {
  try {
    const response = await healthApi.health()
    backendStatus.value = true
    // 这里可以根据实际的后端健康检查接口返回信息来判断各个组件状态
    dbStatus.value = true
    redisStatus.value = true
  } catch (error) {
    backendStatus.value = false
    dbStatus.value = false
    redisStatus.value = false
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

onMounted(() => {
  // 获取用户信息
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    userInfo.value = JSON.parse(userInfoStr)
  }
  
  checkSystemStatus()
  loadStats()
})
</script>

<style scoped>
.home {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-info {
  margin-left: 20px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.user-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-avatar {
  margin-right: 15px;
}

.user-details h3 {
  margin: 0 0 5px 0;
  color: #303133;
}

.user-details p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.user-actions {
  margin-left: auto;
}
</style> 