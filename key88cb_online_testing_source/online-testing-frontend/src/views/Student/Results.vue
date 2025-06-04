<template>
  <div class="exam-results-page">
    <h1>我的考试记录</h1>

    <div class="controls-container search-filter-panel">
      <div class="search-group">
        <i class="icon-search input-icon"></i>
        <input
            v-model="searchQuery"
            placeholder="搜索科目、课程或日期"
            class="form-control search-input with-icon"
        />
        <button @click="clearSearch" class="btn clear-btn" title="清除搜索">
          <i class="icon-clear"></i> 清除
        </button>
      </div>

      <div class="filter-group">
        <label for="date-filter" class="form-label visually-hidden">日期筛选：</label>
        <i class="icon-calendar input-icon"></i>
        <select v-model="selectedDateFilter" class="form-control date-filter with-icon" id="date-filter">
          <option value="">筛选时间范围</option>
          <option value="today">今天</option>
          <option value="thisWeek">本周</option>
          <option value="thisMonth">本月</option>
        </select>
      </div>
    </div>

    <div class="content-section">
      <div v-if="isLoading" class="loading-indicator">
        <p>正在加载考试记录...</p>
      </div>
      <div v-else-if="filteredResults.length > 0" class="results-grid">
        <div
            v-for="result in filteredResults"
            :key="result.id"
            class="result-card"
            @click="viewDetails(result)"
            tabindex="0"
            role="button"
            :aria-label="`查看 ${result.subject} 的考试结果`"
        >
          <div class="card-content-wrapper">
            <div class="card-info-main">
              <h3 class="result-title">{{result.paperName  }}</h3>
              <div class="result-meta">
                <span><i class="icon-book"></i> {{ result.course }}</span>
                <span><i class="icon-calendar-alt"></i> {{ formatDate(result.openTime) }}</span>
              </div>
            </div>
            <div class="card-score-section">
              <div class="score-value" :class="getScoreBadgeClass(result.score)">{{ result.totalScore }}</div>
              <div class="score-unit">分</div>
            </div>
          </div>
          <div class="card-action-indicator">
            查看详情 <i class="icon-arrow-right"></i>
          </div>
        </div>
      </div>

      <div v-else class="no-results">
        <p>没有找到相关的考试记录。</p>
        <p v-if="searchQuery || selectedDateFilter" class="sub-text">请尝试调整搜索或筛选条件。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'; // Added onMounted
import { useRouter } from 'vue-router';

const router = useRouter();
const isLoading = ref(false); // Initialize isLoading, set true before fetch, false after
const studentId=ref(1)
const url_front = 'http://localhost:8080/';
// 模拟考试结果数据，实际应从API获取
const examResults = ref([{
  paperId:1,
  courseId:1,
  studentId:1,
  paperName:"",
  totalScore:100,
  closeTime: new Date(),
   openTime: new Date(),
   date:new Date(),//此处是我写的，用openTime来初始化的，你得稍微改改可能
}]); // Initialize as empty

