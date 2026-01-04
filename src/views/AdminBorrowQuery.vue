<template>
  <div class="admin-borrow-query-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>借阅记录查询</h2>
    </div>
    
    <el-card class="query-card">
      <!-- 查询区域 -->
      <div class="query-section">
        <h3 class="section-title">查询条件</h3>
        <el-form :inline="true" :model="queryForm" class="query-form">
          <el-form-item label="学号/工号">
            <el-input
              v-model="queryForm.accountNumber"
              placeholder="支持模糊查询"
              clearable
              style="width: 220px"
            />
          </el-form-item>
          
          <el-form-item label="图书信息">
            <el-input
              v-model="queryForm.bookInfo"
              placeholder="馆藏号或书名，支持模糊查询"
              clearable
              style="width: 280px"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleQuery">
              查询
            </el-button>
            <el-button @click="handleReset">
              重置
            </el-button>
            <el-button type="success" @click="handleExport">
              导出Excel
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-divider />
      
      <!-- 表格区域 -->
      <div class="table-section">
        <el-table
          :data="paginatedRecords"
          stripe
          border
          :default-sort="{ prop: 'borrowDate', order: 'descending' }"
          @sort-change="handleSortChange"
          v-loading="loading"
        >
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="userName" label="借阅人姓名" width="120" />
          <el-table-column prop="accountNumber" label="学号/工号" width="130" />
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
          <el-table-column prop="operator" label="操作员" width="120" />
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="filteredRecords.length"
            layout="total, prev, pager, next, jumper"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useBorrowStore } from '@/stores/borrow'

const borrowStore = useBorrowStore()

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const sortField = ref('borrowDate')
const sortOrder = ref('descending')

// 查询表单
const queryForm = reactive({
  accountNumber: '',
  bookInfo: ''
})

// 过滤后的记录
const filteredRecords = computed(() => {
  let records = [...borrowStore.borrowRecords]
  
  // 学号/工号过滤
  if (queryForm.accountNumber && queryForm.accountNumber.trim()) {
    const searchAccount = queryForm.accountNumber.trim().toLowerCase()
    records = records.filter(record => 
      record.accountNumber.toLowerCase().includes(searchAccount)
    )
  }
  
  // 图书信息过滤（馆藏号或书名）
  if (queryForm.bookInfo && queryForm.bookInfo.trim()) {
    const searchBook = queryForm.bookInfo.trim().toLowerCase()
    records = records.filter(record => 
      record.collectionNumber.toLowerCase().includes(searchBook) ||
      record.bookTitle.toLowerCase().includes(searchBook)
    )
  }
  
  // 排序
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

// 分页后的记录
const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredRecords.value.slice(start, end)
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
 * 处理查询
 */
const handleQuery = () => {
  loading.value = true
  currentPage.value = 1
  
  setTimeout(() => {
    loading.value = false
    ElMessage.info(`查询到 ${filteredRecords.value.length} 条记录`)
  }, 300)
}

/**
 * 处理重置
 */
const handleReset = () => {
  queryForm.accountNumber = ''
  queryForm.bookInfo = ''
  currentPage.value = 1
  ElMessage.info('查询条件已重置')
}

/**
 * 导出Excel
 */
const handleExport = () => {
  if (filteredRecords.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  
  // 构建CSV格式数据
  let csvContent = '\uFEFF' // UTF-8 BOM
  
  // 表头
  const headers = [
    '序号',
    '借阅人姓名',
    '学号/工号',
    '图书名',
    '馆藏号',
    '借阅日期',
    '应还日期',
    '归还日期',
    '借阅状态',
    '操作员'
  ]
  csvContent += headers.join(',') + '\n'
  
  // 数据行
  filteredRecords.value.forEach((record, index) => {
    const row = [
      index + 1,
      record.userName,
      record.accountNumber,
      `"${record.bookTitle}"`, // 书名可能包含逗号，用引号包裹
      record.collectionNumber,
      formatDate(record.borrowDate),
      formatDate(record.dueDate),
      record.returnDate ? formatDate(record.returnDate) : '-',
      getStatusText(record),
      record.operator
    ]
    csvContent += row.join(',') + '\n'
  })
  
  // 创建下载链接
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  
  link.setAttribute('href', url)
  link.setAttribute('download', `借阅记录_${new Date().getTime()}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('导出成功')
}

/**
 * 处理排序变化
 */
const handleSortChange = ({ prop, order }) => {
  sortField.value = prop
  sortOrder.value = order
}

/**
 * 处理页码变化
 */
const handlePageChange = (page) => {
  currentPage.value = page
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
.admin-borrow-query-container {
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

/* 区域标题 */
.section-title {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 查询区域 */
.query-section {
  background-color: #f9fafb;
  padding: 20px;
  border-radius: 8px;
}

.query-form {
  margin-top: 10px;
}

/* 表格区域 */
.table-section {
  margin-top: 20px;
}

/* 分页容器 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 超期文本 */
.overdue-text {
  color: var(--danger-color);
  font-weight: 500;
}

/* 按钮间距 */
:deep(.el-form-item) {
  margin-right: 20px;
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
