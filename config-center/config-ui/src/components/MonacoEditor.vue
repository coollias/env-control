<template>
  <div ref="editorContainer" class="monaco-editor-container"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as monaco from 'monaco-editor'

const props = defineProps({
  modelValue: {
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
  }
})

const emit = defineEmits(['update:modelValue'])

const editorContainer = ref(null)
let editor = null

const createEditor = () => {
  if (!editorContainer.value) return

  // 确保容器有尺寸
  if (editorContainer.value.offsetHeight === 0) {
    editorContainer.value.style.height = '300px'
  }

  const defaultOptions = {
    theme: 'vs', // 使用浅色主题
    fontSize: 14,
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
    wordWrap: 'on',
    lineNumbers: 'on',
    folding: true,
    selectOnLineNumbers: true,
    roundedSelection: false,
    readOnly: false,
    cursorStyle: 'line',
    automaticLayout: true,
    scrollbar: {
      vertical: 'visible',
      horizontal: 'visible'
    }
  }

  const options = {
    ...defaultOptions,
    ...props.options,
    value: props.modelValue || '',
    language: props.language
  }

  try {
    // 如果已经存在编辑器，先销毁
    if (editor) {
      editor.dispose()
    }
    
    editor = monaco.editor.create(editorContainer.value, options)
    
    // 监听内容变化
    editor.onDidChangeModelContent(() => {
      const value = editor.getValue()
      emit('update:modelValue', value)
    })

    // 强制重新布局
    setTimeout(() => {
      if (editor) {
        editor.layout()
      }
    }, 100)
  } catch (error) {
    console.error('MonacoEditor初始化失败:', error)
  }
}

onMounted(async () => {
  await nextTick()
  // 延迟初始化，确保DOM完全渲染
  setTimeout(() => {
    createEditor()
  }, 100)
})

onBeforeUnmount(() => {
  if (editor) {
    editor.dispose()
  }
})

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
  if (editor && newValue !== editor.getValue()) {
    editor.setValue(newValue || '')
  }
}, { immediate: true })

// 监听language变化
watch(() => props.language, (newLanguage) => {
  if (editor) {
    const model = editor.getModel()
    if (model) {
      monaco.editor.setModelLanguage(model, newLanguage)
    }
  }
})

// 监听options变化
watch(() => props.options, () => {
  if (editor) {
    editor.dispose()
    createEditor()
  }
}, { deep: true })
</script>

<style scoped>
.monaco-editor-container {
  width: 100%;
  height: 300px;
  min-height: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #ffffff;
}
</style> 