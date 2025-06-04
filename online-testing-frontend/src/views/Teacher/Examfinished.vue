<template>
  <div class="ended-exams-page">
    <h1>已结束考试管理</h1>

    <div class="controls-container filter-bar">
      <div class="filter-group">
        <label for="filter-course-id">班级ID：</label>
        <select id="filter-course-id" v-model="filterCourseId" class="form-control">
          <option value="">全部班级</option>
          <option v-for="id in uniqueCourseIds" :key="id" :value="id">{{ id }}</option>
        </select>
      </div>
      <div class="filter-group">
        <label for="filter-creator">创建者：</label>
        <select id="filter-creator" v-model="filterCreator" class="form-control">
          <option value="">全部创建者</option>
          <option v-for="c in uniqueCreators" :key="c" :value="c">{{ c }}</option>
        </select>
      </div>
    </div>

    <div class="content-section">
      <div v-if="isLoading" class="loading-indicator">
        <p>正在加载已结束考试...</p>
      </div>
      <div v-else-if="filteredEndedpaperInfos.length > 0" class="paperInfos-grid">
        <div
            v-for="paperInfo in filteredEndedpaperInfos"
            :key="paperInfo.paperId"
            class="paperInfo-card status-ended"
        >
          <div class="card-main-info">
            <h3 class="paper-title">{{ paperInfo.paperName || '未命名试卷' }}</h3>
            <p class="paper-meta">课程ID: {{ paperInfo.courseId }} | 创建者: {{ paperInfo.creator }}</p>
            <p class="paper-time">考试时间：{{ formatDate(paperInfo.openTime) }} - {{ formatDate(paperInfo.closeTime) }}</p>
          </div>
          <div class="card-actions">
            <button class="btn primary-btn icon-btn" @click="searchforinfo(paperInfo.paperId,paperInfo.courseId)" title="查看本次考试的详细统计分析">
              <i class="icon-report"></i> 考试分析
            </button>
            <button class="btn secondary-btn icon-btn" @click="gotostudentinfo(paperInfo)" title="查看并管理学生成绩">
              <i class="icon-student-list"></i> 学生成绩
            </button>
          </div>
        </div>
      </div>
      <div v-else class="no-results">
        <p>没有找到符合条件的已结束考试。</p>
        <p class="sub-text">请尝试调整筛选条件或等待考试结束后查看。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'; // Removed unused 'watch'
import { useRouter } from 'vue-router';

const router = useRouter();
const isLoading = ref(true); // Added for loading state
const paperInfos = ref([]);
const url_front = 'http://localhost:8080/';

// const constId = ref(3); // Hardcoded courseId filter for fetching, consider making dynamic or removing if API fetches all
// const creator = ref(""); // Creator filter for fetching

const filterCourseId = ref(''); // For client-side filtering
const filterCreator = ref(''); // For client-side filtering

onMounted(() => {
  fetchPaperInfos();
});

const fetchPaperInfos = async () => {
  isLoading.value = true;
  try {
    // Construct params carefully. If constId or creator are for initial fetch filtering:
    const params = new URLSearchParams();
    // Example: if you want to always filter by a default course or creator from backend
    // if (constId.value) params.append('courseId', constId.value);
    // if (creator.value) params.append('creator', creator.value);
    // If no initial backend filtering is needed, params can be empty for query-all
    // For now, assuming it fetches all relevant papers based on the URL

    // The URL implies fetching all papers, then client-side filtering is applied.
    // If your backend supports filtering for query-all-papers, you could pass filterCourseId and filterCreator here.
    // For now, sticking to client-side filtering after fetching all.
    const url = url_front+`api/paper-questions/query-all-papers`; // Removed params for now if they are for client filtering
    const res = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });

    if (!res.ok) {
      const errorData = await res.json().catch(() => null);
      throw new Error(errorData?.message || `网络错误 ${res.status}`);
    }
    const data = await res.json();
    paperInfos.value = data.map(p => ({
      ...p,
      paperName: p.paperName || "未命名试卷", // Provide default if paperName is null
      openTime: p.openTime,
      closeTime: p.closeTime
    }));
    console.log('已拉取试卷数据:', paperInfos.value.length);
  } catch (error) {
    alert('加载考试列表失败: ' + error.message);
    console.error("fetchPaperInfos error:", error);
    paperInfos.value = [];
  } finally {
    isLoading.value = false;
  }
};


const uniqueCourseIds = computed(() => {
  return [...new Set(paperInfos.value.map(item => item.courseId).filter(id => id != null))].sort((a,b)=> a-b);
});

const uniqueCreators = computed(() => {
  return [...new Set(paperInfos.value.map(item => item.creator).filter(Boolean))].sort();
});

// Filter logic for "ended" papers based on the full list, then apply UI filters
const endedpaperInfos = computed(() => {
  const now = new Date();
  return paperInfos.value.filter(e => e.closeTime && new Date(e.closeTime) <= now);
});

const filteredEndedpaperInfos = computed(() => {
  return endedpaperInfos.value.filter(paperInfo => {
    const courseMatch = !filterCourseId.value || String(paperInfo.courseId) === String(filterCourseId.value);
    const creatorMatch = !filterCreator.value || paperInfo.creator === filterCreator.value;
    return courseMatch && creatorMatch;
  });
});

