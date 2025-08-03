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
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

// 应用管理API
export const applicationApi = {
  // 获取应用列表
  getApplications: (params) => api.get('/applications', { params }),
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
  searchConfigs: (params) => api.get('/config-items/search', { params })
}

// 客户端API
export const clientApi = {
  // 获取配置
  getConfig: (appId, envId) => api.get(`/client/configs/${appId}/${envId}`),
  // 获取配置项
  getConfigItem: (appId, envId, key) => api.get(`/client/config/${appId}/${envId}/${key}`)
}

// 健康检查API
export const healthApi = {
  // 健康检查
  health: () => api.get('/health')
}

export default api 