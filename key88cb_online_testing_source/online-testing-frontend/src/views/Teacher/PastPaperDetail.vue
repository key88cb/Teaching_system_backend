<template>
  <div class="exam-detail-page">
    <div class="page-actions-bar">
      <button class="btn secondary-outline-btn back-btn" @click="goBack">
        <i class="icon-back-arrow"></i> 返回列表
      </button>
    </div>

    <h1>考试详情 - {{ paperInfo?.paperName || '加载中...' }}</h1>

    <div v-if="isLoading" class="loading-indicator main-loader">
      <p>正在加载考试详情，请稍候...</p>
    </div>
    <div v-else-if="!paperInfo" class="no-results main-error">
      <p>无法加载考试信息，或指定的考试不存在。</p>
      <p>请检查链接或返回列表重试。</p>
    </div>
    <template v-else>
      <div class="exam-info-card">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">课程：</span>
            <span class="info-value">{{ paperInfo.courseName || paperInfo.courseId || 'N/A' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建者：</span>
            <span class="info-value">{{ paperInfo.creator || 'N/A' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">试卷总分：</span>
            <span class="info-value">{{ paperInfo.totalScores !== undefined ? paperInfo.totalScores + ' 分' : 'N/A' }}</span>
          </div>
          <div class="info-item info-item-full-width time-info">
            <span class="info-label">开放时间：</span>
            <span class="info-value">{{ formatDate(paperInfo.openTime) }}</span>
          </div>
          <div class="info-item info-item-full-width time-info">
            <span class="info-label">结束时间：</span>
            <span class="info-value">{{ formatDate(paperInfo.closeTime) }}</span>
          </div>
        </div>
      </div>

      <div class="question-list-section">
        <h2 class="section-title">考试题目及作答分析</h2>
        <div v-if="processedQuestions.length > 0" class="questions-container">
          <div
              v-for="(question, index) in processedQuestions"
              :key="question.questionId"
              class="question-card"
          >
            <div class="question-header">
              <span class="type-badge" :class="`type-${(question.questionTypeOriginal || 'unknown').toLowerCase().replace(/[\s/]+/g, '_')}`">
                {{ getDisplayQuestionType(question.questionTypeOriginal) }}
              </span>
              <span class="question-text-title">{{ index + 1 }}. {{ question.questionText }}</span>
              <span class="question-score-info">
                (平均得分: <span class="avg-score">{{ question.avgScore }}</span> / {{ question.points }}分)
              </span>
            </div>
            <div class="options-analysis">
              <h4>选项分析：</h4>
              <div
                  v-for="(option, optionIndex) in question.displayOptions"
                  :key="option.value || optionIndex"
                  :class="['option-detail', { correct: option.isCorrect }]"
              >
                <div class="option-label-text">
                  <span class="option-letter">{{ option.value }}.</span>
                  <span class="option-text">{{ option.label }}</span>
                  <span v-if="option.isCorrect" class="correct-indicator">
                    <i class="icon-check-mark"></i> (正确答案)
                  </span>
                </div>
                <div class="option-stats">
                  <div class="progress-bar-container">
                    <div class="progress-bar" :style="{ width: calculateOptionPercentage(question, option.count) + '%' }"></div>
                  </div>
                  <span class="option-count">{{ option.count }}人 ({{ calculateOptionPercentage(question, option.count) }}%)</span>
                </div>
              </div>
              <div v-if="!question.displayOptions || question.displayOptions.length === 0" class="no-options-info">
                该题目选项信息暂无或不适用。
              </div>
            </div>
          </div>
        </div>
        <div v-else-if="!isLoading" class="no-questions">
          <p>暂无考试题目或题目分析数据。</p>
        </div>
      </div>

      <!-- <div class="charts-container-section">
        <h2 class="section-title">整体分数分布</h2>
        <div v-if="scoreDistributionData.length > 0 && !isLoading" class="chart-area">
          <div class="score-distribution-chart">
            <div
                v-for="range in scoreDistributionData"
                :key="range.label"
                class="distribution-bar-item"
            >
              <div class="bar-value-label">{{ range.count }}人 ({{ range.percentage }}%)</div>
              <div class="bar-graphic-container">
                <div
                    class="distribution-bar"
                    :style="{ height: range.percentage + '%', backgroundColor: range.color }"
                    :title="`${range.label}: ${range.count}人 (${range.percentage}%)`"
                ></div>
              </div>
              <div class="bar-category-label">{{ range.label }}</div>
            </div>
          </div>
        </div>
        <div v-else-if="!isLoading" class="no-results">
          <p>暂无足够数据生成分数分布图。</p>
        </div>
      </div> -->
    </template>
  </div>
</template>

<script setup>
// NO CHANGES TO SCRIPT LOGIC FROM YOUR LAST PROVIDED VERSION (Turn 29/31)
// The script from Turn 31 which correctly handles data fetching and processing for
// paperInfo, allResultsAnalysis, allExamResultsForChart, processedQuestions,
// and scoreDistributionData should be used here.
// Key functions: fetchPaperQuestions, fetchResultAnalysis, fetchExamResultsForChart,
// processedQuestions (computed), calculateOptionPercentage, scoreDistributionData (computed),
// formatDate, getDisplayQuestionType, etc.

// For brevity, I'm not repeating the entire script here,
// assume it's the same as the fully functional one from my previous comprehensive response (Turn 31/32).
// The following is just a placeholder to indicate where script from Turn 31/32 goes.
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const url_front = 'http://localhost:8080/';

const isLoading = ref(true);
const paperInfo = ref(null);
const allResultsAnalysis = ref(null);
const allExamResultsForChart = ref([]);

const QUESTION_TYPE_MAP_TO_CHINESE = {
  'Single Choice': '单选', '单选题': '单选',
  'Multiple Choice': '多选', '多选题': '多选',
  'True/False': '判断', '判断题': '判断',
  'unknown': '未知类型'
};
const getDisplayQuestionType = (backendType) => {
  if (!backendType) return QUESTION_TYPE_MAP_TO_CHINESE['unknown'];
  const mapped = QUESTION_TYPE_MAP_TO_CHINESE[backendType];
  return mapped || backendType;
};

const COURSE_ID_TO_NAME_MAP = {
  1: '示例课程 (ID:1)',
  201: '操作系统原理',
};
const getCourseNameById = (courseId) => {
  return COURSE_ID_TO_NAME_MAP[courseId] || `课程ID ${courseId}`;
};

const fetchPaperQuestions = async (paperId, courseId) => {
  try {
    const params = new URLSearchParams({ courseId: String(courseId), paperId: String(paperId) });
    const url = url_front+`api/paper-questions/query-paper-and-questions?${params}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`获取试卷信息失败 (${res.status})`);
    const data = await res.json();
    paperInfo.value = {
      ...data,
      paperName: data.paperName || '未命名试卷',
      courseName: getCourseNameById(data.courseId),
      paperQuestions: Array.isArray(data.paperQuestions) ? data.paperQuestions.map(q => ({...q, options: Array.isArray(q.options) ? q.options : Object.values(q.options || {}) })) : []
    };
  } catch (error) {
    console.error('fetchPaperQuestions error:', error);
    paperInfo.value = { paperName: '加载失败', paperQuestions: [] };
  }
};

const fetchResultAnalysis = async (paperId, courseId) => {
  try {
    const params = new URLSearchParams({ courseId: String(courseId), paperId: String(paperId) });
    const url = url_front+`api/exam/search-exam-for-all?${params}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`获取题目分析失败 (${res.status})`);
    const data = await res.json();
    allResultsAnalysis.value = {
      ...data,
      analyses: Array.isArray(data.analyses) ? data.analyses : []
    };
  } catch (error) {
    console.error('fetchResultAnalysis error:', error);
    allResultsAnalysis.value = { analyses: [] };
  }
};

const fetchExamResultsForChart = async (paperId, courseId) => {
  try {
    const params = new URLSearchParams({ courseId: String(courseId), paperId: String(paperId) });
    const url = url_front+`api/exam/search-examResult-for-all?${params}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`获取考试结果失败 (${res.status})`);
    allExamResultsForChart.value = await res.json() || [];
  } catch (error) {
    console.error('fetchExamResultsForChart error:', error);
    allExamResultsForChart.value = [];
  }
};

onMounted(async () => {
  isLoading.value = true;
  const paperIdParam = parseInt(route.params.paperId);
  const courseIdParam = parseInt(route.params.courseId);

  if (isNaN(paperIdParam) || isNaN(courseIdParam)) {
    alert('无效的考试ID或课程ID');
    router.push('/teacher/past-papers');
    isLoading.value = false;
    return;
  }
  try {
    await Promise.all([
      fetchPaperQuestions(paperIdParam, courseIdParam),
      fetchResultAnalysis(paperIdParam, courseIdParam),
      fetchExamResultsForChart(paperIdParam, courseIdParam)
    ]);
  } catch (e) {
    console.error("Error during onMounted Promise.all:", e);
  } finally {
    isLoading.value = false;
  }
});

const processedQuestions = computed(() => {
  if (!paperInfo.value?.paperQuestions || !Array.isArray(paperInfo.value.paperQuestions)) {
    return [];
  }
  const analysesData = allResultsAnalysis.value?.analyses || [];

  return paperInfo.value.paperQuestions.map(question => {
    const analysis = analysesData.find(item => item.questionId === question.questionId);
    const counts = analysis?.counts || [];
    let displayOptions = [];
    const questionType = question.questionType;
    const correctAnswerStr = typeof question.correctAnswer === 'string' ? question.correctAnswer : '';
    const optionLetters = ['A', 'B', 'C', 'D', 'E', 'F'];

    if (questionType === 'True/False' || questionType === '判断题') {
      const trueFalseLabels = { A: '正确', B: '错误' };
      const labelA = question.optionA !== undefined ? question.optionA : trueFalseLabels.A;
      const labelB = question.optionB !== undefined ? question.optionB : trueFalseLabels.B;
      displayOptions = [
        { value: 'A', label: labelA, isCorrect: correctAnswerStr.includes('A'), count: counts[0] ?? 0 },
        { value: 'B', label: labelB, isCorrect: correctAnswerStr.includes('B'), count: counts[1] ?? 0 }
      ];
    } else if (questionType === 'Single Choice' || questionType === 'Multiple Choice' ||
        questionType === '单选题' || questionType === '多选题') {
      displayOptions = optionLetters
          .map((letter, index) => {
            const optionText = question[`option${letter}`];
            if (optionText !== undefined && optionText !== null && optionText !== '') {
              return {
                value: letter,
                label: optionText,
                isCorrect: correctAnswerStr.includes(letter),
                count: counts[index] ?? 0,
              };
            }
            return null;
          })
          .filter(Boolean);
    }

    return {
      ...question,
      questionTypeOriginal: question.questionType,
      avgScore: analysis?.avgscore?.toFixed(2) ?? 'N/A',
      displayOptions: displayOptions,
      totalResponses: counts.reduce((sum, c) => sum + (c || 0), 0)
    };
  });
});

const calculateOptionPercentage = (question, optionCount) => {
  if (optionCount === undefined || optionCount === null || !question.totalResponses || question.totalResponses === 0) return 0;
  return parseFloat(((optionCount / question.totalResponses) * 100).toFixed(1));
};

// const scoreDistributionData = computed(() => {
//   if (!allExamResultsForChart.value || allExamResultsForChart.value.length === 0 || !paperInfo.value?.totalScores || paperInfo.value.totalScores === 0) {
//     return [];
//   }
//   const distributionRanges = {
//     '优秀 (≥90%)': { min: 90, max: Infinity, count: 0, color: '#28a745' },
//     '良好 (80-89%)':  { min: 80, max: 89.99, count: 0, color: '#17a2b8' },
//     '中等 (70-79%)':  { min: 70, max: 79.99, count: 0, color: '#ffc107' },
//     '及格 (60-69%)':  { min: 60, max: 69.99, count: 0, color: '#fd7e14' },
//     '不及格 (<60%)': { min: 0, max: 59.99, count: 0, color: '#dc3545' },
//   };
//   const totalStudents = allExamResultsForChart.value.length;
//   const paperFullScore = paperInfo.value.totalScores;

//   allExamResultsForChart.value.forEach(result => {
//     if (typeof result.totalScore !== 'number' || isNaN(result.totalScore)) return;
//     const percent = (result.totalScore / paperFullScore) * 100;
//     for (const key in distributionRanges) {
//       if (percent >= distributionRanges[key].min && percent <= distributionRanges[key].max) {
//         distributionRanges[key].count++;
//         break;
//       }
//     }
//   });

//   return Object.entries(distributionRanges).map(([label, data]) => ({
//     label,
//     count: data.count,
//     percentage: totalStudents ? parseFloat(((data.count / totalStudents) * 100).toFixed(1)) : 0,
//     color: data.color
//   }));
// });

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '日期无效';
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
};

const goBack = () => {
  router.push('/teacher/past-papers');
};
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
.page-actions-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}
.page-actions-bar .btn {
  font-size: 0.95em;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 20px;
  font-size: 2.2em;
  font-weight: 600;
}
.section-title {
  font-size: 1.6em;
  color: #007bff;
  margin-top: 30px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #007bff;
  display: inline-block;
}

/* --- 按钮通用样式 --- */
.btn {
  padding: 10px 20px; border: none; border-radius: 8px; cursor: pointer;
  font-size: 1em; font-weight: 500; transition: all 0.2s ease-in-out;
  display: inline-flex; align-items: center; justify-content: center;
  gap: 8px; text-decoration: none; line-height: 1.5;
  box-shadow: 0 2px 5px rgba(0,0,0,0.08);
}
.btn:hover:not(:disabled) { opacity: 0.85; transform: translateY(-1px); box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
.btn:active:not(:disabled) { transform: translateY(0); box-shadow: 0 2px 5px rgba(0,0,0,0.08); }
.btn:disabled { background-color: #adb5bd; color: #6c757d; cursor: not-allowed; box-shadow: none; opacity: 0.7;}
.btn i[class^="icon-"] { font-size: 1.1em; }

.primary-btn { background-color: #007bff; color: white; }
.primary-btn:hover:not(:disabled) { background-color: #0069d9; }
.secondary-outline-btn {
  background-color: transparent; color: #6c757d; border: 1px solid #6c757d;
}
.secondary-outline-btn:hover:not(:disabled) { background-color: #6c757d; color: white; }

/* --- 考试信息卡片 --- */
.exam-info-card {
  background: white;
  padding: 25px 30px;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.07);
  margin-bottom: 30px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 18px 30px;
}
.info-item {
  font-size: 1em;
  line-height: 1.6;
  padding: 5px 0;
}
.info-item.info-item-full-width {
  grid-column: 1 / -1;
}
.info-label {
  font-weight: 600;
  color: #495057;
  margin-right: 8px;
  display: inline-block;
}
.info-value {
  color: #212529;
}
.info-item.time-info .info-value {
  font-weight: 500;
}

/* --- 题目列表区域 --- */
.question-list-section, .charts-container-section {
  background: white;
  padding: 25px 30px;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.07);
  margin-bottom: 30px;
}
.questions-container {
  margin-top: 20px;
}
.question-card {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e9ecef;
  transition: box-shadow 0.2s ease-in-out;
}
.question-card:hover {
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
}
.question-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
  flex-wrap: nowrap;
  gap: 10px;
}
.type-badge {
  padding: 5px 12px; border-radius: 20px; font-weight: 500;
  color: white; font-size: 0.8em; text-transform: capitalize;
  background-color: #6c757d; white-space: nowrap;
  flex-shrink: 0;
  align-self: center;
}
.type-badge.type-multiple_choice { background-color: #fd7e14; }
.type-badge.type-single_choice { background-color: #17a2b8; }
.type-badge.type-true_false { background-color: #20c997; }
.type-badge.type-unknown { background-color: #dc3545;}


.question-text-title {
  flex-grow: 1;
  font-weight: 600;
  color: #343a40;
  font-size: 1.05em;
  line-height: 1.5;
}
.question-score-info {
  font-weight: 500;
  color: #495057;
  font-size: 0.9em;
  white-space: nowrap;
  margin-left: 15px;
  background-color: #e9ecef;
  padding: 4px 8px;
  border-radius: 4px;
  flex-shrink: 0;
  align-self: center;
}
.question-score-info .avg-score {
  font-weight: 700;
  color: #007bff;
}

.options-analysis {
  margin-top: 15px;
  padding-left: 10px;
}
.options-analysis h4 {
  font-size: 0.95em;
  color: #333;
  margin-top: 0;
  margin-bottom: 12px;
  font-weight: 600;
}
.option-detail {
  margin-bottom: 12px;
  font-size: 0.9em;
  color: #495057;
  padding: 10px; /* MODIFICATION: Added padding */
  border-radius: 6px;
  border: 1px solid #f0f0f0;
  background-color: #fff; /* MODIFICATION: Default background for all options */
}
.option-detail.correct {
  background-color: #d1e7dd; /* MODIFICATION: Highlight entire correct option block */
  border-color: #a3cfbb; /* MODIFICATION: Border for correct option */
}
.option-detail.correct .option-label-text,
.option-detail.correct .correct-indicator {
  color: #0f5132; /* Dark green for correct option text */
  font-weight: 600;
}
.option-detail.correct .progress-bar { /* Progress bar for correct option */
  background-color: #28a745 !important;
}


.option-label-text {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}
.option-letter {
  font-weight: 600;
  margin-right: 8px;
  color: #007bff;
  min-width: 1.5em;
}
.option-text {
  flex-grow: 1;
  line-height: 1.4;
}
.correct-indicator {
  margin-left: 10px;
  font-size: 0.9em;
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: 600;
  white-space: nowrap;
}
/* Icon styling within correct-indicator */
.correct-indicator i.icon-check-mark {
  margin-right: 4px;
  font-weight: bold;
  /* Color will be inherited from .option-detail.correct .correct-indicator */
}


.option-stats {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: calc(1.5em + 8px);
  padding: 3px 0;
}
.progress-bar-container {
  flex-grow: 1;
  height: 12px;
  background-color: #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
}
.progress-bar {
  height: 100%;
  background-color: #007bff;
  border-radius: 6px;
  transition: width 0.3s ease-in-out;
  min-width: 2px;
}
.option-count {
  font-size: 0.9em;
  color: #555;
  white-space: nowrap;
  min-width: 85px;
  text-align: right;
}
.no-options-info {
  font-style: italic;
  color: #888;
  margin-left: calc(1.5em + 8px);
  font-size: 0.9em;
}

/* --- Charts --- */
.chart-area { padding: 20px 0; }
.score-distribution-chart {
  display: flex;
  align-items: flex-end; /* MODIFICATION: Ensure bars start from bottom */
  justify-content: space-evenly;
  gap: 5px;
  height: 320px; /* This is the container for bar heights calculation */
  border: 1px solid #e0e0e0;
  padding: 25px 10px 10px 10px;
  background-color: #fdfdff;
  border-radius: 8px;
  overflow-x: auto;
  min-width: 100%;
  box-sizing: border-box;
}
.distribution-bar-item { /* MODIFICATION: Renamed from -wrapper */
  flex: 1;
  display: flex;
  flex-direction: column; /* Stack label, bar-container, label */
  align-items: center;
  text-align: center;
  min-width: 80px;
  max-width: 120px;
  height: 100%; /* MODIFICATION: Take full height of chart-area's cross axis */
  position: relative; /* For positioning labels if necessary */
}

.bar-value-label {
  font-size: 0.9em;
  color: #333;
  font-weight: 500;
  white-space: normal;
  word-break: break-word;
  min-height: 2.5em;
  display: flex;
  align-items: center;
  justify-content: center;
  order: 1; /* Top label */
  width: 100%;
}
.bar-graphic-container { /* MODIFICATION: New wrapper for the bar itself */
  flex-grow: 1; /* Takes up remaining space in the column */
  width: 100%;
  display: flex;
  flex-direction: column; /* To align bar at the bottom */
  justify-content: flex-end; /* Align bar at the bottom */
  align-items: center; /* Center bar horizontally */
  order: 2; /* Middle element */
}
.distribution-bar {
  width: 65%; /* Relative to bar-graphic-container's width */
  max-width: 60px; /* Max physical width */
  transition: height 0.5s ease-in-out, background-color 0.3s ease;
  border-radius: 5px 5px 0 0;
  box-shadow: inset 0 -2px 4px rgba(0,0,0,0.05);
  min-height: 3px;
  /* height is set by inline style: range.percentage + '%' */
  /* This percentage will now be of .bar-graphic-container's height */
}
.bar-category-label {
  margin-top: 8px; /* Space above category label */
  font-size: 0.85em;
  color: #555;
  white-space: nowrap;
  font-weight: 500;
  order: 3; /* Bottom label */
  width: 100%;
  height: 2.5em; /* Fixed height to help with alignment */
  display: flex;
  align-items: flex-start; /* Align text to top of its box */
  justify-content: center;
}


.loading-indicator.main-loader, .no-results.main-error {
  padding: 60px 20px;
  font-size: 1.2em;
}
.no-questions, .loading-indicator, .no-results {
  text-align: center; padding: 40px 20px; color: #6c757d;
  background-color: #f0f3f5; border-radius: 8px;
  font-size: 1.1em; margin: 20px 0;
  border: 1px dashed #d0d9e0;
}
.loading-indicator p, .no-results p, .no-questions p { margin: 0; }

/* --- 响应式调整 --- */
@media (max-width: 768px) {
  .exam-detail-page { padding: 20px 15px; }
  .page-actions-bar { flex-direction: column; gap: 10px; align-items: stretch; }
  .page-actions-bar .btn { width: 100%; }
  h1 { font-size: 1.8em; }
  .section-title { font-size: 1.3em; }
  .exam-info-card, .question-list-section, .charts-container-section { padding: 20px; }
  .info-grid { grid-template-columns: 1fr; gap: 12px; }
  .question-header { flex-direction: column; align-items: flex-start; gap: 8px; }
  .question-text-title { font-size: 1em; }
  .question-score-info { margin-left: 0; margin-top: 8px; font-size: 0.85em;}
  .options-analysis { margin-left: 0; padding-left: 0; border-left: none;}
  .option-stats { flex-direction: column; align-items: flex-start; margin-left: 10px; gap: 5px;}
  .progress-bar-container { max-width: 100%; width:100%;}
  .score-distribution-chart { height: auto; min-height:280px; padding: 20px 5px 10px 5px; flex-wrap: wrap; justify-content: center;}
  .distribution-bar-item { min-width: 70px; flex-basis: calc(33.33% - 10px); margin-bottom: 20px;}
  .bar-value-label, .bar-category-label { font-size: 0.8em; }
}
@media (max-width: 480px) {
  .distribution-bar-item { flex-basis: calc(50% - 10px); }
  .bar-value-label, .bar-category-label { font-size: 0.75em; }
}

/* Placeholder Icons */
.icon-back-arrow::before { content: "←"; margin-right: 6px; font-weight: bold; }
.icon-student-list::before { content: "👥"; margin-right: 6px; }
.icon-check-mark::before { content: "✔"; margin-right: 4px; /* color: #155724; */ /* Inherits from parent */ font-weight: bold; }
</style>