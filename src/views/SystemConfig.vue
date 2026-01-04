<template>
  <div class="system-config-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统基础配置</h2>
    </div>
    
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>借阅参数配置</span>
          <el-tag type="info" size="small">修改后立即生效</el-tag>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="150px"
        label-position="right"
        class="config-form"
      >
        <!-- 教师借阅期限 -->
        <el-form-item label="教师借阅期限" prop="teacherBorrowDays">
          <el-input-number
            v-model="formData.teacherBorrowDays"
            :min="30"
            :max="365"
            :step="1"
            :precision="0"
            controls-position="right"
            class="input-number"
          />
          <span class="input-unit">天</span>
          <span class="input-tip">（最少30天，最多365天）</span>
        </el-form-item>
        
        <!-- 学生借阅期限 -->
        <el-form-item label="学生借阅期限" prop="studentBorrowDays">
          <el-input-number
            v-model="formData.studentBorrowDays"
            :min="30"
            :max="365"
            :step="1"
            :precision="0"
            controls-position="right"
            class="input-number"
          />
          <span class="input-unit">天</span>
          <span class="input-tip">（最少30天，最多365天）</span>
        </el-form-item>
        
        <!-- 单次最大借阅数 -->
        <el-form-item label="单次最大借阅数" prop="maxBorrowCount">
          <el-input-number
            v-model="formData.maxBorrowCount"
            :min="1"
            :max="20"
            :step="1"
            :precision="0"
            controls-position="right"
            class="input-number"
          />
          <span class="input-unit">本</span>
          <span class="input-tip">（最少1本，最多20本）</span>
        </el-form-item>
        
        <!-- 操作按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            :loading="saveLoading"
            @click="handleSave"
          >
            保存参数
          </el-button>
          <el-button
            :loading="resetLoading"
            @click="handleReset"
          >
            恢复默认
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 参数说明 -->
      <el-divider />
      
      <div class="config-description">
        <h4>参数说明：</h4>
        <ul>
          <li><strong>教师借阅期限</strong>：教师身份用户借阅图书的最长期限，到期后需归还或续借</li>
          <li><strong>学生借阅期限</strong>：学生身份用户借阅图书的最长期限，到期后需归还或续借</li>
          <li><strong>单次最大借阅数</strong>：用户单次最多可借阅的图书数量，限制借阅行为</li>
        </ul>
        
        <el-alert
          title="温馨提示"
          type="warning"
          :closable="false"
          show-icon
          class="config-alert"
        >
          <template #default>
            <p>1. 修改参数后立即生效，影响后续所有借阅操作</p>
            <p>2. 已借阅的图书不受参数修改影响，仍按原借阅期限执行</p>
            <p>3. 建议根据实际情况合理设置参数，避免频繁修改</p>
          </template>
        </el-alert>
      </div>
      
      <!-- 默认值展示 -->
      <el-divider />
      
      <div class="default-values">
        <h4>默认参数值：</h4>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="教师借阅期限">
            {{ configStore.DEFAULT_CONFIG.teacherBorrowDays }} 天
          </el-descriptions-item>
          <el-descriptions-item label="学生借阅期限">
            {{ configStore.DEFAULT_CONFIG.studentBorrowDays }} 天
          </el-descriptions-item>
          <el-descriptions-item label="单次最大借阅数">
            {{ configStore.DEFAULT_CONFIG.maxBorrowCount }} 本
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useSystemConfigStore } from '@/stores/systemConfig'

const configStore = useSystemConfigStore()

const formRef = ref(null)
const saveLoading = ref(false)
const resetLoading = ref(false)

// 表单数据
const formData = reactive({
  teacherBorrowDays: 90,
  studentBorrowDays: 60,
  maxBorrowCount: 5
})

