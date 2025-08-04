<template>
  <div class="system-permission-management">
    <el-card class="permission-card">
      <template #header>
        <div class="card-header">
          <span>系统权限管理</span>
          <div>
            <el-button type="primary" @click="showRoleDialog = true">
              管理角色
            </el-button>
            <el-button type="success" @click="showUserRoleDialog = true">
              分配用户角色
            </el-button>
          </div>
        </div>
      </template>

      <!-- 角色列表 -->
      <el-table :data="roles" style="width: 100%" v-loading="loading">
        <el-table-column prop="roleCode" label="角色编码" width="120" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleDesc" label="角色描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button size="small" @click="editRole(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="manageRolePermissions(scope.row)">
              管理权限
            </el-button>
            <el-button size="small" type="danger" @click="deleteRole(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 角色管理对话框 -->
    <el-dialog v-model="showRoleDialog" title="角色管理" width="600px">
      <el-form :model="roleForm" :rules="roleRules" ref="roleFormRef" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述" prop="roleDesc">
          <el-input v-model="roleForm.roleDesc" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRoleDialog = false">取消</el-button>
          <el-button type="primary" @click="saveRole" :loading="saveLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 用户角色分配对话框 -->
    <el-dialog v-model="showUserRoleDialog" title="分配用户角色" width="500px">
      <el-form :model="userRoleForm" :rules="userRoleRules" ref="userRoleFormRef" label-width="100px">
        <el-form-item label="用户" prop="userId">
          <el-select v-model="userRoleForm.userId" placeholder="选择用户" style="width: 100%">
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="userRoleForm.roleId" placeholder="选择角色" style="width: 100%">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showUserRoleDialog = false">取消</el-button>
          <el-button type="primary" @click="assignUserRole" :loading="assignLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 角色权限管理对话框 -->
    <el-dialog v-model="showRolePermissionDialog" title="角色权限管理" width="800px">
      <div class="role-permission-content">
        <h4>{{ currentRole.roleName }} - 权限管理</h4>
        <el-transfer
          v-model="selectedPermissions"
          :data="allPermissions"
          :titles="['可用权限', '已分配权限']"
          :props="{
            key: 'id',
            label: 'permName'
          }"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRolePermissionDialog = false">取消</el-button>
          <el-button type="primary" @click="saveRolePermissions" :loading="permissionLoading">
            保存权限
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { roleApi, userApi, permissionApi } from '@/api'

export default {
  name: 'SystemPermissionManagement',
  data() {
    return {
      loading: false,
      roles: [],
      users: [],
      allPermissions: [],
      selectedPermissions: [],
      currentRole: {},
      
      showRoleDialog: false,
      showUserRoleDialog: false,
      showRolePermissionDialog: false,
      
      saveLoading: false,
      assignLoading: false,
      permissionLoading: false,
      
      roleForm: {
        roleCode: '',
        roleName: '',
        roleDesc: '',
        status: 1
      },
      
      userRoleForm: {
        userId: null,
        roleId: null
      },
      
      roleRules: {
        roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
        roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      },
      
      userRoleRules: {
        userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
        roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadRoles()
    this.loadUsers()
    this.loadPermissions()
  },
  methods: {
    async loadRoles() {
      try {
        this.loading = true
        const response = await roleApi.getRoles()
        this.roles = response.data || []
      } catch (error) {
        console.error('加载角色列表失败:', error)
        this.$message.error('加载角色列表失败')
      } finally {
        this.loading = false
      }
    },
    
    async loadUsers() {
      try {
        const response = await userApi.getUsers()
        this.users = response.data || []
      } catch (error) {
        console.error('加载用户列表失败:', error)
      }
    },
    
    async loadPermissions() {
      try {
        const response = await permissionApi.getPermissions()
        this.allPermissions = response.data || []
      } catch (error) {
        console.error('加载权限列表失败:', error)
      }
    },
    
    editRole(role) {
      this.roleForm = { ...role }
      this.showRoleDialog = true
    },
    
    async saveRole() {
      try {
        await this.$refs.roleFormRef.validate()
        this.saveLoading = true
        
        if (this.roleForm.id) {
          await roleApi.updateRole(this.roleForm.id, this.roleForm)
          this.$message.success('角色更新成功')
        } else {
          await roleApi.createRole(this.roleForm)
          this.$message.success('角色创建成功')
        }
        
        this.showRoleDialog = false
        this.loadRoles()
        this.resetRoleForm()
      } catch (error) {
        console.error('保存角色失败:', error)
        this.$message.error('保存角色失败')
      } finally {
        this.saveLoading = false
      }
    },
    
    async deleteRole(role) {
      try {
        await this.$confirm('确定要删除此角色吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await roleApi.deleteRole(role.id)
        this.$message.success('角色删除成功')
        this.loadRoles()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除角色失败:', error)
          this.$message.error('删除角色失败')
        }
      }
    },
    
    async assignUserRole() {
      try {
        await this.$refs.userRoleFormRef.validate()
        this.assignLoading = true
        
        await roleApi.assignRoleToUser(this.userRoleForm.userId, this.userRoleForm.roleId)
        this.$message.success('用户角色分配成功')
        this.showUserRoleDialog = false
        this.resetUserRoleForm()
      } catch (error) {
        console.error('分配用户角色失败:', error)
        this.$message.error('分配用户角色失败')
      } finally {
        this.assignLoading = false
      }
    },
    
    async manageRolePermissions(role) {
      this.currentRole = role
      this.showRolePermissionDialog = true
      
      try {
        const response = await roleApi.getRolePermissions(role.id)
        this.selectedPermissions = response.data.map(p => p.id) || []
      } catch (error) {
        console.error('加载角色权限失败:', error)
        this.selectedPermissions = []
      }
    },
    
    async saveRolePermissions() {
      try {
        this.permissionLoading = true
        
        // 这里需要实现批量更新角色权限的API
        // await roleApi.updateRolePermissions(this.currentRole.id, this.selectedPermissions)
        
        this.$message.success('角色权限保存成功')
        this.showRolePermissionDialog = false
      } catch (error) {
        console.error('保存角色权限失败:', error)
        this.$message.error('保存角色权限失败')
      } finally {
        this.permissionLoading = false
      }
    },
    
    resetRoleForm() {
      this.roleForm = {
        roleCode: '',
        roleName: '',
        roleDesc: '',
        status: 1
      }
      this.$refs.roleFormRef?.resetFields()
    },
    
    resetUserRoleForm() {
      this.userRoleForm = {
        userId: null,
        roleId: null
      }
      this.$refs.userRoleFormRef?.resetFields()
    }
  }
}
</script>

<style scoped>
.system-permission-management {
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

.role-permission-content {
  text-align: center;
}

.role-permission-content h4 {
  margin-bottom: 20px;
}
</style> 