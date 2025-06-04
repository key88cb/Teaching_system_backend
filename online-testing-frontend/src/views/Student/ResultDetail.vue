<template>
  <div class="student-exam-result-page"> 
    <div class="page-actions-bar">
      <button class="btn secondary-outline-btn back-btn" @click="goBack">
        <i class="icon-back-arrow"></i> 返回列表
      </button>
    </div>

    <div class="exam-header-info">
      <h1>{{ paperInfo?.paperName || '考试结果详情' }}</h1>
      <div class="exam-meta-info">
        <div class="meta-item"><strong>考试时间：</strong>{{ formatDate(paperInfo?.openTime) }} 至 {{ formatDate(paperInfo?.closeTime) }}</div>
        <div class="meta-item"><strong>您的得分：</strong><span class="student-total-score">{{ examresult?.totalScore ?? 'N/A' }}</span> / {{ paperInfo?.totalScores ?? 'N/A' }}</div>
      </div>
    </div>

    <div class="question-list-section"> 
      <h2 class="section-title">题目及作答详情</h2>
      <div v-if="isLoading" class="loading-indicator"><p>加载中...</p></div>
      <div v-else-if="processedRecords.length > 0" class="questions-container">
        <div
            v-for="(record, index) in processedRecords"
            :key="record.questionId"
            class="question-card"
        >
          <div class="question-header">
            <span class="type-badge" :class="`type-${(record.questionTypeOriginal || 'unknown').toLowerCase().replace(/[\s/]+/g, '_')}`">
              {{ getDisplayQuestionType(record.questionTypeOriginal) }}
            </span>
            <span class="question-text-title">{{ index + 1 }}. {{ record.questionText }}</span>
            <span class="question-score-display">
              得分: <span class="student-points" :class="record.getpoints === record.points ? 'full-mark' : (record.getpoints > 0 ? 'partial-mark' : 'no-mark')">{{ record.getpoints }}</span> / {{ record.points }}分
            </span>
          </div>
          <div class="options">
            <div
                v-for="(option) in record.displayOptions"
                :key="option.value"
                :class="['option-item', {
                  selected: option.studentSelected,
                  correct: option.isCorrect,
                  wrong: option.studentSelected && !option.isCorrect
                }]"
            >
              <span class="option-indicator">
                <i v-if="option.studentSelected" class="icon-radio-checked selected-icon"></i>
                <i v-else class="icon-radio-unchecked"></i>
              </span>
              <span class="option-letter">{{ option.value }}.</span>
              <span class="option-label">{{ option.label }}</span>
              <span v-if="option.isCorrect" class="feedback-badge correct-answer-badge">
                <i class="icon-check-simple"></i> 正确答案
              </span>
            </div>
            <div v-if="!record.displayOptions || record.displayOptions.length === 0" class="no-options-info">
              该题目选项信息无法加载。
            </div>
          </div>
          <div class="answer-feedback-summary">
            <p><strong>正确答案：</strong><span class="correct-answer-text">{{ record.correctAnswer }}</span></p>
            <p><strong>您的答案：</strong><span :class="record.getpoints === record.points ? 'student-answer-correct' : (record.studentAnswer ? 'student-answer-wrong' : 'student-answer-none')">{{ record.studentAnswer || '未作答' }}</span></p>
            <div v-if="record.questionTypeOriginal === 'Multiple Choice' || record.questionTypeOriginal === '多选题'" class="multi-choice-feedback">
              <template v-for="option in record.displayOptions" :key="`feedback-${option.value}`">
                    <span v-if="option.isCorrect && !option.studentSelected" class="option-feedback missed-correct">
                        选项 {{option.value}} (正确) 您未选
                    </span>
                <span v-if="!option.isCorrect && option.studentSelected" class="option-feedback chosen-wrong">
                        选项 {{option.value}} (错误) 您选择了
                    </span>
              </template>
            </div>
          </div>
        </div>
      </div>
      <div v-else-if="!isLoading" class="no-questions">
        <p>暂无题目信息或学生作答记录。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const isLoading = ref(true);
