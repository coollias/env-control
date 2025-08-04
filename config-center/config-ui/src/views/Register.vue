<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>配置中心</h2>
        <p>创建您的账户</p>
      </div>
      
      <el-form 
        ref="registerForm" 
        :model="registerForm" 
        :rules="registerRules" 
        class="register-form"
        @submit.native.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名"
            prefix-icon="el-icon-user"
            size="large"
            @blur="checkUsername"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="el-icon-lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            prefix-icon="el-icon-lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="真实姓名"
            prefix-icon="el-icon-user"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="邮箱"
            prefix-icon="el-icon-message"
            size="large"
            @blur="checkEmail"
          />
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="手机号（可选）"
            prefix-icon="el-icon-phone"
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-button"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        
        <div class="register-footer">
          <span>已有账户？</span>
          <el-link type="primary" @click="goToLogin">立即登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { authApi } from '@/api'

export default {
  name: 'Register',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      loading: false,
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        realName: '',
        email: '',
        phone: ''
      },
      registerRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    async handleRegister() {
      try {
        await this.$refs.registerForm.validate()
        this.loading = true
        
        console.log('发送注册请求:', this.registerForm)
        const response = await authApi.register(this.registerForm)
        console.log('注册响应:', response)
        
        if (response.success) {
          // 保存token和用户信息
          localStorage.setItem('token', response.data.token)
          localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
          
          this.$message.success('注册成功')
          this.$router.push('/')
        } else {
          this.$message.error(response.message || '注册失败')
        }
      } catch (error) {
        console.error('注册错误:', error)
        console.error('错误详情:', error.response)
        this.$message.error(error.message || '注册失败')
      } finally {
        this.loading = false
      }
    },
    
    async checkUsername() {
      if (this.registerForm.username) {
        try {
          const response = await authApi.checkUsername(this.registerForm.username)
          if (!response.data) {
            this.$message.error('用户名已存在')
          }
        } catch (error) {
          console.error('检查用户名错误:', error)
        }
      }
    },
    
    async checkEmail() {
      if (this.registerForm.email) {
        try {
          const response = await authApi.checkEmail(this.registerForm.email)
          if (!response.data) {
            this.$message.error('邮箱已存在')
          }
        } catch (error) {
          console.error('检查邮箱错误:', error)
        }
      }
    },
    
    goToLogin() {
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #333;
  margin-bottom: 10px;
}

.register-header p {
  color: #666;
  font-size: 14px;
}

.register-form {
  margin-top: 20px;
}

.register-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.register-footer .el-link {
  margin-left: 5px;
}
</style> 