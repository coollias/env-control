import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Applications from '../views/Applications.vue'
import Environments from '../views/Environments.vue'
import Configs from '../views/Configs.vue'
import AppEnvironments from '../views/AppEnvironments.vue'
import ConfigVersions from '../views/ConfigVersions.vue'
import Client from '../views/Client.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import PermissionManagement from '../views/PermissionManagement.vue'
import SystemPermissionManagement from '../views/SystemPermissionManagement.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/applications',
    name: 'Applications',
    component: Applications,
    meta: { requiresAuth: true }
  },
  {
    path: '/environments',
    name: 'Environments',
    component: Environments,
    meta: { requiresAuth: true }
  },
  {
    path: '/configs',
    name: 'Configs',
    component: Configs,
    meta: { requiresAuth: true }
  },
  {
    path: '/app-environments',
    name: 'AppEnvironments',
    component: AppEnvironments,
    meta: { requiresAuth: true }
  },
  {
    path: '/config-versions',
    name: 'ConfigVersions',
    component: ConfigVersions,
    meta: { requiresAuth: true }
  },
  {
    path: '/client',
    name: 'Client',
    component: Client,
    meta: { requiresAuth: true }
  },
  {
    path: '/permissions',
    name: 'PermissionManagement',
    component: PermissionManagement,
    meta: { requiresAuth: true }
  },
  {
    path: '/system-permissions',
    name: 'SystemPermissionManagement',
    component: SystemPermissionManagement,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
    } else {
      next()
    }
  } else {
    if (token && (to.path === '/login' || to.path === '/register')) {
      next('/')
    } else {
      next()
    }
  }
})

export default router 