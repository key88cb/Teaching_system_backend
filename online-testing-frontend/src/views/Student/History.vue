<template>
  <div class="history-container">
    <h1>历史试卷</h1>
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="考试名称">
          <el-input v-model="filterForm.title" placeholder="请输入考试名称"></el-input>
        </el-form-item>
        <el-form-item label="考试时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="historyTests" style="width: 100%" border>
      <el-table-column prop="title" label="考试名称" />
      <el-table-column prop="examTime" label="考试时间" width="180" />
      <el-table-column prop="score" label="得分" width="100">
        <template #default="scope">
          <span :class="{ 'high-score': scope.row.score >= 90, 'low-score': scope.row.score < 60 }">
            {{ scope.row.score }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" link @click="viewDetail(scope.row)">查看详情</el-button>
          <el-button type="primary" link @click="downloadPaper(scope.row)">下载试卷</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const filterForm = reactive({
  title: '',
  dateRange: []
})

// 模拟历史考试数据
const historyTests = ref([
  {
    id: 1,
    title: '2024年春季学期期中考试',
    examTime: '2024-03-20 09:00-11:00',
    score: 85,
    status: '已完成'
  },
  {
    id: 2,
    title: 'Java程序设计基础测试',
    examTime: '2024-03-15 14:00-15:30',
    score: 92,
    status: '已完成'
  }
])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

const handleSearch = () => {
  // TODO: 实现搜索逻辑
  console.log('搜索条件：', filterForm)
}

const resetFilter = () => {
  filterForm.title = ''
  filterForm.dateRange = []
}

const getStatusType = (status) => {
  const statusMap = {
    '已完成': 'success',
    '进行中': 'warning',
    '未开始': 'info'
  }
  return statusMap[status] || 'info'
}

const viewDetail = (row) => {
  // TODO: 实现查看详情逻辑
  console.log('查看详情：', row)
}

const downloadPaper = (row) => {
  // TODO: 实现下载试卷逻辑
  console.log('下载试卷：', row)
}

const handleSizeChange = (val) => {
  pageSize.value = val
  // TODO: 重新加载数据
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  // TODO: 重新加载数据
}
</script>

<style scoped>
h1{
  color: black;
}
.history-container {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.high-score {
  color: #67c23a;
  font-weight: bold;
}

.low-score {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 