const fetchexamsdata = async() => { // Function to populate mock data
  try {
    const params = new URLSearchParams({ studentId:studentId.value });
    const url = url_front+`api/exam/search-examResult-for-one-student?${params}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`获取考试结果失败 (${res.status})`);
    const temp = await res.json() || [];
    examResults.value=temp.map((result) => {
      return {
        ...result,
        paperName:result.paperName? result.paperName:"未命名",
        date:result.openTime,
      }
})
  console.log(examResults.value)
  } catch (error) {
    console.error('fetchExamResultsForChart error:', error);
    examResults.value = [];
  }
};

onMounted(() => {
  // In a real app, you would fetch data here:
  // isLoading.value = true;
  // fetchExamResultsAPI().then(data => {
  //   examResults.value = data.sort((a, b) => new Date(b.date) - new Date(a.date));
  //   isLoading.value = false;
  // }).catch(err => {
  //   console.error(err);
  //   isLoading.value = false;
  //   alert("加载考试记录失败");
  // });
  fetchexamsdata(); // Using mock data for now
});


const searchQuery = ref('');
const selectedDateFilter = ref('');

const filteredResults = computed(() => {
  let results = [...examResults.value];
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase().trim();
    results = results.filter(result =>
        result.subject.toLowerCase().includes(query) ||
        result.course.toLowerCase().includes(query) ||
        formatDate(result.date).toLowerCase().includes(query)
    );
  }
  if (selectedDateFilter.value) {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()); // Start of today

    switch (selectedDateFilter.value) {
      case 'today':
        results = results.filter(result => {
          const date = new Date(result.date);
          return date >= today && date < new Date(today.getTime() + 24 * 60 * 60 * 1000);
        });
        break;
      case 'thisWeek':
        // Assuming week starts on Monday
        const dayOfWeek = today.getDay(); // 0 (Sun) - 6 (Sat)
        const diffToMonday = today.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1); // Adjust to Monday
        const startOfWeek = new Date(today.setDate(diffToMonday));
        startOfWeek.setHours(0,0,0,0);

        const endOfWeek = new Date(startOfWeek);
        endOfWeek.setDate(startOfWeek.getDate() + 6);
        endOfWeek.setHours(23,59,59,999);

        results = results.filter(result => {
          const date = new Date(result.date);
          return date >= startOfWeek && date <= endOfWeek;
        });
        break;
      case 'thisMonth':
        results = results.filter(result => {
          const date = new Date(result.date);
          return date.getMonth() === now.getMonth() &&
              date.getFullYear() === now.getFullYear();
        });
        break;
    }
  }
  return results;
});

const getScoreBadgeClass = (score) => { // Now returns text color class
  if (score === undefined || score === null) return 'score-unknown';
  if (score >= 90) return 'score-excellent';
  if (score >= 80) return 'score-good';
  if (score >= 60) return 'score-pass';
  return 'score-fail';
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '日期无效';
  // More concise date format
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};
const pad = (num) => String(num).padStart(2, '0');

const formatDuration = (seconds) => {
  if (seconds === undefined || seconds === null) return 'N/A';
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${minutes} 分钟 ${pad(remainingSeconds)} 秒`;
};

const clearSearch = () => {
  searchQuery.value = '';
  selectedDateFilter.value = '';
};

const viewDetails = (result) => {
  router.push(`/student/result/${result.courseId}/${result.paperId}/${result.studentId}/details`);
};
</script>

<style scoped>
/* --- 全局与页面布局 --- */
.exam-results-page {
  padding: 25px 35px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f0f3f7; /* Slightly different light background */
  min-height: 100vh;
  color: #343a40; /* Darker base text color */
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 35px;
  font-size: 2.5em; /* Slightly larger */
  font-weight: 700; /* Bolder */
  letter-spacing: -0.5px;
}

/* --- 控制区域容器 --- */
.controls-container.search-filter-panel { /* Renamed from search-filter */
  background-color: #fff;
  padding: 20px 25px;
  border-radius: 12px; /* Softer radius */
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06); /* Softer, more diffused shadow */
  margin-bottom: 30px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.search-group {
  flex: 2; /* Give more space to search */
  min-width: 280px;
  display: flex;
  align-items: center; /* Align icon, input, button */
  position: relative; /* For icon positioning */
}
.search-group .input-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #adb5bd;
  font-size: 1.1em;
  pointer-events: none; /* So it doesn't interfere with input click */
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
  transition: border-color .15s ease-in-out, box-shadow .15s ease-in-out;
  box-sizing: border-box;
}
.form-control.with-icon { /* Input/Select with icon */
  padding-left: 45px; /* Space for the icon */
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

.filter-group {
  display: flex;
  align-items: center;
  flex: 1; /* Allow filter to take space */
  min-width: 200px; /* Min width for filter */
  position: relative; /* For icon */
}
.filter-group .form-label.visually-hidden { /* Hide label visually but keep for screen readers */
  position: absolute;
  width: 1px;
  height: 1px;
  margin: -1px;
  padding: 0;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
}
.filter-group .input-icon { /* Icon for select */
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #adb5bd;
  font-size: 1em;
  pointer-events: none;
}


select.form-control.date-filter {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m2 5 6 6 6-6'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 0.9rem center;
  background-size: 16px 12px;
  cursor: pointer;
  padding-right: 30px; /* Ensure space for custom arrow */
}


/* --- 按钮通用样式 --- */
.btn {
  padding: 12px 20px; border: none; border-radius: 8px; cursor: pointer;
  font-size: 1em; font-weight: 500; transition: all 0.2s ease-in-out;
  display: inline-flex; align-items: center; justify-content: center;
  gap: 8px; text-decoration: none; line-height: 1.5;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
}
.btn:hover:not([disabled]) { opacity: 0.9; transform: translateY(-1px); box-shadow: 0 4px 8px rgba(0,0,0,0.12); }
.btn:active:not([disabled]) { transform: translateY(0); box-shadow: 0 2px 4px rgba(0,0,0,0.08); }
.btn i { font-size: 1.1em; }

.secondary-btn { background-color: #6c757d; color: white; }
.secondary-btn:hover:not([disabled]) { background-color: #5a6268; }

.clear-btn.btn {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  white-space: nowrap;
  flex-shrink: 0;
  background-color: #f8f9fa; /* Lighter clear button */
  color: #495057;
  border: 1px solid #ced4da;
  border-left: none; /* Remove left border as it's connected to input */
  box-shadow: none;
}
.clear-btn.btn:hover {
  background-color: #e9ecef;
  color: #2c3e50;
  opacity: 1;
}

/* --- Results List & Cards --- */
.content-section {
  margin-top: 30px; /* Increased space */
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(330px, 1fr)); /* Slightly larger min card width */
  gap: 25px;
}

.result-card {
  background: white;
  border-radius: 12px; /* Softer radius */
  box-shadow: 0 6px 20px rgba(100, 110, 130, 0.08); /* More refined shadow */
  cursor: pointer;
  transition: transform 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94), box-shadow 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  display: flex;
  flex-direction: column;
  overflow: hidden; /* For action indicator positioning */
  position: relative; /* For action indicator */
}
.result-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 10px 30px rgba(100, 110, 130, 0.12);
}
.result-card:focus-visible {
  outline: 2px solid #007bff;
  outline-offset: 3px; /* More visible offset */
}