const nopaperInfos = computed(() => {
  return !isLoading.value && filteredEndedpaperInfos.value.length === 0;
});

const searchforinfo = (paperId, courseId) => {
  router.push(`/teacher/exams_detail/${courseId}/${paperId}`);
};

const gotostudentinfo = (paperInfo) => {
  // Passing mode=edit seems intentional from user's code, implies teacher might edit student scores/details
  router.push(`/teacher/exam-details-student-score/${paperInfo.courseId}/${paperInfo.paperId}?mode=edit`);
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '日期无效';
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
};

</script>

<style scoped>
/* --- 全局与页面布局 --- */
.ended-exams-page { /* Renamed from .paperInfo-management */
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

/* --- Filter Bar using .controls-container style --- */
.controls-container.filter-bar {
  background-color: #fff;
  padding: 20px 25px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
  margin-bottom: 30px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-grow: 1;
  min-width: 250px;
}
.filter-group label {
  font-weight: 500;
  color: #495057;
  white-space: nowrap;
  margin-bottom: 0; /* Align with select */
}
.filter-group .form-control { /* Selects in filter bar */
  flex-grow: 1;
}

/* --- 表单控件统一样式 --- */
.form-control {
  display: block;
  width: 100%;
  padding: 10px 15px;
  font-size: 1em;
  font-weight: 400;
  line-height: 1.5;
  color: #495057;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid #ced4da;
  appearance: none;
  border-radius: 6px;
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
select.form-control {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m2 5 6 6 6-6'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 0.9rem center;
  background-size: 16px 12px;
  cursor: pointer;
}

/* --- 按钮通用样式 --- */
.btn {
  padding: 10px 18px; border: none; border-radius: 6px; cursor: pointer;
  font-size: 0.95em; font-weight: 500; transition: all 0.2s ease-in-out;
  display: inline-flex; align-items: center; justify-content: center;
  gap: 8px; text-decoration: none; line-height: 1.5;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
}
.btn:hover:not(:disabled) { opacity: 0.85; transform: translateY(-1px); box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
.btn:active:not(:disabled) { transform: translateY(0); box-shadow: 0 2px 4px rgba(0,0,0,0.08); }
.btn i[class^="icon-"] { font-size: 1.1em; }

.primary-btn { background-color: #007bff; color: white; }
.primary-btn:hover { background-color: #0069d9; }
.secondary-btn { background-color: #6c757d; color: white; }
.secondary-btn:hover { background-color: #5a6268; }


/* --- 考试列表与卡片 --- */
.content-section {
  margin-top: 20px;
}
.paperInfos-grid { /* Renamed from .paperInfos-list for consistency with other grid names */
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); /* Adjusted minmax */
  gap: 25px;
}

.paperInfo-card {
  background: white;
  padding: 20px 25px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.07);
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out, border-left-color 0.2s ease-out;
  display: flex;
  flex-direction: column;
  border-left: 5px solid #6c757d; /* Default "ended" color */
}
.paperInfo-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}
.card-main-info {
  flex-grow: 1;
}
.paper-title { /* Was h3 */
  color: #2c3e50;
  font-size: 1.2em;
  font-weight: 600;
  margin-top: 0;
  margin-bottom: 10px;
  word-break: break-word;
}
.paper-meta {
  font-size: 0.85em;
  color: #6c757d;
  margin-bottom: 8px;
  line-height: 1.5;
}
.paper-time {
  font-size: 0.9em;
  color: #333;
  margin-bottom: 15px;
  line-height: 1.5;
}

.card-actions { /* Was .paperInfo-actions */
  margin-top: auto; /* Push actions to bottom */
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-start; /* Align buttons to start or use space-between if preferred */
}
.card-actions .btn { /* Smaller buttons within cards */
  padding: 8px 15px;
  font-size: 0.9em;
}

/* --- 空状态与加载状态 --- */
.loading-indicator,
.no-results {
  grid-column: 1 / -1; /* Span across grid if parent is grid */
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
.no-results p {
  margin: 0 0 8px 0;
}
.no-results .sub-text { /* Optional sub-text for no-results */
  font-size: 0.95em;
  color: #868e96;
}


/* --- 响应式调整 --- */
@media (max-width: 768px) {
  .ended-exams-page { padding: 20px 15px; }
  h1 { font-size: 2em; margin-bottom: 25px; }

  .controls-container.filter-bar { padding: 15px; flex-direction: column; gap: 15px; }
  .filter-group { flex-direction: column; align-items: stretch; width: 100%; min-width: unset;}
  .filter-group label { margin-bottom: 5px; }
  .filter-group .form-control { width: 100%; }

  .paperInfos-grid { grid-template-columns: 1fr; gap: 15px; }
  .paperInfo-card { padding: 15px 20px; }
  .paper-title { font-size: 1.1em; }
  .card-actions { justify-content: space-around; }
  .card-actions .btn { flex-grow: 1; max-width: 48%; } /* Two buttons per row */
}

/* Placeholder Icons */
.icon-report::before { content: "📊"; margin-right: 6px;}
.icon-student-list::before { content: "👥"; margin-right: 6px;}

</style>