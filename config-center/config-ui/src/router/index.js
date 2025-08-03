import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Applications from '../views/Applications.vue'
import Environments from '../views/Environments.vue'
import Configs from '../views/Configs.vue'
import Client from '../views/Client.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/applications',
    name: 'Applications',
    component: Applications
  },
  {
    path: '/environments',
    name: 'Environments',
    component: Environments
  },
  {
    path: '/configs',
    name: 'Configs',
    component: Configs
  },
  {
    path: '/client',
    name: 'Client',
    component: Client
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 