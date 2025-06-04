<template>
  <div class="student-exam-detail-page">
    <div class="page-actions-bar">
      <button class="btn secondary-outline-btn back-btn" @click="goBack">
        <i class="icon-back-arrow"></i> 返回
      </button>
      <button class="btn primary-btn upload-scores-btn" v-if="isedit" @click="handleUploadScores">
        <i class="icon-upload"></i> 上传成绩
      </button>
    </div>

    <div class="exam-header-info">
      <h1>考试详情 - {{ paperInfo?.paperName || '加载中...' }}</h1>
      <div class="exam-meta-info">
        <div class="meta-item"><strong>考试时间：</strong>{{ formatDate(paperInfo?.openTime) }} 至 {{ formatDate(paperInfo?.closeTime) }}</div>
        <div class="meta-item"><strong>试卷总分：</strong>{{ paperInfo?.totalScores }} / 学生得分：<span class="student-total-score">{{ examresult?.totalScore }}</span></div>
      </div>
    </div>

    <div class="question-list-section">
      <div class="section-header">
        <h2 class="section-title">题目列表及作答情况</h2>
        <button class="btn primary-outline-btn edit-scores-toggle-btn" @click="showSettingsModal = !showSettingsModal">
          <i :class="showSettingsModal ? 'icon-cancel' : 'icon-edit-pen'"></i>
          {{ showSettingsModal ? '取消批量修改分数' : '批量修改分数' }}
        </button>
      </div>

      <div v-if="isLoading" class="loading-indicator"><p>加载题目详情中...</p></div>
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
              得分: <span class="student-points">{{ record.getpoints }}</span> / {{ record.points }}分
            </span>
          </div>
          <div class="options">
            <div
                v-for="(option) in record.displayOptions"
                :key="option.value"
                :class="['option', {
                  selected: option.studentSelected,
                  correct: option.isCorrect,
                  wrong: option.studentSelected && !option.isCorrect
                }]"
            >
              <span class="option-letter">{{ option.value }}.</span>
              <span class="option-label">{{ option.label }}</span>
              <span v-if="option.isCorrect && !option.studentSelected && record.questionType === 'Multiple Choice'" class="option-feedback missed-correct">(正确，未选)</span>
              <span v-if="!option.isCorrect && option.studentSelected" class="option-feedback chosen-wrong">(选错)</span>
              <span v-if="option.isCorrect && option.studentSelected" class="option-feedback chosen-correct">(正确选择)</span>
            </div>
            <div v-if="!record.displayOptions || record.displayOptions.length === 0" class="no-options-info">
              该题目选项信息无法加载。
            </div>
          </div>

          <div v-if="showSettingsModal" class="edit-score-panel">
            <label :for="`score-input-${record.questionId}`">修改此题得分:</label>
            <div class="input-with-button">
              <input
                  type="number"
                  :id="`score-input-${record.questionId}`"
                  class="form-control score-edit-input"
                  v-model.number="record.editScore"
                  min="0"
                  :max="record.points"
              />
              <button class="btn primary-btn save-score-btn" @click="saveNewScore(record)">保存</button>
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
const url_front = 'http://localhost:8080/';

const isLoading = ref(true);
const showSettingsModal = ref(false); // Toggles visibility of ALL score edit inputs

const paperInfo = ref(null);
const examresult = ref(null);
const records = ref(null); // Will store { studentId, courseId, paperId, detailedResults: [] }

const isedit = computed(() => route.query.mode === 'edit'); // Check if in edit mode from query

// --- 题目类型中英文映射 ---
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
// ---

