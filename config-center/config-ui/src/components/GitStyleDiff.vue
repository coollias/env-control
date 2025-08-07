<template>
  <div class="git-style-diff">
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
        <el-button size="small" @click="refreshDiff">
          <el-icon><Refresh /></el-icon>
          刷新
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
        <!-- Monaco Diff Editor -->
        <div class="monaco-diff-container">
          <div class="diff-editor">
            <div class="editor-header">
              <div class="version-info">
                <span class="version-label">{{ version1 }}</span>
                <el-icon><ArrowRight /></el-icon>
                <span class="version-label">{{ version2 }}</span>
              </div>
              <el-tag size="small" type="info">配置对比</el-tag>
            </div>
            <div class="editor-container">
              <MonacoDiffEditor
                :original="configText1"
                :modified="configText2"
                :options="monacoDiffOptions"
                :height="500"
                language="properties"
                readonly
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ArrowRight, View, Refresh } from '@element-plus/icons-vue'
import MonacoEditor from './MonacoEditor.vue'
import MonacoDiffEditor from './MonacoDiffEditor.vue'

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

// 切换视图模式
const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'side-by-side' ? 'unified' : 'side-by-side'
}

// 刷新diff
const refreshDiff = () => {
  // 触发父组件重新加载diff数据
  emit('refresh')
}

const emit = defineEmits(['refresh'])

// Monaco Diff Editor配置
const monacoDiffOptions = {
  theme: 'vs',
  fontSize: 14,
  readOnly: true,
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  wordWrap: 'on',
  renderSideBySide: true,
  enableSplitViewResizing: true,
  renderOverviewRuler: true,
  renderIndicators: true,
  renderMarginRevertIcon: true
}

// 生成配置文本
const configText1 = computed(() => {
  if (!props.diffData || !props.diffData.fullConfig1) {
    return ''
  }
  
  const config = props.diffData.fullConfig1
  const lines = []
  
  // 添加头部注释
  lines.push(`# ${props.version1} 配置`)
  lines.push(`# 生成时间: ${new Date().toLocaleString()}`)
  lines.push('')
  
  // 按字母顺序排序配置键
  const sortedKeys = Object.keys(config).sort()
  
  sortedKeys.forEach(key => {
    const value = config[key]
    if (value !== undefined && value !== null) {
      lines.push(`${key}=${value}`)
    }
  })
  
  return lines.join('\n')
})

const configText2 = computed(() => {
  if (!props.diffData || !props.diffData.fullConfig2) {
    return ''
  }
  
  const config = props.diffData.fullConfig2
  const lines = []
  
  // 添加头部注释
  lines.push(`# ${props.version2} 配置`)
  lines.push(`# 生成时间: ${new Date().toLocaleString()}`)
  lines.push('')
  
  // 按字母顺序排序配置键
  const sortedKeys = Object.keys(config).sort()
  
  sortedKeys.forEach(key => {
    const value = config[key]
    if (value !== undefined && value !== null) {
      lines.push(`${key}=${value}`)
    }
  })
  
  return lines.join('\n')
})

// 生成左侧行数据
const leftLines = computed(() => {
  return generateVersionLines(props.version1, props.diffData, 'left')
})

// 生成右侧行数据
const rightLines = computed(() => {
  return generateVersionLines(props.version2, props.diffData, 'right')
})

// 生成统一视图行数据
const unifiedLines = computed(() => {
  return generateUnifiedLines(props.diffData)
})

