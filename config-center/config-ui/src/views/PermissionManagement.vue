<template>
      <div class="permission-management">
    <el-card class="permission-card">
      <template #header>
        <div class="card-header">
          <span>应用权限管理</span>
          <div>
            <el-button type="primary" @click="showAssignDialog = true">
              分配应用权限
            </el-button>
            <el-button type="success" @click="showBatchAssignDialog = true">
              批量分配
            </el-button>
          </div>
        </div>
      </template>
  
        <!-- 用户权限列表 -->
        <el-table :data="userPermissions" style="width: 100%" v-loading="loading">
          <el-table-column prop="userName" label="用户名" width="120" />
          <el-table-column prop="appName" label="应用名称" width="150" />
          <el-table-column prop="permissionType" label="权限类型" width="120">
            <template #default="scope">
              <el-tag :type="getPermissionTypeColor(scope.row.permissionType)">
                {{ getPermissionTypeText(scope.row.permissionType) }}
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
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" type="danger" @click="revokePermission(scope.row)">
                撤销权限
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
  
      <!-- 分配权限对话框 -->
      <el-dialog v-model="showAssignDialog" title="分配权限" width="500px">
        <el-form :model="assignForm" :rules="assignRules" ref="assignFormRef" label-width="100px">
          <el-form-item label="用户" prop="userId">
            <el-select v-model="assignForm.userId" placeholder="选择用户" style="width: 100%">
              <el-option
                v-for="user in users"
                :key="user.id"
                :label="user.username"
                :value="user.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="应用" prop="appId">
            <el-select v-model="assignForm.appId" placeholder="选择应用" style="width: 100%">
              <el-option
                v-for="app in applications"
                :key="app.id"
                :label="app.appName"
                :value="app.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="权限类型" prop="permissionType">
            <el-select v-model="assignForm.permissionType" placeholder="选择权限类型" style="width: 100%">
              <el-option label="只读" value="READ" />
              <el-option label="读写" value="WRITE" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showAssignDialog = false">取消</el-button>
            <el-button type="primary" @click="assignPermission" :loading="assignLoading">
              确定
            </el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import { permissionApi, userApi, applicationApi } from '@/api'
  
  export default {
    name: 'PermissionManagement',
    data() {
      return {
        loading: false,
        userPermissions: [],
        users: [],
        applications: [],
        showAssignDialog: false,
        assignLoading: false,
        assignForm: {
          userId: null,
          appId: null,
          permissionType: ''
        },
        assignRules: {
          userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
          appId: [{ required: true, message: '请选择应用', trigger: 'change' }],
          permissionType: [{ required: true, message: '请选择权限类型', trigger: 'change' }]
        }
      }
    },
    mounted() {
      this.loadUserPermissions()
      this.loadUsers()
      this.loadApplications()
    },
    methods: {
          async loadUserPermissions() {
      try {
        this.loading = true
        // 获取所有应用权限列表
        const response = await permissionApi.getAllAppPermissions()
        this.userPermissions = response.data || []
      } catch (error) {
        console.error('加载权限列表失败:', error)
        this.$message.error('加载权限列表失败')
      } finally {
        this.loading = false
      }
    },
      async loadUsers() {
        try {
          const response = await userApi.getUsersForPermissions()
          this.users = response.data || []
        } catch (error) {
          console.error('加载用户列表失败:', error)
        }
      },
      async loadApplications() {
        try {
          const response = await applicationApi.getApplicationsForPermissions()
          this.applications = response.data || []
        } catch (error) {
          console.error('加载应用列表失败:', error)
        }
      },
      async assignPermission() {
        try {
          await this.$refs.assignFormRef.validate()
          this.assignLoading = true
          
          await permissionApi.assignAppPermission(
            this.assignForm.userId,
            this.assignForm.appId,
            this.assignForm.permissionType
          )
          
          this.$message.success('权限分配成功')
          this.showAssignDialog = false
          this.loadUserPermissions()
          this.resetAssignForm()
        } catch (error) {
          console.error('分配权限失败:', error)
          this.$message.error('分配权限失败')
        } finally {
          this.assignLoading = false
        }
      },
      async revokePermission(permission) {
        try {
          await this.$confirm('确定要撤销此权限吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          
          await permissionApi.revokeAppPermission(permission.userId, permission.appId)
          this.$message.success('权限撤销成功')
          this.loadUserPermissions()
        } catch (error) {
          if (error !== 'cancel') {
            console.error('撤销权限失败:', error)
            this.$message.error('撤销权限失败')
          }
        }
      },
      resetAssignForm() {
        this.assignForm = {
          userId: null,
          appId: null,
          permissionType: ''
        }
        this.$refs.assignFormRef?.resetFields()
      },
      getPermissionTypeColor(type) {
        const colors = {
          'READ': 'info',
          'WRITE': 'warning',
          'ADMIN': 'danger'
        }
        return colors[type] || 'info'
      },
      getPermissionTypeText(type) {
        const texts = {
          'READ': '只读',
          'WRITE': '读写',
          'ADMIN': '管理员'
        }
        return texts[type] || type
      }
    }
  }
  </script>
  
  <style scoped>
  .permission-management {
    padding: 20px;
  }
  
  .permission-card {
    margin-bottom: 20px;
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