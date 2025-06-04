<template>
  <div class="student-dashboard-page">
    <h1>考试信息</h1>

    <div class="controls-container">
      <div class="search-bar">
        <input
            v-model="searchQuery"
            placeholder="输入课程ID或考试名称搜索"
            class="form-control search-input"
        />
        <button @click="clearSearch" class="btn secondary-btn clear-btn" title="清除搜索">
          <i class="icon-clear"></i> 清除
        </button>
      </div>

      <div class="tabs-wrapper">
        <div class="tabs">
          <button
              :class="['tab-btn', { active: currentTab === 'ongoing' }]"
              @click="setActiveTab('ongoing')"
          >
            <i class="icon-ongoing"></i> 进行中考试 ({{ ongoingExams.length }})
          </button>
          <button
              :class="['tab-btn', { active: currentTab === 'notStarted' }]"
              @click="setActiveTab('notStarted')"
          >
            <i class="icon-future"></i> 未开始考试 ({{ notStartedExams.length }})
          </button>
        </div>
      </div>
    </div>

    <div class="content-section">
      <div v-if="isLoading" class="loading-indicator">
        <p>正在加载考试列表...</p>
      </div>
      <div v-else-if="filteredExams.length > 0" class="exams-grid">
        <div
            v-for="exam in filteredExams"
            :key="exam.paperId"
            class="exam-card"
            :class="{
              'status-ongoing': currentTab === 'ongoing',
              'status-not-started': currentTab === 'notStarted'
            }"
            @click="viewExamInfo(exam)"
            tabindex="0"
            role="button"
            :aria-label="`查看考试信息: ${exam.paperName}`"
        >
          <div class="card-header">
            <h3 class="exam-title">{{ exam.paperName || '未命名考试' }}</h3>
            <span v-if="currentTab === 'ongoing'" class="status-indicator ongoing">进行中</span>
            <span v-if="currentTab === 'notStarted'" class="status-indicator not-started">未开始</span>
          </div>
          <div class="exam-info-body">
            <p><span class="info-label">课程：</span>{{ getCourseNameById(exam.courseId) }} (ID: {{ exam.courseId }})</p>
            <p><span class="info-label">出卷人：</span>{{ exam.creator || '未知' }}</p>
            <p v-if="currentTab === 'ongoing'" class="time-info">
              <span class="info-label">截止时间：</span>{{ formatDate(exam.closeTime) }}
              <span class="remaining-time" v-if="exam.remainingTime !== undefined"> (剩余: {{ formatRemainingTime(exam.remainingTime) }})</span>
            </p>
            <p v-if="currentTab === 'notStarted'" class="time-info">
              <span class="info-label">开始时间：</span>{{ formatDate(exam.openTime) }}
            </p>
          </div>
          <div class="card-footer">
            <button
                class="btn start-exam-btn"
                :class="isExamOngoing(exam) ? 'primary-btn' : 'disabled-btn'"
                :disabled="!isExamOngoing(exam)"
                @click.stop="goToConcreteExam(exam)"
                :title="isExamOngoing(exam) ? '参加本次考试' : '考试尚未开始或已结束'"
            >
              <i :class="isExamOngoing(exam) ? 'icon-play' : 'icon-time-wait'"></i>
              {{ isExamOngoing(exam) ? '参加考试' : (isExamEnded(exam) ? '考试已结束' : '等待开始') }}
            </button>
          </div>
        </div>
      </div>
      <div v-else-if="!isLoading" class="no-results">
        <p>当前分类下没有找到相关考试。</p>
        <p v-if="searchQuery" class="sub-text">请尝试修改搜索词或清除搜索。</p>
      </div>
    </div>


    <div v-if="showDialog && selectedExam" class="modal-overlay" @click.self="closeDialog">
      <div class="modal-dialog exam-info-dialog">
        <div class="modal-header">
          <h2 class="modal-title">{{ selectedExam.paperName }} - 考试信息</h2>
          <button class="modal-close-btn" @click="closeDialog" aria-label="关闭">&times;</button>
        </div>
        <div class="modal-body">
          <div class="dialog-info-item"><span class="info-label">课程ID：</span>{{ selectedExam.courseId }}</div>
          <div class="dialog-info-item"><span class="info-label">课程名称：</span>{{ getCourseNameById(selectedExam.courseId) }}</div>
          <div class="dialog-info-item"><span class="info-label">出卷人：</span>{{ selectedExam.creator || '未知' }}</div>
          <hr>
          <div class="dialog-info-item"><span class="info-label">开始时间：</span>{{ formatDate(selectedExam.openTime) }}</div>
          <div class="dialog-info-item"><span class="info-label">结束时间：</span>{{ formatDate(selectedExam.closeTime) }}</div>
          <hr>
          <div class="dialog-info-item"><span class="info-label">单选题数：</span>{{ selectedExam.singleChoiceNum ?? '未指定' }}</div>
          <div class="dialog-info-item"><span class="info-label">多选题数：</span>{{ selectedExam.multipleChoiceNum ?? '未指定' }}</div>
          <div class="dialog-info-item"><span class="info-label">判断题数：</span>{{ selectedExam.trueFalseNum ?? '未指定' }}</div>
          <div class="dialog-info-item"><span class="info-label">总分：</span>{{ selectedExam.totalScores ?? '未指定' }} 分</div>
        </div>
        <div class="modal-footer">
          <button @click="closeDialog" class="btn secondary-btn">关闭</button>
          <button
              v-if="isExamOngoing(selectedExam)"
              class="btn primary-btn"
              @click="goToConcreteExam(selectedExam); closeDialog();">
            <i class="icon-play"></i> 参加考试
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import axios from 'axios'; // Assuming axios is installed
import { useRouter } from 'vue-router';

