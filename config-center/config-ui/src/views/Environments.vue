<template>
  <div class="environments">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>环境管理</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            创建环境
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索环境名称或代码"
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

      <!-- 环境列表 -->
      <el-table :data="environments" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="envCode" label="环境代码" width="150" />
        <el-table-column prop="envName" label="环境名称" width="200" />
        <el-table-column prop="envDesc" label="描述" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="100" />
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
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
      :title="editingEnvironment ? '编辑环境' : '创建环境'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="环境代码" prop="envCode">
          <el-input v-model="form.envCode" placeholder="请输入环境代码" />
        </el-form-item>
        <el-form-item label="环境名称" prop="envName">
          <el-input v-model="form.envName" placeholder="请输入环境名称" />
        </el-form-item>
        <el-form-item label="环境描述" prop="envDesc">
          <el-input
            v-model="form.envDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入环境描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { environmentApi } from '../api'

const loading = ref(false)
const environments = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const statusFilter = ref('')
const showCreateDialog = ref(false)
const editingEnvironment = ref(null)
const formRef = ref()

const form = reactive({
  envCode: '',
  envName: '',
  envDesc: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  envCode: [
    { required: true, message: '请输入环境代码', trigger: 'blur' },
    { min: 2, max: 32, message: '长度在 2 到 32 个字符', trigger: 'blur' }
  ],
  envName: [
    { required: true, message: '请输入环境名称', trigger: 'blur' },
    { min: 2, max: 64, message: '长度在 2 到 64 个字符', trigger: 'blur' }
  ]
}

const loadEnvironments = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchKeyword.value,
      status: statusFilter.value
    }
    const response = await environmentApi.getEnvironments(params)
    environments.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('加载环境列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadEnvironments()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadEnvironments()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadEnvironments()
}

const handleEdit = (row) => {
  editingEnvironment.value = row
  Object.assign(form, row)
  showCreateDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除环境 "${row.envName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await environmentApi.deleteEnvironment(row.id)
    ElMessage.success('删除成功')
    loadEnvironments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (editingEnvironment.value) {
      await environmentApi.updateEnvironment(editingEnvironment.value.id, form)
      ElMessage.success('更新成功')
    } else {
      await environmentApi.createEnvironment(form)
      ElMessage.success('创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    loadEnvironments()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

const resetForm = () => {
  editingEnvironment.value = null
  Object.assign(form, {
    envCode: '',
    envName: '',
    envDesc: '',
    sortOrder: 0,
    status: 1
  })
  formRef.value?.resetFields()
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

onMounted(() => {
  loadEnvironments()
})
</script>

<style scoped>
.environments {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 