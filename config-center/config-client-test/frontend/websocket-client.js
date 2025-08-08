/**
 * WebSocket客户端示例
 * 用于接收配置中心的配置推送
 */

class ConfigWebSocketClient {
  constructor(appId, instanceId, instanceIp, clientVersion) {
    this.appId = appId
    this.instanceId = instanceId
    this.instanceIp = instanceIp
    this.clientVersion = clientVersion
    this.stompClient = null
    this.connected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 5000 // 5秒
  }

  // 连接到WebSocket服务器
  connect() {
    try {
      // 创建WebSocket连接
      const socket = new SockJS('http://localhost:8080/ws')
      this.stompClient = Stomp.over(socket)
      
      // 设置连接选项
      this.stompClient.connect({}, 
        // 连接成功回调
        (frame) => {
          console.log('WebSocket连接成功:', frame)
          this.connected = true
          this.reconnectAttempts = 0
          
          // 发送客户端注册信息
          this.registerClient()
          
          // 订阅配置更新主题
          this.subscribeToConfigUpdates()
          
          // 启动心跳
          this.startHeartbeat()
        },
        // 连接失败回调
        (error) => {
          console.error('WebSocket连接失败:', error)
          this.connected = false
          this.handleReconnect()
        }
      )
    } catch (error) {
      console.error('创建WebSocket连接失败:', error)
      this.handleReconnect()
    }
  }

  // 注册客户端
  registerClient() {
    if (!this.stompClient || !this.connected) return
    
    const message = {
      appId: this.appId,
      instanceId: this.instanceId,
      instanceIp: this.instanceIp,
      clientVersion: this.clientVersion
    }
    
    this.stompClient.send('/app/connect', {}, JSON.stringify(message))
    console.log('客户端注册信息已发送:', message)
  }

  // 订阅配置更新
  subscribeToConfigUpdates() {
    if (!this.stompClient || !this.connected) return
    
    // 订阅应用配置更新
    this.stompClient.subscribe(`/topic/app/${this.appId}/config`, (message) => {
      this.handleConfigUpdate(message)
    })
    
    // 订阅环境配置更新
    this.stompClient.subscribe(`/topic/app/${this.appId}/env/*/config`, (message) => {
      this.handleConfigUpdate(message)
    })
    
    // 订阅实例配置更新
    this.stompClient.subscribe(`/topic/instance/${this.instanceId}/config`, (message) => {
      this.handleConfigUpdate(message)
    })
    
    // 订阅配置变更通知
    this.stompClient.subscribe(`/topic/app/${this.appId}/notifications`, (message) => {
      this.handleConfigNotification(message)
    })
    
    console.log('已订阅配置更新主题')
  }

  // 处理配置更新
  handleConfigUpdate(message) {
    try {
      const data = JSON.parse(message.body)
      console.log('收到配置更新:', data)
      
      if (data.type === 'CONFIG_UPDATE') {
        // 处理配置更新
        this.updateLocalConfig(data.configData)
        
        // 显示通知
        this.showNotification('配置已更新', `应用 ${data.appId} 的配置已更新到版本 ${data.versionNumber || '最新'}`)
      }
    } catch (error) {
      console.error('处理配置更新失败:', error)
    }
  }

  // 处理配置变更通知
  handleConfigNotification(message) {
    try {
      const data = JSON.parse(message.body)
      console.log('收到配置变更通知:', data)
      
      if (data.type === 'CONFIG_CHANGE_NOTIFICATION') {
        this.showNotification('配置变更通知', 
          `应用 ${data.appId} 环境 ${data.envId} 的配置已变更，版本: ${data.versionNumber}`)
      }
    } catch (error) {
      console.error('处理配置变更通知失败:', error)
    }
  }