const router = useRouter();
const url_front = 'http://localhost:8080/';

const currentTab = ref('ongoing'); // Default to ongoing exams
const notStartedExams = ref([]);
const ongoingExams = ref([]);
const searchQuery = ref('');
const isLoading = ref(true);

const showDialog = ref(false);
const selectedExam = ref(null);

// --- 科目ID到名称的映射 (示例) ---
const COURSE_ID_TO_NAME_MAP = {
  1: '计算机基础', 201: '操作系统原理',
  2: '软件工程',   3: '计算机网络',
  // 根据您的实际情况补充
};
const getCourseNameById = (courseId) => {
  return COURSE_ID_TO_NAME_MAP[courseId] || `课程ID ${courseId}`;
};
// ---

let nowInterval = null;
const currentTime = ref(new Date());

const fetchExams = async () => {
  isLoading.value = true;
  try {
    const [notStartedRes, ongoingRes] = await Promise.all([
      axios.get(url_front+'api/paper-questions/exams', { params: { status: 'notStarted' } }),
      axios.get(url_front+'api/paper-questions/exams', { params: { status: 'ongoing' } })
    ]);
    notStartedExams.value = (notStartedRes.data || []).map(exam => ({...exam, paperName: exam.paperName || '未命名考试'}));
    ongoingExams.value = (ongoingRes.data || []).map(exam => {
      const remainingTime = Math.max(0, Math.floor((new Date(exam.closeTime).getTime() - currentTime.value.getTime()) / 1000));
      return {...exam, paperName: exam.paperName || '未命名考试', remainingTime};
    });
  } catch (e) {
    console.error('获取考试信息失败:', e);
    alert('获取考试信息失败，请稍后重试。');
    notStartedExams.value = [];
    ongoingExams.value = [];
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchExams();
  nowInterval = setInterval(() => {
    currentTime.value = new Date();
    // Re-calculate remaining time for ongoing exams
    ongoingExams.value = ongoingExams.value.map(exam => ({
      ...exam,
      remainingTime: Math.max(0, Math.floor((new Date(exam.closeTime).getTime() - currentTime.value.getTime()) / 1000))
    }));
  }, 1000); // Update every second for remaining time
});

onUnmounted(() => {
  if (nowInterval) clearInterval(nowInterval);
});


const setActiveTab = (tab) => {
  currentTab.value = tab;
};

const isExamStarted = (exam) => {
  if (!exam || !exam.openTime) return false;
  return currentTime.value >= new Date(exam.openTime);
};
const isExamEnded = (exam) => {
  if (!exam || !exam.closeTime) return true; // If no close time, assume ended or invalid
  return currentTime.value > new Date(exam.closeTime);
};
const isExamOngoing = (exam) => {
  // Use the reactive currentTime for accurate status
  return exam && exam.openTime && exam.closeTime &&
      currentTime.value >= new Date(exam.openTime) &&
      currentTime.value <= new Date(exam.closeTime);
};


const goToConcreteExam = (exam) => { // Renamed from viewExamDetails for clarity
  if (isExamOngoing(exam)) {
    router.push(`/student/exam/${exam.courseId}/${exam.paperId}`);
  } else {
    // This case should ideally be handled by disabling the button
    alert(isExamEnded(exam) ? '本次考试已结束。' : '考试尚未开始，请在指定时间进入。');
  }
};