// 生成版本行数据
const generateVersionLines = (version, diffData, side) => {
  const lines = []
  
  // 添加头部注释
  lines.push({ type: 'comment', content: `# ${version} 配置`, changeType: null })
  lines.push({ type: 'comment', content: `# 生成时间: ${new Date().toLocaleString()}`, changeType: null })
  lines.push({ type: 'empty', content: '', changeType: null })
  
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
    const changeType = getChangeType(diff)
    
    if (value !== undefined && value !== null) {
      // 添加注释说明变更类型
      if (changeType === '新增' && side === 'left') {
        lines.push({ type: 'comment', content: `# ${key} - 新增`, changeType: 'added' })
      } else if (changeType === '删除' && side === 'right') {
        lines.push({ type: 'comment', content: `# ${key} - 删除`, changeType: 'removed' })
      } else if (changeType === '修改') {
        lines.push({ type: 'comment', content: `# ${key} - 修改`, changeType: 'modified' })
      }
      
      lines.push({ 
        type: 'config', 
        content: `${key}=${value}`, 
        changeType: changeType === '新增' && side === 'left' ? 'added' : 
                   changeType === '删除' && side === 'right' ? 'removed' : 
                   changeType === '修改' ? 'modified' : null 
      })
    } else {
      // 对于不存在的值，添加注释
      if (side === 'left' && changeType === '删除') {
        lines.push({ type: 'comment', content: `# ${key} - 删除`, changeType: 'removed' })
        lines.push({ type: 'config', content: `# ${key}=`, changeType: 'removed' })
      } else if (side === 'right' && changeType === '新增') {
        lines.push({ type: 'comment', content: `# ${key} - 新增`, changeType: 'added' })
        lines.push({ type: 'config', content: `# ${key}=`, changeType: 'added' })
      }
    }
    lines.push({ type: 'empty', content: '', changeType: null })
  })
  
  return lines
}

// 生成统一视图行数据
const generateUnifiedLines = (diffData) => {
  const lines = []
  
  // 添加头部
  lines.push({ type: 'header', content: `--- ${props.version1}`, changeType: null })
  lines.push({ type: 'header', content: `+++ ${props.version2}`, changeType: null })
  lines.push({ type: 'empty', content: '', changeType: null })
  
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
      lines.push({ type: 'added', content: `+ ${key}=${diff.version2}`, changeType: 'added' })
    } else if (changeType === '删除') {
      lines.push({ type: 'removed', content: `- ${key}=${diff.version1}`, changeType: 'removed' })
    } else if (changeType === '修改') {
      lines.push({ type: 'removed', content: `- ${key}=${diff.version1}`, changeType: 'removed' })
      lines.push({ type: 'added', content: `+ ${key}=${diff.version2}`, changeType: 'added' })
    }
  })
  
  return lines
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

// 获取行CSS类
const getLineClass = (line) => {
  if (line.changeType === 'added') {
    return 'line-added'
  } else if (line.changeType === 'removed') {
    return 'line-removed'
  } else if (line.changeType === 'modified') {
    return 'line-modified'
  }
  return ''
}

// 高亮行内容
const highlightLine = (line) => {
  if (line.type === 'config' && line.changeType) {
    // 高亮配置键
    const parts = line.content.split('=')
    if (parts.length === 2) {
      const key = parts[0]
      const value = parts[1]
      return `<span class="config-key">${key}</span>=<span class="config-value">${value}</span>`
    }
  }
  return line.content
}

// 监听diffData变化
watch(() => props.diffData, (newData) => {
  // 可以在这里添加数据变化处理逻辑
}, { immediate: true })
</script>

<style scoped>
.git-style-diff {
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

.monaco-diff-container {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
}

.diff-editor {
  display: flex;
  flex-direction: column;
  height: 600px;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.editor-container {
  flex: 1;
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
  overflow: auto;
}

.diff-lines {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
}

.diff-line {
  display: flex;
  padding: 2px 8px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.diff-line:hover {
  background-color: #f8f9fa;
}

.line-number {
  width: 50px;
  color: #909399;
  font-size: 11px;
  text-align: right;
  padding-right: 10px;
  user-select: none;
}

.line-content {
  flex: 1;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 行类型样式 */
.line-added {
  background-color: #f6ffed;
  border-left: 3px solid #52c41a;
}

.line-removed {
  background-color: #fff2f0;
  border-left: 3px solid #ff4d4f;
}

.line-modified {
  background-color: #fff7e6;
  border-left: 3px solid #fa8c16;
}

/* 配置键值高亮 */
.config-key {
  color: #1890ff;
  font-weight: 500;
}

.config-value {
  color: #595959;
}

/* 统一视图 */
.unified-view {
  height: 100%;
}

.diff-content {
  height: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: auto;
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