// 表单验证规则
const rules = {
  teacherBorrowDays: [
    { required: true, message: '请输入教师借阅期限', trigger: 'blur' },
    { 
      type: 'number', 
      min: 30, 
      max: 365, 
      message: '借阅期限必须在30-365天之间', 
      trigger: 'change' 
    }
  ],
  studentBorrowDays: [
    { required: true, message: '请输入学生借阅期限', trigger: 'blur' },
    { 
      type: 'number', 
      min: 30, 
      max: 365, 
      message: '借阅期限必须在30-365天之间', 
      trigger: 'change' 
    }
  ],
  maxBorrowCount: [
    { required: true, message: '请输入单次最大借阅数', trigger: 'blur' },
    { 
      type: 'number', 
      min: 1, 
      max: 20, 
      message: '最大借阅数必须在1-20本之间', 
      trigger: 'change' 
    }
  ]
}

/**
 * 加载当前配置
 */
const loadConfig = () => {
  formData.teacherBorrowDays = configStore.config.teacherBorrowDays
  formData.studentBorrowDays = configStore.config.studentBorrowDays
  formData.maxBorrowCount = configStore.config.maxBorrowCount
}

/**
 * 保存参数
 */
const handleSave = async () => {
  // 验证表单
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      saveLoading.value = true
      
      setTimeout(() => {
        // 更新配置
        configStore.updateConfig({
          teacherBorrowDays: formData.teacherBorrowDays,
          studentBorrowDays: formData.studentBorrowDays,
          maxBorrowCount: formData.maxBorrowCount
        })
        
        saveLoading.value = false
        ElMessage.success('参数保存成功')
      }, 300)
    } else {
      ElMessage.error('请检查表单输入')
      return false
    }
  })
}

/**
 * 恢复默认
 */
const handleReset = () => {
  ElMessageBox.confirm(
    '确定要恢复默认参数吗？此操作将覆盖当前配置。',
    '恢复默认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      resetLoading.value = true
      
      setTimeout(() => {
        // 恢复默认配置
        configStore.resetToDefault()
        
        // 重新加载表单数据
        loadConfig()
        
        resetLoading.value = false
        ElMessage.success('已恢复默认参数')
      }, 300)
    })
    .catch(() => {
      // 取消操作
    })
}

onMounted(() => {
  // 恢复配置
  configStore.restoreFromStorage()
  
  // 加载当前配置
  loadConfig()
})
</script>

<style scoped>
/* 页面容器 */
.system-config-container {
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

/* 配置卡片 */
.config-card {
  max-width: 900px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span:first-child {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 配置表单 */
.config-form {
  margin-top: 20px;
  padding: 20px;
  background-color: #f9fafb;
  border-radius: 8px;
}

/* 数字输入框 */
.input-number {
  width: 200px;
}

/* 输入单位 */
.input-unit {
  margin-left: 10px;
  font-size: 14px;
  color: var(--text-primary);
}

/* 输入提示 */
.input-tip {
  margin-left: 15px;
  font-size: 12px;
  color: var(--text-secondary);
}

/* 表单项优化 */
:deep(.el-form-item) {
  margin-bottom: 28px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary);
}

:deep(.el-form-item__content) {
  display: flex;
  align-items: center;
}

/* 按钮样式 */
:deep(.el-button) {
  min-width: 120px;
  margin-right: 15px;
}

/* 参数说明 */
.config-description {
  margin-top: 20px;
}

.config-description h4 {
  margin: 0 0 15px 0;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.config-description ul {
  margin: 0;
  padding-left: 20px;
  line-height: 2;
}

.config-description li {
  font-size: 14px;
  color: var(--text-regular);
}

.config-description strong {
  color: var(--text-primary);
}

/* 提示框 */
.config-alert {
  margin-top: 15px;
}

.config-alert p {
  margin: 5px 0;
  font-size: 14px;
  line-height: 1.6;
}

/* 默认值展示 */
.default-values {
  margin-top: 20px;
}

.default-values h4 {
  margin: 0 0 15px 0;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 数字输入框优化 */
:deep(.el-input-number) {
  line-height: 32px;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
  padding-left: 15px;
}

/* 描述列表优化 */
:deep(.el-descriptions__label) {
  font-weight: 500;
}

:deep(.el-descriptions__content) {
  color: var(--primary-color);
  font-weight: 500;
}
</style>
