<template>
  <div id="app">
    <!-- 登录和注册页面不显示导航栏 -->
    <div v-if="isAuthPage" style="height: 100vh;">
      <router-view />
    </div>
    
    <!-- 其他页面显示完整布局 -->
    <el-container v-else style="height: 100vh;">
      <el-header style="background-color: #409EFF; color: white; display: flex; align-items: center;">
        <h2 style="margin: 0;">配置中心管理系统</h2>
      </el-header>
      
      <el-container>
        <el-aside width="200px" style="background-color: #f5f5f5;">
          <el-menu
            :default-active="$route.path"
            router
            style="border-right: none;">
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/applications">
              <el-icon><Grid /></el-icon>
              <span>应用管理</span>
            </el-menu-item>
            <el-menu-item index="/environments">
              <el-icon><Setting /></el-icon>
              <span>环境管理</span>
            </el-menu-item>
            <el-menu-item index="/configs">
              <el-icon><Document /></el-icon>
              <span>配置管理</span>
            </el-menu-item>
            <el-menu-item index="/app-environments">
              <el-icon><View /></el-icon>
              <span>应用环境</span>
            </el-menu-item>
            <el-menu-item index="/client">
              <el-icon><Connection /></el-icon>
              <span>客户端API</span>
            </el-menu-item>
                    <el-menu-item index="/permissions">
                      <el-icon><Lock /></el-icon>
                      <span>应用权限</span>
                    </el-menu-item>
                    <el-menu-item index="/system-permissions">
                      <el-icon><Setting /></el-icon>
                      <span>系统权限</span>
                    </el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { House, Grid, Setting, Document, View, Connection, Lock } from '@element-plus/icons-vue'

const route = useRoute()

// 判断当前页面是否是认证页面（登录或注册）
const isAuthPage = computed(() => {
  return route.path === '/login' || route.path === '/register'
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
</style> 