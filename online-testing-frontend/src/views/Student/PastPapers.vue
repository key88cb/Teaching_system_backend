<template>
  <div class="past-papers-page">
    <h1>历年真题试卷</h1>

    <div class="controls-container">
      <div class="search-bar">
        <input
            v-model="searchQuery"
            placeholder="输入科目或关键词搜索试卷"
            class="form-control search-input"
        />
        <button @click="clearSearch" class="btn secondary-btn clear-btn">
          <i class="icon-clear"></i> 清除
        </button>
      </div>

      <div class="filter-options">
        <select v-model="selectedSubject" class="form-control subject-filter">
          <option value="">全部科目</option>
          <option v-for="subject in uniqueSubjects" :key="subject" :value="subject">{{ subject }}</option>
        </select>

        <select v-model="selectedYear" class="form-control year-filter"> {/* Added .form-control */}
          <option value="">全部年份</option>
          <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}</option>
        </select>
      </div>
    </div>

    <div class="papers-grid">
      <div v-if="isLoading" class="loading-indicator">
        <p>正在加载试卷...</p>
      </div>
      <div
          v-else-if="filteredPapers.length > 0"
          v-for="paper in filteredPapers"
          :key="paper.paperId"
          class="paper-card"
          @click="viewPaperDetails(paper)"
          tabindex="0"
          role="button"
          :aria-label="`查看试卷详情: ${paper.title}`"
      >
        <div class="card-header">
          <h3 class="paper-title">{{ paper.title }}</h3>
          <span class="paper-year-badge">{{ paper.year }}</span>
        </div>
        <div class="paper-info">
          <p><span class="info-label">科目：</span>hbgg抱歉没有这一项呢</p>
          <p><span class="info-label">总题数：</span>{{ paper.multipleChoiceNum+paper.singleChoiceNum+paper.trueFalseNum }} 题</p>
          <p><span class="info-label">总分：</span>{{ paper.totalScores }} 分</p>
        </div>
        <div class="card-footer">
          <span class="view-details-link">查看详情 &rarr;</span>
        </div>
      </div>

      <div v-if="!isLoading && filteredPapers.length === 0" class="no-results">
        <p>没有找到符合条件的历年试卷。</p>
        <p class="sub-text">请尝试调整搜索关键词或筛选条件。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'; // Added onMounted if loading state is used
import { useRouter } from 'vue-router';

const router = useRouter();
const isLoading = ref(false); // Optional for loading state
const url_front = 'http://localhost:8080/';

const pastPapers = ref([
  { id: 1, title: '2023年操作系统原理期末考试', subject: '操作系统原理', year: 2023, totalQuestions: 20, fullScore: 100 },
  { id: 2, title: '2023年需求分析与设计期中考试', subject: '需求分析与设计', year: 2023, totalQuestions: 15, fullScore: 75 },
  { id: 3, title: '2022年数据库基础期末考试', subject: '数据库基础', year: 2022, totalQuestions: 25, fullScore: 100 },
  { id: 4, title: '2022年操作系统原理期中考试', subject: '操作系统原理', year: 2022, totalQuestions: 18, fullScore: 90 },
  { id: 5, title: '2024年计算机网络模拟测试A卷', subject: '计算机网络', year: 2024, totalQuestions: 30, fullScore: 100 },
  { id: 6, title: '2024年数据结构期末考试', subject: '数据结构与算法', year: 2024, totalQuestions: 22, fullScore: 100 },
]);

const searchQuery = ref('');
const selectedSubject = ref('');
const selectedYear = ref('');

const uniqueSubjects = computed(() => {
  return [...new Set(pastPapers.value.map(paper => paper.subject))].sort();
});

const yearOptions = computed(() => {
  const years = new Set(pastPapers.value.map(paper => paper.year));
  return Array.from(years).sort((a, b) => b - a);
});

const filteredPapers = computed(() => {
  let results = [...pastPapers.value];
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase().trim();
    results = results.filter(paper =>
        paper.title.toLowerCase().includes(query) ||
        paper.subject.toLowerCase().includes(query)
    );
  }
  if (selectedSubject.value) {
    results = results.filter(paper => paper.subject === selectedSubject.value);
  }
  if (selectedYear.value) {
    results = results.filter(paper => paper.year.toString() === selectedYear.value.toString());
  }
  return results;
});

