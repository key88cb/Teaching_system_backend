<template>
  <div class="test-container">
    <h1>在线测试</h1>
    <div class="test-list">
      <el-card v-for="test in availableTests" :key="test.id" class="test-card">
        <template #header>
          <div class="test-header">
            <span class="test-title">{{ test.title }}</span>
            <el-tag :type="test.status === '进行中' ? 'success' : 'info'">{{ test.status }}</el-tag>
          </div>
        </template>
        <div class="test-info">
          <p>考试时长：{{ test.duration }}分钟</p>
          <p>总分：{{ test.totalScore }}分</p>
          <p>开始时间：{{ test.startTime }}</p>
          <p>结束时间：{{ test.endTime }}</p>
        </div>
        <div class="test-actions">
          <el-button type="primary" @click="startTest(test.id)" :disabled="test.status !== '进行中'">
            开始考试
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 模拟考试数据
const availableTests = ref([
  {
    id: 1,
    title: '2024年春季学期期中考试',
    status: '进行中',
    duration: 120,
    totalScore: 100,
    startTime: '2024-03-20 09:00',
    endTime: '2024-03-20 11:00'
  },
  {
    id: 2,
    title: 'Java程序设计基础测试',
    status: '未开始',
    duration: 90,
    totalScore: 100,
    startTime: '2024-03-25 14:00',
    endTime: '2024-03-25 15:30'
  }
])

const startTest = (testId) => {
  // TODO: 实现开始考试的逻辑
  console.log('开始考试:', testId)
}
</script>

<style scoped>
.test-container {
  padding: 20px;
}

.test-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.test-card {
  transition: transform 0.3s;
}

.test-card:hover {
  transform: translateY(-5px);
}

.test-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.test-title {
  font-size: 18px;
  font-weight: bold;
}

.test-info {
  margin: 15px 0;
}

.test-info p {
  margin: 8px 0;
  color: #666;
}

.test-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}
</style> 