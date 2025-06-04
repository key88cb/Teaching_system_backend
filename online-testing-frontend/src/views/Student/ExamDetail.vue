<template>
  <div class="exam-detail-page">
    <h1>考试详情</h1>

    <div v-if="exam" class="exam-info-card-container">
      <div class="exam-info-card">
        <div class="card-header">
          <h2 class="exam-title">{{ exam.course }} - {{ exam.subject }}</h2>
        </div>
        <div class="card-body">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">出卷人：</span>
              <span class="info-value">{{ exam.teacher }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">题量：</span>
              <span class="info-value">{{ exam.totalQuestions }} 题</span>
            </div>
            <div class="info-item">
              <span class="info-label">满分：</span>
              <span class="info-value">{{ exam.fullScore }} 分</span>
            </div>
            <div class="info-item info-item-full-width time-info">
              <span class="info-label">开放时间：</span>
              <span class="info-value">{{ formatDate(exam.startTime) }}</span>
            </div>
            <div class="info-item info-item-full-width time-info">
              <span class="info-label">结束时间：</span>
              <span class="info-value">{{ formatDate(exam.endTime) }}</span>
            </div>
          </div>
        </div>
        <div class="card-footer">
          <button
              class="btn start-btn"
              :class="isExamOngoing ? 'primary-btn' : 'disabled-btn'"
              :disabled="!isExamOngoing"
              @click="startTheExam"
          >
            <i :class="isExamOngoing ? 'icon-play' : 'icon-time-wait'"></i>
            {{ buttonText }}
          </button>
          <p v-if="!isExamOngoing && !isExamEnded" class="exam-status-note">
            考试将于 {{ formatDate(exam.startTime) }} 开始
          </p>
          <p v-if="isExamEnded" class="exam-status-note ended-note">
            本次考试已于 {{ formatDate(exam.endTime) }} 结束
          </p>
        </div>
      </div>
    </div>
    <div v-else class="loading-or-error-indicator">
      <p>正在加载考试详情或考试不存在...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'; // Added onMounted
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const exam = ref(null); // Initialize as null
const isLoading = ref(true); // Loading state

// 示例数据 - 实际应从 API 获取或通过路由状态传递
const mockExams = [
  {
    id: 1,
    course: '计算机基础',
    subject: '操作系统原理',
    teacher: '张老师',
    startTime: new Date(Date.now() - 1000 * 60 * 30).toISOString(), // 30分钟前开始
    endTime: new Date(Date.now() + 3600 * 1000 * 0.5).toISOString(), // 30分钟后结束
    fullScore: 100,
    totalQuestions: 20
  },
  {
    id: 2,
    course: '软件工程',
    subject: '需求分析与设计',
    teacher: '李老师',
    startTime: new Date(Date.now() + 86400 * 1000).toISOString(), // 一天后开始
    endTime: new Date(Date.now() + 86400 * 1000 + 3600 * 1000).toISOString(),
    fullScore: 75,
    totalQuestions: 15
  },
  {
    id: 3,
    course: '高级算法',
    subject: '动态规划专题',
    teacher: '王教授',
    startTime: new Date(Date.now() - 86400 * 1000 * 2).toISOString(), // 两天前开始
    endTime: new Date(Date.now() - 86400 * 1000).toISOString(), // 一天前结束
    fullScore: 100,
    totalQuestions: 10
  }
];

onMounted(() => {
  isLoading.value = true;
  const examId = parseInt(route.params.id);
  // 实际应用中，这里应该是API调用
  // const fetchedExam = await fetchExamByIdAPI(examId);
  const foundExam = mockExams.find(e => e.id === examId);

  if (foundExam) {
    exam.value = foundExam;
  } else {
    alert('考试不存在或加载失败');
    router.push('/student/dashboard'); // 或者一个错误页面
  }
  isLoading.value = false;
});


const now = ref(new Date()); // Reactive current time for ongoing checks
let timerId = null;

onMounted(() => {
  timerId = setInterval(() => {
    now.value = new Date();
  }, 1000 * 60); // Update 'now' every minute for ongoing status check
});

import { onUnmounted } from 'vue'; // Import onUnmounted
onUnmounted(() => {
  if (timerId) {
    clearInterval(timerId);
  }
});


const isExamStarted = computed(() => {
  if (!exam.value || !exam.value.startTime) return false;
  return now.value >= new Date(exam.value.startTime);
});

const isExamEnded = computed(() => {
  if (!exam.value || !exam.value.endTime) return false;
  return now.value > new Date(exam.value.endTime);
});

const isExamOngoing = computed(() => {
  return isExamStarted.value && !isExamEnded.value;
});

const buttonText = computed(() => {
  if (!exam.value) return '加载中...';
  if (isExamEnded.value) return '考试已结束';
  if (isExamOngoing.value) return '开始答题';
  return '考试未开始';
});

const startTheExam = () => {
  if (isExamOngoing.value && exam.value) {
    router.push(`/student/exam/${exam.value.id}/questions`);
  } else if (isExamEnded.value) {
    alert('本次考试已结束。');
  } else {
    alert('考试尚未开始，请在指定时间进入。');
  }
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '无效日期';
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};
const pad = (num) => String(num).padStart(2, '0');

</script>

<style scoped>
/* --- 全局与页面布局 --- */
.exam-detail-page {
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

/* --- 信息卡片容器 --- */
.exam-info-card-container {
  max-width: 700px; /* 限制卡片最大宽度 */
  margin: 0 auto 30px auto; /* 上下间距，左右居中 */
}

.exam-info-card {
  background: white;
  padding: 0; /* Remove padding, handle with sections */
  border-radius: 12px; /* Softer radius */
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.09); /* Softer, more spread shadow */
  overflow: hidden; /* Ensure child elements conform to border-radius */
}

.card-header {
  background-color: #007bff; /* Primary theme color */
  color: white;
  padding: 20px 30px;
  border-bottom: 1px solid #0069d9;
}
.exam-title { /* Was h2 */
  margin: 0;
  font-size: 1.6em;
  font-weight: 600;
}

.card-body {
  padding: 25px 30px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); /* Responsive columns */
  gap: 18px 25px; /* Row gap, Column gap */
}

