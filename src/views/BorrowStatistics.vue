<template>
  <div class="statistics-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>借阅统计</h2>
    </div>
    
    <el-card class="statistics-card">
      <!-- 筛选区域 -->
      <div class="filter-section">
        <h3 class="section-title">统计维度</h3>
        <el-form :inline="true" class="filter-form">
          <el-form-item label="时间维度">
            <el-select
              v-model="filterForm.timeDimension"
              placeholder="请选择时间维度"
              style="width: 150px"
            >
              <el-option label="本周" value="week" />
              <el-option label="本月" value="month" />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleQuery" :loading="loading">
              查询统计
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-divider />
      
      <!-- 图表展示区域 -->
      <div class="chart-section">
        <h3 class="section-title">借阅TOP5图书</h3>
        <div ref="chartRef" class="chart-container"></div>
      </div>
      
      <el-divider />
      
      <!-- 列表展示区域 -->
      <div class="table-section">
        <h3 class="section-title">TOP5图书详情</h3>
        <el-table :data="topBooks" stripe border class="top-table">
          <el-table-column type="index" label="排名" width="80" align="center">
            <template #default="scope">
              <el-tag
                :type="getRankType(scope.$index)"
                size="large"
                effect="dark"
              >
                {{ scope.$index + 1 }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="bookTitle" label="图书名" min-width="250" />
          <el-table-column prop="collectionNumber" label="馆藏号" width="180" />
          <el-table-column prop="author" label="作者" width="150" />
          <el-table-column prop="borrowCount" label="借阅次数" width="120" align="center">
            <template #default="scope">
              <span class="borrow-count">{{ scope.row.borrowCount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="percentage" label="借阅占比" width="120" align="center">
            <template #default="scope">
              <el-progress
                :percentage="scope.row.percentage"
                :color="customColors"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { useBorrowStore } from '@/stores/borrow'

const borrowStore = useBorrowStore()

const loading = ref(false)
const chartRef = ref(null)
let chartInstance = null

// 筛选表单
const filterForm = reactive({
  timeDimension: 'week' // 默认本周
})

// TOP5图书数据
const topBooks = ref([])

// 进度条颜色配置
const customColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#409EFF', percentage: 100 }
]

/**
 * 获取排名标签类型
 */
const getRankType = (index) => {
  if (index === 0) return ''
  if (index === 1) return 'success'
  if (index === 2) return 'warning'
  return 'info'
}

/**
 * 生成模拟统计数据
 * 根据借阅记录统计TOP5图书
 */
const generateStatisticsData = () => {
  // 获取借阅记录
  const records = borrowStore.borrowRecords
  
  // 统计每本书的借阅次数
  const bookStats = {}
  
  records.forEach(record => {
    const key = record.collectionNumber
    if (!bookStats[key]) {
      bookStats[key] = {
        collectionNumber: record.collectionNumber,
        bookTitle: record.bookTitle,
        author: record.bookAuthor || '未知作者',
        borrowCount: 0
      }
    }
    bookStats[key].borrowCount++
  })
  
  // 转换为数组并排序
  let statsArray = Object.values(bookStats)
  statsArray.sort((a, b) => b.borrowCount - a.borrowCount)
  
  // 取TOP5
  statsArray = statsArray.slice(0, 5)
  
  // 如果数据不足5条，添加模拟数据
  if (statsArray.length === 0) {
    statsArray = [
      {
        collectionNumber: 'TS20260101120000001',
        bookTitle: '算法导论（原书第3版）',
        author: 'Thomas H.Cormen',
        borrowCount: 45
      },
      {
        collectionNumber: 'TS20260101130000002',
        bookTitle: 'Java核心技术 卷I',
        author: 'Cay S. Horstmann',
        borrowCount: 38
      },
      {
        collectionNumber: 'TS20260101140000003',
        bookTitle: '深入理解计算机系统',
        author: 'Randal E. Bryant',
        borrowCount: 32
      },
      {
        collectionNumber: 'TS20260102150000004',
        bookTitle: 'Python编程从入门到实践',
        author: 'Eric Matthes',
        borrowCount: 28
      },
      {
        collectionNumber: 'TS20260102160000005',
        bookTitle: '设计模式：可复用面向对象软件的基础',
        author: 'Erich Gamma',
        borrowCount: 25
      }
    ]
  }
  
  // 计算总借阅次数
  const totalCount = statsArray.reduce((sum, item) => sum + item.borrowCount, 0)
  
  // 计算占比
  statsArray.forEach(item => {
    item.percentage = Math.round((item.borrowCount / totalCount) * 100)
  })
  
  return statsArray
}

/**
 * 初始化图表
 */
const initChart = () => {
  if (!chartRef.value) return
  
  // 如果已存在实例，先销毁
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  // 创建图表实例
  chartInstance = echarts.init(chartRef.value)
  
  // 配置图表选项
  const option = {
    title: {
      text: '借阅次数统计',
      left: 'center',
      textStyle: {
        color: '#303133',
        fontSize: 16,
        fontWeight: 500
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const data = params[0]
        return `${data.name}<br/>借阅次数：<strong>${data.value}</strong>`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: topBooks.value.map(item => item.bookTitle),
      axisLabel: {
        interval: 0,
        rotate: 30,
        formatter: (value) => {
          // 书名过长时截断
          return value.length > 12 ? value.substring(0, 12) + '...' : value
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '借阅次数',
      minInterval: 1
    },
    series: [
      {
        name: '借阅次数',
        type: 'bar',
        data: topBooks.value.map(item => item.borrowCount),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        },
        barWidth: '50%'
      }
    ]
  }
  
  // 设置图表选项
  chartInstance.setOption(option)
  
  // 自适应窗口大小
  window.addEventListener('resize', () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  })
}

/**
 * 查询统计
 */
const handleQuery = () => {
  loading.value = true
  
  setTimeout(() => {
    // 生成统计数据
    topBooks.value = generateStatisticsData()
    
    // 初始化图表
    nextTick(() => {
      initChart()
    })
    
    loading.value = false
    
    const timeDimensionText = filterForm.timeDimension === 'week' ? '本周' : '本月'
    ElMessage.success(`${timeDimensionText}统计完成`)
  }, 500)
}

/**
 * 销毁图表实例
 */
const destroyChart = () => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  })
}

onMounted(() => {
  // 恢复借阅数据
  borrowStore.restoreFromStorage()
  
  // 默认加载本周统计
  handleQuery()
})

// 组件卸载时销毁图表
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  destroyChart()
})
</script>

<style scoped>
/* 页面容器 */
.statistics-container {
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

/* 统计卡片 */
.statistics-card {
  padding: 20px;
}

/* 区域标题 */
.section-title {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 筛选区域 */
.filter-section {
  background-color: #f9fafb;
  padding: 20px;
  border-radius: 8px;
}

.filter-form {
  margin-top: 10px;
}

/* 图表区域 */
.chart-section {
  margin-top: 20px;
}

.chart-container {
  width: 100%;
  height: 400px;
}

/* 列表区域 */
.table-section {
  margin-top: 20px;
}

.top-table {
  margin-top: 15px;
}

/* 借阅次数样式 */
.borrow-count {
  font-size: 18px;
  font-weight: 600;
  color: var(--primary-color);
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

/* 排名标签优化 */
:deep(.el-tag--large) {
  font-size: 16px;
  font-weight: 600;
  padding: 8px 12px;
}
</style>
