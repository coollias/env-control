<template>
  <div class="config-diff">
    <div class="diff-header">
      <div class="diff-title">
        <h3>版本差异对比</h3>
        <div class="version-info">
          <el-tag type="info">{{ version1 }}</el-tag>
          <el-icon><ArrowRight /></el-icon>
          <el-tag type="primary">{{ version2 }}</el-tag>
        </div>
      </div>
      <div class="diff-actions">
        <el-button size="small" @click="toggleViewMode">
          <el-icon><View /></el-icon>
          {{ viewMode === 'side-by-side' ? '统一视图' : '并排视图' }}
        </el-button>
        <el-button size="small" @click="expandAll">
          <el-icon><Expand /></el-icon>
          展开全部
        </el-button>
        <el-button size="small" @click="collapseAll">
          <el-icon><Fold /></el-icon>
          折叠全部
        </el-button>
      </div>
    </div>

    <div class="diff-content">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else-if="!diffData || Object.keys(diffData).length === 0" class="empty-state">
        <el-empty description="两个版本配置完全相同，无差异" />
      </div>
      
      <div v-else class="diff-viewer" :class="viewMode">
        <!-- 并排视图 -->
        <div v-if="viewMode === 'side-by-side'" class="side-by-side-view">
          <div class="diff-panel">
            <div class="panel-header">
              <span class="version-label">{{ version1 }}</span>
              <el-tag size="small" type="info">原版本</el-tag>
            </div>
            <div class="panel-content">
              <MonacoEditor
                v-model="leftContent"
                :language="'properties'"
                :options="monacoOptions"
                :readonly="true"
                height="600px"
              />
            </div>
          </div>
          
          <div class="diff-panel">
            <div class="panel-header">
              <span class="version-label">{{ version2 }}</span>
              <el-tag size="small" type="success">新版本</el-tag>
            </div>
            <div class="panel-content">
              <MonacoEditor
                v-model="rightContent"
                :language="'properties'"
                :options="monacoOptions"
                :readonly="true"
                height="600px"
              />
            </div>
          </div>
        </div>
        
        <!-- 统一视图 -->
        <div v-else class="unified-view">
          <div class="diff-content">
            <MonacoEditor
              v-model="unifiedContent"
              :language="'diff'"
              :options="monacoOptions"
              :readonly="true"
              height="600px"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ArrowRight, View, Expand, Fold } from '@element-plus/icons-vue'
import MonacoEditor from './MonacoEditor.vue'

const props = defineProps({
  version1: {
    type: String,
    required: true
  },
  version2: {
    type: String,
    required: true
  },
  diffData: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const viewMode = ref('side-by-side') // 'side-by-side' | 'unified'

const monacoOptions = {
  theme: 'vs',
  fontSize: 12,
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  automaticLayout: true,
  readOnly: true,
  wordWrap: 'on',
  lineNumbers: 'on',
  folding: true,
  renderLineHighlight: 'all'
}

// 切换视图模式
const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'side-by-side' ? 'unified' : 'side-by-side'
}

// 展开所有项目
const expandAll = () => {
  // 这里可以添加展开逻辑
}

// 折叠所有项目
const collapseAll = () => {
  // 这里可以添加折叠逻辑
}

// 生成左侧内容（原版本）
const leftContent = computed(() => {
  return generateVersionContent(props.version1, props.diffData, 'left')
})

// 生成右侧内容（新版本）
const rightContent = computed(() => {
  return generateVersionContent(props.version2, props.diffData, 'right')
})

// 生成统一视图内容
const unifiedContent = computed(() => {
  return generateUnifiedDiff(props.diffData)
})

// 生成版本内容
const generateVersionContent = (version, diffData, side) => {
  const lines = []
  
  // 添加头部注释
  lines.push(`# ${version} 配置`)
  lines.push(`# 生成时间: ${new Date().toLocaleString()}`)
  lines.push('')
  
  // 获取所有配置键
  const allKeys = new Set()
  Object.keys(diffData).forEach(key => {
    allKeys.add(key)
  })
  
  // 按字母顺序排序
  const sortedKeys = Array.from(allKeys).sort()
  
  sortedKeys.forEach(key => {
    const diff = diffData[key]
    const value = side === 'left' ? diff.version1 : diff.version2
    
    if (value !== undefined && value !== null) {
      // 添加注释说明变更类型
      const changeType = getChangeType(diff)
      if (changeType === '新增' && side === 'left') {
        lines.push(`# ${key} - 新增`)
      } else if (changeType === '删除' && side === 'right') {
        lines.push(`# ${key} - 删除`)
      } else if (changeType === '修改') {
        lines.push(`# ${key} - 修改`)
      }
      
      lines.push(`${key}=${value}`)
    } else {
      // 对于不存在的值，添加注释
      if (side === 'left' && changeType === '删除') {
        lines.push(`# ${key} - 删除`)
        lines.push(`# ${key}=`)
      } else if (side === 'right' && changeType === '新增') {
        lines.push(`# ${key} - 新增`)
        lines.push(`# ${key}=`)
      }
    }
    lines.push('')
  })
  
  return lines.join('\n')
}

// 生成统一diff格式
const generateUnifiedDiff = (diffData) => {
  const lines = []
  
  // 添加头部
  lines.push(`--- ${props.version1}`)
  lines.push(`+++ ${props.version2}`)
  lines.push('')
  
  // 获取所有配置键
  const allKeys = new Set()
  Object.keys(diffData).forEach(key => {
    allKeys.add(key)
  })
  
  // 按字母顺序排序
  const sortedKeys = Array.from(allKeys).sort()
  
  sortedKeys.forEach(key => {
    const diff = diffData[key]
    const changeType = getChangeType(diff)
    
    if (changeType === '新增') {
      lines.push(`+ ${key}=${diff.version2}`)
    } else if (changeType === '删除') {
      lines.push(`- ${key}=${diff.version1}`)
    } else if (changeType === '修改') {
      lines.push(`- ${key}=${diff.version1}`)
      lines.push(`+ ${key}=${diff.version2}`)
    }
  })
  
  return lines.join('\n')
}

// 获取变更类型
const getChangeType = (diff) => {
  if (!diff.version1 && diff.version2) {
    return '新增'
  } else if (diff.version1 && !diff.version2) {
    return '删除'
  } else {
    return '修改'
  }
}

// 监听diffData变化
watch(() => props.diffData, (newData) => {
  // 可以在这里添加数据变化处理逻辑
}, { immediate: true })
</script>

<style scoped>
.config-diff {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.diff-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 20px;
}

.diff-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.diff-title h3 {
  margin: 0;
  color: #303133;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.diff-actions {
  display: flex;
  gap: 8px;
}

.diff-content {
  flex: 1;
  overflow: hidden;
}

.loading-state {
  padding: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.diff-viewer {
  height: 100%;
  overflow: hidden;
}

/* 并排视图 */
.side-by-side-view {
  display: flex;
  height: 100%;
  gap: 20px;
}

.diff-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

.version-label {
  font-weight: 500;
  color: #303133;
}

.panel-content {
  flex: 1;
  overflow: hidden;
}

/* 统一视图 */
.unified-view {
  height: 100%;
}

.diff-content {
  height: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .diff-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .side-by-side-view {
    flex-direction: column;
  }
  
  .diff-panel {
    height: 300px;
  }
}
</style> 