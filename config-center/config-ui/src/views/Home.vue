<template>
  <div class="home">
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
import { Grid, Setting, Document, Connection, Plus, View } from '@element-plus/icons-vue'
import { healthApi } from '../api'

const stats = ref({
  applications: 0,
  environments: 0,
  configs: 0,
  active: 0
})

const backendStatus = ref(false)
const dbStatus = ref(false)
const redisStatus = ref(false)

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

onMounted(() => {
  checkSystemStatus()
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
</style> 