<template>
  <div class="analysis-page-container">
    <h1>成绩分析</h1>

    <div class="overview-cards-section">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="overview-card accent-blue">
            <template #header>
              <div class="card-header">
                <i class="icon-average-score"></i>
                <span>平均分</span>
              </div>
            </template>
            <div class="card-content">
              <span class="score">{{ averageScore }}</span>
              <span class="unit">分</span>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="overview-card accent-green">
            <template #header>
              <div class="card-header">
                <i class="icon-highest-score"></i>
                <span>最高分</span>
              </div>
            </template>
            <div class="card-content">
              <span class="score">{{ highestScore }}</span>
              <span class="unit">分</span>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="overview-card accent-orange">
            <template #header>
              <div class="card-header">
                <i class="icon-exam-count"></i>
                <span>考试次数</span>
              </div>
            </template>
            <div class="card-content">
              <span class="score">{{ examCount }}</span>
              <span class="unit">次</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="chart-section">
      <el-row :gutter="24">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <i class="icon-trend"></i>
                <span>成绩趋势</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">成绩趋势图表区域</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <i class="icon-distribution"></i>
                <span>成绩分布</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">成绩分布图表区域</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="detail-section">
      <el-card class="detail-table-card">
        <template #header>
          <div class="card-header">
            <i class="icon-details"></i>
            <span>详细成绩列表</span>
          </div>
        </template>
        <el-table :data="scoreDetails" style="width: 100%" border stripe class="styled-table">
          <el-table-column prop="examName" label="考试名称" min-width="200" />
          <el-table-column prop="examTime" label="考试时间" width="180" align="center" />
          <el-table-column prop="score" label="得分" width="100" align="center" sortable>
            <template #default="scope">
              <span :class="{ 'high-score': scope.row.score >= 90, 'low-score': scope.row.score < 60, 'medium-score': scope.row.score >=60 && scope.row.score < 90 }">
                {{ scope.row.score }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="rank" label="排名" width="100" align="center" sortable />
          <el-table-column prop="averageScore" label="班级平均分" width="120" align="center" />
          <el-table-column prop="highestScore" label="班级最高分" width="120" align="center" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// 模拟数据 (保持不变)
const averageScore = ref(85.5);
const highestScore = ref(98);
const examCount = ref(10);

const scoreDetails = ref([
  { examName: '2024年春季学期期中考试', examTime: '2024-03-20', score: 85, rank: 15, averageScore: 82.5, highestScore: 98 },
  { examName: 'Java程序设计基础测试', examTime: '2024-03-15', score: 92, rank: 5, averageScore: 85.0, highestScore: 95 },
  { examName: 'Web前端技术摸底考试', examTime: '2024-04-02', score: 58, rank: 32, averageScore: 70.5, highestScore: 93 },
  { examName: '数据库原理综合测验', examTime: '2024-04-10', score: 75, rank: 10, averageScore: 72.0, highestScore: 89 },
]);
</script>

<style scoped>
/* --- 全局与页面布局 --- */
.analysis-page-container { /* Was .analysis-container */
  padding: 25px 35px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f8f9fa;
  min-height: 100vh;
  color: #333;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 35px;
  font-size: 2.4em;
  font-weight: 600;
}

/* --- Element Plus Card 全局微调 (如果需要) --- */
:deep(.el-card__header) { /* 使用 :deep() 来穿透 Element Plus 组件的样式 */
  padding: 18px 25px; /* 统一卡片头部内边距 */
  border-bottom: 1px solid #e9ecef; /* 更柔和的分割线 */
  background-color: #fcfdff; /* 非常浅的头部背景 */
}
:deep(.el-card__body) {
  padding: 25px; /* 统一卡片主体内边距 */
}

/* 自定义卡片头部内容样式 */
.card-header {
  display: flex;
  align-items: center;
  font-size: 1.15em; /* 调整头部文字大小 */
  font-weight: 600;
  color: #343a40;
}
.card-header i[class^="icon-"] { /* 图标占位符 */
  margin-right: 10px;
  font-size: 1.2em;
  color: #007bff; /* 图标使用主题色 */
}


/* --- 概览卡片区域 --- */
.overview-cards-section { /* Was .overview-cards */
  margin-bottom: 30px; /* 增加与下方图表区域的间距 */
}

.overview-card {
  text-align: center;
  border-radius: 10px; /* 卡片圆角 */
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
}
.overview-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.1); /* 更明显的悬停阴影 */
}
/* 不同概览卡的强调色 */
.overview-card.accent-blue :deep(.el-card__header) { border-left: 5px solid #007bff; }
.overview-card.accent-green :deep(.el-card__header) { border-left: 5px solid #28a745; }
.overview-card.accent-orange :deep(.el-card__header) { border-left: 5px solid #fd7e14; }

.overview-card .card-header span {
  color: #495057; /* 头部文字颜色调整 */
}

.overview-card .card-content {
  padding: 25px 10px; /* 调整内容区内边距 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100px; /* 保证一定高度 */
}
.overview-card .score {
  font-size: 3em; /* 更大的分数 */
  font-weight: 700; /* 更粗的字重 */
  color: #2c3e50; /* 深色分数 */
  line-height: 1.1;
  margin-bottom: 5px;
}
.overview-card.accent-blue .score { color: #007bff; }
.overview-card.accent-green .score { color: #28a745; }
.overview-card.accent-orange .score { color: #fd7e14; }

.overview-card .unit {
  font-size: 1em; /* 单位字号 */
  color: #6c757d; /* 单位颜色 */
  margin-left: 5px;
}

/* --- 图表区域 --- */
.chart-section {
  margin-bottom: 30px;
}

.chart-card {
  height: 420px; /* 增加卡片高度 */
  border-radius: 10px;
  display: flex; /* 使得内部 .chart-container 可以 flex-grow */
  flex-direction: column;
}
.chart-card :deep(.el-card__body) {
  flex-grow: 1; /* 让 card body 占据剩余空间 */
  display: flex; /* 为 chart-container 提供 flex 上下文 */
  padding: 20px; /* 调整内边距 */
}

.chart-container {
  flex-grow: 1; /* 占据 card body 的所有可用空间 */
  height: auto !important; /* 覆盖 Element Plus 可能设置的固定高度，让其自适应 */
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f3f5; /* 占位符背景 */
  border-radius: 6px;
  border: 1px dashed #d0d9e0; /* 虚线边框 */
}
.chart-placeholder {
  color: #868e96; /* 占位符文字颜色 */
  font-size: 1.1em;
  text-align: center;
}

/* --- 详细成绩表格区域 --- */
.detail-section {
  margin-top: 20px; /* 与原样式保持一致 */
}
.detail-table-card {
  border-radius: 10px;
}
.detail-table-card .card-header { /* 自定义表头样式 */
  /* 已通过全局 .card-header 应用 */
}

/* Element Table 样式微调 */
.styled-table :deep(th.el-table__cell) { /* 表头样式 */
  background-color: #f5f7fa !important;
  color: #303133 !important;
  font-weight: 600;
  font-size: 0.95em;
}
.styled-table :deep(td.el-table__cell) { /* 单元格样式 */
  padding: 10px 0; /* 调整单元格上下内边距 */
  font-size: 0.9em;
}
.styled-table :deep(.el-table__row--striped td.el-table__cell) { /* 斑马条纹 */
  background-color: #fafcff !important;
}


.high-score {
  color: #28a745; /* 更鲜明的绿色 */
  font-weight: 600; /* 加粗 */
}
.medium-score { /* 新增，用于普通分数 */
  color: #007bff; /* 蓝色 */
}
.low-score {
  color: #dc3545; /* 更鲜明的红色 */
  font-weight: 600;
}

/* --- 响应式调整 --- */
@media (max-width: 992px) { /* 中等屏幕 */
  .overview-cards-section .el-col {
    margin-bottom: 20px; /* 概览卡片在换行时增加间距 */
  }
  .overview-cards-section .el-col:last-child {
    margin-bottom: 0;
  }
  .chart-section .el-col {
    margin-bottom: 20px;
  }
  .chart-section .el-col:last-child {
    margin-bottom: 0;
  }
}

@media (max-width: 768px) { /* 小型屏幕 */
  .analysis-page-container { padding: 20px 15px; }
  h1 { font-size: 2em; margin-bottom: 25px; }
  :deep(.el-card__header) { padding: 15px 20px; }
  .card-header { font-size: 1.05em; }
  :deep(.el-card__body) { padding: 20px; }

  .overview-card .score { font-size: 2.5em; }
  .overview-card .unit { font-size: 0.9em; }

  .chart-card { height: 350px; /* 减少图表卡片高度 */ }
  .chart-placeholder { font-size: 1em; }

  .styled-table :deep(th.el-table__cell),
  .styled-table :deep(td.el-table__cell) {
    font-size: 0.85em; /* 减小表格字体 */
    padding: 8px 0;
  }
  .el-table :deep(.el-table__cell) { /* Element Plus >=2.4.0 */
    padding: 8px 0 !important;
  }

}

/* Placeholder Icons (您需要用真实图标替换) */

</style>
<!--.icon-average-score::before { content: "📊"; /* 示例，下同 */ }-->
<!--.icon-highest-score::before { content: "🏆"; }-->
<!--.icon-exam-count::before { content: " countable"; }-->
<!--.icon-trend::before { content: "📈"; }-->
<!--.icon-distribution::before { content: "📉"; } /* 也许用饼图或柱状图图标 */-->
<!--.icon-details::before { content: "📋"; }-->