.card-content-wrapper { /* New wrapper for main content + score */
  display: flex;
  padding: 20px 25px; /* Main padding here */
  gap: 15px;
  flex-grow: 1;
}
.card-info-main {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.result-title {
  margin: 0 0 10px 0; /* Space below title */
  color: #2c3e50;
  font-size: 1.25em; /* Slightly larger */
  font-weight: 600;
  line-height: 1.3;
}

.card-score-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-left: 15px;
  border-left: 1px solid #e9ecef;
  margin-left: 15px;
  flex-shrink: 0;
  text-align: center;
}
.score-value {
  font-size: 2.2em; /* Prominent score */
  font-weight: 700;
  line-height: 1;
}
.score-unit {
  font-size: 0.9em;
  color: #6c757d;
  margin-top: 2px;
}
/* Score color classes */
.score-excellent { color: #28a745; } /* Green */
.score-good { color: #17a2b8; }      /* Teal */
.score-pass { color: #ffc107; }      /* Yellow */
.score-fail { color: #dc3545; }      /* Red */
.score-unknown { color: #6c757d; }   /* Gray */


.result-meta { /* Renamed from .result-info */
  margin-top: auto; /* Push meta info down if title is short */
  font-size: 0.9em;
  color: #555;
  padding-top: 10px; /* Space above meta if title is long */
}
.result-meta p {
  margin: 6px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6c757d; /* Consistent color for meta text */
}
.result-meta i {
  font-size: 1em; /* Ensure icons are not too large */
  color: #868e96; /* Softer icon color */
  width: 16px; /* For alignment */
  text-align: center;
}
.result-meta .info-label { /* Removed, let icons provide context */
  /* font-weight: 500; */
  /* color: #495057; */
}

.card-action-indicator {
  background-color: #007bff;
  color: white;
  text-align: center;
  padding: 10px 0;
  font-size: 0.9em;
  font-weight: 500;
  transition: background-color 0.2s ease;
  border-top: 1px solid #0069d9; /* Separator */
}
.result-card:hover .card-action-indicator {
  background-color: #0069d9;
}
.card-action-indicator i {
  margin-left: 5px;
  transition: transform 0.2s ease;
}
.result-card:hover .card-action-indicator i {
  transform: translateX(3px);
}

/* --- Empty State --- */
.loading-indicator,
.no-results {
  text-align: center; padding: 50px 20px; /* Increased padding */
  margin-top: 20px; background-color: #fff; /* White background for empty state in content area */
  border-radius: 10px; border: 1px solid #e9ecef; /* Softer border */
  font-size: 1.1em; color: #6c757d; /* Muted text */
  box-shadow: 0 4px 12px rgba(0,0,0,0.05); /* Subtle shadow */
}
.loading-indicator p, .no-results p { margin: 0 0 10px 0; }
.no-results .sub-text { font-size: 0.95em; color: #868e96;}

/* --- Responsive --- */
@media (max-width: 768px) {
  .exam-results-page { padding: 20px 15px; }
  h1 { font-size: 2em; margin-bottom: 25px; }
  .controls-container.search-filter-panel { flex-direction: column; align-items: stretch; gap: 15px; padding: 15px; }
  .search-group { flex-direction: column; }
  .search-input.form-control { border-radius: 8px; border-bottom-left-radius: 0; border-bottom-right-radius: 0; border-right: 1px solid #ced4da;}
  .clear-btn.btn { border-radius: 0 0 8px 8px; width: 100%; border-left: 1px solid #ced4da; }

  .filter-group { flex-direction: column; align-items: stretch; }
  .filter-group .form-label.visually-hidden + .input-icon { display: none; } /* Hide icon if label is hidden and stacking */
  .filter-group .form-control.with-icon { padding-left: 18px; } /* Reset padding if icon is hidden */
  .filter-group select.form-control.date-filter { width: 100%; }


  .results-grid { grid-template-columns: 1fr; gap: 18px; }
  .result-card { padding: 0; /* Padding now handled by card-content-wrapper */ }
  .card-content-wrapper { flex-direction: column; align-items: flex-start; gap: 10px; padding: 18px 20px;}
  .card-score-section {
    border-left: none;
    margin-left: 0;
    padding-left: 0;
    padding-top: 15px;
    border-top: 1px solid #e9ecef;
    width: 100%;
    align-items: flex-start; /* Align score to left on mobile */
    flex-direction: row; /* Score and unit on same line */
    justify-content: flex-start;
    gap: 5px;
  }
  .score-value { font-size: 1.8em; }
  .score-unit { align-self: flex-end; margin-bottom: 3px; }


  .result-title { font-size: 1.15em; }
  .result-meta { font-size: 0.85em; }
}

.icon-search::before { content: "🔍"; }
.icon-clear::before { content: "✗"; font-weight: bold;}
.icon-arrow-right::before { content: "→"; }
</style>
<!--.icon-calendar::before { content: "📅"; }-->
<!--.icon-book::before { content: "📚"; }-->
<!--.icon-clock::before { content: "⏱️"; }-->
<!--.icon-timer::before { content: "⏱️"; } /* Alias for clock */-->
<!--.icon-calendar-alt::before { content: "🗓️"; } /* Alias for calendar */-->