const viewExamInfo = (exam) => {
  selectedExam.value = exam;
  showDialog.value = true;
};

const closeDialog = () => {
  showDialog.value = false;
  selectedExam.value = null;
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '日期无效';
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};
const pad = (num) => String(num).padStart(2, '0');

const formatRemainingTime = (totalSeconds) => {
  if (typeof totalSeconds !== 'number' || totalSeconds < 0) return 'N/A';
  if (totalSeconds === 0) return '时间已到';
  const days = Math.floor(totalSeconds / (3600 * 24));
  const hours = Math.floor((totalSeconds % (3600 * 24)) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  let result = '';
  if (days > 0) result += `${days}天 `;
  if (hours > 0 || days > 0) result += `${String(hours).padStart(2, '0')}时`;
  if (minutes > 0 || hours > 0 || days > 0) result += `${String(minutes).padStart(2, '0')}分`;
  result += `${String(seconds).padStart(2, '0')}秒`;
  return result.trim();
};

const filteredExams = computed(() => {
  let examsToFilter = currentTab.value === 'ongoing' ? ongoingExams.value : notStartedExams.value;
  if (!Array.isArray(examsToFilter)) examsToFilter = []; // Ensure it's an array

  if (searchQuery.value) {
    const query = searchQuery.value.trim().toLowerCase();
    examsToFilter = examsToFilter.filter(exam =>
        (exam.courseId && String(exam.courseId).includes(query)) ||
        (exam.paperName && exam.paperName.toLowerCase().includes(query))
    );
  }
  return examsToFilter;
});

const clearSearch = () => {
  searchQuery.value = '';
};
</script>

<style scoped>
/* --- 全局与页面布局 --- */
.student-dashboard-page {
  padding: 25px 35px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f8f9fa;
  min-height: 100vh;
  color: #333;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 30px;
  font-size: 2.4em;
  font-weight: 600;
}

/* --- 控制区域容器 --- */
.controls-container {
  background-color: #fff;
  padding: 20px 25px; /* Adjusted padding */
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
  margin-bottom: 30px;
  /* display: flex; flex-direction: column; gap: 20px; */ /* Tabs now in their own wrapper */
}

.search-bar {
  display: flex;
  width: 100%;
  margin-bottom: 20px; /* Space between search and tabs */
}

/* --- 表单控件统一样式 --- */
.form-control {
  display: block;
  width: 100%;
  padding: 12px 18px;
  font-size: 1em;
  font-weight: 400;
  line-height: 1.5;
  color: #495057;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid #ced4da;
  appearance: none;
  border-radius: 8px;
  transition: border-color .15s ease-in-out,box-shadow .15s ease-in-out;
  box-sizing: border-box;
}
.form-control:focus {
  color: #495057;
  background-color: #fff;
  border-color: #86b7fe;
  outline: 0;
  box-shadow: 0 0 0 0.25rem rgba(13,110,253,.25);
}

.search-input.form-control {
  flex-grow: 1;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  border-right: none;
}
.search-input.form-control:focus {
  z-index: 1;
}

/* --- 按钮通用样式 --- */
.btn {
  padding: 12px 20px; border: none; border-radius: 8px; cursor: pointer;
  font-size: 1em; font-weight: 500; transition: all 0.2s ease-in-out;
  display: inline-flex; align-items: center; justify-content: center;
  gap: 8px; text-decoration: none; line-height: 1.5;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
}
.btn:hover:not([disabled]) { opacity: 0.85; transform: translateY(-1px); box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
.btn:active:not([disabled]) { transform: translateY(0); box-shadow: 0 2px 4px rgba(0,0,0,0.08); }
.btn i[class^="icon-"] { font-size: 1.1em; }

.primary-btn { background-color: #007bff; color: white; }
.primary-btn:hover:not([disabled]) { background-color: #0069d9; }
.secondary-btn { background-color: #6c757d; color: white; }
.secondary-btn:hover:not([disabled]) { background-color: #5a6268; }
.disabled-btn, .btn:disabled {
  background-color: #adb5bd !important; /* Ensure important to override other btn styles */
  color: #6c757d !important;
  cursor: not-allowed !important;
  box-shadow: none !important;
  opacity: 0.65 !important; /* Make it look more disabled */
}


.clear-btn.btn { /* Applied to clear button */
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  white-space: nowrap;
  flex-shrink: 0; /* Prevent shrinking */
}

/* --- Tabs --- */
.tabs-wrapper { /* New wrapper for tabs, if controls-container is not used for tabs */
  /* background-color: #fff; */ /* Part of controls-container now */
  /* border-radius: 8px; */
  /* box-shadow: 0 2px 8px rgba(0,0,0,0.05); */
  /* padding: 8px; */
  overflow-x: auto;
}
.tabs {
  display: flex;
  justify-content: stretch; /* Make tabs take full width if possible */
  /* border-bottom: 2px solid #dee2e6; */ /* Removed, using button borders now */
  min-width: fit-content;
}
.tab-btn {
  flex: 1; /* Tabs share width equally */
  padding: 12px 20px; /* Adjusted padding */
  font-size: 1.05em;
  font-weight: 500;
  color: #495057;
  background-color: #f0f3f5; /* Default tab background */
  border: 1px solid #dee2e6; /* Borders around tabs */
  border-bottom: 2px solid #007bff; /* Active border color at bottom */
  cursor: pointer;
  transition: color 0.2s ease, background-color 0.2s ease, border-color 0.2s ease;
  margin-right: -1px; /* Overlap borders */
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  justify-content: center;
  border-radius: 6px 6px 0 0; /* Rounded top corners */
}
.tab-btn:first-child {
  /* border-top-left-radius: 6px; */
}
.tab-btn:last-child {
  /* border-top-right-radius: 6px; */
  margin-right: 0;
}
.tab-btn:hover {
  color: #0056b3;
  background-color: #e9ecef;
}
.tab-btn.active {
  color: #fff;
  background-color: #007bff;
  border-color: #007bff;
  border-bottom: 2px solid #007bff; /* Keep bottom border consistent or hide for active */
  font-weight: 600;
  z-index: 1; /* Bring active tab to front */
}
.tab-btn.active:hover {
  background-color: #0069d9;
}
.tab-btn i[class^="icon-"] {
  font-size: 1.2em;
}


/* --- Exam List & Cards --- */
.content-section {
  margin-top: 20px;
}
.exams-grid { /* Renamed from .exam-list */
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 25px;
}

.exam-card {
  background: white;
  padding: 0; /* Remove direct padding, handle with internal sections */
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.07);
  cursor: pointer;
  transition: transform 0.25s ease-out, box-shadow 0.25s ease-out, border-left-color 0.25s ease-out;
  display: flex;
  flex-direction: column;
  border-left: 5px solid transparent; /* Base for status colors */
  overflow: hidden; /* Ensure content respects border-radius */
}
.exam-card.status-ongoing { border-left-color: #28a745; } /* Green */
.exam-card.status-not-started { border-left-color: #17a2b8; } /* Teal */

.exam-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}
.exam-card:focus-visible {
  outline: 2px solid #007bff;
  outline-offset: 2px;
}

.exam-card .card-header { /* Optional distinct header section */
  padding: 18px 22px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.exam-title { /* Was h3 */
  color: #2c3e50;
  font-size: 1.2em;
  font-weight: 600;
  margin: 0; /* Remove default margin if inside card-header */
  word-break: break-word;
}
.status-indicator {
  font-size: 0.8em;
  font-weight: 500;
  padding: 3px 8px;
  border-radius: 15px;
  color: white;
}
.status-indicator.ongoing { background-color: #28a745; }
.status-indicator.not-started { background-color: #17a2b8; }


.exam-info-body { /* Main content area of the card */
  padding: 18px 22px;
  flex-grow: 1;
}
.exam-info-body p {
  margin: 6px 0 10px 0; /* Adjust spacing */
  font-size: 0.9em;
  color: #555;
  line-height: 1.6;
}
.exam-info-body .info-label {
  font-weight: 500;
  color: #333;
  margin-right: 5px;
}
.exam-info-body .time-info {
  font-size: 0.85em;
  color: #6c757d;
}
.remaining-time {
  font-weight: 600;
  color: #e67e22; /* Orange for remaining time */
  margin-left: 5px;
}

.exam-card .card-footer {
  padding: 15px 22px;
  background-color: #f8f9fa; /* Slight background for footer */
  border-top: 1px solid #f0f0f0;
  text-align: right; /* Align button to the right */
}
.start-exam-btn.btn { /* Was .start-btn */
  padding: 10px 20px; /* Specific padding for this button */
  font-size: 0.95em;
}

/* --- Modal Styling --- */
.modal-overlay {
  position: fixed; inset: 0; background-color: rgba(0, 0, 0, 0.55);
  display: flex; align-items: center; justify-content: center;
  z-index: 1050; padding: 20px; overflow-y: auto;
}
.modal-dialog { /* Was .exam-dialog */
  background: white; border-radius: 10px; box-shadow: 0 8px 25px rgba(0,0,0,0.15);
  width: 100%; max-width: 550px; /* Adjusted max-width */
  max-height: 90vh;
  display: flex; flex-direction: column; margin: auto;
  text-align: left; /* Reset text-align from .exam-dialog */
}
.modal-header {
  padding: 18px 25px; border-bottom: 1px solid #e9ecef;
  display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0;
}
.modal-title { /* Was h2 inside .exam-dialog */
  margin: 0; font-size: 1.4em; font-weight: 600; color: #343a40;
}
.modal-close-btn {
  background: none; border: none; font-size: 1.8em; font-weight: 300;
  color: #6c757d; cursor: pointer; padding: 0; line-height: 1; opacity: 0.7;
}
.modal-close-btn:hover { color: #343a40; opacity: 1; }

.modal-body { padding: 25px; overflow-y: auto; flex-grow: 1; }
.dialog-info-item {
  padding: 8px 0;
  font-size: 1em;
  line-height: 1.6;
  border-bottom: 1px solid #f0f0f0;
}
.dialog-info-item:last-child {
  border-bottom: none;
}
.dialog-info-item .info-label { /* Reusing .info-label style */
  font-weight: 600; color: #495057; margin-right: 8px; display: inline-block; min-width: 80px;
}

.modal-footer {
  padding: 18px 25px; border-top: 1px solid #e9ecef;
  display: flex; justify-content: flex-end; gap: 10px;
  flex-shrink: 0; background-color: #f8f9fa;
  border-bottom-left-radius: 10px; border-bottom-right-radius: 10px;
}
.modal-footer .btn { font-size: 0.95em; } /* Was .dialog-close-btn for specific styling */


/* --- Empty State --- */
.loading-indicator,
.no-results {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px 20px;
  margin-top: 20px;
  background-color: #f0f3f5;
  border-radius: 8px;
  border: 1px dashed #d0d9e0;
  font-size: 1.1em;
  color: #5a6268;
}
.loading-indicator p,
.no-results p { margin: 0 0 8px 0; }
.no-results .sub-text { font-size: 0.95em; color: #868e96;}

/* --- Responsive --- */
@media (max-width: 768px) {
  .student-dashboard-page { padding: 20px 15px; }
  h1 { font-size: 2em; margin-bottom: 25px; }
  .controls-container { padding: 15px; }
  .search-bar { flex-direction: column; gap: 10px; margin-bottom: 15px; }
  .search-input.form-control { border-radius: 8px; border-right: 1px solid #ced4da;}
  .clear-btn.btn { border-radius: 8px; width: 100%; }

  .tabs-wrapper { margin-top: 0; }
  .tabs { flex-direction: column; align-items: stretch; /* border-bottom: none; */ }
  .tab-btn {
    justify-content: center;
    /* border-bottom: 1px solid #dee2e6;  */
    margin-bottom: -1px; /* Overlap borders if stacked */
    margin-right: 0;
    border-radius: 6px; /* Full radius when stacked */
  }
  .tab-btn + .tab-btn { margin-top: 5px;}
  .tab-btn.active {
    /* border-color: #007bff; */
    /* For stacked tabs, a left border might look better */
    border-left: 3px solid #007bff;
    border-bottom-color: #007bff; /* Keep active bottom border */
  }

  .exams-grid { grid-template-columns: 1fr; gap: 18px; }
  .exam-card .card-header { padding: 15px 18px; }
  .exam-card .exam-info-body { padding: 15px 18px; }
  .exam-card .card-footer { padding: 12px 18px; }
  .exam-title { font-size: 1.1em; }
  .start-exam-btn.btn { width: 100%; }

  .modal-dialog { max-width: 95%; }
  .modal-title { font-size: 1.3em;}
  .modal-body, .modal-header, .modal-footer { padding: 15px 20px; }
  .modal-footer { flex-direction: column; }
  .modal-footer .btn { width: 100%; }
}

/* Placeholder Icons */
.icon-clear::before { content: "✗"; margin-right: 5px; font-weight: bold;}
.icon-play::before { content: "▶"; margin-right: 8px; }
.icon-time-wait::before { content: "🕒"; margin-right: 8px; }
</style>