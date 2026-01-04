<template>
  <div class="book-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>图书管理</h2>
    </div>
    
    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="management-tabs">
      <!-- 标签1：图书单本入库 -->
      <el-tab-pane label="图书单本入库" name="entry">
        <el-card class="tab-card">
          <el-form
            ref="entryFormRef"
            :model="entryForm"
            :rules="entryRules"
            label-width="100px"
            class="entry-form"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <!-- ISBN输入 -->
                <el-form-item label="ISBN" prop="isbn">
                  <el-input
                    v-model="entryForm.isbn"
                    placeholder="请输入或扫描ISBN号"
                    clearable
                    @focus="handleScanFocus('isbn')"
                  />
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <!-- 书名输入 -->
                <el-form-item label="书名" prop="title">
                  <el-input
                    v-model="entryForm.title"
                    placeholder="请输入书名"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <!-- 作者输入 -->
                <el-form-item label="作者" prop="author">
                  <el-input
                    v-model="entryForm.author"
                    placeholder="请输入作者"
                    clearable
                  />
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <!-- 出版社输入 -->
                <el-form-item label="出版社" prop="publisher">
                  <el-input
                    v-model="entryForm.publisher"
                    placeholder="请输入出版社"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <!-- 馆藏位置输入 -->
                <el-form-item label="馆藏位置" prop="location">
                  <el-input
                    v-model="entryForm.location"
                    placeholder="例如：A区-3架-2层"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <!-- 操作按钮 -->
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="entryLoading"
                @click="handleBookEntry"
              >
                提交入库
              </el-button>
              <el-button size="large" @click="resetEntryForm">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 标签2：图书基础查询 -->
      <el-tab-pane label="图书基础查询" name="query">
        <el-card class="tab-card">
          <!-- 查询表单 -->
          <el-form :model="queryForm" :inline="true" class="query-form">
            <el-form-item label="ISBN">
              <el-input
                v-model="queryForm.isbn"
                placeholder="支持扫码和模糊查询"
                clearable
                style="width: 220px"
                @focus="handleScanFocus('query-isbn')"
              />
            </el-form-item>
            
            <el-form-item label="书名">
              <el-input
                v-model="queryForm.title"
                placeholder="支持模糊查询"
                clearable
                style="width: 220px"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleQuery">
                查询
              </el-button>
              <el-button @click="handleResetQuery">
                重置
              </el-button>
            </el-form-item>
          </el-form>
          
          <!-- 查询结果表格 -->
          <el-table
            :data="paginatedBooks"
            stripe
            border
            class="query-table"
            v-loading="queryLoading"
          >
            <el-table-column prop="collectionNumber" label="馆藏号" width="180" />
            <el-table-column prop="isbn" label="ISBN" width="150" />
            <el-table-column prop="title" label="书名" min-width="200" />
            <el-table-column prop="author" label="作者" width="150" />
            <el-table-column prop="publisher" label="出版社" width="180" />
            <el-table-column prop="location" label="馆藏位置" width="150" />
            <el-table-column prop="status" label="当前状态" width="100">
              <template #default="scope">
                <el-tag
                  :type="getStatusType(scope.row.status)"
                  disable-transitions
                >
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="queryResults.length"
              layout="total, prev, pager, next, jumper"
              @current-change="handlePageChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 标签3：图书状态维护 -->
      <el-tab-pane label="图书状态维护" name="status">
        <el-card class="tab-card">
          <!-- 查询图书 -->
          <el-form :inline="true" class="status-form">
            <el-form-item label="馆藏号">
              <el-input
                v-model="statusForm.collectionNumber"
                placeholder="请输入或扫描馆藏号"
                clearable
                style="width: 250px"
                @focus="handleScanFocus('collection')"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleQueryBookByCollection">
                查询图书
              </el-button>
            </el-form-item>
          </el-form>
          
          <!-- 图书信息展示 -->
          <el-divider />
          
          <div v-if="currentBook" class="book-info-section">
            <h3>图书信息</h3>
            <el-descriptions :column="2" border class="book-info">
              <el-descriptions-item label="馆藏号">
                {{ currentBook.collectionNumber }}
              </el-descriptions-item>
              <el-descriptions-item label="ISBN">
                {{ currentBook.isbn }}
              </el-descriptions-item>
              <el-descriptions-item label="书名">
                {{ currentBook.title }}
              </el-descriptions-item>
              <el-descriptions-item label="作者">
                {{ currentBook.author }}
              </el-descriptions-item>
              <el-descriptions-item label="出版社">
                {{ currentBook.publisher }}
              </el-descriptions-item>
              <el-descriptions-item label="馆藏位置">
                {{ currentBook.location }}
              </el-descriptions-item>
              <el-descriptions-item label="当前状态">
                <el-tag :type="getStatusType(currentBook.status)">
                  {{ currentBook.status }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
            
            <!-- 状态修改 -->
            <div class="status-update-section">
              <h3>修改状态</h3>
              <el-form :inline="true">
                <el-form-item label="新状态">
                  <el-select
                    v-model="statusForm.newStatus"
                    placeholder="请选择状态"
                    style="width: 150px"
                  >
                    <el-option label="可借阅" value="可借阅" />
                    <el-option label="已借出" value="已借出" />
                    <el-option label="下架" value="下架" />
                  </el-select>
                </el-form-item>
                
                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="statusLoading"
                    @click="handleUpdateStatus"
                  >
                    提交修改
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
          
          <el-empty v-else description="请先查询图书" />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useBookStore } from '@/stores/book'
