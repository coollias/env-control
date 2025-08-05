import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 添加token到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    const { data } = response
    if (data.code === 200) {
      return data
    } else {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 清除token并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    } else if (error.response && error.response.status === 400) {
      // 处理400错误
      const errorData = error.response.data
      if (errorData && errorData.message) {
        ElMessage.error(errorData.message)
      } else {
        ElMessage.error('请求参数错误')
      }
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

// 应用管理API
export const applicationApi = {
  // 获取应用列表
  getApplications: (params) => api.get('/applications', { params }),
  // 获取应用列表（用于权限管理）
  getApplicationsForPermissions: () => api.get('/applications/for-permissions'),
  // 获取应用详情
  getApplication: (id) => api.get(`/applications/${id}`),
  // 创建应用
  createApplication: (data) => api.post('/applications', data),
  // 更新应用
  updateApplication: (id, data) => api.put(`/applications/${id}`, data),
  // 删除应用
  deleteApplication: (id) => api.delete(`/applications/${id}`),
  // 搜索应用
  searchApplications: (keyword) => api.get('/applications/search', { params: { keyword } })
}

// 环境管理API
export const environmentApi = {
  // 获取环境列表
  getEnvironments: (params) => api.get('/environments', { params }),
  // 获取环境详情
  getEnvironment: (id) => api.get(`/environments/${id}`),
  // 创建环境
  createEnvironment: (data) => api.post('/environments', data),
  // 更新环境
  updateEnvironment: (id, data) => api.put(`/environments/${id}`, data),
  // 删除环境
  deleteEnvironment: (id) => api.delete(`/environments/${id}`)
}

// 配置管理API
export const configApi = {
  // 获取配置列表
  getConfigs: (params) => api.get('/config-items', { params }),
  // 获取配置详情
  getConfig: (id) => api.get(`/config-items/${id}`),
  // 创建配置
  createConfig: (data) => api.post('/config-items', data),
  // 更新配置
  updateConfig: (id, data) => api.put(`/config-items/${id}`, data),
  // 删除配置
  deleteConfig: (id) => api.delete(`/config-items/${id}`),
  // 批量操作
  batchOperation: (data) => api.post('/config-items/batch', data),
  // 搜索配置
  searchConfigs: (params) => api.get('/config-items/search', { params }),
  // 上传配置文件
  uploadConfigFile: (formData) => api.post('/config-items/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),
  // 获取支持的文件格式
  getSupportedFormats: () => api.get('/config-items/supported-formats'),
  
  // ==================== 环境级配置覆盖功能 ====================
  // 获取应用在指定环境下的完整配置（包含继承的配置）
  getMergedConfigs: (appId, envId) => api.get(`/config-items/app/${appId}/env/${envId}/merged`),
  // 获取应用在指定环境下的配置映射（键值对形式）
  getMergedConfigMap: (appId, envId) => api.get(`/config-items/app/${appId}/env/${envId}/merged-map`),
  // 获取指定配置键在环境继承链中的最终值
  getConfigWithInheritance: (appId, envId, configKey) => api.get(`/config-items/app/${appId}/env/${envId}/key/${configKey}/inherited`),
  // 获取环境继承链
  getEnvironmentInheritanceChain: (appId, envId) => api.get(`/config-items/app/${appId}/env/${envId}/inheritance-chain`),
  // 获取应用在所有环境下的配置差异
  getConfigDifferences: (appId) => api.get(`/config-items/app/${appId}/config-differences`)
}

// 客户端API
export const clientApi = {
  // 获取配置
  getConfig: (appId, envId) => api.get(`/client/configs/${appId}/${envId}`),
  // 获取配置项
  getConfigItem: (appId, envId, key) => api.get(`/client/config/${appId}/${envId}/${key}`)
}

// 认证API
export const authApi = {
  // 用户登录
  login: (data) => api.post('/auth/login', data),
  // 用户注册
  register: (data) => api.post('/auth/register', data),
  // 检查用户名
  checkUsername: (username) => api.get('/auth/check-username', { params: { username } }),
  // 检查邮箱
  checkEmail: (email) => api.get('/auth/check-email', { params: { email } })
}

// 用户管理API
export const userApi = {
  // 获取用户列表
  getUsers: () => api.get('/users'),
  // 获取用户列表（用于权限管理）
  getUsersForPermissions: () => api.get('/users/for-permissions'),
  // 获取用户详情
  getUser: (id) => api.get(`/users/${id}`),
  // 创建用户
  createUser: (data) => api.post('/users', data),
  // 更新用户
  updateUser: (id, data) => api.put(`/users/${id}`, data),
  // 删除用户
  deleteUser: (id) => api.delete(`/users/${id}`),
  // 检查用户名
  checkUsername: (username) => api.get('/users/check-username', { params: { username } }),
  // 检查邮箱
  checkEmail: (email) => api.get('/users/check-email', { params: { email } })
}

// 健康检查API
export const healthApi = {
  // 健康检查
  health: () => api.get('/health'),
  // 获取统计数据
  getStats: () => api.get('/health/stats')
}

// 角色管理API
export const roleApi = {
  // 获取角色列表
  getRoles: (params) => api.get('/roles', { params }),
  // 获取角色详情
  getRole: (id) => api.get(`/roles/${id}`),
  // 创建角色
  createRole: (data) => api.post('/roles', data),
  // 更新角色
  updateRole: (id, data) => api.put(`/roles/${id}`, data),
  // 删除角色
  deleteRole: (id) => api.delete(`/roles/${id}`),
  // 获取启用角色
  getEnabledRoles: () => api.get('/roles/enabled'),
  // 更新角色状态
  updateRoleStatus: (id, status) => api.put(`/roles/${id}/status`, null, { params: { status } }),
  // 检查角色编码
  checkRoleCode: (roleCode) => api.get(`/roles/check-code/${roleCode}`),
  // 分配角色给用户
  assignRoleToUser: (userId, roleId) => api.post('/roles/assign', null, { params: { userId, roleId } }),
  // 撤销用户角色
  revokeRoleFromUser: (userId, roleId) => api.delete('/roles/revoke', { params: { userId, roleId } }),
  // 获取用户角色
  getUserRoles: (userId) => api.get(`/roles/user/${userId}`),
  // 检查用户角色
  checkUserRole: (userId, roleCode) => api.get('/roles/check', { params: { userId, roleCode } })
}

// 权限管理API
export const permissionApi = {
  // ==================== RBAC权限管理 ====================
  // 获取权限列表
  getPermissions: (params) => api.get('/permissions', { params }),
  // 获取权限详情
  getPermission: (id) => api.get(`/permissions/${id}`),
  // 创建权限
  createPermission: (data) => api.post('/permissions', data),
  // 更新权限
  updatePermission: (id, data) => api.put(`/permissions/${id}`, data),
  // 删除权限
  deletePermission: (id) => api.delete(`/permissions/${id}`),
  // 获取启用权限
  getEnabledPermissions: () => api.get('/permissions/enabled'),
  // 更新权限状态
  updatePermissionStatus: (id, status) => api.put(`/permissions/${id}/status`, null, { params: { status } }),
  // 检查权限编码
  checkPermissionCode: (permCode) => api.get(`/permissions/check-code/${permCode}`),
  // 为角色分配权限
  assignPermissionToRole: (roleId, permissionId) => api.post('/permissions/assign-to-role', null, { params: { roleId, permissionId } }),
  // 撤销角色权限
  revokePermissionFromRole: (roleId, permissionId) => api.delete('/permissions/revoke-from-role', { params: { roleId, permissionId } }),
  // 获取角色权限
  getRolePermissions: (roleId) => api.get(`/permissions/role/${roleId}`),
  // 获取用户权限
  getUserPermissions: (userId) => api.get(`/permissions/user/${userId}`),
  // 检查用户权限
  checkUserPermission: (userId, permCode) => api.get('/permissions/check', { params: { userId, permCode } }),
  // 获取用户所有权限
  getUserAllPermissions: (userId) => api.get(`/permissions/user/${userId}/all`),

  // ==================== 应用权限管理 ====================
  // 分配应用权限
  assignAppPermission: (userId, appId, permissionType) => api.post('/permissions/assign-app', null, {
    params: { userId, appId, permissionType }
  }),
  // 撤销应用权限
  revokeAppPermission: (userId, appId) => api.delete('/permissions/revoke-app', {
    params: { userId, appId }
  }),
  // 获取用户应用权限
  getUserAppPermissions: (userId) => api.get(`/permissions/app/user/${userId}`),
  // 获取应用用户权限
  getAppUserPermissions: (appId) => api.get(`/permissions/app/${appId}`),
  // 检查应用权限
  checkAppPermission: (userId, appId, permissionType) => api.get('/permissions/app/check', {
    params: { userId, appId, permissionType }
  }),
  // 获取所有应用权限列表
  getAllAppPermissions: () => api.get('/permissions/app/all')
}

export default api 