<template>
  <div class="user-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>用户管理</h2>
    </div>
    
    <!-- 用户角色说明提示框 -->
    <el-alert
      title="用户角色说明"
      type="info"
      :closable="false"
      class="role-alert"
    >
      <template #default>
        <div class="role-description">
          <p><strong>管理员：</strong>拥有全功能权限</p>
          <p><strong>普通用户：</strong>仅拥有借阅、个人查询、修改个人信息权限</p>
        </div>
      </template>
    </el-alert>
    
    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="management-tabs">
      <!-- 标签1：用户单条录入 -->
      <el-tab-pane label="用户单条录入" name="entry">
        <el-card class="tab-card">
          <el-form
            ref="entryFormRef"
            :model="entryForm"
            :rules="entryRules"
            label-width="120px"
            class="entry-form"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <!-- 学号/工号输入 -->
                <el-form-item label="学号/工号" prop="accountNumber">
                  <el-input
                    v-model="entryForm.accountNumber"
                    placeholder="请输入学号或工号"
                    clearable
                  />
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <!-- 姓名输入 -->
                <el-form-item label="姓名" prop="name">
                  <el-input
                    v-model="entryForm.name"
                    placeholder="请输入姓名"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <!-- 身份选择 -->
                <el-form-item label="身份" prop="identity">
                  <el-select
                    v-model="entryForm.identity"
                    placeholder="请选择身份"
                    style="width: 100%"
                  >
                    <el-option label="教师" value="教师" />
                    <el-option label="学生" value="学生" />
                  </el-select>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <!-- 校园卡号输入 -->
                <el-form-item label="校园卡号" prop="cardNumber">
                  <el-input
                    v-model="entryForm.cardNumber"
                    placeholder="请输入校园卡号"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <!-- 提示信息 -->
            <el-row>
              <el-col :span="24">
                <el-alert
                  title="初始密码将自动设置为学号/工号后6位"
                  type="warning"
                  :closable="false"
                  show-icon
                  class="password-tip"
                />
              </el-col>
            </el-row>
            
            <!-- 操作按钮 -->
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="entryLoading"
                @click="handleUserEntry"
              >
                提交录入
              </el-button>
              <el-button size="large" @click="resetEntryForm">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 标签2：用户密码重置 -->
      <el-tab-pane label="用户密码重置" name="reset">
        <el-card class="tab-card">
          <!-- 查询表单 -->
          <el-form :inline="true" class="query-form">
            <el-form-item label="学号/工号">
              <el-input
                v-model="resetForm.accountNumber"
                placeholder="请输入学号或工号"
                clearable
                style="width: 220px"
              />
            </el-form-item>
            
            <el-form-item label="校园卡号">
              <el-input
                v-model="resetForm.cardNumber"
                placeholder="请输入校园卡号"
                clearable
                style="width: 220px"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleQueryUser">
                查询用户
              </el-button>
            </el-form-item>
          </el-form>
          
          <el-divider />
          
          <!-- 用户信息展示 -->
          <div v-if="currentUser" class="user-info-section">
            <h3>用户信息</h3>
            <el-descriptions :column="2" border class="user-info">
              <el-descriptions-item label="学号/工号">
                {{ currentUser.accountNumber }}
              </el-descriptions-item>
              <el-descriptions-item label="姓名">
                {{ currentUser.name }}
              </el-descriptions-item>
              <el-descriptions-item label="身份">
                <el-tag :type="currentUser.identity === '教师' ? 'success' : 'primary'">
                  {{ currentUser.identity }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="校园卡号">
                {{ currentUser.cardNumber }}
              </el-descriptions-item>
              <el-descriptions-item label="账号状态">
                <el-tag :type="currentUser.status === '正常' ? 'success' : 'danger'">
                  {{ currentUser.status }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="用户角色">
                {{ currentUser.role === 'admin' ? '管理员' : '普通用户' }}
              </el-descriptions-item>
            </el-descriptions>
            
            <!-- 重置密码操作 -->
            <div class="reset-action-section">
              <el-alert
                title="密码将被重置为学号/工号后6位"
                type="warning"
                :closable="false"
                show-icon
                class="reset-tip"
              />
              
              <el-button
                type="danger"
                size="large"
                :loading="resetLoading"
                @click="handleResetPassword"
              >
                重置密码
              </el-button>
            </div>
          </div>
          
          <el-empty v-else description="请先查询用户" />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserDataStore } from '@/stores/userData'

const userDataStore = useUserDataStore()

// 当前激活的标签页
const activeTab = ref('entry')

// ========== 用户录入相关 ==========
const entryFormRef = ref(null)
const entryLoading = ref(false)

// 录入表单数据
const entryForm = reactive({
  accountNumber: '',
  name: '',
  identity: '',
  cardNumber: ''
})

// 表单验证规则
const entryRules = {
  accountNumber: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' },
    { min: 3, max: 20, message: '学号/工号长度为3-20个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为2-20个字符', trigger: 'blur' }
  ],
  identity: [
    { required: true, message: '请选择身份', trigger: 'change' }
  ],
  cardNumber: [
    { required: true, message: '请输入校园卡号', trigger: 'blur' },
    { min: 3, max: 20, message: '校园卡号长度为3-20个字符', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        // 校园卡号唯一性校验
        if (userDataStore.isCardNumberExist(value.trim())) {
          callback(new Error('该校园卡号已存在'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

/**
 * 提交用户录入
 */
const handleUserEntry = async () => {
  if (!entryFormRef.value) return
  
  try {
    // 表单验证
    await entryFormRef.value.validate()
    
    entryLoading.value = true
    
    // 模拟网络延迟
    setTimeout(() => {
      const result = userDataStore.addUser({
        accountNumber: entryForm.accountNumber.trim(),
        name: entryForm.name.trim(),
        identity: entryForm.identity,
        cardNumber: entryForm.cardNumber.trim()
      })
      
      if (result.success) {
        ElMessage.success({
          message: `用户录入成功！初始密码为：${result.initialPassword}`,
          duration: 5000
        })
        
        // 清空表单
        resetEntryForm()
      } else {
        ElMessage.error(result.message)
      }
      
      entryLoading.value = false
    }, 500)
    
  } catch (error) {
    console.log('表单验证失败:', error)
  }
}

/**
 * 重置录入表单
 */
const resetEntryForm = () => {
  if (entryFormRef.value) {
    entryFormRef.value.resetFields()
  }
  Object.keys(entryForm).forEach(key => {
    entryForm[key] = ''
  })
}

// ========== 密码重置相关 ==========
const resetLoading = ref(false)

// 重置表单数据
const resetForm = reactive({
  accountNumber: '',
  cardNumber: ''
})

// 当前查询到的用户
const currentUser = ref(null)

/**
 * 查询用户
 */
const handleQueryUser = () => {
  if (!resetForm.accountNumber.trim()) {
    ElMessage.warning('请输入学号/工号')
    return
  }
  
  if (!resetForm.cardNumber.trim()) {
    ElMessage.warning('请输入校园卡号')
    return
  }
  
  const user = userDataStore.getUserByAccountAndCard(
    resetForm.accountNumber.trim(),
    resetForm.cardNumber.trim()
  )
  
  if (user) {
    currentUser.value = user
    ElMessage.success('查询成功')
  } else {
    currentUser.value = null
    ElMessage.error('未找到该用户，请确认学号/工号和校园卡号')
  }
}

/**
 * 重置密码
 */
const handleResetPassword = () => {
  if (!currentUser.value) {
    ElMessage.warning('请先查询用户')
    return
  }
  
  ElMessageBox.confirm(
    `确定要重置用户【${currentUser.value.name}】的密码吗？`,
    '密码重置确认',
    {
      confirmButtonText: '确定重置',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    resetLoading.value = true
    
    setTimeout(() => {
      const result = userDataStore.resetUserPassword(
        currentUser.value.accountNumber,
        currentUser.value.cardNumber
      )
      
      if (result.success) {
        ElMessage.success({
          message: `密码重置成功！新密码为学号/工号后6位：${result.initialPassword}`,
          duration: 5000
        })
        
        // 刷新用户信息
        currentUser.value = userDataStore.getUserByAccountAndCard(
          currentUser.value.accountNumber,
          currentUser.value.cardNumber
        )
      } else {
        ElMessage.error(result.message)
      }
      
      resetLoading.value = false
    }, 500)
  }).catch(() => {
    ElMessage.info('已取消密码重置')
  })
}

// ========== 生命周期 ==========
onMounted(() => {
  // 恢复用户数据
  userDataStore.restoreFromStorage()
  
  // 初始化模拟数据（如果没有数据）
  userDataStore.initMockData()
})
</script>

<style scoped>
/* 页面容器 */
.user-management-container {
  padding: 20px;
}

/* 页面标题 */
.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 角色说明提示框 */
.role-alert {
  margin-bottom: 20px;
}

.role-description {
  line-height: 1.8;
}

.role-description p {
  margin: 5px 0;
}

.role-description strong {
  color: var(--primary-color);
}

/* 标签页 */
.management-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

/* 标签卡片 */
.tab-card {
  margin-top: 0;
}

/* 录入表单 */
.entry-form {
  max-width: 900px;
}

/* 密码提示 */
.password-tip {
  margin-bottom: 20px;
}

/* 查询表单 */
.query-form {
  margin-bottom: 20px;
}

/* 用户信息区域 */
.user-info-section {
  margin-top: 20px;
}

.user-info-section h3 {
  margin: 20px 0 15px 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

.user-info {
  margin-bottom: 30px;
}

/* 重置密码操作区域 */
.reset-action-section {
  margin-top: 30px;
}

.reset-tip {
  margin-bottom: 20px;
}

/* 按钮间距 */
:deep(.el-form-item) {
  margin-right: 20px;
}

/* Alert 样式优化 */
:deep(.el-alert__title) {
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-alert__description) {
  font-size: 14px;
  line-height: 1.6;
}
</style>
