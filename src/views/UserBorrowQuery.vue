<template>
  <div class="user-borrow-query-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的借阅记录</h2>
    </div>
    
    <el-card class="query-card">
      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-label">当前在借</div>
              <div class="stat-value primary">{{ currentBorrowCount }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-label">历史借阅</div>
              <div class="stat-value success">{{ totalBorrowCount }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-label">超期图书</div>
              <div class="stat-value danger">{{ overdueCount }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <el-divider />
      
      <!-- 借阅记录表格 -->
      <div class="table-section">
        <el-table
          :data="sortedRecords"
          stripe
          border
          :default-sort="{ prop: 'dueDate', order: 'ascending' }"
          @sort-change="handleSortChange"
          v-loading="loading"
        >
          <el-table-column prop="bookTitle" label="图书名" min-width="200" />
          <el-table-column prop="collectionNumber" label="馆藏号" width="180" />
          <el-table-column prop="borrowDate" label="借阅日期" width="120" sortable="custom">
            <template #default="scope">
              {{ formatDate(scope.row.borrowDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="应还日期" width="120" sortable="custom">
            <template #default="scope">
              <span :class="{ 'overdue-text': isOverdue(scope.row) && scope.row.status === '借阅中' }">
                {{ formatDate(scope.row.dueDate) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="returnDate" label="归还日期" width="120">
            <template #default="scope">
              {{ scope.row.returnDate ? formatDate(scope.row.returnDate) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="借阅状态" width="120">
            <template #default="scope">
              <el-tag
                :type="getStatusType(scope.row)"
                disable-transitions
              >
                {{ getStatusText(scope.row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="剩余天数" width="100">
            <template #default="scope">
              <span v-if="scope.row.status === '借阅中'">
                <span v-if="isOverdue(scope.row)" class="overdue-text">
                  超期{{ getOverdueDays(scope.row) }}天
                </span>
                <span v-else class="normal-text">
                  剩余{{ getRemainingDays(scope.row) }}天
                </span>
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 空状态 -->
        <el-empty v-if="userRecords.length === 0" description="暂无借阅记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBorrowStore } from '@/stores/borrow'
import { useUserStore } from '@/stores/user'

const borrowStore = useBorrowStore()
const userStore = useUserStore()

const loading = ref(false)
const sortField = ref('dueDate')
const sortOrder = ref('ascending')

// 当前用户的借阅记录
const userRecords = computed(() => {
  const currentUser = userStore.userInfo
  if (!currentUser) return []
  
  // 根据用户的账号查找借阅记录
  return borrowStore.borrowRecords.filter(record => 
    record.accountNumber === currentUser.account
  )
})

// 排序后的记录
const sortedRecords = computed(() => {
  const records = [...userRecords.value]
  
  if (sortField.value && sortOrder.value) {
    records.sort((a, b) => {
      const aValue = new Date(a[sortField.value]).getTime()
      const bValue = new Date(b[sortField.value]).getTime()
      
      if (sortOrder.value === 'ascending') {
        return aValue - bValue
      } else {
        return bValue - aValue
      }
    })
  }
  
  return records
})

// 当前在借数量
const currentBorrowCount = computed(() => {
  return userRecords.value.filter(r => r.status === '借阅中').length
})

// 历史借阅总数
const totalBorrowCount = computed(() => {
  return userRecords.value.length
})

// 超期图书数量
const overdueCount = computed(() => {
  return userRecords.value.filter(r => 
    r.status === '借阅中' && isOverdue(r)
  ).length
})

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
const isOverdue = (record) => {
  if (record.status !== '借阅中') return false
  const now = new Date()
  const dueDate = new Date(record.dueDate)
  return now > dueDate
}

/**
 * 获取状态文本
 */
const getStatusText = (record) => {
  if (record.status === '借阅中' && isOverdue(record)) {
    return '超期'
  }
  return record.status === '借阅中' ? '已借出' : '已归还'
}

/**
 * 获取状态标签类型
 */
const getStatusType = (record) => {
  if (record.status === '借阅中' && isOverdue(record)) {
    return 'danger'
  }
  return record.status === '借阅中' ? 'warning' : 'success'
}

/**
 * 获取超期天数
 */
const getOverdueDays = (record) => {
  return borrowStore.calculateOverdueDays(record.dueDate)
}

/**
 * 获取剩余天数
 */
const getRemainingDays = (record) => {
  const now = new Date()
  const dueDate = new Date(record.dueDate)
  const diffTime = dueDate - now
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays > 0 ? diffDays : 0
}

/**
 * 处理排序变化
 */
const handleSortChange = ({ prop, order }) => {
  sortField.value = prop
  sortOrder.value = order
}

onMounted(() => {
  loading.value = true
  
  // 恢复借阅数据
  borrowStore.restoreFromStorage()
  
  setTimeout(() => {
    loading.value = false
  }, 300)
})
</script>

<style scoped>
/* 页面容器 */
.user-borrow-query-container {
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

/* 查询卡片 */
.query-card {
  padding: 20px;
}

/* 统计信息区域 */
.stats-section {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
}

.stat-value.primary {
  color: var(--primary-color);
}

.stat-value.success {
  color: var(--success-color);
}

.stat-value.danger {
  color: var(--danger-color);
}

/* 表格区域 */
.table-section {
  margin-top: 20px;
}

/* 超期文本 */
.overdue-text {
  color: var(--danger-color);
  font-weight: 500;
}

/* 正常文本 */
.normal-text {
  color: var(--success-color);
}

/* 表格优化 */
:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 500;
}

/* 超期行高亮 */
:deep(.el-table__row) {
  transition: background-color 0.3s;
}
</style>