const paperInfo = ref(null);
const examresult = ref(null);
const records = ref(null); // Will store { studentId, ..., detailedResults: [] }
const url_front = 'http://localhost:8080/';

// const isedit = ref(route.query.mode === 'edit'); // From your original code if needed for "上传成绩"

// --- 题目类型中英文映射 ---
const QUESTION_TYPE_MAP_TO_CHINESE = {
  'Single Choice': '单选题',
  'Multiple Choice': '多选题',
  'True/False': '判断题',
  '单选题': '单选题',
  '多选题': '多选题',
  '判断题': '判断题',
  'unknown': '未知题型'
};
const getDisplayQuestionType = (backendType) => {
  if (!backendType) return QUESTION_TYPE_MAP_TO_CHINESE['unknown'];
  return QUESTION_TYPE_MAP_TO_CHINESE[backendType] || backendType;
};
// ---

onMounted(async () => {
  isLoading.value = true;
  const paperId = parseInt(route.params.paperId);
  const courseId = parseInt(route.params.courseId);
  const studentId = parseInt(route.params.studentId);

  if (isNaN(paperId) || isNaN(courseId) || isNaN(studentId)) {
    alert('页面参数无效！');
    router.push('/student/results'); // Or student dashboard
    isLoading.value = false;
    return;
  }

  try {
    await Promise.all([
      fetchPaperQuestions(paperId, courseId),
      fetchExamResult(paperId, courseId, studentId),
      fetchRecords(paperId, courseId, studentId)
    ]);
  } catch (e) {
    console.error("Error during onMounted data fetching:", e);
  } finally {
    isLoading.value = false;
  }
});

const fetchPaperQuestions = async (paperId, courseId) => {
  try {
    const params = new URLSearchParams({ courseId: String(courseId), paperId: String(paperId) });
    const url = url_front+`api/paper-questions/query-paper-and-questions?${params}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`获取试卷信息失败 (${res.status})`);
    const data = await res.json();
    paperInfo.value = {
      ...data,
      paperName: data.paperName || '考试详情', // Default title
      paperQuestions: Array.isArray(data.paperQuestions) ? data.paperQuestions : []
    };
  } catch (error) {
    console.error('fetchPaperQuestions error:', error);
    alert(`加载试卷信息失败: ${error.message}`);
    paperInfo.value = { paperName: '加载失败', paperQuestions: [], totalScores: 0, openTime: new Date().toISOString(), closeTime: new Date().toISOString() };
  }
};

const fetchExamResult = async (paperId, courseId, studentId) => {
  try {
    const params = new URLSearchParams({ courseId: String(courseId), paperId: String(paperId), studentId: String(studentId) });
    const url = url_front+`api/exam/search-examResult-for-one?${params}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`获取学生总分失败 (${res.status})`);
    examresult.value = await res.json();
  } catch (error) {
    console.error('fetchExamResult error:', error);
    alert(`加载学生总分信息失败: ${error.message}`);
    examresult.value = { totalScore: 'N/A' };
  }
};

