<template>
  <div ref="editorContainer" class="monaco-diff-editor-container"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as monaco from 'monaco-editor'

// 配置Monaco Editor的Web Worker
if (typeof window !== 'undefined') {
  window.MonacoEnvironment = {
    getWorker: function (moduleId, label) {
      // 禁用Web Worker，在主线程中运行
      return {
        postMessage: function () {},
        addEventListener: function () {},
        removeEventListener: function () {},
        terminate: function () {}
      }
    }
  }
}

const props = defineProps({
  original: {
    type: String,
    default: ''
  },
  modified: {
    type: String,
    default: ''
  },
  language: {
    type: String,
    default: 'plaintext'
  },
  options: {
    type: Object,
    default: () => ({})
  },
  height: {
    type: Number,
    default: 500
  }
})

const editorContainer = ref(null)
let diffEditor = null

const createDiffEditor = () => {
  if (!editorContainer.value) return

  // 确保容器有尺寸
  if (editorContainer.value.offsetHeight === 0) {
    editorContainer.value.style.height = props.height + 'px'
  }

  const defaultOptions = {
    theme: 'vs',
    fontSize: 14,
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
    wordWrap: 'on',
    lineNumbers: 'on',
    folding: true,
    selectOnLineNumbers: true,
    roundedSelection: false,
    readOnly: true,
    cursorStyle: 'line',
    automaticLayout: true,
    scrollbar: {
      vertical: 'visible',
      horizontal: 'visible'
    },
    renderSideBySide: true,
    enableSplitViewResizing: true,
    renderOverviewRuler: true,
    renderIndicators: true,
    renderMarginRevertIcon: true
  }

  const options = {
    ...defaultOptions,
    ...props.options
  }

  try {
    // 如果已经存在编辑器，先销毁
    if (diffEditor) {
      diffEditor.dispose()
    }
    
    // 创建原始模型
    const originalModel = monaco.editor.createModel(props.original || '', props.language)
    
    // 创建修改模型
    const modifiedModel = monaco.editor.createModel(props.modified || '', props.language)
    
    // 创建diff编辑器
    diffEditor = monaco.editor.createDiffEditor(editorContainer.value, options)
    
    // 设置模型
    diffEditor.setModel({
      original: originalModel,
      modified: modifiedModel
    })

    // 强制重新布局
    setTimeout(() => {
      if (diffEditor) {
        diffEditor.layout()
      }
    }, 100)
  } catch (error) {
    console.error('MonacoDiffEditor初始化失败:', error)
  }
}

onMounted(async () => {
  await nextTick()
  // 延迟初始化，确保DOM完全渲染
  setTimeout(() => {
    createDiffEditor()
  }, 100)
})

onBeforeUnmount(() => {
  if (diffEditor) {
    diffEditor.dispose()
  }
})

// 监听original变化
watch(() => props.original, (newValue) => {
  if (diffEditor) {
    const originalModel = diffEditor.getOriginalEditor().getModel()
    if (originalModel && newValue !== originalModel.getValue()) {
      originalModel.setValue(newValue || '')
    }
  }
}, { immediate: true })

// 监听modified变化
watch(() => props.modified, (newValue) => {
  if (diffEditor) {
    const modifiedModel = diffEditor.getModifiedEditor().getModel()
    if (modifiedModel && newValue !== modifiedModel.getValue()) {
      modifiedModel.setValue(newValue || '')
    }
  }
}, { immediate: true })

// 监听language变化
watch(() => props.language, (newLanguage) => {
  if (diffEditor) {
    const originalModel = diffEditor.getOriginalEditor().getModel()
    const modifiedModel = diffEditor.getModifiedEditor().getModel()
    
    if (originalModel) {
      monaco.editor.setModelLanguage(originalModel, newLanguage)
    }
    if (modifiedModel) {
      monaco.editor.setModelLanguage(modifiedModel, newLanguage)
    }
  }
})

// 监听options变化
watch(() => props.options, () => {
  if (diffEditor) {
    diffEditor.dispose()
    createDiffEditor()
  }
}, { deep: true })

// 监听height变化
watch(() => props.height, (newHeight) => {
  if (editorContainer.value) {
    editorContainer.value.style.height = newHeight + 'px'
    if (diffEditor) {
      diffEditor.layout()
    }
  }
})
</script>

<style scoped>
.monaco-diff-editor-container {
  width: 100%;
  height: 500px;
  min-height: 300px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #ffffff;
}
</style> 