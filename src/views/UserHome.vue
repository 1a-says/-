<template>
  <div class="page-container">
    <!-- 顶部导航栏 -->
    <el-header class="page-header">
      <div class="header-left">
        <h3>高校图书馆管理系统</h3>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><User /></el-icon>
            <span>{{ userStore.userInfo?.name }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <!-- 主体内容 -->
    <el-container class="main-container">
      <!-- 侧边栏菜单 -->
      <el-aside width="200px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <!-- 图书查询 -->
          <el-menu-item index="1">
            <el-icon><Search /></el-icon>
            <span>图书查询</span>
          </el-menu-item>
          
          <!-- 我的借阅 -->
          <el-menu-item index="2">
            <el-icon><Reading /></el-icon>
            <span>我的借阅</span>
          </el-menu-item>
          
          <!-- 借阅历史 -->
          <el-menu-item index="3">
            <el-icon><Document /></el-icon>
            <span>借阅历史</span>
          </el-menu-item>
          
          <!-- 个人信息 -->
          <el-menu-item index="4">
            <el-icon><UserFilled /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 内容区域 -->
      <el-main class="content-area">
        <div class="welcome-container">
          <el-card class="welcome-card">
            <h2>欢迎使用图书馆管理系统</h2>
            <p>您好，{{ userStore.userInfo?.name }}（{{ userStore.userInfo?.account }}）！</p>
            
            <!-- 个人借阅统计 -->
            <el-row :gutter="20" class="stats-row">
              <el-col :span="8">
                <el-statistic title="当前在借" :value="borrowStats.current">
                  <template #prefix>
                    <el-icon style="color: #409EFF;"><Reading /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="8">
                <el-statistic title="历史借阅" :value="borrowStats.history">
                  <template #prefix>
                    <el-icon style="color: #67C23A;"><Document /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="8">
                <el-statistic title="可借数量" :value="borrowStats.available">
                  <template #prefix>
                    <el-icon style="color: #E6A23C;"><Tickets /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
            </el-row>
            
            <!-- 我的在借图书 -->
            <div class="my-books-section">
              <h3>我的在借图书</h3>
              <el-table :data="currentBooks" stripe style="width: 100%">
                <el-table-column prop="bookName" label="书名" width="300" />
                <el-table-column prop="author" label="作者" width="150" />
                <el-table-column prop="borrowDate" label="借阅日期" width="120" />
                <el-table-column prop="dueDate" label="应还日期" width="120">
                  <template #default="scope">
                    <span :class="{ 'overdue': scope.row.isOverdue }">
                      {{ scope.row.dueDate }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag v-if="scope.row.isOverdue" type="danger">已逾期</el-tag>
                    <el-tag v-else type="success">正常</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="handleRenew(scope.row)">
                      续借
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <!-- 空状态 -->
              <el-empty v-if="currentBooks.length === 0" description="暂无在借图书" />
            </div>
            
            <!-- 快捷操作 -->
            <div class="quick-actions">
              <h3>快捷操作</h3>
              <el-space wrap>
                <el-button type="primary" @click="quickAction('search')">
                  <el-icon><Search /></el-icon>查询图书
                </el-button>
                <el-button type="success" @click="quickAction('recommend')">
                  <el-icon><Star /></el-icon>推荐图书
                </el-button>
                <el-button type="info" @click="quickAction('profile')">
                  <el-icon><UserFilled /></el-icon>个人信息
                </el-button>
              </el-space>
            </div>
          </el-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, 
  ArrowDown, 
  Search, 
  Reading, 
  Document, 
  UserFilled,
  Tickets,
  Star
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = ref('1')

// 借阅统计
const borrowStats = ref({
  current: 3,    // 当前在借
  history: 24,   // 历史借阅
  available: 7   // 可借数量（假设上限10本）
})

// 当前在借图书（模拟数据）
const currentBooks = ref([
  {
    id: 1,
    bookName: '算法导论（原书第3版）',
    author: 'Thomas H.Cormen',
    borrowDate: '2026-12-15',
    dueDate: '2026-01-15',
    isOverdue: false
  },
  {
    id: 2,
    bookName: 'Java核心技术 卷I',
    author: 'Cay S. Horstmann',
    borrowDate: '2026-12-20',
    dueDate: '2026-01-20',
    isOverdue: false
  },
  {
    id: 3,
    bookName: '深入理解计算机系统',
    author: 'Randal E. Bryant',
    borrowDate: '2026-12-01',
    dueDate: '2026-01-01',
    isOverdue: true
  }
])

/**
 * 处理菜单选择
 */
const handleMenuSelect = (index) => {
  activeMenu.value = index
  
  // 我的借阅 - 跳转到借阅查询页面
  if (index === '2') {
    router.push('/user/borrow-query')
    return
  }
  
  const menuMap = {
    '1': '图书查询',
    '3': '借阅历史',
    '4': '个人信息'
  }
  ElMessage.info(`功能开发中：${menuMap[index]}`)
}

/**
 * 处理下拉菜单命令
 */
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  }
}

/**
 * 续借图书
 */
const handleRenew = (book) => {
  if (book.isOverdue) {
    ElMessage.error('该图书已逾期，无法续借，请尽快归还')
    return
  }
  
  ElMessageBox.confirm(`确定要续借《${book.bookName}》吗？`, '续借确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    ElMessage.success('续借成功，借阅期限已延长30天')
    // 实际项目中这里应该调用API
  }).catch(() => {})
}

/**
 * 快捷操作
 */
const quickAction = (action) => {
  const actionMap = {
    'search': '查询图书',
    'recommend': '推荐图书',
    'profile': '个人信息'
  }
  ElMessage.info(`功能开发中：${actionMap[action]}`)
}
</script>

<style scoped>
/* 页面容器 */
.page-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color);
}

/* 顶部导航栏 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.header-left h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: var(--bg-color);
}

.user-info span {
  margin: 0 8px;
  color: var(--text-primary);
}

/* 主容器 */
.main-container {
  flex: 1;
  overflow: hidden;
}

/* 侧边栏 */
.sidebar {
  background-color: #fff;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

/* 内容区域 */
.content-area {
  background-color: var(--bg-color);
  overflow-y: auto;
}

/* 欢迎容器 */
.welcome-container {
  padding: 20px;
}

.welcome-card {
  padding: 20px;
}

.welcome-card h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: var(--text-primary);
}

.welcome-card > p {
  margin: 0 0 30px 0;
  font-size: 14px;
  color: var(--text-secondary);
}

/* 统计行 */
.stats-row {
  margin-bottom: 30px;
  padding: 20px 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
}

/* 我的图书区域 */
.my-books-section {
  margin-top: 30px;
}

.my-books-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: var(--text-primary);
}

/* 逾期标记 */
.overdue {
  color: var(--danger-color);
  font-weight: 500;
}

/* 快捷操作 */
.quick-actions {
  margin-top: 30px;
}

.quick-actions h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: var(--text-primary);
}

/* 统计数字样式 */
:deep(.el-statistic__head) {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

:deep(.el-statistic__number) {
  font-size: 28px;
  font-weight: 600;
}
</style>