const clearSearch = () => {
  searchQuery.value = '';
  selectedSubject.value = '';
  selectedYear.value = '';
};

const viewPaperDetails = (paper) => {
  // 实际应用中，你可能需要传递paperId或其他唯一标识符
  // router.push({ name: 'PastPaperDetails', params: { id: paper.id }, state: { paperData: paper } });
  // console.log('paper',paper)
  router.push(`/student/past-paper/${paper.courseId}/${paper.paperId}/details`); // Assuming this route exists
  // console.log('Viewing paper:', paper.title);
};

// Optional: Simulate API call for initial data
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
    const url = url_front+`api/paper-questions/query-all-past-papers`; // Removed params for now if they are for client filtering

    const res = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    });

    if (!res.ok) {
      const errorData = await res.json().catch(() => null);
      throw new Error(errorData?.message || `网络错误 ${res.status}`);
    }
    const data = await res.json();
    console.log('fetchPaperInfos:', data);
    pastPapers.value = data.map(p => ({
      ...p,
      paperName: p.paperName || "未命名试卷", // Provide default if paperName is null
      openTime: p.openTime,
      closeTime: p.closeTime,
      year:(new Date(p.closeTime)).getFullYear()
    }));
    console.log('已拉取试卷数据:', pastPapers.value.length);
    console.log( '已拉取试卷数据:', pastPapers.value)
  } catch (error) {
    alert('加载考试列表失败: ' + error.message);
    console.error("fetchpastPapers error:", error);
    pastPapers.value = [];
  } finally {
    isLoading.value = false;
  }
};


</script>

<style scoped>
/* --- 全局与页面布局 --- */
.past-papers-page {
  padding: 25px 35px; /* 增加内边距 */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f8f9fa; /* 淡雅背景色 */
  min-height: 100vh;
  color: #333;
}

h1 {
  color: #2c3e50; /* 深蓝灰色标题 */
  text-align: center;
  margin-bottom: 35px; /* 增加底部间距 */
  font-size: 2.4em; /* 稍大字号 */
  font-weight: 600;
}

/* --- 控制区域容器 --- */
.controls-container {
  background-color: #fff;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
  margin-bottom: 30px;
  display: flex;
  flex-direction: column; /* 内部元素垂直排列 */
  gap: 20px; /* 各控制组之间的间距 */
}

.search-bar {
  display: flex;
  width: 100%; /* 搜索栏占满宽度 */
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
  border-color: #86b7fe; /* 柔和的蓝色焦点 */
  outline: 0;
  box-shadow: 0 0 0 0.25rem rgba(13,110,253,.25);
}
select.form-control {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m2 5 6 6 6-6'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 0.9rem center; /* 调整箭头位置 */
  background-size: 16px 12px;
}

.search-input { /* 特定于搜索框的样式调整 */
  flex-grow: 1; /* 占据可用空间 */
  border-top-right-radius: 0; /* 与清除按钮无缝连接 */
  border-bottom-right-radius: 0;
  border-right: none; /* 移除右边框，由按钮提供 */
}
.search-input:focus { /* 确保焦点时右侧没有额外边框 */
  z-index: 1; /* 避免被按钮覆盖阴影 */
}

/* --- 按钮通用样式 --- */
.btn {
  padding: 12px 20px; border: none; border-radius: 8px; cursor: pointer;
  font-size: 1em; font-weight: 500; transition: all 0.2s ease-in-out;
  display: inline-flex; align-items: center; justify-content: center;
  gap: 8px; text-decoration: none; line-height: 1.5;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
}
.btn:hover { opacity: 0.85; transform: translateY(-1px); box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
.btn:active { transform: translateY(0); box-shadow: 0 2px 4px rgba(0,0,0,0.08); }
.btn i[class^="icon-"] { font-size: 1.1em; } /* 假设的图标类 */

.secondary-btn { background-color: #6c757d; color: white; }
.secondary-btn:hover { background-color: #5a6268; }

.clear-btn {
  /* background-color: #6c757d; color: white; */ /* 使用 secondary-btn 类代替 */
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  padding: 12px 20px; /* 与输入框高度匹配 */
  white-space: nowrap; /* 防止文字换行 */
}

.filter-options {
  display: flex;
  flex-wrap: wrap; /* 允许换行 */
  gap: 20px; /* 筛选框之间的间距 */
  width: 100%;
}

.subject-filter,
.year-filter {
  flex: 1; /* 平分空间 */
  min-width: 200px; /* 最小宽度，防止过度压缩 */
}

/* --- 试卷卡片网格 --- */
.papers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); /* 调整卡片最小宽度 */
  gap: 25px; /* 卡片间距 */
}

.paper-card {
  background: white;
  padding: 22px 28px; /* 调整内边距 */
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.07); /* 更柔和的阴影 */
  cursor: pointer;
  transition: transform 0.25s ease-out, box-shadow 0.25s ease-out; /* 平滑过渡 */
  display: flex;
  flex-direction: column; /* 使页脚能固定在底部 */
  border-left: 5px solid #007bff; /* 主题色强调 */
}
.paper-card:hover {
  transform: translateY(-6px); /* 更明显的悬停效果 */
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}
.paper-card:focus-visible { /* 键盘焦点样式 */
  outline: 2px solid #007bff;
  outline-offset: 2px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}


