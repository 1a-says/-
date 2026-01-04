<template>
  <div class="page-container">
    <!-- 顶部导航栏 -->
    <el-header class="page-header">
      <div class="header-left">
        <h3>高校图书馆管理系统 - 管理员</h3>
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
      <el-aside width="220px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <!-- 图书管理 -->
          <el-sub-menu index="1">
            <template #title>
              <el-icon><Reading /></el-icon>
              <span>图书管理</span>
            </template>
            <el-menu-item index="1-1">图书列表</el-menu-item>
            <el-menu-item index="1-2">添加图书</el-menu-item>
            <el-menu-item index="1-3">图书分类</el-menu-item>
          </el-sub-menu>
          
          <!-- 借阅管理 -->
          <el-sub-menu index="2">
            <template #title>
              <el-icon><DocumentCopy /></el-icon>
              <span>借阅管理</span>
            </template>
            <el-menu-item index="2-1">借阅记录</el-menu-item>
            <el-menu-item index="2-2">归还记录</el-menu-item>
            <el-menu-item index="2-3">逾期处理</el-menu-item>
          </el-sub-menu>
          
          <!-- 用户管理 -->
          <el-sub-menu index="3">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="3-1">用户列表</el-menu-item>
            <el-menu-item index="3-2">添加用户</el-menu-item>
            <el-menu-item index="3-3">权限管理</el-menu-item>
          </el-sub-menu>
          
          <!-- 统计报表 -->
          <el-sub-menu index="4">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>统计报表</span>
            </template>
            <el-menu-item index="4-1">借阅统计</el-menu-item>
            <el-menu-item index="4-2">图书统计</el-menu-item>
            <el-menu-item index="4-3">用户统计</el-menu-item>
          </el-sub-menu>
          
          <!-- 系统设置 -->
          <el-sub-menu index="5">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </template>
            <el-menu-item index="5-1">基础设置</el-menu-item>
            <el-menu-item index="5-2">借阅规则</el-menu-item>
            <el-menu-item index="5-3">密码重置</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      
      <!-- 内容区域 -->
      <el-main class="content-area">
        <div class="welcome-container">
          <el-card class="welcome-card">
            <h2>欢迎使用图书馆管理系统</h2>
            <p>您好，{{ userStore.userInfo?.name }}！您拥有管理员权限。</p>
            
            <!-- 快捷统计 -->
            <el-row :gutter="20" class="stats-row">
              <el-col :span="6">
                <el-statistic title="图书总数" :value="stats.totalBooks">
                  <template #prefix>
                    <el-icon><Reading /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="在借图书" :value="stats.borrowedBooks">
                  <template #prefix>
                    <el-icon><DocumentCopy /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="注册用户" :value="stats.totalUsers">
                  <template #prefix>
                    <el-icon><UserFilled /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="今日借阅" :value="stats.todayBorrow">
                  <template #prefix>
                    <el-icon><DataAnalysis /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
            </el-row>
            
            <!-- 快捷操作 -->
            <div class="quick-actions">
              <h3>快捷操作</h3>
              <el-space wrap>
                <el-button type="primary" @click="quickAction('add-book')">
                  <el-icon><Plus /></el-icon>添加图书
                </el-button>
                <el-button type="success" @click="quickAction('borrow')">
                  <el-icon><DocumentAdd /></el-icon>借阅登记
                </el-button>
                <el-button type="warning" @click="quickAction('return')">
                  <el-icon><DocumentRemove /></el-icon>归还登记
                </el-button>
                <el-button type="info" @click="quickAction('add-user')">
                  <el-icon><UserFilled /></el-icon>添加用户
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
  Reading, 
  DocumentCopy, 
  UserFilled, 
  DataAnalysis, 
  Setting,
  Plus,
  DocumentAdd,
  DocumentRemove
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = ref('1-1')

// 模拟统计数据
const stats = ref({
  totalBooks: 12580,
  borrowedBooks: 3426,
  totalUsers: 8562,
  todayBorrow: 127
})

/**
 * 处理菜单选择
 */
const handleMenuSelect = (index) => {
  activeMenu.value = index
  
  // 图书列表 - 跳转到图书管理页面
  if (index === '1-1') {
    router.push('/admin/book-management')
    return
  }
  
  // 借阅记录 - 跳转到借阅管理页面
  if (index === '2-1') {
    router.push('/admin/borrow-management')
    return
  }
  
  // 借阅统计 - 跳转到借阅查询页面
  if (index === '4-1') {
    router.push('/admin/borrow-query')
    return
  }
  
  // 图书统计 - 跳转到借阅统计页面
  if (index === '4-2') {
    router.push('/admin/borrow-statistics')
    return
  }
  
  // 基础设置 - 跳转到系统配置页面
  if (index === '5-1') {
    router.push('/admin/system-config')
    return
  }
  
  // 用户列表 - 跳转到用户管理页面
  if (index === '3-1') {
    router.push('/admin/user-management')
    return
  }
  
  ElMessage.info(`功能开发中：${getMenuName(index)}`)
}

/**
 * 获取菜单名称
 */
const getMenuName = (index) => {
  const menuMap = {
    '1-1': '图书列表',
    '1-2': '添加图书',
    '1-3': '图书分类',
    '2-1': '借阅记录',
    '2-2': '归还记录',
    '2-3': '逾期处理',
    '3-1': '用户列表',
    '3-2': '添加用户',
    '3-3': '权限管理',
    '4-1': '借阅统计',
    '4-2': '图书统计',
    '4-3': '用户统计',
    '5-1': '基础设置',
    '5-2': '借阅规则',
    '5-3': '密码重置'
  }
  return menuMap[index] || '未知功能'
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
 * 快捷操作
 */
const quickAction = (action) => {
  const actionMap = {
    'add-book': '添加图书',
    'borrow': '借阅登记',
    'return': '归还登记',
    'add-user': '添加用户'
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

/* 快捷操作 */
.quick-actions {
  margin-top: 20px;
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
  color: var(--primary-color);
}
</style>