const fetchRecords = async (paperId, courseId, studentId) => {
  try {
    const params = new URLSearchParams({ courseId: String(courseId), paperId: String(paperId), studentId: String(studentId) });
    const url = url_front+`api/exam/search-exam-for-one?${params}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`获取学生答题记录失败 (${res.status})`);
    const data = await res.json();
    records.value = {
      ...data,
      detailedResults: Array.isArray(data.detailedResults) ? data.detailedResults : []
    };
  } catch (error) {
    alert(`加载学生答题记录失败: ${error.message}`);
    console.error('fetchRecords error:', error);
    records.value = { detailedResults: [] };
  }
};

// --- MODIFIED: processedRecords (was EditedRecord) to build displayOptions ---
const processedRecords = computed(() => {
  if (!paperInfo.value?.paperQuestions || !records.value?.detailedResults) {
    return [];
  }

  return paperInfo.value.paperQuestions.map(question => {
    const studentResultForQuestion = records.value.detailedResults.find(r => r.questionId === question.questionId);
    const studentAnswerStr = typeof studentResultForQuestion?.studentAnswer === 'string' ? studentResultForQuestion.studentAnswer : '';
    const correctAnswerStr = typeof question.correctAnswer === 'string' ? question.correctAnswer : '';

    let displayOptions = [];
    const questionType = question.questionType; // e.g., "单选题", "判断题", or "Multiple Choice"
    const optionLetters = ['A', 'B', 'C', 'D', 'E', 'F'];

    // Prioritize question.options if it's an array of strings (from your mock)
    if (Array.isArray(question.options) && question.options.every(opt => typeof opt === 'string')) {
      let optionLabels = question.options;
      if (questionType === '判断题' || questionType === 'True/False') {
        const trueFalseDefaults = ['正确', '错误'];
        optionLabels = [
          optionLabels[0] !== undefined ? optionLabels[0] : trueFalseDefaults[0],
          optionLabels[1] !== undefined ? optionLabels[1] : trueFalseDefaults[1]
        ].slice(0,2);
      }
      displayOptions = optionLabels.slice(0, optionLetters.length).map((label, index) => {
        const letter = optionLetters[index];
        return {
          value: letter,
          label: label,
          isCorrect: correctAnswerStr.includes(letter),
          studentSelected: studentAnswerStr.includes(letter)
        };
      });
    }
    // Fallback to optionA, optionB etc. properties (from API data structure in other examples)
    else {
      if (questionType === '判断题' || questionType === 'True/False') {
        const trueFalseLabels = { A: '正确', B: '错误' };
        displayOptions = ['A', 'B'].map(letter => ({
          value: letter,
          label: question[`option${letter}`] || trueFalseLabels[letter],
          isCorrect: correctAnswerStr.includes(letter),
          studentSelected: studentAnswerStr.includes(letter)
        }));
      } else { // Single/Multiple Choice with optionA, optionB...
        displayOptions = optionLetters
            .map(letter => {
              const optionText = question[`option${letter}`];
              if (optionText !== undefined && optionText !== null && optionText !== '') {
                return {
                  value: letter,
                  label: optionText,
                  isCorrect: correctAnswerStr.includes(letter),
                  studentSelected: studentAnswerStr.includes(letter)
                };
              }
              return null;
            })
            .filter(Boolean);
      }
    }

    return {
      ...question,
      questionTypeOriginal: question.questionType,
      studentAnswer: studentAnswerStr,
      getpoints: studentResultForQuestion?.points ?? 0,
      displayOptions
    };
  });
});
// ---

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '日期无效';
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'});
};

const goBack = () => {
  // Determine the correct "back" destination.
  // If coming from teacher's exam list, go there. If from student exam result list, go there.
  // For now, assuming a teacher view context based on previous pages.
    router.push('/student/results'); // Default back for teacher
};

// Placeholder, as this page is for viewing, not editing scores directly without a modal
// const saveNewScore = async (newScore, courseId, paperId, questionId) => { /* ... */ };

// Placeholder for a function if needed, though the current "上传成绩" button logic is simple v-if
// const handleUploadScores = () => { alert("上传成绩功能待实现"); };

</script>

<style scoped>
/* --- 全局与页面布局 --- */
.student-exam-detail-page {
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

.exam-header-info {
  text-align: center;
  margin-bottom: 30px;
}
h1 { /* Page Title */
  color: #2c3e50;
  font-size: 2.2em;
  font-weight: 600;
  margin-top: 0;
  margin-bottom: 10px;
}
.exam-meta-info {
  color: #555;
  font-size: 1em;
}
.exam-meta-info .meta-item {
  display: inline-block;
  margin: 0 12px;
  white-space: nowrap;
}
.student-total-score {
  font-weight: bold;
  color: #007bff;
  font-size: 1.1em;
}

.section-title { /* For "考试题目及作答详情" */
  font-size: 1.6em;
  color: #007bff;
  margin-top: 10px; /* Reduced top margin */
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #007bff;
  display: inline-block;
}

/* Edit scores toggle button container */
.question-list-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px; /* Replaces h2 margin */
}
.edit-scores-toggle-btn { /* Was .edit-button > button */
  /* Uses .btn .primary-outline-btn */
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
.primary-outline-btn {
  background-color: transparent; color: #007bff; border: 1px solid #007bff;
}
.primary-outline-btn:hover:not(:disabled) { background-color: #007bff; color: white; }


/* --- 题目列表区域 --- */
.question-list-section { /* Was .question-list */
  background: white;
  padding: 25px 30px;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.07);
  margin-bottom: 30px;
}
.questions-container {
  margin-top: 10px;
}
.question-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: box-shadow 0.2s ease-in-out;
}
.question-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
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
.type-badge.type-单选题 { background-color: #17a2b8; }
.type-badge.type-多选题 { background-color: #fd7e14; }
.type-badge.type-判断题 { background-color: #20c997; }
.type-badge.type-unknown { background-color: #dc3545;}


.question-text-title { /* Was .question-number */
  flex-grow: 1;
  font-weight: 600;
  color: #343a40;
  font-size: 1.1em; /* Increased size */
  line-height: 1.5;
}
.question-score-display { /* Was .question-score & .question-score2 */
  font-weight: 600;
  color: #495057;
  font-size: 0.95em;
  white-space: nowrap;
  margin-left: 15px;
  background-color: #e9ecef;
  padding: 5px 10px;
  border-radius: 6px;
  flex-shrink: 0;
  align-self: center;
}
.question-score-display .student-points {
  font-size: 1.1em;
}
.student-points.full-mark { color: #28a745; } /* Green for full mark */
.student-points.partial-mark { color: #ffc107; } /* Yellow for partial */
.student-points.no-mark { color: #dc3545; } /* Red for no mark */


.options {
  margin-left: 10px;
  margin-top: 15px; /* Added margin top */
}
.option-item { /* Was .option */
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 10px 12px;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
  background-color: #fdfdff;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}
.option-indicator { /* For custom radio/checkbox look if not using native styled by browser */
  margin-right: 10px;
  font-size: 1.2em; /* Adjust icon size */
}
.option-indicator .icon-radio-checked { color: #007bff; }
.option-indicator .icon-radio-unchecked { color: #adb5bd; }


.option-letter {
  font-weight: 600;
  margin-right: 8px;
  color: #0056b3;
  min-width: 1.5em;
}
.option-label { /* Was .option-label div */
  flex-grow: 1;
  line-height: 1.4;
  font-size: 0.95em;
}
.option-feedback { /* For "未选择正确选项" / "选择错误选项" */
  margin-left: auto; /* Push to the right */
  font-size: 0.85em;
  padding: 3px 8px;
  border-radius: 4px;
  font-style: italic;
  white-space: nowrap;
}
.option-feedback.missed-correct {
  background-color: #fff3cd; /* Light yellow */
  color: #856404;
  border: 1px solid #ffeeba;
}
.option-feedback.chosen-wrong {
  background-color: #f8d7da; /* Light red */
  color: #721c24;
  border: 1px solid #f5c6cb;
}
.option-feedback.chosen-correct { /* If student chose correctly, less emphasis needed if .correct is also styled */
  /* color: #155724; */
}


/* Styling for option states */
.option-item.selected { /* Student's selected answer */
  border-color: #007bff;
  background-color: #e7f3ff;
}
.option-item.correct { /* The actual correct answer */
  /* Background for correct option if not selected by student */
  /* If selected AND correct, .selected style might take precedence or combine */
}
.option-item.correct .option-label,
.option-item.correct .option-letter {
  /* color: #155724; */ /* Green text if it's the correct option line */
  /* font-weight: 600; */ /* Make correct option text bold */
}
.option-item.wrong { /* Student's selected AND wrong answer */
  border-color: #dc3545;
  background-color: #fde;
}
.option-item.wrong .option-label, .option-item.wrong .option-letter {
  color: #721c24;
}
.correct-answer-badge { /* For explicitly marking the correct answer label */
  margin-left: 10px;
  font-size: 0.85em;
  padding: 3px 8px;
  border-radius: 4px;
  background-color: #d1e7dd; /* Light green */
  color: #0f5132; /* Dark green text */
  font-weight: 500;
}
.correct-answer-badge i { margin-right: 4px; }


.answer-feedback-summary {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #e0e0e0;
  font-size: 0.9em;
}
.answer-feedback-summary p {
  margin: 5px 0;
  color: #495057;
}
.answer-feedback-summary strong {
  color: #343a40;
}
.correct-answer-text {
  color: #28a745;
  font-weight: bold;
}
.student-answer-correct {
  color: #28a745;
  font-weight: bold;
}
.student-answer-wrong {
  color: #dc3545;
  font-weight: bold;
  text-decoration: line-through;
}
.student-answer-none {
  color: #6c757d;
  font-style: italic;
}
.multi-choice-feedback span {
  display: block;
  margin-top: 3px;
  font-size: 0.9em;
}


/* Inline Score Edit Panel - If showSettingsModal logic is re-enabled */
.edit-score-panel {
  margin-top: 15px;
  padding: 15px;
  background-color: #f0f3f5;
  border-radius: 6px;
  border: 1px solid #d6d8db;
}
.edit-score-panel label {
  display: block;
  margin-bottom: 8px;
  font-size: 0.9em;
  font-weight: 500;
  color: #495057;
}
.input-with-button {
  display: flex;
  align-items: center;
  gap: 10px;
}
.score-edit-input.form-control {
  flex-grow: 1;
  height: 38px;
  text-align: center;
}
.save-score-btn.btn {
  padding: 8px 15px;
  font-size: 0.9em;
  flex-shrink: 0;
}


.no-options-info, .no-questions, .loading-indicator {
  text-align: center; padding: 20px; color: #6c757d;
  background-color: #f8f9fa; border-radius: 8px;
  font-size: 1em; margin: 15px 0;
  border: 1px dashed #e0e0e0;
}
.loading-indicator.main-loader, .no-results.main-error {
  padding: 60px 20px; font-size: 1.2em;
}
.loading-indicator p, .no-results p, .no-questions p { margin: 0; }


/* --- 响应式调整 --- */
@media (max-width: 768px) {
  .student-exam-detail-page { padding: 20px 15px; }
  .page-actions-bar { flex-direction: column; gap: 10px; align-items: stretch; }
  .page-actions-bar .btn { width: 100%; }
  h1 { font-size: 1.8em; }
  .exam-meta-info { flex-direction: column; align-items: center; text-align: center; gap: 5px;}
  .exam-meta-info .meta-item { margin: 0 5px; }

  .section-header { flex-direction: column; align-items: stretch; gap: 10px;}
  .section-header .section-title { margin-bottom: 0; }
  .section-header .edit-scores-toggle-btn { width: 100%; }

  .question-list-section { padding: 20px; }
  .question-header { flex-direction: column; align-items: flex-start; gap: 8px; }
  .question-text-title { font-size: 1em; }
  .question-score-display { margin-left: 0; margin-top: 8px; font-size: 0.9em;}

  .options { margin-left: 0; }
  .option-item { flex-direction: column; align-items: flex-start; }
  .option-indicator { margin-bottom: 5px; }
  .option-feedback { margin-left: 0; margin-top: 5px; }
}

/* Placeholder Icons */
.icon-back-arrow::before { content: "←"; margin-right: 6px; font-weight: bold; }
.icon-student-list::before { content: "👥"; margin-right: 6px; }
.icon-upload::before { content: "↑"; margin-right: 6px;}
.icon-edit-pen::before { content: "✏️"; margin-right: 6px;}
.icon-cancel::before { content: "🚫"; margin-right: 6px;}
.icon-radio-checked::before { content: "◉"; /* Or a checkmark for checkbox */ color: #007bff;}
.icon-radio-unchecked::before { content: "◯"; color: #adb5bd;}
.icon-check-simple::before { content: "✓"; margin-right: 4px; }

</style>