.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start; /* 标题和年份徽章顶部对齐 */
  margin-bottom: 12px;
}
.paper-title { /* 原 h3 */
  color: #2c3e50; /* 更深的标题颜色 */
  font-size: 1.25em; /* 调整字号 */
  font-weight: 600; /* 加粗 */
  margin: 0; /* 移除默认margin */
  line-height: 1.4;
  flex-grow: 1; /* 使标题占据可用空间 */
}
.paper-year-badge {
  background-color: #6c757d; /* 徽章背景色 */
  color: white;
  padding: 4px 10px;
  border-radius: 15px; /* 胶囊形状 */
  font-size: 0.8em;
  font-weight: 500;
  white-space: nowrap;
  margin-left: 10px; /* 与标题间距 */
}

.paper-info {
  margin-top: 8px; /* 调整与标题间距 */
  font-size: 0.9em; /* 稍小字号 */
  color: #555;
  line-height: 1.6; /* 增加行高 */
  flex-grow: 1; /* 占据中间空间，把页脚推到底部 */
}
.paper-info p {
  margin: 6px 0; /* 调整段落间距 */
  display: flex; /* 用于对齐标签和值 */
}
.paper-info .info-label {
  font-weight: 500;
  color: #333;
  min-width: 60px; /* 确保标签对齐 */
  display: inline-block;
}

.card-footer {
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}
.view-details-link {
  color: #007bff;
  font-weight: 500;
  font-size: 0.9em;
  text-decoration: none;
}
.view-details-link:hover {
  text-decoration: underline;
  color: #0056b3;
}


/* --- 空状态与加载状态 --- */
.loading-indicator,
.no-results {
  grid-column: 1 / -1; /* 如果在grid中，使其横跨所有列 */
  text-align: center;
  padding: 50px 20px;
  margin-top: 20px;
  background-color: #f0f3f5; /* 更柔和的背景 */
  border-radius: 8px;
  border: 1px dashed #d0d9e0; /* 虚线边框 */
}
.no-results p {
  margin: 0 0 8px 0;
  font-size: 1.15em;
  color: #5a6268; /* 调整颜色 */
}
.no-results .sub-text {
  font-size: 0.95em;
  color: #868e96;
}


/* --- 响应式调整 --- */
@media (max-width: 768px) {
  .past-papers-page { padding: 20px; }
  h1 { font-size: 2em; margin-bottom: 25px; }
  .controls-container { padding: 20px; flex-direction: column; }
  .search-bar { flex-direction: column; gap: 10px; }
  .search-input { border-radius: 8px; border-right: 1px solid #ced4da; } /* 恢复圆角 */
  .clear-btn { border-radius: 8px; width: 100%; } /* 恢复圆角并全宽 */

  .filter-options { flex-direction: column; gap: 15px; }
  .subject-filter, .year-filter { min-width: 100%; }

  .papers-grid { grid-template-columns: 1fr; gap: 18px; } /* 单列显示 */
  .paper-card { padding: 18px 22px; }
  .paper-title { font-size: 1.15em; }
  .paper-info { font-size: 0.85em; }
}

/* Placeholder icons (需要您替换为真实的图标字体或SVG) */
.icon-clear::before { content: "✗"; margin-right: 5px; font-weight: bold;}
</style>