import { useUserStore } from '@/stores/user'

const bookStore = useBookStore()
const userStore = useUserStore()

// 当前激活的标签页
const activeTab = ref('entry')

// ========== 图书入库相关 ==========
const entryFormRef = ref(null)
const entryLoading = ref(false)

// 入库表单数据
const entryForm = reactive({
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  location: ''
})

// ISBN格式验证（支持ISBN-10和ISBN-13）
const validateISBN = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入ISBN号'))
  }
  // 移除所有连字符和空格
  const cleanISBN = value.replace(/[-\s]/g, '')
  
  // 检查长度（ISBN-10: 10位, ISBN-13: 13位）
  if (cleanISBN.length !== 10 && cleanISBN.length !== 13) {
    return callback(new Error('ISBN号应为10位或13位'))
  }
  
  // 检查是否全为数字（最后一位可能是X）
  const pattern = /^[\dX]+$/i
  if (!pattern.test(cleanISBN)) {
    return callback(new Error('ISBN号格式不正确'))
  }
  
  callback()
}

// 表单验证规则
const entryRules = {
  isbn: [
    { required: true, validator: validateISBN, trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入书名', trigger: 'blur' },
    { min: 1, max: 100, message: '书名长度在1-100个字符', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' },
    { min: 1, max: 50, message: '作者长度在1-50个字符', trigger: 'blur' }
  ],
  publisher: [
    { required: true, message: '请输入出版社', trigger: 'blur' },
    { min: 1, max: 50, message: '出版社长度在1-50个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入馆藏位置', trigger: 'blur' },
    { min: 1, max: 30, message: '馆藏位置长度在1-30个字符', trigger: 'blur' }
  ]
}

/**
 * 提交图书入库
 */
const handleBookEntry = async () => {
  if (!entryFormRef.value) return
  
  try {
    // 表单验证
    await entryFormRef.value.validate()
    
    entryLoading.value = true
    
    // 模拟网络延迟
    setTimeout(() => {
      const result = bookStore.addBook({
        isbn: entryForm.isbn.trim(),
        title: entryForm.title.trim(),
        author: entryForm.author.trim(),
        publisher: entryForm.publisher.trim(),
        location: entryForm.location.trim()
      })
      
      if (result.success) {
        ElMessage.success({
          message: `入库成功！馆藏号：${result.collectionNumber}`,
          duration: 3000
        })
        
        // 清空表单
        resetEntryForm()
      }
      
      entryLoading.value = false
    }, 500)
    
  } catch (error) {
    console.log('表单验证失败:', error)
  }
}

/**
 * 重置入库表单
 */
const resetEntryForm = () => {
  if (entryFormRef.value) {
    entryFormRef.value.resetFields()
  }
  Object.keys(entryForm).forEach(key => {
    entryForm[key] = ''
  })
}

// ========== 图书查询相关 ==========
const queryLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

// 查询表单
const queryForm = reactive({
  isbn: '',
  title: ''
})

// 查询结果
const queryResults = ref([])

/**
 * 执行查询
 */
const handleQuery = () => {
  queryLoading.value = true
  
  setTimeout(() => {
    queryResults.value = bookStore.queryBooks({
      isbn: queryForm.isbn,
      title: queryForm.title
    })
    
    currentPage.value = 1 // 重置到第一页
    queryLoading.value = false
    
    ElMessage.info(`查询到 ${queryResults.value.length} 条记录`)
  }, 300)
}

/**
 * 重置查询
 */
const handleResetQuery = () => {
  queryForm.isbn = ''
  queryForm.title = ''
  queryResults.value = []
  currentPage.value = 1
}

/**
 * 分页后的数据
 */
const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return queryResults.value.slice(start, end)
})