onMounted(async () => {
  isLoading.value = true;
  const paperId = parseInt(route.params.paperId);
  const courseId = parseInt(route.params.courseId);
  const studentId = parseInt(route.params.studentId);

  if (isNaN(paperId) || isNaN(courseId) || isNaN(studentId)) {
    alert('页面参数无效！');
    router.push('/teacher/exam-management'); // Or appropriate fallback
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
    // Alerts are handled within individual fetch functions
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
      paperName: data.paperName || '未命名试卷',
      paperQuestions: Array.isArray(data.paperQuestions) ? data.paperQuestions : []
    };
  } catch (error) {
    console.error('fetchPaperQuestions error:', error);
    alert(`加载试卷基本信息失败: ${error.message}`);
    paperInfo.value = { paperName: '加载失败', paperQuestions: [], totalScores:0, openTime: new Date().toISOString(), closeTime: new Date().toISOString() };
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

const processedRecords = computed(() => {
  if (!paperInfo.value?.paperQuestions || !records.value?.detailedResults) {
    return [];
  }
  return paperInfo.value.paperQuestions.map(question => {
    const studentResultForQuestion = records.value.detailedResults.find(r => r.questionId === question.questionId);
    const studentAnswerStr = typeof studentResultForQuestion?.studentAnswer === 'string' ? studentResultForQuestion.studentAnswer : '';
    const correctAnswerStr = typeof question.correctAnswer === 'string' ? question.correctAnswer : '';

    let displayOptions = [];
    const questionType = question.questionType;
    // The user's original data example for paperQuestions had 'options' as an array of strings.
    // The newer data example had optionA, optionB. Assuming optionA, B, C, D on question object.
    const optionLetters = ['A', 'B', 'C', 'D', 'E', 'F'];

    if (questionType === "判断题" || questionType === "True/False") {
      const trueFalseLabels = { A: '正确', B: '错误' };
      displayOptions = ['A', 'B'].map(letter => ({
        value: letter,
        label: question[`option${letter}`] || trueFalseLabels[letter], // Prefer optionA/B, fallback to fixed
        isCorrect: correctAnswerStr.includes(letter),
        studentSelected: studentAnswerStr.includes(letter)
      }));
    } else if (questionType === "单选题" || questionType === "多选题" ||
        questionType === "Single Choice" || questionType === "Multiple Choice") {
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

    return {
      ...question,
      questionTypeOriginal: question.questionType, // For CSS class
      studentAnswer: studentAnswerStr,
      getpoints: studentResultForQuestion?.points ?? 0,
      editScore: studentResultForQuestion?.points ?? 0, // Initialize editScore
      displayOptions
    };
  });
});

const saveNewScore = async (recordToSave) => { // Pass the whole record object
  const studentId = parseInt(route.params.studentId);
  if (recordToSave.editScore < 0 || recordToSave.editScore > recordToSave.points) {
    alert(`输入的分数 (${recordToSave.editScore}) 无效，应在 0 到 ${recordToSave.points} 之间。`);
    recordToSave.editScore = recordToSave.getpoints; // Revert to original score
    return;
  }

  console.log("修改分数:", recordToSave.editScore, recordToSave.courseId, recordToSave.paperId, studentId, recordToSave.questionId);
  try {
    const url = url_front+`api/exam/edit-score`;
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        courseId: recordToSave.courseId,
        paperId: recordToSave.paperId,
        studentId: studentId,
        questionId: recordToSave.questionId,
        score: recordToSave.editScore // Send the new score
      })
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: '网络响应错误' }));
      throw new Error(errorData.message || `HTTP error ${res.status}`);
    }
    console.log("修改分数成功");
    // After successful save, update the local data to reflect the change immediately
    // And also re-fetch to ensure consistency and get updated total score.
    const paperId = parseInt(route.params.paperId); // Re-fetch paperId and courseId
    const courseId = parseInt(route.params.courseId);
    await fetchRecords(paperId, courseId, studentId); // Re-fetch detailed records
    await fetchExamResult(paperId, courseId, studentId); // Re-fetch student's total score
    alert("分数修改成功！");
  } catch (error) {
    alert(`修改分数失败: ${error.message}`);
    console.error(error);
    // Optionally revert editScore on failure
    recordToSave.editScore = recordToSave.getpoints;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '日期无效';
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'});
};

const goBack = () => {
  // Based on user's original logic:
  // if (isedit.value) router.push('/teacher/endedexam-detail'); // This route seems specific and not general
  // else if (route.params.courseId && route.params.paperId) router.push(`/teacher/exams_detail/${route.params.courseId}/${route.params.paperId}`);
  // Fallback to a more general list:
  router.push('/teacher/exam-management');
};

const intoStudentScore = () => {
  // This button is on the student score page already. Maybe it's for a different action or was a copy-paste?
  // For now, let's assume it might be for printing or another action.
  alert("“查看学生答题情况”按钮功能待定或已在本页。");
};

