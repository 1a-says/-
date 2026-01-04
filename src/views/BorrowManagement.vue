<template>
  <div class="borrow-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>借阅管理</h2>
    </div>
    
    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="management-tabs">
      <!-- 标签1：图书借阅 -->
      <el-tab-pane label="图书借阅" name="borrow">
        <el-card class="tab-card">
          <!-- 输入区域 -->
          <div class="input-section">
            <h3 class="section-title">借阅信息录入</h3>
            <el-form :inline="true" class="borrow-form">
              <!-- 校园卡号输入 -->
              <el-form-item label="校园卡号" class="form-item-wide">
                <el-input
                  v-model="borrowForm.cardNumber"
                  placeholder="请输入或扫描校园卡号"
                  clearable
                  style="width: 300px"
                  @focus="handleScanFocus('card')"
                  @keyup.enter="handleValidateBorrow"
                />
              </el-form-item>
              
              <!-- 馆藏号输入 -->
              <el-form-item label="馆藏号" class="form-item-wide">
                <el-input
                  v-model="borrowForm.collectionNumbers"
                  placeholder="请输入或扫描馆藏号，多本用逗号分隔（最多5本）"
                  clearable
                  style="width: 500px"
                  @focus="handleScanFocus('collection')"
                  @keyup.enter="handleValidateBorrow"
                />
              </el-form-item>
              
              <!-- 查询验证按钮 -->
              <el-form-item>
                <el-button
                  type="primary"
                  :loading="borrowLoading"
                  @click="handleValidateBorrow"
                >
                  查询验证
                </el-button>
                <el-button @click="resetBorrowForm">
                  重置
                </el-button>
              </el-form-item>
            </el-form>
            
            <el-alert
              :title="`提示：单次最多可借阅${configStore.getMaxBorrowCount()}本图书`"
              type="info"
              :closable="false"
              show-icon
              class="borrow-tip"
            />
          </div>
          
          <el-divider />
          
          <!-- 展示区域 -->
          <div v-if="borrowValidation" class="display-section">
            <h3 class="section-title">借阅信息确认</h3>
            
            <!-- 用户信息 -->
            <div class="info-block">
              <h4>借阅人信息</h4>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="姓名">
                  {{ borrowValidation.user.name }}
                </el-descriptions-item>
                <el-descriptions-item label="身份">
                  <el-tag :type="borrowValidation.user.identity === '教师' ? 'success' : 'primary'">
                    {{ borrowValidation.user.identity }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="学号/工号">
                  {{ borrowValidation.user.accountNumber }}
                </el-descriptions-item>
                <el-descriptions-item label="校园卡号">
                  {{ borrowValidation.user.cardNumber }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
            
            <!-- 图书信息 -->
            <div class="info-block">
              <h4>图书信息（共{{ borrowValidation.books.length }}本）</h4>
              <el-table :data="borrowValidation.books" border stripe>
                <el-table-column type="index" label="序号" width="60" />
                <el-table-column prop="collectionNumber" label="馆藏号" width="180" />
                <el-table-column prop="title" label="书名" min-width="200" />
                <el-table-column prop="author" label="作者" width="150" />
                <el-table-column prop="publisher" label="出版社" width="180" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag type="success">{{ scope.row.status }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <!-- 借阅信息 -->
            <div class="info-block">
              <el-alert
                :title="`应还日期：${formatDate(borrowDueDate)}（${borrowValidation.user.identity === '教师' ? '90' : '60'}天借期）`"
                type="warning"
                :closable="false"
                show-icon
              />
            </div>
            
            <!-- 确认借阅按钮 -->
            <div class="action-buttons">
              <el-button
                type="primary"
                size="large"
                :loading="confirmLoading"
                @click="handleConfirmBorrow"
              >
                确认借阅
              </el-button>
              <el-button size="large" @click="cancelBorrow">
                取消
              </el-button>
            </div>
          </div>
          
          <el-empty v-else description="请先输入校园卡号和馆藏号进行验证" />
        </el-card>
      </el-tab-pane>
      
      <!-- 标签2：图书归还 -->
      <el-tab-pane label="图书归还" name="return">
        <el-card class="tab-card">
          <!-- 输入区域 -->
          <div class="input-section">
            <h3 class="section-title">归还信息录入</h3>
            <el-form :inline="true" class="return-form">
              <!-- 馆藏号输入 -->
              <el-form-item label="馆藏号" class="form-item-wide">
                <el-input
                  v-model="returnForm.collectionNumber"
                  placeholder="请输入或扫描馆藏号"
                  clearable
                  style="width: 300px"
                  @focus="handleScanFocus('return-collection')"
                  @keyup.enter="handleQueryBorrowRecord"
                />
              </el-form-item>
              
              <!-- 查询按钮 -->
              <el-form-item>
                <el-button
                  type="primary"
                  :loading="returnLoading"
                  @click="handleQueryBorrowRecord"
                >
                  查询借阅记录
                </el-button>
                <el-button @click="resetReturnForm">
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <el-divider />
          
          <!-- 展示区域 -->
          <div v-if="currentBorrowRecord" class="display-section">
            <h3 class="section-title">借阅记录</h3>
            
            <!-- 借阅信息 -->
            <div class="info-block">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="借阅人姓名">
                  {{ currentBorrowRecord.userName }}
                </el-descriptions-item>
                <el-descriptions-item label="校园卡号">
                  {{ currentBorrowRecord.cardNumber }}
                </el-descriptions-item>
                <el-descriptions-item label="图书名称">
                  {{ currentBorrowRecord.bookTitle }}
                </el-descriptions-item>
                <el-descriptions-item label="馆藏号">
                  {{ currentBorrowRecord.collectionNumber }}
                </el-descriptions-item>
                <el-descriptions-item label="借阅日期">
                  {{ formatDate(currentBorrowRecord.borrowDate) }}
                </el-descriptions-item>
                <el-descriptions-item label="应还日期">
                  <span :class="{ 'overdue-text': isOverdue(currentBorrowRecord.dueDate) }">
                    {{ formatDate(currentBorrowRecord.dueDate) }}
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="借阅状态">
                  <el-tag type="warning">{{ currentBorrowRecord.status }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="操作员">
                  {{ currentBorrowRecord.operator }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
            
            <!-- 超期提示 -->
            <div v-if="isOverdue(currentBorrowRecord.dueDate)" class="info-block">
              <el-alert
                :title="`该图书已超期${calculateOverdueDaysDisplay(currentBorrowRecord.dueDate)}天`"
                type="error"
                :closable="false"
                show-icon
              />
            </div>
            
            <!-- 确认归还按钮 -->
            <div class="action-buttons">
              <el-button
                type="primary"
                size="large"
                :loading="confirmReturnLoading"
                @click="handleConfirmReturn"
              >
                确认归还
              </el-button>
              <el-button size="large" @click="cancelReturn">
                取消
              </el-button>
            </div>
          </div>
          
          <el-empty v-else description="请先输入馆藏号查询借阅记录" />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useBorrowStore } from '@/stores/borrow'
import { useUserStore } from '@/stores/user'
import { useSystemConfigStore } from '@/stores/systemConfig'

const borrowStore = useBorrowStore()
const userStore = useUserStore()
const configStore = useSystemConfigStore()

// 当前激活的标签页
const activeTab = ref('borrow')

// ========== 图书借阅相关 ==========
const borrowLoading = ref(false)
const confirmLoading = ref(false)

// 借阅表单
const borrowForm = reactive({
  cardNumber: '',
  collectionNumbers: ''
})

// 借阅验证结果
const borrowValidation = ref(null)

// 应还日期
const borrowDueDate = computed(() => {
  if (!borrowValidation.value) return null
  return borrowStore.calculateDueDate(borrowValidation.value.user.identity)
})

/**
 * 验证借阅条件
 */
const handleValidateBorrow = () => {
  if (!borrowForm.cardNumber.trim()) {
    ElMessage.warning('请输入校园卡号')
    return
  }
  
  if (!borrowForm.collectionNumbers.trim()) {
    ElMessage.warning('请输入馆藏号')
    return
  }
  
  // 解析馆藏号（支持逗号分隔）
  const collectionNumbers = borrowForm.collectionNumbers
    .split(/[,，\s]+/)
    .map(n => n.trim())
    .filter(n => n)
  
  // 检查数量限制
  const maxCount = configStore.getMaxBorrowCount()
  if (collectionNumbers.length > maxCount) {
    ElMessage.error(`单次最多可借阅${maxCount}本图书`)
    return
  }
  
  if (collectionNumbers.length === 0) {
    ElMessage.warning('请输入有效的馆藏号')
    return
  }
  
  borrowLoading.value = true
  
  setTimeout(() => {
    const result = borrowStore.validateBorrow(
      borrowForm.cardNumber.trim(),
      collectionNumbers
    )
    
    if (result.success) {
      borrowValidation.value = result
      ElMessage.success('验证通过，请确认借阅信息')
    } else {
      borrowValidation.value = null
      ElMessage.error(result.message)
    }
    
    borrowLoading.value = false
  }, 500)
}

/**
 * 确认借阅
 */
const handleConfirmBorrow = () => {
  if (!borrowValidation.value) {
    ElMessage.warning('请先验证借阅信息')
    return
  }
  
  const collectionNumbers = borrowValidation.value.books.map(b => b.collectionNumber)
  
  ElMessageBox.confirm(
    `确定要为【${borrowValidation.value.user.name}】借出${collectionNumbers.length}本图书吗？`,
    '借阅确认',
    {
      confirmButtonText: '确定借阅',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    confirmLoading.value = true
    
    setTimeout(() => {
      const result = borrowStore.borrowBooks(
        borrowForm.cardNumber.trim(),
        collectionNumbers,
        userStore.userInfo.name
      )
      
      if (result.success) {
        ElMessage.success({
          message: `借阅成功！应还日期：${formatDate(result.dueDate)}`,
          duration: 5000
        })
        
        // 重置表单
        resetBorrowForm()
        borrowValidation.value = null
      } else {
        ElMessage.error(result.message)
      }
      
      confirmLoading.value = false
    }, 500)
  }).catch(() => {
    ElMessage.info('已取消借阅')
  })
}

/**
 * 取消借阅
 */
const cancelBorrow = () => {
  borrowValidation.value = null
  ElMessage.info('已取消')
}

/**
 * 重置借阅表单
 */
const resetBorrowForm = () => {
  borrowForm.cardNumber = ''
  borrowForm.collectionNumbers = ''
  borrowValidation.value = null
}

// ========== 图书归还相关 ==========
const returnLoading = ref(false)
const confirmReturnLoading = ref(false)

// 归还表单
const returnForm = reactive({
  collectionNumber: ''
})

// 当前借阅记录
const currentBorrowRecord = ref(null)

/**
 * 查询借阅记录
 */
const handleQueryBorrowRecord = () => {
  if (!returnForm.collectionNumber.trim()) {
    ElMessage.warning('请输入馆藏号')
    return
  }
  
  returnLoading.value = true
  
  setTimeout(() => {
    const record = borrowStore.getBorrowRecordByCollection(
      returnForm.collectionNumber.trim()
    )
    
    if (record) {
      currentBorrowRecord.value = record
      ElMessage.success('查询成功')
    } else {
      currentBorrowRecord.value = null
      ElMessage.error('未找到该图书的借阅记录或该图书已归还')
    }
    
    returnLoading.value = false
  }, 300)
}

/**
 * 确认归还
 */
const handleConfirmReturn = () => {
  if (!currentBorrowRecord.value) {
    ElMessage.warning('请先查询借阅记录')
    return
  }
  
  ElMessageBox.confirm(
    `确定要归还图书【${currentBorrowRecord.value.bookTitle}】吗？`,
    '归还确认',
    {
      confirmButtonText: '确定归还',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    confirmReturnLoading.value = true
    
    setTimeout(() => {
      const result = borrowStore.returnBook(
        currentBorrowRecord.value.collectionNumber,
        userStore.userInfo.name
      )
      
      if (result.success) {
        if (result.overdueDays > 0) {
          ElMessage.warning({
            message: result.message,
            duration: 5000
          })
        } else {
          ElMessage.success(result.message)
        }
        
        // 重置表单
        resetReturnForm()
        currentBorrowRecord.value = null
      } else {
        ElMessage.error(result.message)
      }
      
      confirmReturnLoading.value = false
    }, 500)
  }).catch(() => {
    ElMessage.info('已取消归还')
  })
}

/**
 * 取消归还
 */
const cancelReturn = () => {
  currentBorrowRecord.value = null
  ElMessage.info('已取消')
}

/**
 * 重置归还表单
 */
const resetReturnForm = () => {
  returnForm.collectionNumber = ''
  currentBorrowRecord.value = null
}

// ========== 工具函数 ==========
/**
 * 格式化日期
 */
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 判断是否超期
 */
const isOverdue = (dueDate) => {
  const now = new Date()
  const due = new Date(dueDate)
  return now > due
}

/**
 * 计算超期天数（用于显示）
 */
const calculateOverdueDaysDisplay = (dueDate) => {
  return borrowStore.calculateOverdueDays(dueDate)
}

/**
 * 处理扫码枪聚焦
 */
const handleScanFocus = (field) => {
  console.log(`${field} 准备接收扫码数据`)
}

// ========== 生命周期 ==========
onMounted(() => {
  // 恢复系统配置
  configStore.restoreFromStorage()
  
  // 恢复借阅数据
  borrowStore.restoreFromStorage()
  
  // 初始化模拟数据
  borrowStore.initMockData()
})
</script>

<style scoped>
/* 页面容器 */
.borrow-management-container {
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

/* 区域标题 */
.section-title {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 输入区域 */
.input-section {
  background-color: #f9fafb;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

/* 借阅表单 */
.borrow-form,
.return-form {
  margin-top: 15px;
}

.form-item-wide {
  margin-bottom: 15px;
}

/* 借阅提示 */
.borrow-tip {
  margin-top: 15px;
}

/* 展示区域 */
.display-section {
  margin-top: 20px;
}

/* 信息块 */
.info-block {
  margin-bottom: 25px;
}

.info-block h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-regular);
}

/* 操作按钮 */
.action-buttons {
  margin-top: 30px;
  text-align: center;
}

/* 超期文本 */
.overdue-text {
  color: var(--danger-color);
  font-weight: 500;
}

/* 分隔线 */
.el-divider {
  margin: 25px 0;
}

/* 按钮间距 */
:deep(.el-form-item) {
  margin-right: 20px;
}

/* Alert优化 */
:deep(.el-alert) {
  margin-bottom: 0;
}

/* 表格优化 */
:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 500;
}
</style>
