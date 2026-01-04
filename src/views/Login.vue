<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="login-bg"></div>
    
    <!-- 登录卡片 -->
    <el-card class="login-card">
      <!-- 标题 -->
      <div class="login-header">
        <h2>高校图书馆管理系统</h2>
        <p>Library Management System</p>
      </div>
      
      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <!-- 账号输入框 -->
        <el-form-item prop="account">
          <el-input
            v-model="loginForm.account"
            placeholder="请输入学号/工号/管理员账号"
            size="large"
            clearable
            :disabled="loading"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            clearable
            :disabled="loading"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <!-- 忘记密码链接 -->
        <div class="forgot-password">
          <el-link type="primary" @click="handleForgotPassword">
            忘记密码？
          </el-link>
        </div>
        
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 提示信息 -->
      <div class="login-tips">
        <el-text type="info" size="small">
          提示：连续3次密码错误将锁定账号30分钟
        </el-text>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import CryptoJS from 'crypto-js'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  account: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
}

/**
 * 模拟用户数据库
 * 实际项目中应从后端接口获取
 */
const mockUsers = {
  'admin': {
    account: 'admin',
    password: encryptPassword('admin123'), // 默认密码：admin123
    role: 'admin',
    name: '系统管理员'
  },
  '2021001': {
    account: '2021001',
    password: encryptPassword('123456'), // 默认密码：123456
    role: 'user',
    name: '张三'
  },
  '2021002': {
    account: '2021002',
    password: encryptPassword('123456'), // 默认密码：123456
    role: 'user',
    name: '李四'
  },
  'T001': {
    account: 'T001',
    password: encryptPassword('123456'), // 默认密码：123456
    role: 'user',
    name: '王老师'
  }
}

/**
 * 密码加密函数
 * 使用SHA256加密
 * @param {string} password - 明文密码
 * @returns {string} - 加密后的密码
 */
function encryptPassword(password) {
  return CryptoJS.SHA256(password).toString()
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  // 表单验证
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
  } catch (error) {
    return
  }
  
  // 检查账号是否被锁定
  const lockStatus = userStore.checkAccountLock(loginForm.account)
  if (lockStatus.isLocked) {
    ElMessage.error(`账号已被锁定，请${lockStatus.remainingTime}分钟后再试`)
    return
  }
  
  loading.value = true
  
  // 模拟网络延迟
  setTimeout(() => {
    // 验证账号是否存在
    const user = mockUsers[loginForm.account]
    
    if (!user) {
      ElMessage.error('账号不存在')
      loading.value = false
      return
    }
    
    // 加密输入的密码
    const encryptedPassword = encryptPassword(loginForm.password)
    
    // 验证密码
    if (encryptedPassword !== user.password) {
      // 记录登录失败
      const failResult = userStore.recordLoginFail(loginForm.account)
      
      if (failResult.isLocked) {
        ElMessage.error(`密码错误次数过多，账号已被锁定30分钟`)
      } else {
        ElMessage.error(`密码错误，您还有${3 - failResult.count}次机会`)
      }
      
      loading.value = false
      return
    }
    
    // 登录成功
    ElMessage.success('登录成功')
    
    // 保存用户信息到store
    userStore.login({
      account: user.account,
      name: user.name
    }, user.role)
    
    loading.value = false
    
    // 根据角色跳转到对应页面
    if (user.role === 'admin') {
      router.push('/admin')
    } else {
      router.push('/user')
    }
  }, 800) // 模拟800ms网络延迟
}

/**
 * 处理忘记密码
 */
const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}
</script>

<style scoped>
/* 登录容器 - 全屏居中 */
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.login-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

/* 登录卡片 */
.login-card {
  width: 420px;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
}

/* 登录标题 */
.login-header {
  text-align: center;
  margin-bottom: 35px;
}

.login-header h2 {
  font-size: 26px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.login-header p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* 登录表单 */
.login-form {
  margin-top: 20px;
}

/* 忘记密码 */
.forgot-password {
  text-align: right;
  margin-top: -10px;
  margin-bottom: 20px;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 10px;
}

/* 提示信息 */
.login-tips {
  text-align: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media screen and (max-width: 480px) {
  .login-card {
    width: 90%;
    margin: 0 20px;
  }
}
</style>