// Placeholder for uploadScores logic if `isedit` is true
const handleUploadScores = () => {
  alert("“上传成绩”功能待实现。");
};

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

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 10px; /* Reduced margin */
  font-size: 2.2em;
  font-weight: 600;
}
.exam-meta-info {
  text-align: center;
  color: #555;
  font-size: 0.95em;
  margin-bottom: 30px;
}
.exam-meta-info .meta-item {
  display: inline-block;
  margin: 0 15px;
}
.student-total-score {
  font-weight: bold;
  color: #007bff;
  font-size: 1.1em;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-title { /* Was h2 for "考试题目" */
  font-size: 1.6em;
  color: #007bff;
  /* margin-top: 30px; */ /* Removed top margin as it's part of section-header */
  /* margin-bottom: 20px; */
  padding-bottom: 10px;
  border-bottom: 2px solid #007bff;
  display: inline-block;
}
.edit-scores-toggle-btn { /* Button to toggle all score edit inputs */
  font-size: 0.9em;
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
  margin-top: 10px; /* Reduced from 20px as section-header now exists */
}
.question-card {
  background: #fff; /* Changed from #f8f9fa */
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e0e0e0; /* Softer border */
  box-shadow: 0 2px 8px rgba(0,0,0,0.05); /* Softer shadow */
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
.type-badge.type-单选题 { background-color: #17a2b8; } /* Assuming question.questionType is Chinese */
.type-badge.type-多选题 { background-color: #fd7e14; }
.type-badge.type-判断题 { background-color: #20c997; }
.type-badge.type-unknown { background-color: #dc3545;}


.question-text-title {
  flex-grow: 1;
  font-weight: 600;
  color: #343a40;
  font-size: 1.05em;
  line-height: 1.5;
}
.question-score-display { /* Renamed from .question-score & .question-score2 */
  font-weight: 600; /* Make all of it bold */
  color: #495057;
  font-size: 0.95em; /* Slightly larger */
  white-space: nowrap;
  margin-left: 15px;
  background-color: #e9ecef;
  padding: 5px 10px; /* Increased padding */
  border-radius: 6px; /* Softer radius */
  flex-shrink: 0;
  align-self: center;
}
.question-score-display .student-points { /* Specifically style the student's obtained points */
  color: #007bff; /* Blue for obtained points */
  font-size: 1.1em; /* Make it slightly bigger */
}

.options { /* Container for all options */
  margin-left: 10px; /* Indent options block */
  margin-top: 10px;
}
.option {
  display: flex; /* Use flex for better alignment of letter, label, feedback */
  align-items: flex-start; /* Align items to the top */
  margin-bottom: 10px;
  padding: 10px 12px; /* Add padding to each option row */
  border-radius: 6px;
  border: 1px solid transparent; /* Base border */
  font-size: 0.95em;
}
.option-letter {
  font-weight: 600;
  margin-right: 8px;
  color: #007bff;
  min-width: 1.5em; /* Ensure 'A.' 'B.' align */
}
.option-label {
  flex-grow: 1;
  line-height: 1.4;
}
.option-feedback {
  margin-left: 10px;
  font-size: 0.85em;
  padding: 2px 6px;
  border-radius: 3px;
  white-space: nowrap;
  font-style: italic;
}

/* Styling for option states */
.option.selected {
  border-color: #007bff; /* Blue border for selected answer */
  background-color: #e7f3ff; /* Light blue background */
}
.option.correct {
  /* Correct answer, whether selected or not, gets this base style */
  /* If also selected, .selected style might override background */
}
.option.correct .option-label, .option.correct .option-letter {
  color: #155724; /* Dark green text for correct option text */
}
.option.wrong { /* Specifically for student's selected AND wrong answer */
  border-color: #dc3545;
  background-color: #c49898; /* Light red background */
}
.option.wrong .option-label, .option.wrong .option-letter {
  color: #721c24; /* Dark red text for wrong selected option */
}

.option-feedback.missed-correct {
  color: #ff8800; /* Orange for missed correct option */
}
.option-feedback.chosen-wrong {
  color: #c00; /* Strong red for chosen wrong option */
}
.option-feedback.chosen-correct {
  color: #107700; /* Strong green for chosen correct option */
  font-weight: bold;
}

.no-options-info {
  font-style: italic;
  color: #888;
  margin-left: calc(1.5em + 8px);
  font-size: 0.9em;
}

/* Inline Score Edit Panel */
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
.score-edit-input.form-control { /* Use form-control for consistency */
  flex-grow: 1;
  height: 38px; /* Slightly smaller for inline edit */
  text-align: center;
}
.save-score-btn.btn { /* Button to save individual score */
  padding: 8px 15px;
  font-size: 0.9em;
  flex-shrink: 0; /* Prevent button from shrinking */
}


/* --- Loading/No Results States --- */
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
  .student-exam-detail-page { padding: 20px 15px; }
  .page-actions-bar { flex-direction: column; gap: 10px; align-items: stretch; }
  .page-actions-bar .btn { width: 100%; }
  h1 { font-size: 1.8em; }
  .exam-meta-info { flex-direction: column; align-items: center; text-align: center; gap: 5px;}
  .exam-meta-info .meta-item { margin: 0; }


  .section-header { flex-direction: column; align-items: stretch; gap: 10px;}
  .section-header .section-title { margin-bottom: 0; }
  .section-header .edit-scores-toggle-btn { width: 100%; }


  .question-list-section { padding: 20px; }
  .question-header { flex-direction: column; align-items: flex-start; gap: 8px; }
  .question-text-title { font-size: 1em; }
  .question-score-display { margin-left: 0; margin-top: 8px; font-size: 0.9em;}

  .options { margin-left: 0; }
  .option-stats { margin-left: 10px; } /* Reduce indent on mobile */
}

/* Placeholder Icons */
.icon-back-arrow::before { content: "←"; margin-right: 6px; font-weight: bold; }
.icon-upload::before { content: "↑"; margin-right: 6px;}
.icon-edit-pen::before { content: "✏️"; margin-right: 6px;}
.icon-cancel::before { content: "🚫"; margin-right: 6px;}
.icon-check-mark::before { content: "✔"; margin-right: 4px; font-weight: bold; }

</style>