.info-item {
  font-size: 1.05em; /* Slightly larger text */
  line-height: 1.6;
  display: flex; /* For aligning label and value if needed, or just for block display */
  flex-direction: column; /* Stack label and value */
}
.info-item.info-item-full-width { /* For time items to span full width if needed */
  grid-column: 1 / -1;
}


.info-label {
  font-weight: 600; /* Bolder label */
  color: #495057;
  margin-bottom: 5px; /* Space between label and value */
  font-size: 0.9em; /* Label slightly smaller */
  text-transform: uppercase; /* Optional: make labels uppercase */
  letter-spacing: 0.5px;
}

.info-value {
  color: #212529; /* Darker value text */
  font-weight: 400;
}
.info-item.time-info .info-value {
  font-weight: 500;
  color: #0056b3; /* Emphasize time */
}


/* --- 操作按钮与状态提示 --- */
.card-footer {
  padding: 25px 30px;
  background-color: #f8f9fa; /* Slight background for footer */
  border-top: 1px solid #e9ecef;
  text-align: center; /* Center button and status note */
}

.btn {
  padding: 12px 28px; /* Generous padding */
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.1em; /* Larger font for main action */
  font-weight: 500;
  transition: all 0.2s ease-in-out;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px; /* Space for icon */
  text-decoration: none;
  line-height: 1.5;
  min-width: 180px; /* Minimum button width */
}
.btn:hover:not([disabled]) {
  opacity: 0.85;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.12);
}
.btn:active:not([disabled]) {
  transform: translateY(0);
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}
.btn i[class^="icon-"] {
  font-size: 1.2em;
}

.primary-btn { background-color: #007bff; color: white; box-shadow: 0 2px 5px rgba(0,123,255,0.25); }
.primary-btn:hover:not([disabled]) { background-color: #0069d9; }

.disabled-btn,
.start-btn[disabled] { /* Retaining .start-btn[disabled] for specificity from original */
  background-color: #adb5bd; /* More appealing disabled color */
  color: #6c757d;
  cursor: not-allowed;
  box-shadow: none;
}
.disabled-btn:hover,
.start-btn[disabled]:hover {
  opacity: 1;
  transform: none;
}


.exam-status-note {
  margin-top: 15px;
  font-size: 0.95em;
  color: #6c757d;
}
.exam-status-note.ended-note {
  color: #dc3545; /* Red for ended status */
  font-weight: 500;
}

.loading-or-error-indicator {
  text-align: center;
  padding: 50px 20px;
  margin-top: 20px;
  background-color: #f0f3f5;
  border-radius: 8px;
  border: 1px dashed #d0d9e0;
  font-size: 1.1em;
  color: #5a6268;
}


/* --- 响应式调整 --- */
@media (max-width: 768px) {
  .exam-detail-page { padding: 20px 15px; }
  h1 { font-size: 2em; margin-bottom: 25px; }
  .exam-info-card-container { margin-left: 0; margin-right: 0; }
  .card-header { padding: 15px 20px; }
  .exam-title { font-size: 1.3em; }
  .card-body { padding: 20px; }
  .info-grid { grid-template-columns: 1fr; /* Stack info items on small screens */ gap: 15px;}
  .info-item { font-size: 1em; }
  .card-footer { padding: 20px; }
  .btn.start-btn { width: 100%; font-size: 1em; padding: 12px 20px; }
}

/* Placeholder icons (replace with actual icon font/SVG classes) */
.icon-play::before { content: "▶"; margin-right: 8px; }

</style>