/**
 * 页码变化
 */
const handlePageChange = (page) => {
  currentPage.value = page
}

/**
 * 获取状态标签类型
 */
const getStatusType = (status) => {
  const typeMap = {
    '可借阅': 'success',
    '已借出': 'warning',
    '下架': 'info'
  }
  return typeMap[status] || 'info'
}

// ========== 图书状态维护相关 ==========
const statusLoading = ref(false)

// 状态维护表单
const statusForm = reactive({
  collectionNumber: '',
  newStatus: ''
})

// 当前查询到的图书
const currentBook = ref(null)

/**
 * 根据馆藏号查询图书
 */
const handleQueryBookByCollection = () => {
  if (!statusForm.collectionNumber.trim()) {
    ElMessage.warning('请输入馆藏号')
    return
  }
  
  const book = bookStore.getBookByCollectionNumber(statusForm.collectionNumber.trim())
  
  if (book) {
    currentBook.value = book
    statusForm.newStatus = book.status // 默认选中当前状态
    ElMessage.success('查询成功')
  } else {
    currentBook.value = null
    ElMessage.error('未找到该图书')
  }
}

/**
 * 更新图书状态
 */
const handleUpdateStatus = () => {
  if (!currentBook.value) {
    ElMessage.warning('请先查询图书')
    return
  }
  
  if (!statusForm.newStatus) {
    ElMessage.warning('请选择新状态')
    return
  }
  
  if (statusForm.newStatus === currentBook.value.status) {
    ElMessage.info('状态未变化')
    return
  }
  
  statusLoading.value = true
  
  setTimeout(() => {
    const result = bookStore.updateBookStatus(
      currentBook.value.collectionNumber,
      statusForm.newStatus,
      userStore.userInfo.name // 当前登录管理员
    )
    
    if (result.success) {
      ElMessage.success('状态修改成功')
      
      // 刷新当前图书信息
      currentBook.value = bookStore.getBookByCollectionNumber(currentBook.value.collectionNumber)
    } else {
      ElMessage.error(result.message)
    }
    
    statusLoading.value = false
  }, 500)
}

// ========== 扫码枪支持 ==========
/**
 * 处理输入框聚焦（准备接收扫码数据）
 */
const handleScanFocus = (field) => {
  // 扫码枪通常会快速输入数据并回车
  // 这里只是标记，实际扫码枪会直接将数据填充到聚焦的输入框
  console.log(`${field} 准备接收扫码数据`)
}

// ========== 生命周期 ==========
onMounted(() => {
  // 恢复图书数据
  bookStore.restoreFromStorage()
  
  // 初始化模拟数据（如果没有数据）
  bookStore.initMockData()
  
  // 默认加载所有图书到查询结果
  queryResults.value = bookStore.queryBooks({})
})
</script>

<style scoped>
/* 页面容器 */
.book-management-container {
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

/* 入库表单 */
.entry-form {
  max-width: 900px;
}

/* 查询表单 */
.query-form {
  margin-bottom: 20px;
}

/* 查询表格 */
.query-table {
  margin-top: 20px;
}

/* 分页容器 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 状态表单 */
.status-form {
  margin-bottom: 20px;
}

/* 图书信息区域 */
.book-info-section {
  margin-top: 20px;
}

.book-info-section h3 {
  margin: 20px 0 15px 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

.book-info {
  margin-bottom: 30px;
}

/* 状态更新区域 */
.status-update-section {
  margin-top: 30px;
}

/* 按钮间距 */
:deep(.el-form-item) {
  margin-right: 20px;
}

/* 表格自适应 */
:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 500;
}
</style>