  // 更新本地配置
  updateLocalConfig(configData) {
    try {
      // 这里应该根据实际需求更新本地配置
      // 例如：更新环境变量、配置文件、内存中的配置等
      
      console.log('更新本地配置:', configData)
      
      // 示例：更新环境变量
      Object.keys(configData).forEach(key => {
        process.env[key] = configData[key]
      })
      
      // 示例：保存到配置文件
      this.saveConfigToFile(configData)
      
      // 示例：重启应用或热加载配置
      this.reloadApplication()
      
    } catch (error) {
      console.error('更新本地配置失败:', error)
    }
  }

  // 保存配置到文件
  saveConfigToFile(configData) {
    // 这里应该根据实际需求保存配置到文件
    // 例如：保存为JSON、YAML、Properties等格式
    
    const fs = require('fs')
    const path = require('path')
    
    try {
      const configPath = path.join(process.cwd(), 'config', 'application.json')
      const configDir = path.dirname(configPath)
      
      // 确保目录存在
      if (!fs.existsSync(configDir)) {
        fs.mkdirSync(configDir, { recursive: true })
      }
      
      // 保存配置
      fs.writeFileSync(configPath, JSON.stringify(configData, null, 2))
      console.log('配置已保存到文件:', configPath)
    } catch (error) {
      console.error('保存配置到文件失败:', error)
    }
  }

  // 重启应用
  reloadApplication() {
    // 这里应该根据实际需求重启应用或热加载配置
    console.log('应用配置已更新，建议重启应用或热加载配置')
    
    // 示例：发送信号给进程
    // process.kill(process.pid, 'SIGUSR2')
  }

  // 启动心跳
  startHeartbeat() {
    if (!this.stompClient || !this.connected) return
    
    this.heartbeatInterval = setInterval(() => {
      if (this.connected) {
        this.stompClient.send('/app/heartbeat', {}, JSON.stringify({}))
      }
    }, 30000) // 30秒发送一次心跳
  }

  // 停止心跳
  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  // 处理重连
  handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('达到最大重连次数，停止重连')
      return
    }
    
    this.reconnectAttempts++
    console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
    
    setTimeout(() => {
      this.connect()
    }, this.reconnectInterval)
  }

  // 断开连接
  disconnect() {
    this.stopHeartbeat()
    
    if (this.stompClient) {
      this.stompClient.send('/app/disconnect', {}, JSON.stringify({}))
      this.stompClient.disconnect()
    }
    
    this.connected = false
    console.log('WebSocket连接已断开')
  }

  // 显示通知
  showNotification(title, message) {
    // 这里应该根据实际需求显示通知
    // 例如：桌面通知、日志记录、邮件通知等
    
    console.log(`[${title}] ${message}`)
    
    // 示例：桌面通知（如果支持）
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(title, { body: message })
    }
  }
}

// 使用示例
function createConfigClient() {
  // 从环境变量或配置文件获取客户端信息
  const appId = process.env.APP_ID || 1
  const instanceId = process.env.INSTANCE_ID || `instance-${Date.now()}`
  const instanceIp = process.env.INSTANCE_IP || '127.0.0.1'
  const clientVersion = process.env.CLIENT_VERSION || '1.0.0'
  
  const client = new ConfigWebSocketClient(appId, instanceId, instanceIp, clientVersion)
  
  // 连接WebSocket
  client.connect()
  
  // 处理进程退出
  process.on('SIGINT', () => {
    console.log('正在断开WebSocket连接...')
    client.disconnect()
    process.exit(0)
  })
  
  return client
}

// 如果在浏览器环境中使用
if (typeof window !== 'undefined') {
  // 浏览器环境
  window.ConfigWebSocketClient = ConfigWebSocketClient
  window.createConfigClient = createConfigClient
} else {
  // Node.js环境
  module.exports = {
    ConfigWebSocketClient,
    createConfigClient
  }
}

// 自动创建客户端（如果在浏览器中直接运行）
if (typeof window !== 'undefined' && !window.configClient) {
  window.configClient = createConfigClient()
}
